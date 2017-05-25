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
    public static byte[] zipCompress(List<byte[]> files) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry zipEntry;
        int size;
        for (int i = 0; i < files.size(); i++) {
            try {
                zipEntry = new ZipEntry(String.valueOf(i));
                zos.putNextEntry(zipEntry);
                zos.write(files.get(i));
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
        byte[] result = baos.toByteArray();
        try {
            baos.close();
            zos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
