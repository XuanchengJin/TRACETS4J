package com.extract.info.cpg;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class CpgParams {
    private List<String> inputfilelist;
    private String resultpath;
    private String taskid;
    private String repoparentpath;

    public CpgParams(String filePath, String repoName) {
        String[] split = filePath.split(repoName);
        this.taskid = UUID.randomUUID().toString();
        this.repoparentpath = split.length > 1 ? split[0] : "";
        this.resultpath = "/mnt/temporarily";
        this.inputfilelist = new ArrayList<>();
        this.inputfilelist.add(filePath);
    }
}
