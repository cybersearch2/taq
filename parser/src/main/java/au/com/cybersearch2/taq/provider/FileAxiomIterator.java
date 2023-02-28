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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import com.thoughtworks.xstream.XStream;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * FileAxiomIterator
 * @author Andrew Bowley
 * 8Jan.,2017
 */
public class FileAxiomIterator implements Iterator<Axiom>
{
    int count;
    File axiomFile;
    Axiom current;
    FileInputStream fileInputStream;
    ObjectInputStream ois;

    public FileAxiomIterator(File axiomFile)
    {
        this.axiomFile = axiomFile;
    }
    
    public Runnable getOnCloseHandler()
    {
        return new Runnable(){

            @Override
            public void run()
            {
                close(ois);
            }};
    }

    public boolean isOpen() {
    	return ois != null;
    }
    
	public boolean isEmpty() {
		return count > 0;
	}

	public void setFilename(String filename) {
		axiomFile = new File(axiomFile.getParentFile(), filename);
	}
	
	
    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext()
    {
    	if (!isOpen())
    		open();
        return current != null;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public Axiom next()
    {
    	if (!hasNext())
            throw new NoSuchElementException("Axiom from " + axiomFile.toString());
        Axiom nextAxiom = current;
        if (count > 0)
            doIterate();
        else
            current = null;
        return nextAxiom;
    }

   private void doIterate()
    {
        try
        {
            current = readNextAxiom();
            --count;
        }
        catch (IOException e)
        {
            throw new ExpressionException(axiomFile.toString() + " error", e);
        }
        catch (ClassNotFoundException e)
        {
            throw new ExpressionException(axiomFile.toString() + " file wrong type or corrupt", e);
        }
        finally
        {
            if (((count <= 0) || (current == null)) && (fileInputStream != null))
            {
                close(fileInputStream);
                fileInputStream = null;
            }
        }
    }
    
    private Axiom readNextAxiom() throws IOException, ClassNotFoundException
    {
        Object marshalled = ois.readObject();
        if (marshalled == null)
            throw new ExpressionException(axiomFile.toString() + " no data");
        return (Axiom)marshalled;
    }

    private void open() {
        FileInputStream reader = null;
        try
        {
            XStream xStream = new XStream();
            XStream.setupDefaultSecurity(xStream); // to be removed after 1.5
            Class<?>[] classes = new Class[] { AxiomHeader.class };
            xStream.allowTypes(classes);        
            xStream.alias("axiomHeader", AxiomHeader.class);
            File headerFile = new File(axiomFile.getAbsolutePath() + ".xml");
            if (headerFile.exists())
            {
                reader = new FileInputStream(headerFile);
                AxiomHeader axiomHeader = (AxiomHeader) xStream.fromXML(reader);
                count = axiomHeader.getCount();
                if (count > 0)
                {
                    fileInputStream = new FileInputStream(axiomFile);
                    ois = new ObjectInputStream(fileInputStream);
                    doIterate();
                }
            }
        }
        catch (FileNotFoundException e)
        {
            throw new ExpressionException(axiomFile.toString() + " not found", e);
        }
        catch (IOException e)
        {
            throw new ExpressionException(axiomFile.toString() + " error", e);
        }
        finally
        {
            if ((count <= 0) && (fileInputStream != null))
                close(fileInputStream);
            if (reader != null)
                close(reader);
        }
    }
    
    /**
     * Closes input stream quietly
     * @param instream InputStream
     */
    private void close(InputStream instream) 
    {
        if (instream != null)
            try
            {
                instream.close();
            }
            catch (IOException e)
            {
            }
    }

    
}
