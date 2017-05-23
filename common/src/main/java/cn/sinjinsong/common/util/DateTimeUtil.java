package cn.sinjinsong.common.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Created by SinjinSong on 2017/4/23.
 */
public class DateTimeUtil {
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static LocalDateTime toLocalDateTime(Long date) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault());
    }
    
    public static LocalDate toLocalDate(Long date) {
        return Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static long toLong(LocalDateTime localDateTime) {
        return Timestamp.valueOf(localDateTime).getTime();
    }
    
    public static String formatLocalDateTime(LocalDateTime dateTime){
        return dateTime.format(dateTimeFormatter);
    }
    
    public static String formatLocalDateTime(Long dateTime){
        return toLocalDateTime(dateTime).format(dateTimeFormatter);
    }
    
}
