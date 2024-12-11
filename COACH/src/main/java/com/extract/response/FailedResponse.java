package com.extract.response;

public class FailedResponse<T> extends AbstractResponse<T> {
    public FailedResponse(int code, final String message) {
        super(code, message);
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
    public T getResult() {
        return null;
    }
}
