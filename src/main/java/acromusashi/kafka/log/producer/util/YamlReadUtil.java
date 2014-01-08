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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

/**
 * Yamlから設定値マップを読み込むユーティリティクラス
 * 
 * @author kimura
 */
public class YamlReadUtil
{
    /**
     * インスタンス化を防止するためのコンストラクタ
     */
    private YamlReadUtil()
    {}

    /**
     * ファイルパスで指定された設定ファイル（Yaml形式）を読み込み、Yaml設定値オブジェクトを返す。
     * 
     * @param filePath 読込先ファイルパス
     * @return 設定ファイルを読みこんだ設定値オブジェクト
     * @throws IOException 入出力例外発生時
     */
    @SuppressWarnings({"unchecked"})
    public static Map<String, Object> readYaml(String filePath) throws IOException
    {
        Map<String, Object> configObject = null;
        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        InputStreamReader steamReader = null;
        File file = new File(filePath);

        try
        {
            inputStream = new FileInputStream(file.getAbsolutePath());
            steamReader = new InputStreamReader(inputStream, "UTF-8");
            configObject = (Map<String, Object>) yaml.load(steamReader);
        }
        catch (Exception ex)
        {
            // ScannerException/IOExceptionが発生する可能性がある。
            // 2つの例外間に継承関係がなく、ハンドリングが同じのためExceptionでキャッチしている。
            throw new IOException(ex);
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
        }

        return configObject;
    }
}
