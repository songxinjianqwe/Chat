package cn.sinjinsong.common.enumeration;

/**
 * Created by SinjinSong on 2017/5/23.
 */
public enum ResponseType {
    NORMAL(1,"消息"),
    PROMPT(2,"提示"),
    FILE(3,"文件");
    
    private int code;
    private String desc;

    ResponseType(int code, String desc) {
        this.code = code;
        this.desc = desc;
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
}
