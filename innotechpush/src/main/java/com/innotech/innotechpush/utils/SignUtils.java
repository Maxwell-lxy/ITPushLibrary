package com.innotech.innotechpush.utils;

import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 签名工具类
 */

public class SignUtils {

    private static final String AK = "2ad2ae6d-142d-433d-b9b4-2f51e84f4475";
    private static final String SK = "bea2c365-75f8-411b-b278-0782244b02d4";
    private static final String CONTENT_TYPE = "application/json";

    /**
     * 生成签名信息
     *
     * @param method：GET           POST
     * @param path：接口地址（不含host部分）
     * @param json：接口参数，转化为json字符串
     * @return 签名信息
     */
    public static String sign(String method, String path, String json) {
        StringBuffer sb = new StringBuffer();
        sb.append(method).append(" ").append(path).append("\n");
        sb.append("Host: ").append(NetWorkUtils.HOST).append("\n");
        sb.append("Content-Type: ").append(CONTENT_TYPE).append("\n");
        sb.append("\n");
        sb.append(json);
        String b64code = hmac_sha1(sb.toString());
        b64code = b64code.replace('/', '_').replace('+', '-');
        return "mt " + AK + ":" + b64code;
    }

    /**
     * 生成签名信息
     *
     * @param method：GET           POST
     * @param path：接口地址（不含host部分）
     * @param json：接口参数，转化为json字符串
     * @return 签名信息
     */
    public static String sign(String host, String method, String path, String json) {
        StringBuffer sb = new StringBuffer();
        sb.append(method).append(" ").append(path).append("\n");
        sb.append("Host: ").append(host).append("\n");
        sb.append("Content-Type: ").append(CONTENT_TYPE).append("\n");
        sb.append("\n");
        sb.append(json);
        String b64code = hmac_sha1(sb.toString());
        b64code = b64code.replace('/', '_').replace('+', '-');
        return "mt " + AK + ":" + b64code;
    }

    /**
     * hmac加密
     */
    private static String hmac_sha1(String data) {
        String reString = "";
        try {
            //生成一个指定 Mac 算法 的 Mac 对象
            Mac mac = Mac.getInstance("HmacSHA1");
            //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
            SecretKey secretKey = new SecretKeySpec(SK.getBytes(), mac.getAlgorithm());
            //用给定密钥初始化 Mac 对象
            mac.init(secretKey);
            byte[] digest = mac.doFinal(data.getBytes());
            reString = Base64.encodeToString(digest, Base64.DEFAULT);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return reString;
    }

}
