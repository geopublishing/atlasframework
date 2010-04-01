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
package org.geopublishing.geopublisher.gui.datapool;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.swing.AVDialogManager;
import org.geopublishing.atlasViewer.swing.AtlasViewer;
import org.geopublishing.atlasViewer.swing.plaf.BasicMapLayerLegendPaneUI;
import org.geopublishing.geopublisher.AtlasCreator;
import org.geopublishing.geopublisher.EditAttributesJDialog;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.swing.JPanel;
import skrueger.creator.GPDialogManager;
import skrueger.geotools.AttributeMetadataMap;
import skrueger.swing.Cancellable;
import skrueger.swing.SmallButton;

public class DpLayerVectorAttributesJPanel extends JPanel implements
		Cancellable {

	private Charset backupCRS;
	private final DpLayerVectorFeatureSource dplv;
	private Integer atts;
	private Integer numAtts;
	private Integer visNumAtts;
	private Integer visAtts;
	private Integer txtAtts;
	private Integer visTxtAtts;

	final JLabel visTxtAttsLabel = new JLabel();
	final JLabel txtAttsLabel = new JLabel();
	private final JLabel visNumAttsLabel = new JLabel();
	private final JLabel numAttsLabel = new JLabel();
	private final JLabel attsLabel = new JLabel();
	private final JLabel visAttsLabel = new JLabel();

	/**
	 * Update the JLabels which contain the overwiew of visible columns.
	 */
	private void updateAttributeStats() {
		/**
		 * Calculate the overview numbers
		 */
		final SimpleFeatureType schema = dplv.getSchema();

		if (schema == null)
			return;

		atts = ASUtil.getValueFieldNames(schema, false).size();

		final List<String> numericalFieldNames = FeatureUtil.getNumericalFieldNames(
				schema, false);
		numAtts = numericalFieldNames.size();
		visNumAtts = 0;

		for (final String numAttName : numericalFieldNames) {
			if (dplv.getAttributeMetaDataMap().get(numAttName) != null
					&& dplv.getAttributeMetaDataMap().get(numAttName)
							.isVisible())
				visNumAtts++;
		}
		visAtts = dplv.getAttributeMetaDataMap().sortedValuesVisibleOnly()
				.size();

		txtAtts = atts - numAtts;
		visTxtAtts = visAtts - visNumAtts;

		/**
		 * Update the JLabels
		 */
		visAttsLabel.setText(visAtts.toString());
		attsLabel.setText(atts.toString());
		visTxtAttsLabel.setText(visTxtAtts.toString());
		txtAttsLabel.setText(txtAtts.toString());
		visNumAttsLabel.setText(visNumAtts.toString());
		numAttsLabel.setText(numAtts.toString());

		// numAttsLabel.setText(new Random().nextInt()+"sd");
	}

	final WindowAdapter updateStatsWindowListener = new WindowAdapter() {
		@Override
		public void windowClosed(final WindowEvent e) {
			updateAttributeStats();
		}

	};
	private AttributeMetadataMap backupAttributeMetadataMap;

	public DpLayerVectorAttributesJPanel(final DpLayerVectorFeatureSource dplv) {
		super(new MigLayout("width 100%, wrap 1", "[grow]"));
		this.dplv = dplv;

		backup();

		/**
		 * A Panel giving an overview of all available attributes
		 */
		{
			final JPanel attPanel = new JPanel(new MigLayout(
					"width 100%, wrap 3", "[grow][grow][grow]"));
			attPanel.setBorder(BorderFactory
					.createTitledBorder(R("EditDpEntryGUI.attributes.border")));
			add(attPanel, "growx");

			attPanel.add(new JLabel());
			attPanel.add(new JLabel(
					R("DpLayerVectorAttributesJPanel.attOverview.inTable")),
					"center");
			attPanel.add(new JLabel(
					R("DpLayerVectorAttributesJPanel.attOverview.visible")),
					"center");

			attPanel.add(new JLabel(
					R("DpLayerVectorAttributesJPanel.attOverview.totalAtts")
							+ ":"), "right");

			attPanel.add(attsLabel, "center");
			attPanel.add(visAttsLabel, "center");

			attPanel
					.add(
							new JLabel(
									R("DpLayerVectorAttributesJPanel.attOverview.numericalAtts")
											+ ":"), "right");
			attPanel.add(numAttsLabel, "center");
			attPanel.add(visNumAttsLabel, "center");

			attPanel.add(new JLabel(
					R("DpLayerVectorAttributesJPanel.attOverview.textualAtts")
							+ ":"), "right");
			attPanel.add(txtAttsLabel, "center");

			attPanel.add(visTxtAttsLabel, "center,wrap");

			//
			// A JButton to edit the Columns...
			//
			{
				final JButton editColumns = new SmallButton(new AbstractAction(
						R("DataPoolWindow_Action_EditColumns_label")) {

					@Override
					public void actionPerformed(final ActionEvent e) {
						final EditAttributesJDialog d = GPDialogManager.dm_EditAttribute
								.getInstanceFor(dplv,
										DpLayerVectorAttributesJPanel.this,
										dplv);

						if (!Arrays.asList(d.getWindowListeners()).contains(
								updateStatsWindowListener)) {
							d.addWindowListener(updateStatsWindowListener);
						}

					}
				});
				attPanel.add(editColumns, "span 3, split 2, right");
			}

			//
			// A JButton to open the attribute table
			//
			{
				final JButton openTable = new SmallButton(new AbstractAction(
						AtlasViewer.R("LayerToolMenu.table"),
						BasicMapLayerLegendPaneUI.ICON_TABLE) {

					@Override
					public void actionPerformed(final ActionEvent e) {
						AVDialogManager.dm_AttributeTable.getInstanceFor(dplv,
								DpLayerVectorAttributesJPanel.this, dplv, null);
					}
				});
				attPanel.add(openTable, "right");
			}

			updateAttributeStats();

		}

		/**
		 * A Panel to define the charset.
		 */
		{
			final JPanel charsetPanel = new JPanel(new MigLayout(
					"width 100%, wrap 2", "[grow]"));
			charsetPanel.setBorder(BorderFactory
					.createTitledBorder(R("EditDpEntryGUI.charset.border")));

			charsetPanel.add(new JLabel(R("EditDpEntryGUI.charset.explanation",
					dplv.getType().getDesc())),
					"span 2, right, width 100%, growx");

			charsetPanel.setToolTipText(R("EditDpEntryGUI.ChartsetLabel.TT"));
			charsetPanel.add(new JLabel(R("EditDpEntryGUI.ChartsetLabel")),
					"right");

			final SortedMap<String, Charset> availableCharsets = Charset
					.availableCharsets();
			final String[] charsetNames = availableCharsets.keySet().toArray(
					new String[] {});

			final JComboBox charSetJCombobox = new JComboBox(charsetNames);

			charSetJCombobox.setSelectedItem(dplv.getCharset().name());
			charSetJCombobox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.DESELECTED)
						return;

					final String item = (String) e.getItem();

					Charset newCharset = Charset.forName(item);
					
					System.out.println("new charset = "+newCharset);
					
					dplv.setCharset(newCharset);
				}

			});

			/** A renderer that display a human readable form of the charset */
			charSetJCombobox.setRenderer(new DefaultListCellRenderer() {

				@Override
				public Component getListCellRendererComponent(final JList list,
						final Object value, final int index,
						final boolean isSelected, final boolean cellHasFocus) {
					final JLabel proto = (JLabel) super
							.getListCellRendererComponent(list, value, index,
									isSelected, cellHasFocus);
					final Charset cs = Charset.forName((String) value);
					proto.setText(cs.displayName());
					proto.setToolTipText(cs.aliases().toString());
					return proto;
				}

			});

			charsetPanel.add(charSetJCombobox, "left");

			add(charsetPanel, "growx");
		}
	}

	private void backup() {
		backupCRS = dplv.getCharset();

		backupAttributeMetadataMap = dplv.getAttributeMetaDataMap().copy();
	}

	@Override
	public void cancel() {
		dplv.setCharset(backupCRS);

		backupAttributeMetadataMap.copyTo(dplv.getAttributeMetaDataMap());
	}

	protected String R(final String string, final Object... obj) {
		return AtlasCreator.R(string, obj);
	}

}
