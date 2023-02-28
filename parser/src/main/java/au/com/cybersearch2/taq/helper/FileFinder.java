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
package au.com.cybersearch2.taq.helper;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class FileFinder implements FileVisitor<Path> {

	private static final FileVisitResult CONTINUE = FileVisitResult.CONTINUE;
	private static final FileVisitResult TERMINATE = FileVisitResult.TERMINATE;

	private static boolean quietMode = false;

    private final Path startDir;
    private final String filename;
    
    private Path file;
    
	public FileFinder(Path startDir, String filename) {
		this.startDir = startDir;
		this.filename = filename;
	}
	
	public Path getFile() {
		return file == null ? Paths.get("") : file;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
	    return CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		Path name = file.getFileName();
	    if ((name != null) && filename.equals(name.toString())) {
	    	this.file = file;
	    	if (!quietMode)
	            System.out.println("File found: " + file.toString());
	        return TERMINATE;
        }
	    return CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return TERMINATE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
	    boolean finishedSearch = Files.isSameFile(dir, startDir);
	    if (finishedSearch) {
	        System.out.println("File not found: " + filename);
	        return TERMINATE;
	    }
	    return CONTINUE;
	}

	public static void setQuietMode() {
		quietMode = true;
	}
	
}
