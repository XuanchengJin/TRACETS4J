package com.extract.response;

import com.alibaba.fastjson.annotation.JSONField;

public class SuccessfulResponse<T> extends AbstractResponse<T> {
    private static final int SUCCESS_CODE = 0;
    private static final String SUCCESS_MESSAGE = "success";
    private static final int ORDINAL_THREE = 3;
    @JSONField(ordinal = ORDINAL_THREE)
    private T result;

    public SuccessfulResponse(T result) {
        super(SUCCESS_CODE, SUCCESS_MESSAGE);
        this.result = result;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public T getResult(){
        return result;
    }
}
