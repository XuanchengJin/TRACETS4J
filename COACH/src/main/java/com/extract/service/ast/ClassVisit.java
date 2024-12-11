package com.extract.service.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.extract.info.ClassParams;
import com.extract.info.FileParseEntity;
import com.extract.utils.FileUtils;
import com.extract.utils.ParseUtils;

import java.util.Objects;
import java.util.Optional;;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClassVisit extends ASTVisitor{
    private FileParseEntity fileParseEntity;
    
    private CompilationUnit compilationUnit;

    private ParseUtils parseUtils;
    
    private FileUtils fileUtils;

    public ClassVisit() {
    }

    public ClassVisit(CompilationUnit compilationUnit, FileParseEntity fileParseEntity, ParseUtils parseUtils, FileUtils fileUtils) {
        this.compilationUnit = compilationUnit;
        this.fileParseEntity = fileParseEntity;
        this.parseUtils = parseUtils;
        this.fileUtils = fileUtils;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        if(node.getParent() instanceof CompilationUnit || node.getParent() instanceof TypeDeclaration) {
            ClassParams classParams = new ClassParams();
            classParams.setClassName(parseUtils.nodeToText(node.getName()));
            classParams.setClassDeclareText(dealInnerClassDeclare(node));

            Optional.ofNullable(node.superInterfaceTypes()).ifPresent(list -> {
                list.forEach(text -> classParams.getInheritancesText().add(parseUtils.nodeToText((ASTNode) text)));
            });
            Optional.ofNullable(node.getSuperclassType()).ifPresent(superClass -> {
                classParams.getInheritancesText().add(parseUtils.nodeToText(superClass));
            });
            InnerVisit innerVisit = new InnerVisit(classParams);
            node.accept(innerVisit);
            VariablesVisit variablesVisit = new VariablesVisit(compilationUnit, classParams, fileParseEntity.getFileParams().getFilePath(), parseUtils);
            node.accept(variablesVisit);
            fileParseEntity.getClassParamsList().add(classParams);
        }
        return true;
    }

    /**
     * Handling declaration expressions for inner classes
     */
    private String dealInnerClassDeclare(TypeDeclaration node) {
        String result = "";
        if (node.getParent() instanceof TypeDeclaration) {
            result = dealInnerClassDeclare((TypeDeclaration) node.getParent()) + "###";
        }
        Javadoc javadoc = node.getJavadoc();
        int classDeclareStartLine = Objects.isNull(javadoc) ? parseUtils.nodeStartLine(compilationUnit, node) : parseUtils.nodeEndLine(compilationUnit, javadoc) + 1;
        int classDeclareEndLine = ObjectUtils.isEmpty(node.bodyDeclarations()) ? parseUtils.nodeEndLine(compilationUnit, node) : parseUtils.nodeStartLine(compilationUnit, (ASTNode) node.bodyDeclarations().get(0)) -1;
        return result + fileUtils.dealDeclareString(fileParseEntity.getFileParams().getFilePath(), classDeclareStartLine, classDeclareEndLine);
    }

    class InnerVisit extends ASTVisitor {
        ClassParams classParams;

        public InnerVisit(ClassParams classParams) {
            this.classParams = classParams;
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            if (node.getParent() instanceof TypeDeclaration) {
                TypeDeclaration parent = (TypeDeclaration) node.getParent();
                if (Objects.equals(parseUtils.nodeToText(parent.getName()), this.classParams.getClassName())) {
                    Javadoc javadoc = node.getJavadoc();
                    int functionDeclareStartLine = Objects.isNull(javadoc) ? parseUtils.nodeStartLine(compilationUnit, node) : parseUtils.nodeEndLine(compilationUnit, javadoc) + 1;
                    int functionDeclareEndLine = Objects.isNull(node.getBody()) ? parseUtils.nodeEndLine(compilationUnit, node) : parseUtils.nodeStartLine(compilationUnit, node.getBody());
                    if (node.isConstructor()) {
                        classParams.getConstructMethodDeclareText().add(fileUtils.dealDeclareString(fileParseEntity.getFileParams().getFilePath(), functionDeclareStartLine, functionDeclareEndLine));
                    } else {
                        classParams.getMethodDeclareText().add(fileUtils.dealDeclareString(fileParseEntity.getFileParams().getFilePath(), functionDeclareStartLine, functionDeclareEndLine));
                    }
                } 
            }
            return true;
        }
    }
}
