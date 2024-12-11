package com.extract.service.ast;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.extract.info.FileParams;
import com.extract.info.FileParseEntity;
import com.extract.info.FunctionParams;
import com.extract.info.VariablesParams;
import com.extract.utils.FileUtils;
import com.extract.utils.ParseUtils;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Parsing {
    @Autowired
    private ParseUtils parseUtils;
    @Autowired
    private FileUtils fileUtils;

    /**
     * parsing
     */
    public FileParseEntity parsing(String filePath) {

        CompilationUnit result = createCompilationUnit(filePath);

        FileParseEntity fileParseEntity = new FileParseEntity();
        FileParams fileParams = getFileParams(result, filePath);
        fileParseEntity.setFileParams(fileParams);

        ClassVisit classVisit = new ClassVisit(result, fileParseEntity, parseUtils, fileUtils);
        result.accept(classVisit);
        
        FunctionVisit functionVisit = new FunctionVisit(result, fileParseEntity, parseUtils, fileUtils);
        result.accept(functionVisit);

        dealFileParseEntity(fileParseEntity);

        return fileParseEntity;
    }

    private CompilationUnit createCompilationUnit(String filePath) {
        ASTParser parser = ASTParser.newParser(AST.JLS11);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        Map<String, String> compilerOptions = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, compilerOptions);
        parser.setCompilerOptions(compilerOptions);
        char[] sourse = fileUtils.readFile(filePath).toCharArray();
        parser.setSource(sourse);
        return (CompilationUnit) parser.createAST(null);
    }

    private FileParams getFileParams(CompilationUnit unit, String filePath) {
        FileParams fileParams = new FileParams();
        fileParams.setFilePath(filePath);
        Optional.ofNullable(unit.getPackage()).ifPresent(unitPackage -> {
            int startLine = Objects.isNull(unitPackage.getJavadoc()) ? parseUtils.nodeStartLine(unit, unitPackage) : parseUtils.nodeEndLine(unit, unitPackage.getJavadoc()) + 1;
            int endLine = parseUtils.nodeEndLine(unit, unitPackage);
            fileParams.setPackageText(fileUtils.dealDeclareString(filePath, startLine, endLine));
        });
        File file = new File(filePath);
        fileParams.setFileName(file.getName().substring(0, file.getName().lastIndexOf(".")));
        fileParams.setBelongModuleName(file.getParent());

        Optional.ofNullable(unit.imports()).ifPresent(list -> list.forEach(text -> {
            ImportDeclaration node = (ImportDeclaration) text;
            fileParams.getImportTexts().add(fileUtils.getFileSpecifiedRange(filePath, parseUtils.nodeStartLine(unit, node), parseUtils.nodeStartLine(unit, node)));
        }));
        return fileParams;
    }

    public void dealFileParseEntity(FileParseEntity fileParseEntity) {
        fileParseEntity.getFunctionParamsList().forEach(functionParams -> {
            List<VariablesParams> variablesParamsList = functionParams.getVariablesParams();
            fileParseEntity.getClassParamsList().forEach(classParams -> {
                variablesParamsList.addAll(classParams.getVariablesParamsList());
            });
            functionParams.getCallMethods().forEach(callMethodParams -> {
                callMethodParams.getArgumentParamsList().forEach(argumentParams -> {
                    argumentParams.setDataType(parseUtils.belongClass(argumentParams.getContent(), variablesParamsList));
                });
            });
            functionParams.getCallConstructorMethods().forEach(callConstructorMethodParams -> {
                callConstructorMethodParams.getArgumentParamsList().forEach(argumentParams -> {
                    argumentParams.setDataType(parseUtils.belongClass(argumentParams.getContent(), variablesParamsList));
                });
            });
        });

        List<FunctionParams> collect = fileParseEntity.getFunctionParamsList().stream().filter(FunctionParams::isTestMethod).collect(Collectors.toList());
        if (ObjectUtils.isNotEmpty(collect)) {
            fileParseEntity.setFunctionParamsList(collect);
            fileParseEntity.setTestFile(true);
        }
    }
}


