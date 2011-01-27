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
package org.geopublishing.geopublisher.gui.datapool;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.dp.DataPool;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpEntryType;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.gui.DpEntryTypeTableCellRenderer;
import org.geopublishing.geopublisher.gui.MegaByteTableCellRenderer;
import org.geopublishing.geopublisher.gui.QualityPercentageTableCellRenderer;
import org.geopublishing.geopublisher.gui.TableModelWithToolTooltip;
import org.geopublishing.geopublisher.gui.TableRowHeightAdjustment;
import org.geopublishing.geopublisher.gui.internal.DefaultTableCellRendererWithTooltip;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import de.schmitzm.i18n.Translation;
import de.schmitzm.jfree.chart.style.ChartStyle;
import de.schmitzm.swing.SwingUtil;

/**
 * This {@link JTable} displays the content of the {@link DataPool}. It can be
 * sorted by clicking the columns.
 * 
 * @author SK
 * 
 */
public class DataPoolJTable extends JTable {
	static final private Logger LOGGER = Logger.getLogger(DataPoolJTable.class);

	private final org.geopublishing.atlasViewer.dp.DataPool dataPool;
	private final AtlasConfigEditable ace;

	private final PropertyChangeListener listenToDPChangesAndUpdateTheModel;

	public DataPoolJTable(final AtlasConfigEditable ace) {
		this.ace = ace;
		this.dataPool = ace.getDataPool();

		setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);

		init();

		/**
		 * Adds a listener to update the TableModel whenever the DataPool has
		 * changed. Backups and restores the selected row.
		 */
		listenToDPChangesAndUpdateTheModel = new PropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				final int backup = getSelectedRow();
				final AbstractTableModel m = (AbstractTableModel) getModel();

				if (evt.getPropertyName().equals(
						DataPool.EventTypes.changeDpe.toString())) {
					m.fireTableDataChanged();
				} else {
					m.fireTableStructureChanged();
				}

				getSelectionModel().addSelectionInterval(backup, backup);

