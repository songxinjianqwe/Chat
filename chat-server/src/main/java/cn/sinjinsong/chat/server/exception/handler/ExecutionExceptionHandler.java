package cn.sinjinsong.chat.server.exception.handler;

import cn.sinjinsong.chat.server.exception.DownloadException;
import cn.sinjinsong.chat.server.property.PromptMsgProperty;
import cn.sinjinsong.common.domain.DownloadInfo;
import cn.sinjinsong.common.domain.Message;
import cn.sinjinsong.common.domain.Response;
import cn.sinjinsong.common.domain.ResponseHeader;
import cn.sinjinsong.common.enumeration.ResponseType;
import cn.sinjinsong.common.util.ProtoStuffUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by SinjinSong on 2017/5/24.
 */
@Component("executionExceptionHandler")
public class ExecutionExceptionHandler {
    public void handle(Throwable e) {
        try {
            if (e instanceof DownloadException) {
                DownloadException downloadException = (DownloadException) e;
                DownloadInfo downloadInfo = downloadException.getInfo();
                Message message = downloadInfo.getMessage();
                byte[] response = ProtoStuffUtil.serialize(
                        new Response(
                                ResponseHeader.builder()
                                        .type(ResponseType.PROMPT)
                                        .sender(message.getHeader().getSender())
                                        .timestamp(message.getHeader().getTimestamp()).build(),
                                PromptMsgProperty.TASK_FAILURE.getBytes()));
                System.out.println("返回任务执行失败信息");
                downloadInfo.getReceiver().write(ByteBuffer.wrap(response));
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
