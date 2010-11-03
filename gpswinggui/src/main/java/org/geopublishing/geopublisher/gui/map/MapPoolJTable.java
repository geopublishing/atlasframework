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
package org.geopublishing.geopublisher.gui.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.map.MapPool;
import org.geopublishing.atlasViewer.map.MapPool.EventTypes;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.gui.MegaByteTableCellRenderer;
import org.geopublishing.geopublisher.gui.QualityPercentageTableCellRenderer;
import org.geopublishing.geopublisher.gui.TableModelWithToolTooltip;
import org.geopublishing.geopublisher.gui.internal.DefaultTableCellRendererWithTooltip;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import schmitzm.swing.SwingUtil;
import skrueger.i8n.Translation;

/**
 * This {@link JTable} displays the content of the {@link MapPool}. It can be
 * sorted by clicking the columns.
 * 
 * @author Stefan A. Tzeggai
 * 
 */
public class MapPoolJTable extends JTable {
	static final private Logger LOGGER = Logger.getLogger(MapPoolJTable.class);

	private final MapPool mapPool;
	private final AtlasConfigEditable ace;

	/**
	 * Add a listener to update the TableModel whenever the MapPool has changed.
	 * Backups and restores the selected row. It also resets the cached HTML
	 * info files.
	 */
	final private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {

			if (evt.getPropertyName().equals(EventTypes.changeMap.toString())) {
				((Map) evt.getNewValue()).resetMissingHTMLinfos();
			}

			int backup = getSelectedRow();

			AbstractTableModel m = (AbstractTableModel) getModel();
			m.fireTableDataChanged();

			getSelectionModel().addSelectionInterval(backup, backup);

			setTableCellRenderers();
			setTableCellRenderers();
		}

	};

	public MapPoolJTable(final AtlasConfigEditable ace) {
		this.ace = ace;
		this.mapPool = ace.getMapPool();

		init();

		mapPool.addChangeListener(propertyChangeListener);
	}

	class MapPoolTableModel extends AbstractTableModel implements
			TableModelWithToolTooltip {

		private static final int CRSCOL = 2;

		@Override
		/*
		 * Returns the column name.
		 * 
		 * @return a name for this column using the string value of the
		 * appropriate member in <code>columnIdentifiers</code>. If
		 * <code>columnIdentifiers</code> does not have an entry for this index,
		 * returns the default name provided by the superclass.
		 */
		public String getColumnName(int column) {
			if (column == 0) {
				return GeopublisherGUI.R("MapPoolJTable.ColumnName.Quality");
			} else if (column == 1) {
				return GeopublisherGUI.R("MapPoolJTable.ColumnName.NameLang",
						Translation.getActiveLang());
			} else if (column == CRSCOL) {
				return GeopublisherGUI.R("CRS");
			} else if (column == 3) {
				return GeopublisherGUI.R("sizeOnFilesystemWithoutSVN");
			}
			return super.getColumnName(column);
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public int getRowCount() {
			return getMapPool().size();
		}

		@Override
		/*
		 * For correct sorting ...
		 */
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 3)
				return Double.class;
			return super.getColumnClass(columnIndex);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			

			Map map = getMapPool().get(rowIndex);

			if (columnIndex == 0) {
				return (map.getQuality());
			} else if (columnIndex == 1) {
				return map.getTitle().toString();
			} else if (columnIndex == CRSCOL) {
				return map.getUsedCrs();
			} else if (columnIndex == 3) {
				return getSize(map);
			}
			return getValueAt(rowIndex, columnIndex);
		}

		@Override
		/*
		 * Return a tooltip String for the given cell at row/col.
		 * 
		 * @return <code>null</code> or {@link String}
		 */
		public String getToolTipFor(int rowIndex, int columnIndex) {

			rowIndex = convertRowIndexToModel(rowIndex);
			columnIndex = convertColumnIndexToModel(columnIndex);

			final Map map = getMapPool().get(rowIndex);

			if (columnIndex == 0) {

				if (map.getQuality() == 1.)
					return "Super!";
				/**
				 * Create a nice ToolTip for the QM of that Map
				 */

				final List<String> langs = getAce().getLanguages();
				final StringBuffer tooltTipHtml = new StringBuffer("<html><b>"
						+ GeopublisherGUI.R(
								"MapPoolJTable.ColumnName.Quality.Tooltip",
								NumberFormat.getPercentInstance().format(
										getValueAt(rowIndex, columnIndex)))
						+ "</B><ul>");

				if (map.getLayers().size() > 0) {

					String averageQMformatted = NumberFormat
							.getPercentInstance().format(
									map.getAverageLayerQuality());
					tooltTipHtml.append("<li>"
							+ GeopublisherGUI.R("MapPool_AverageLayerQM",
									averageQMformatted + "</li>"));
				} else {
					tooltTipHtml.append("<li>"
							+ GeopublisherGUI
									.R("MapPool_AverageLayerQM.NoLayers")
							+ "</li>");
				}

				/**
				 * Check the Title translations:
				 */
				List<String> missing = AVUtil.getMissingLanguages(getAce(),
						map.getTitle());
				if (missing.size() > 0) {
					tooltTipHtml.append("<li>"
							+ GeopublisherGUI.R("DataPool_Title"));
					if (langs.size() > 1) {
						tooltTipHtml.append(": " + missing.toString());
					}
					tooltTipHtml.append("</li>");
				}

				/**
				 * Check the Description translations:
				 */
				missing = AVUtil.getMissingLanguages(getAce(), map.getDesc());
				if (missing.size() > 0) {

					tooltTipHtml.append("<li>"
							+ GeopublisherGUI.R("DataPool_Description"));
					if (langs.size() > 1) {
						tooltTipHtml.append(": " + missing.toString());
					}
					tooltTipHtml.append("</li>");
				}

				// /**
				// * Check the Keyword translations:
				// */
				// missing = AVUtil.getMissingLanguages(getAce(), map
				// .getKeywords());
				// if (missing.size() > 0) {
				//
				// tooltTipHtml.append("<li>"
				// + GeopublisherGUI.R("DataPool_Keywords"));
				// if (langs.size() > 1) {
				// tooltTipHtml.append(": " + missing.toString());
				// }
				// tooltTipHtml.append("</li>");
				// }

				/**
				 * Layers additionally have HTML files that can be missing
				 */
				missing = map.getMissingHTMLLanguages();
				if (missing.size() > 0) {

					tooltTipHtml.append("<li>"
							+ GeopublisherGUI.R("DataPool_HTML"));
					if (langs.size() > 1) {
						tooltTipHtml.append(": " + missing.toString());
					}
					tooltTipHtml.append("</li>");
				}

				/**
				 * The max map Extend should be set!
				 */
				if (map.getMaxExtend() == null) {

					tooltTipHtml
							.append("<li>"
									+ GeopublisherGUI
											.R("MapPoolJTable.QualityMissing.MapMaxEntend"));
					tooltTipHtml.append("</li>");
				}

				tooltTipHtml.append("</ul></html>");
				return tooltTipHtml.toString();
			} else if (columnIndex == CRSCOL) {
				List valueAt = (List) getValueAt(rowIndex, columnIndex);
				if (valueAt != null && valueAt.size() > 1) {
					return GeopublisherGUI.R("MapCRS.tooManyWarning");
				} else
					return null;
			}
			return null;
		}

	};

	/**
	 * Define, that the Table columns have to change their labels when the
	 * Locale has been changed
	 */
	final PropertyChangeListener localeChangeListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			// The Locale has changed. Lets update the table columns

			final AbstractTableModel m = (AbstractTableModel) getModel();
			m.fireTableStructureChanged();

			setTableCellRenderers();

			resizeAndRepaint();
		}

	};

	private void init() {

		setModel(new MapPoolTableModel());

		/**
		 * Use Java 1.6 to make the columns sortable:
		 */
		RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(
				getModel());
		setRowSorter(sorter);

		/**
		 * Define selection behavior
		 */
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// ****************************************************************************
		// MouseListener to create a JPopupMenu
		// ****************************************************************************
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {

				/**
				 * If the lines under the mouse is not selected, select it
				 * first...
				 */
				if ((SwingUtilities.isRightMouseButton(me) && (me
						.getClickCount() == 1))) {

					if (getSelectedRowCount() != 1
							|| rowAtPoint(me.getPoint()) != getSelectedRow()) {
						getSelectionModel().clearSelection();
						getSelectionModel().addSelectionInterval(
								rowAtPoint(me.getPoint()),
								rowAtPoint(me.getPoint()));
					}

					// if right mouse button clicked (or me.isPopupTrigger())
					if (getSelectedRowCount() == 1
							&& rowAtPoint(me.getPoint()) == getSelectedRow()) {

						/**
						 * Define a popup Menu
						 */
						new MapPoolJPopupMenu(MapPoolJTable.this).show(
								MapPoolJTable.this, me.getX(), me.getY());
					}
				}
			}
		});

		Translation.addLocaleChangeListener(localeChangeListener);

		setTableCellRenderers();
	}

	protected void setTableCellRenderers() {
		/**
		 * Define an additional cell renderer (for all columns) that will mark
		 * the default map green
		 */
		final DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRendererWithTooltip() {

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {

				Component fromSuper = super.getTableCellRendererComponent(
						table, value, isSelected, hasFocus, row, column);

				/**
				 * If this row is the row of the start Map, we put the Component
				 * into a JPanel with a green border.
				 */
				return putBorderIfStartMap(mapPool, row, fromSuper);
			}

		};
		defaultTableCellRenderer.setVerticalAlignment(SwingConstants.TOP);
		setDefaultRenderer(Object.class, defaultTableCellRenderer);

		/**
		 * Define a cell renderer for the folder size column. This renderer
		 * recycles the default renderer defined above
		 */
		TableColumnModel tcm = getColumnModel();
		tcm.setColumnSelectionAllowed(false);

		SwingUtil.setColumnLook(this, 0,
				new QualityPercentageTableCellRenderer(getAce()),
				QualityPercentageTableCellRenderer.MINWIDTH, null,
				QualityPercentageTableCellRenderer.MAXWIDTH);

		SwingUtil.setColumnLook(this, 2, new MapCRSCellRenderer(), null,
				MapCRSCellRenderer.PREFWIDTH, MapCRSCellRenderer.MAXWIDTH);

		SwingUtil.setColumnLook(this, 3, new MegaByteTableCellRenderer(), null,
				null, MegaByteTableCellRenderer.MAXWIDTH);

		setAutoResizeMode(AUTO_RESIZE_NEXT_COLUMN);
		// new TableRowHeightAdjustment().packRows(this, 4);
	}

	/**
	 * If this row belongs to the default start map, but a green line border
	 * around the rendering component
	 * 
	 * @param mapPool2
	 * @param row
	 * @param component
	 * @return
	 */
	protected Component putBorderIfStartMap(MapPool mapPool2, int row,
			Component component) {

		if (mapPool.getStartMapID() != null
				&& mapPool.getStartMapID().equals(
						mapPool.get(convertRowIndexToModel(row)).getId())) {
			JComponent newComponent = new JPanel(new BorderLayout());
			newComponent.add(component, BorderLayout.CENTER);
			newComponent.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0,
					Color.green));
			return newComponent;
		}

		return component;
	}

	/**
	 * @return the size of all layers used in the map in bytes.
	 * 
	 * @param map
	 *            The map to calculate the size for.
	 */
	protected Double getSize(Map map) {
		Long sum = 0l;
		for (DpRef dpr : map.getLayers()) {

			sum += getAce().getFolderSize(dpr.getTarget());

		}
		return (sum.doubleValue());
	}

	public MapPool getMapPool() {
		return mapPool;
	}

	public AtlasConfigEditable getAce() {
		return ace;
	}

	/**
	 * Searches the datapool for the given ID and select the row if found. Also
	 * scrolls the row into the visible area of the {@link JScrollPane}.
	 * 
	 * @param mapId
	 *            The ID of the DPE to select and zoom to
	 */
	public void select(final String mapId) {
		/*
		 * We now have to determine which index the clicked layer has in the
		 * sorted table model
		 */
		int foundAt = -1;
		for (Integer idx = 0; idx < mapPool.size(); idx++) {
			if (mapPool.get(idx).getId().equals(mapId)) {
				foundAt = idx;
				break;
			}
		}

		if (foundAt == -1) {
			LOGGER.warn("Could not select a map with ID " + mapId);
			return;
		}

		// idx now contains the position in the table model
		final int viewIndex = convertRowIndexToView(foundAt);

		if (getSelectedRow() != viewIndex) {
			selectionModel.clearSelection();
			selectionModel.addSelectionInterval(viewIndex, viewIndex);

			/** Scrollen */
			scrollRectToVisible(getCellRect(viewIndex, 0, true));
		}
	}
}
