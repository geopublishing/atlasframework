/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasStyler SLD/SE Editor application - A Java Swing-based GUI to create OGC Styled Layer Descriptor (SLD 1.0) / OGC Symbology Encoding 1.1 (SE) XML files.
 * http://www.geopublishing.org
 * 
 * AtlasStyler SLD/SE Editor is part of the Geopublishing Framework hosted at:
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
package skrueger.sld.classification;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import skrueger.sld.ASUtil;

public abstract class Classification {
	protected Logger LOGGER = ASUtil.createLogger(this);

	private Set<ClassificationChangedListener> listeners = new HashSet<ClassificationChangedListener>();

	/**
	 * @return Returns the number of classes
	 */
	public abstract int getNumClasses();

	public void addListener(ClassificationChangedListener l) {
		listeners.add(l);
	}

	public void removeListener(ClassificationChangedListener l) {
		listeners.remove(l);
	}

	/**
	 * Fires the given {@link ClassificationChangeEvent} to all listeners.
	 */
	public void fireEvent(final ClassificationChangeEvent e) {

		LOGGER.debug("Classification fires event: " + e.getType());

		// SwingUtilities.invokeLater(new Runnable() {
		// public void run() {

		for (ClassificationChangedListener l : listeners) {
			switch (e.getType()) {
			case NORM_CHG:
				l.classifierNormalizationChanged(e);
				break;
			case VALUE_CHG:
				l.classifierValueFieldChanged(e);
				break;
			case METHODS_CHG:
				l.classifierMethodChanged(e);
				break;
			case EXCLUDES_FILETER_CHG:
				l.classifierExcludeFilterChanged(e);
				break;
			case NUM_CLASSES_CHG:
				l.classifierNumClassesChanged(e);
				break;
			case START_NEW_STAT_CALCULATION:
				l.classifierCalculatingStatistics(e);
				break;
			case CLASSES_CHG:
				l.classifierAvailableNewClasses(e);
				break;

			}
		}
		// }
		// });

	}
}
