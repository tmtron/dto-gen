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
// the class visibility must be public because the template has public (User = package)

// the @DtoConfig annotation must be skipped
@Generated(
        value = "com.tmtron.dtogen.processor.DtoConfig",
        date = "1976-12-14T15:16:17.234+02:00",
        comments = "origin=com.tmtron.dtogen.processor.test.UserDto_"
)
public abstract class UserDto {
    // the annotation must be copied
    @Value.Auxiliary
    // the visibility must be protected (same as in the User class) because the template does not override it
    protected abstract String firstName();

    // the visibility must be package (same as in the User class) because the template does not override it
    abstract String lastName();

    // the function nonAbstract() must not be copied
}