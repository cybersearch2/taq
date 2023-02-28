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
package task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProcessRunner {

	// From mkyong.com
    private final static String OS = System.getProperty("os.name").toLowerCase();
	public  final static boolean IS_WINDOWS = (OS.indexOf("win") >= 0);
	public  final static boolean IS_MAC = (OS.indexOf("mac") >= 0);
	public  final static boolean IS_UNIX = (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
	private final static String[] LINUX_CMD = new String[] { "bash", "-c"};
	private final static String[] WINDOWS_CMD = new String[] { "cmd", "/c"};
	private final static String CLASSPATH = "CLASSPATH";

	/** Working directory of process */
	private final File directory;
	private final List<String> command;
	private String classpath;

	public ProcessRunner(File directory) {
		this.directory = directory;
		this.command = new ArrayList<>();
		classpath = "";
	}

	public void setShellCommand(List<String> command) {
		this.command.clear();
		String[] osCommand;
		if (IS_UNIX || (IS_MAC))
			osCommand = LINUX_CMD;
		else if (IS_WINDOWS)
			osCommand = WINDOWS_CMD;
		else 
			throw new ProcessException(String.format("Unknown operating system \"%s\"", OS));
		Arrays.asList(osCommand).forEach(arg -> this.command.add(arg.toString()));
		StringBuilder builder = new StringBuilder();
		command.forEach(arg -> builder.append(' ').append(arg));
		this.command.add(builder.toString());
	}
	
	/**
	 * Returns process after executing commands in give line buffer
	 * @param lineBuffer Line buffer
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
	 * @return Process object
	 */
	public Process execute(List<String> lineBuffer, long timeout, TimeUnit unit) {
	    ProcessBuilder pb = new ProcessBuilder(command);
	    Map<String, String> env = pb.environment();
	    env.put(CLASSPATH, classpath);
	    //env.remove("OTHERVAR");
	    //env.put("VAR2", env.get("VAR1") + "suffix");
	    pb.directory(directory);
	    //pb.inheritIO();
	    //File log = new File("log");
	    pb.redirectErrorStream(true);
	    pb.redirectOutput(Redirect.PIPE);
	    Process process = null;
	    try {
	        process = pb.start();
	        InputStream is = process.getInputStream();
	        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
	        LineBufferThread lineReader = new LineBufferThread(bufferedReader, lineBuffer);
	        lineReader.start();
	    } catch (IOException e) {
	    	throw new ProcessException("Error while streaming process", e);
	    }
	    CompletableFuture<Process> processFuture = process.onExit();
	    try {
			return processFuture.get(timeout, unit);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
	    	throw new ProcessException("Process failed to terminate normally", e);
		}
	}
	
	public void addJars(String root, File folder) {
		initializeClasspath();
		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("jar");
			}};
		StringBuilder builder = new StringBuilder();
		File[] filterFiles = folder.listFiles(filter);
		if (filterFiles == null)
			throw new ProcessException(String.format("Collecting jar files from folder %s failed", folder.toString()));
		Arrays.asList(filterFiles).forEach(file -> {
			if (builder.length() > 0)
				builder.append(File.pathSeparatorChar);
			builder.append(root).append(File.separatorChar).append(file.getName());
		});
		classpath += builder.toString();
	}
	
	public void addDirectory(String path) {
		initializeClasspath();
		classpath += path;
	}
	
	private void initializeClasspath() {
		if (classpath.isEmpty())
		    classpath = ".";
	    classpath += File.pathSeparatorChar;
	}

}
