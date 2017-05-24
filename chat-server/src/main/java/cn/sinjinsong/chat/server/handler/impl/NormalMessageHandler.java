package cn.sinjinsong.chat.server.handler.impl;

import cn.sinjinsong.chat.server.handler.MessageHandler;
import cn.sinjinsong.chat.server.property.PromptMsgProperty;
import cn.sinjinsong.chat.server.user.UserManager;
import cn.sinjinsong.common.domain.*;
import cn.sinjinsong.common.enumeration.ResponseType;
import cn.sinjinsong.common.util.ProtoStuffUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

/**
 * Created by SinjinSong on 2017/5/23.
 */
@Component("MessageHandler.normal")
public class NormalMessageHandler extends MessageHandler {
    @Autowired
    private UserManager userManager;

    @Override
    public void handle(Message message, Selector server, SelectionKey client, BlockingQueue<DownloadInfo> queue) {
        try {
            SocketChannel clientChannel = (SocketChannel) client.channel();
            MessageHeader header = message.getHeader();
            SocketChannel receiverChannel = userManager.getUserChannel(header.getReceiver());
            if (receiverChannel == null) {
                //接收者下线
                byte[] response = ProtoStuffUtil.serialize(
                        new Response(
                                ResponseHeader.builder()
                                        .type(ResponseType.PROMPT)
                                        .sender(message.getHeader().getSender())
                                        .timestamp(message.getHeader().getTimestamp())
                                        .build(),
                                PromptMsgProperty.RECEIVER_LOGGED_OFF.getBytes(PromptMsgProperty.charset)));
                clientChannel.write(ByteBuffer.wrap(response));
            } else {
                byte[] response = ProtoStuffUtil.serialize(
                        new Response(
                                ResponseHeader.builder()
                                        .type(ResponseType.NORMAL)
                                        .sender(message.getHeader().getSender())
                                        .timestamp(message.getHeader().getTimestamp())
                                        .build(),
                                message.getBody().getBytes(PromptMsgProperty.charset)));
                System.out.println("已转发给" + receiverChannel);
                receiverChannel.write(ByteBuffer.wrap(response));
                //也给自己发送一份
                clientChannel.write(ByteBuffer.wrap(response));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
