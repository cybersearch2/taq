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
package au.com.cybersearch2.taq.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.engine.Parser;
import au.com.cybersearch2.taq.engine.SourceTracker;

/**
 * ResourceCompiler
 * Compiles external scripts accessed via streaming. 
 */
public class ResourceCompiler 
{
	/** Interface to main parser class generated by javacc */
	protected final Parser parser;
    /** Context for parse operation */
	protected final SourceTracker sourceTracker;
    protected final File resourceBase;

    /**
     * Construct ParserResources object
     * @param parser Interface to main parser class generated by javacc
     * @param sourceTracker Source tracker
     * @param resourceBase Resources root location
     */
	public ResourceCompiler(Parser parser, SourceTracker sourceTracker, File resourceBase) 
	{
		this.parser = parser;
		this.sourceTracker = sourceTracker;
		this.resourceBase = resourceBase;
	}

    /**
     * Include script from an external streaming source	
     * @param resourceName Resource name
     * @param parserContext Parser context
     * @throws IOException if there is a file error
     * @throws CompilerException if TAQ compile fails
     */
	public void includeResource(String resourceName, ParserContext parserContext) throws IOException
	{	
	    File resourceFile = name2File(resourceName);
		InputStream instream =  new FileInputStream(resourceFile);
		sourceTracker.pushSourceDocument(resourceFile.toString());
		Parser nestedParser = parser.parserInstance(instream);
		Compiler compiler = new Compiler(nestedParser, parserContext);
		try
		{
			compiler.compile();
		} 
		finally
		{
			close(instream, resourceName);
			sourceTracker.popSourceDocument();
		}
	}

    /**
     * Closes input stream quietly
     * @param instream InputStream
     * @param resourceName Name of resource being closed
     */
    private void close(InputStream instream, String resourceName) 
    {
        if (instream != null)
            try
            {
                instream.close();
            }
            catch (IOException e)
            {
                //log.warn(TAG, "Error closing resource " + resourceName, e);
            }
    }
    
    private File name2File(String resourceName) throws IOException 
    {
        return new File(resourceBase, resourceName);
    }

}
