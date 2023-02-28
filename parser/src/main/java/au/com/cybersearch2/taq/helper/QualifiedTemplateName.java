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
package au.com.cybersearch2.taq.helper;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * QualifiedTemplateName
 * @author Andrew Bowley
 * 11Jan.,2017
 */
public class QualifiedTemplateName extends QualifiedName
{

    private static final long serialVersionUID = 3634005314122046014L;

    public QualifiedTemplateName(String scope, String name)
    {
        super(QueryProgram.GLOBAL_SCOPE.equals(scope) ? EMPTY : scope, name, EMPTY);
    }

    public static String toString(String scope, String template)
    {
        StringBuilder builder = new StringBuilder();
        if (!scope.isEmpty())
            builder.append(scope).append('.');
        if (!template.isEmpty())
        {
            builder.append(template);
        }
        return builder.toString();
    }
    
}
