package com.innotech.innotechpush.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    /**
     * 时间戳转换成字符窜
     *
     * @param milSecond：时间戳
     * @param pattern：格式
     * @return 字符串日期
     */
    public static String getDateToString(long milSecond, String pattern) {
        Date date = new Date(milSecond);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }
}
