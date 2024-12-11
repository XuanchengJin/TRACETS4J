package com.extract.info;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
public class FunctionResult {
    @JSONField(ordinal = 1)
    private String func_comment;
    @JSONField(ordinal = 2)
    private String func_name;
    @JSONField(ordinal = 3)
    private List<String> func_arg;
    @JSONField(ordinal = 4)
    private List<String> annotation;
    @JSONField(ordinal = 5)
    private String func_body;
    @JSONField(ordinal = 6)
    private int start_line;
    @JSONField(ordinal = 7)
    private int end_line;
    @JSONField(ordinal = 8)
    private List<CallMethodParams> callMethods;
    @JSONField(ordinal = 9)
    private List<String> func_return;

    public FunctionResult(FunctionParams params) {
        if(Objects.nonNull(params)) {
            this.func_comment = params.getComment();
            this.func_name = params.getFuncName();
            this.func_arg = params.getArgumentParams().stream().map(ArgumentParams::getContent).collect(Collectors.toList());
            this.func_body = params.getFuncContent();
            this.start_line = params.getStartLine();
            this.end_line = params.getEndLine();
            this.callMethods = new ArrayList<>(params.getCallMethods());
            this.func_return = new ArrayList<>(params.getReturnText());
            this.annotation = new ArrayList<>(params.getAnnotationText());
        }
    }
}
