package cn.sinjinsong.chat.server.task.thread;

import java.util.concurrent.Callable;

/**
 * Created by SinjinSong on 2017/5/25.
 */
public class ImageThread implements Callable<byte[]>{
    private String url;
    public ImageThread(String url){
        this.url = url;
    }
    
    @Override
    public byte[] call() throws Exception {
        return new byte[0];
    }
}
