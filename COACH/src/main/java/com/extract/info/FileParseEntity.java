package com.extract.info;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FileParseEntity {
    private FileParams fileParams;
    private List<ClassParams> classParamsList;
    private List<FunctionParams> functionParamsList;
    private boolean isTestFile;

    public FileParseEntity() {
        this.classParamsList = new ArrayList<>();
        this.functionParamsList = new ArrayList<>();
    }
}