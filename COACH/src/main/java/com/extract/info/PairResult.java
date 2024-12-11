package com.extract.info;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PairResult {
    @JSONField(ordinal = 1)
    private String test_file_path;
    @JSONField(ordinal = 2)
    private List<String> test_class;
    @JSONField(ordinal = 3)
    private Map<String, List<String>> test_method;
    @JSONField(ordinal = 4)
    private Map<String, List<String>> test_construct;
    @JSONField(ordinal = 5)
    private Map<String, List<String>> test_variable;
    @JSONField(ordinal = 6)
    private List<String> test_import;
    @JSONField(ordinal = 7)
    private String file_path;
    @JSONField(ordinal = 8)
    private List<String> file_class;
    @JSONField(ordinal = 9)
    private Map<String, List<String>> file_method;
    @JSONField(ordinal = 10)
    private Map<String, List<String>> file_construct;
    @JSONField(ordinal = 11)
    private Map<String, List<String>> file_variable;
    @JSONField(ordinal = 12)
    private List<String> file_import;
    @JSONField(ordinal = 13)
    private FunctionResult test_info;
    @JSONField(ordinal = 14)
    private FunctionResult func_info;
    @JSONField(ordinal = 15)
    private ContextResult context;
    @JSONField(ordinal = 16)
    private String extract_method;
    @JSONField(ordinal = 17)
    private RepoResult repo_info;

    public PairResult(FileResultInfo fileResultInfo, RepoResult repoResult, MatchResult matchResult, String extractMethod) {
        this.test_file_path = fileResultInfo.getTest_file_path();
        this.test_class = fileResultInfo.getTest_class();
        this.test_method = fileResultInfo.getTest_method();
        this.test_construct = fileResultInfo.getTest_construct();
        this.test_variable = fileResultInfo.getTest_variable();
        this.test_import = fileResultInfo.getTest_import();
        this.file_path = fileResultInfo.getFile_path();
        this.file_class = fileResultInfo.getFile_class();
        this.file_method = fileResultInfo.getFile_method();
        this.file_construct = fileResultInfo.getFile_construct();
        this.file_variable = fileResultInfo.getFile_variable();
        this.file_import = fileResultInfo.getFile_import();
        this.test_info = matchResult.getTest_info();
        this.func_info = matchResult.getFunc_info();
        this.context = matchResult.getContext();
        this.extract_method = extractMethod;
        this.repo_info = repoResult;
    }
}
