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
package skrueger.atlas.dp.media;

import java.awt.Component;

import org.apache.log4j.Logger;

import skrueger.atlas.AVUtil;
import skrueger.atlas.AtlasConfig;
import skrueger.atlas.dp.DpEntryType;

public class DpMediaPDF extends DpMedia {
	static private final Logger LOGGER = Logger.getLogger(DpMediaPDF.class);

	public DpMediaPDF(AtlasConfig ac) {
		super(ac);
		setType(DpEntryType.PDF	);
	}
//	
//	@Override
//	public DpMediaPDF copy() {
//		return copyTo(new DpMediaPDF(ac));
//	}
//	
//	@Override
//	public DpMediaPDF copyTo(Object t) {
//		throw new RuntimeException("not implemented yet");
//	}

	/**
	 * Tries to open a PDF viewer of the host system.
	 */
	@Override
	public Object show(Component owner) {
		Exception error = AVUtil.launchPDFViewer(owner, getUrl(owner), getTitle().toString());
		
		if (error != null) {
			setBrokenException(error);
		}

		return error;
	}


	@Override
	public void exportWithGUI(Component owner) {
		// TODO exportWithGUI PDF
		throw new RuntimeException("Sorry, export of PDF with GUI is not yet implemented.");
	}

}
