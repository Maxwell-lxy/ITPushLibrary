package com.innotech.innotechpush;

/**
 * Created by admin on 2018/5/8.
 */

public interface RequestCallback {
    public void onSuccess(String msg);
    public void onFail(String msg);
}
