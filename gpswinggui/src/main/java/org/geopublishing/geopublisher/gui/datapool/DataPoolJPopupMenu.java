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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.GpCoreUtil;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.dp.media.DpMedia;
import org.geopublishing.atlasViewer.swing.AVDialogManager;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import de.schmitzm.jfree.chart.style.ChartStyle;

public class DataPoolJPopupMenu extends JPopupMenu {

	static final Logger LOGGER = Logger.getLogger(DataPoolJPopupMenu.class);

	public DataPoolJPopupMenu(final DataPoolJTable dpTable) {

		final Component owner = dpTable;

		if (dpTable.getSelectedRow() == -1)
			return;
		final DpEntry<?> dpe = dpTable.getDataPool().get(
				dpTable.convertRowIndexToModel(dpTable.getSelectedRow()));

		if (dpe instanceof DpLayer) {
			final DpLayer<?, ? extends ChartStyle> dpLayer = (DpLayer<?, ? extends ChartStyle>) dpe;

			if (dpLayer instanceof DpLayerVectorFeatureSource) {
				final DpLayerVectorFeatureSource dpfs = (DpLayerVectorFeatureSource) dpLayer;
//
//				/*******************************************************************
//				 * Create a new chart using the ChartWizard
//				 */
//				JMenuItem chartWizardJMenuItem = new JMenuItem(
//						new AbstractAction(Geopublisher
//								.R("LayerToolMenu.chartWizard"),
//								Icons.ICON_CHART_SMALL) {
//
//							@Override
//							public void actionPerformed(ActionEvent e) {
//								FeatureChartStyle newChart = ChartWizard
//										.showWizard(dpfs.getFeatureSource(),
//												dpfs.getAttributeMetaDataMap(),
//												dpTable.getAce().getLanguages());
//								if (newChart == null)
//									return;
//
//								/**
//								 * Adding the chart to the DpLayerVector's list
//								 * of ChartStyles
//								 */
//								dpfs.getCharts().add(newChart);
//							}
//
//						});
//				add(chartWizardJMenuItem);

				/*******************************************************************
				 * Show attribute table
				 */
				JMenuItem attributeTableJMenuItem = new JMenuItem(
						new AbstractAction(
								GpCoreUtil.R("LayerToolMenu.table"),
								Icons.ICON_TABLE) {

							@Override
							public void actionPerformed(ActionEvent e) {
								AVDialogManager.dm_AttributeTable
										.getInstanceFor(dpfs, owner, dpfs, null);
							}

						});
				add(attributeTableJMenuItem);
//
//				/***************************************************************
//				 * Translate and change visibility of attributes
//				 */
//				add(new JMenuItem(new DataPoolEditColumnsAction(dpTable, owner)));
//
//				/**
//				 * Add a new MenuItem, to switch "showTableInLegend" on/off
//				 */
//				JCheckBoxMenuItem showTableInLegendOnOff = new JCheckBoxMenuItem(
//						new AbstractAction(Geopublisher
//								.R("LayerToolMenu.showTableInLegend")) {
//
//							@Override
//							public void actionPerformed(ActionEvent e) {
//								JCheckBoxMenuItem checkBoxMenuItem = (JCheckBoxMenuItem) e
//										.getSource();
//								dpLayer.setTableInLegend(checkBoxMenuItem
//										.isSelected());
//							}
//
//						});
//				showTableInLegendOnOff.setSelected(dpLayer
//						.isTableVisibleInLegend());
//				add(showTableInLegendOnOff);

				
				/**
				 * Add a new MenuItem, to switch "showFilterInLegend" on/off
				 */
				JCheckBoxMenuItem showFilterInLegendOnOff = new JCheckBoxMenuItem(
						new AbstractAction(GeopublisherGUI
								.R("LayerToolMenu.showFilterInLegend")) {

							@Override
							public void actionPerformed(ActionEvent e) {
								JCheckBoxMenuItem checkBoxMenuItem = (JCheckBoxMenuItem) e
										.getSource();
								dpLayer.setFilterInLegend(checkBoxMenuItem
										.isSelected());
							}

						});
				showFilterInLegendOnOff.setSelected(dpLayer.isFilterInLegend());
				add(showFilterInLegendOnOff);

				/**
				 * Add a new MenuItem, to switch "showStylerInLegend" on/off
				 */
				JCheckBoxMenuItem showStylerInLegendOnOff = new JCheckBoxMenuItem(
						new AbstractAction(GeopublisherGUI
								.R("LayerToolMenu.showStylerInLegend")) {

							@Override
							public void actionPerformed(ActionEvent e) {
								JCheckBoxMenuItem checkBoxMenuItem = (JCheckBoxMenuItem) e
										.getSource();
								dpLayer.setStylerInLegend(checkBoxMenuItem
										.isSelected());
							}

						});
				showStylerInLegendOnOff.setSelected(dpLayer.isStylerInLegend());
				add(showStylerInLegendOnOff);

			}

			/*******************************************************************
			 * Edit additional Styles menu
			 */
			JMenuItem editAdditionalStyles = new JMenuItem();
			editAdditionalStyles.setText(GeopublisherGUI
					.R("DataPoolWindow_Action_ManageLayerStyles_label"));
			editAdditionalStyles.setToolTipText(GeopublisherGUI
					.R("DataPoolWindow_Action_ManageLayerStyles_tt"));

			editAdditionalStyles.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					new ManageLayerStylesDialog(owner, dpLayer, dpTable
							.getAce()).setVisible(true);
				}

			});
			add(editAdditionalStyles);


			addSeparator();

			add(new DataPoolEditHTMLAction(dpTable));

			/**
			 * Determine the number of HTML files that exist
			 */
			int countExisting = dpTable.getAce().getLanguages().size()
					- dpLayer.getMissingHTMLLanguages().size();
			if (countExisting > 0)
				add(new DataPoolDeleteAllHTMLAction(dpTable, owner,
						countExisting));

			addSeparator();

		} else if (dpe instanceof DpMedia) {
			add(new DataPoolPreviewAction(dpTable, owner));
		}

		add(new DataPoolEditAction(dpTable, owner));
		add(new DataPoolDeleteAction(dpTable, owner));

		addSeparator();

	}

}
