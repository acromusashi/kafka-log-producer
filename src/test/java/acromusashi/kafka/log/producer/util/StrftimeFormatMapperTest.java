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

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.junit.Test;

/**
 * StrftimeFormatMapperクラスのテストコード
 * 
 * @author kimura
 */
public class StrftimeFormatMapperTest
{
    /**
     * Apacheデフォルト時刻形式「dd/MMM/yyyy:HH:mm:ss Z」がパース可能であることを確認する。
     * 
     * @target {@link StrftimeFormatMapper#convertStftToDateStr(String, String, String)}
     * @test パースが行われること。
     *    condition:: 時刻とデフォルトフォーマット、出力フォーマットを指定し、パースを実施。
     *    result:: パースが行われること。
     */
    @Test
    public void testConvertStftToDateStr_デフォルト形式() throws ParseException
    {
        // 準備
        String timeStr = "08/Jan/2014:15:32:30 +0900";
        // strfFormatStrを空で指定すると「dd/MMM/yyyy:HH:mm:ss Z」(デフォルト値)として使用される。
        String strfFormatStr = "";
        String outputFormat = "yyyy-MM-dd'T'HH:mm:ssZ";

        String expected = "2014-01-08T15:32:30+0900";

        // 実施
        String actual = StrftimeFormatMapper.convertStftToDateStr(timeStr, strfFormatStr,
                outputFormat);

        // 検証
        assertEquals(expected, actual);
    }

}
