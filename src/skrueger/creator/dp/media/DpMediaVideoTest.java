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
