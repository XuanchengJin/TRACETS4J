package com.extract.utils;

import com.alibaba.fastjson.JSONObject;
import com.extract.info.CallMethodParams;
import com.extract.info.FileResultInfo;
import com.extract.info.MatchResult;
import com.extract.info.PairResult;
import com.extract.info.RepoResult;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class FileUtils {
    
    public String dealDeclareString(String filePath, int stratLine, int endLine) {
        String declareString = getFileSpecifiedRange(filePath, stratLine, endLine);
        int length = declareString.lastIndexOf("{") == -1 ? declareString.length() : declareString.lastIndexOf("{");
        return declareString.substring(0, length);
    }

    public String getFileSpecifiedRange(String filePath, int stratLine, int endLine) {
        try (FileReader in = new FileReader(filePath); LineNumberReader reader = new LineNumberReader(in)) {
            StringBuilder result = new StringBuilder();
            String lineContent = reader.readLine();
            while (lineContent != null) {
                if (reader.getLineNumber() >= stratLine && reader.getLineNumber() <= endLine) {
                    result.append(lineContent).append("\n");
                }
                lineContent = reader.readLine();
            }
            return beautifyString(result.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getFileSpecifiedRange(String filePath, int startLine, int endLine, Set<Integer> deleteLines, List<CallMethodParams> callMethodParamsList) {
        try (FileReader in = new FileReader(filePath); LineNumberReader reader = new LineNumberReader(in)) {
            StringBuilder result = new StringBuilder();
            String lineContent = reader.readLine();
            while (lineContent != null) {
                int spaceLength = lineContent.replaceAll("([]*).*", "$1").length();
                if (reader.getLineNumber() >= startLine && reader.getLineNumber() <= endLine && !deleteLines.contains((reader.getLineNumber()))) {
                    result.append(lineContent).append("\n");
                }
                callMethodParamsList.stream().filter(callMethodParams -> Objects.equals(callMethodParams.getStartLine(), reader.getLineNumber())).findFirst().ifPresent(callMethodParams -> {
                    result.append(String.format("%1$" + spaceLength + "s", "")).append(callMethodParams.getContent()).append("\n");
                });
                lineContent = reader.readLine();
            }
            return beautifyString(result.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * read file
     */
    public String readFile(String filePath) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)),StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            log.warn("Failed to read the file.", e);
        }
        return builder.toString();
    }

    public String beautifyString(String result) {
        return StringUtils.removeEnd(result, "\n");
    }

    /**
     * write json
     */
    public void writeObjectJson(String path, Object fileResult) {
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }
        try (FileWriter writer = new FileWriter(file)) {
            String result = JSONObject.toJSONString(fileResult);
            writer.write(result);
            writer.flush();
        } catch (IOException e) {
            log.warn("Failed to write the file.", e);
        }
    }

    public List<File> readRepoPath(String repoPath)
    {
        File repoFile = new File(repoPath);
        List<File> paths = new ArrayList<>();
        getFilePathFromRepoPath(repoFile, paths);
        return paths;
    }
    
    /**
     * get java files
     */
    public void getFilePathFromRepoPath(File repoFile, List<File> filePaths) {
        File[] files = repoFile.listFiles();
        Optional.ofNullable(files).ifPresent(list -> Arrays.asList(files).forEach(file -> {
            if (file.isFile() && file.getName().endsWith("java")) {
                filePaths.add(file);
            }
            if (file.isDirectory()) {
                getFilePathFromRepoPath(file, filePaths);
            }
        }));
    }

    public boolean isExistFile(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /*
     * write json
     */
    public void writePairJson(FileResultInfo fileResult, RepoResult repoResult, String outputPath) {
        for(MatchResult matchResult : fileResult.getPairs_same_name()) {
            PairResult pairResult = new PairResult(fileResult, repoResult, matchResult, "Same Name");
            String path = outputPath + File.separator + repoResult.repo_id + File.separator + repoResult.repo_id + "_" + repoResult.extract_num + ".json";
            repoResult.extract_num++;
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdir();
            }
            try (FileWriter writer = new FileWriter(file)) {
                String result = JSONObject.toJSONString(pairResult);
                writer.write(result);
                writer.flush();
            } catch (IOException e) {
                log.warn("Failed to write the file.", e);
            }
        }
        for(MatchResult matchResult : fileResult.getPairs_diff_name()) {
            PairResult pairResult = new PairResult(fileResult, repoResult, matchResult, "Similar Name");
            String path = outputPath + File.separator + repoResult.repo_id + File.separator + repoResult.repo_id + "_" + repoResult.extract_num + ".json";
            repoResult.extract_num++;
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdir();
            }
            try (FileWriter writer = new FileWriter(file)) {
                String result = JSONObject.toJSONString(pairResult);
                writer.write(result);
                writer.flush();
            } catch (IOException e) {
                log.warn("Failed to write the file.", e);
            }
        }
        for(MatchResult matchResult : fileResult.getPairs_unique_call()) {
            PairResult pairResult = new PairResult(fileResult, repoResult, matchResult, "Unique Call");
            String path = outputPath + File.separator + repoResult.repo_id + File.separator + repoResult.repo_id + "_" + repoResult.extract_num + ".json";
            repoResult.extract_num++;
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdir();
            }
            try (FileWriter writer = new FileWriter(file)) {
                String result = JSONObject.toJSONString(pairResult);
                writer.write(result);
                writer.flush();
            } catch (IOException e) {
                log.warn("Failed to write the file.", e);
            }
        }
    }
}
