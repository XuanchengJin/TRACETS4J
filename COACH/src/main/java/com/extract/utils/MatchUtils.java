package com.extract.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import com.extract.info.*;

@Component
@Slf4j  
public class MatchUtils {
    @Autowired
    private FileResultInfo fileResultInfo;
    @Autowired
    private FileUtils fileUtils;
    public static int priMAX = Integer.MAX_VALUE;

    public void matchAndExtract(List<FileParseEntity> fileParseEntityList, String repoName, String outputInPairPath, RepoResult repoResult) {
        List<FileParseEntity> testFiles = fileParseEntityList.stream().filter(FileParseEntity::isTestFile).toList();
        log.info("Test File List Size: {}", testFiles.size());
        List<FileParseEntity> files = fileParseEntityList.stream().filter(fileParseEntity -> !fileParseEntity.isTestFile()).collect(Collectors.toList());
        log.info("File List Size: {}", files.size());
        for (FileParseEntity testFileParse : testFiles) {
            FileResultInfo fileResult = new FileResultInfo();
            fileResultInfo.buildTestFileResult(testFileParse, fileResult);
            files.stream().filter(file -> matchFile(testFileParse, file)).findFirst().ifPresent(filterFile -> {
                fileResultInfo.buildFileResult(filterFile, fileResult);
                matchSameAndContainMethod(testFileParse, filterFile, files, fileResult);
                matchConstructorMethod(testFileParse, filterFile, files, fileResult);
                matchUniuqeCallMethod(testFileParse, filterFile, files, fileResult);
                fileResult.extract_num = fileResult.pairs_same_name_num + fileResult.pairs_diff_name_num + fileResult.pairs_constructor_num + fileResult.pairs_unique_call_num;
                fileUtils.writePairJson(fileResult, repoResult, outputInPairPath);
            });
            // if (fileResult.extract_num > 0) {
            //     String fileResultPath = outputInFilePath + File.separator + repoResult.getRepo_id() + File.separator + testFileParse.getFileParams().getFileName() + ".json";
            //     File file = new File(fileResultPath);
            //     if (file.exists()) {
            //         int id = 1;
            //         fileResultPath = outputInFilePath + File.separator + repoResult.getRepo_id() + File.separator + testFileParse.getFileParams().getFileName() + id + ".json";
            //         file = new File(fileResultPath);
            //         while(file.exists()) {
            //             id ++;
            //             fileResultPath = outputInFilePath + File.separator + repoResult.getRepo_id() + File.separator + testFileParse.getFileParams().getFileName() + id + ".json";
            //             file = new File(fileResultPath);
            //         }
            //     }
            //     fileUtils.writeObjectJson(fileResultPath, fileResult);
            // }
        }
    }

    /**
     * fiie matching
     */
    public boolean matchFile(FileParseEntity testFile, FileParseEntity file) {
        String testFileName = testFile.getFileParams().getFileName().replace("Tests", "").replace("Test", "");
        String testBelongModuleName = testFile.getFileParams().getBelongModuleName().replace("\\test\\", "\\main\\");
        return Objects.equals(testBelongModuleName, file.getFileParams().getBelongModuleName()) && Objects.equals(testFile.getFileParams().getPackageText(), file.getFileParams().getPackageText()) && Objects.equals(testFileName, file.getFileParams().getFileName());
    }

    /** 
     * method matching
     */
    private void matchSameAndContainMethod(FileParseEntity testFileParse, FileParseEntity fileParse, List<FileParseEntity> files, FileResultInfo fileResult) {
        for (FunctionParams testFunction : testFileParse.getFunctionParamsList()) {
            String testName = testFunction.getFuncName().replace("tests", "").replace("test", "").replace("Tests", "").replace("Test", "").toLowerCase();
            if (!addToPairsSameName(fileParse, testName, testFunction, testFileParse, files, fileResult)) {
                addToPairsContainsName(fileParse, testName, testFunction, testFileParse, files, fileResult);            
            }
        }
    }

