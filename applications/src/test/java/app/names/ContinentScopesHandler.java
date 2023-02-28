package app.names;

import java.util.List;

import app.AppCompletionHandler;

public class ContinentScopesHandler extends AppCompletionHandler  {

	@Override
	public void onAppComplete(List<String> lineBuffer) {
		compareFile(lineBuffer, "names/continent-scopes.txt");
	}
}
