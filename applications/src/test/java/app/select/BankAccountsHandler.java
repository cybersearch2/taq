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
package app.select;

import java.util.List;

import app.AppCompletionHandler;

public class BankAccountsHandler extends AppCompletionHandler {

	String[] expected = new String[] { 
			"account_type(account_type=cre)",
			"account_type(account_type=sav)",
			"account_type(account_type=chq)",
			"",
			"bank_account(prefix=456448, bank=Bank of Queensland, bsb=124-001, account_type=cre)",
			"bank_account(prefix=456445, bank=Commonwealth Bank Aust., bsb=527-146, account_type=sav)",
			"bank_account(prefix=456443, bank=Bendigo Bank LTD, bsb=633-000, account_type=chq)",
		};

		@Override
		public void onAppComplete(List<String> lineBuffer) {
			compareArray(expected, lineBuffer);
		}

}
