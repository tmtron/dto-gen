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

import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public class DtoConfigElementProcessor {
    private final ProcessingEnvironment processingEnv;
    private final TypeElement elementAnnotatedWithDtoConfig;

    private TypeSpec.Builder typeSpecBuilder;
    private final Set<String> membersToIgnore = new HashSet<>();
    private final CodeScanner codeScanner;

    public DtoConfigElementProcessor(ProcessingEnvironment processingEnv, TypeElement elementAnnotatedWithDtoConfig) {
        this.processingEnv = processingEnv;
        this.elementAnnotatedWithDtoConfig = elementAnnotatedWithDtoConfig;
        codeScanner = new CodeScanner(processingEnv);
    }

    private String getPackageName() {
        return processingEnv.getElementUtils().getPackageOf(elementAnnotatedWithDtoConfig).getQualifiedName()
                .toString();
    }

    public void work() {
        // e.g."com.tmtron.dtogen.processor.test.User.class"
        final String msg = "processing DtoConfig for: " + elementAnnotatedWithDtoConfig.getQualifiedName();
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg, elementAnnotatedWithDtoConfig);

        try {
            JavaFile.builder(getPackageName(), buildTypeSpec())
                    .skipJavaLangImports(true)
                    .build()
                    .writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private TypeSpec buildTypeSpec() {
        initMembersToIgnore(membersToIgnore);
        typeSpecBuilder = getTypeSpecBuilder();

        copyClassModifiers();
        copyClassAnnotations();
        // TODO: maybe copy javadoc
        // TODO: do not copy superclass/interfaces - they are only used for the template

        List<? extends TypeMirror> interfaces = elementAnnotatedWithDtoConfig.getInterfaces();
        for (TypeMirror interfaceTm : interfaces) {
            TypeElement interfaceTypeElement = MoreTypes.asTypeElement(interfaceTm);
            processSourceElement(interfaceTypeElement);
        }
        TypeMirror superclass = elementAnnotatedWithDtoConfig.getSuperclass();
        TypeElement superClassTypeElement = MoreTypes.asTypeElement(superclass);
        if (!superClassTypeElement.getSimpleName().contentEquals(Object.class.getSimpleName())) {
            processSourceElement(superClassTypeElement);
        }

        return typeSpecBuilder.build();
    }

    private void initMembersToIgnore(Set<String> membersToIgnore) {
        membersToIgnore.clear();
        for (Element element : elementAnnotatedWithDtoConfig.getEnclosedElements()) {
            if (element.getAnnotation(DtoIgnore.class) != null) {
                switch (element.getKind()) {
                    case METHOD:
                        membersToIgnore.add(element.getSimpleName().toString());
                        break;
                    case FIELD:
                        VariableElement variableElement = (VariableElement) element;
                        String fieldInitializer = codeScanner.getFieldInitializerOrBlank(elementAnnotatedWithDtoConfig
                                , variableElement.getSimpleName().toString());
                        if (fieldInitializer.endsWith("()")) {
                            String memberName = StringUtils.removeLastChars(fieldInitializer, 2);
                            membersToIgnore.add(memberName);
                        }
                        break;
                }
            }
        }
    }

    private Element getElementFromTemplateOrNull(Name elementName) {
        for (Element element : elementAnnotatedWithDtoConfig.getEnclosedElements()) {
            if (element.getSimpleName().contentEquals(elementName)) {
                return element;
            }
        }
        return null;
    }

    /**
     * @param sourceTypeElement a source class or interface from which we may copy methods
     */
    private void processSourceElement(TypeElement sourceTypeElement) {
        for (Element element : sourceTypeElement.getEnclosedElements()) {
            if (membersToIgnore.contains(element.getSimpleName().toString())) {
                continue;
            }

            switch (element.getKind()) {
                case METHOD:
                    ExecutableElement methodExecutableElement = (ExecutableElement) element;
                    if (element.getModifiers().contains(Modifier.ABSTRACT)) {
                        Element templateElementOrNull = getElementFromTemplateOrNull(element.getSimpleName());
                        if (templateElementOrNull != null) {
                            // the template has an element with this name - use it
                        } else {
                            // the template does not have an element with this name - copy it from the implemented
                            // interface
                            MethodSpec.Builder copyMethodBuilder = JavaPoetUtil.copyMethod(methodExecutableElement);
                            typeSpecBuilder.addMethod(copyMethodBuilder.build());
                        }
                    }
                    break;
                default:
                    // IGNORE
            }
        }
    }

    private void copyClassAnnotations() {
        // copy all annotations, ..
        List<AnnotationSpec> annotationSpecs = JavaPoetUtil.getAnnotationSpecs(elementAnnotatedWithDtoConfig);
        typeSpecBuilder.addAnnotations(annotationSpecs);
    }

    private void copyClassModifiers() {
        Modifier[] modifiers = elementAnnotatedWithDtoConfig.getModifiers().toArray(new Modifier[0]);
        typeSpecBuilder.addModifiers(modifiers);
    }

    private TypeSpec.Builder getTypeSpecBuilder() {
        String targetClassName = getTargetClassName();

        ClassName targetName = ClassName.bestGuess(targetClassName);
        TypeSpec.Builder result;
        switch (elementAnnotatedWithDtoConfig.getKind()) {
            case CLASS:
                result = TypeSpec.classBuilder(targetName);
                break;
            case INTERFACE:
                result = TypeSpec.interfaceBuilder(targetName);
                break;
            default:
                throw new RuntimeException("Unsupported type " + elementAnnotatedWithDtoConfig.getKind().name());
        }
        return result;
    }

    private String getTargetClassName() {
        String targetClassName = elementAnnotatedWithDtoConfig.getQualifiedName().toString();
        if (targetClassName.endsWith("_")) {
            targetClassName = StringUtils.removeLastChars(targetClassName, 1);
        }
        return targetClassName;
    }
}
