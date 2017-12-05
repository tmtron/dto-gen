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

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void removeCurlyBraces() {
        Assert.assertEquals("x=3;", StringUtils.removeCurlyBraces("{x=3;}"));
    }

    @Test
    public void removeCurlyBracesWithLineBreaks() {
        Assert.assertEquals("\r\nx=3;\r\n", StringUtils.removeCurlyBraces("{\r\nx=3;\r\n}"));
    }

    @Test
    public void removeCurlyBracesReturnsInput() {
        Assert.assertEquals("y=3;", StringUtils.removeCurlyBraces("y=3;"));
    }

    @Test
    public void removeCurlyBracesForNull() {
        Assert.assertEquals("", StringUtils.removeCurlyBraces(null));
    }

    @Test
    public void removeLastCharsForNull() {
        Assert.assertEquals("", StringUtils.removeLastChars(null, 7));
    }

    @Test
    public void removeLastCharsForZero() {
        Assert.assertEquals("abc", StringUtils.removeLastChars("abc", 0));
    }

    @Test
    public void removeLastCharsForNegative() {
        Assert.assertEquals("abc", StringUtils.removeLastChars("abc", -3));
    }

    @Test
    public void removeLastCharsForMoreThanInputLen() {
        Assert.assertEquals("", StringUtils.removeLastChars("abc", 8));
    }

    @Test
    public void removeLastChars() {
        Assert.assertEquals("a", StringUtils.removeLastChars("abc", 2));
    }

    @Test
    public void removeLastCharsForAll() {
        Assert.assertEquals("", StringUtils.removeLastChars("abc", 3));
    }

}