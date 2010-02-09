/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
 * http://www.geopublishing.org
 * 
 * Geopublisher is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.creator.gui.datapool.layer;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;

import schmitzm.swing.SwingUtil;
import skrueger.AttributeMetadata;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.atlas.gui.internal.TranslationCellRenderer;
import skrueger.creator.AtlasCreator;
import skrueger.creator.gui.NoDataEditListDialog;
import skrueger.creator.gui.QualityPercentageTableCellRenderer;
import skrueger.creator.gui.TableRowHeightAdjustment;
import skrueger.geotools.AttributeMetadataMap;
import skrueger.i8n.Translation;
import skrueger.swing.TranslationAskJDialog;
import skrueger.swing.TranslationEditJPanel;

/**
 * This {@link JTable} has a model based on an {@link AttributeMetadataMap}
 * 
 */
public class AttribTranslationJTable extends JTable {

	static private final Logger LOGGER = Logger
			.getLogger(AttribTranslationJTable.class);

	protected static final int COLIDX_QUALITY = 0;
	protected static final int COLIDX_WEIGHT = 1;
	protected static final int COLIDX_VIS = 2;
	protected static final int COLIDX_NAME = 3;
	protected static final int COLIDX_TYPE = 4;
	protected static final int COLIDX_UNIT = 5;
	protected static final int COLIDX_NODATA = 6;
	// protected static final int COLIDX_TRANSLATE = 7;
	protected static final int COLIDX_TITLES = 7;
	protected static final int COLIDX_DESCS = 8;

	public static final Font FATFONT = new JLabel().getFont().deriveFont(
			Font.BOLD);

	private final DefaultTableModel model;

	private final DpLayerVectorFeatureSource dplv;

	private final AttributeMetadataMap attMetadataMap;

