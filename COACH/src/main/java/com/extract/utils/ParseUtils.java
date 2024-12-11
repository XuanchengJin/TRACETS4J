package com.extract.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.extract.info.VariablesParams;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ParseUtils {
    @Autowired
    private FileUtils fileUtils;

    public int nodeStartLine(CompilationUnit compilationUnit, ASTNode node) {
        return compilationUnit.getLineNumber(node.getStartPosition());
    }

    public int nodeEndLine(CompilationUnit compilationUnit, ASTNode node) {
        return compilationUnit.getLineNumber(node.getStartPosition() + node.getLength() - 1);
    }

    public String nodeToText(ASTNode node) {
        return fileUtils.beautifyString(node.toString());
    }

    public String belongClass(String content, List<VariablesParams> variablesParamsList) {
        if (StringUtils.isNumeric(content)) {
            return "Integer";
        } 
        else if (Objects.equals(content, "true") || Objects.equals(content, "false")) {
            return "boolean";
        } 
        else if (content.startsWith("new")) {
            int startIndex = content.indexOf("new") + "new".length();
            int endIndex = content.indexOf("(");
            if (startIndex >= 0 && endIndex >= 0 && startIndex < endIndex) {
                String result = content.substring(startIndex, endIndex);
                return result;
            }
        }
        else if (content.contains("\"")) {
            return "String";
        }
        String variable = content.contains(".") ? content.split("\\.")[0] : content;
        List<VariablesParams> collect = variablesParamsList.stream().filter(variablesParams -> Objects.equals(variablesParams.getName(), variable)).collect(Collectors.toList());
        return ObjectUtils.isNotEmpty(collect) ? collect.get(0).getType() : "";
    }
}
