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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

/**
 * StrftimeのフォーマットをJavaのDateFormatで使用可能な形式に変換するユーティリティクラス
 * 
 * @author hiroki
 */
public class StrftimeFormatMapper
{
    /** Apacheでデフォルトで使用されるStrftime */
    private static final String              DEFAULT_STRFTIME = "dd/MMM/yyyy:HH:mm:ss Z";

    /** Key:Strftime、Value:Strftimeに対応するDateFromat用文字列 格納Map */
    private static final Map<String, String> FORMAT_MAPPING;

    /**
     * インスタンス化を防止するためのコンストラクタ
     */
    private StrftimeFormatMapper()
    {}

    /**
     * クラス生成時にマッピング定義を生成する。
     */
    static
    {
        FORMAT_MAPPING = Maps.newHashMap();
        FORMAT_MAPPING.put("a", "EEE");
        FORMAT_MAPPING.put("A", "EEEE");
        FORMAT_MAPPING.put("b", "MMM");
        FORMAT_MAPPING.put("B", "MMMM");
        FORMAT_MAPPING.put("c", "EEE MMM d HH:mm:ss yyyy");
        FORMAT_MAPPING.put("d", "dd");
        FORMAT_MAPPING.put("D", "MM/dd/yy");
        FORMAT_MAPPING.put("e", "dd");
        FORMAT_MAPPING.put("F", "yyyy-MM-dd");
        FORMAT_MAPPING.put("g", "yy");
        FORMAT_MAPPING.put("G", "yyyy");
        FORMAT_MAPPING.put("H", "HH");
        FORMAT_MAPPING.put("h", "MMM");
        FORMAT_MAPPING.put("I", "hh");
        FORMAT_MAPPING.put("j", "DDD");
        FORMAT_MAPPING.put("k", "HH");
        FORMAT_MAPPING.put("l", "hh");
        FORMAT_MAPPING.put("m", "MM");
        FORMAT_MAPPING.put("M", "mm");
        FORMAT_MAPPING.put("n", "\n");
        FORMAT_MAPPING.put("p", "a");
        FORMAT_MAPPING.put("P", "a");
        FORMAT_MAPPING.put("r", "hh:mm:ss a");
        FORMAT_MAPPING.put("R", "HH:mm");
        FORMAT_MAPPING.put("S", "ss");
        FORMAT_MAPPING.put("t", "\t");
        FORMAT_MAPPING.put("T", "HH:mm:ss");
        FORMAT_MAPPING.put("V", "ww");
        FORMAT_MAPPING.put("X", "HH:mm:ss");
        FORMAT_MAPPING.put("x", "MM/dd/yy");
        FORMAT_MAPPING.put("y", "yy");
        FORMAT_MAPPING.put("Y", "yyyy");
        FORMAT_MAPPING.put("Z", "z");
        FORMAT_MAPPING.put("z", "Z");
        FORMAT_MAPPING.put("%", "%");
    }

    /**
     * 指定したSTRFTime形式の時刻値をSTRFTimeフォーマット、変換先時刻フォーマットを指定して変換する。
     * 
     * @param timeStr STRFTime形式の時刻値
     * @param strfFormatStr STRFTimeフォーマット
     * @param outputFormatStr 出力時刻フォーマット
     * @return フォーマット変換後の時刻文字列
     * @throws ParseException パース失敗時
     */
    public static String convertStftToDateStr(String timeStr, String strfFormatStr,
            String outputFormatStr) throws ParseException
    {
        DateFormat strfFormat;

        // STRFTimeフォーマットが指定されていない場合はApacheのデフォルト形式を使用
        if (StringUtils.isEmpty(strfFormatStr) == true)
        {
            strfFormat = new SimpleDateFormat(DEFAULT_STRFTIME, Locale.ENGLISH);

        }
        else
        {
            // STRFTimeフォーマットからDateFormatオブジェクトに変換する。
            strfFormat = StrftimeFormatMapper.convertStftToDateFormat(strfFormatStr);
        }

        // STRFTimeフォーマットに対応したDateFormatを用いてDate形式に変換
        Date logDate = strfFormat.parse(timeStr);

        // 出力時刻フォーマット形式の時刻に変換する
        DateFormat outputDateFormat = new SimpleDateFormat(outputFormatStr);
        String returnDate = outputDateFormat.format(logDate);
        return returnDate;
    }

