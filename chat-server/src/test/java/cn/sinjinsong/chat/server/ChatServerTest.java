package cn.sinjinsong.chat.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by SinjinSong on 2017/5/23.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class ChatServerTest {
    
    @Test
    public void test() {
    }
}