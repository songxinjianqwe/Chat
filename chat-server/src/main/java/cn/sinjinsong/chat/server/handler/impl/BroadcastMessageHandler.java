package cn.sinjinsong.chat.server.handler.impl;

import cn.sinjinsong.chat.server.handler.MessageHandler;
import cn.sinjinsong.common.domain.DownloadInfo;
import cn.sinjinsong.common.domain.Message;
import cn.sinjinsong.common.domain.Response;
import cn.sinjinsong.common.domain.ResponseHeader;
import cn.sinjinsong.common.enumeration.ResponseType;
import cn.sinjinsong.common.util.ProtostuffUtil;
import org.springframework.stereotype.Component;

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
@Component("MessageHandler.broadcast")
public class BroadcastMessageHandler implements MessageHandler {
    @Override
    public void handle(Message message, Selector server, SelectionKey client, BlockingQueue<DownloadInfo> queue) {
        try {
            byte[] response = ProtostuffUtil.serialize(
                    new Response(
                            ResponseHeader.builder()
                                    .type(ResponseType.NORMAL)
                                    .sender(message.getHeader().getSender())
                                    .timestamp(message.getHeader().getTimestamp()).build(),
                                    ByteBuffer.wrap(message.getBody().getBytes())));
            for (SelectionKey selectionKey : server.keys()) {
                Channel channel = selectionKey.channel();
                if (channel instanceof SocketChannel) {
                    SocketChannel dest = (SocketChannel) channel;
                    if (dest.isConnected()) {
                        System.out.println("已转发给" + dest);
                        dest.write(ByteBuffer.wrap(response));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
