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
package entities_axioms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

import agriculture.Agri20Year;
import agriculture.AgriAreaPercent;
import agriculture.provider.ReadAgriSolution;
import agriculture.provider.ReadAgriSolution.RecordCallback;
import au.com.cybersearch2.taq.db.ConnectionProfile;
import au.com.cybersearch2.taq.db.h2.H2;
import au.com.cybersearch2.taq.helper.Taq;
import utils.ResourceHelper;

/**
 * AgricultureReportTest
 * @author Andrew Bowley
 */
public class AgricultureReport2Test
{
	
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
    @Test
    public void testAgricultureReport2() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("agriculture-report2");
        Taq taq = new Taq(args);
		taq.findFile();
		taq.compile();
		testAgriPercent(taq);
        File testFile = ResourceHelper.getTestResourceFile("entities_axioms/agriculture-report.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"));
    	RecordCallback callback = new RecordCallback() {

           	NumberFormat formatter = new DecimalFormat("#0");  

			@Override
			public void onNextRecord(Agri20Year ari20Year) {
				StringBuilder builder = new StringBuilder();
				builder.append(ari20Year.getId()).append(' ');
				builder.append(ari20Year.getCountry()).append(' ');
				builder.append(formatter.format(ari20Year.getSurfaceArea()));
	            try {
					assertThat(builder.toString()).isEqualTo(reader.readLine());
				} catch (IOException e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
			}};
		taq.execute("more_agriculture");
		String agriSolutionPath = Paths.get(Taq.WORKSPACE, "db", "agri_20_year2").toString();
		ReadAgriSolution readAgriSolution = new ReadAgriSolution();
		ConnectionProfile profile = new ConnectionProfile( "agri_20_year2", new H2(), agriSolutionPath);
		assertThat(readAgriSolution.selectAll(profile, callback)).isEqualTo(65);
        reader.close();
    }

	private void testAgriPercent(Taq taq) throws IOException {
        File testFile = ResourceHelper.getResourceFile("agri-decades.taq");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"));
        assertThat("axiom list agri_decades ".equals(reader.readLine())).isEqualTo(true);
        assertThat("( country, surface_area_Km2, Y1970, Y1980, Y1990, Y2000, Y2010  )".equals(reader.readLine())).isEqualTo(true);
        agriculture.provider.ReadAgriData2.RecordCallback callback =
        	new agriculture.provider.ReadAgriData2.RecordCallback() {

           	NumberFormat formatter = new DecimalFormat("#0");  

 				@Override
				public void onNextRecord(AgriAreaPercent agriAreaPercent) {
					String record = null;
					try {
						record = reader.readLine();
					} catch (IOException e) {
						e.printStackTrace();
						fail(e.getMessage());
					}
					int pos1 = record.indexOf('"');
					int pos2 = record.indexOf('"', pos1 + 1);
					String country = record.substring(pos1 + 1, pos2);
					pos1 = record.indexOf('"', pos2 + 1);
					pos2 = record.indexOf('"', pos1 + 1);
	        	    Scanner scanner = new Scanner(record.substring(pos1 + 1, pos2));
					double surface_area_Km2 = scanner.nextDouble();
					record = record.substring(pos2 + 2, record.length() - 2).trim();
					String[] parts = record.split(",");
					String Y1970 = formatter.format(parseDouble(parts[0]));
					String Y1980 = formatter.format(parseDouble(parts[1]));
					String Y1990 = formatter.format(parseDouble(parts[2]));
					String Y2000 = formatter.format(parseDouble(parts[3]));
					String Y2010 = formatter.format(parseDouble(parts[4]));
					assertThat(agriAreaPercent.getCountry()).isEqualTo(country);
					assertThat(agriAreaPercent.getSurfaceAreaKm2()).isEqualTo(surface_area_Km2);
					assertThat(formatter.format(agriAreaPercent.getY1970())).isEqualTo(Y1970);
					assertThat(formatter.format(agriAreaPercent.getY1980())).isEqualTo(Y1980);
					assertThat(formatter.format(agriAreaPercent.getY1990())).isEqualTo(Y1990);
					assertThat(formatter.format(agriAreaPercent.getY2000())).isEqualTo(Y2000);
					assertThat(formatter.format(agriAreaPercent.getY2010())).isEqualTo(Y2010);
				}

				private double parseDouble(String string) {
					string = string.trim();
					return Double.parseDouble(string);
				}
			};
			taq.execute("initialize" + "." + "do_export");
			String agriDataPath = Paths.get(Taq.WORKSPACE, "db", "agri-area-percent2").toString();
			agriculture.provider.ReadAgriData2 readAgriData = new agriculture.provider.ReadAgriData2();
			ConnectionProfile profile = new ConnectionProfile("agri_20_year2", new H2(), agriDataPath);
			assertThat(readAgriData.selectAll(profile, callback)).isEqualTo(208);
	        reader.close();
	}

}
