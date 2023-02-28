package agriculture.provider;

import au.com.cybersearch2.taq.db.DatabaseProvider;
import au.com.cybersearch2.taq.db.DatabaseSupport;
import au.com.cybersearch2.taq.db.EntityCollector;
import au.com.cybersearch2.taq.db.EntityEmitter;
import au.com.cybersearch2.taq.interfaces.FunctionProvider;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.provider.CallHandler;

import java.security.ProviderException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import agriculture.Agri20Year;
import agriculture.AgriAreaPercent;

public class Agri20Year2Provider implements FunctionProvider {

	public static final class AgriDataCollback implements ReadAgriData2.RecordCallback {

		@Override
		public void onNextRecord(AgriAreaPercent agriAreaPercent) {
           	NumberFormat formatter = new DecimalFormat("#0");  
            StringBuilder builder = new StringBuilder();
            builder.append(agriAreaPercent.getId()).append(' ');
            builder.append(agriAreaPercent.getCountry()).append(',');
            builder.append(formatter.format(agriAreaPercent.getSurfaceAreaKm2())).append(',');
            builder.append(formatter.format(agriAreaPercent.getY1970())).append(',');
            builder.append(formatter.format(agriAreaPercent.getY1980())).append(',');
            builder.append(formatter.format(agriAreaPercent.getY1990())).append(',');
            builder.append(formatter.format(agriAreaPercent.getY2000())).append(',');
            builder.append(formatter.format(agriAreaPercent.getY2010()));
            System.out.println(builder.toString());
		}
	}
	
	public static final class AgriDataCallHandler extends CallHandler implements DatabaseSupport {

		private final Map<String,DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>>>
		providerMap;

		protected AgriDataCallHandler(String name) {
			super(name);
			this.providerMap = new HashMap<>();
		}

		@Override
		public void setDatabaseProviders(
				List<DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>>> providers) {
			providers.forEach(provider -> providerMap.put(provider.getName(), provider));
		}

		@Override
		public boolean evaluate(List<Term> argumentList) {
			if (!argumentList.isEmpty()) {
				String reaourceName = argumentList.get(0).getValue().toString();
				DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>> provider = providerMap.get(reaourceName);
				if (provider != null) {
					ReadAgriData2 readAgriData = new ReadAgriData2();
					int count = readAgriData.selectAll(provider.getConnectionProfile(),  new AgriDataCollback());
					if (count == 0)
						System.out.println(NO_RECORDS);
					return true;
				}
			}
			return false;
		}
	}

	public static final class AgriSolutionCollback implements ReadAgriSolution.RecordCallback {

		@Override
		public void onNextRecord(Agri20Year ari20Year) {
           	NumberFormat formatter = new DecimalFormat("#0");  
            StringBuilder builder = new StringBuilder();
            builder.append(ari20Year.getId()).append(' ');
            builder.append(ari20Year.getCountry()).append(',');
            builder.append(formatter.format(ari20Year.getSurfaceArea())).append(',');
            System.out.println(builder.toString());
		}
	}
	
	public static final class AgriSolutionCallHandler extends CallHandler implements DatabaseSupport {

		
		private final Map<String,DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>>>
		providerMap;

		protected AgriSolutionCallHandler(String name) {
			super(name);
			this.providerMap = new HashMap<>();
		}

		@Override
		public void setDatabaseProviders(
				List<DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>>> providers) {
			providers.forEach(provider -> providerMap.put(provider.getName(), provider));
		}

		@Override
		public boolean evaluate(List<Term> argumentList) {
			if (!argumentList.isEmpty()) {
				String reaourceName = argumentList.get(0).getValue().toString();
				DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>> provider = providerMap.get(reaourceName);
				if (provider != null) {
					ReadAgriSolution readAgriSolution = new ReadAgriSolution();
					int count = readAgriSolution.selectAll(provider.getConnectionProfile(),  new AgriSolutionCollback());
					if (count == 0)
						System.out.println(NO_RECORDS);
					return true;
				}
			}
			return false;
		}
	}

	private static final String NO_RECORDS = "No records found in the database table";

	@Override
	public String getName() {
		return "agri20_year1_sql2";
	}

	@Override
	public CallHandler getCallEvaluator(String identifier) {
		if ("print_data".equals(identifier))
		    return new AgriDataCallHandler(getName());
		else if ("print_solution".equals(identifier))
		    return new AgriSolutionCallHandler(getName());
		else
			throw new ProviderException(String.format("Unknown function %s", identifier));
	}

}
