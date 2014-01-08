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
import java.util.Properties;

import kafka.producer.ProducerConfig;

/**
 * Yamlから読み込んだ設定値Mapを基にKafkaProducer用の設定を生成するクラス
 * 
 * @author kimura
 */
public class ProducerConfigConverter
{
    /**
     * インスタンス化を防止するためのコンストラクタ
     */
    private ProducerConfigConverter()
    {}

    /**
     * Yamlから読み込んだ設定マップオブジェクトをKafkaProducer用設定に変換する。
     * 
     * @param yamlConf Yamlから読み込んだ設定マップオブジェクト
     * @return KafkaProducer用設定
     */
    public static ProducerConfig convertToProducerConfig(Map<String, Object> yamlConf)
    {
        String serializerClass = yamlConf.get("kafka.serializer.class").toString();
        String compressionCodec = yamlConf.get("kafka.compression.codec").toString();
        String brokerList = yamlConf.get("kafka.broker.list").toString();

        Properties props = new Properties();
        props.put("serializer.class", serializerClass);
        props.put("compression.codec", compressionCodec);
        props.put("metadata.broker.list", brokerList);

        ProducerConfig result = new ProducerConfig(props);
        return result;
    }
}
