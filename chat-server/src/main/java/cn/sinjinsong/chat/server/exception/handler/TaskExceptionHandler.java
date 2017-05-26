package cn.sinjinsong.chat.server.exception.handler;

import cn.sinjinsong.chat.server.exception.TaskException;
import cn.sinjinsong.chat.server.property.PromptMsgProperty;
import cn.sinjinsong.common.domain.Task;
import cn.sinjinsong.common.domain.Message;
import cn.sinjinsong.common.domain.Response;
import cn.sinjinsong.common.domain.ResponseHeader;
import cn.sinjinsong.common.enumeration.ResponseType;
import cn.sinjinsong.common.util.ProtoStuffUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by SinjinSong on 2017/5/24.
 */

/**
 * UncaughtExceptionHandler异常处理器可以处理ExecutorService通过execute方法提交的线程中抛出的RuntimeException
 */
@Component("taskExceptionHandler")
@Slf4j
public class TaskExceptionHandler implements Thread.UncaughtExceptionHandler{
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            if (e instanceof TaskException) {
                TaskException taskException = (TaskException) e;
                Task task = taskException.getInfo();
                Message message = task.getMessage();
                byte[] response = ProtoStuffUtil.serialize(
                        new Response(
                                ResponseHeader.builder()
                                        .type(ResponseType.PROMPT)
                                        .sender(message.getHeader().getSender())
                                        .timestamp(message.getHeader().getTimestamp()).build(),
                                PromptMsgProperty.TASK_FAILURE.getBytes(PromptMsgProperty.charset)));
                log.info("返回任务执行失败信息");
                task.getReceiver().write(ByteBuffer.wrap(response));
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
