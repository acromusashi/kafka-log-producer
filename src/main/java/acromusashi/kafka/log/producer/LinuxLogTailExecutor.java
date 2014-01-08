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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acromusashi.kafka.log.producer.util.KeyedMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

/**
 * コマンドを実行して、kafka producerで送信するクラス
 * 
 * @author hiroki
 */
public class LinuxLogTailExecutor implements Runnable
{
    /** ロガー */
    private static final Logger                             logger                = LoggerFactory.getLogger(LinuxLogTailExecutor.class);

    /** デフォルトの最大送信サイズ */
    private static final int                                DEFAULT_MAX_SEND_SIZE = 100;

    /** KafkaのProducer */
    private kafka.javaapi.producer.Producer<String, String> producer;

    /** 実行するコマンド(tail -F) */
    private String                                          tailCommandStr;

    /** 送信する際にKafkaの入れておく場所の名前 */
    private String                                          topic;

    /** apacheのログフォーマット */
    private String                                          apacheLogFormat;

    /** jsonで送る際の時刻の形式 */
    private String                                          jsonDateFormatStr;

    /** Jacksonを用いた変換マッパーオブジェクト */
    protected transient ObjectMapper                        objectMapper;

    /** producerで一度にsendする最大量(32767を超えるとsendに失敗しエラーが発生する) */
    private int                                             maxSendSize           = DEFAULT_MAX_SEND_SIZE;

    /** LogAgentが配置されたホスト名 */
    private String                                          host                  = "defaultHost";

    /** エンコード */
    private String                                          encoding              = "UTF-8";

    /**
     * 以下のパラメタを指定するコンストラクタ
     * 
     * @param tailCommandStr 実行するコマンド
     * @param topic 送信する際のトピック
     * @param apacheLogFormat apacheのログのフォーマット
     * @param jsonDateFormat jsonで送る際の時刻の形式
     * @param hostName Producerが動作するホスト
     */
    public LinuxLogTailExecutor(String tailCommandStr, String topic, String apacheLogFormat,
            String jsonDateFormat, String hostName)
    {
        this.tailCommandStr = tailCommandStr;
        this.topic = topic;
        this.apacheLogFormat = apacheLogFormat;
        this.jsonDateFormatStr = jsonDateFormat;
        this.host = hostName;
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
        // シグナル等で終了されない限りtailコマンドを継続して実行し続ける
        while (true)
        {
            sendTailedLog();
        }
    }

    /**
     * ログのTailを行い、結果をKafkaBrokerに対して送信する。
     */
    protected void sendTailedLog()
    {
        String[] tailCommandArgs = this.tailCommandStr.split("\\s+");

        BufferedReader tailReader = null;
        Process tailProcess = null;

        try
        {
            tailProcess = new ProcessBuilder(tailCommandArgs).start();
            tailReader = new BufferedReader(new InputStreamReader(tailProcess.getInputStream(),
                    this.encoding));

            String tailedLine = null;
            List<String> tailedLineList = Lists.newArrayList();
            int count = 0;

            while ((tailedLine = tailReader.readLine()) != null)
            {
                tailedLineList.add(tailedLine);
                count++;

                if (count >= this.maxSendSize)
                {
                    List<KeyedMessage<String, String>> messageList = getKeyedMessage(tailedLineList);
                    tailedLineList.clear();
                    this.producer.send(messageList);
                    count = 0;
                }

            }

            List<KeyedMessage<String, String>> messageList = getKeyedMessage(tailedLineList);
            tailedLineList.clear();
            this.producer.send(messageList);
        }
        catch (Exception e)
        {
            logger.error("Failed while running command: " + this.tailCommandStr, e);
            if (e instanceof InterruptedException)
            {
                Thread.currentThread().interrupt();
            }
        }
        finally
        {
            if (tailReader != null)
            {
                IOUtils.closeQuietly(tailReader);
            }
            if (tailProcess != null)
            {
                tailProcess.destroy();
                try
                {
                    tailProcess.waitFor();
                }
                catch (InterruptedException ex)
                {
                    Thread.currentThread().interrupt();
                }
            }
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
}
