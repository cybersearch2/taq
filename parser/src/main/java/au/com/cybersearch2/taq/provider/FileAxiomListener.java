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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Locale;

import com.thoughtworks.xstream.XStream;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * FileAxiomListener
 * @author Andrew Bowley
 * 8Jan.,2017
 */
public class FileAxiomListener implements LocaleAxiomListener
{
    int count;
    String name;
    File axiomFile;
    FileOutputStream fos;
    ObjectOutputStream oos;
    
    public FileAxiomListener(String name, File axiomFile)
    {
        this.name = name;
        this.axiomFile = axiomFile;
    }
 
    public boolean isOpen() {
    	return oos != null;
    }
    
    /**
     * @see au.com.cybersearch2.taq.interfaces.LocaleAxiomListener#onNextAxiom(au.com.cybersearch2.taq.pattern.Axiom)
     */
    @Override
    public boolean onNextAxiom(QualifiedName qname, Axiom axiom, Locale locale)
    {
        try
        {
            if (!isOpen())
                try
                {
                     openFile();
                }
                catch(IOException e)
                {
                    throw new ExpressionException(axiomFile.toString() + " error opening file", e);
                }
            oos.writeObject(axiom);
            oos.flush();
            ++count;
			return true;
        }
        catch (IOException e)
        {
            throw new ExpressionException(axiomFile.toString() + " error", e);
        }
    }

    public Runnable getOnCloseHandler()
    {
        return new Runnable(){

            @Override
            public void run()
            {
                close();
            }};
    }

	public void setFilename(String filename) {
		axiomFile = new File(axiomFile.getParentFile(), filename);
	}
	
    private void openFile() throws IOException
    {
        fos = new FileOutputStream(axiomFile);
        oos = new ObjectOutputStream(fos);
    }
    
    /**
     * Closes input stream quietly
     * @param instream InputStream
     */
    private void close() 
    {
        if (isOpen())
        {
            try
            {
                oos.close();
                if (count > 0)
                {
                    writeHeader();
                    count = 0;
                }
                oos = null;
            }
            catch (IOException e)
            {
            }
        }
    }

    private void writeHeader()
    {
        AxiomHeader axiomHeader = new AxiomHeader();
        axiomHeader.setName(name);
        axiomHeader.setCreated(new Date());
        axiomHeader.setUser(System.getProperty("user.name"));
        axiomHeader.setCount(count);
        XStream xStream = new XStream();
        xStream.alias("axiomHeader", AxiomHeader.class);
        File headerFile = new File(axiomFile.getAbsolutePath() + ".xml");
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter(headerFile);
            xStream.toXML(axiomHeader, writer);
        }
        catch (FileNotFoundException e)
        {
            throw new ExpressionException(axiomFile.toString() + " error writing axiom header", e);
        }
        finally
        {
            if (writer != null)
                writer.close();
        }
        
    }

}
