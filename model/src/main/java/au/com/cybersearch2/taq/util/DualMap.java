/** Copyright 2023 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.taq.util;

/**
 * Bi-directional map which computes content in the principal direction
 *
 * @param <T1> Principal key type
 * @param <T2> Secondary key type
 */
public interface DualMap<T1,T2> {

    /**
     * If the specified key is not already associated with a value (or is mapped
     * to {@code null}), computed its value using built-in mapping
     * function and enters it into this map.
     * @param key key with which the specified value is to be associated
     * @return the current (existing or computed) value associated with
     *         the specified key
     */
     T2 computeIfAbsent(T1 key);

    /**
     * Returns the value to which the specified key is mapped.
     * @param key the key of type T2 whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this map contains no mapping for the key
     */
    T1 get(T2 key);
}
