package cn.sinjinsong.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by SinjinSong on 2017/5/25.
 */
public class ZipUtil {
    private static final String FILE_NAME_PATTERN = "%s.%s";
    private static final String DEFAULT_SUFFIX = "data";
    public static byte[] zipCompress(List<byte[]> files,String suffix) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry zipEntry;
        for (int i = 0; i < files.size(); i++) {
            try {
                zipEntry = new ZipEntry(String.format(FILE_NAME_PATTERN,String.valueOf(i),suffix));
                System.out.println(i+"放入Entry");
                zos.putNextEntry(zipEntry);
                zos.write(files.get(i));
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
        try {
            zos.closeEntry();
            zos.close();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] result = baos.toByteArray();
        System.out.println("最终数据:"+result.length);
        return result;
    }
}
