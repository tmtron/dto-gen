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

import org.junit.Test;

public class ClassTemplateTest extends AnnotationProcessorTest {

    @Test
    public void testSimpleCopy() {
        assertGenerationWithoutWarnings("SimpleCopy");
    }

    @Test
    public void testClassTemplateAnnotation() {
        assertGenerationWithoutWarnings("ClassTemplateAnnotation");
    }

    @Test
    public void testMethodAnnotation() {
        assertGenerationWithoutWarnings("MethodAnnotation");
    }

    @Test
    public void testTemplateCopyAbstractMethod() {
        assertGenerationWithoutWarnings("TemplateCopyAbstractMethod");
    }

    @Test
    public void testTemplateCopyMethod() {
        assertGenerationWithoutWarnings("TemplateCopyMethod");
    }

    @Test
    public void testTemplateCopyField() {
        assertGenerationWithoutWarnings("TemplateCopyField");
    }

    @Test
    public void testTemplateCopyFieldWithAnnotation() {
        assertGenerationWithoutWarnings("TemplateCopyFieldWithAnnotation");
    }

    @Test
    public void testTemplateCopyFieldWithInitializer() {
        assertGenerationWithoutWarnings("TemplateCopyFieldWithInitializer");
    }

    @Test
    public void testRenameMethod() {
        assertGenerationWithoutWarnings("RenameMethod");
    }

    @Test
    public void testUserDto() {
        assertGenerationWithoutWarnings("UserDto");
    }

}
