package com.extract.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.extract.info.CallMethodParams;
import com.extract.info.FileParseEntity;
import com.extract.info.FunctionParams;
import com.extract.info.cpg.CpgNodeEntity;
import com.extract.info.cpg.CpgParams;
import com.extract.service.cpgservice.CpgApi;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CspRuleUtils {
    @Autowired
    private CsvFileUtils csvFileUtils;
    @Autowired
    private CpgApi cpgApi;
    @Autowired
    private FileUtils fileUtils;

    @Value("${cpg.api.url}")
    private String cpgApiUrl;

    public void testFileToCpgResult(FileParseEntity testFileParse, String repoName, String outputPath) {
        String fileResultPath = outputPath + File.separator + repoName + File.separator + testFileParse.getFileParams().getFileName() + "_strip.json";
        CpgParams cpgParams = new CpgParams(testFileParse.getFileParams().getFilePath(), repoName);
        String filePath = cpgApi.callCPGAPI(cpgApiUrl, cpgParams);
        String resultPath = cpgParams.getResultpath() + File.separator + filePath;
        if (fileUtils.isExistFile(resultPath + File.separator + "nodes.csv")) {
            List<CpgNodeEntity> cpgNodeEntityList = csvFileUtils.readNodeCsv(resultPath + File.separator + "node.csv");
            Map<Long, List<Long>> cpgEdgesMap = csvFileUtils.readEdgesCsv(resultPath + File.separator + "edges.csv");
            List<FunctionParams> matchTestFunctionList = testFileParse.getFunctionParamsList().stream().filter(functionParams -> Objects.nonNull(functionParams.getTestMethodName())).collect(Collectors.toList());
            matchTestFunctionList.forEach(functionParams -> {
                cspRuleDeal(functionParams, cpgNodeEntityList, cpgEdgesMap);
            });
            outputTestFunctionStrip(matchTestFunctionList, fileResultPath);
        }
    }

    private void cspRuleDeal(FunctionParams testMethod, List<CpgNodeEntity> cpgNodeEntityList, Map<Long, List<Long>> cpgEdgesMap) {
        Set<Integer> nonDeleteLine = new HashSet<>();
        for (CallMethodParams callMethodParams : testMethod.getCallMethods()) {
            if (callMethodParams.getContent().contains("mock")) {
                for (int current = callMethodParams.getStartLine(); current <= callMethodParams.getEndLine(); current++) {
                    nonDeleteLine.add(current);
                }
            }
        }
        List<CallMethodParams> testCallMethods = testMethod.getCallMethods().stream().filter(callMethodParams -> Objects.equals(callMethodParams.getMethodName(), testMethod.getTestMethodName())).collect(Collectors.toList());
        nonDeleteLine.addAll(callMethodAssociated(testCallMethods, cpgNodeEntityList, cpgEdgesMap));
        List<CallMethodParams> assertCallMethods = testMethod.getCallMethods().stream().filter(callMethodParams -> callMethodParams.getMethodName().toLowerCase().contains("assert")).collect(Collectors.toList());
        Set<Integer> assertDeleteLine = callMethodAssociated(assertCallMethods,cpgNodeEntityList, cpgEdgesMap);
        Set<Integer> deleteLineList = assertDeleteLine.stream().filter(current -> !nonDeleteLine.contains(current)).collect(Collectors.toSet());
        List<CallMethodParams> callMethodParamsList = matchCallMethod(testCallMethods, assertCallMethods);
        for (CallMethodParams callMethodParams : callMethodParamsList) {
            for (int current = callMethodParams.getStartLine(); current <= callMethodParams.getEndLine(); current++) {
                deleteLineList.add(current);
            }
        }
        testMethod.setFuncContent(fileUtils.getFileSpecifiedRange(testMethod.getFilePath(), testMethod.getStartLine(), testMethod.getEndLine(), deleteLineList, callMethodParamsList));
        testMethod.setBodyContent(fileUtils.getFileSpecifiedRange(testMethod.getFilePath(), testMethod.getBodyStartLine(), testMethod.getBodyEndLine(), deleteLineList, callMethodParamsList));
    }

    private List<CallMethodParams> matchCallMethod(List<CallMethodParams> testCallMethods, List<CallMethodParams> assertCallMethods) {
        List<Integer> assertStartLineList = assertCallMethods.stream().map(CallMethodParams::getStartLine).collect(Collectors.toList());
        return testCallMethods.stream().filter(callMethodParams -> assertStartLineList.contains(callMethodParams.getStartLine())).collect(Collectors.toList());
    }

    private Set<Integer> callMethodAssociated(List<CallMethodParams> callMethodParamsList, List<CpgNodeEntity> cpgNodeEntityList, Map<Long, List<Long>> cpgEdgesMap) {
        Set<Integer> callMethodLines = new HashSet<>();
        if (ObjectUtils.isNotEmpty(cpgNodeEntityList)) {
            for (CallMethodParams callMethodParams : callMethodParamsList) {
                String methodName = callMethodParams.getMethodName();
                if (callMethodParams.getContent().contains("invokeMethod")) {
                    methodName = "invokeMethod";
                }
                Set<Long> callAssociatedSet = searchAssociated(callMethodParams.getStartLine(), methodName, cpgNodeEntityList, cpgEdgesMap);
                List<CpgNodeEntity> nodeAssociated = cpgNodeEntityList.stream().filter(cpgNodeEntity -> callAssociatedSet.contains(cpgNodeEntity.getKey())).collect(Collectors.toList());
                for (CpgNodeEntity cpgNodeEntity : nodeAssociated) {
                    for (int current = cpgNodeEntity.getStartLine(); current <= cpgNodeEntity.getEndLine(); current++) {
                        callMethodLines.add(current);
                    }
                }
            }
        }
        return callMethodLines;
    }

    private Set<Long> searchAssociated(Integer line, String name, List<CpgNodeEntity> cpgNodeEntityList, Map<Long, List<Long>> cpgEdgesMap) {
        Set<Long> resultNodes = new HashSet<>();

        List<Long> nodeKeys = cpgNodeEntityList.stream().filter(cpgNodeEntity -> Objects.equals(line, cpgNodeEntity.getLine()) && Objects.equals(name, cpgNodeEntity.getName())).map(CpgNodeEntity::getKey).collect(Collectors.toList());
        nodesAssociated(nodeKeys, resultNodes, cpgEdgesMap);
        return resultNodes;
    }

    private void nodesAssociated(List<Long> nodeKeys, Set<Long> resultNodes, Map<Long, List<Long>> cpgEdgesMap) {
        for (Long key : nodeKeys) {
            if (!resultNodes.contains(key)) {
                resultNodes.add(key);
                if (cpgEdgesMap.containsKey(key)) {
                    nodesAssociated(cpgEdgesMap.get(key), resultNodes, cpgEdgesMap);
                }
            }
        }
    }

    private void outputTestFunctionStrip(List<FunctionParams> testFunctionParams, String fileResultPath) {
        Map<String, String> map = new HashMap<>();
        Map<String, List<FunctionParams>> testMethodNameMap = testFunctionParams.stream().collect(Collectors.groupingBy(FunctionParams::getTestMethodName));
        testMethodNameMap.values().forEach(testMethodNameList -> {
            if (testMethodNameList.size() == 1) {
                String key = testMethodNameList.get(0).getBelongClassName() + "." + testMethodNameList.get(0).getFuncName();
                map.put(key, testMethodNameList.get(0).getFuncContent());
            } else {
                String key = testMethodNameList.get(0).getBelongClassName() + "." + testMethodNameList.get(0).getTestMethodName() + "Test";
                map.put(key, mergesTestFunctionBodyContent(testMethodNameList));
            }
        });
        fileUtils.writeObjectJson(fileResultPath, map);
    }

    /**
     * merge body
     */
    private String mergesTestFunctionBodyContent(List<FunctionParams> functionParamsList) {
        StringBuilder mergeBody = new StringBuilder();
        mergeBody.append("    @Test").append("\n").append("    public void ").append(functionParamsList.get(0).getTestMethodName()).append("Test () {").append("\n");
        functionParamsList.forEach(functionParams -> {
            mergeBody.append("\n").append(functionParams.getBodyContent()).append("\n");
        });
        mergeBody.append("    }");
        return mergeBody.toString();
    }
}

