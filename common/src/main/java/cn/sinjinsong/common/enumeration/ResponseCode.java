package cn.sinjinsong.common.enumeration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SinjinSong on 2017/5/23.
 */
public enum ResponseCode {
    LOGIN_SUCCESS(1,"登录成功"),
    LOGIN_FAILURE(2,"登录失败"),
    LOGOUT_SUCCESS(3,"下线成功");
    
    private int code;
    private String desc;
    private static Map<Integer, ResponseCode> map = new HashMap<>();
    
    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    static {
        for (ResponseCode code : values()) {
            map.put(code.getCode(), code);
        }
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    public static ResponseCode fromCode(int code) {
        return map.get(code);
    }
}
