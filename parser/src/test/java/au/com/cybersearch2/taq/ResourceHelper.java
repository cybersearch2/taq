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
import java.io.IOException;
import java.nio.file.Files;

/**
 * Resolves location of test directories and files. Assumes current working directory
 * to be either parser project location or root project location.
 * ResourceHelper
 */
public class ResourceHelper {

	private static final String MAIN_JAVA = "src/main/java/";
	private static final String PARSER = "parser/";

    public static File getJavaPath() {
   	    File resourcePath = new File(MAIN_JAVA);
   	    if (!resourcePath.exists())
   	    	resourcePath = new File(PARSER + MAIN_JAVA);
  	    if (!resourcePath.exists())
  	    	throw new RuntimeException("Project path " + MAIN_JAVA + "not found");
  	    return resourcePath;
    }

    public static File getJavaFile(String name) {
    	return new File(getJavaPath(), name);
    }

    public static File getTestResourcePath() {
    	String mainPath = getJavaPath().getAbsolutePath();
   	    File testPath = new File(mainPath.replace("main/java", "test/resources"));
   	    if (!testPath.exists())
			try {
				Files.createDirectory(testPath.toPath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
   	    return testPath;
    }

    public static File getTestResourceFile(String name) {
    	String resourcePath = getTestResourcePath().getAbsolutePath();
    	int index = name.lastIndexOf(File.separator);
    	if (index > 0) {
       	    File testPath = new File((resourcePath + name.substring(0, index + 1)));
       	    if (!testPath.exists())
				try {
					Files.createDirectory(testPath.toPath());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
    	}
    	File testFile = new File(resourcePath + File.separator, name);
   	    return testFile;
    }

}
