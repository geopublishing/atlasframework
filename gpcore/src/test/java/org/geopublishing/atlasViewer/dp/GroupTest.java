package org.geopublishing.atlasViewer.dp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.LinkedList;

import org.geopublishing.atlasViewer.AtlasRefInterface;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.map.MapRef;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.junit.Test;

import de.schmitzm.testing.TestingClass;
import de.schmitzm.testing.TestingUtil;
public class GroupTest extends TestingClass {

	@Test
	public void testRemoveMapFromMappoolMustRemoveMapRefFromGroups2()
			throws IOException {

		AtlasConfigEditable ace = new AtlasConfigEditable(
				TestingUtil.getNewTempDir());
		ace.getMapPool().add(new Map(ace));

		Map map = ace.getMapPool().get(0);

		Group rootGroup = ace.getRootGroup();
		rootGroup.add(new MapRef(map, ace.getMapPool()));

		Group subMenu = new Group(ace);
		rootGroup.add(subMenu);

		subMenu.add(new MapRef(map, ace.getMapPool()));
		subMenu.add(new MapRef(map, ace.getMapPool()));

		// cOUNT references to this map in the Groups
		{
			LinkedList<AtlasRefInterface<?>> foundin = new LinkedList<AtlasRefInterface<?>>();
			Group.findReferencesTo(rootGroup, map, foundin, false);
			assertEquals(3, foundin.size());
		}

		// Delete references to this map in the Groups
		LinkedList<AtlasRefInterface<?>> deletedIn = new LinkedList<AtlasRefInterface<?>>();
		Group.findReferencesTo(rootGroup, map, deletedIn, true);
		assertEquals(3, deletedIn.size());

		{
			LinkedList<AtlasRefInterface<?>> foundin = new LinkedList<AtlasRefInterface<?>>();
			Group.findReferencesTo(rootGroup, map, foundin, false);
			assertEquals(0, foundin.size());
		}

	}

	@Test
	public void testRemoveMapFromMappoolMustRemoveMapRefFromGroups()
			throws IOException {

		AtlasConfigEditable ace = new AtlasConfigEditable(
				TestingUtil.getNewTempDir());
		ace.getMapPool().add(new Map(ace));

		Map map = ace.getMapPool().get(0);
		Group rootGroup = ace.getRootGroup();
		rootGroup.add(new MapRef(map, ace.getMapPool()));

		// Delete references to this map in the Groups
		{
			LinkedList<AtlasRefInterface<?>> foundin = new LinkedList<AtlasRefInterface<?>>();
			Group.findReferencesTo(rootGroup, map, foundin, false);
			assertEquals(1, foundin.size());
		}

		// Delete references to this map in the Groups
		LinkedList<AtlasRefInterface<?>> deletedIn = new LinkedList<AtlasRefInterface<?>>();
		Group.findReferencesTo(rootGroup, map, deletedIn, true);
		assertEquals(1, deletedIn.size());

		{
			LinkedList<AtlasRefInterface<?>> foundin = new LinkedList<AtlasRefInterface<?>>();
			Group.findReferencesTo(rootGroup, map, foundin, false);
			assertEquals(0, foundin.size());
		}

	}

}
