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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import acromusashi.kafka.log.producer.entity.ParsedLog;

import com.google.common.collect.Maps;

/**
 * Apacheのログをパースし、JSON変換用のエンティティを返すユーティリティクラス
 * 
 * @author kimura
 */
public class ApacheLogParseUtil
{
    /** Apacheのログフォーマットをキー、正規表現をvalueとしたマップ */
    private static final Map<String, String> REGEX_APACHE_MAP;

    /** StrfTimeのログフォーマットをキー、正規表現をvalueとしたマップ。 */
    private static final Map<String, String> REGEX_STRF_MAP;

    /**
     * インスタンス化を防止するためのコンストラクタ
     */
    private ApacheLogParseUtil()
    {}

    /**
     * クラス生成時にマッピング定義を生成する。
     */
    static
    {
        REGEX_APACHE_MAP = Maps.newHashMap();
        REGEX_APACHE_MAP.put("%h", "([\\d.]+)");
        REGEX_APACHE_MAP.put("%l", "(\\S+)");
        REGEX_APACHE_MAP.put("%u", "(\\S+)");
        REGEX_APACHE_MAP.put("%t", "\\[([\\w:/]+\\s[+\\-]\\d{4})\\]");
        REGEX_APACHE_MAP.put("%r", "\"(.+?)\"");
        REGEX_APACHE_MAP.put("%>s", "(\\d{3})");
        REGEX_APACHE_MAP.put("%b", "(\\S+)");
        REGEX_APACHE_MAP.put("%{Referer}i", "\"(.*?)\"");
        REGEX_APACHE_MAP.put("%{User-Agent}i", "\"(.*?)\"");
        REGEX_APACHE_MAP.put("%D", "(\\d+)");
        REGEX_APACHE_MAP.put("%T", "(\\d+)");
        REGEX_APACHE_MAP.put("%U%q", "(\\S+)");
        REGEX_APACHE_MAP.put("%m", "(\\S+)");
        REGEX_APACHE_MAP.put("%H", "(\\S+)");

        REGEX_STRF_MAP = Maps.newHashMap();
        REGEX_STRF_MAP.put("%a", "(\\S{3})");
        REGEX_STRF_MAP.put("%A", "(\\S+?)");
        REGEX_STRF_MAP.put("%b", "(\\S{3})");
        REGEX_STRF_MAP.put("%B", "(\\S+?)");
        REGEX_STRF_MAP.put("%c", "(\\S{3}\\s\\S{3}\\s\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\s\\S{3})");
        REGEX_STRF_MAP.put("%C", "(\\d{2})");
        REGEX_STRF_MAP.put("%d", "(\\d{2})");
        REGEX_STRF_MAP.put("%D", "(\\d{2}/\\d{2}/\\d{2})");
        REGEX_STRF_MAP.put("%e", "(\\d{2})");
        REGEX_STRF_MAP.put("%F", "(\\d{4}-\\d{2}-\\d{2})");
        REGEX_STRF_MAP.put("%G", "(\\d{4})");
        REGEX_STRF_MAP.put("%g", "(\\d{2})");
        REGEX_STRF_MAP.put("%h", "(\\S{3})");
        REGEX_STRF_MAP.put("%H", "(\\d{2})");
        REGEX_STRF_MAP.put("%I", "(\\d{2})");
        REGEX_STRF_MAP.put("%j", "(\\d{3})");
        REGEX_STRF_MAP.put("%k", "(\\S+?)");
        REGEX_STRF_MAP.put("%l", "(\\S+?)");
        REGEX_STRF_MAP.put("%m", "(\\d{2})");
        REGEX_STRF_MAP.put("%M", "(\\d{2})");
        REGEX_STRF_MAP.put("%p", "(\\S{2})");
        REGEX_STRF_MAP.put("%P", "(\\S{2})");
        REGEX_STRF_MAP.put("%r", "(\\d{2}:\\d{2}:\\d{2}\\s\\S{2})");
        REGEX_STRF_MAP.put("%R", "(\\d{2}:\\d{2})");
        REGEX_STRF_MAP.put("%s", "(\\d+?)");
        REGEX_STRF_MAP.put("%S", "(\\d{2})");
        REGEX_STRF_MAP.put("%T", "(\\d{2}:\\d{2}:\\d{2})");
        REGEX_STRF_MAP.put("%u", "(\\d{1})");
        REGEX_STRF_MAP.put("%U", "(\\d{2})");
        REGEX_STRF_MAP.put("%V", "(\\d{2})");
        REGEX_STRF_MAP.put("%w", "(\\d{1})");
        REGEX_STRF_MAP.put("%x", "(\\d{2}/\\d{2}/\\d{2})");
        REGEX_STRF_MAP.put("%X", "(\\d{2}:\\d{2}:\\d{2})");
        REGEX_STRF_MAP.put("%y", "(\\d{2})");
        REGEX_STRF_MAP.put("%Y", "(\\d{4})");
        REGEX_STRF_MAP.put("%z", "(\\S\\d+?)");
        REGEX_STRF_MAP.put("%Z", "(\\S{3})");
    }

