package com.extract.info;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Data
public class FileResultInfo {
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
    public int pairs_same_name_num;
    @JSONField(ordinal = 14)
    private List<MatchResult> pairs_same_name;
    @JSONField(ordinal = 15)
    public int pairs_diff_name_num;
    @JSONField(ordinal = 16)
    private List<MatchResult> pairs_diff_name;
    @JSONField(ordinal = 17)
    public int pairs_relate_name_num;
    @JSONField(ordinal = 18)
    private List<MatchResult> pairs_relate_name;
    @JSONField(ordinal = 19)
    public int pairs_constructor_num;
    @JSONField(ordinal = 20)
    private List<MatchResult> pairs_constructor;
    @JSONField(ordinal = 21)
    public int pairs_unique_call_num;
    @JSONField(ordinal = 22)
    private List<MatchResult> pairs_unique_call;
    @JSONField(ordinal = 23)
    public int test_and_possible_func_num;
    @JSONField(ordinal = 24)
    private List<AbstructResult> test_and_possible_func;
    @JSONField(ordinal = 25)
    public int extract_num;

    public FileResultInfo() {
        this.pairs_same_name = new ArrayList<>();
        this.pairs_diff_name = new ArrayList<>();
        this.pairs_relate_name = new ArrayList<>();
        this.pairs_constructor = new ArrayList<>();
        this.pairs_unique_call = new ArrayList<>();
        this.test_and_possible_func = new ArrayList<>();
        pairs_same_name_num = 0;
        pairs_diff_name_num = 0;
        pairs_relate_name_num = 0;
        pairs_constructor_num = 0;
        pairs_unique_call_num = 0;
        test_and_possible_func_num = 0;
        extract_num = 0;
    }

    public void buildTestFileResult(FileParseEntity fileParseEntity, FileResultInfo result) {
        Map<String, List<String>> testVariable = new HashMap<>();
        result.test_file_path = fileParseEntity.getFileParams().getFilePath();
        result.test_class = fileParseEntity.getClassParamsList().stream().map(ClassParams::getClassDeclareText).collect(Collectors.toList());
        result.test_method = fileParseEntity.getClassParamsList().stream().collect(Collectors.toMap(ClassParams::getClassDeclareText, ClassParams::getMethodDeclareText, (c1, c2) -> c1));
        result.test_construct = fileParseEntity.getClassParamsList().stream().collect(Collectors.toMap(ClassParams::getClassDeclareText, ClassParams::getConstructMethodDeclareText, (c1, c2) -> c1));
        fileParseEntity.getClassParamsList().forEach(classParams -> {
            List<String> variables = classParams.getVariablesParamsList().stream().map(VariablesParams::getContent).collect(Collectors.toList());
            testVariable.put(classParams.getClassDeclareText(), variables);
        });
        result.test_variable = testVariable;
        result.test_import = fileParseEntity.getFileParams().getImportTexts();
    }

    public void buildFileResult(FileParseEntity fileParseEntity, FileResultInfo result) {
        Map<String, List<String>> fileVariable = new HashMap<>();
        result.file_path = fileParseEntity.getFileParams().getFilePath();
        result.file_class = fileParseEntity.getClassParamsList().stream().map(ClassParams::getClassDeclareText).collect((Collectors.toList()));
        result.file_method = fileParseEntity.getClassParamsList().stream().collect(Collectors.toMap(ClassParams::getClassDeclareText, ClassParams::getMethodDeclareText));
        result.file_construct = fileParseEntity.getClassParamsList().stream().collect(Collectors.toMap(ClassParams::getClassDeclareText, ClassParams::getConstructMethodDeclareText));
        fileParseEntity.getClassParamsList().forEach(classParams -> {
            List<String> variables = classParams.getVariablesParamsList().stream().map(VariablesParams::getContent).collect(Collectors.toList());
            fileVariable.put(classParams.getClassDeclareText(), variables);
        });
        result.file_variable = fileVariable;
        result.file_import = fileParseEntity.getFileParams().getImportTexts();
    }
}
