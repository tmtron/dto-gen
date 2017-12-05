/*
 * Copyright © 2017 Martin Trummer (martin.trummer@tmtron.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tmtron.dtogen.processor;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
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

    public String getMethodBodyOrBlank(TypeElement parentOfMethod, String methodName) {
        MethodScanner scanner = new MethodScanner(methodName);
        TreePath path = treesInstance.getPath(parentOfMethod);
        if (path != null) {
            MethodTree methodTree = scanner.scan(path, treesInstance);
            if (methodTree != null) {
                return methodTree.getBody().toString();
            }
        }
        return "";
    }

    /**
     * A {@link TreePathScanner} which returns a single result or null when you call scan()
     *
     * @param <R> the result type
     */
    private static class ScannerWithSingleResult<R> extends TreePathScanner<R, Trees> {

        @Override
        public final R reduce(R item1, R item2) {
            if (item1 != null) {
                return item1;
            } else if (item2 != null) {
                return item2;
            } else {
                return null;
            }
        }

    }

    /**
     * returns the initializer of a field or {@code null}
     */
    private static class FieldScanner extends ScannerWithSingleResult<ExpressionTree> {

        private final String fieldName;

        private FieldScanner(String fieldName) {
            this.fieldName = fieldName;
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

    private static class MethodScanner extends ScannerWithSingleResult<MethodTree> {

        private final String nameToFind;

        private MethodScanner(String nameToFind) {
            this.nameToFind = nameToFind;
        }

        @Override
        public MethodTree visitMethod(MethodTree methodTree, Trees trees) {
            if (methodTree.getName().contentEquals(nameToFind)) {
                return methodTree;
            } else {
                return null;
            }
        }
    }
}