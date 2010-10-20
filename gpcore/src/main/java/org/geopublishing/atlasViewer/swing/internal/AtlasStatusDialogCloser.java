/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.swing.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.SwingWorker;

import org.geopublishing.atlasViewer.AtlasStatusDialogInterface;

public class AtlasStatusDialogCloser implements PropertyChangeListener {
	private AtlasStatusDialogInterface dialog;

	public AtlasStatusDialogCloser(AtlasStatusDialogInterface dialog) {
		this.dialog = dialog;
	}

	public void propertyChange(PropertyChangeEvent event) {
		if ("state".equals(event.getPropertyName())
				&& SwingWorker.StateValue.DONE == event.getNewValue()) {
			dialog.complete();
		}
	}
}
