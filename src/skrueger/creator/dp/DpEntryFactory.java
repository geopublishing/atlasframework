/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
 * http://www.geopublishing.org
 * 
 * Geopublisher is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.creator.dp;

import java.awt.Component;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import skrueger.atlas.AtlasConfig;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.exceptions.AtlasImportException;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.dp.media.DpMediaPDFTester;
import skrueger.creator.dp.media.DpMediaVideoTest;
import skrueger.creator.gui.datapool.layer.DpLayerVectorFeatureSourceTest;

/**
 * This class tries to instantiate a subclass of {@link DpEntry} for an
 * arbitrary imported file.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */

public class DpEntryFactory {
	static final Logger LOGGER = Logger.getLogger(DpEntryFactory.class);

	static private List<DpEntryTesterInterface> testers = new LinkedList<DpEntryTesterInterface>();

	static {
		testers.add(new DpLayerVectorFeatureSourceTest());
		testers.add(new DpMediaVideoTest());
		testers.add(new DpMediaPDFTester());
		testers.add(new DpLayerRasterTest());
		testers.add(new DpLayerRasterPyramidTest());
	}

	/**
	 * This is a static class. => private constructor
	 */
	private DpEntryFactory() {
	}

	/**
	 * Try to create the correct {@link DpEntry}-type by running through all
	 * registered tests. If a test returns true, the file and corresponding
	 * files will be copied to the ad/data dir.
	 * 
	 * @return A fully instantiated {@link DpEntry} sub-type for {@link File} OR
	 *         <code>null</code>
	 * @param ace
	 *            The {@link AtlasConfig} to create the {@link DpEntry}
	 * @param file
	 *            The {@link File} to try to import. Must not be
	 *            <code>null</code>.
	 */
	public static DpEntry create(AtlasConfigEditable ace, File file,
			Component owner) throws AtlasImportException {

		/**
		 * We DON'T check for a compatible spelling here. Here we just check if
		 * we can read it. During import we should correct the spelling/writing
		 * though.
		 */

		for (DpEntryTesterInterface test : testers) {
			// If the test is successful create the "fitting"
			// DatapoolEntry. Test test function works on the real
			// filenames. When create imports the data to the ad/data
			// folder, names and dbf columns are corrected.
			if (test.test(owner, file))
				return test.create(ace, file, owner);
		}
		return null;
	}

}
