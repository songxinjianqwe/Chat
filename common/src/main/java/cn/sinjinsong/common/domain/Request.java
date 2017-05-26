package cn.sinjinsong.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by SinjinSong on 2017/5/26.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    private String url;
    private Map<String,String> params;
}
