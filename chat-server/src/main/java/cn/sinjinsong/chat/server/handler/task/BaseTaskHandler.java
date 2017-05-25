package cn.sinjinsong.chat.server.handler.task;

import cn.sinjinsong.chat.server.exception.TaskException;
import cn.sinjinsong.chat.server.http.HttpConnectionManager;
import cn.sinjinsong.chat.server.task.TaskManagerThread;
import cn.sinjinsong.common.domain.Response;
import cn.sinjinsong.common.domain.Task;
import cn.sinjinsong.common.util.ProtoStuffUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by SinjinSong on 2017/5/25.
 */
public abstract class BaseTaskHandler implements Runnable {
    protected Task info;
    protected HttpConnectionManager manager;


    abstract protected Response process() throws IOException, InterruptedException;

    abstract protected void init(TaskManagerThread parentThread);

    @Override
    public final void run() {
        try {
            info.getReceiver().write(ByteBuffer.wrap(ProtoStuffUtil.serialize(process())));
        } catch (IOException e) {
            e.printStackTrace();
            throw new TaskException(info);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new TaskException(info);
        }
    }

    public void init(Task info, HttpConnectionManager manager, TaskManagerThread parentThread) {
        this.info = info;
        this.manager = manager;
        init(parentThread);
    }

}
