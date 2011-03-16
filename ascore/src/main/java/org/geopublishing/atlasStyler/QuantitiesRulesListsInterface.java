package org.geopublishing.atlasStyler;


public interface QuantitiesRulesListsInterface {
	/**
	 * Different Methods to classify
	 */
	public enum METHOD {
		EI, MANUAL, QUANTILES;
		
		// TODO NATURAL BREAKS

		public String getDesc() {
			return ASUtil
					.R("QuantitiesClassifiction.Method.ComboboxEntry."
							+ toString());
		}

		public String getToolTip() {
			return ASUtil
					.R("QuantitiesClassifiction.Method.ComboboxEntry."
							+ toString() + ".TT");
		}
	}


	public static final METHOD DEFAULT_METHOD = METHOD.QUANTILES;

	void setMethod(METHOD method);

	METHOD getMethod();

}
