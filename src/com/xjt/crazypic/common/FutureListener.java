package com.xjt.crazypic.common;

public interface FutureListener<T> {
    public void onFutureDone(Future<T> future);
}
