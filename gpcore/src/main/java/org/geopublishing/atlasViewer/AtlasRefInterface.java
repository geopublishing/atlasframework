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
package org.geopublishing.atlasViewer;

import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;

public interface AtlasRefInterface<ReferencedTargetClass> {

	/**
	 * @return The ID of the {@link AtlasRefInterface} referenced
	 */
	public String getTargetId();

	/**
	 * @return true if the target {@link AtlasRefInterface} is a {@link DpLayer}
	 */
	public boolean isTargetLayer();

	/**
	 * @return the targeted {@link DpRef}
	 */
	public ReferencedTargetClass getTarget();

}
