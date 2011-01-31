package org.geopublishing.geopublisher.export;

import org.geopublishing.atlasViewer.AtlasConfig;

public interface AtlasExporter {

	/**
	 * Exports the given {@link AtlasConfig}
	 */
	public abstract void export() throws Exception;

}
