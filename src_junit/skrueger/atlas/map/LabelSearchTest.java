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

import java.io.File;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;
import schmitzm.io.IOUtil;
import skrueger.atlas.AtlasViewer;
import skrueger.atlas.gui.map.AtlasMapView;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.TestingUtil;
import skrueger.geotools.labelsearch.LabelSearch;
import skrueger.geotools.labelsearch.SearchResult;

public class LabelSearchTest extends TestCase {

	public void testSearch() throws Exception {

		URL atlasURL = ClassLoader.getSystemResource("data/testAtlas1");
		File atlasFile = IOUtil.urlToFile(atlasURL);
		AtlasConfigEditable ace = TestingUtil.getAtlasConfigE(atlasFile
				.getAbsolutePath());

		assertNotNull(ace);

		Map map = ace.getMapPool().get(0);

		AtlasMapView amv = new AtlasMapView(null, ace);
		amv.setMap(map);

		System.out.println("start");

		LabelSearch labelSearch = new LabelSearch(amv.getMapPane());
		List<SearchResult> search = labelSearch.search("Banikoara");

		assertEquals("One Banikoara is found", 1, search.size());

		search = labelSearch.search("banikOAra");
		assertTrue("Search is not case insensitive", search.size() == 1);

		search = labelSearch.search("ban");
		assertTrue("Search is not extending with a wildcard",
				search.size() == 2);

		System.out.println("finisched");

		AtlasViewer.getInstance();

		// SearchMapDialog searchMapDialog = new SearchMapDialog(null,
		// labelSearch, mapPane);
		// searchMapDialog.setModal(true);
		// searchMapDialog.setVisible(true);

	}
}
