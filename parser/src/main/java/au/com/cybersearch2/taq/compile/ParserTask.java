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
package au.com.cybersearch2.taq.compile;

import au.com.cybersearch2.taq.interfaces.ParserRunner;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * ParserTask
 * Executes code to complete compilation. Self ordering by priority.
 * @author Andrew Bowley
 * 20Jan.,2017
 */
public class ParserTask implements ParserRunner, Comparable<ParserTask>
{
    public enum Priority
    {
        list,
        variable,
        fix
    }
    
    /** Pending task */
    protected ParserRunner pending;
    /** Qualified name of enclosing scope/template context */
    protected QualifiedName qualifiedContextname;
    /** Name of scope owning task */
    protected String scopeName;
    /** Priority - lowest is 0 */
    protected int priority;
 
    /**
     * Construct ParserTask object
     * @param scopeName Name of scope owning task
     * @param qualifiedContextname Qualified name of enclosing scope/template context
     */
    public ParserTask(String scopeName, QualifiedName qualifiedContextname)
    {
        this.scopeName = scopeName;
        this.qualifiedContextname = qualifiedContextname;
    }

    /**
     * Returns scope name
     * @return String
     */
    public String getScopeName()
    {
        return scopeName;
    }

    /**
     * Set task to run
     * @param pending Runnable object
     */
    public void setPending(ParserRunner pending)
    {
        this.pending = pending;
    }

    /**
     * Set priority - 0 is lowest and default
     * @param priority Priority
     */
    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    /**
     * Returns qualified name of enclosing scope/template context
     * @return QualifiedName object
     */
    public QualifiedName getQualifiedContextname()
    {
        return qualifiedContextname;
    }
 
    /**
     * Execute task
     */
    @Override
    public void run(ParserAssembler parserAssembler)
    {
        if (pending != null)
            pending.run(parserAssembler);
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(ParserTask other)
    {
        return other.priority - priority;
    }
}
