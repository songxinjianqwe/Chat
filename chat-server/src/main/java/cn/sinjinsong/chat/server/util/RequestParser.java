package cn.sinjinsong.chat.server.util;

import cn.sinjinsong.common.domain.Request;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SinjinSong on 2017/5/26.
 */
public class RequestParser {
    
    public static Request parse(String rawURL){
        String[] slices = rawURL.split("\\?");
        Map<String,String> params = null;
        if(slices.length > 1){
            params = new HashMap<>();
            String [] paramsStr = slices[1].split("&");
            for(String param: paramsStr){
                String key = param.substring(0,param.indexOf("="));
                String value = param.substring(param.indexOf("=")+1);
                if(StringUtils.isEmpty(key) || StringUtils.isEmpty(value)){
                    continue;
                }
                params.put(key,value);
            }
        }
        return new Request(slices[0],params);
    }
}
