package cn.sinjinsong.chat.server.task;

import cn.sinjinsong.common.domain.DownloadInfo;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;

/**
 * Created by SinjinSong on 2017/5/23.
 */
public class DownloadHandler implements Callable<ByteBuffer> {
    private DownloadInfo info;

    public DownloadHandler(DownloadInfo info) {
        this.info = info;
    }
        
    @Override
    public ByteBuffer call() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteBuffer buf = ByteBuffer.allocate(4096);
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(info.getUrl(), 80));
        int size;
        while ((size = channel.read(buf)) != -1) {
            baos.write(buf.array(), 0, size);
        }
        byte[] bytes = baos.toByteArray();
        baos.close();
        return ByteBuffer.wrap(bytes);
    }
}
