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
