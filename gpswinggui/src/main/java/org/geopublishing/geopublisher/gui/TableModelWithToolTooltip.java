/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui;

import javax.swing.table.AbstractTableModel;

/**
 * An interface thought to be used with {@link AbstractTableModel}. It adds a
 * method to request a tooltip for a given row and column.
 */
public interface TableModelWithToolTooltip {

	/**
	 * Paramters row and column are supposed to be queried in the view order.
	 * The method internally converts it into model row and column numbers.
	 * 
	 * @return <code>null</code> or a {@link String} which may contain
	 *         <code>html</code>
	 */
	String getToolTipFor(int rowIndex, int column);

}
