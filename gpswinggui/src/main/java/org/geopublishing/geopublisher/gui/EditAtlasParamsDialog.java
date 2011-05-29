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
/** 
 Copyright 2009 Stefan Alfons Tzeggai 

 atlas-framework - This file is part of the Atlas Framework

 This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA

 Diese Bibliothek ist freie Software; Sie dürfen sie unter den Bedingungen der GNU Lesser General Public License, wie von der Free Software Foundation veröffentlicht, weiterverteilen und/oder modifizieren; entweder gemäß Version 2.1 der Lizenz oder (nach Ihrer Option) jeder späteren Version.
 Diese Bibliothek wird in der Hoffnung weiterverbreitet, daß sie nützlich sein wird, jedoch OHNE IRGENDEINE GARANTIE, auch ohne die implizierte Garantie der MARKTREIFE oder der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK. Mehr Details finden Sie in der GNU Lesser General Public License.
 Sie sollten eine Kopie der GNU Lesser General Public License zusammen mit dieser Bibliothek erhalten haben; falls nicht, schreiben Sie an die Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA.
 **/

package org.geopublishing.geopublisher.gui;

import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpUtil;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import de.schmitzm.swing.TranslationAskJDialog;
import de.schmitzm.swing.TranslationEditJPanel;

/**
 * A {@link JDialog} that allows to translate the basic atlas parameters for a
 * given {@link AtlasConfigEditable}. The dialog is model, not visible by
 * default and handle's the cancel button itself.
 */
public class EditAtlasParamsDialog extends TranslationAskJDialog {

	private final AtlasConfigEditable ace;

	public EditAtlasParamsDialog(Component owner, AtlasConfigEditable ace) {
		super(owner, GpUtil.R("AtlasParamsTranslationDialog.DialogTitle"));
		this.ace = ace;

		TranslationEditJPanel transName = new TranslationEditJPanel(
				GeopublisherGUI.R("AtlasParamsTranslationDialog.Title"),
				ace.getTitle(), ace.getLanguages());
		TranslationEditJPanel transDesc = new TranslationEditJPanel(
				GeopublisherGUI.R("AtlasParamsTranslationDialog.Description"),
				ace.getDesc(), ace.getLanguages());
		TranslationEditJPanel transCreator = new TranslationEditJPanel(
				GeopublisherGUI.R("AtlasParamsTranslationDialog.Creator"),
				ace.getCreator(), ace.getLanguages());
		TranslationEditJPanel transCopyright = new TranslationEditJPanel(
				GeopublisherGUI.R("AtlasParamsTranslationDialog.Copyright"),
				ace.getCopyright(), ace.getLanguages());

		setComponents(getBasenamePanel(), transName, transDesc, transCreator,
				transCopyright);

		pack();
	}

	private JPanel abnPanel;
	private JTextField basenameTextfield;

	/**
	 * Asks for the atlasbasename
	 * 
	 * @since 1.7 a requires ASCII id for each atlas
	 */
	private JPanel getBasenamePanel() {
		if (abnPanel == null) {
			abnPanel = new JPanel(new MigLayout("wrap 2"));
			abnPanel.add(new JLabel(GpUtil.R("AtlasBasename.Explanation")),
					"span 2");
			abnPanel.add(new JLabel(GpUtil.R("AtlasBasename")), "");
			abnPanel.add(getBasenameJTextfield(), "growx");
		}
		return abnPanel;
	}

	JTextField getBasenameJTextfield() {
		if (basenameTextfield == null) {
			basenameTextfield = new JTextField(ace.getBaseName());
		}
		return basenameTextfield;
	}

	@Override
	public boolean okClose() {
		if (!AtlasConfigEditable.checkBasename(getBasenameJTextfield()
				.getText())) {
			AVSwingUtil.showMessageDialog(this, "Not a valid atlas name."); // i8n
			return false;
		}
		boolean result = super.okClose();
		if (result) {
			ace.setBaseName(StringUtils.trimToEmpty(getBasenameJTextfield().getText()));
			ace.fireChangeEvents();
		}
		return result;
	}

}
