package com.innotech.innotechpush.utils;

import android.content.Context;

import com.innotech.innotechpush.config.PushConstant;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import static android.content.Context.MODE_APPEND;

public class FileUtils {

    //向指定的文件中写入指定的数据
    public static void writeFileData(Context context, String content) {
        try {
            FileOutputStream fos = context.openFileOutput(PushConstant.LOG_FILE, MODE_APPEND);//获得FileOutputStream
            //将要写入的字符串转换为byte数组
            byte[] bytes = content.getBytes();
            fos.write(bytes);//将byte数组写入文件
            fos.close();//关闭文件输出流
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //打开指定文件，读取其数据，返回字符串对象
    public static String readFileData(Context context) {
        String result = "";
        try {
            FileInputStream fis = context.openFileInput(PushConstant.LOG_FILE);
            //获取文件长度
            int lenght = fis.available();
            byte[] buffer = new byte[lenght];
            fis.read(buffer);
            //将byte数组转换成指定格式的字符串
            result = new String(buffer, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
