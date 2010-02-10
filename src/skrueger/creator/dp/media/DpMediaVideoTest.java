/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.creator.dp.media;

import java.awt.Component;
import java.io.File;

import org.apache.log4j.Logger;

import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.media.DpMediaVideo;
import skrueger.atlas.exceptions.AtlasImportException;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.dp.DpEntryTesterInterface;
import skrueger.creator.dp.DpMediaVideoEd;

public class DpMediaVideoTest implements DpEntryTesterInterface {
	Logger log = Logger.getLogger(DpMediaVideoTest.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see skrueger.atlas.datapool.DatapoolEntryTester#test(java.io.File)
	 */
	@Override
	public final boolean test(Component owner, File f) {
		return false;
//		URL mediaURL;
//
//		if (!f.getName().toLowerCase().endsWith(".avi"))
//			return false;
//
//		// log.info(f.getName() + " has a video suffix, try to play it..");
//
//		try {
//			mediaURL = f.toURI().toURL();
//		} catch (MalformedURLException e) {
//			log.debug("test: mediaURL = f.toURL()", e);
//			return false;
//		}
//		JFrame mediaTest = new JFrame(AtlasCreator
//				.R("DpMediaVideoTest.TestVideoCompatability"));
//		mediaTest.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//		try {
//			TestVideoDialog tmd = new TestVideoDialog(owner, mediaURL);
//			return tmd.isAccepted();
//		} catch (AtlasRecoverableException e) {
//			ExceptionDialog.show(owner, e);
//			return false;
//		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * skrueger.atlas.datapool.DatapoolEntryTester#create(skrueger.atlas.AtlasConfig
	 * , java.io.File)
	 */
	@Override
	public final DpEntry create(AtlasConfigEditable ace, File file,
			Component owner)
			throws AtlasImportException {
		final DpMediaVideo datapoolMediaVideo = new DpMediaVideoEd(ace, file);
		return datapoolMediaVideo;
	}
}
