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
package au.com.cybersearch2.taq.terms;

import java.io.Serializable;

import au.com.cybersearch2.taq.language.Term;

/**
 * TermStore
 * Stores Term object state for serialization
 * @author Andrew Bowley
 * 7Jan.,2017
 */
public class TermStore implements Serializable
{
    private static final long serialVersionUID = -1512862620778240664L;
    
    private int id;
    private String name;
    private Object value;

    public TermStore(Term term)
    {
        id = term.getId();
        name = term.getName();
        value = term.getValue();
    }
    
    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public Object getValue()
    {
        return value;
    }
    public void setValue(Object value)
    {
        this.value = value;
    }
}
