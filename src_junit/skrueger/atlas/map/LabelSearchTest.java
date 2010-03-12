/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
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
