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

import javax.annotation.Generated;

import org.immutables.value.Value;

@Value.Immutable
// the @DtoConfig annotation must be skipped
@Generated(
        value = "com.tmtron.dtogen.processor.DtoConfig",
        date = "1976-12-14T15:16:17.234+02:00",
        comments = "origin=com.tmtron.dtogen.processor.test.UserIgnoreField_"
)
// the class visibility must be public because the template has public (User = package)
public abstract class UserIgnoreField {

    abstract String lastName();

    // the function nickName() must not be copied - it has the DtoIgnore annotation
}