package org.geopublishing.atlasViewer.dp;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.geopublishing.atlasViewer.AtlasRefInterface;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil;
import org.junit.Test;

public class GroupTest {

	@Test
	public void testRemoveMapFromMappoolMustRemoveMapRefFromGroups() {
		AtlasConfigEditable ace = GpTestingUtil.TestAtlas.small.getAce();

		Map map = ace.getMapPool().get(0);

		ace.getRootGroup();

		// Delete references to this map in the Groups
		{
			LinkedList<AtlasRefInterface<?>> foundin = new LinkedList<AtlasRefInterface<?>>();
			Group.findReferencesTo(ace.getRootGroup(), map, foundin, false);
			assertEquals(1, foundin.size());
		}

		// Delete references to this map in the Groups
		LinkedList<AtlasRefInterface<?>> deletedIn = new LinkedList<AtlasRefInterface<?>>();
		Group.findReferencesTo(ace.getRootGroup(), map, deletedIn, true);
		assertEquals(1, deletedIn.size());

		{
			LinkedList<AtlasRefInterface<?>> foundin = new LinkedList<AtlasRefInterface<?>>();
			Group.findReferencesTo(ace.getRootGroup(), map, foundin, false);
			assertEquals(0, foundin.size());
		}

	}

}
