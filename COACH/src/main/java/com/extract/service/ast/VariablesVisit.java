package com.extract.service.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import com.extract.info.ClassParams;
import com.extract.info.VariablesParams;
import com.extract.utils.ParseUtils;
import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = true)
public class VariablesVisit extends ASTVisitor{
    private ClassParams classParams;

    private CompilationUnit compilationUnit;

    private String filePath;

    private ParseUtils parseUtils;

    public VariablesVisit() {
    }

    public VariablesVisit(CompilationUnit compilationUnit, ClassParams classParams, String filePath, ParseUtils parseUtils) {
        this.compilationUnit = compilationUnit;
        this.classParams = classParams;
        this.filePath = filePath;
        this.parseUtils = parseUtils;
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        if (node.getParent() instanceof TypeDeclaration) {
            TypeDeclaration parent = (TypeDeclaration) node.getParent();
            if (Objects.equals(parseUtils.nodeToText(parent.getName()), this.classParams.getClassName())) {
                VariablesParams variablesParams = new VariablesParams();
                variablesParams.setStartLine(parseUtils.nodeStartLine(compilationUnit, node));
                variablesParams.setEndLine(parseUtils.nodeEndLine(compilationUnit, node));
                int variableStartLine = Objects.isNull(node.getJavadoc()) ? parseUtils.nodeStartLine(compilationUnit, node) : parseUtils.nodeEndLine(compilationUnit, node.getJavadoc()) + 1;
                variablesParams.setContent(parseUtils.nodeToText(node));
                variablesParams.setType(parseUtils.nodeToText(node.getType()));
                if (ObjectUtils.isNotEmpty(node.fragments())) {
                    VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.fragments().get(0);
                    variablesParams.setName(parseUtils.nodeToText(fragment.getName()));
                }
                this.classParams.getVariablesParamsList().add(variablesParams);
            }
        }
        return true;
    }
}
