package pers.gloria.controller;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

public class ArpcController implements RpcController {
    private String errText;
    private boolean isfailed;

    @Override
    public void reset() {
        this.isfailed = false;
        this.errText = "";

    }

    @Override
    public boolean failed() {
        return isfailed;
    }

    @Override
    public String errorText() {
        return errText;
    }

    @Override
    public void startCancel() {

    }

    @Override
    public void setFailed(String s) {
        this.isfailed = true;
        this.errText = s;

    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void notifyOnCancel(RpcCallback<Object> rpcCallback) {

    }
}
