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
package au.com.cybersearch2.taq.pattern;

/**
 * Regular expression flags
 *
 */
public enum RegexFlag {
    unix_lines(0x01),
    case_insensitive(0x02),
    comments(0x04),
    multiline(0x08),
    literal(0x10),
    dotall(0x20),
    unicode_case(0x40),
    canon_eq(0x80),
    unicode_character_class(0x100);
    
    public final int flag;
	
	private RegexFlag(int flag) {
		this.flag = flag;
	}
}
