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
package com.tmtron.dtogen.processor.test;

import org.immutables.value.Value;

// the @DtoConfig annotation must be skipped
public abstract class TemplateCopyAbstractMethod {

    // the template member method must be copied from the template
    @Value.Auxiliary
    public abstract String abstractTemplateMemberMethod();

    // the id method must be copied from the source
    public abstract String id();

}
