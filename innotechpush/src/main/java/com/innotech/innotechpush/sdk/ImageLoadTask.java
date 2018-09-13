package com.innotech.innotechpush.sdk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageLoadTask extends AsyncTask<String, Void, Bitmap> {
    private ImageLoadCallback mCallback;

    public ImageLoadTask(ImageLoadCallback callback) {
        mCallback = callback;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        try {
            URL url = new URL(params[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(6000);//设置超时
            conn.setDoInput(true);
            conn.setUseCaches(false);//不缓存
            conn.connect();
            int code = conn.getResponseCode();
            Bitmap bitmap = null;
            if (code == 200) {
                InputStream is = conn.getInputStream();//获得图片的数据流
                bitmap = BitmapFactory.decodeStream(is);
            }
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        if (result != null && mCallback != null) {
            mCallback.onResult(result);
        }
    }
}