    /**
     * same name maching
     */
    private boolean addToPairsSameName(FileParseEntity fileParse, String testName, FunctionParams testFunction, FileParseEntity testFileParse, List<FileParseEntity> files, FileResultInfo fileResult) {
        List<FunctionParams> sameFocalFunctions = fileParse.getFunctionParamsList().stream().filter(function -> Objects.equals(testName, function.getFuncName().toLowerCase())).collect(Collectors.toList());
        List<CallMethodParams> sameCallFunctions = testFunction.getCallMethods().stream().filter(function -> Objects.equals(testName, function.getMethodName().toLowerCase())).collect(Collectors.toList()); 
        List<FunctionParams> matchList = filterFocalFromCallWithArgument(sameCallFunctions, sameFocalFunctions);
        if (matchList.size() > 0) {
            FunctionParams matchFunction = matchList.get(0);
            testFunction.setTestMethodName(matchFunction.getFuncName());
            MatchResult matchResult = createMatchResult(testFunction, matchFunction);
            fileResult.getPairs_same_name().add(matchResult);
            fileResult.pairs_same_name_num ++;
            testFunction.setExtracted(true);
            return true;
        }
        return false;
    }

    /**
     * signature check
     */
    private List<FunctionParams> filterFocalFromCallWithArgument(List<CallMethodParams> callList, List<FunctionParams> focalList) {
        List<FunctionParams> resultList = new ArrayList<>();
        List<FunctionParams> sameNameAndArgsCountFocalList = new ArrayList<>();
        for(CallMethodParams callFunction : callList) {
            sameNameAndArgsCountFocalList.clear();
            for(FunctionParams focalFunction : focalList) {
                if(Objects.equals(callFunction.getMethodName().toLowerCase(), focalFunction.getFuncName().toLowerCase()) && (callFunction.getArgsCount() == focalFunction.getArgumentParams().size())) {
                    sameNameAndArgsCountFocalList.add(focalFunction);
                }
            }
            if(sameNameAndArgsCountFocalList.size() == 0) {
                continue;
            }
            else if(sameNameAndArgsCountFocalList.size() == 1) {
                if(!resultList.contains(sameNameAndArgsCountFocalList.get(0))) {
                    resultList.add(sameNameAndArgsCountFocalList.get(0));
                }
            }
            else if(sameNameAndArgsCountFocalList.size() > 1) {
                FunctionParams chooseFunction = null;
                int[] priorityChoose;
                priorityChoose = new int[callFunction.getArgsCount()];
                for (int i = 0; i < priorityChoose.length; i++) {
                    priorityChoose[i] = priMAX;
                }
                for(FunctionParams possibleFunction : sameNameAndArgsCountFocalList) {
                    int[] priorityPossible = getPriorityList(possibleFunction, callFunction);
                    for (int i = 0; i < priorityChoose.length; i++) {
                        if(priorityPossible[i] < priorityChoose[i]) {
                            chooseFunction = possibleFunction;
                            for (int j = 0; j < priorityChoose.length; j++) {
                                priorityChoose[j] = priorityPossible[j];
                            }
                            break;
                        }
                        else if(priorityPossible[i] == priorityChoose[i]) {
                            continue;
                        }
                        else if(priorityPossible[i] > priorityChoose[i]) {
                            break;
                        }
                    }
                }
                if(chooseFunction == null) {
                    if(!resultList.contains(sameNameAndArgsCountFocalList.get(0))) {
                        resultList.add(sameNameAndArgsCountFocalList.get(0));
                        log.info("Dangerous Choose!");
                    }
                }
                else {
                    if(!resultList.contains(chooseFunction)) {
                        resultList.add(chooseFunction);
                        log.info("Safe Choose!");
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * differ name matching
     */
    private void addToPairsContainsName(FileParseEntity fileParse, String testName, FunctionParams testFunction, FileParseEntity testFileParse, List<FileParseEntity> files, FileResultInfo fileResult) {
        List<FunctionParams> containFunctions = fileParse.getFunctionParamsList().stream().filter(function -> testName.contains(function.getFuncName().toLowerCase()) && !Objects.equals(testName, function.getFuncName().toLowerCase())).collect(Collectors.toList());
        List<FunctionParams> resultList = filterFocalFromCallWithArgument(testFunction.getCallMethods(), containFunctions);
        if (resultList.size() > 0) {
            FunctionParams matchFunction = resultList.get(0);
            testFunction.setTestMethodName(matchFunction.getFuncName());
            MatchResult matchResult = createMatchResult(testFunction, matchFunction);
            fileResult.getPairs_diff_name().add(matchResult);
            fileResult.pairs_diff_name_num ++;
            testFunction.setExtracted(true);
        }
    }

    /**
     * constructor matching
     */
    private void matchConstructorMethod(FileParseEntity testFileParse, FileParseEntity fileParse, List<FileParseEntity> files, FileResultInfo fileResult) {
        for (FunctionParams testFunction : testFileParse.getFunctionParamsList()) {
            if(testFunction.isExtracted() == false) {
                String testName = testFunction.getFuncName().replace("tests", "").replace("test", "").replace("Tests", "").replace("Test", "").toLowerCase();
                if(testName.contains("construct")||testName.contains("constructor")||testName.contains("create")||testName.contains("creator")) {
                    addToPairsConstructor(fileParse, testFunction, testFileParse, files, fileResult, "");
                }
                else {
                    for(ClassParams classParams : fileParse.getClassParamsList()) {
                        if(Objects.equals(classParams.getClassName().toLowerCase(), testName)) {
                            addToPairsConstructor(fileParse, testFunction, testFileParse, files, fileResult, testName);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void addToPairsConstructor(FileParseEntity fileParse, FunctionParams testFunction, FileParseEntity testFileParse, List<FileParseEntity> files, FileResultInfo fileResult, String constructorName) {
        List<CallMethodParams> possibleCallConstructorList = new ArrayList<>();
        if(constructorName == "") {
            possibleCallConstructorList = testFunction.getCallConstructorMethods();
        }
        else {
            for (CallMethodParams callMethodParams : testFunction.getCallConstructorMethods()) {
                if(Objects.equals(constructorName, callMethodParams.getMethodName().toLowerCase())) {
                    possibleCallConstructorList.add(callMethodParams);
                }
            }
        }
        List<FunctionParams> focalList = fileParse.getFunctionParamsList();
        List<FunctionParams> resultList = filterFocalFromCallWithArgument(possibleCallConstructorList, focalList);
        if (resultList.size() > 0) {
            FunctionParams matchFunction = resultList.get(0);
            testFunction.setTestMethodName(matchFunction.getFuncName());
            MatchResult matchResult = createMatchResult(testFunction, matchFunction);
            fileResult.getPairs_constructor().add(matchResult);
            fileResult.pairs_constructor_num ++;
            testFunction.setExtracted(true);
        }
    }

    /** 
     * unique call matching
     */
    private void matchUniuqeCallMethod(FileParseEntity testFileParse, FileParseEntity fileParse, List<FileParseEntity> files, FileResultInfo fileResult) {
        for (FunctionParams testFunction : testFileParse.getFunctionParamsList()) {
            if(testFunction.isExtracted() == false) {
                addToPairsUniqueCall(fileParse, testFunction, testFileParse, files, fileResult);
            }
        }
    }

    private void addToPairsUniqueCall(FileParseEntity fileParse, FunctionParams testFunction, FileParseEntity testFileParse, List<FileParseEntity> files, FileResultInfo fileResult) {
        List<FunctionParams> calledAndParameterMatchedFunctionList = filterFocalFromCallWithArgument(testFunction.getCallMethods(), fileParse.getFunctionParamsList());
        log.info("function:{}, calledAndParameterMatchedFunctionList size:{}",testFunction.getFuncName(),calledAndParameterMatchedFunctionList.size());
        if (calledAndParameterMatchedFunctionList.size() == 1) {
            FunctionParams matchFunction = calledAndParameterMatchedFunctionList.get(0);
            testFunction.setTestMethodName(matchFunction.getFuncName());
            MatchResult matchResult = createMatchResult(testFunction, matchFunction);
            fileResult.getPairs_unique_call().add(matchResult);
            fileResult.pairs_unique_call_num ++;
            testFunction.setExtracted(true);
        }   
        else if(calledAndParameterMatchedFunctionList.size() > 1) {
            List<FunctionParams> classUniqueList = new ArrayList<>();
            String testClassName = testFunction.getBelongClassName().replace("tests", "").replace("test", "").replace("Tests", "").replace("Test", "").toLowerCase();
            for(FunctionParams calledAndParameterMatchedFunction : calledAndParameterMatchedFunctionList) {
                if(calledAndParameterMatchedFunction.getBelongClassName() != null && Objects.equals(calledAndParameterMatchedFunction.getBelongClassName().toLowerCase(), testClassName)) 
                {
                    classUniqueList.add(calledAndParameterMatchedFunction);
                }
            }
            if (classUniqueList.size() == 1) {
                FunctionParams matchFunction = classUniqueList.get(0);
                testFunction.setTestMethodName(matchFunction.getFuncName());
                MatchResult matchResult = createMatchResult(testFunction, matchFunction);
                fileResult.getPairs_unique_call().add(matchResult);
                fileResult.pairs_unique_call_num ++;
                testFunction.setExtracted(true);
            }       
        }
    }

    private int[] getPriorityList(FunctionParams possibleCalledFunction, CallMethodParams callFunction) {
        int[] pri = new int[callFunction.getArgsCount()];
        for (int index = 0; index < possibleCalledFunction.getArgumentParams().size(); index++) {
            pri[index] = getPriority(possibleCalledFunction.getArgumentParams().get(index).getDataType(), callFunction.getArgumentParamsList().get(index).getDataType());
        }
        return pri;
    }

    private int getPriority(String focalType, String callType) {
        String focalToLowCase = focalType.toLowerCase();
        String callToLowCase = callType.toLowerCase();
        if(Objects.equals(focalToLowCase, callToLowCase)) {
            return 0;
        }
        if(focalToLowCase.contains(callToLowCase)) {
            return priMAX - callToLowCase.length();
        }
        if(callToLowCase.contains(focalToLowCase)) {
            return priMAX - focalToLowCase.length();
        }
        if(focalToLowCase.equals("object") || callToLowCase.equals("object")) {
            return 1;
        }
        if(Objects.equals(callType.toLowerCase(), "integer")) {
            if(Objects.equals(focalType, "int") || Objects.equals(focalType, "long"))
                return 1;
        }
        if(Objects.equals(callType.toLowerCase(), "int")) {
            if(Objects.equals(focalType, "long"))
                return 1;
        }
        if(Objects.equals(callType.toLowerCase(), "string")) {
            if(Objects.equals(focalType, "CharSequence" ))
                return 1;
        }
        if(Objects.equals(callType.toLowerCase(), "")) {
            if(Objects.equals(focalType, "Atom[]"))
                return 1;
        }
        return priMAX; 
    }

    private MatchResult createMatchResult(FunctionParams test, FunctionParams file) {
        MatchResult matchResult = new MatchResult();
        matchResult.setFunc_info(new FunctionResult(file));
        matchResult.setTest_info(new FunctionResult(test));
        ContextResult contextResult = new ContextResult();
        contextResult.setFunc_class(file.getBelongClassName());
        contextResult.setTest_class(test.getBelongClassName());
        matchResult.setContext(contextResult);
        return matchResult;
    }
}
