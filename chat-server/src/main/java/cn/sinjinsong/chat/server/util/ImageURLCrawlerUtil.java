package cn.sinjinsong.chat.server.util;

import cn.sinjinsong.common.domain.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by SinjinSong on 2017/5/26.
 */
public class ImageURLCrawlerUtil {
    private static final String URL_PATTERN = "https://movie.douban.com/subject/%s/all_photos";
    private static String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; LCTE; rv:11.0) like Gecko";
    private static Map<String, String> COOKIE;
    private static final int DEFAULT_IMAGE_SIZE = 5;

    static {
        COOKIE = new HashMap<>();
        COOKIE.put("ll", "118159");
        COOKIE.put("gr_user_id", "a6ea507c-dc79-4214-ac1e-0c5b9246408e");
        COOKIE.put("ap", "1");
        COOKIE.put("__yadk_uid", "jlwWZzSCtzNDgXqqwJCn2xWm6yxf0hhw");
        COOKIE.put("viewed", "24250054_26593466_10470970_25866350_4199483_1080511_3288908");
        COOKIE.put("ps", "y");
        COOKIE.put("_pk_ref.100001.4cf6", "%5B%22%22%2C%22%22%2C1494650634%2C%22https%3A%2F%2Fwww.baidu.com%2Flink%3Furl%3DOmmNgRKm_H4wbKs1z5hkXQaTY1-PDjHjyUe8kXnJ5jESsyOLAFP38nTEQvjGrKjMmOwNOcj-Ddy5QioPxvuS7F8BAH3wcX74uFB-ahzZ8kS%26wd%3D%26eqid%3D90651dd6000c77350000000358fad91c%22%5D");
        COOKIE.put("ue", "songxinjianzx@163.com");
        COOKIE.put("dbcl2", "158407811:CpSF262azOs");
        COOKIE.put("ck", "KmbD");
        COOKIE.put("__utmt", "1");
        COOKIE.put("_pk_id.100001.4cf6", "318f4af4591e4864.1489738240.44.1494654109.1494607104.");
        COOKIE.put("_pk_ses.100001.4cf6", "*");
        COOKIE.put("__utma", "223695111.1583004744.1489738240.1494607101.1494650634.44");
        COOKIE.put("__utmb", "223695111.0.10.1494650634");
        COOKIE.put("__utmc", "223695111");
        COOKIE.put("__utmz", "30149280.1493910451.48.15.utmcsr=baidu|utmccn=(organic)|utmcmd=organic");
        COOKIE.put("__utmv", "30149280.15840");
        COOKIE.put("bid", "hxJ0ucxP-Qw");
    }


    public static List<String> crawl(Request request) {
        String movieId = request.getUrl();
        int imageSize = DEFAULT_IMAGE_SIZE;
        Map<String, String> params = request.getParams();
        if(params != null){
            if(params.containsKey("imageSize")){
                int size = Integer.parseInt(params.get("imageSize"));
                if(size > 0){
                    imageSize = size;
                }
            }
        }
        List<String> images = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(String.format(URL_PATTERN, movieId))
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .cookies(COOKIE)
                    .get();
            Elements uls = doc.getElementsByClass("pic-col5");
            Element ul = uls.first();

            Elements imgs = ul.getElementsByTag("a");
            String imgURL;
            for (int i = 0; i < Math.min(imgs.size() - 1, imageSize); i++) {
                imgURL = imgs.get(i).attr("href");
                Document imgDoc = Jsoup.connect(imgURL)
                        .userAgent(USER_AGENT)
                        .timeout(10000)
                        .cookies(COOKIE)
                        .get();
                Elements mainphotos = imgDoc.getElementsByClass("mainphoto");
                Element mainphoto = mainphotos.first();
                Elements imgTags = mainphoto.getElementsByTag("img");
                for (Element imgTag : imgTags) {
                    String src = imgTag.attr("src");
                    images.add(src);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return images;
    }
}
