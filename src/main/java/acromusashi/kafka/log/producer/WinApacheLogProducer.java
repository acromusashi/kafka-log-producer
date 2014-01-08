/**
* Copyright (c) Acroquest Technology Co, Ltd. All Rights Reserved.
* Please read the associated COPYRIGHTS file for more details.
*
* THE SOFTWARE IS PROVIDED BY Acroquest Technolog Co., Ltd.,
* WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
* BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDER BE LIABLE FOR ANY
* CLAIM, DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
* OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
*/
package acromusashi.kafka.log.producer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acromusashi.kafka.log.producer.util.KeyedMessageConverter;
import acromusashi.kafka.log.producer.util.ProducerConfigConverter;
import acromusashi.kafka.log.producer.util.YamlReadUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

/**
 * Windows版のKafkaProducer。
 * ファイルのローテートに対応した。
 *
 * @author hiroki
 */
public class WinApacheLogProducer extends Thread
{
    /** Logger */
    private static final Logger                             logger                = LoggerFactory.getLogger(WinApacheLogProducer.class);

    /** デフォルトの確保サイズ */
    private static final int                                DEFAULT_ALLOCATE_SIZE = 65535;

    /** KafkaのProducer */
    private kafka.javaapi.producer.Producer<String, String> producer;

    /** 送信する際にKafkaの入れておく場所の名前 */
    private String                                          topic;

    /** イベントリスナー */
    private EventListenerList                               listenerList          = new EventListenerList();

    /** ファイルの末尾 */
    private long                                            tailPos;

    /** ログファイルがある場所のパス */
    private String                                          path;

    /** LogAgentが配置されたホスト名 */
    private String                                          host;

    /** ローテートして次に読み込むログファイル */
    private List<String>                                    newFileName           = new ArrayList<String>();

    /** ログを収集する対象のファイル */
    private File                                            targetFile;

    /** 収集するログファイルのパターン */
    private String                                          logPattern            = "access_[0-9][0-9][0-9][0-9][0-9].*";

    /** apacheのログフォーマット */
    private String                                          apacheLogFormat;

    /** Jacksonを用いた変換マッパーオブジェクト */
    protected transient ObjectMapper                        objectMapper;

    /** 接続をログファイルから拒否されたときにリトライする回数 */
    private int                                             retryNum;

    /** jsonで送る際の時刻のフォーマット */
    private String                                          jsonDateFormatStr;

    /** エンコード */
    private String                                          encoding              = "UTF-8";

    /**
     * パラメータを指定せずにインスタンスを生成する。
     */
    public WinApacheLogProducer()
    {}

    /**
     * プログラムエントリポイント<br/>
     * <br/>
     * 下記の引数/オプションを使用する。<br/>
     * <ul>
     * <li>-c WinApacheLogProducer用設定ファイルパス(必須入力）</li>
     * <li>-h ヘルプ表示</li>
     * </ul>
     * 
     * @param args 起動引数
     */
    public static void main(String... args)
    {
        WinApacheLogProducer producer = new WinApacheLogProducer();
        producer.startProducer(args);
    }

    /**
     * 指定された起動引数を基にProducerを開始する。
     * 
     * @param args 起動引数
     */
    protected void startProducer(String... args)
    {
        Options cliOptions = createOptions();
        CommandLineParser parser = new PosixParser();
        CommandLine commandLine = null;
        HelpFormatter help = new HelpFormatter();

        try
        {
            commandLine = parser.parse(cliOptions, args);
        }
        catch (ParseException pex)
        {
            help.printHelp(WinApacheLogProducer.class.getName(), cliOptions, true);
            return;
        }

        if (commandLine.hasOption("h"))
        {
            // ヘルプオプションが指定されていた場合にはヘルプを表示して終了
            help.printHelp(WinApacheLogProducer.class.getName(), cliOptions, true);
            return;
        }

        // コマンドラインから設定値を取得
        String confPath = commandLine.getOptionValue("c");

        Map<String, Object> configMap = null;

        try
        {
            configMap = YamlReadUtil.readYaml(confPath);
        }
        catch (IOException ex)
        {
            // 読み込み失敗した場合は終了する
            ex.printStackTrace();
            return;
        }

        startTailLog(configMap);
    }