    /**
     * Apacheのログをparseしてmapにして返す。
     * 
     * @param apacheLogFormat apacheのログフォーマット
     * @param log Apacheのログ
     * @return ログをparseしたものをMapにしたもの
     */
    public static ParsedLog getParsedLog(String log, String apacheLogFormat)
    {
        List<String> eachApacheLogFormat = getEachApacheLogFormat(apacheLogFormat);
        ParsedLog parsedLog = new ParsedLog(new HashMap<String, String>(), "");
        String regex = convertLogFormat2Regex(eachApacheLogFormat, parsedLog);

        Pattern pattern = Pattern.compile(regex);

        //得た正規表現からmatcherを作成する。
        Matcher matcher = pattern.matcher(log);

        List<String> eachLog = new ArrayList<String>();
        //正規表現とマッチした場合、それぞれの正規表現にマッチした部分を取り出す。
        if (matcher.find())
        {
            int groupCount = matcher.groupCount();
            for (int count = 1; count <= groupCount; count++)
            {
                eachLog.add(matcher.group(count));
            }
        }

        Map<String, String> returnMap = new HashMap<String, String>();
        int logSize = eachLog.size();
        for (int index = 0; index < logSize; index++)
        {
            returnMap.put(eachApacheLogFormat.get(index), eachLog.get(index));
        }

        parsedLog.setParsedLog(returnMap);

        return parsedLog;
    }

    /**
     * apacheのログフォーマットから個々のフォーマットを取り出す。
     * 
     * @param apacheLogFormat apacheのログフォーマット
     * @return eachApacheLogFormat 個々のフォーマット
     */
    protected static List<String> getEachApacheLogFormat(String apacheLogFormat)
    {
        String formatParseRegex = "((%U%q)|(%>s)|(%[a-zA-Z])|(%\\{.*?\\}[a-zA-Z]))";
        Pattern formatParsePattern = Pattern.compile(formatParseRegex);

        //個々のフォーマットを取り出す。
        Matcher matcher = formatParsePattern.matcher(apacheLogFormat);

        List<String> eachApacheLogFormat = new ArrayList<String>();
        while (matcher.find())
        {
            eachApacheLogFormat.add(matcher.group(1));
        }
        return eachApacheLogFormat;
    }

    /**
     * apacheのログフォーマットを正規表現に変換する。
     * 
     * @param eachApacheLogFormat コンフィグ
     * @param parsedLog ParsedLogオブジェクト
     * @return returnRegex 正規表現
     */
    protected static String convertLogFormat2Regex(List<String> eachApacheLogFormat,
            ParsedLog parsedLog)
    {
        StringBuilder regexBuilder = new StringBuilder();
        String strfTimeRegex = "%\\{.*\\}t";
        Pattern strfTimePattern = Pattern.compile(strfTimeRegex);
        //個々のログフォーマットをkeyに対応する正規表現を取り出しつなげていく。
        for (String format : eachApacheLogFormat)
        {
            if (strfTimePattern.matcher(format).find())
            {
                //フォーマットがstrftimeの場合はconvertFormatTime2Regexを用いて正規表現化する。
                regexBuilder.append(convertFormatTime2Regex(format, parsedLog) + " ");
            }
            else
            {
                regexBuilder.append(REGEX_APACHE_MAP.get(format) + " ");
            }

        }

        String returnRegex = regexBuilder.toString();
        int regexLength = returnRegex.length();
        if (regexLength > 2)
        {
            returnRegex = returnRegex.substring(0, regexLength - 1);
        }

        return returnRegex;
    }

    /**
     * strftime形式の時刻を正規表現に変換する。
     * 
     * @param strftimeFormat strftimeのフォーマット
     * @param parsedLog ParsedLogオブジェクト
     * @return returnRegex 対応する正規表現
     */
    protected static String convertFormatTime2Regex(String strftimeFormat, ParsedLog parsedLog)
    {
        String strftime = getStrftimeFormat(strftimeFormat);
        parsedLog.setStrftimeFormat(strftimeFormat);

        for (Entry<String, String> formatEntry : REGEX_STRF_MAP.entrySet())
        {
            Pattern pattern = Pattern.compile(formatEntry.getKey());
            Matcher match = pattern.matcher(strftime);
            if (match.find())
            {
                String regex = formatEntry.getValue();
                strftime = strftime.replace(formatEntry.getKey(), regex);
            }
        }
        return strftime;
    }

    /**
     * %{???}tの中から???をstrftimeのフォーマットである???を取り出す
     * 
     * @param strfTime %{???}tという文字列
     * @return strfTimeFormat ???の部分 
     */
    private static String getStrftimeFormat(String strfTime)
    {
        String parseRegex = "(%\\{)(.*)(\\}t)";
        Pattern strfPattern = Pattern.compile(parseRegex);
        Matcher matcher = strfPattern.matcher(strfTime);
        //フォーマットで定義されたものを、replaceで正規表現に変えていく。
        String strfTimeFormat = "";

        if (matcher.find())
        {
            strfTimeFormat = matcher.group(2);
        }

        return strfTimeFormat;
    }
}
