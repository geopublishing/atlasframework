/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher;

import java.awt.Component;
import java.io.File;
import java.net.URL;

import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.AtlasStatusDialogInterface;

/**
 * This interface guarantees that you can edit and save the {@link AtlasConfig}
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 */
public interface DpEditableInterface {

	/**
	 * Cast you {@link AtlasConfig} to {@link AtlasConfigEditable} if you are
	 * sure that you have got one ;-)
	 * 
	 * @return {@link AtlasConfigEditable}
	 */
	public AtlasConfigEditable getAce();
	
	
	public void copyFiles(URL sourceUrl, Component owner, File targetDir,
			AtlasStatusDialogInterface atlasStatusDialog) throws Exception;

}
