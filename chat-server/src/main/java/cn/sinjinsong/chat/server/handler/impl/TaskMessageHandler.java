package cn.sinjinsong.chat.server.handler.impl;

import cn.sinjinsong.chat.server.handler.MessageHandler;
import cn.sinjinsong.common.domain.DownloadInfo;
import cn.sinjinsong.common.domain.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

/**
 * Created by SinjinSong on 2017/5/23.
 * 生产者
 */
@Component("MessageHandler.task")
public class TaskMessageHandler extends MessageHandler {
    @Override
    public void handle(Message message, Selector server, SelectionKey client, BlockingQueue<DownloadInfo> queue) {
        DownloadInfo downloadInfo = new DownloadInfo((SocketChannel) client.channel(), message.getBody(), message);
        try {
            queue.put(downloadInfo);
            System.out.println(downloadInfo.getReceiver().getRemoteAddress()+"已放入阻塞队列");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
