package com.extract.info;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FileParams {
    private String filePath;
    private String fileName;
    private String fileContext;
    private String belongModuleName;
    private String packageText;
    private List<String> importTexts;

    public FileParams() {
        this.importTexts = new ArrayList<>();
    }
}