				setTableCellRenderers();
			}

		};
		dataPool.addChangeListener(listenToDPChangesAndUpdateTheModel);
	}

	public class DataPoolTableModel extends AbstractTableModel implements
			TableModelWithToolTooltip {

		/**
		 * Returns the column name.
		 */
		@Override
		public String getColumnName(final int column) {
			/*
			 * DataPoolJTable.ColumnName.Quality=Quality
			 * DataPoolJTable.ColumnName.Type=Type
			 * DataPoolJTable.ColumnName.TitleLang=Title (${0})
			 * DataPoolJTable.ColumnName.ViewsLang=Views (${0})
			 * DataPoolJTable.ColumnName.Filename=Filename
			 * sizeOnFilesystemWithoutSVN=Size (MB)
			 */
			if (column == 0) {
				return GeopublisherGUI.R("DataPoolJTable.ColumnName.Quality");
			} else if (column == 1) {
				return GeopublisherGUI.R("DataPoolJTable.ColumnName.Type");
			} else if (column == 2) {
				return GeopublisherGUI.R("DataPoolJTable.ColumnName.TitleLang",
						Translation.getActiveLang());
			} else if (column == 3) {
				return GeopublisherGUI.R("DataPoolJTable.ColumnName.ViewsLang",
						Translation.getActiveLang());
			} else if (column == 4) {
				return GeopublisherGUI.R("DataPoolJTable.ColumnName.Filename");
			} else if (column == 5) {
				return GeopublisherGUI.R("CRS");
			} else if (column == 6) {
				return GeopublisherGUI.R("sizeOnFilesystemWithoutSVN");
			}
			return super.getColumnName(column);
		}

		@Override
		public int getColumnCount() {
			return 7;
		}

		@Override
		public int getRowCount() {
			return getDataPool().size();
		}

		/**
		 * For correct sorting of the columns
		 */
		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			if (columnIndex == 1) {
				return DpEntryType.class;
			} else if (columnIndex == 6)
				return Double.class;
			return super.getColumnClass(columnIndex);
		}

		@Override
		public Object getValueAt(final int rowIndex, final int column) {
			final DpEntry<? extends ChartStyle> dpe = getDataPool().get(
					rowIndex);

			if (column == 0) {
				return dpe.getQuality();
			} else if (column == 1) {
				return dpe.getType();
			} else if (column == 2) {
				return dpe.getTitle().toString();
			} else if (column == 3) {
				if (dpe instanceof DpLayer) {
					final DpLayer<?, ? extends ChartStyle> dpl = (DpLayer<?, ? extends ChartStyle>) dpe;
					return dpl.getLayerStyles();
				} else
					return null;
			} else if (column == 4) {
				final String filename = dpe.getFilename();
				return FilenameUtils.getBaseName(filename);
			} else if (column == 5) {
				if (dpe instanceof DpLayer) {
					final DpLayer<?, ? extends ChartStyle> dpl = (DpLayer<?, ? extends ChartStyle>) dpe;
					return dpl.getCRSString();
				} else
					return null;
			} else if (column == 6) {
				return getAce().getFolderSize(dpe);
			}

			return dpe.getTitle().toString();
		}

		/**
		 * Return a tooltip String for the given cell at row/col.
		 * 
		 * @return <code>null</code> or {@link String}
		 */
		@Override
		public String getToolTipFor(int rowIndex, int columnIdex) {

			rowIndex = convertRowIndexToModel(rowIndex);
			columnIdex = convertColumnIndexToModel(columnIdex);

			final DpEntry<? extends ChartStyle> dpe = getDataPool().get(
					rowIndex);

			// System.out.println("\n\ndpe = "+dpe.getTitle().toString()+" roxModel "+convertRowIndexToModel(rowIndex));

			if (columnIdex == 0) {
				/**
				 * Create a nice ToolTip for the QM of that DpEntry
				 */

				final List<String> langs = getAce().getLanguages();
				boolean somethingMissing = false;
				final StringBuffer tooltTipHtml = new StringBuffer("<html><b>"
						+ GeopublisherGUI.R(
								"MapPoolJTable.ColumnName.Quality.Tooltip",
								NumberFormat.getPercentInstance().format(
										getValueAt(rowIndex, columnIdex)))
						+ "</B><ul>");

				/**
				 * Check for broken
				 */
				if (dpe.isBroken()) {
					somethingMissing = true;
					tooltTipHtml.append("<li><font color='red'><b>ERROR: "
							+ dpe.getBrokenException().getLocalizedMessage());
					tooltTipHtml.append("</b></font></li>");
				}

				/**
				 * Check the Title translations:
				 */
				List<String> missing = AVUtil.getMissingLanguages(getAce(),
						dpe.getTitle());
				if (missing.size() > 0) {
					somethingMissing = true;
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
				missing = AVUtil.getMissingLanguages(getAce(), dpe.getDesc());
				if (missing.size() > 0) {
					somethingMissing = true;

					tooltTipHtml.append("<li>"
							+ GeopublisherGUI.R("DataPool_Description"));
					if (langs.size() > 1) {
						tooltTipHtml.append(": " + missing.toString());
					}
					tooltTipHtml.append("</li>");
				}

				/**
				 * Check the Keyword translations:
				 */
				missing = AVUtil.getMissingLanguages(getAce(),
						dpe.getKeywords());
				if (missing.size() > 0) {
					somethingMissing = true;

					tooltTipHtml.append("<li>"
							+ GeopublisherGUI.R("DataPool_Keywords"));
					if (langs.size() > 1) {
						tooltTipHtml.append(": " + missing.toString());
					}
					tooltTipHtml.append("</li>");
				}

				if (dpe instanceof DpLayer<?, ?>) {
					/**
					 * Layers additionally have HTML files that can be missing
					 */
					final DpLayer<?, ? extends ChartStyle> dpl = (DpLayer<?, ? extends ChartStyle>) dpe;
					missing = dpl.getMissingHTMLLanguages();
					if (missing.size() > 0) {
						somethingMissing = true;

						tooltTipHtml.append("<li>"
								+ GeopublisherGUI.R("DataPool_HTML"));
						if (langs.size() > 1) {
							tooltTipHtml.append(": " + missing.toString());
						}
						tooltTipHtml.append("</li>");
					}

					/**
					 * QM of the LayerStyles could be bad
					 */
					final double lsQM = dpl.getQualityLayerStyles();
					if (lsQM < 1.) {
						somethingMissing = true;

						tooltTipHtml.append("<li>"
								+ GeopublisherGUI.R("DataPool_LayerStylesQM",
										dpl.getLayerStyles().size(),
										NumberFormat.getPercentInstance()
												.format(lsQM)));
						tooltTipHtml.append("</li>");
					}

				}

				if (dpe instanceof DpLayerVectorFeatureSource) {
					/**
					 * Vector Layers additionally have a Table with Columns
					 * attached which adds the aspect of quality for column
					 * translations.
					 */
					final DpLayerVectorFeatureSource dplv = (DpLayerVectorFeatureSource) dpe;
					// int visibleAttributesCount =
					// dplv.getVisibleAttributesCount();

					final int visibleAttributesCount = dplv
							.getAttributeMetaDataMap()
							.sortedValuesVisibleOnly().size();
					if (dplv.getQuality() < 1. && visibleAttributesCount > 0) {
						double quality = dplv
								.getAttributeMetaDataMap()
								.getQuality(dpe.getAtlasConfig().getLanguages());
						tooltTipHtml.append("<li>"
								+ GeopublisherGUI.R("DataPool_ColumQM",
										visibleAttributesCount,
										NumberFormat.getPercentInstance()
												.format(quality)));
						tooltTipHtml.append("</li>");
					}
				}

				tooltTipHtml.append("</ul></html>");
				if (somethingMissing)
					return tooltTipHtml.toString();
			}
			return null;
		}

	}

	private void init() {
		final TableModel model = new DataPoolTableModel();
		setModel(model);

		/**
		 * Use Java 1.6 to make the columns sortable:
		 */
		final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(
				getModel());
		setRowSorter(sorter);

//		sorter.setComparator(1, DpEntryType.getComparatorForDpe());

		/**
		 * Define selection behavior
		 */
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// ****************************************************************************
		// MouseListener to create a JPopupMenu
		// ****************************************************************************
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent evt) {

				/**
				 * If the lines under the mouse is not selected, select it
				 * first...
				 */
				if ((SwingUtilities.isRightMouseButton(evt) && (evt
						.getClickCount() == 1))
						|| (SwingUtilities.isLeftMouseButton(evt) && evt
								.getClickCount() == 2)) {

					if ((getSelectedRowCount() != 1 || rowAtPoint(evt
							.getPoint()) != getSelectedRow())) {
						getSelectionModel().clearSelection();
						getSelectionModel().addSelectionInterval(
								rowAtPoint(evt.getPoint()),
								rowAtPoint(evt.getPoint()));
					}

					// if right mouse button clicked (or me.isPopupTrigger())
					if (getSelectedRowCount() == 1
							&& rowAtPoint(evt.getPoint()) == getSelectedRow()) {

						/**
						 * reate a PopupMenu depending on the selected DpE
						 */

						new DataPoolJPopupMenu(DataPoolJTable.this).show(
								DataPoolJTable.this, evt.getX(), evt.getY());
					}
				}

			}
		});

		// React to Locale changes: the Table columns have to change their
		// labels when the
		// Locale has been changed
		Translation.addLocaleChangeListener(localeChangeListener);

		setTableCellRenderers();

	}

	/**
	 * Change the table columns when the Locale has changed
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

	void setTableCellRenderers() {
		/**
		 * Define a cell renderer for the folder SIZE in MB column.
		 */
		final TableColumnModel tcm = getColumnModel();
		tcm.setColumnSelectionAllowed(false);

		final DefaultTableCellRenderer defaultCellRenderer = new DefaultTableCellRendererWithTooltip();
		defaultCellRenderer.setVerticalAlignment(SwingConstants.TOP);
		setDefaultRenderer(Object.class, defaultCellRenderer);
		/**
		 * Define a cell renderer for the folder Quality in % in column.
		 */
		SwingUtil.setColumnLook(this, 0,
				new QualityPercentageTableCellRenderer(getAce()),
				QualityPercentageTableCellRenderer.MINWIDTH, null,
				QualityPercentageTableCellRenderer.MAXWIDTH);
		SwingUtil.setColumnLook(this, 1, new DpEntryTypeTableCellRenderer(),
				DpEntryTypeTableCellRenderer.MINWIDTH, null,
				DpEntryTypeTableCellRenderer.MAXWIDTH);

		SwingUtil.setColumnLook(this, 2, null, 30, 110, null);

		SwingUtil.setColumnLook(this, 3, new LayerViewsTableCellRenderer(), 0,
				100, null);

		SwingUtil.setColumnLook(this, 4, null, 0, 70, null);

		SwingUtil.setColumnLook(this, 5, null, 0, 70, null);

		SwingUtil.setColumnLook(this, 6, new MegaByteTableCellRenderer(), null,
				null, MegaByteTableCellRenderer.MAXWIDTH);

		setAutoResizeMode(AUTO_RESIZE_NEXT_COLUMN);
		new TableRowHeightAdjustment().packRows(this, 4);
	}

	public DataPool getDataPool() {
		return dataPool;
	}

	public AtlasConfigEditable getAce() {
		return ace;
	}

	/**
	 * Searches the datapool for the given ID and select the row if found. Also
	 * scrolls the row into the visible area of the {@link JScrollPane}.
	 * 
	 * @param layerId
	 *            The ID of the DPE to select and zoom to
	 */
	public void select(final String layerId) {
		/*
		 * We now have to determine which index the clicked layer has in the
		 * sorted table model
		 */
		int foundAt = -1;
		for (Integer idx = 0; idx < getDataPool().size(); idx++) {
			if (getDataPool().get(idx).getId().equals(layerId)) {
				foundAt = idx;
				break;
			}
		}

		if (foundAt == -1) {
			LOGGER.warn("Could not select a DPE with ID " + layerId);
			return;
		}

		// idx now contains the position in the table model
		final int viewIndex = convertRowIndexToView(foundAt);

		if (getSelectedRow() != viewIndex) {
			getSelectionModel().clearSelection();
			getSelectionModel().addSelectionInterval(viewIndex, viewIndex);

			/** Scrollen */
			scrollRectToVisible(getCellRect(viewIndex, 0, true));
		}

	}

	/**
	 * Help the GC
	 */
	public void dispose() {
		Translation.removeLocaleChangeListener(localeChangeListener);
	}

}
