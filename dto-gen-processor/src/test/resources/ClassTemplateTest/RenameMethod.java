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

// the @DtoConfig annotation must be skipped

import javax.annotation.Generated;

@Generated(
        value = "com.tmtron.dtogen.processor.DtoConfig",
        date = "1976-12-14T15:16:17.234+02:00",
        comments = "origin=com.tmtron.dtogen.processor.test.RenameMethod_"
)
public abstract class RenameMethod {

    // the id field must be copied and must use the type, modifiers, etc. from the source field
    public abstract String renamedId();
}