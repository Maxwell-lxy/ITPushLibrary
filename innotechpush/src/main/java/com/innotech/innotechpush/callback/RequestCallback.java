package com.innotech.innotechpush.callback;

public interface RequestCallback {
    void onSuccess(String msg);
    void onFail(String msg);
}
