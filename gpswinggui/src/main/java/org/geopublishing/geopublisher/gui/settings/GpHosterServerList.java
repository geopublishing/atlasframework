package org.geopublishing.geopublisher.gui.settings;

import org.geopublishing.geopublisher.export.GpHosterServerSettings;

import de.schmitzm.io.AbstractServerList;

public class GpHosterServerList extends
		AbstractServerList<GpHosterServerSettings> {

	public GpHosterServerList(String propertiesString) {
		super(propertiesString);
	}

	@Override
	protected GpHosterServerSettings newInstance() {
		return new GpHosterServerSettings();
	}

}
