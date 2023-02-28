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
package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Resolves location of test directories and files. Assumes current working directory
 * to be either examples project location or root project location.
 * ResourceHelper
 */
public class ResourceHelper {

	private static final String MAIN_RESOURCES = "src/main/resources/";
	private static final String EXAMPLES = "examples/";
	private static final String APPS = "applications/";
	private static final String WORKSPACE = "workspace/";
	private static final String LIBRARIES = "lib";
	
    public static File getResourcePath() {
    	String projectPath;
		try {
			projectPath = new File("../" + EXAMPLES).getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
   	    File resourcePath = new File(projectPath, MAIN_RESOURCES);
    	//System.out.println(resourcePath.getAbsolutePath());
  	    //if (!resourcePath.exists())
   	    //	resourcePath = new File(EXAMPLES + MAIN_RESOURCES);
  	    if (!resourcePath.exists())
  	    	throw new RuntimeException("Project path " + MAIN_RESOURCES + "not found");
  	    return resourcePath;
    }

    public static File getResourceFile(String name) {
    	return new File(getResourcePath(), name);
    }

    public static File getTestResourcePath() {
    	String mainPath = getResourcePath().getAbsolutePath();
   	    File testPath = new File(mainPath.replace("main", "test"));
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
       	    File testPath = new File((resourcePath + File.separator + name.substring(0, index + 1)));
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

    public static File getExamplesPath() {
    	File resourcePath;
    	String projectPath;
		try {
			projectPath = new File("../" + EXAMPLES).getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    	//System.out.println(projectPath);
	    resourcePath = new File(projectPath);
	    if (resourcePath.exists()) {
    	    if (!resourcePath.getName().equals(EXAMPLES.substring(0,EXAMPLES.length() - 1 ))) {
       	    	resourcePath = new File(resourcePath, EXAMPLES);
    	    }
	    }
  	    if (!resourcePath.exists())
  	    	throw new RuntimeException("Project path " + EXAMPLES + "not found");
  	    return resourcePath;
    }

   public static File getWorkspacePath() {
   	    File resourcePath = new File(WORKSPACE);
   	    if (!resourcePath.exists())
   	    	resourcePath = new File(APPS + WORKSPACE);
  	    if (!resourcePath.exists())
  	    	throw new RuntimeException("Project path " + WORKSPACE + "not found");
  	    return resourcePath;
    }

    public static File getLibrariesePath() {
    	String projectPath;
		try {
			projectPath = new File("../" + EXAMPLES).getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    	File resourcePath = new File(projectPath, LIBRARIES);
   	    if (!resourcePath.exists())
   	    	resourcePath = new File(EXAMPLES + LIBRARIES);
  	    if (!resourcePath.exists())
  	    	throw new RuntimeException("Project path " + LIBRARIES + "not found");
  	    return resourcePath;
    }

    public static File getWorkspaceFile(String name) {
    	String resourcePath = getWorkspacePath().getAbsolutePath();
    	int index = name.lastIndexOf(File.separator);
    	if (index > 0) {
       	    File testPath = new File((resourcePath + File.separator + name.substring(0, index + 1)));
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