	public AttribTranslationJTable(final DpLayerVectorFeatureSource dplv_) {

		dplv = dplv_;
		attMetadataMap = dplv.getAttributeMetaDataMap();

		/**
		 * The TableModel for this...
		 */
		model = new DefaultTableModel() {
			@Override
			public int getColumnCount() {
				return 9;
			}

			@Override
			public void fireTableDataChanged() {
				super.fireTableDataChanged();
				// When using multiple languages, the rows do have special
				// heights.. This is adjusted here...
				new TableRowHeightAdjustment().packRows(
						AttribTranslationJTable.this, 0);
			}

			@Override
			public int getRowCount() {
				return attMetadataMap.size();
			}

			@Override
			public void fireTableChanged(TableModelEvent e) {
				super.fireTableChanged(e);
			}

			@Override
			public Object getValueAt(final int row, final int column) {
				AttributeMetadata attMetadata = getAttMetaData(row);

				if (column == COLIDX_QUALITY) {
					return attMetadata;
				} else if (column == COLIDX_WEIGHT) {
					return attMetadata.getWeight();
				} else if (column == COLIDX_VIS) {
					return attMetadata.isVisible();
				} else if (column == COLIDX_NAME) {
					AttributeDescriptor ad = dplv.getSchema().getDescriptor(
							attMetadata.getName());
					return ad.getName();
				} else if (column == COLIDX_TYPE) {
					AttributeDescriptor ad = dplv.getSchema().getDescriptor(
							attMetadata.getName());
					return ad.getType().getBinding().getSimpleName();
				} else if (column == COLIDX_UNIT) {
					return attMetadata.getUnit();
				} else if (column == COLIDX_NODATA) {
					return attMetadata.getNodataValues();
					// } else if (column == COLIDX_TRANSLATE) {
					// return attMetadata;
				} else if (column == COLIDX_TITLES) {
					return attMetadata.getTitle();
				} else if (column == COLIDX_DESCS) {
					return attMetadata.getDesc();
				} else
					return super.getValueAt(row, column);
			}

			@Override
			public void setValueAt(final Object value, final int row,
					final int column) {
				AttributeMetadata attMetadata = getAttMetaData(row);

				if (column == COLIDX_WEIGHT) {
					Integer newWeight = (Integer) value;

					// We don't want doubles in the list, when entered by the
					// editor
					boolean doubleWeight = false;
					for (AttributeMetadata a : attMetadataMap.values()) {
						if (a.getWeight() == newWeight) {
							doubleWeight = true;
							break;
						}
					}

					if (doubleWeight) {
						// We don't want doubles in the list, when entered by
						// the
						// editor
						for (AttributeMetadata a : attMetadataMap.values()) {
							if (a.getWeight() >= newWeight)
								a.setWeight(a.getWeight() + 1);
						}
					}
					attMetadata.setWeight(newWeight);

					fireTableDataChanged();

				} else if (column == COLIDX_VIS) {
					attMetadata.setVisible((Boolean) value);
					fireTableDataChanged();

				} else if (column == COLIDX_UNIT) {
					attMetadata.setUnit((String) value);
				} else
					throw new IllegalAccessError("col = " + column
							+ " should not be editable.");

			}

			// This method returns the Class object of the first
			// cell in specified column in the table model.
			// Unless this method is overridden, all values are
			// assumed to be the type Object.
			@Override
			public Class<?> getColumnClass(final int columnIndex) {
				if (columnIndex == COLIDX_QUALITY)
					return AttributeMetadata.class;
				if (columnIndex == COLIDX_NAME)
					return Name.class;
				if (columnIndex == COLIDX_UNIT)
					return String.class;
				if (columnIndex == COLIDX_VIS)
					return Boolean.class;
				if (columnIndex == COLIDX_WEIGHT)
					return Integer.class;
				if (columnIndex == COLIDX_TITLES)
					return Translation.class;
				if (columnIndex == COLIDX_DESCS)
					return Translation.class;

				return super.getColumnClass(columnIndex);
			}

			/**
			 * Only the column for WEIGHT and UNIT and VISIBLITY are editable.
			 */
			@Override
			public boolean isCellEditable(final int row, final int column) {
				if (column == COLIDX_WEIGHT || column == COLIDX_UNIT
						|| column == COLIDX_VIS)
					return true;
				return false;
			}

			@Override
			public String getColumnName(int column) {
				switch (column) {
				case COLIDX_QUALITY:
					return AtlasCreator.R("MapPoolJTable.ColumnName.Quality");
				case COLIDX_WEIGHT:
					return AtlasCreator.R("Attributes.Edit.Weight");
				case COLIDX_VIS:
					return AtlasCreator.R("Attributes.Edit.Visibility");
				case COLIDX_NAME:
					return AtlasCreator.R("Attributes.Edit.Name");
				case COLIDX_TYPE:
					return AtlasCreator.R("Attributes.Edit.Type");
				case COLIDX_UNIT:
					return AtlasCreator.R("Unit");
				case COLIDX_NODATA:
					return AtlasCreator.R("NodataValues");
					// case COLIDX_TRANSLATE:
					// return AtlasCreator.R("Attributes.Edit.TitleDesc");
				case COLIDX_TITLES:
					return AtlasCreator.R("Attributes.Edit.Title");
				case COLIDX_DESCS:
					return AtlasCreator.R("Attributes.Edit.Desc");
				}
				return super.getColumnName(column);
			}
		};

		setModel(model);
		setColumnSelectionAllowed(false);

		SwingUtil.setColumnLook(this, COLIDX_QUALITY,
				new QualityPercentageTableCellRenderer(dplv_.getAc()),
				QualityPercentageTableCellRenderer.MINWIDTH, null,
				QualityPercentageTableCellRenderer.MAXWIDTH);

		SwingUtil.setColumnLook(this, COLIDX_WEIGHT, null, null, 70, 110);
		SwingUtil.setColumnLook(this, COLIDX_VIS, null, null, 30, 90);
		SwingUtil.setColumnLook(this, COLIDX_NAME, new NameTableCellRenderer(),
				null, null, null);
		SwingUtil.setColumnLook(this, COLIDX_TYPE, null, null, null, 120);
		SwingUtil.setColumnLook(this, COLIDX_UNIT, null, null, 40, 100);
		SwingUtil.setColumnLook(this, COLIDX_NODATA,
				new NoDataTableCellRenderer(), null, 100, 140);
		// SwingUtil.setColumnLook(this, COLIDX_TRANSLATE,
		// new ButtonCellRenderer(), null, 80, null);
		SwingUtil.setColumnLook(this, COLIDX_TITLES,
				new TranslationCellRenderer(dplv_.getAc()), 200, 250, null);
		SwingUtil.setColumnLook(this, COLIDX_DESCS,
				new TranslationCellRenderer(dplv_.getAc()), 200, 300, null);

		// passes the clicks to the button if they are visible.
		addMouseListener(new JTableButtonMouseListener());
		/**
		 * Use Java 1.6 to make the columns sortable: By default sort by
		 * visibile and weight
		 */
		final RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(
				getModel()) {
			@Override
			public Comparator<?> getComparator(int column) {
				if (column == COLIDX_QUALITY) {

					// Sorting the QM column is tricky, because it is of class
					// AttributeMetaData and would usually be sorted by the
					// weight.
					return new Comparator<AttributeMetadata>() {

						@Override
						public int compare(AttributeMetadata o1,
								AttributeMetadata o2) {
							return new Double(o1.isVisible() ? o1
									.getQuality(attMetadataMap.getLanguages())
									: -1.).compareTo(o2.isVisible() ? o2
									.getQuality(attMetadataMap.getLanguages())
									: -1.);
						}
					};
				}
				return super.getComparator(column);
			}
		};
		setRowSorter(sorter);
		ArrayList<SortKey> keys = new ArrayList<SortKey>();
		keys.add(new SortKey(COLIDX_VIS, SortOrder.DESCENDING));
		keys.add(new SortKey(COLIDX_WEIGHT, SortOrder.ASCENDING));
		sorter.setSortKeys(keys);

		new TableRowHeightAdjustment()
				.packRows(AttribTranslationJTable.this, 0);

	}

