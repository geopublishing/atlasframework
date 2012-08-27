/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.dp;

import java.awt.Component;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.media.DpMediaPICTURE;
import org.geopublishing.atlasViewer.exceptions.AtlasImportException;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.dp.media.DpMediaPDFTester;
import org.geopublishing.geopublisher.dp.media.DpMediaPICTURETester;
import org.geopublishing.geopublisher.gui.datapool.layer.DpLayerVectorFeatureSourceTester;

/**
 * This class tries to instantiate a subclass of {@link DpEntry} for an
 * arbitrary imported file.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 */

public class DpEntryFactory {
	static final Logger LOGGER = Logger.getLogger(DpEntryFactory.class);

	public static final FileFilter FILEFILTER_ALL_DPE_IMPORTABLE = new FileFilter() {

		@Override
		public String getDescription() {
			return "All importable"; // i8n
		}

		@Override
		public boolean accept(File f) {
			if (DpLayerRasterReaderTester.FILEFILTER.accept(f))
				return true;
			if (DpLayerVectorFeatureSourceTester.FILEFILTER.accept(f))
				return true;
			if (DpMediaPDFTester.FILEFILTER.accept(f))
				return true;
			if (DpMediaPICTURETester.FILEFILTER.accept(f))
				return true;
			return false;
		}
	};

	public final static List<DpEntryTesterInterface> testers = new LinkedList<DpEntryTesterInterface>();

	static {
		testers.add(new DpLayerVectorFeatureSourceTester());
		testers.add(new DpMediaPDFTester());
		testers.add(new DpMediaPICTURETester());
		testers.add(new DpLayerRasterReaderTester());
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

	public static boolean test(File file, Component owner) {
		for (DpEntryTesterInterface test : testers) {
			// If the test is successful create the "fitting"
			// DatapoolEntry. Test test function works on the real
			// filenames. When create imports the data to the ad/data
			// folder, names and dbf columns are corrected.
			if (test.test(owner, file))
				return true;
		}
		return false;
	};

}
