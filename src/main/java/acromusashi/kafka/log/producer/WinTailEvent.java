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

import java.util.EventObject;

public class WinTailEvent extends EventObject
{
    /** serialVersionUID */
    private static final long serialVersionUID = 8989765755987666300L;

    /** 受信データ */
    private final byte[]      tail;

    /**
     * イベント受信元と受信データを指定してインスタンスを生成する。
     * 
     * @param source イベント受信元
     * @param tail 受信データ
     */
    public WinTailEvent(WinTailThread source, byte[] tail)
    {
        super(source);

        if (tail == null)
        {
            throw new NullPointerException("tail");
        }

        this.tail = tail;
    }

    /**
     * @return the tail
     */
    public byte[] getTail()
    {
        return this.tail;
    }
}
