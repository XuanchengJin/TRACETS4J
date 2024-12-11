package com.extract.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CException extends Exception {

    protected int errorCode;
    protected String errorDesc;

    public CException(int errorCode, String errorDesc) {
        super(errorDesc);
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }

    public CException(int errorCode, String errorDesc, Throwable throwable) {
        super(errorDesc, throwable);
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }
}
