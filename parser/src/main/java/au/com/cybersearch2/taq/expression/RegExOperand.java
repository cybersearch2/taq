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
package au.com.cybersearch2.taq.expression;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.Group;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;


/**
 * RegExOperand
 * Evaluates regular expression from current value and optionally populates group operands.
 * The evaluation returns false on no match which causes unification to short circuit. 
 * @author Andrew Bowley
 * 20 Dec 2014
 */
public class RegExOperand extends BooleanOperand 
{
	// In multiline mode the expressions '^' and '$' match
    // just after or just before, respectively, a line terminator or the end of
    // the input sequence. 
	protected static int REGEX_DEFAULT_FLAGS = 0; // Pattern.MULTILINE;
	
	/** Regular expression */
	protected String regex;
	/** Pre-compiled pattern */
	protected Pattern pattern;
	/** Optional object to assign group values on evaluation */
	protected Group group;
	/** Optional flags to modify regular expression behavior */
	protected int flags;
	/** Regular expression operand */
	protected Operand regexOp;
	/** Text to match on */
    protected String input;
	
	/**
	 * Construct RegExOperand with given pattern.
     * @param qname Qualified name
	 * @param patternFactory Supplies PatternOperand instances to use in association with regular expressions
	 * @param inputOp Input operand (optional)
	 * @param group Group object or null if grouping not used
	 */
	public RegExOperand(QualifiedName qname, PatternFactory patternFactory, Operand inputOp, Group group) 
	{
		this(qname, patternFactory.getPattenOperand(), inputOp, group);
		flags = patternFactory.getFlags();
		setPrivate(true);
		//this.regex = regex.replace("\\\\", "\\");
	}

	/**
	 * Construct RegExOperand object using literal or variable pattern. If required, flags
	 * must be embedded in the pattern. 
     * @param qname Qualified name
	 * @param regexOp Regular expression operand
	 * @param inputOp Input operand (optional)
	 * @param group Group object or null if grouping not used
	 */
	public RegExOperand(QualifiedName qname, Operand regexOp, Operand inputOp, Group group) 
	{
		super(qname, inputOp);
		this.group = group;
		this.regexOp = regexOp;
		if ((regexOp != null) && !regexOp.isEmpty())
			regex = regexOp.getValue().toString();
		else
			regex = "";
		flags = 0;
	}

   /**
     * Delegate to perform actual unification with other Term. If successful, two terms will be equivalent. 
     * @param otherTerm Term with which to unify
     * @param id Identity of caller, which must be provided for backup()
     * @return Identity passed in param "id" or zero if unification failed
     * @see #backup(int id)
     */
    @Override
    public int unify(Term otherTerm, int id)
    {
        this.id = id;
        input = otherTerm.getValue().toString();
        return this.id;
    }

	/**
	 * Match input to pattern and set the value of this operand to the boolean match result
	 * @param id Identity of caller, which must be provided for backup()
	 * @return Flag set true if evaluation is to continue
	 */
	@Override
	public EvaluationStatus evaluate(int id) 
	{
	    this.id = id;
        if (leftOperand != null)
        {
            EvaluationStatus status = leftOperand.evaluate(id);
            if (status != EvaluationStatus.COMPLETE)
                return status;
            if (leftOperand.isEmpty())
                input = "";
            else
                input = leftOperand.getValue().toString();
        }
		if (regexOp != null) 
		{
			if (regexOp.isEmpty())
				regexOp.evaluate(id);
			regex = regexOp.getValue().toString().replace("\\\\", "\\");
		}
		// Note id not required as this object id is set during unification
        boolean isMatch = false;
		if (input != null)
		{
		    Matcher matcher = null;
		    if (regex.isEmpty())
		        isMatch = input.isEmpty();
		    else
		    {
		        // Retain value on match
		        matcher = getMatcher();
			    isMatch = matcher.find();
		    }
			if (isMatch && (group != null) && (matcher != null))
			{   // Assign values to group operands which are members of the same template as this term
				List<? super IOperand> groupList = group.getGroupList();
				String[] groupValues = getGroups(matcher);
				if (groupValues.length > 0)
				{
					int index = -1;
					for (String group: groupValues)
					{   
                        ++index;
					    if (group == null)
					        continue;
					    // Group(0) is the input text
						if (index > 0)
						{   // Groups in regex start at group(1)
							if (index > groupList.size())
								break;
							Parameter param = new Parameter(Term.ANONYMOUS, group);
							param.setId(id);
							((IOperand) groupList.get(index - 1)).assign(param);
						}
					}
				}
			}
		}
		setValue(isMatch);
		return EvaluationStatus.COMPLETE;
	}

   /**
     * Backup to intial state if given id matches id assigned on unification or given id = 0. 
     * @param id Identity of caller. 
     * @return boolean true if backup occurred
     * @see au.com.cybersearch2.taq.language.Parameter#unify(Term otherParam, int id)
     */
    @Override
    public boolean backup(int id)
    {
        input = null;
        if (regexOp != null)
            regexOp.backup(id);
        return super.backup(id);
    }

	/**
	 * Override toString() to report &lt;empty&gt;, null or value
	 * @see au.com.cybersearch2.taq.language.Parameter#toString()
	 */
	@Override
	public String toString()
	{
	    if (regex.isEmpty() && (regexOp != null) && !regexOp.isEmpty())
            regex = regexOp.getValue().toString();
		if (empty || regex.isEmpty())
		    return getName() + " \\" + regex + "\\";
		String text = super.toString();
        if (!getMatcher().find())
            text += ": false";
        return text;
	}

	/**
	 * Returns values for group 0 and above as array
	 * @param matcher Matcher object in matched state
	 * @return String array
	 */
	public static String[] getGroups(Matcher matcher)
	{
		String[] groups = new String[matcher.groupCount() + 1];
		for (int i = 0; i < groups.length; i++)
			groups[i] = matcher.group(i);
		return groups;
	}

	/**
	 * Returns expression Operand to an operand visitor
	 * @return Operand object or null if expression not set
	 */
	@Override
	public Operand getRightOperand() 
	{
		return regexOp;
	}

	protected Matcher getMatcher()
	{
        try
        {
            pattern = Pattern.compile(regex, flags | REGEX_DEFAULT_FLAGS);
        }
        catch(PatternSyntaxException e)
        {
            throw new ExpressionException("Error in regular expression", e);
        }
        // Retain value on match
        return pattern.matcher(input);
	}
}
