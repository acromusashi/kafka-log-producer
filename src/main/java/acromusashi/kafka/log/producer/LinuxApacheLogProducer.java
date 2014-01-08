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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

import acromusashi.kafka.log.producer.util.ProducerConfigConverter;
import acromusashi.kafka.log.producer.util.YamlReadUtil;

/**
 * Linux上のログをTailして随時KafkaBrokerに送信するKafkaProducer
 * 
 * @author kimura
 */
public class LinuxApacheLogProducer
{
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(LinuxApacheLogProducer.class);

    /**
     * パラメータを指定せずにインスタンスを生成する。
     */
    public LinuxApacheLogProducer()
    {}

    /**
     * プログラムエントリポイント<br/>
     * <br/>
     * 下記の引数/オプションを使用する。<br/>
     * <ul>
     * <li>-c LinuxApacheLogProducer用設定ファイルパス(必須入力）</li>
     * <li>-h ヘルプ表示</li>
     * </ul>
     * 
     * @param args 起動引数
     */
    public static void main(String... args)
    {
        LinuxApacheLogProducer producer = new LinuxApacheLogProducer();
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
            help.printHelp(LinuxApacheLogProducer.class.getName(), cliOptions, true);
            return;
        }

        if (commandLine.hasOption("h"))
        {
            // ヘルプオプションが指定されていた場合にはヘルプを表示して終了
            help.printHelp(LinuxApacheLogProducer.class.getName(), cliOptions, true);
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
    protected void startTailLog(Map<String, Object> configMap)
    {
        String tailCommand = configMap.get("tail.command").toString();
        String tailPath = configMap.get("tail.path").toString();
        String tailCommandStr = tailCommand + " " + tailPath;
        String kafkaTopic = configMap.get("kafka.topic").toString();
        String apacheLogFormat = configMap.get("apachelog.format").toString();
        String jsonDateFormat = configMap.get("jsondate.format").toString();

        String hostname = "defaultHost";

        try
        {
            hostname = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException ex)
        {
            logger.warn("HostName resolve failed. Use default. : default=" + hostname, ex);
        }

        ProducerConfig producerConfig = ProducerConfigConverter.convertToProducerConfig(configMap);

        logger.info("Producer starting. Command=" + tailCommandStr);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        LinuxLogTailExecutor executor = new LinuxLogTailExecutor(tailCommandStr, kafkaTopic,
                apacheLogFormat, jsonDateFormat, hostname);
        executor.initialize(producerConfig);

        executorService.execute(executor);

        logger.info("Producer started");
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
        OptionBuilder.withArgName("LinuxApacheLogProducer Conf Path");
        OptionBuilder.withDescription("LinuxApacheLogProducer Conf Path");
        OptionBuilder.isRequired(true);
        Option confPathOption = OptionBuilder.create("c");

        // ヘルプオプション
        OptionBuilder.withDescription("show help");
        Option helpOption = OptionBuilder.create("h");

        cliOptions.addOption(confPathOption);
        cliOptions.addOption(helpOption);
        return cliOptions;
    }
}
