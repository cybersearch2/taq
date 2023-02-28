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
package au.com.cybersearch2.taq.provider;

import java.util.Deque;
import java.util.Iterator;
import java.util.Locale;

import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Axiom;

public class ListenerChain implements LocaleAxiomListener{

    private final Deque<LocaleAxiomListener> listenerChain;

    public ListenerChain(Deque<LocaleAxiomListener> listenerChain) {
    	this.listenerChain = listenerChain;
    }

	@Override
	public boolean onNextAxiom(QualifiedName qname, Axiom axiom, Locale locale) {
		Iterator<LocaleAxiomListener> iterator = listenerChain.iterator();
		while (iterator.hasNext())
			if (!iterator.next().onNextAxiom(qname, axiom, locale))
                return false;
		return true;
	}
}
