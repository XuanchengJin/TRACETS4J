package com.extract.response;

import com.alibaba.fastjson.annotation.JSONField;

public abstract class AbstractResponse<T> {
    private static final int ORDINAL_ONE = 1;
    private static final int ORDINAL_TWO = 2;

    @JSONField(ordinal = ORDINAL_ONE)
    protected int code;

    @JSONField(ordinal = ORDINAL_TWO)
    protected String message;

    public AbstractResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public abstract int getCode();

    public abstract String getMessage();

    public abstract T getResult();

    public void setCode(int code) {
        throw new UnsupportedOperationException();
    }

    public void setMessage(String meaasge) {
        throw new UnsupportedOperationException();
    }

    public void SetResult(T result) {
        throw new UnsupportedOperationException();
    }
}
