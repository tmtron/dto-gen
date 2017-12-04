package com.tmtron.dtogen.processor;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * see: https://stackoverflow.com/a/9793567/6287240
 */
public class CodeScanner {
    private final Trees treesInstance;

    CodeScanner(ProcessingEnvironment processingEnv) {
        this.treesInstance = Trees.instance(processingEnv);
    }

    public String getFieldInitializerOrBlank(TypeElement parentOfField, String fieldName) {
        FieldScanner fieldScanner = new FieldScanner(fieldName);
        TreePath path = treesInstance.getPath(parentOfField);
        ExpressionTree fieldInitializer = fieldScanner.scan(path, treesInstance);
        if (fieldInitializer != null) {
            return fieldInitializer.toString();
        } else {
            return "";
        }

    }

    private static class FieldScanner extends TreePathScanner<ExpressionTree, Trees> {

        private final String fieldName;

        private FieldScanner(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public ExpressionTree reduce(ExpressionTree et1, ExpressionTree et2) {
            if (et1 != null) {
                return et1;
            } else if (et2 != null) {
                return et2;
            } else {
                return null;
            }
        }

        @Override
        public ExpressionTree visitVariable(VariableTree variableTree, Trees trees) {
            if (variableTree.getName().toString().equals(this.fieldName)) {
                return variableTree.getInitializer();
            } else {
                return null;
            }
        }
    }
}