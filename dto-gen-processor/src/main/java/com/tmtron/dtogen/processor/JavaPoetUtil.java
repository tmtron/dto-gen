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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

public class JavaPoetUtil {

    public static List<ParameterSpec> getParametersOf(ExecutableElement method) {
        List<ParameterSpec> result = new ArrayList<>();
        for (VariableElement parameter : method.getParameters()) {
            result.add(ParameterSpec.get(parameter));
        }
        return result;
    }

    /**
     * Returns a method builder that is initialized with a copy of the method parameter
     *
     * @see MethodSpec#overriding(ExecutableElement)
     */
    public static MethodSpec.Builder copyMethod(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        Set<Modifier> modifiers = method.getModifiers();

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName);
        methodBuilder.addModifiers(modifiers);

        for (TypeParameterElement typeParameterElement : method.getTypeParameters()) {
            TypeVariable var = (TypeVariable) typeParameterElement.asType();
            methodBuilder.addTypeVariable(TypeVariableName.get(var));
        }

        methodBuilder.returns(TypeName.get(method.getReturnType()));
        methodBuilder.addParameters(getParametersOf(method));
        methodBuilder.varargs(method.isVarArgs());

        for (TypeMirror thrownType : method.getThrownTypes()) {
            methodBuilder.addException(TypeName.get(thrownType));
        }

        // copy all annotations, ..
        methodBuilder.addAnnotations(getAnnotationSpecs(method));

        return methodBuilder;
    }

    public static List<AnnotationSpec> getAnnotationSpecs(AnnotatedConstruct executableElement) {
        List<AnnotationSpec> result = new ArrayList<>();
        List<? extends AnnotationMirror> annotationMirrors = executableElement.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            Name simpleName = annotationMirror.getAnnotationType().asElement().getSimpleName();
            // .. except for the DtoConfig annotation
            if (!simpleName.toString().equals(DtoConfig.class.getSimpleName())) {
                AnnotationSpec annotationSpec = AnnotationSpec.get(annotationMirror);
                result.add(annotationSpec);
            }
        }
        return result;
    }

    public static Modifier[] modifiersAsArray(Set<Modifier> modifiers) {
        return modifiers.toArray(new Modifier[0]);
    }

    public static FieldSpec.Builder copyField(VariableElement variableElement) {
        String fieldName = variableElement.getSimpleName().toString();
        Modifier[] modifiers = modifiersAsArray(variableElement.getModifiers());

        TypeName typeName = TypeName.get(variableElement.asType());
        FieldSpec.Builder builder = FieldSpec.builder(typeName, fieldName, modifiers);
        builder.addAnnotations(getAnnotationSpecs(variableElement));

        return builder;
    }

}
