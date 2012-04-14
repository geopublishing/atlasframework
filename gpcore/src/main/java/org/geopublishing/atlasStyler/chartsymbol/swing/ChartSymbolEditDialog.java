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
package org.geopublishing.atlasStyler.chartsymbol.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ChartGraphic;
import org.geopublishing.atlasStyler.svg.swing.SVGSelector;
import org.geotools.styling.ExternalGraphic;

import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.CancellableDialogAdapter;

/**
 * Swing dialog to create/define a chartsymbol.
 * 
 * 
 * @see http://docs.geotools.org/latest/userguide/library/render/chart.html
 */
public class ChartSymbolEditDialog extends CancellableDialogAdapter {

	static private final Logger LOGGER = LangUtil
			.createLogger(ChartSymbolEditDialog.class);

	private JPanel jContentPane;

	private ExternalGraphic[] backup;

	private JPanel jPanelButtons;

	private final List<String> numericalAttributeNames;

	public ChartSymbolEditDialog(Component parentWindow,
			ExternalGraphic[] preSelection, List<String> numericalAttributeNames) {
		super(parentWindow);
		backup = preSelection;
		this.numericalAttributeNames = numericalAttributeNames;
		initialize();
	}

	/**
	 * A property changed event with this ID is fired, when the ExternalGraphic
	 * has been changed.
	 */
	public static final String PROPERTY_UPDATED = "Property Updated event ID";

	private void initialize() {
		this.setSize(350, 450);
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel(new MigLayout());

			jContentPane.add(new JButton(new AbstractAction("ggoo") {

				@Override
				public void actionPerformed(ActionEvent e) {
					ChartGraphic chartGraphic = new ChartGraphic();
					chartGraphic.addAttribute(numericalAttributeNames.get(0));
					chartGraphic.addAttribute(numericalAttributeNames.get(1));
					chartGraphic.addAttribute(numericalAttributeNames.get(2));

					ExternalGraphic[] egs = new ExternalGraphic[] { chartGraphic
							.getChartGraphic() };

					ChartSymbolEditDialog.this.firePropertyChange(
							SVGSelector.PROPERTY_UPDATED, null, egs);
				}
			}), "wrap");

			jContentPane.add(getJPanelButtons(), "bottom");
		}
		return jContentPane;
	}

	@Override
	public void cancel() {
		// Reset any changes by promoting the backed-up symbol
		firePropertyChange(SVGSelector.PROPERTY_UPDATED, null, backup);
	}

	/**
	 * This method initializes a panel with OK and Close buttons
	 */
	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel(new MigLayout());
			jPanelButtons.add(getOkButton(), "tag ok");
			jPanelButtons.add(getCancelButton(), "tag cancel");
		}
		return jPanelButtons;
	}

}
