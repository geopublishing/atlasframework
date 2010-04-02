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
package org.geopublishing.geopublisher;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.swing.AVDialogManager;
import org.geopublishing.geopublisher.gui.datapool.layer.AttribTranslationJTable;

import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import skrueger.AttributeMetadataImpl;
import skrueger.geotools.AttributeMetadataMap;
import skrueger.i8n.Translation;
import skrueger.swing.CancelButton;
import skrueger.swing.CancellableDialogAdapter;
import skrueger.swing.Checkable;
import skrueger.swing.OkButton;

/**
 * This dialog allows to edit title, description and visibility of the columns.
 */
public class EditAttributesJDialog extends CancellableDialogAdapter implements
		Checkable {

	private DpLayerVectorFeatureSource dplv;
	
	private AttributeMetadataMap backupAttributeMetadataMap;

	private AttribTranslationJTable attribTranslationJTable;

	public EditAttributesJDialog(Component owner,
			DpLayerVectorFeatureSource dpLayerVectorFeatureSource) {
		super(SwingUtil.getParentWindow(owner), AtlasCreator.R(
				"Attributes.Edit.Dialog.Title", dpLayerVectorFeatureSource
						.getTitle()));
		dplv = dpLayerVectorFeatureSource;

		backup();

		final JPanel cp = new JPanel(new BorderLayout());
		attribTranslationJTable = new AttribTranslationJTable(
				dplv);
		cp.add(new JScrollPane(attribTranslationJTable), BorderLayout.CENTER);
		
		JPanel buttons = new JPanel(new MigLayout());
		CancelButton cancelB = new CancelButton( );
		cancelB.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelClose();
			}
		});
		OkButton okB = new OkButton( );
		okB.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				okClose();
			}
		});
		buttons.add( okB, "tag ok");
		buttons.add( cancelB, "tag cancel");
		cp.add(buttons, BorderLayout.SOUTH);
		
		cp.add(new JLabel(AtlasCreator.R("EditAttributesDialog.explanation.html")), BorderLayout.NORTH);

		setContentPane(cp);

		pack();
		
		SwingUtil.setRelativeFramePosition(this, owner, SwingUtil.BOUNDS_OUTER,
				SwingUtil.EAST);
	}

	private void backup() {
		backupAttributeMetadataMap = dplv.getAttributeMetaDataMap().copy();

	}

	@Override
	public void cancel() {
		backupAttributeMetadataMap.copyTo(dplv.getAttributeMetaDataMap());
	}

	@Override
	public boolean okClose() {
		if (checkValidInputs()) {
			dispose();
			// Closing all related attribute tables 
			AVDialogManager.dm_AttributeTable.disposeInstanceFor(dplv);
			return true;
		}
		return false;
	}

	@Override
	public boolean checkValidInputs() {
		for (AttributeMetadataImpl amd : dplv.getAttributeMetaDataMap().values()) {

			if (!Translation.checkValid(amd.getTitle())) {
				JOptionPane
						.showMessageDialog(
								this,
								SwingUtil
										.R("TranslationAskJDialog.ErrorMsg.InvalidCharacterInTranslation"));
				return false;
			}

			if (!Translation.checkValid(amd.getDesc())) {
				JOptionPane
						.showMessageDialog(
								this,
								SwingUtil
										.R("TranslationAskJDialog.ErrorMsg.InvalidCharacterInTranslation"));
				return false;
			}
		}
		return true;
	}

	/**
	 * May be called outside to 
	 */
	public void refreshTable() {
		((DefaultTableModel)attribTranslationJTable.getModel()).fireTableDataChanged();
	}

}
