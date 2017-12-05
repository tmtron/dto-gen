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
    private final Set<String> doNotCopyFromSources = new HashSet<>();
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
        initMembersToIgnore(doNotCopyFromSources);
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

    /**
     * This function will fill the doNotCopyFromSources set with all elements that must not be copied from any source
     * to the target. This includes:
     * <ul>
     * <li>all members that have a {@link DtoIgnore} annotation</li>
     * <li>all non abstract members (those must be copied from the template verbatim)</li>
     * </ul>
     *
     * @param doNotCopyFromSources will be filled
     */
    private void initMembersToIgnore(Set<String> doNotCopyFromSources) {
        doNotCopyFromSources.clear();
        for (Element element : elementAnnotatedWithDtoConfig.getEnclosedElements()) {
            if (element.getAnnotation(DtoIgnore.class) != null) {
                switch (element.getKind()) {
                    case METHOD:
                        doNotCopyFromSources.add(element.getSimpleName().toString());
                        break;
                    case FIELD:
                        VariableElement variableElement = (VariableElement) element;
                        String fieldInitializer = codeScanner.getFieldInitializerOrBlank
                                (elementAnnotatedWithDtoConfig
                                        , variableElement.getSimpleName().toString());
                        if (fieldInitializer.endsWith("()")) {
                            String memberName = StringUtils.removeLastChars(fieldInitializer, 2);
                            doNotCopyFromSources.add(memberName);
                        }
                        break;
                }
            }
            if (!element.getModifiers().contains(Modifier.ABSTRACT)) {
                // non-abstract members are always ignored because they are copied directly from the template
                doNotCopyFromSources.add(element.getSimpleName().toString());
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
     * Will loop over all elements in the sourceTypeElement (which is the superclass of the template, or
     * an implemented interface) and may copy it to the target class
     *
     * @param sourceTypeElement a source class or interface from which we may copy methods
     */
    private void processSourceElement(TypeElement sourceTypeElement) {
        for (Element sourceElement : sourceTypeElement.getEnclosedElements()) {
            if (doNotCopyFromSources.contains(sourceElement.getSimpleName().toString())) {
                continue;
            }

            switch (sourceElement.getKind()) {
                case METHOD:
                    ExecutableElement sourceMethodExecElement = (ExecutableElement) sourceElement;
                    Element templateElementOrNull = getElementFromTemplateOrNull(sourceElement.getSimpleName());
                    if (templateElementOrNull != null) {
                        // TODO: the template has a sourceElement with this name - use it
                    } else {
                        // the template does not have a sourceElement with this name - copy it from the source
                        if (sourceMethodExecElement.getModifiers().contains(Modifier.ABSTRACT)) {
                            MethodSpec.Builder copyMethodBuilder = JavaPoetUtil.copyMethod(sourceMethodExecElement);
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
