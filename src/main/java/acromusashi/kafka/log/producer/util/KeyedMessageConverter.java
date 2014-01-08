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
package acromusashi.kafka.log.producer.util;

import java.util.Map;
import java.util.Map.Entry;

import kafka.producer.KeyedMessage;
import acromusashi.kafka.log.producer.entity.ParsedLog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

/**
 * ApacheLogの1行のログをKafka投入用のメッセージに変換するコンバータ
 * 
 * @author kimura
 */
public class KeyedMessageConverter
{
    /**
     * インスタンス化を防止するためのコンストラクタ
     */
    private KeyedMessageConverter()
    {}

    /**
     * ApacheLogの1行の内容をKafka用メッセージに変換する。
     * 
     * @param apacheLogStr ApacheLogの1行
     * @param topic 送信Topic
     * @param host 送信元ホスト
     * @param apacheLogFormat ApacheLogフォーマット
     * @param jsonDateFormatStr Kafka用メッセージに設定する日付フォーマット
     * @return Kafka用メッセージ
     * @throws Exception 変換失敗時
     */
    public static KeyedMessage<String, String> convertToMessage(String apacheLogStr, String topic,
            String host, String apacheLogFormat, String jsonDateFormatStr) throws Exception
    {
        ParsedLog parsedLogObj = ApacheLogParseUtil.getParsedLog(apacheLogStr, apacheLogFormat);

        Map<String, String> parsedLog = parsedLogObj.getParsedLog();
        Map<String, String> jsonMapLog = Maps.newHashMap();
        String strfTimeFormat = parsedLogObj.getStrftimeFormat();

        for (Entry<String, String> apacheLogEntry : parsedLog.entrySet())
        {
            String jsonKey = ApacheLtsvMapper.convertApacheFormatToLtsvKey(apacheLogEntry.getKey());

            //jsonKeyがない場合はstrttimeの形式、timeの場合は、共通の時刻のフォーマットに変換する。
            if (jsonKey == null || jsonKey.equals("") || jsonKey.equals("time"))
            {
                String javaFormatTime = StrftimeFormatMapper.convertStftToDateStr(
                        apacheLogEntry.getValue(), strfTimeFormat, jsonDateFormatStr);

                jsonMapLog.put("time", javaFormatTime);
            }
            else
            {
                jsonMapLog.put(jsonKey, apacheLogEntry.getValue());
            }
        }

        // hostnameを追加
        jsonMapLog.put("hostname", host);

        ObjectMapper mapper = new ObjectMapper();
        String jsonLog = mapper.writeValueAsString(jsonMapLog);

        KeyedMessage<String, String> result = new KeyedMessage<String, String>(topic, host, jsonLog);
        return result;
    }
}
