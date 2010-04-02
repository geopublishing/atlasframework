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
package org.geopublishing.geopublisher.gui.map;

import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.ExportableLayer;
import org.geopublishing.atlasViewer.dp.DataPool.EventTypes;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.dp.layer.LayerStyle;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.resource.icons.Icons;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.AtlasMapLayerLegend;
import org.geopublishing.atlasViewer.swing.MapLayerLegend;
import org.geopublishing.atlasViewer.swing.plaf.BasicMapLayerLegendPaneUI;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.AtlasCreator;
import org.geopublishing.geopublisher.gui.DesignAtlasStylerDialog;
import org.geopublishing.geopublisher.gui.SimplyHTMLUtil;
import org.geopublishing.geopublisher.gui.datapool.DataPoolJTable;
import org.geopublishing.geopublisher.swing.GpSwingUtil;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;
import org.geotools.swing.utils.MapLayerUtils;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.gui.FeatureLayerFilterDialog;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.swing.SwingUtil;
import skrueger.creator.GPDialogManager;
import skrueger.geotools.MapView;
import skrueger.geotools.StyledLayerUtil;
import skrueger.geotools.StyledRasterInterface;
import skrueger.i8n.I8NUtil;

/**
 * This extension of the basic {@link MapLayerLegend} introduces menu items and
 * behaviour which are only available in the {@link AtlasCreator} context.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 */
public class DesignAtlasMapLayerLegend extends AtlasMapLayerLegend {

	protected Logger LOGGER = Logger.getLogger(DesignAtlasMapLayerLegend.class);

	protected AtlasConfigEditable ace;

	protected final DesignAtlasMapLegend mapLegend;

	private Color backup;

	/**
	 * Returns <code>null</code> or a tooltip for the legend. The tool-tip does
	 * not start/end with a <code>html</code> tag. That is added later.
	 */
	@Override
	public String getLegendTooltip() {
		if (dpLayer == null)
			return null;

		String tt = super.getLegendTooltip();

		String newtt = "";

		if (dpLayer.isBroken()) {
			newtt += "ERROR: "
					+ dpLayer.getBrokenException().getLocalizedMessage();
		}

		if (!hasVisibleAttributes() && MapLayerUtils.isGridLayer(getMapLayer())) {
			newtt += "<b>&lowast;</b> <b>"
					+ AtlasCreator
							.R("DesignAtlasMapLayer.TT.layerHasNoVisibleAttribute")
					+ "</b><br/>";
		}

		if (isHiddenInLegend()) {
			newtt += "<b>&lowast;</b> "
					+ AtlasCreator
							.R("DesignAtlasMapLayer.TT.layerWillbeHiddenInAtlasLegend")
					+ "<br/>";
		}

		if (map.isSelectableFor(dpLayer.getId())) {
			newtt += "<font color='green'><b>&lowast;</b> "
					+ AtlasCreator.R("DesignAtlasMapLayer.TT.layerSelectable")
					+ "</font><br/>";
		} else {
			newtt += "<font color='red'><b>&lowast;</b> "
					+ AtlasCreator
							.R("DesignAtlasMapLayer.TT.layerNotSelectable")
					+ "</font><br/>";
		}

		return newtt + "<hr/>" + tt;
	}

	/**
	 * This extension of the basic {@link MapLayerLegend} introduces menu items
	 * and behavior which are only available in Geopublisher.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * @param map
	 *            Map that this
	 */
	public DesignAtlasMapLayerLegend(final MapLayer mapLayer,
			ExportableLayer exportable, DesignAtlasMapLegend mapLegend_,
			DpLayer<?, ChartStyle> dpLayer, Map map) {
		super(mapLayer, exportable, mapLegend_, dpLayer, map);

		this.mapLegend = mapLegend_;

		// this.atlasLayerPanel = mapLegend_;

		this.ace = mapLegend.getAce();

		updateHiddenInLegendAppearance();

		addMouseListener(listenToClicksAndSelectinDatapool);

	}

	/**
	 * This MouseListener selects the clicked layers in the
	 * {@link DataPoolJTable}.
	 */
	final MouseAdapter listenToClicksAndSelectinDatapool = new MouseAdapter() {

		@Override
		public void mouseClicked(MouseEvent e) {
			final DataPoolJTable dpJTable = AtlasCreator.getInstance()
					.getJFrame().getDatapoolJTable();

			dpJTable.select(getMapLayer().getTitle());
		}

	};

