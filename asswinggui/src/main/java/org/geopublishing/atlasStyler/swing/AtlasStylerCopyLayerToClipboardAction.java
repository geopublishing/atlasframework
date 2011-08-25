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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasViewer.swing.Icons;

import de.schmitzm.geotools.styling.StyledLayerInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.io.IOUtil;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.ExceptionDialog;

public class AtlasStylerCopyLayerToClipboardAction extends AbstractAction {
	private static final long serialVersionUID = 4726448851995462364L;

	static private final Logger LOGGER = Logger.getLogger(AtlasStylerCopyLayerToClipboardAction.class);;

	private final StyledLayerInterface<?> styledLayer;

	private final Component owner;

	public AtlasStylerCopyLayerToClipboardAction(Component owner, StyledLayerInterface<?> styledLayer) {
		super(ASUtil.R("AtlasStylerGUI.copyLayerToClipboard"), Icons.ICON_EXPORT);
		this.owner = owner;
		this.styledLayer = styledLayer;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		File tempFile;
		try {
			tempFile = File.createTempFile("atlasStylerClipboard", ".sld");
		} catch (IOException e2) {
			throw new RuntimeException(e2);
		}
		
		try {
			StylingUtil.saveStyleToSld(styledLayer.getStyle(), tempFile);
			
			List<Exception> es = StylingUtil.validateSld(new FileInputStream(tempFile));
			if (es.size() > 0) {
				ExceptionDialog.show(
						owner,
						new IllegalStateException(ASUtil.R("AtlasStylerExport.WarningSLDNotValid",
								IOUtil.escapePath(styledLayer.getSldFile())), es.get(0)));
			}
			
			LangUtil.copyToClipboard(IOUtil.readFileAsString(tempFile));

		} catch (Exception e1) {
			LOGGER.error("saveStyleToSLD", e1);
			ExceptionDialog.show(owner, e1);
			return;
		} finally {
			tempFile.delete();
		}
	}
}
