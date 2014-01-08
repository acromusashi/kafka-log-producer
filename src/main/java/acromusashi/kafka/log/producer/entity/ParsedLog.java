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
package acromusashi.kafka.log.producer.entity;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * parseしたApacheログとstrftimeのフォーマットを持つエンティティ
 * 
 * @author hiroki
 */
public class ParsedLog
{
    /** parserされたログのマップ */
    private Map<String, String> parsedLog;

    /** strftimeのフォーマット */
    private String              strftimeFormat;

    /**
     * parsedLogとstrftimeFormatを指定するコンストラクタ。
     * 
     * @param parsedLog パースされたログ
     * @param strftimeFormat strftimeのフォーマット
     */
    public ParsedLog(Map<String, String> parsedLog, String strftimeFormat)
    {
        this.parsedLog = parsedLog;
        this.strftimeFormat = strftimeFormat;
    }

    /**
     * parsedLogのゲッター
     * 
     * @return parsedLog パースされたログ
     */
    public Map<String, String> getParsedLog()
    {
        return this.parsedLog;
    }

    /**
     * parsedLogのセッター
     * 
     * @param parsedLog パースされたログ
     */
    public void setParsedLog(Map<String, String> parsedLog)
    {
        this.parsedLog = parsedLog;
    }

    /**
     * strftimeFormatのゲッター
     * 
     * @return strftimeFormat strftimeのフォーマット
     */
    public String getStrftimeFormat()
    {
        return this.strftimeFormat;
    }

    /**
     * strftimeFormatのセッター
     * 
     * @param strftimeFormat strftimeのフォーマット
     */
    public void setStrftimeFormat(String strftimeFormat)
    {
        this.strftimeFormat = strftimeFormat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        String result = ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        return result;
    }
}
