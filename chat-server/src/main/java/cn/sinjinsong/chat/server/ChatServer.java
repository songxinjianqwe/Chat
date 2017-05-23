package cn.sinjinsong.chat.server;

import cn.sinjinsong.chat.server.handler.MessageHandler;
import cn.sinjinsong.chat.server.task.DownloadManager;
import cn.sinjinsong.chat.server.util.SpringContextUtil;
import cn.sinjinsong.common.domain.DownloadInfo;
import cn.sinjinsong.common.domain.Message;
import cn.sinjinsong.common.util.ProtoStuffUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.*;

/**
 * Created by SinjinSong on 2017/3/25.
 */
public class ChatServer {
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ExecutorService readPool;
    private ExecutorService downloadPool;
    private BlockingQueue<DownloadInfo> downloadTaskQueue;
    private DownloadManager downloadManager;

    public ChatServer() {
        System.out.println("服务器启动");
        initServer();
        listen();
    }
    
    private void initServer() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            //切换为非阻塞模式
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(9000));
            //获得选择器
            selector = Selector.open();
            //将channel注册到selector上
            //第二个参数是选择键，用于说明selector监控channel的状态
            //可能的取值：SelectionKey.OP_READ OP_WRITE OP_CONNECT OP_ACCEPT
            //监控的是channel的接收状态
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            readPool = new ThreadPoolExecutor(5, 10, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10), new ThreadPoolExecutor.CallerRunsPolicy());
            downloadPool = new ThreadPoolExecutor(5, 10, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10), new ThreadPoolExecutor.CallerRunsPolicy());
            downloadTaskQueue = new ArrayBlockingQueue<>(20);
            downloadManager = new DownloadManager(downloadPool, downloadTaskQueue);
            new Thread(downloadManager).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * 入口
     */
    private void listen() {
        try {
            //如果有一个及以上的客户端的数据准备就绪
            while (true) {
                //当注册的事件到达时，方法返回；否则,该方法会一直阻塞  
                selector.select();
                //获取当前选择器中所有注册的监听事件
                for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                    SelectionKey key = it.next();
                    //删除已选的key,以防重复处理 
                    it.remove();
                    //如果"接收"事件已就绪
                    if (key.isAcceptable()) {
                        //交由接收事件的处理器处理
                        handleAcceptRequest();
                    } else if (key.isReadable()) {
                        //如果"读取"事件已就绪
                        //取消可读触发标记，本次处理完后才打开读取事件标记
                        key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                        //交由读取事件的处理器处理
                        readPool.execute(new ReadEventHandler(key));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理客户端的连接请求
     */
    private void handleAcceptRequest() {
        try {
            SocketChannel client = serverSocketChannel.accept();
            // 接收的客户端也要切换为非阻塞模式
            client.configureBlocking(false);
            // 监控客户端的读操作是否就绪
            client.register(selector, SelectionKey.OP_READ);
            System.out.println("服务器连接客户端:" + client.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ReadEventHandler implements Runnable {

        private ByteBuffer buf;
        private SocketChannel client;
        private ByteArrayOutputStream baos;
        private SelectionKey key;

        public ReadEventHandler(SelectionKey key) {
            this.key = key;
            this.client = (SocketChannel) key.channel();
            this.buf = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
            this.baos = new ByteArrayOutputStream();
        }

        @Override
        public void run() {
            try {
                int size;
                while ((size = client.read(buf)) > 0) {
                    buf.flip();
                    baos.write(buf.array(), 0, size);
                    buf.clear();
                }
                if(size == -1){
                    return;
                }
                System.out.println("读取完毕，继续监听");
                //继续监听读取事件
                key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                key.selector().wakeup();
                byte[] bytes = baos.toByteArray();
                baos.close();
                if(bytes.length == 0){
                    return;
                }
                Message message = ProtoStuffUtil.deserialize(bytes, Message.class);
                System.out.println(message.getHeader().getType().toString().toLowerCase());
                MessageHandler messageHandler = SpringContextUtil.getBean("MessageHandler", message.getHeader().getType().toString().toLowerCase());
                messageHandler.handle(message, selector, key, downloadTaskQueue);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        System.out.println("Initialing...");
        new ChatServer();
    }
}
