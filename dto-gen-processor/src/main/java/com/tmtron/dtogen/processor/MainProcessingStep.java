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

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Processing step for the {@link DtoConfig} annotation
 */
public class MainProcessingStep implements BasicAnnotationProcessor.ProcessingStep {

    private final ProcessingEnvironment processingEnv;

    public MainProcessingStep(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    @Override
    public Set<? extends Class<? extends Annotation>> annotations() {
        return ImmutableSet.of(DtoConfig.class);
    }

    @Override
    public Set<Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
        try {
            if (elementsByAnnotation.size() != 1) {
                throw new Exception("Exactly one " + DtoConfig.class.getName() + " annotation is required!");
            }

            Set<Element> elementsAnnotatedWithDtoConfig = elementsByAnnotation.get(DtoConfig.class);
            processDtoConfig(elementsAnnotatedWithDtoConfig);
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Annotation processing error: " + e.getClass().getSimpleName() + "-" + e.getMessage());
        }

        return Collections.emptySet();
    }

    private void processDtoConfig(Set<Element> elementsAnnotatedWithDtoConfig) {
        for (Element element : elementsAnnotatedWithDtoConfig) {
            try {
                TypeMirror classTypeMirror = element.asType();
                TypeElement classTypeElement = MoreTypes.asTypeElement(classTypeMirror);
                // e.g. classTypeElement.getQualifiedName() = com.tmtron.dtogen.processor.test.User.class
                new DtoConfigElementProcessor(processingEnv, classTypeElement).work();
            } catch (Exception e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR
                        , "Annotation processing error: " + e.getClass().getSimpleName() + "-" + e.getMessage()
                        , element);
            }
        }
    }
}
