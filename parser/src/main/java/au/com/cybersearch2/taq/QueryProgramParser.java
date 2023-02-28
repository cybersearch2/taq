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
package au.com.cybersearch2.taq;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.compiler.Compiler;
import au.com.cybersearch2.taq.compiler.CompilerException;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.ProviderFactory;
import au.com.cybersearch2.taq.interfaces.ResourceProvider;
import au.com.cybersearch2.taq.model.TaqParser;

/**
 * QueryProgramParser
 * Creates a QueryProgram object by compiling an TAQ file
 * @author Andrew Bowley
 * 5Feb.,2017
 */
public class QueryProgramParser
{
	/** Provider factory used when a given resource provider is used as a singleton */
	private static class SingletonProviderFactory implements ProviderFactory {

		private final ResourceProvider resourceProvider; 
		
		public SingletonProviderFactory(ResourceProvider resourceProvider) {
			this.resourceProvider = resourceProvider;
		}
		
		@Override
		public boolean isResourceName(String name) {
			return resourceProvider.getName().equals(name);
		}

		@Override
		public ResourceProvider createResourceProvider(String name) {
			if (isResourceName(name))
			    return resourceProvider;
			return null;
		}
		
	}
	
	/** Container for ResourceProvider factories. Also performs any required persistence work */
    protected ProviderManager providerManager;
    /** Path to resource files */
    protected File resourcePath;
    /** Parser context to aggregates variables required while parsing */
    protected ParserContext context;

    /**
     * Create QueryProgramParser object
     * @param resourcePath Path to resource files
     */
    public QueryProgramParser(File resourcePath)
    {
        this.resourcePath = resourcePath;
    }
 
    /**
     * Create QueryProgramParser object with given provider factories
     * @param resourcePath Path to resource files
     * @param providerFactory One or more resource provider factory objects
     */
    public QueryProgramParser(File resourcePath, ProviderFactory... providerFactory)
    {
        this.resourcePath = resourcePath;
        providerManager = new ProviderManager(resourcePath);
        for (ProviderFactory factory: providerFactory)
            providerManager.putResourceProvider(factory);
    }
 
    /**
     * Create QueryProgramParser object with given singleton providers
     * @param resourcePath Path to resource files
     * @param providerFactory One or more resource provider objects
     */
    public QueryProgramParser(File resourcePath, ResourceProvider... resourceProvider)
    {
        this.resourcePath = resourcePath;
        providerManager = new ProviderManager(resourcePath);
        for (ResourceProvider provider: resourceProvider)
            providerManager.putResourceProvider(new SingletonProviderFactory(provider));
    }
    
    /**
     * Create QueryProgramParser object with given provider manager
     * @param resourcePath Path to resource files
     * @param providerManager Provider manager
     */
    public QueryProgramParser(File resourcePath, ProviderManager providerManager)
    {
        this.resourcePath = resourcePath;
        this.providerManager = providerManager;
    }
    
    /**
     * Create QueryProgramParser object with given provider manager with resourcePath configured
     * @param providerManager Provider manager
     */
    public QueryProgramParser(ProviderManager providerManager)
    {
        this.providerManager = providerManager;
        resourcePath = providerManager.getResourceBase();
    }
    
    /**
     * Returns query program from compiling given file
     * @param programFile File path
     * @return QueryProgram object
     */
    public synchronized QueryProgram loadScript(String programFile)
    {
        File filePath = new File(resourcePath, programFile);
        QueryProgram queryProgram = null;
        InputStream stream = null;
        try
        {
            stream = new FileInputStream(filePath);
            TaqParser queryParser = new TaqParser(stream, "UTF-8");
            queryProgram = new QueryProgram(providerManager);
            queryProgram.setResourceBase(resourcePath);
            context = new ParserContext(queryProgram, filePath.toString());
        	Compiler compiler = new Compiler(queryParser.publish(), context);
        	compiler.compile();
        	queryProgram.setCompiler(compiler);
            return queryProgram;
        }
        catch (IOException e)
        {
            throw new ExpressionException(filePath.toString(), e);
        }
        catch (CompilerException e)
        {
            throw new ExpressionException(filePath.toString(), e);
        }
        finally
        {
            if (stream != null)
                try
                {
                    stream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
        }
    }

    /**
     * Returns query program from compiling given file. 
     * Use in testing when not intending to execute a query.
     * @param programFile File path
     * @return QueryProgram object
     */
    public synchronized QueryProgram loadProgram(String programFile) {
    	QueryProgram queryProgram = loadScript(programFile);
    	queryProgram.runPreLaunchTasks();
    	return queryProgram;
    }
    
    /**
     * @return the context
     */
    public ParserContext getContext()
    {
        return context;
    }
}
