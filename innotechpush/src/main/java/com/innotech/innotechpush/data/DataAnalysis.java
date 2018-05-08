package com.innotech.innotechpush.data;

import android.text.TextUtils;
import android.util.Log;

import com.innotech.innotechpush.bean.BaseResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 数据处理类
 */

public class DataAnalysis<T> {

    /**
     * 处理返回值，并返回泛型对象。
     * @param response：返回值
     * @param className：泛型T的类名
     * @return
     */
    public BaseResponse<T> analysisData(String response, String className) {
        BaseResponse<T> baseResponse = new BaseResponse<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            int code = jsonObject.getInt("code");
            String msg = jsonObject.getString("msg");
            String data = jsonObject.getString("data");
            baseResponse.setCode(code);
            baseResponse.setMsg(msg);
            if (!TextUtils.isEmpty(className)) {
                Object obj = jsonToT(className, data);
                if (obj != null) {
                    baseResponse.setData((T) obj);
                }
            }
        } catch (JSONException e) {
            Log.e("Innotech_Push", "接口返回值处理过程中json转化出现异常：" + e.getMessage());
        }
        return baseResponse;
    }

    /**
     * 通过反射实例化接口返回值的Data对象。
     *
     * @param className
     * @param json
     * @return
     */
    private Object jsonToT(String className, String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            Class<?> clazz = Class.forName(className);
            Object obj = clazz.newInstance();
            Method[] methods = clazz.getDeclaredMethods();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                try {
                    String fieldName = field.getName();
                    String fieldType = field.getType().getSimpleName();
                    String fieldSetName = parSetName(fieldName);
                    if (!checkSetMet(methods, fieldSetName)) {
                        continue;
                    }
                    Method fieldSetMet = clazz.getMethod(fieldSetName,
                            field.getType());

                    if ("String".equals(fieldType)) {
                        String value = jsonObject.getString(fieldName);
                        if (!TextUtils.isEmpty(value)) {
                            fieldSetMet.invoke(obj, value);
                        }
                    } else if ("Integer".equals(fieldType)
                            || "int".equals(fieldType)) {
                        fieldSetMet.invoke(obj, jsonObject.getInt(fieldName));
                    } else if ("Long".equalsIgnoreCase(fieldType)) {
                        fieldSetMet.invoke(obj, jsonObject.getLong(fieldName));
                    } else if ("Double".equalsIgnoreCase(fieldType)) {
                        fieldSetMet.invoke(obj, jsonObject.getDouble(fieldName));
                    } else if ("Boolean".equalsIgnoreCase(fieldType)) {
                        fieldSetMet.invoke(obj, jsonObject.getBoolean(fieldName));
                    }
                } catch (NoSuchMethodException e) {
                    continue;
                } catch (IllegalAccessException e) {
                    continue;
                } catch (JSONException e) {
                    continue;
                } catch (InvocationTargetException e) {
                    continue;
                }
            }
            return obj;
        } catch (Exception e) {
            Log.e("Innotech_Push", "反射转化对象时出现异常：" + e.getMessage());
            return null;
        }
    }

    /**
     * 判断是否存在某属性的 set方法
     *
     * @param methods
     * @param fieldSetMet
     * @return boolean
     */
    public static boolean checkSetMet(Method[] methods, String fieldSetMet) {
        for (Method met : methods) {
            if (fieldSetMet.equals(met.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 拼接在某属性的 set方法
     *
     * @param fieldName
     * @return String
     */
    public static String parSetName(String fieldName) {
        if (null == fieldName || "".equals(fieldName)) {
            return null;
        }
        int startIndex = 0;
        if (fieldName.charAt(0) == '_')
            startIndex = 1;
        return "set"
                + fieldName.substring(startIndex, startIndex + 1).toUpperCase()
                + fieldName.substring(startIndex + 1);
    }
}
