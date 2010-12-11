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
package org.geopublishing.atlasStyler;

import org.apache.log4j.Logger;

public class FreeMapSymbols {
	static public final String BASE_URL = "http://freemapsymbols/";

	static public final String LINE_URL = BASE_URL + "line";
	protected static Logger LOGGER = ASUtil.createLogger(FreeMapSymbols.class);
	static public final String POINT_URL = BASE_URL + "point";
	static public final String POLYGON_URL = BASE_URL + "polygon";

}
