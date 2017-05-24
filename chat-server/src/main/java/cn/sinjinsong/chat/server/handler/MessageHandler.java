package cn.sinjinsong.chat.server.handler;

import cn.sinjinsong.common.domain.DownloadInfo;
import cn.sinjinsong.common.domain.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

/**
 * Created by SinjinSong on 2017/5/23.
 */
public abstract class MessageHandler {
    
    public static final String SYSTEM_SENDER = "系统提示";
    abstract public void handle(Message message, Selector server, SelectionKey client, BlockingQueue<DownloadInfo> queue);
    
    protected void broadcast(byte[] data, Selector server) throws IOException {
        for (SelectionKey selectionKey : server.keys()) {
            Channel channel = selectionKey.channel();
            if (channel instanceof SocketChannel) {
                SocketChannel dest = (SocketChannel) channel;
                if (dest.isConnected()) {
                    dest.write(ByteBuffer.wrap(data));
                }
            }
        }
    }
}
