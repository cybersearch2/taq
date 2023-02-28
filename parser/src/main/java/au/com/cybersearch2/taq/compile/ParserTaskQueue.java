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

import java.util.ArrayList;
import java.util.PriorityQueue;

import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.interfaces.ParserRunner;
import au.com.cybersearch2.taq.list.AxiomList;
import au.com.cybersearch2.taq.list.AxiomTermList;

/**
 * ParserTaskQueue
 * @author Andrew Bowley
 * 13May,2017
 */
public class ParserTaskQueue
{
    /** Tasks delayed until parsing complete */
    protected ArrayList<ParserTask> pendingList;

    public ParserTaskQueue()
    {
        pendingList = new ArrayList<ParserTask>();
    }
    
    /**
     * Add ParserTask to pending list
     * @param scope Scope
     * @return ParserTask
     */
    public ParserTask addPending(Scope scope)
    {
        ParserTask parserTask = new ParserTask(scope.getName(), scope.getParserAssembler().getQualifiedContextname());
        pendingList.add(parserTask);
        return parserTask;
    }

    /**
     * Add Runnable to pending list
     * @param pending Runnable to execute parser task
     * @param scope Scope
     * @return ParserTask
     */
    public ParserTask addPending(ParserRunner pending, Scope scope)
    {
        ParserTask parserTask = addPending(scope);
        parserTask.setPending(pending);
        return parserTask;
    }

    /**
     * Collect pending parser tasks into priority queue
     * @param priorityQueue Priority queue
     */
    public void getPending(PriorityQueue<ParserTask> priorityQueue)
    {
        if (pendingList != null)
        {
            priorityQueue.addAll(pendingList);
            pendingList.clear();
        }
    }

    /**
     * Queue task to bind list to it's source which may not yet be declared
     * @param axiomTermList Axiom term list object
     * @param scope Scope
     */
    public void registerAxiomTermList(final AxiomTermList axiomTermList, Scope scope)
    {
        ParserTask parserTask = addPending(new ParserRunner(){
            @Override
            public void run(ParserAssembler parserAssember)
            {
                parserAssember.bindAxiomList(axiomTermList);
            }}, scope);
        // Boost priority so list is processed before any variables which reference it
        parserTask.setPriority(ParserTask.Priority.list.ordinal());
    }

    /**
     * Queue task to bind list to it's source which may not yet be declared
     * @param axiomList The axiom list
     * @param scope Scope
     */
    public void registerAxiomList(final AxiomList axiomList, Scope scope) 
    {
        ParserTask parserTask = addPending(new ParserRunner(){
            @Override
            public void run(ParserAssembler parserAssember)
            {
                parserAssember.bindAxiomList(axiomList);
            }}, scope);
        // Boost priority so list is processed before any variables which reference it
        parserTask.setPriority(ParserTask.Priority.list.ordinal());
    }
    
}
