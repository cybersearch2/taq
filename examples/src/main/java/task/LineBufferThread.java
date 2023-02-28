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
import java.io.IOException;
import java.util.List;

public class LineBufferThread extends Thread {

    public final static int BUFFER_SIZE = 256;

	private final static class LineBufferRunnable implements Runnable {

	    private final BufferedReader bufferedReader;
		private final List<String> lineBuffer;

	    private int cursor;
	    private String[] circularBuffer = new String[BUFFER_SIZE];

	    public LineBufferRunnable(BufferedReader bufferedReader, List<String> lineBuffer) {
	    	this.bufferedReader = bufferedReader;
	    	this.lineBuffer = lineBuffer;
	    }
	    
        @Override
        public void run() {
        	cursor = 0;
            String line = null;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    //logger.verbose(line);
                	circularBuffer[cursor++] = line;
                    if (cursor == BUFFER_SIZE) {
                    	for (int i = 0; i < BUFFER_SIZE; ++i)
                    	    lineBuffer.add(circularBuffer[i]);
                    	cursor = 0;
                    }
                 }
            } catch (IOException e) {
                // do nothing.
            } finally {
            	for (int i = 0; i < cursor; ++i)
            	    lineBuffer.add(circularBuffer[i]);
                try { 
                	bufferedReader.close(); 
                } catch (IOException e) {
                }
            }
        }
	}
	
	public LineBufferThread(BufferedReader bufferedReader, List<String> lineBuffer) {
		super(new LineBufferRunnable(bufferedReader, lineBuffer));
	}
	
}