	/**
	 * If this layer would be hidden in a normal {@link AtlasMapLayerLegend}, we
	 * give it a gray backgroung;
	 * 
	 * @param id
	 */
	private void updateHiddenInLegendAppearance() {
		if (backup == null)
			backup = getForeground();
		if (map.getHideInLegendFor(dpLayer.getId())) {
			setForeground(Color.white.darker());
		} else {
			setForeground(backup);
		}

	}

	/**
	 * @return <code>true</code> if the Datatable for this layer may be viewed
	 *         from within the AV.
	 */
	@Override
	public boolean isTableViewable() {
		return super.isTableViewable() && dpLayer.isTableVisibleInLegend();
	}

	/**
	 * @return <code>true</code> if the {@link Style} is possible to be edited
	 *         with the AS from the legend's tool menu.
	 */
	@Override
	public boolean isStyleEditable() {
		final boolean rasterStylable = styledLayer instanceof StyledRasterInterface
				&& StyledLayerUtil
						.isStyleable((StyledRasterInterface<?>) styledLayer);
		final boolean featureStylable = FeatureUtil
				.getWrappedGeoObject((FeatureSource<SimpleFeatureType, SimpleFeature>) getMapLayer()
						.getFeatureSource()) instanceof FeatureCollection;

		// LOGGER.debug("rasterStylable = " + rasterStylable);
		return featureStylable || rasterStylable;
	}

	/**
	 * @return <code>true</code> if the {@link MapLayer} is possible to be
	 *         filtered with {@link FeatureLayerFilterDialog}
	 */
	@Override
	public boolean isFilterable() {
		boolean hasFeatures = FeatureUtil
				.getWrappedGeoObject((FeatureSource<SimpleFeatureType, SimpleFeature>) getMapLayer()
						.getFeatureSource()) instanceof FeatureCollection;

		boolean hasVisibleAttributes = hasVisibleAttributes();

		return hasFeatures && hasVisibleAttributes;

		// return JMapPane.getLayerSourceObject(mapLayer) instanceof
		// FeatureCollection;
	}

