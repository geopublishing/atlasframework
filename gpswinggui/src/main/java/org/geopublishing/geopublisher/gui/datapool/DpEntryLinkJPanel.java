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
package org.geopublishing.geopublisher.gui.datapool;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.media.DpMediaPDF;
import org.geopublishing.atlasViewer.dp.media.DpMediaPICTURE;
import org.geopublishing.atlasViewer.swing.AvUtil;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import de.schmitzm.jfree.chart.style.ChartStyle;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.JPanel;

/**
 * This panel provides prepared links to links to this PDF
 */
public class DpEntryLinkJPanel extends JPanel {

	private final AtlasConfigEditable ace;
	private final DpEntry<? extends ChartStyle> dpe;

	public DpEntryLinkJPanel(final DpEntry<? extends ChartStyle> dpe) {

		super(new MigLayout("width 100%, wrap 1", "[grow]"));
		this.dpe = dpe;

		ace = (AtlasConfigEditable) dpe.getAtlasConfig();

		// LInks zum PDF als HTML Interne LInks...
		if (dpe instanceof DpMediaPDF || dpe instanceof DpMediaPICTURE) {

			JPanel linktoPanel = new JPanel(new MigLayout("w 100%, wrap"));
			linktoPanel.setBorder(BorderFactory
					.createTitledBorder((dpe instanceof DpMediaPDF) ? 
							AvUtil.R("DpEntryLinkJPanel.PDF") :
							AvUtil.R("DpEntryLinkJPanel.PICTURE")));

			linktoPanel.add(new JLabel(GeopublisherGUI
					.R("MapPreferences.LinkToThisMap.Label")));

			Box box = Box.createVerticalBox();
			final List<String> languages = dpe.getAtlasConfig().getLanguages();

			for (String lang : languages) {
				final JTextField linkToMeTextfield;
				linkToMeTextfield = new JTextField(dpe.getInternalLink());
				linkToMeTextfield.setEditable(false);
				JPanel oneLine = new JPanel(new BorderLayout());
				oneLine.add(linkToMeTextfield, BorderLayout.CENTER);
				JButton copyButton = new JButton(new AbstractAction(
						GeopublisherGUI.R("CopyButton.Label")) {

					@Override
					public void actionPerformed(ActionEvent e) {
						LangUtil.copyToClipboard(linkToMeTextfield.getText());
					}

				});
				copyButton.setToolTipText(GeopublisherGUI.R("CopyButton.TT"));

				copyButton.setBorder(BorderFactory.createEtchedBorder());

				oneLine.add(copyButton, BorderLayout.EAST);
				box.add(oneLine);

			}
			linktoPanel.add(box, "growx");
			add(linktoPanel, "grow x");
		}
	}

	protected String R(String string, Object... obj) {
		return GeopublisherGUI.R(string, obj);
	}

}
