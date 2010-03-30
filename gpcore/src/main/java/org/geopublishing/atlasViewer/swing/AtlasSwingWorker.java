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
package org.geopublishing.atlasViewer.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasViewer.AtlasStatusDialogInterface;
import org.geopublishing.atlasViewer.swing.internal.AtlasStatusDialog;
import org.geopublishing.atlasViewer.swing.internal.AtlasStatusDialogCloser;


public abstract class AtlasSwingWorker<K> extends SwingWorker<K, String> {
	protected Logger LOGGER = ASUtil.createLogger(this);

	protected final AtlasStatusDialogInterface statusDialog;

	@Override
	protected void process(List<String> chunks) {
		for (String s : chunks)
			statusDialog.setDescription(s);

	}

	public AtlasSwingWorker(AtlasStatusDialogInterface statusDialog) {
		this.statusDialog = statusDialog;
		addPropertyChangeListener(new AtlasStatusDialogCloser(statusDialog));
		
		statusDialog.addCancelListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				AtlasSwingWorker.this.cancel(true);
			}
		});
	}

	public AtlasSwingWorker(Component parentGUI) {
		this(new AtlasStatusDialog(parentGUI));
	}

	/**
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws CancellationException When cancel has been pressed
	 */
	public K executeModal() throws InterruptedException, ExecutionException, CancellationException {
		execute();
		statusDialog.startModal();
		return get();
	}

}
