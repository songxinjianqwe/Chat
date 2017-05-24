package cn.sinjinsong.chat.server.exception;

import cn.sinjinsong.common.domain.DownloadInfo;
import lombok.Data;

/**
 * Created by SinjinSong on 2017/5/24.
 */
@Data
public class DownloadException extends RuntimeException{
    private DownloadInfo info;
    public DownloadException(DownloadInfo info){
        super(info.getUrl()+"文件下载失败");
        this.info = info;
    }
}
