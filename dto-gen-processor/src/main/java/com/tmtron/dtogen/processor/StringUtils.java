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

public class StringUtils {

    public static String removeLastChars(String input, int noOfCharsToRemove) {
        if (noOfCharsToRemove <= 0) return input;

        if (input == null) return "";
        if (noOfCharsToRemove >= input.length()) return "";
        int endIndex = input.length() - noOfCharsToRemove;
        return input.substring(0, endIndex);
    }

    public static String removeCurlyBraces(String input) {
        if (input == null) return "";

        if (input.startsWith("{") && input.endsWith("}")) {
            return input.substring(1, input.length() - 1);
        }
        return input;
    }
}
