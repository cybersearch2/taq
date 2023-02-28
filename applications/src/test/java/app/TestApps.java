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
package app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import task.LineBufferThread;
import task.ProcessRunner;
import utils.ResourceHelper;

public class TestApps {

	private static final class AppTest {
		
		private final String classname;
		private final String handler;
		
		public AppTest(String classname) {
			this.classname = classname;
			this.handler = "app." + classname + "Handler";
		}
		
		public String getClassname() {
			return classname;
		}
		
		public String getHandler() {
			return handler;
		}
	}

	private List<String> command;
	private ProcessRunner processRunner;
	private List<String> lineBuffer;
	private List<AppTest> appTestList;
	
	public TestApps() { 
		appTestList = new ArrayList<>(); 
		appTestList.add(new AppTest("introduction.HighCities"));
		appTestList.add(new AppTest("names.ContinentScopes"));
		appTestList.add(new AppTest("queries.CustomerCharge"));
		appTestList.add(new AppTest("queries.LogicChain"));
		appTestList.add(new AppTest("queries.HighCities2"));
		appTestList.add(new AppTest("terms.Types"));
		appTestList.add(new AppTest("terms.HighCities3"));
		appTestList.add(new AppTest("terms.EuroCities"));
		appTestList.add(new AppTest("operations.AmericanMegaCities"));
		appTestList.add(new AppTest("operations.QueryInWords"));
		appTestList.add(new AppTest("operations.MultiCurrency"));
		appTestList.add(new AppTest("expressions.Expressions"));
		appTestList.add(new AppTest("expressions.ScopeCycles"));
		appTestList.add(new AppTest("expressions.Declarations"));
		appTestList.add(new AppTest("data_flow.AsiaTopTen"));
		appTestList.add(new AppTest("data_flow.Grouping"));
		appTestList.add(new AppTest("data_flow.MoreAgriculture"));
		appTestList.add(new AppTest("basic_lists.AssignMarks"));
		appTestList.add(new AppTest("basic_lists.Lists"));
		appTestList.add(new AppTest("basic_lists.Personalities"));
		appTestList.add(new AppTest("basic_lists.SaleItems"));
		appTestList.add(new AppTest("basic_lists.Personalities2"));
		appTestList.add(new AppTest("axioms.BlackIsWhite"));
		appTestList.add(new AppTest("axioms.Gaming"));
		appTestList.add(new AppTest("axioms.HighCitiesDynamic"));
		appTestList.add(new AppTest("archetypes.WhoIsVaxxed"));
		appTestList.add(new AppTest("archetypes.Birds"));
		appTestList.add(new AppTest("flow.ConvertAreas"));
		appTestList.add(new AppTest("flow.Factorial"));
		appTestList.add(new AppTest("flow.HighCitiesSorted"));
		appTestList.add(new AppTest("flow.NestedLoops"));
		appTestList.add(new AppTest("cursor.CurrencyCursor"));
		appTestList.add(new AppTest("cursor.EmptyList"));
		appTestList.add(new AppTest("cursor.HighCitiesSorted2"));
		appTestList.add(new AppTest("cursor.PetNames"));
		appTestList.add(new AppTest("cursor.ReversePetNames"));
		appTestList.add(new AppTest("select.BankAccounts"));
		appTestList.add(new AppTest("select.ColorSwatch"));
		appTestList.add(new AppTest("select.ColorSwatch2"));
		appTestList.add(new AppTest("select.DefaultAccount"));
		appTestList.add(new AppTest("select.DynamicGrouping"));
		appTestList.add(new AppTest("select.PerfectMatch"));;
		appTestList.add(new AppTest("functions.FunctionServiceItems"));
		appTestList.add(new AppTest("functions.FunctionStudentScores"));
		appTestList.add(new AppTest("functions.Types2"));
		appTestList.add(new AppTest("query_call.Birds2"));
		appTestList.add(new AppTest("query_call.Circumference"));
		appTestList.add(new AppTest("query_call.QueryStudentScores"));
		appTestList.add(new AppTest("receiver.Birds3"));
		appTestList.add(new AppTest("receiver.ChargePlusTax"));
		appTestList.add(new AppTest("receiver.ReceiveServiceItems"));
		appTestList.add(new AppTest("receiver.ReceiveStudentScores"));
		appTestList.add(new AppTest("receiver.ReceiveStudentScores2"));
		appTestList.add(new AppTest("receiver.StampDuty2"));
		appTestList.add(new AppTest("scopes.CalculateSquareMiles3"));
		appTestList.add(new AppTest("scopes.ForeignColors"));
		appTestList.add(new AppTest("scopes.GermanColors"));
		appTestList.add(new AppTest("scopes.ForeignTotal"));
		appTestList.add(new AppTest("scopes.Lists2"));
		appTestList.add(new AppTest("scopes.Lists3"));
		appTestList.add(new AppTest("locales.ShowLocale"));
		appTestList.add(new AppTest("locales.EuroTotal"));
		appTestList.add(new AppTest("locales.EuroTotal2"));
		appTestList.add(new AppTest("locales.ScopeChain"));
		appTestList.add(new AppTest("regex.GroupInWords"));
		appTestList.add(new AppTest("regex.RegexPetNames"));
		appTestList.add(new AppTest("regex.RegexReversePetNames"));
		appTestList.add(new AppTest("regex.Pets"));
		appTestList.add(new AppTest("regex.ServiceItems"));
		appTestList.add(new AppTest("resources.Dictionary"));
		appTestList.add(new AppTest("resources.Dictionary2"));
		appTestList.add(new AppTest("resources.ForeignColors2"));
		appTestList.add(new AppTest("puzzles.Sudoku"));
		appTestList.add(new AppTest("puzzles.TowersOfHanoi"));
		appTestList.add(new AppTest("entities_axioms.Cities"));
		appTestList.add(new AppTest("entities_axioms.HighCitiesSorted3"));
		appTestList.add(new AppTest("entities_axioms.PerfectMatch2"));
		appTestList.add(new AppTest("entities_axioms.AgricultureReport"));
		appTestList.add(new AppTest("entities_axioms.AgricultureReport2"));
	}
	
