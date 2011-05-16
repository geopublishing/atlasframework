package org.geopublishing.geopublisher.gui.settings;

import org.geopublishing.geopublisher.export.GpHosterServerSettings;

import de.schmitzm.io.AbstractServerList;

public class GpHosterServerList extends
		AbstractServerList<GpHosterServerSettings> {

	public GpHosterServerList(String propertiesString) {
		super(propertiesString);
		//
		// if (size() == 0) {
		// // Whenever this list is empty after parsing the properties list, we
		// // add the default GpHoster
		// add(GpHosterServerSettings.DEFAULT);
		// }
	}

	@Override
	protected GpHosterServerSettings newInstance() {
		return new GpHosterServerSettings();
	}

}
