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
package org.geopublishing.geopublisher.gui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.swing.JPanel;
import skrueger.AttributeMetadataInterface;
import skrueger.geotools.AttributeMetadataMap;
import skrueger.swing.SmallButton;

/**
 * This {@link JPanel} displays a short list of NODATA-Values for an attribute
 * and provides a button to edit the NODATA-Values ina modal dialog. The
 * attribute of which the NODATA is displayed can be changed by
 * {@link #setAttribute} methods.<br/> {@link PropertyChangeListener}s may be
 * registered to get Events of type {@link #PROPERTY_NODATAVALUES} when the
 * NODATA values are changed.
 */
public class NoDataPanel extends JPanel {

	/** Used for the PropertyChangeListener **/
	public static final String PROPERTY_NODATAVALUES = "NODATA Values";

	private JTextField noDataTextField;
	private final AttributeMetadataMap attributeMetaDataMap;
	private AttributeMetadataInterface atm;
//	private final SimpleFeatureType schema;

	/**
	 * @param attributeMetaDataMap
	 * @param attributeName
	 *            The selected attribute
	 */
	public NoDataPanel(AttributeMetadataMap attributeMetaDataMap,
			String attributeName, final SimpleFeatureType schema) {
		super(new MigLayout());
		this.attributeMetaDataMap = attributeMetaDataMap;
//		this.schema = schema;

		add(new SmallButton(new AbstractAction(GeopublisherGUI.R("NodataValues") + ":") { 

					@Override
					public void actionPerformed(ActionEvent e) {
						NoDataEditListDialog nodataEditListDialog = new NoDataEditListDialog(
								NoDataPanel.this, schema.getDescriptor(
										atm.getName()).getType().getBinding(),
								atm);
						nodataEditListDialog.setVisible(true);

						if (!nodataEditListDialog.isCancelled()){
							getNoDataTextfield().setText(atm.getNoDataValuesFormatted());
							getNoDataTextfield().setToolTipText(atm.getNoDataValuesFormatted());

							NoDataPanel.this.firePropertyChange(PROPERTY_NODATAVALUES, null, atm.getNodataValues());
						}
					}
				}));

		add(getNoDataTextfield());

		setAttribute(attributeName);
	}


	public void setAttribute(String attributeName) {

		this.atm = attributeMetaDataMap.get(attributeName);

		if (atm == null) {
			getNoDataTextfield().setText("");
			getNoDataTextfield().setToolTipText("");
			return;
		}

		getNoDataTextfield().setText(atm.getNoDataValuesFormatted());
		getNoDataTextfield().setToolTipText(atm.getNoDataValuesFormatted());
	}

	private JTextField getNoDataTextfield() {
		if (noDataTextField == null) {
			noDataTextField = new JTextField(60);
			noDataTextField.setEditable(false);
		}
		return noDataTextField;
	}

}
