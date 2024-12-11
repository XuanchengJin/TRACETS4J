package com.extract.info;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ClassParams {
    private String className;
    private String classDeclareText;
    private List<String> methodDeclareText;
    private List<String> constructMethodDeclareText;
    private List<VariablesParams> variablesParamsList;
    private List<String> inheritancesText;
    private int startLine;
    private int endLine;

    public ClassParams() {
        this.inheritancesText = new ArrayList<>();
        this.variablesParamsList = new ArrayList<>();
        this.methodDeclareText = new ArrayList<>();
        this.constructMethodDeclareText = new ArrayList<>();
    }
}
