package com.extract.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExtractRequest {
    @NotBlank(message = "project path can not empty")
    private String projectPath;
    @NotBlank(message = "result path can not empty")
    private String resultPath;
    @NotBlank(message = "repo id can not empty")
    private String repoId;
    @NotBlank(message = "repo name can not empty")
    private String repoName;
    private String repoUrl;
}