	@Override
	public JPopupMenu getToolMenu() {
		JPopupMenu menu = super.getToolMenu();

		menu.addSeparator();

		/**
		 * Edit DPE
		 */
		menu.add(new AbstractAction(AtlasCreator
				.R("DataPoolWindow_Action_EditDPE_label"),
				BasicMapLayerLegendPaneUI.ICON_TOOL) {

			@Override
			public void actionPerformed(ActionEvent e) {
				GPDialogManager.dm_EditDpEntry.getInstanceFor(dpLayer,
						mapLegend, styledLayer);
			}

		});

		/*
		 * Edit HTML ..
		 */
		menu.add(new AbstractAction(AtlasCreator
				.R("DataPoolWindow_Action_EditDPEHTML_label")) {

			@Override
			public void actionPerformed(ActionEvent e) {

				Window owner = SwingUtil
						.getParentWindow(DesignAtlasMapLayerLegend.this);

				List<File> infoFiles = GpSwingUtil.getHTMLFilesFor(dpLayer);

				ArrayList<String> tabTitles = new ArrayList<String>();

				for (String l : ace.getLanguages()) {
					tabTitles.add(AtlasCreator.R("EditLayerHTML.Tabs.Titles",
							I8NUtil.getLocaleFor(l).getDisplayLanguage()));
				}

				SimplyHTMLUtil.openHTMLEditors(owner, ace, infoFiles,
						tabTitles, AtlasCreator.R("EditLayerHTML.Dialog.Title",
								dpLayer.getTitle().toString()));

				/**
				 * Try to update a few cached values for the TODO nicer!
				 */
				dpLayer.uncache();
				ace.getDataPool().fireChangeEvents(EventTypes.changeDpe);
			}

		});

		/*
		 * Delete all HTML files...
		 */
		final int countExisting = map.getAc().getLanguages().size()
				- dpLayer.getMissingHTMLLanguages().size();
		if (countExisting > 0) {
			menu.add(new AbstractAction(AtlasCreator.R(
					"DataPoolWindow_Action_DeleteAllDPEHTML_label",
					countExisting), BasicMapLayerLegendPaneUI.ICON_REMOVE) {

				@Override
				public void actionPerformed(ActionEvent e) {
					Window owner = SwingUtil
							.getParentWindow(DesignAtlasMapLayerLegend.this);

					AVSwingUtil.askYesNo(owner, AtlasCreator.R(
							"DataPoolWindow_Action_DeleteAllDPEHTML_Question",
							countExisting));

					List<File> infoFiles = GpSwingUtil.getHTMLFilesFor(dpLayer);

					// TODO Delete image directory? NO!
					for (File f : infoFiles) {
						f.delete();
					}

					/**
					 * Try to update a few cached values for .. TODO nicer!
					 */
					dpLayer.uncache();
					ace.getDataPool().fireChangeEvents(EventTypes.changeDpe);
				}

			});
		}

		/*
		 * The menu items to manage available styles and available charts are
		 * valid on DpLayerVectorFeatureSource
		 */
		if (dpLayer instanceof DpLayerVectorFeatureSource) {
			final DpLayerVectorFeatureSource dplv = (DpLayerVectorFeatureSource) dpLayer;

			menu.addSeparator();

			/*
			 * Menuitem: Manage Available Styles for this layer in this map
			 */
			JMenuItem itemManageViews = new JMenuItem();
			itemManageViews.setText(AtlasCreator
					.R("DataPoolWindow_Action_ManageLayerStyles_label"));
			itemManageViews.setToolTipText(AtlasCreator
					.R("DataPoolWindow_Action_ManageLayerStyles_tt"));
			itemManageViews.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					getManageLayerStylesForMapDialog().setVisible(true);
					// atlasMapLegend.updateLegendIcon(styledLayer);
					atlasMapLegend.recreateLayerList(getMapLayer().getTitle());
				}

				private JDialog getManageLayerStylesForMapDialog() {

					Style styleUsedRightNow = getMapLayer().getStyle();

					ManageLayerStylesForMapDialog dialog = new ManageLayerStylesForMapDialog(
							DesignAtlasMapLayerLegend.this, dplv, ace,
							atlasMapLegend.getMap(), styleUsedRightNow,
							mapLegend);
					SwingUtil.centerFrameOnScreenRandom(dialog);
					return dialog;
				}

			});
			menu.add(itemManageViews);

			/**
			 * Menu item: Manage available Charts for this layer in this map
			 */
			JMenuItem itemManageCharts = new JMenuItem(Icons.ICON_CHART_SMALL);
			itemManageCharts.setText(AtlasCreator
					.R("DataPoolWindow_Action_ManageCharts_label"));
			itemManageCharts.setToolTipText(AtlasCreator
					.R("DataPoolWindow_Action_ManageCharts_tt"));
			itemManageCharts.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					GPDialogManager.dm_ManageCharts.getInstanceFor(dplv,
							DesignAtlasMapLayerLegend.this, dplv, mapLegend);

				}

			});
			itemManageCharts.setEnabled(hasVisibleAttributes());
			menu.add(itemManageCharts);

		}

		/*
		 * A separator to all the on/off checkboxes
		 */
		menu.addSeparator();

		/*
		 * Add a new MenuItem, to switch "hideInLegend" on/off
		 */
		JCheckBoxMenuItem hideOnOff = new JCheckBoxMenuItem(new AbstractAction(
				AtlasCreator.R("LayerToolMenu.hideInLegend")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem checkBoxMenuItem = (JCheckBoxMenuItem) e
						.getSource();
				getMap().getHideInLegendMap().put(dpLayer.getId(),
						checkBoxMenuItem.isSelected());

				updateHiddenInLegendAppearance();
			}

		});
		Boolean hideThis = getMap().getHideInLegendMap().get(dpLayer.getId());
		hideOnOff.setSelected(getMap().getHideInLegendMap()
				.get(dpLayer.getId()) == null ? false : hideThis);
		menu.add(hideOnOff);

		/*
		 * Add a new MenuItem, to switch "showTableInLegend" on/off
		 */
		JCheckBoxMenuItem showTableInLegendOnOff = new JCheckBoxMenuItem(
				new AbstractAction(AtlasCreator
						.R("LayerToolMenu.showTableInLegend")) {

					@Override
					public void actionPerformed(ActionEvent e) {
						JCheckBoxMenuItem checkBoxMenuItem = (JCheckBoxMenuItem) e
								.getSource();
						dpLayer.setTableInLegend(checkBoxMenuItem.isSelected()
								&& hasVisibleAttributes());
					}

				});
		showTableInLegendOnOff.setSelected(dpLayer.isTableVisibleInLegend());
		showTableInLegendOnOff.setEnabled(hasVisibleAttributes());
		menu.add(showTableInLegendOnOff);

		/*
		 * Add a new MenuItem, to switch "showFilterInLegend" on/off
		 */
		JCheckBoxMenuItem showFilterInLegendOnOff = new JCheckBoxMenuItem(
				new AbstractAction(AtlasCreator
						.R("LayerToolMenu.showFilterInLegend")) {

					@Override
					public void actionPerformed(ActionEvent e) {
						JCheckBoxMenuItem checkBoxMenuItem = (JCheckBoxMenuItem) e
								.getSource();
						dpLayer.setFilterInLegend(checkBoxMenuItem.isSelected()
								&& hasVisibleAttributes());
					}

				});

		showFilterInLegendOnOff.setSelected(dpLayer.isFilterInLegend());
		showFilterInLegendOnOff.setEnabled(hasVisibleAttributes());
		menu.add(showFilterInLegendOnOff);

		/**
		 * Add a new MenuItem, to switch "showStylerInLegend" on/off
		 */
		JCheckBoxMenuItem showStylerInLegendOnOff = new JCheckBoxMenuItem(
				new AbstractAction(AtlasCreator
						.R("LayerToolMenu.showStylerInLegend")) {

					@Override
					public void actionPerformed(ActionEvent e) {
						JCheckBoxMenuItem checkBoxMenuItem = (JCheckBoxMenuItem) e
								.getSource();
						dpLayer
								.setStylerInLegend(checkBoxMenuItem
										.isSelected());
					}

				});
		showStylerInLegendOnOff.setSelected(dpLayer.isStylerInLegend());
		menu.add(showStylerInLegendOnOff);

		/**
		 * Add a new MenuItem, to switch "anklickbar" on/off
		 */
		JCheckBoxMenuItem layerSelectable = new JCheckBoxMenuItem(
				new AbstractAction(AtlasCreator.R("LayerToolMenu.selectable")) {

					@Override
					public void actionPerformed(ActionEvent e) {
						JCheckBoxMenuItem checkBoxMenuItem = (JCheckBoxMenuItem) e
								.getSource();
						map.setSelectableFor(styledLayer.getId(),
								checkBoxMenuItem.isSelected());

						mapLegend.getGeoMapPane().getMapPane()
								.setMapLayerSelectable(
										getMapLayer(),
										checkBoxMenuItem.isSelected()
												&& hasVisibleAttributes());
					}

				});
		layerSelectable.setIcon(new ImageIcon(MapView.class
				.getResource("resource/icons/info.png")));
		layerSelectable.setToolTipText(AtlasCreator
				.R("LayerToolMenu.selectable.TT"));

		layerSelectable.setSelected(map.isSelectableFor(styledLayer.getId()));
		layerSelectable.setEnabled(hasVisibleAttributes());

		menu.add(layerSelectable);

		return menu;
	}

	@Override
	public DesignAtlasStylerDialog openStylerDialog() {

		LayerStyle selectedStyle = map.getSelectedStyle(dpLayer.getId());
		Object key = selectedStyle == null ? dpLayer : selectedStyle;
		return GPDialogManager.dm_DesignAtlasStyler.getInstanceFor(key,
				mapLegend, dpLayer, mapLegend, getMapLayer(), selectedStyle);
	}

	/**
	 * Change the visibility of the associated {@link MapLayer} (on/off). This
	 * overwritten method also stores the information in the {@link Map}
	 */
	@Override
	public boolean toggleVisibility() {
		boolean result = super.toggleVisibility();

		map.setHiddenFor(dpLayer.getId(), !result);
		Boolean hiddenFor = map.getHiddenFor(dpLayer.getId());
		LOGGER.debug(dpLayer.getId() + " hidden flag is set to " + hiddenFor);

		return result;
	}

}
