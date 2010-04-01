/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.AtlasCreator;
import org.geopublishing.geopublisher.UncacheAtlasAction;
import org.geotools.referencing.CRS;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import schmitzm.geotools.GTUtil;
import schmitzm.geotools.io.GeoImportUtil;
import schmitzm.swing.SwingUtil;
import skrueger.swing.CancelButton;
import skrueger.swing.CancellableDialogAdapter;
import skrueger.swing.OkButton;

/**
 * A {@link JPanel} that allows to enter/input the default CRS for the Atlas.
 * The default CRS is used for all Geodata that has no .PRJ or where the CRS
 * metadata can not be read. This dialog stes the static setton on
 * {@link GeoImportUtil}. Multiple instances of {@link AtlasCreator} will hence
 * always use the same default CRS - a future problem if this will ever happen.
 * 
 * @author Stefan A. Krueger
 * 
 * TODO should implement {@link CancellableDialogAdapter}
 */
public class DefaultCRSSelectionJDialog extends JDialog {
	private static final String UNKNOWNEPSG = "EPSG: - ";
	private static final Component explanationJLabel = new JLabel(AtlasCreator
			.R("DefaultCRSSelectionJPanel.explanation"));
	private final Component parentGUI;
	private JTextArea crsDefinitionJTextArea;
	private JLabel crsResultJLabel;
	private OkButton okButton;
	private CancelButton cancelButton;
	private String crsNotRecognizedText = AtlasCreator
			.R("DefaultCRSSelectionJPanel.NotRecognized");
	private final AtlasConfigEditable atlasConfigEditable;

	/**
	 * This is <code>null</code> or the {@link CRS} parsed from the
	 * {@link JTextArea}
	 **/
	private volatile CoordinateReferenceSystem crs;

	public DefaultCRSSelectionJDialog(Component parentGUI_,
			AtlasConfigEditable atlasConfigEditable) {
		super(SwingUtil.getParentWindow(parentGUI_));
		this.parentGUI = parentGUI_;
		this.atlasConfigEditable = atlasConfigEditable;

		initGui();

		pack();
	}

	private void initGui() {
		JPanel contentPane = new JPanel(new MigLayout("wrap 1,width 500"));

		contentPane.add(explanationJLabel, "sgx");
		contentPane.add(new JScrollPane(getCrsDefinitionJTextArea()), "sgx");
		contentPane.add(getCRSResultJLabel(), "sgx, grow");
		contentPane.add(getOkButton(), "split 2, right, tag ok");
		contentPane.add(getCancelButton(), "tag cancel");

		setContentPane(contentPane);
	}

	private OkButton getOkButton() {
		if (okButton == null) {
			okButton = new OkButton();
			getOkButton().addActionListener(new ActionListener() {

				/**
				 * The OkButton changes the default CRS in in GeoImportUtil and
				 * un-caches the AtlasConfigEditable.
				 */
				@Override
				public void actionPerformed(ActionEvent e) {
					GeoImportUtil.setDefaultCRS(crs);
					if (atlasConfigEditable != null) {
						new UncacheAtlasAction(parentGUI, atlasConfigEditable)
								.actionPerformed(null);
					}
					DefaultCRSSelectionJDialog.this.dispose();
				}

			});
		}
		return okButton;
	}

	private CancelButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new CancelButton();
			cancelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					DefaultCRSSelectionJDialog.this.dispose();
				}

			});
		}
		return cancelButton;
	}

	private JLabel getCRSResultJLabel() {
		if (crsResultJLabel == null) {
			crsResultJLabel = new JLabel();

			getCrsDefinitionJTextArea().getDocument().addDocumentListener(
					new DocumentListener() {

						@Override
						public void changedUpdate(DocumentEvent e) {
							tryToEvaluateToCrs();
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							tryToEvaluateToCrs();
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							tryToEvaluateToCrs();
						}

					});

			tryToEvaluateToCrs();
		}
		return crsResultJLabel;
	}

	private void tryToEvaluateToCrs() {
		String userInput = getCrsDefinitionJTextArea().getText();
		crs = GTUtil.createCRS(userInput);
		if (crs != null) {

			String humanReadable1 = crs.getName().getCode().replace('_', ' ');

			String authorityCodeTextString = ", " + getEPSGString(crs);

			getCRSResultJLabel().setText(
					AtlasCreator.R(
							"DefaultCRSSelectionJPanel.NewCRSRecognized",
							humanReadable1, authorityCodeTextString));
		} else {
			getCRSResultJLabel().setText(crsNotRecognizedText);
		}
		/**
		 * The OK button is only enabled if the CRS has been understood
		 **/
		getOkButton().setEnabled(
				crs != null
						&& (!CRS.equalsIgnoreMetadata(crs, GeoImportUtil
								.getDefaultCRS())));

	}

	/**
	 * Tries to convert the given CRS to a EPSG:1234 formatted string. If no
	 * EPSG code is stored in the meta-data, it returns {@link #UNKNOWNEPSG}
	 */
	private String getEPSGString(CoordinateReferenceSystem crs_) {

		if (!crs_.getIdentifiers().isEmpty()) {
			Object next = crs_.getIdentifiers().iterator().next();
			if (next instanceof Identifier) {
				Identifier identifier = (Identifier) next;
				String authority = identifier.getAuthority().getTitle()
						.toString();
				if (authority.equals("European Petroleum Survey Group"))
					authority = "EPSG";

				return authority + ":" + identifier.getCode();
			}
		}

		return UNKNOWNEPSG;

	}

	/**
	 * The JTextArea showing a presentation of the actual default CRS. If
	 * possible, the CRS is presented in EPSG:12345 syntax.
	 */
	private JTextArea getCrsDefinitionJTextArea() {
		if (crsDefinitionJTextArea == null) {
			String textContent = getEPSGString(GeoImportUtil.getDefaultCRS());
			if (textContent.equals(UNKNOWNEPSG))
				textContent = GeoImportUtil.getDefaultCRS().toWKT();
			crsDefinitionJTextArea = new JTextArea(textContent, 30, 13);
			crsDefinitionJTextArea.setLineWrap(true);
			crsDefinitionJTextArea.setWrapStyleWord(true);

		}
		return crsDefinitionJTextArea;
	}

	public static void main(String[] args) {
		DefaultCRSSelectionJDialog dialog = new DefaultCRSSelectionJDialog(
				null, null);
		dialog.setModal(true);
		dialog.setVisible(true);

	}

}
