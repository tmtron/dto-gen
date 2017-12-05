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

import static com.google.common.truth.Truth.assertThat;

public class StringUtilsTest {

    @Test
    public void removeCurlyBraces() {
        assertThat("x=3;").isEqualTo(StringUtils.removeCurlyBraces("{x=3;}"));
    }

    @Test
    public void removeCurlyBracesWithLineBreaks() {
        assertThat("\r\nx=3;\r\n").isEqualTo(StringUtils.removeCurlyBraces("{\r\nx=3;\r\n}"));
    }

    @Test
    public void removeCurlyBracesReturnsInput() {
        assertThat("y=3;").isEqualTo(StringUtils.removeCurlyBraces("y=3;"));
    }

    @Test
    public void removeCurlyBracesForNull() {
        assertThat("").isEqualTo(StringUtils.removeCurlyBraces(null));
    }

    @Test
    public void removeLastCharsForNull() {
        assertThat("").isEqualTo(StringUtils.removeLastChars(null, 7));
    }

    @Test
    public void removeLastCharsForZero() {
        assertThat("abc").isEqualTo(StringUtils.removeLastChars("abc", 0));
    }

    @Test
    public void removeLastCharsForNegative() {
        assertThat("abc").isEqualTo(StringUtils.removeLastChars("abc", -3));
    }

    @Test
    public void removeLastCharsForMoreThanInputLen() {
        assertThat("").isEqualTo(StringUtils.removeLastChars("abc", 8));
    }

    @Test
    public void removeLastChars() {
        assertThat("a").isEqualTo(StringUtils.removeLastChars("abc", 2));
    }

    @Test
    public void removeLastCharsForAll() {
        assertThat("").isEqualTo(StringUtils.removeLastChars("abc", 3));
    }

}