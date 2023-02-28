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
package puzzles;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import utils.ResourceHelper;

/**
Deemonstrates numeric analysis of a 4x4 matrix to fill in the missing cells 
of a Sudoku puzzle.
*/
public class Sudoku
{
    private QueryProgramParser queryProgramParser;

    public Sudoku()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles the sudoku.taq script and runs the "" queries.<br/>
     * The expected results:<br/>
        4, 1, 2, 3,<br/>
        2, 3, 4, 1,<br/>
        1, 2, 3, 4,<br/>
        3, 4, 1, 2,<br/>
     */
    public void  calculateSudoku()
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("puzzles/sudoku.taq");
        queryProgram.executeQuery("sudoku");
    }

    /**
     * Run tutorial
     * @param args
     */
    public static void main(String[] args)
    {
        try 
        {
            Sudoku sudoku = new Sudoku();
            sudoku.calculateSudoku();
        } 
        catch (Throwable e) 
        { 
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
