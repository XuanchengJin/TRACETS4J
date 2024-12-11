package com.extract.service; 

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.extract.info.FileParseEntity;
import com.extract.info.RepoResult;
import com.extract.request.ExtractRequest;
import com.extract.service.ast.Parsing;
import com.extract.utils.FileUtils;
import com.extract.utils.MatchUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ExtractService {
    @Autowired
    private MatchUtils matchUtils;
    @Autowired
    private FileUtils fileUtils;
    @Autowired
    private Parsing parsing;

    public void dataSetExtract(ExtractRequest request) {
        log.info("{} Test Data Extraction Running", request.getRepoName());
        List<FileParseEntity> fileParseEntityList = parseRepoAllFile(request.getProjectPath());
        log.info("File Parse Entity List Size: {}", fileParseEntityList.size());
        RepoResult repoResult = new RepoResult(request.getRepoId(), request.getRepoName(), request.getRepoUrl());
        log.info("Build RepoResult successfully, RepoID: {} , RepoName: {} ,RepoUrl: {}", repoResult.repo_id, repoResult.repo_name, repoResult.repo_url);
        matchUtils.matchAndExtract(fileParseEntityList, request.getRepoName(), request.getResultPath(), repoResult);
        log.info("{} Test Data Extraction Finish", request.getRepoName());
    }

    /**
     * parse all java file
     */
    private List<FileParseEntity> parseRepoAllFile(String repoPath) {
        List<FileParseEntity> result = new ArrayList<>();
        List<File> javaFiles = fileUtils.readRepoPath(repoPath);
        Optional.of(javaFiles).ifPresent(fileList -> {
            fileList.forEach(file -> {
                FileParseEntity fileParseEntity = parsing.parsing(file.getAbsolutePath());
                result.add(fileParseEntity);
            });
        });
        return result;
    }
}