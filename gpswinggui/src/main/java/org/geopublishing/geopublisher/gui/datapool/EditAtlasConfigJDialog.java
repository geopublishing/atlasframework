package org.geopublishing.geopublisher.gui.datapool;
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
///** 
// Copyright 2008 Stefan Alfons Tzeggai 
// 
// atlas-framework - This file is part of the Atlas Framework
//
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
//
// Diese Bibliothek ist freie Software; Sie dürfen sie unter den Bedingungen der GNU Lesser General Public License, wie von der Free Software Foundation veröffentlicht, weiterverteilen und/oder modifizieren; entweder gemäß Version 2.1 der Lizenz oder (nach Ihrer Option) jeder späteren Version.
// Diese Bibliothek wird in der Hoffnung weiterverbreitet, daß sie nützlich sein wird, jedoch OHNE IRGENDEINE GARANTIE, auch ohne die implizierte Garantie der MARKTREIFE oder der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK. Mehr Details finden Sie in der GNU Lesser General Public License.
// Sie sollten eine Kopie der GNU Lesser General Public License zusammen mit dieser Bibliothek erhalten haben; falls nicht, schreiben Sie an die Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA.
// **/
//package skrueger.creator.gui.datapool;
//
//import java.awt.BorderLayout;
//import java.awt.Component;
//import java.awt.Frame;
//import java.awt.event.ActionEvent;
//
//import javax.swing.AbstractAction;
//import javax.swing.Box;
//import javax.swing.JDialog;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JTextField;
//import javax.swing.SpringLayout;
//
//import org.apache.log4j.Logger;
//import org.geotools.swing.ExceptionDialog;
//
//import schmitzm.swing.SpringUtilities;
//import schmitzm.swing.SwingUtil;
//import skrueger.atlas.AtlasConfig;
//import skrueger.atlas.exceptions.AtlasUserinputException;
//import skrueger.creator.AtlasConfigEditable;
//import skrueger.i8n.I8NUtil;
//import skrueger.swing.CancelButton;
//import skrueger.swing.OkButton;
//import skrueger.swing.TranslationEditJPanel;
//
///**
// * Shows a little GUI with settings and translations for the atlas in general
// * 
// * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
// */
//public class EditAtlasConfigJDialog extends JDialog {
//	private static Logger LOGGER = Logger
//			.getLogger(EditAtlasConfigJDialog.class);
//
//	private AtlasConfigEditable atlasConfig;
//
//	private boolean successful = false;
//
//	private JTextField sprachenTextfield;
//
//	private JTextField atlasVersionTextfield;
//
//	/**
//	 * Opens a modal {@link JDialog} that allows editing the parameters of an
//	 * atlas. See {@link #isSuccessful()} after the {@link JDialog} is closed.
//	 * 
//	 * @param owner
//	 */
//	public EditAtlasConfigJDialog(Frame owner, AtlasConfigEditable atlasConfig) {
//		super(owner);
//		this.atlasConfig = atlasConfig;
//		initialize();
//	}
//
//	/**
//	 * This method initializes this
//	 * 
//	 * @return void
//	 */
//	private void initialize() {
//		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
//		this.setSize(300, 200);
//		this.setContentPane(getJContentPane());
//		this.setTitle("Edit the parameters of the Atlas"); // i8nAC
//		this.pack();
//		SwingUtil.centerFrameOnScreen(this);
//		this.setModal(true);
//		this.setVisible(true);
//	}
//
//	/**
//	 * This method initializes jContentPane
//	 * 
//	 * @return javax.swing.JPanel
//	 */
//	private JPanel getJContentPane() {
//		JPanel jContentPane = new JPanel(new BorderLayout());
//		jContentPane.add(getAbfragenPanel(), BorderLayout.CENTER);
//		jContentPane.add(getButtonsPanel(), BorderLayout.SOUTH);
//		return jContentPane;
//	}
//
//	private Component getButtonsPanel() {
//		Box buttonsBox = Box.createHorizontalBox();
//
//		buttonsBox.add(getCancelButton());
//		buttonsBox.add(Box.createHorizontalGlue());
//		buttonsBox.add(getOkButton());
//
//		return buttonsBox;
//
//	}
//
//	private Component getCancelButton() {
//		CancelButton cancelButton = new CancelButton(new AbstractAction() {
//
//			public void actionPerformed(ActionEvent e) {
//				setSuccessful(false);
//				dispose();
//			}
//
//		});
//		return cancelButton;
//	}
//
//	/**
//	 * The Dialog was successfully or not
//	 */
//	protected void setSuccessful(boolean success) {
//		successful = success;
//	}
//
//	private Component getOkButton() {
//		OkButton okButton = new OkButton(new AbstractAction() {
//
//			public void actionPerformed(ActionEvent e) {
//				if (checkInputsAndSave())
//					dispose();
//			}
//		});
//		return okButton;
//	}
//
//	/**
//	 * Check values and save them in the target {@link AtlasConfig} if they are
//	 * valid
//	 * 
//	 * @return true if the Dialog is not needed anymore
//	 */
//	protected boolean checkInputsAndSave() {
//
//		String atlasversion = getAtlasversionTextfield().getText();
//		LOGGER.debug("AtlasVersion entered  = " + atlasversion);
//		Float avFloat = null;
//		try {
//			avFloat = Float.valueOf(atlasversion);
//		} catch (java.lang.NumberFormatException ex) {
//			// i8nAC
//			ExceptionDialog
//					.show(
//							this,
//							new AtlasUserinputException(
//									"The version of the atlas must be a float number (e.g. 1.0)."));
//
//			return false;
//		}
//
//		String langtxt = getSprachenTextfield().getText();
//		// langtxt = en,de
//		String[] langs = langtxt.toLowerCase().split(",");
//		for (String s : langs) {
//			s = s.trim();
//			if (!I8NUtil.isValidISOLangCode(s)) {
//				// i8nAC
//				ExceptionDialog
//						.show(
//								this,
//								new AtlasUserinputException(
//										"'"
//												+ s
//												+ "' is not a valid two-letter ISO-code for a language."));
//				return false;
//			}
//		}
//		// Saving the entered language-settings
//		atlasConfig.setLanguages(langs);
//
//		// Remembering the projectDir, and thereby registering the
//		// FileResourceLoader with ResMan
//		atlasConfig.setAtlasversion(avFloat);
//		setSuccessful(true);
//
//		TranslationEditJPanel transName = new TranslationEditJPanel(
//				"name of the atlas:", atlasConfig.getTitle(), // i8nAC
//				atlasConfig.getLanguages());
//		TranslationEditJPanel transDesc = new TranslationEditJPanel(
//				"desciption of the atlas:", atlasConfig.getDesc(), // i8nAC
//				atlasConfig.getLanguages());
//		TranslationEditJPanel transCreator = new TranslationEditJPanel(
//				"creator or vendor or responsible institution:", atlasConfig
//						.getCreator(), // i8nAC
//				atlasConfig.getLanguages());
//		TranslationEditJPanel transCopyright = new TranslationEditJPanel(
//				"copyright note:", atlasConfig.getCopyright(), // i8nAC
//				atlasConfig.getLanguages());
//
//		TranslationEditJPanel.ask(this, transName, transDesc, transCreator,
//				transCopyright);
//
//		return true;
//	}
//
//	protected JTextField getAtlasversionTextfield() {
//		if (atlasVersionTextfield == null) {
//			atlasVersionTextfield = new JTextField(5);
//
//			Float atlasversion = atlasConfig.getAtlasversion();
//			if (atlasversion != null) {
//				atlasVersionTextfield.setText(String.valueOf(atlasversion));
//			}
//		}
//		return atlasVersionTextfield;
//	}
//
//	private JPanel getAbfragenPanel() {
//		JPanel abfragenPanel2 = new JPanel(new SpringLayout());
//
//		JLabel atlasversionLabel = new JLabel("Version (z.B. 1.0)"); // i8nAC
//		atlasversionLabel.setLabelFor(getAtlasversionTextfield());
//		abfragenPanel2.add(atlasversionLabel);
//		abfragenPanel2.add(getAtlasversionTextfield());
//
//		JLabel sprachenLabel = new JLabel("Sprachen des Atlas"); // i8nAC
//		sprachenLabel.setLabelFor(getSprachenTextfield());
//		abfragenPanel2.add(sprachenLabel);
//		abfragenPanel2.add(getSprachenTextfield());
//
//		// Lay out the panel.
//		SpringUtilities.makeCompactGrid(abfragenPanel2, 2, 2, // rows, cols
//				6, 6, // initX, initY
//				6, 6); // xPad, yPad
//
//		return abfragenPanel2;
//	}
//
//	private JTextField getSprachenTextfield() {
//		if (sprachenTextfield == null) {
//			sprachenTextfield = new JTextField(10);
//
//			String codeList = "";
//			for (String code : atlasConfig.getLanguages()) {
//				codeList += code + ",";
//			}
//			// Remove last comma
//			if (codeList.lastIndexOf(",") > 0) {
//				codeList = codeList.substring(0, codeList.length() - 1);
//			}
//
//			LOGGER.debug(" Editing the languagecodes : " + codeList);
//			sprachenTextfield.setText(codeList);
//			// i8nAC
//			sprachenTextfield
//					.setToolTipText("Kommagetrennte Codes, z.B. de,en");
//		}
//		return sprachenTextfield;
//	}
//
//	/**
//	 * This is actually working on the Translations... so successful = false is
//	 * not working for the translations yet!
//	 */
//	public final boolean isSuccessful() {
//		return successful;
//	}
//
//}
