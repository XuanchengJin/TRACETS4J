package com.extract.service.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import com.extract.info.ArgumentParams;
import com.extract.info.CallMethodParams;
import com.extract.info.ClassParams;
import com.extract.info.FileParseEntity;
import com.extract.info.FunctionParams;
import com.extract.info.VariablesParams;
import com.extract.utils.FileUtils;
import com.extract.utils.ParseUtils;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class FunctionVisit extends ASTVisitor{
    private FileParseEntity fileParseEntity;

    private CompilationUnit compilationUnit;

    private ParseUtils parseUtils;

    private FileUtils fileUtils;

    public FunctionVisit() {
    }

    public FunctionVisit(CompilationUnit compilationUnit, FileParseEntity fileParseEntity, ParseUtils parseUtils, FileUtils fileUtils) {
        this.fileParseEntity = fileParseEntity;
        this.compilationUnit = compilationUnit;
        this.parseUtils = parseUtils;
        this.fileUtils = fileUtils;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        FunctionParams functionParams = new FunctionParams();
        functionParams.setFilePath(fileParseEntity.getFileParams().getFilePath());
        functionParams.setFuncName(parseUtils.nodeToText(node.getName()));
        functionParams.setStartLine(parseUtils.nodeStartLine(compilationUnit, node));
        functionParams.setEndLine(parseUtils.nodeEndLine(compilationUnit, node));
        if (Objects.nonNull(node.getBody())) {
            functionParams.setBodyStartLine(parseUtils.nodeStartLine(compilationUnit, node.getBody()) + 1);
            functionParams.setBodyEndLine(parseUtils.nodeEndLine(compilationUnit, node.getBody()) - 1);
        }
        if (node.getParent() instanceof TypeDeclaration) {
            TypeDeclaration parent = (TypeDeclaration) node.getParent();
            List<ClassParams> collect = fileParseEntity.getClassParamsList().stream().filter(classParams -> Objects.equals(classParams.getClassName(), parseUtils.nodeToText(parent.getName()))).collect(Collectors.toList());
            if (ObjectUtils.isNotEmpty(collect)) {
                functionParams.setBelongClassDeclareText(collect.get(0).getClassDeclareText());
                functionParams.setBelongClassName(collect.get(0).getClassName());
            }
        }
        Optional.ofNullable(node.modifiers()).ifPresent(list -> {
            list.forEach(text -> {
                if (text instanceof MarkerAnnotation) {
                    functionParams.getAnnotationText().add(parseUtils.nodeToText((MarkerAnnotation) text));
                }
                if (text instanceof Modifier) {
                    functionParams.getSpecifierText().add(parseUtils.nodeToText((Modifier) text));
                }
            });
        });
        Optional.ofNullable(node.parameters()).ifPresent(list -> {
            list.forEach(text -> {
                SingleVariableDeclaration declaration = (SingleVariableDeclaration) text;
                ArgumentParams argumentParams = new ArgumentParams();
                argumentParams.setContent(parseUtils.nodeToText(declaration));
                argumentParams.setDataType(parseUtils.nodeToText(declaration.getType()));
                functionParams.getArgumentParams().add(argumentParams);
            });
        });
        Optional.ofNullable(node.getReturnType2()).ifPresent(returnClass -> {
            functionParams.setFuncReturnClass(parseUtils.nodeToText(returnClass));
        });
        Optional.ofNullable(node.getJavadoc()).ifPresent(javadoc -> {
            functionParams.setComment(fileUtils.getFileSpecifiedRange(fileParseEntity.getFileParams().getFilePath(), parseUtils.nodeStartLine(compilationUnit, javadoc), parseUtils.nodeEndLine(compilationUnit, javadoc)));
        });
        int functionDeclareStartLine = Objects.isNull(node.getJavadoc()) ? parseUtils.nodeStartLine(compilationUnit, node) : parseUtils.nodeEndLine(compilationUnit, node.getJavadoc()) + 1;
        functionParams.setFuncContent(fileUtils.getFileSpecifiedRange(fileParseEntity.getFileParams().getFilePath(), functionDeclareStartLine, functionParams.getEndLine()));
        InnerVisit innerVisit = new InnerVisit(functionParams);
        node.accept(innerVisit);
        if (functionParams.getAnnotationText().contains("@Test")) {
            functionParams.setTestMethod(true);
        }
        fileParseEntity.getFunctionParamsList().add(functionParams);
        return true;
    }

    public class InnerVisit extends ASTVisitor {
        public FunctionParams functionParams;

        public InnerVisit(FunctionParams functionParams) {
            this.functionParams = functionParams;
        }

        @Override
        public boolean visit(ReturnStatement node) {
            this.functionParams.getReturnText().add(parseUtils.nodeToText(node));
            return true;
        }

        /**
         * call function
         */
        @Override
        public boolean visit(MethodInvocation node) {
            String methodName = parseUtils.nodeToText(node.getName());
            CallMethodParams callMethodParams = Objects.equals(methodName, "invokeMethod") ? createCallMethodFromInvokeMethod(node) : createCallMethod(node);
            functionParams.getCallMethods().add(callMethodParams);
            return true;
        }

        /**
         * call constructor
         */
        @Override
        public boolean visit(ClassInstanceCreation node) {
            CallMethodParams callMethodParams = new CallMethodParams();
            callMethodParams.setMethodName(parseUtils.nodeToText(node.getType()));
            callMethodParams.setContent(parseUtils.nodeToText(node));
            callMethodParams.setStartLine(parseUtils.nodeStartLine(compilationUnit, node));
            callMethodParams.setEndLine(parseUtils.nodeEndLine(compilationUnit, node));
            Optional.ofNullable(node.arguments()).ifPresent(list -> {
                callMethodParams.setArgsCount(list.size());
                list.forEach(text -> {
                    ArgumentParams argumentParams = new ArgumentParams();
                    argumentParams.setContent(parseUtils.nodeToText((ASTNode) text));
                    callMethodParams.getArgumentParamsList().add(argumentParams);
                });
            });
            functionParams.getCallConstructorMethods().add(callMethodParams);
            return true;
        }

        @Override
        public boolean visit(VariableDeclarationStatement node) {
            VariablesParams variablesParams = new VariablesParams();
            variablesParams.setStartLine(parseUtils.nodeStartLine(compilationUnit, node));
            variablesParams.setEndLine(parseUtils.nodeEndLine(compilationUnit, node));
            variablesParams.setContent(parseUtils.nodeToText(node));
            variablesParams.setType(parseUtils.nodeToText(node.getType()));
            if (ObjectUtils.isNotEmpty(node.fragments())) {
                VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.fragments().get(0);
                variablesParams.setName(parseUtils.nodeToText(fragment.getName()));
            }
            functionParams.getVariablesParams().add(variablesParams);
            return true;
        }
        
        private CallMethodParams createCallMethod(MethodInvocation node) {
            CallMethodParams callMethodParams = new CallMethodParams();
            callMethodParams.setMethodName(parseUtils.nodeToText(node.getName()));
            callMethodParams.setContent(parseUtils.nodeToText(node));
            callMethodParams.setStartLine(parseUtils.nodeStartLine(compilationUnit, node));
            callMethodParams.setEndLine(parseUtils.nodeEndLine(compilationUnit, node));
            Optional.ofNullable(node.arguments()).ifPresent(list -> {
                callMethodParams.setArgsCount(list.size());
                list.forEach(text -> {
                    ArgumentParams argumentParams = new ArgumentParams();
                    argumentParams.setContent(parseUtils.nodeToText((ASTNode) text));
                    callMethodParams.getArgumentParamsList().add(argumentParams);
                });
            });
            return callMethodParams;
        }
    }

    private CallMethodParams createCallMethodFromInvokeMethod(MethodInvocation node) {
        List arguments = node.arguments();
        CallMethodParams callMethodParams = new CallMethodParams();
        callMethodParams.setStartLine(parseUtils.nodeStartLine(compilationUnit, node));
        callMethodParams.setEndLine(parseUtils.nodeEndLine(compilationUnit, node));
        callMethodParams.setContent(parseUtils.nodeToText(node));
        if (arguments.size() >= 2) {
            String methodName = parseUtils.nodeToText((ASTNode) arguments.get(1));
            callMethodParams.setMethodName(methodName.replace("\"", ""));
            callMethodParams.setArgsCount(arguments.size() - 2);
            for (int index = 2; index < arguments.size(); index++) {
                ArgumentParams argumentParams = new ArgumentParams();
                argumentParams.setContent(parseUtils.nodeToText((ASTNode) arguments.get(index)));
                callMethodParams.getArgumentParamsList().add(argumentParams);
            }
        }
        return callMethodParams;
    }
}
