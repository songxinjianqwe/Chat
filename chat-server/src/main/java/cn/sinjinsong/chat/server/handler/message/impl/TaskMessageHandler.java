package cn.sinjinsong.chat.server.handler.message.impl;

import cn.sinjinsong.chat.server.handler.message.MessageHandler;
import cn.sinjinsong.common.domain.Task;
import cn.sinjinsong.common.domain.Message;
import cn.sinjinsong.common.domain.TaskDescription;
import cn.sinjinsong.common.util.ProtoStuffUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by SinjinSong on 2017/5/23.
 * 生产者
 * 注意所有的InterruptedException，要么抛给上层，要么自己处理
 */
@Component("MessageHandler.task")
@Slf4j
public class TaskMessageHandler extends MessageHandler {
    
    @Override
    public void handle(Message message, Selector server, SelectionKey client, BlockingQueue<Task> queue, AtomicInteger onlineUsers) throws InterruptedException {
        TaskDescription taskDescription = ProtoStuffUtil.deserialize(message.getBody(), TaskDescription.class);
        Task task = new Task((SocketChannel) client.channel(), taskDescription.getType(), taskDescription.getDesc(), message);
        try {
            queue.put(task);
            log.info("{}已放入阻塞队列",task.getReceiver().getRemoteAddress());
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
