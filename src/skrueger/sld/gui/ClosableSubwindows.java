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
package skrueger.sld.gui;

import java.awt.Window;

import org.geotools.util.WeakHashSet;

public interface ClosableSubwindows {

	/** All GUIs have to register their open GUIs here.. thereby */
	public WeakHashSet<Window> openWindows = new WeakHashSet<Window>(Window.class);

	public void dispose();

}
