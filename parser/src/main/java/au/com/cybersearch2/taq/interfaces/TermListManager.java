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
package au.com.cybersearch2.taq.interfaces;

import java.util.List;

import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.terms.TermMetaData;

/**
 * TermListManager
 * Interface for TermList instance factory
 * @author Andrew Bowley
 * 3May,2017
 */
public interface TermListManager
{
    QualifiedName getQualifiedName();
    String getName();
    int getTermCount();
    int addTerm(TermMetaData termMetaData);
    void checkTerm(TermMetaData termMetaData);
    int getIndexForName(String termName);
    int getIndexForName(String termName, boolean caseInsensitiveNameMatch);
    TermMetaData getMetaData(int index);
    boolean changeName(int index, String name);
    boolean isMutable();
    boolean isAnonymousTerms();
    int getNamedTermCount();
    List<String> getTermNameList();
    void clearMutable();
    void setDuplicateTermNames(boolean isDuplicateTermNames);
    TermMetaData analyseTerm(Term term, int index);
}
