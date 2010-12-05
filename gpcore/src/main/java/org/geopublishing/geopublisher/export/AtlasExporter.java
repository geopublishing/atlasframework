package org.geopublishing.geopublisher.export;

import org.geopublishing.atlasViewer.AtlasConfig;
import org.netbeans.spi.wizard.ResultProgressHandle;


public interface AtlasExporter {

	/**
	 * Exports the given {@link AtlasConfig}
	 */
	public void export(final ResultProgressHandle progress) throws Exception ;

}