	/**
	 * @param row
	 *            in view coordinates!
	 * @return
	 */
	private AttributeMetadata getAttMetaDataView(int viewRow) {
		return getAttMetaData(convertRowIndexToModel(viewRow));
	}

	/**
	 * @param row
	 *            in model coordinates!
	 * @return
	 */
	private AttributeMetadata getAttMetaData(final int row) {
		ArrayList<AttributeMetadata> listValues = new ArrayList<AttributeMetadata>(
				attMetadataMap.values());
		Collections.sort(listValues);
		AttributeMetadata attMetadata = listValues.get(row);
		return attMetadata;
	}

	// This renderer extends a component. It is used each time a
	// cell must be displayed.
	public class ButtonCellRenderer extends DefaultTableCellRenderer {
		JButton button = new JButton("edit"); // i8n

		// This method is called each time a cell in a column
		// using this renderer needs to be rendered.
		public Component getTableCellRendererComponent(final JTable table,
				final Object value, final boolean isSelected,
				final boolean hasFocus, final int rowIndex, final int vColIndex) {

			final AttributeMetadata atm = (AttributeMetadata) value;
			if (!atm.isVisible())
				return super.getTableCellRendererComponent(table, "",
						isSelected, hasFocus, rowIndex, vColIndex);

			return button;
		}

	}

	// This renderer extends a component. It is used each time a
	// cell must be displayed.
	public class NameTableCellRenderer extends DefaultTableCellRenderer {

		// This method is called each time a cell in a column
		// using this renderer needs to be rendered.
		public Component getTableCellRendererComponent(final JTable table,
				final Object value, final boolean isSelected,
				final boolean hasFocus, final int rowIndex, final int vColIndex) {

			Name name = (Name) value;

			JLabel tableCellRendererComponent = (JLabel) super
					.getTableCellRendererComponent(table, name.toString(),
							isSelected, hasFocus, rowIndex, vColIndex);
			tableCellRendererComponent.setFont(FATFONT);

			return tableCellRendererComponent;
		}

	}

	// This renderer nicely paints a list of NoData values. Mainly it just put "
	// around Strings.
	public class NoDataTableCellRenderer extends DefaultTableCellRenderer {

		// This method is called each time a cell in a column
		// using this renderer needs to be rendered.
		public Component getTableCellRendererComponent(final JTable table,
				final Object value, final boolean isSelected,
				final boolean hasFocus, final int rowIndex, final int vColIndex) {

			JLabel tableCellRendererComponent = (JLabel) super
					.getTableCellRendererComponent(table, AttributeMetadata
							.formatNoDataValues((Set<Object>) value),
							isSelected, hasFocus, rowIndex, vColIndex);

			tableCellRendererComponent.setFont(FATFONT);

			return tableCellRendererComponent;
		}

	}

	/**
	 * This {@link MouseListener} passes the clicks to the button if they are
	 * visible.
	 * 
	 */
	class JTableButtonMouseListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			int column = columnAtPoint(e.getPoint());
			int row = rowAtPoint(e.getPoint());

			if (convertColumnIndexToModel(column) == COLIDX_NODATA) {
				AttributeMetadata attMetaData = getAttMetaDataView(row);

				// This is modal
				NoDataEditListDialog ad = new NoDataEditListDialog(
						AttribTranslationJTable.this, dplv.getSchema(), attMetaData);
				ad.setVisible(true);

				// if (!ad.isCancelled()) {
				model.fireTableDataChanged();
				// }
			}

			if (convertColumnIndexToModel(column) == COLIDX_TITLES
					|| convertColumnIndexToModel(column) == COLIDX_DESCS) {
				AttributeMetadata attMetaData = getAttMetaDataView(row);

				if (!attMetaData.isVisible())
					return;

				AttribtTranslationDialog ad = new AttribtTranslationDialog(
						attMetaData.getName(), attMetaData.getTitle(),
						attMetaData.getDesc());
				ad.setModal(true);
				ad.setVisible(true);

				// if (!ad.isCancelled()) {
				model.fireTableDataChanged();
				// }
			}
		}

	}

	/**
	 * This {@link TranslationAskJDialog} asks to translate title and
	 * description for an attribute
	 */
	public class AttribtTranslationDialog extends TranslationAskJDialog {

		public AttribtTranslationDialog(Name attName, Translation title,
				Translation desc) {
			super(AttribTranslationJTable.this);

			List<String> languages = dplv.getAc().getLanguages();

			TranslationEditJPanel transName = new TranslationEditJPanel(
					AtlasCreator.R("EditDPEDialog.TranslateTitle"), title,
					languages);
			TranslationEditJPanel transDesc = new TranslationEditJPanel(
					AtlasCreator.R("EditDPEDialog.TranslateDescription"), desc,
					languages);

			setComponents(transName, transDesc);
			setTitle(attName.toString());
		}
	}

}
