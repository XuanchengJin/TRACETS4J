package com.extract.info;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FunctionParams {
    private String funcName;
    private String comment;
    private List<ArgumentParams> argumentParams;
    private List<String> annotationText;
    private List<String> specifierText;
    private String funcContent;
    private int startLine;
    private int endLine;
    private int bodyStartLine;
    private int bodyEndLine;
    private String bodyContent;
    private List<String> returnText;
    private String funcReturnClass;
    private List<CallMethodParams> callMethods;
    private List<VariablesParams> variablesParams;
    private List<CallMethodParams> callConstructorMethods;
    private boolean isTestMethod;
    private boolean isExtracted;
    private String belongClassDeclareText;
    private String belongClassName;
    private String testMethodName;
    private String filePath;

    public FunctionParams() {
        this.comment = "";
        this.isTestMethod = false;
        this.isExtracted = false;
        this.argumentParams = new ArrayList<>();
        this.annotationText = new ArrayList<>();
        this.specifierText = new ArrayList<>();
        this.returnText = new ArrayList<>();
        this.callMethods = new ArrayList<>();
        this.variablesParams = new ArrayList<>();
        this.callConstructorMethods = new ArrayList<>();
        this.belongClassDeclareText = "";
    }
}
