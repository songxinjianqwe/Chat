package cn.sinjinsong.chat.server.handler.task.crawl;

import cn.sinjinsong.chat.server.http.HttpConnectionManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Created by SinjinSong on 2017/5/25.
 */
public class ImageThread implements Callable<byte[]>{
    private String url;
    private HttpConnectionManager manager;
    public ImageThread(String url,HttpConnectionManager manager){
        this.url = url;
        this.manager = manager;
    }
    
    @Override
    public byte[] call() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (!manager.copyStream(url, baos)) {
            throw new IOException();
        }
        System.out.println("下载图片成功");
        byte[] bytes = baos.toByteArray();
        baos.close();
        return bytes;
    }
}
