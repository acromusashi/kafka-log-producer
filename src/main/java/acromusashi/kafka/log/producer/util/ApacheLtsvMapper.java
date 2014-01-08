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

import java.util.HashMap;
import java.util.Map;

/**
 * ApacheLogフォーマットを対応するLTSV用のキーに変換するユーティリティクラス
 * 
 * @author hiroki
 */
public class ApacheLtsvMapper
{
    /** Apacheのログフォーマットをキー、名前をvalueとしたマップ。 */
    private static final Map<String, String> NAME_MAP;

    /**
     * インスタンス化を防止するためのコンストラクタ
     */
    private ApacheLtsvMapper()
    {}

    /**
     * クラス生成時にマッピング定義を生成する。
     */
    static
    {
        NAME_MAP = new HashMap<String, String>();
        NAME_MAP.put("%h", "host");
        NAME_MAP.put("%l", "ident");
        NAME_MAP.put("%u", "user");
        NAME_MAP.put("%t", "time");
        NAME_MAP.put("%r", "req");
        NAME_MAP.put("%>s", "status");
        NAME_MAP.put("%b", "size");
        NAME_MAP.put("%{Referer}i", "referer");
        NAME_MAP.put("%{User-Agent}i", "ua");
        NAME_MAP.put("%D", "reqtime_microsec");
        NAME_MAP.put("%T", "reqtime");
        NAME_MAP.put("%U%q", "uri");
        NAME_MAP.put("%m", "method");
        NAME_MAP.put("%H", "protocol");
    }

    /**
     * ApacheLogFormat中の文字列をLtsvのキーに変換する。
     * 
     * @param apacheFomat ApacheLogFormat中の文字列
     * @return Ltsvのキー
     */
    public static String convertApacheFormatToLtsvKey(String apacheFomat)
    {
        return NAME_MAP.get(apacheFomat);
    }
}