	@Before
	public void setUp() {
		command = new ArrayList<>();
		File examplesPath = ResourceHelper.getExamplesPath();
		processRunner = new ProcessRunner(examplesPath);
		lineBuffer = new ArrayList<>(LineBufferThread.BUFFER_SIZE);
		processRunner.addDirectory("." + File.separator + "target" + File.separator + "classes");
		processRunner.addDirectory("." + File.separator + "lib");
		processRunner.addJars("." + File.separator + "libs", new File(examplesPath, "libs"));
	}
	
	@SuppressWarnings("unchecked")
	@Test 
	public void testAllApps() {
		for (AppTest appTest: appTestList) {
			String classname = appTest.getClassname();
			int pos = classname.indexOf('.');
			String simpleName = classname.substring(pos + 1);
			String topic = classname.substring(0, pos);
			String progress = String.format("Tesing %s in %s", simpleName, topic);
			System.out.println(progress);
			command.clear();
			command.add("java");
			command.add(classname);
			processRunner.setShellCommand(command);
			lineBuffer.clear();
			Process process = processRunner.execute(lineBuffer, 1L, TimeUnit.MINUTES);
			//lineBuffer.forEach(line -> System.out.println(line));
			assertThat(process.exitValue() == 0);
			Class<AppCompletionHandler> clazz = null;
			try {
				clazz = (Class<AppCompletionHandler>) Class.forName(appTest.getHandler());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				fail();
			}
			try {
				AppCompletionHandler handler = clazz.getDeclaredConstructor().newInstance();
				handler.onAppComplete(lineBuffer);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				fail();
			}
		}
		System.out.println("All tests completed sucessfully");
	}

}
