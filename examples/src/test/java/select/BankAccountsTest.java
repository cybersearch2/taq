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
package select;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.helper.Taq;
import utils.ResourceHelper;

/**
 * BankAccountsTest
 * @author Andrew Bowley
 * 5Feb,2017
 */
public class BankAccountsTest
{
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
    @Test
    public void testBankAccounts() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("bank-accounts");
        Taq taq = new Taq(args);
        List<String> captureList = taq.getCaptureList();
		taq.findFile();
		taq.compile();
		taq.execute("bank_details");
        Iterator<String> iterator = captureList.iterator();
        File testFile = ResourceHelper.getTestResourceFile("select/bank-accounts.txt");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"));
        assertThat(iterator.hasNext()).isTrue();
        while (iterator.hasNext())
            checkSolution(reader, iterator.next());
        reader.close();
        captureList = taq.getCaptureList();
		taq.execute("account");
		iterator = captureList.iterator();
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next().equals("account_type(account_type=cre)"));
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next().equals("account_type(account_type=sav)"));
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next().equals("account_type(account_type=chq)"));
    }
    
    private void checkSolution(BufferedReader reader, String account)
    {
        try
        {
            String line = reader.readLine();
            assertThat(account).isEqualTo(line);
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }

    }
}
