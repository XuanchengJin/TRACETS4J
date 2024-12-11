package com.extract.info;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AbstructResult {
    @JSONField(ordinal = 1)
    private FunctionResult test_info;
    @JSONField(ordinal = 2)
    private List<FunctionResult> func_info;
    @JSONField(ordinal = 3)
    private ContextResult context;

    public AbstructResult() {
        this.func_info = new ArrayList<>();
    }

    public void addFunc_info(FunctionResult func) {
        this.func_info.add(func);
    }
}