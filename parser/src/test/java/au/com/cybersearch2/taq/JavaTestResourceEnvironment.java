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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * JavaTestResourceEnvironment
 * @author Andrew Bowley
 * 05/08/2014
 */
public class JavaTestResourceEnvironment
{
    public static final String DEFAULT_RESOURCE_LOCATION = "resources";
    
    Locale locale = Locale.getDefault();
    final String resourceLocation;

    public JavaTestResourceEnvironment()
    {
        resourceLocation = DEFAULT_RESOURCE_LOCATION;
    }
    
    public JavaTestResourceEnvironment(String resourceLocation)
    {
        this.resourceLocation = resourceLocation;
    }
    
    public InputStream openResource(String resourceName) throws IOException 
    {
        File resourceFile = new File(resourceLocation, resourceName);
        if (!resourceFile.exists())
            throw new FileNotFoundException(resourceName);
        InputStream instream = new FileInputStream(resourceFile);
        return instream;
    }

    public Locale getLocale() 
    {
        return locale;
    }

	public File getDatabaseDirectory() {
		return null;
	}

}
