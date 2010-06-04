/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.map;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DataPool;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geotools.map.MapLayer;
import org.opengis.feature.simple.SimpleFeature;

import schmitzm.geotools.gui.SelectableXMapPane;
import skrueger.geotools.labelsearch.LabelSearch;
import skrueger.geotools.labelsearch.SearchResult;
import skrueger.geotools.labelsearch.SearchResultFeature;
import skrueger.i8n.I8NUtil;

public class AtlasLabelSearch extends LabelSearch {
	final static private Logger LOGGER = Logger
			.getLogger(AtlasLabelSearch.class);
	private final DataPool dp;

	public AtlasLabelSearch(SelectableXMapPane mapPane, DataPool dp) {
		super(mapPane);
		this.dp = dp;
	}

	@Override
	protected SearchResult createSearchResult(SimpleFeature f,
			String labelString, String dpeID, MapLayer ml) {

		DpEntry dpEntry = dp.get(dpeID);

		String title = "";
		if (!I8NUtil.isEmpty(dpEntry.getTitle())) {
			title = dpEntry.getTitle().toString();
		}

		return new SearchResultFeature(f, labelString, title, mapPane, ml);
	}

}
