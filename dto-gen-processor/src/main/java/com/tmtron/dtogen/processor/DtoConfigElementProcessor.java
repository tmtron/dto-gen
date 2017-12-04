package com.tmtron.dtogen.processor;

import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.tools.Diagnostic;

public class DtoConfigElementProcessor {
    private final ProcessingEnvironment processingEnv;
    private final TypeElement classTypeElement;

    private TypeSpec.Builder typeSpecBuilder;

    public DtoConfigElementProcessor(ProcessingEnvironment processingEnv, TypeElement classTypeElement) {
        this.processingEnv = processingEnv;
        this.classTypeElement = classTypeElement;
    }

    private String getPackageName() {
        return processingEnv.getElementUtils().getPackageOf(classTypeElement).getQualifiedName().toString();
    }

    public void work() {
        // e.g."com.tmtron.dtogen.processor.test.User.class"
        final String msg = "processing DtoConfig for: " + classTypeElement.getQualifiedName();
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg, classTypeElement);

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
        typeSpecBuilder = getTypeSpecBuilder();

        copyClassModifiers();
        copyClassAnnotations();
        // TODO: maybe copy javadoc
        // TODO: do not copy superclass/interfaces - they are only used for the template

        List<? extends TypeMirror> interfaces = classTypeElement.getInterfaces();
        for (TypeMirror interfaceTm : interfaces) {
            TypeElement interfaceEt = MoreTypes.asTypeElement(interfaceTm);
            processInterface(interfaceEt);
        }
        return typeSpecBuilder.build();
    }

    private Element getElementFromTemplateOrNull(Name elementName) {
        for (Element element : classTypeElement.getEnclosedElements()) {
            if (element.getSimpleName().contentEquals(elementName)) {
                return element;
            }
        }
        return null;
    }

    static List<ParameterSpec> parametersOf(ExecutableElement method) {
        List<ParameterSpec> result = new ArrayList<>();
        for (VariableElement parameter : method.getParameters()) {
            result.add(ParameterSpec.get(parameter));
        }
        return result;
    }

    /**
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
        methodBuilder.addParameters(parametersOf(method));
        methodBuilder.varargs(method.isVarArgs());

        for (TypeMirror thrownType : method.getThrownTypes()) {
            methodBuilder.addException(TypeName.get(thrownType));
        }

        return methodBuilder;
    }

    private void processInterface(TypeElement interfaceEt) {
        for (Element element : interfaceEt.getEnclosedElements()) {
            switch (element.getKind()) {
                case METHOD:
                    ExecutableElement methodExecutableElement = (ExecutableElement) element;

                    Element tmplElementOrNull = getElementFromTemplateOrNull(element.getSimpleName());
                    if (tmplElementOrNull != null) {
                        // the template has an element with this name - use it
                    } else {
                        // the template does not have an element with this name - copy it from the implemented interface
                        MethodSpec.Builder copyMethodBuilder = copyMethod(methodExecutableElement);
                        typeSpecBuilder.addMethod(copyMethodBuilder.build());
                    }
                    break;
                default:
                    throw new RuntimeException("Unsupported element type: " + element.getKind().name());
            }
        }
    }

    private void copyClassAnnotations() {
        // copy all annotations, ..
        List<? extends AnnotationMirror> annotationMirrors = classTypeElement.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            Name simpleName = annotationMirror.getAnnotationType().asElement().getSimpleName();
            // .. except for the DtoConfig annotation
            if (!simpleName.toString().equals(DtoConfig.class.getSimpleName())) {
                AnnotationSpec annotationSpec = AnnotationSpec.get(annotationMirror);
                typeSpecBuilder.addAnnotation(annotationSpec);
            }
        }
    }

    private void copyClassModifiers() {
        Modifier[] modifiers = classTypeElement.getModifiers().toArray(new Modifier[0]);
        typeSpecBuilder.addModifiers(modifiers);
    }

    private TypeSpec.Builder getTypeSpecBuilder() {
        String targetClassName = getTargetClassName();

        ClassName targetName = ClassName.bestGuess(targetClassName);
        TypeSpec.Builder result;
        switch (classTypeElement.getKind()) {
            case CLASS:
                result = TypeSpec.classBuilder(targetName);
                break;
            case INTERFACE:
                result = TypeSpec.interfaceBuilder(targetName);
                break;
            default:
                throw new RuntimeException("Unsupported type " + classTypeElement.getKind().name());
        }
        return result;
    }

    private String getTargetClassName() {
        String targetClassName = classTypeElement.getQualifiedName().toString();
        if (targetClassName.endsWith("_")) {
            targetClassName = targetClassName.substring(1, targetClassName.length() - 1);
        }
        return targetClassName;
    }
}
