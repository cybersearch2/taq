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
package au.com.cybersearch2.taq.db;

import java.util.List;

/**
 * BatchCollector
 * Interface for post-processing of collected entity objects.
 * The BatchCollector implementation takes a list of entity objects
 * and returns a list of entity objects of the Data type, which
 * can also be the Entity type if only one of more fields are to 
 * have values modified.
 * @param <Entity> Entity type
 * @param <Data>   Batch data type
 */
public interface BatchCollector<Entity,Data> {

    List<Data> processBatch(List<Entity> entityList);
}
