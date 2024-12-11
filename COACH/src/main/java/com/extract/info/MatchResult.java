package com.extract.info;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class MatchResult {
    @JSONField(ordinal = 2)
    private FunctionResult func_info;
    @JSONField(ordinal = 1)
    private FunctionResult test_info;
    @JSONField(ordinal = 3)
    private ContextResult context;
}
