package cn.sinjinsong.chat.server.handler.impl;

import cn.sinjinsong.chat.server.handler.MessageHandler;
import cn.sinjinsong.chat.server.property.PromptMsgProperty;
import cn.sinjinsong.common.domain.DownloadInfo;
import cn.sinjinsong.common.domain.Message;
import cn.sinjinsong.common.domain.Response;
import cn.sinjinsong.common.domain.ResponseHeader;
import cn.sinjinsong.common.enumeration.ResponseType;
import cn.sinjinsong.common.util.ProtoStuffUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.BlockingQueue;

/**
 * Created by SinjinSong on 2017/5/23.
 */
@Component("MessageHandler.broadcast")
public class BroadcastMessageHandler extends MessageHandler {
    @Override
    public void handle(Message message, Selector server, SelectionKey client, BlockingQueue<DownloadInfo> queue) {
        try {
            byte[] response = ProtoStuffUtil.serialize(
                    new Response(
                            ResponseHeader.builder()
                                    .type(ResponseType.NORMAL)
                                    .sender(message.getHeader().getSender())
                                    .timestamp(message.getHeader().getTimestamp()).build(),
                                    message.getBody().getBytes(PromptMsgProperty.charset)));
            super.broadcast(response,server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
