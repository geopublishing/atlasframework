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
package skrueger.atlas.gui.internal;

import java.awt.Component;
import java.io.File;

import skrueger.atlas.AVUtil;
import skrueger.atlas.AtlasViewer;

public abstract class AtlasExportTask extends AtlasTask<Boolean> {

	protected File exportDir;

	protected boolean success = false;

	public AtlasExportTask(final Component owner, final String string) {
		super(owner, string);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see skrueger.atlas.gui.internal.AtlasTask#done()
	 */
	@Override
	protected void done() {
		super.done();

		// Ask to open the folder

		if (success) {
			setPrefix("");

			final String exportDoneMsg = AtlasViewer.R(
					"AtlasExportTask.export_to_XXX_done", exportDir
							.getAbsolutePath());

			if ((exportDir != null)) {
				final String openExportFolderMsg = AtlasViewer
						.R("AtlasExportTask.process.opening_export_folder");
				/**
				 * Starting an explorer might take some time... Sleep for 1s
				 * here to allow the window to pop up.
				 */
				progressWindow.setDescription(openExportFolderMsg);
				try {
					AVUtil.openOSFolder(exportDir);
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					progressWindow.exceptionOccurred(e1);
				}

				progressWindow.setDescription(exportDoneMsg);
			} else {
				progressWindow.setDescription(exportDoneMsg);
			}
		}

//		progressWindow.setCanceled(false); // /??? Why that? 23.6.09 SK
//		progressWindow.complete();
		progressWindow.dispose();

	}
}
