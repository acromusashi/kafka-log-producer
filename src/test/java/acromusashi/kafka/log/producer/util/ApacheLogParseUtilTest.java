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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import acromusashi.kafka.log.producer.entity.ParsedLog;

/**
 * ApacheLogParseUtilクラスのテストコード
 * 
 * @author hiroki
 */
public class ApacheLogParseUtilTest
{
    /**
     * Apacheログの固定フォーマットを正規表現に変換するテスト。
     */
    @Test
    public void testConvertLogFormat2Regex_固定されたフォーマットを指定()
    {
        //準備
        List<String> eachApacheLogFormat = ApacheLogParseUtil.getEachApacheLogFormat("%h %l %u %t \"%r\" %>s %b %D %T");

        //実施
        String actual = ApacheLogParseUtil.convertLogFormat2Regex(eachApacheLogFormat,
                new ParsedLog(new HashMap<String, String>(), ""));

        System.out.println("actual: " + actual);

        //検証
        assertEquals(
                "([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\S+) (\\d+) (\\d+)",
                actual);
    }

    /**
     * Apacheログのユーザー指定のフォーマット(%{format}t)を正規表現に変換するテスト。
     */
    @Test
    public void testConvertLogFormat2Regex_ユーザー指定のフォーマットを指定()
    {
        //準備
        List<String> eachApacheLogFormat = ApacheLogParseUtil.getEachApacheLogFormat("%h %{%a %p %c}t");

        //実施
        String actual = ApacheLogParseUtil.convertLogFormat2Regex(eachApacheLogFormat,
                new ParsedLog(new HashMap<String, String>(), ""));

        System.out.println("actual: " + actual);

        //検証
        assertEquals(
                "([\\d.]+) (\\S{3}) (\\S{2}) (\\S{3}\\s\\S{3}\\s\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\s\\S{3})",
                actual);
    }

    /**
     * 実際にApacheの固定フォーマットのログを正規表現でparseできるかテスト。
     */
    @Test
    public void testConvertLogFormat2Regex_固定ログをパース()
    {
        //準備
        List<String> eachApacheLogFormat = ApacheLogParseUtil.getEachApacheLogFormat("%h %l %u %t \"%r\" %>s %b %D  %T");

        //実施
        String regex = ApacheLogParseUtil.convertLogFormat2Regex(eachApacheLogFormat,
                new ParsedLog(new HashMap<String, String>(), ""));
        System.out.println("regex: " + regex);

        Pattern pattern = Pattern.compile(regex);

        //得た正規表現からmatcherを作成する。
        Matcher matcher = pattern.matcher("127.0.0.1 - - [30/Aug/2013:14:44:00 +0900] \"GET / HTTP/1.1\" 304 0 500 0");

        List<String> actual = new ArrayList<String>();
        //正規表現とマッチした場合、それぞれの正規表現にマッチした部分を取り出す。
        if (matcher.find())
        {
            int groupCount = matcher.groupCount();
            for (int count = 1; count <= groupCount; count++)
            {
                actual.add(matcher.group(count));
            }
        }

        //検証
        assertEquals("127.0.0.1", actual.get(0));
        assertEquals("-", actual.get(1));
        assertEquals("-", actual.get(2));
        assertEquals("30/Aug/2013:14:44:00 +0900", actual.get(3));
        assertEquals("GET / HTTP/1.1", actual.get(4));
        assertEquals("304", actual.get(5));
        assertEquals("0", actual.get(6));
        assertEquals("500", actual.get(7));
        assertEquals("0", actual.get(8));
    }

    /**
     * Apacheのユーザーが指定できるログ(%{format}t)を実際にparseできるかテスト。
     */
    @Test
    public void testConvertLogFormat2Regex_ユーザー設定ログをパース()
    {
        //準備
        List<String> eachApacheLogFormat = ApacheLogParseUtil.getEachApacheLogFormat("%h %{%l %c %s}t %t");

        //実施
        String regex = ApacheLogParseUtil.convertLogFormat2Regex(eachApacheLogFormat,
                new ParsedLog(new HashMap<String, String>(), ""));
        System.out.println("regex: " + regex);

        Pattern pattern = Pattern.compile(regex);

        //得た正規表現からmatcherを作成する。
        Matcher matcher = pattern.matcher("127.0.0.1 1 Thu Sep 25 22:32:00 EDT 11239874392831 [29/Aug/2013:16:34:27 +0900]");

        List<String> actual = new ArrayList<String>();
        //正規表現とマッチした場合、それぞれの正規表現にマッチした部分を取り出す。
        if (matcher.find())
        {
            int groupCount = matcher.groupCount();
            for (int count = 1; count <= groupCount; count++)
            {
                actual.add(matcher.group(count));
            }
        }

        //検証
        assertEquals("127.0.0.1", actual.get(0));
        assertEquals("1", actual.get(1));
        assertEquals("Thu Sep 25 22:32:00 EDT", actual.get(2));
        assertEquals("11239874392831", actual.get(3));
        assertEquals("29/Aug/2013:16:34:27 +0900", actual.get(4));
    }
}
