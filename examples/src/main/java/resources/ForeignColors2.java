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
package resources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import au.com.cybersearch2.taq.ProviderManager;
import au.com.cybersearch2.taq.QueryParams;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.interfaces.ProviderFactory;
import au.com.cybersearch2.taq.interfaces.ResourceProvider;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.provider.FileResourceProvider;
import au.com.cybersearch2.taq.query.Solution;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates a resource with a provider system name which is enclosed 
in quotes to allow any naming convention to be used. A system name is used here becuse 
the provider is named "xstream", a reference to the underlying technology which serializes 
objects to XML and back again, and there are two resources, both using the same provider 
- "german_colors" and "french_colors".
 */
public class ForeignColors2 
{
    private static class FileProviderFactory implements ProviderFactory {

    	private LocaleAxiomListener axiomListener;
     	
		public void setAxiomListener(LocaleAxiomListener axiomListener) {
			this.axiomListener = axiomListener;
		}

		@Override
		public boolean isResourceName(String name) {
			return FileResourceProvider.XSTREAM.equals(name);
		}

		@Override
		public ResourceProvider createResourceProvider(String name) {
			FileResourceProvider lastResourceProvider = null;
			if (isResourceName(name)) {
				lastResourceProvider = new FileResourceProvider(name);
				if (axiomListener != null) 
					lastResourceProvider.chainAxiomListener(axiomListener);
			}
			return lastResourceProvider;
		}
    	
    }
    
    private QueryProgramParser queryProgramParser;
    private FileProviderFactory fileProviderFactory;

    public ForeignColors2()
    {
        queryProgramParser = 
            new QueryProgramParser(provideResourceManager());
    }

    ProviderManager provideResourceManager()
    {
        ProviderManager providerManager = new ProviderManager();
        fileProviderFactory = new FileProviderFactory();
		providerManager.setProperty("workspace", ResourceHelper.getWorkspacePath());
		providerManager.setProperty("resource_base", ResourceHelper.getResourcePath());
        providerManager.putResourceProvider(fileProviderFactory);
        return providerManager;
    }

    public List<Axiom> createForeignLexicon()
    {
        final List<Axiom> axiomList = new ArrayList<Axiom>();
        LocaleAxiomListener axiomListener = new LocaleAxiomListener(){

            @Override
            public boolean onNextAxiom(QualifiedName qname, Axiom axiom, Locale locale)
            {
                axiomList.add(axiom);
				return true;
            }};
            fileProviderFactory.setAxiomListener(axiomListener);
        QueryProgram queryProgram = queryProgramParser.loadScript("resources/foreign-lexicon2.taq");
        try
        {
            queryProgram.executeQuery("color_query"); 
            return axiomList;
        }
        finally
        {
        }
    }
    
    /**
	 * Compiles foreign-colors2.taq and runs the "color_query" query, displaying the solution on the console.
	 * @return AxiomTermList iterator containing the final Calculator solution
	 */
    public String getColorSwatch(String language, String name)
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("resources/foreign-colors2.taq");
        // Create QueryParams object for Global scope and query "color_query"
        QueryParams queryParams = queryProgram.getQueryParams(language, "color_query");
        // Add a shade Axiom with a specified color term
        // This axiom goes into the Global scope and is removed at the start of the next query.
        Solution initialSolution = queryParams.getInitialSolution();
        initialSolution.put("shade", new Axiom("shade", new Parameter("name", name)));
        Result result = queryProgram.executeQuery(queryParams);
        return result.getAxiom(language, "color_query").toString();
	}
	
    /**
     * Run tutorial
     * The expected result:<br/><code>
colors(aqua=Wasser, black=schwarz, blue=blau, white=weiß)
colors(aqua=bleu vert, black=noir, blue=bleu, white=blanc)
color(name=bleu vert, R=0, G=255, B=255)
color(name=noir, R=0, G=0, B=0)
color(name=blanc, R=255, G=255, B=255)
color(name=bleu, R=0, G=0, B=255)
color(name=Wasser, R=0, G=255, B=255)
color(name=schwarz, R=0, G=0, B=0)
color(name=weiß, R=255, G=255, B=255)
color(name=blau, R=0, G=0, B=255)
</code>
     * @param args
     */
	public static void main(String[] args)
	{
		try 
		{
	        ForeignColors2 foreignColors2 = new ForeignColors2();
	        Iterator<Axiom> colors = foreignColors2.createForeignLexicon().iterator();
	        if (!colors.hasNext())
	        	throw new RuntimeException("Foreign lexicon is empty");
	        while (colors.hasNext())
	            System.out.println(colors.next());
            System.out.println(foreignColors2.getColorSwatch("french", "bleu vert"));
            System.out.println(foreignColors2.getColorSwatch("french", "noir"));
            System.out.println(foreignColors2.getColorSwatch("french", "blanc"));
            System.out.println(foreignColors2.getColorSwatch("french", "bleu"));
            System.out.println(foreignColors2.getColorSwatch("german", "Wasser"));
            System.out.println(foreignColors2.getColorSwatch("german", "schwarz"));
            System.out.println(foreignColors2.getColorSwatch("german", "weiß"));
            System.out.println(foreignColors2.getColorSwatch("german", "blau"));
  		} 
		catch (Throwable e) 
		{ 
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
