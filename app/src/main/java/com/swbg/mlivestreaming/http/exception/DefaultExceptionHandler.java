package com.swbg.mlivestreaming.http.exception;

import android.app.Activity;

class DefaultExceptionHandler implements IExceptionHandler {
    @Override
    public boolean handle(Activity activity, Throwable t) {
        t.printStackTrace();
        return true;
    }
}
