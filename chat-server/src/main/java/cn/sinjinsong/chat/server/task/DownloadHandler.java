package cn.sinjinsong.chat.server.task;

import cn.sinjinsong.chat.server.exception.DownloadException;
import cn.sinjinsong.chat.server.http.HttpConnectionManager;
import cn.sinjinsong.common.domain.DownloadInfo;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Callable;

/**
 * Created by SinjinSong on 2017/5/23.
 */

public class DownloadHandler implements Callable<byte[]> {
    private DownloadInfo info;
    private HttpConnectionManager manager;

    public DownloadHandler(DownloadInfo info,HttpConnectionManager manager) {
        this.info = info;
        this.manager = manager;
    }

    @Override
    public byte[] call() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if(!manager.copyStream(info.getUrl(),baos)){
            throw new DownloadException(info);
        }
        byte[] bytes = baos.toByteArray();
        baos.close();
        return bytes;
    }
}
