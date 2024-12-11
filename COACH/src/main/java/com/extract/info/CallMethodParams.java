package com.extract.info;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CallMethodParams {
    private String content;
    private String methodName;
    private int argsCount;
    private int startLine;
    private int endLine;
    private List<ArgumentParams> argumentParamsList;

    public CallMethodParams() {
        this.argumentParamsList = new ArrayList<>();
    }
}
