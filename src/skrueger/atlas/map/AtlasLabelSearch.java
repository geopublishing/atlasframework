/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasViewer application - A GIS viewer application targeting at end-users with no GIS-experience. Its main purpose is to present the atlases created with the Geopublisher application.
 * http://www.geopublishing.org
 * 
 * AtlasViewer is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.atlas.map;

import org.apache.log4j.Logger;
import org.geotools.map.MapLayer;
import org.opengis.feature.simple.SimpleFeature;

import schmitzm.geotools.gui.SelectableXMapPane;
import skrueger.atlas.dp.DataPool;
import skrueger.atlas.dp.DpEntry;
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
