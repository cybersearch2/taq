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
package au.com.cybersearch2.taq.provider;

import java.io.File;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;

/**
 * FileResourceProvider
 * @author Andrew Bowley
 * 6Jan.,2017
 */
public class FileResourceProvider extends ResourceMonitor
{
    public static final String FILENAME = "filename";
    public static final String XSTREAM = "xstream";
    public static final String WORKSPACE = "workspace";
    
    private final String resourceName;
    private File workspace;
    
    private String filename;
    private FileAxiomListener fileAxiomListener;
    private FileAxiomIterator fileAxiomIterator;
    private Runnable sourceCloseHandler;
    private Runnable consumerCloseHandler;
    private LocaleAxiomListener externalListener;

    public FileResourceProvider(String resourceName)
    {
        this.resourceName = resourceName;
        filename = XSTREAM;
   }
    /**
     * Chain given axiom listener ahead of existing listeners
     * @param axiomListener Axiom listener
     * @return flag set true if this feature is supported
     */
    @Override
    public boolean chainAxiomListener(LocaleAxiomListener axiomListener) {
        externalListener = axiomListener;
        return true;
    }
    
    @Override
    public String getName()
    {
        return resourceName;
    }

    @Override
    public void open() throws ExpressionException
    {
    	Map<String, Object> properties = getConnectionProperties();
    	if ((!properties.isEmpty()) && properties.containsKey(FILENAME)) {
    		Object object = properties.get(FILENAME);
    		if (object == null)
    			throw new ExpressionException(String.format("File name property '%s' is null", FILENAME));
            filename = object.toString();
    		if (filename.isEmpty())
    			throw new ExpressionException(String.format("File name property '%s' is empty", FILENAME));
    		if (fileAxiomListener != null)
    		    fileAxiomListener.setFilename(filename);
    		if (fileAxiomIterator != null)
    		    fileAxiomIterator.setFilename(filename);
    	}
    	closeOpenFiles(); 
    }

    @Override
    public void close()
    {
    	closeOpenFiles(); 
    	externalListener = null;
    }

    @Override
    public Iterator<Axiom> iterator(AxiomArchetype archetype)
    {
    	if (filename.equals(XSTREAM))
    		filename = archetype.getQualifiedName().toString();
        File axiomFile = new File(workspace, filename);
        fileAxiomIterator = new FileAxiomIterator(axiomFile); 
        sourceCloseHandler = fileAxiomIterator.getOnCloseHandler();
        return fileAxiomIterator;
    }

    @Override
    public LocaleAxiomListener getAxiomListener(String name)
    {
    	if (workspace == null)
    		throw new IllegalStateException("Workspace property not set");
    	if (filename.equals(XSTREAM))
    		filename = name;
        File axiomFile = new File(workspace, filename);
        fileAxiomListener =  new FileAxiomListener(filename, axiomFile);
        consumerCloseHandler = fileAxiomListener.getOnCloseHandler();
        if (externalListener != null)
        {
            return new LocaleAxiomListener(){

                @Override
                public boolean onNextAxiom(QualifiedName qname, Axiom axiom, Locale locale)
                {
                	if ((externalListener == null) ||
                		externalListener.onNextAxiom(qname, axiom, locale))
                    fileAxiomListener.onNextAxiom(qname, axiom, locale);
                	return true;
                }};
        }
        return fileAxiomListener;
    }

    @Override
    public boolean isEmpty()
    {
    	if (sourceCloseHandler != null) {
    		return fileAxiomIterator.isEmpty();
    	}
        return true;
    }
 
    @Override
    public boolean setProperty(String key, Object value) {
    	if (WORKSPACE.equals(key) && (value instanceof File)) {
    		workspace = new File((File)value, XSTREAM);
    		if (!workspace.exists())
    			workspace.mkdir();
    		return true;
    	} 
    	return false;
	}
    
    private void closeOpenFiles() {
    	if ((sourceCloseHandler != null) && fileAxiomIterator.isOpen()) {
    		sourceCloseHandler.run();
    		sourceCloseHandler = null;
    	}
    	if ((consumerCloseHandler != null) && fileAxiomListener.isOpen()) {
    		consumerCloseHandler.run();
    		consumerCloseHandler = null;
    	}
}
}
