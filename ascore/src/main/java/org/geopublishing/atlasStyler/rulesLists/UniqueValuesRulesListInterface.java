package org.geopublishing.atlasStyler.rulesLists;

import java.io.IOException;
import java.util.Set;

import org.geopublishing.atlasStyler.RuleChangedEvent;

public interface UniqueValuesRulesListInterface<VALUETYPE> extends
		RulesListInterface {

	/**
	 * @param uniqueValue
	 *            Adds a unique value to to the list of values.
	 * 
	 * @return <code>false</code> is the value already exists
	 */
	public boolean addUniqueValue(final VALUETYPE uniqueValue);

	public void pushQuite();

	public void popQuite(RuleChangedEvent ruleChangedEvent);

	public Set<VALUETYPE> getAllUniqueValuesThatAreNotYetIncluded()
			throws IllegalArgumentException, IOException;

}
