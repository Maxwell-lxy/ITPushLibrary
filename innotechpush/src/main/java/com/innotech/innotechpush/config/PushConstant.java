package com.innotech.innotechpush.config;

/**
 * 推送用到的常量
 */

public class PushConstant {

    /**
     * 是否开启debug
     */
    public static final String INNOTECH_PUSH_DEBUG = "INNOTECH_PUSH_DEBUG";

    /**
     * 服务器app_id
     */
    public static final String INNOTECH_APP_ID = "INNOTECH_APP_ID";

    /**
     * 服务器app_key
     */
    public static final String INNOTECH_APP_KEY = "INNOTECH_APP_KEY";

    /**
     * 推送版本号
     */
    public static final String INNOTECH_PUSH_VERSION = "2.1.3";

    /**
     * 默认开启华为
     * 如果不需要开启华为则需要设为false
     */
    public static boolean hasHuawei = true;

    /**
     * 默认开启oppo
     * 如果不需要开启oppo则需要设为false
     */
    public static boolean hasOppo = true;

    /**
     * 默认开启vivo
     * 如果不需要开启vivo则需要设为false
     */
    public static boolean hasVivo = true;

}
