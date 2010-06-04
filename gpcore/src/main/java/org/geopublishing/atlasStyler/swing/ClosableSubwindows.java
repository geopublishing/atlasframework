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
package org.geopublishing.atlasStyler.swing;

import java.awt.Window;

import org.geotools.util.WeakHashSet;

import skrueger.swing.Disposable;

public interface ClosableSubwindows extends Disposable {

	/** All GUIs have to register their open GUIs here.. thereby */
	public WeakHashSet<Window> openWindows = new WeakHashSet<Window>(Window.class);

	public void dispose();

}
