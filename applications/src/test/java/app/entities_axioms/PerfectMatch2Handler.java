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
package app.entities_axioms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import app.AppCompletionHandler;

public class PerfectMatch2Handler extends AppCompletionHandler {

	String[] expected = new String[] { 
			"apply_age_rating(Name=John, Age=23, Starsign=gemini, Rating=1.0, Timestamp=",
			"apply_age_rating(Name=Sue, Age=19, Starsign=cancer, Rating=NaN, Timestamp=",
			"apply_age_rating(Name=Sam, Age=34, Starsign=scorpio, Rating=0.3, Timestamp=",
			"apply_age_rating(Name=Jenny, Age=28, Starsign=gemini, Rating=0.6, Timestamp=",
			"apply_age_rating(Name=Andrew, Age=26, Starsign=virgo, Rating=0.6, Timestamp=",
			"apply_age_rating(Name=Alice, Age=20, Starsign=pisces, Rating=1.0, Timestamp=",
			"apply_age_rating(Name=Ingrid, Age=23, Starsign=cancer, Rating=1.0, Timestamp=",
			"apply_age_rating(Name=Jack, Age=32, Starsign=pisces, Rating=0.3, Timestamp=",
			"apply_age_rating(Name=Sonia, Age=33, Starsign=gemini, Rating=0.3, Timestamp=",
			"apply_age_rating(Name=Alex, Age=22, Starsign=aquarius, Rating=1.0, Timestamp=",
			"apply_age_rating(Name=Jill, Age=33, Starsign=cancer, Rating=0.3, Timestamp=",
			"apply_age_rating(Name=Fiona, Age=29, Starsign=gemini, Rating=0.6, Timestamp=",
			"apply_age_rating(Name=Melissa, Age=30, Starsign=virgo, Rating=0.3, Timestamp=",
			"apply_age_rating(Name=Tom, Age=22, Starsign=cancer, Rating=1.0, Timestamp=",
			"apply_age_rating(Name=Bill, Age=19, Starsign=virgo, Rating=NaN, Timestamp=",
			"",
			"id	Name,Starsign,Rating	Timestamp",
			"1	John,Gemini,23,1.0",
			"2	Sue,Cancer,19,NaN",
			"3	Sam,Scorpio,34,0.3",
			"4	Jenny,Gemini,28,0.6",
			"5	Andrew,Virgo,26,0.6",
			"6	Alice,Pisces,20,1.0",
			"7	Ingrid,Cancer,23,1.0",
			"8	Jack,Pisces,32,0.3",
			"9	Sonia,Gemini,33,0.3",
			"10	Alex,Aquarius,22,1.0",
			"11	Jill,Cancer,33,0.3",
			"12	Fiona,Gemini,29,0.6",
			"13	Melissa,Virgo,30,0.3",
			"14	Tom,Cancer,22,1.0",
			"15	Bill,Virgo,19,NaN"
		};

		@Override
		public void onAppComplete(List<String> lineBuffer) {
			if (lineBuffer.size() != expected.length) {
            	lineBuffer.forEach(err -> System.err.println(err));
                assertThat(lineBuffer.size()).isEqualTo(expected.length);
			}
			int index = 0;
			Iterator<String> iterator = lineBuffer.iterator();
			List<String> truncated = new ArrayList<>();
			while (iterator.hasNext()) {
				if (index == 32)
					break;
				String line = iterator.next();
				if (index < 15) {
					int pos = line.indexOf("Timestamp=");
					truncated.add(line.substring(0, pos + 10));
				} else if (index > 16) {
					int pos = line.indexOf(" ", 3);
					truncated.add(line.substring(0, pos));
				} else
					truncated.add(line);
				++index;
			}
			compareArray(expected, truncated);
		}

}
