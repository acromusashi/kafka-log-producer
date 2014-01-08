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

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javax.swing.event.EventListenerList;

/**
* Windowsにおいてログの末尾を追尾するスレッド。<br>
* 指定したファイルの末尾に追記が行われると、TailEventを発行する。<br>
* TailEvent には、前回からの差分を含む。
*
* @author hiroki
*/
public class WinTailThread implements Runnable
{
    /** イベントリスナー */
    private EventListenerList listenerList = new EventListenerList();

    /** 監視対象のファイル */
    private final File        file;

    /** ファイルの末尾 */
    private long              tailPos;

    /**
     * ファイルオブジェクトを指定してインスタンスを生成する。
     * 
     * @param file ファイルオブジェクト
     */
    public WinTailThread(File file)
    {
        if (file == null)
        {
            throw new NullPointerException("file");
        }
        assert file.exists();
        assert file.isFile();

        this.file = file;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run()
    {
        try (WatchService watcher = FileSystems.getDefault().newWatchService())
        {
            Path parentDir = this.file.toPath().getParent();
            parentDir.register(watcher, ENTRY_MODIFY);

            parentDir.relativize(this.file.toPath());
            resetPos();

            while (true)
            {
                // wait for key to be signaled
                WatchKey key = watcher.take();

                for (WatchEvent<?> event : key.pollEvents())
                {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == OVERFLOW)
                    {
                        continue;
                    }

                    @SuppressWarnings({"unchecked"})
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path eventFile = ev.context();

                    if (this.file.toPath().endsWith(eventFile))
                    {
                        byte[] tail = getTail();

                        if (tail.length != 0)
                        {
                            notifyTailEvent(new WinTailEvent(this, tail));
                        }
                    }
                }

                // Reset the key -- this step is critical to receive further watch events.
                boolean valid = key.reset();
                if (!valid)
                {
                    break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Positionをリセットする。
     * 
     * @throws IOException 入出力エラー発生時
     */
    private void resetPos() throws IOException
    {
        try (RandomAccessFile random = new RandomAccessFile(this.file, "r"))
        {
            this.tailPos = random.length();
        }
    }

    /**
     * 末尾のデータを取得する。
     * 
     * @return 末尾のデータ
     * @throws IOException 入出力エラー発生時
     */
    private byte[] getTail() throws IOException
    {
        byte[] tail = new byte[0];
        try (RandomAccessFile random = new RandomAccessFile(this.file, "r"))
        {
            if (this.tailPos < random.length())
            {
                random.seek(this.tailPos);

                tail = readToEnd(random);
            }
            this.tailPos = random.length();
        }
        return tail;
    }

    /**
     * 指定したファイルを末尾まで読みこみ、データを取得する。
     * 
     * @param random 指定ファイル
     * @return 末尾のデータ
     * @throws IOException 入出力エラー発生時
     */
    private byte[] readToEnd(RandomAccessFile random) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int buf;
        while ((buf = random.read()) != -1)
        {
            out.write(buf);
        }

        return out.toByteArray();
    }

    /**
     * TailEventをリスナーに通知する。
     * 
     * @param event TailEvent
     */
    private void notifyTailEvent(WinTailEvent event)
    {
        for (WinTailEventListener listener : this.listenerList.getListeners(WinTailEventListener.class))
        {
            listener.modify(event);
        }
    }

    /**
     * リスナーを追加する。
     * 
     * @param listener 追加対象リスナー
     */
    public void addTailListener(WinTailEventListener listener)
    {
        this.listenerList.add(WinTailEventListener.class, listener);
    }

    /**
     * リスナーを除去する。
     * 
     * @param listener 除去対象リスナー
     */
    public void removeTailListener(WinTailEventListener listener)
    {
        this.listenerList.remove(WinTailEventListener.class, listener);
    }
}
