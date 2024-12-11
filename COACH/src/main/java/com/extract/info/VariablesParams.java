package com.extract.info;

import lombok.Data;

@Data
public class VariablesParams {
    private String content;
    private String type;
    private String name;
    private int startLine;
    private int endLine;
}
