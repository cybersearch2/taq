/** Copyright 2022 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.taq.language;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextDecoder {

    // The encoded character of each character escape.
    // This array functions as the keys of a sorted map, from encoded characters to decoded characters.
    static final char[] ENCODED_ESCAPES = { '\"', '\'', '\\',  'b',  'f',  'n',  'r',  't' };

    // The decoded character of each character escape.
    // This array functions as the values of a sorted map, from encoded characters to decoded characters.
    static final char[] DECODED_ESCAPES = { '\"', '\'', '\\', '\b', '\f', '\n', '\r', '\t' };

    // A pattern that matches an escape.
    // What follows the escape indicator is captured by group 1=character 2=octal 3=Unicode.
    static final Pattern PATTERN = Pattern.compile("\\\\(?:(b|t|n|f|r|\\\"|\\\'|\\\\)|((?:[0-3]?[0-7])?[0-7])|u+(\\p{XDigit}{4}))");

    public static CharSequence decodeString(CharSequence encodedString) {
        Matcher matcher = PATTERN.matcher(encodedString);
        StringBuffer decodedString = new StringBuffer();
        // Find each escape of the encoded string in succession.
        while (matcher.find()) {
            char ch;
            if (matcher.start(1) >= 0) {
                // Decode a character escape.
                ch = DECODED_ESCAPES[Arrays.binarySearch(ENCODED_ESCAPES, matcher.group(1).charAt(0))];
            } else if (matcher.start(2) >= 0) {
                // Decode an octal escape.
                ch = (char)(Integer.parseInt(matcher.group(2), 8));
            } else /* if (matcher.start(3) >= 0) */ {
                // Decode a Unicode escape.
                ch = (char)(Integer.parseInt(matcher.group(3), 16));
            }
            // Replace the escape with the decoded character.
            matcher.appendReplacement(decodedString, Matcher.quoteReplacement(String.valueOf(ch)));
        }
        // Append the remainder of the encoded string to the decoded string.
        // The remainder is the longest suffix of the encoded string such that the suffix contains no escapes.
        matcher.appendTail(decodedString);
        return decodedString;
    }

}