    /**
     * 指定された設定値を用いてLogのTailを開始する。
     * 
     * @param configMap 設定値格納Map
     */
    private void startTailLog(Map<String, Object> configMap)
    {
        this.path = configMap.get("tail.target.dir").toString();
        this.topic = configMap.get("kafka.topic").toString();
        this.apacheLogFormat = configMap.get("apachelog.format").toString();
        this.jsonDateFormatStr = configMap.get("jsondate.format").toString();

        this.host = "defaultHost";

        try
        {
            this.host = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException ex)
        {
            logger.warn("HostName resolve failed. Use default. : default=" + this.host, ex);
        }

        ProducerConfig producerConfig = ProducerConfigConverter.convertToProducerConfig(configMap);
        initialize(producerConfig);
        start();
    }

    /**
     * KafkaProducer用のConfigオブジェクトを指定し、KafkaProducerを初期化する。
     * 
     * @param config KafkaProducerConfig
     */
    public void initialize(ProducerConfig config)
    {
        this.objectMapper = new ObjectMapper();
        this.producer = new Producer<>(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run()
    {
        while (true)
        {
            File accessLogFile = new File(this.path);
            tailRun(accessLogFile);
        }
    }

    /**
     * tailでファイルを読み込む。
     *
     * @param file 対象のログファイル
     */
    public void tailRun(File file)
    {
        try (WatchService watcher = FileSystems.getDefault().newWatchService())
        {
            Path parentDir = file.toPath().getParent();
            parentDir.register(watcher, ENTRY_MODIFY);
            parentDir.relativize(file.toPath());
            List<String> targetFileNames = getTargetLogFiles(Lists.newArrayList(parentDir.toFile().list()));
            Collections.sort(targetFileNames);
            int logFileNameSize = targetFileNames.size();
            this.targetFile = new File(parentDir + "/" + targetFileNames.get(logFileNameSize - 1));

            while (true)
            {
                WatchKey key = watcher.take();
                for (WatchEvent<?> event : key.pollEvents())
                {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == OVERFLOW)
                    {
                        logger.warn("OVERFLOW");
                        continue;
                    }
                    byte[] tail = null;
                    boolean noRetry = false;
                    for (int retryCount = 0; retryCount < this.retryNum; retryCount++)
                    {
                        try
                        {
                            tail = getTail(this.targetFile);
                            break;
                        }
                        catch (IOException ex)
                        {
                            if (retryCount == this.retryNum - 1)
                            {
                                noRetry = true;
                            }
                        }
                    }
                    // リトライ上限に達してもログを取得できなかった場合は、ループを抜ける
                    if (noRetry)
                    {
                        break;
                    }
                    List<String> allFileName = getTargetLogFiles(Arrays.asList(parentDir.toFile().list()));
                    Collections.sort(allFileName);
                    int allFileNameSize = allFileName.size();
                    if (tail.length > 0)
                    {
                        String inputStr = new String(tail, this.encoding);
                        if (!allFileName.equals(targetFileNames))
                        {
                            this.newFileName.add(allFileName.get(allFileNameSize - 1));
                            targetFileNames = allFileName;
                        }

                        List<String> eachStr = Arrays.asList(inputStr.split(System.getProperty("line.separator")));
                        List<KeyedMessage<String, String>> list = getKeyedMessage(eachStr);
                        this.producer.send(list);
                    }
                    else
                    {
                        if (!allFileName.equals(targetFileNames))
                        {
                            this.newFileName.add(allFileName.get(allFileNameSize - 1));
                            targetFileNames = allFileName;
                        }
                        if (this.newFileName.size() > 0)
                        {
                            this.targetFile = new File(parentDir + "/" + this.newFileName.get(0));
                            this.newFileName.remove(0);
                            targetFileNames = allFileName;
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid)
                {
                    break;
                }
            }
        }
        catch (Exception ex)
        {
            // FindBugsがJava7構文に対応していないためFindBugsでエラーが出るが、実際には必要な例外処理
            logger.error("Failed start producer. ", ex);
        }
    }

    /**
     * 送信するためのkeyedMessageListを作成する。
     * 
     * @param eachStr 実際の複数のログ
     * @return list keyedMessageのリスト
     */
    protected List<KeyedMessage<String, String>> getKeyedMessage(List<String> eachStr)
    {
        List<KeyedMessage<String, String>> list = Lists.newArrayList();
        for (String apacheLogStr : eachStr)
        {
            KeyedMessage<String, String> convertedMessage = null;

            try
            {
                convertedMessage = KeyedMessageConverter.convertToMessage(apacheLogStr, this.topic,
                        this.host, this.apacheLogFormat, this.jsonDateFormatStr);
            }
            catch (Exception ex)
            {
                logger.warn("Log convert failed. Dispose log message. Log=" + apacheLogStr, ex);
                continue;
            }

            list.add(convertedMessage);
        }

        return list;
    }

    /**
     * ログファイルの中から、収集対象のログファイルを収集する。
     *
     * @param candidateFiles ログファイル
     * @return returnLogFiles 対象のログファイル
     */
    private List<String> getTargetLogFiles(List<String> candidateFiles)
    {
        int candidateFilesSize = candidateFiles.size();
        List<String> returnLogFiles = new ArrayList<String>();

        for (int index = 0; index < candidateFilesSize; index++)
        {
            String candidateFileName = candidateFiles.get(index);
            if (candidateFileName.matches(this.logPattern))
            {
                returnLogFiles.add(candidateFileName);
            }
        }

        return returnLogFiles;
    }

    /**
     * ファイルに追記された分を返す。
     *
     * @param file 対象のファイル
     * @return ファイルに追記された分のbyte配列
     * @throws IOException エラー
     */
    private byte[] getTail(File file) throws IOException
    {
        byte[] tail = new byte[0];
        try (RandomAccessFile random = new RandomAccessFile(file, "r"))
        {
            if (this.tailPos < random.length())
            {
                random.seek(this.tailPos);

                tail = readToEnd(random);
            }
            this.tailPos = random.length();
        }
        return tail;
    }

    /**
     * ファイルの末尾までを返す。
     *
     * @param random ファイル
     * @return ファイルの末尾までの配列
     * @throws IOException 例外
     */
    private byte[] readToEnd(RandomAccessFile random) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_ALLOCATE_SIZE);

        while ((random.getChannel().read(buffer)) != -1)
        {
            out.write(buffer.array());
            buffer.clear();
        }

        return out.toByteArray();
    }

    /**
     * コマンドライン解析用のオプションを生成
     * 
     * @return コマンドライン解析用のオプション
     */
    public static Options createOptions()
    {
        Options cliOptions = new Options();

        // 定義ファイルパスオプション
        OptionBuilder.hasArg(true);
        OptionBuilder.withArgName("WinApacheLogProducer Conf Path");
        OptionBuilder.withDescription("WinApacheLogProducer Conf Path");
        OptionBuilder.isRequired(true);
        Option confPathOption = OptionBuilder.create("c");

        // ヘルプオプション
        OptionBuilder.withDescription("show help");
        Option helpOption = OptionBuilder.create("h");

        cliOptions.addOption(confPathOption);
        cliOptions.addOption(helpOption);
        return cliOptions;
    }

    /**
     * tail用のlisetenerを追加する。
     *
     * @param listener リスナー
     */
    public void addTailListener(WinTailEventListener listener)
    {
        this.listenerList.add(WinTailEventListener.class, listener);
    }

    /**
     * tail用のlistenerを取り除く。
     *
     * @param listener リスナー
     */
    public void removeTailListener(WinTailEventListener listener)
    {
        this.listenerList.remove(WinTailEventListener.class, listener);
    }
}