    /**
     * StftTime形式の文字列を指定し、対応したDateFormatオブジェクトに変換する。
     * 
     * @param strfFormat ベースとなるStftTime形式文字列
     * @return 変換結果のDateFormatオブジェクト
     */
    public static DateFormat convertStftToDateFormat(String strfFormat)
    {
        String convertedFormat = convertStrfToDateFormatStr(strfFormat);
        DateFormat dateFormat = new SimpleDateFormat(convertedFormat, Locale.ENGLISH);

        return dateFormat;
    }

    /**
     * StrfTime形式をJavaのDateFormatで解釈可能な形式に変換する。
     * 
     * @param strfFormat StrfTime形式
     * @return JavaのDateFormatで解釈可能な形式
     */
    protected static String convertStrfToDateFormatStr(String strfFormat)
    {
        boolean inside = false;
        boolean mark = false;
        boolean modifiedCommand = false;

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < strfFormat.length(); i++)
        {
            char c = strfFormat.charAt(i);

            if (c == '%' && !mark)
            {
                mark = true;
            }
            else
            {
                if (mark)
                {
                    if (modifiedCommand)
                    {
                        modifiedCommand = false;
                        mark = false;
                    }
                    else
                    {
                        inside = translateCommand(builder, strfFormat, i, inside);
                        if (c == 'O' || c == 'E')
                        {
                            modifiedCommand = true;
                        }
                        else
                        {
                            mark = false;
                        }
                    }
                }
                else
                {
                    if (!inside && c != ' ')
                    {
                        builder.append("'");
                        inside = true;
                    }

                    builder.append(c);
                }
            }
        }

        if (builder.length() > 0)
        {
            char lastChar = builder.charAt(builder.length() - 1);

            if (lastChar != '\'' && inside)
            {
                builder.append('\'');
            }
        }
        return builder.toString();
    }

    /**
     * バックスラッシュで囲むためのメソッド。
     * 
     * @param str 対象の文字列
     * @param insideQuotes クオートの中にあることを表すboolean
     * @return retVal クオートの中になければ、バックスラッシュをつけたものになる。
     */
    protected static String quote(String str, boolean insideQuotes)
    {
        String retVal = str;
        if (!insideQuotes)
        {
            retVal = '\'' + retVal + '\'';
        }
        return retVal;
    }

    /**
     * StrfTimeのパーツをJavaのDateFormatのパーツに変換する。
     *
     * @param oldInside すでに中に入っていたかどうか
     * @param index インデックス
     * @param pattern 変換対象パーツ
     * @param builder 作成途中のJavaフォーマット
     * @return Javaで使用する形式
     */
    protected static boolean translateCommand(StringBuilder builder, String pattern, int index,
            boolean oldInside)
    {
        char firstChar = pattern.charAt(index);
        boolean newInside = oldInside;

        if (firstChar == 'O' || firstChar == 'E')
        {
            if (index + 1 < pattern.length())
            {
                newInside = translateCommand(builder, pattern, index + 1, oldInside);
            }
            else
            {
                builder.append(quote("%" + firstChar, oldInside));
            }
        }
        else
        {
            String command = FORMAT_MAPPING.get(String.valueOf(firstChar));

            if (command == null)
            {
                builder.append(quote("%" + firstChar, oldInside));
            }
            else
            {
                if (oldInside)
                {
                    builder.append('\'');
                }
                builder.append(command);
                newInside = false;
            }
        }
        return newInside;
    }
}
