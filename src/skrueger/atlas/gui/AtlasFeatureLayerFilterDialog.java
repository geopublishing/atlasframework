/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.atlas.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.table.JTableHeader;

import org.apache.log4j.Logger;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.map.MapLayer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;

import schmitzm.geotools.feature.CQLFilterParser;
import schmitzm.geotools.feature.FeatureTypeTableModel;
import schmitzm.geotools.feature.FilterParser;
import schmitzm.geotools.gui.FeatureCollectionFilterPanel;
import schmitzm.geotools.gui.FeatureLayerFilterDialog;
import schmitzm.geotools.gui.SelectableXMapPane;
import schmitzm.lang.LangUtil;
import schmitzm.swing.SwingUtil;
import skrueger.AttributeMetaDataAttributeTypeFilter;
import skrueger.AttributeMetadata;
import skrueger.AttributeMetadataImpl;
import skrueger.atlas.AtlasViewer;
import skrueger.geotools.AttributeMetadataMap;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.i8n.I8NUtil;

/**
 * This dialog extends the {@link FeatureLayerFilterDialog} by special features
 * provided by the {@link AttributeMetadataImpl}:
 * <ul>
 * <li>The attribute table also shows the attribute description from the
 * {@link AttributeMetadataImpl}-map.</li>
 * <li>In the attribute table and the filter preview table only the attributes
 * are shown which are declared as visible in the meta data.</li>
 * </ul>
 * 
 * @author <a href="mailto:schmitzm@bonn.edu">Martin Schmitz</a> (University of
 *         Bonn/Germany)
 * @version 1.0
 */
public class AtlasFeatureLayerFilterDialog extends FeatureLayerFilterDialog {
	/**
	 * Parser, der standardmaessig verwendet wird, um den {@link Filter} zu
	 * erstellen.
	 */
	public static final FilterParser FILTER_PARSER = new CQLFilterParser();

	static Logger LOGGER = Logger
			.getLogger(AtlasFeatureLayerFilterDialog.class);

	/** Holds the attribute meta data for the layer */
	protected AttributeMetadataMap attrMetaDataMap;

	private final StyledFeaturesInterface<?> styedFeatures;

	/**
	 * Created a new dialog
	 * 
	 * @param parent
	 *            the parent Frame
	 * @param mapPane
	 *            the {@link SelectableXMapPane} where the layer is shown in
	 * @param mapLayer
	 *            the layer the features are taken from
	 * @param attrMetaDataMap
	 *            the attribute meta data for the features
	 * @param initGUI
	 *            determines, whether {@link #initGUI()} is called. If {@code
	 *            false}, the constructor of the derived class must ensure that
	 *            {@link #initGUI()} is called.
	 * @param layerName
	 *            Translated name of the layer
	 * @throws IOException
	 *             if an error occurs during determining the features form the
	 *             layer
	 */
	protected AtlasFeatureLayerFilterDialog(Component parent,
			StyledFeaturesInterface<?> styledFeatures,
			SelectableXMapPane mapPane, MapLayer mapLayer, boolean initGUI) {
		super(parent, mapPane, mapLayer, false, false);
		this.attrMetaDataMap = styledFeatures.getAttributeMetaDataMap();
		this.styedFeatures = styledFeatures;
		if (!styledFeatures.getFilter().equals(Filter.INCLUDE)) {
			setFilterRule(CQL.toCQL(styledFeatures.getFilter()));
		}
		if (initGUI) {
			initGUI(styledFeatures.getTitle().toString());
			pack();
		}
	}

	/**
	 * Created a new dialog
	 * 
	 * @param parent
	 *            the parent Frame
	 * @param mapPane
	 *            the {@link SelectableXMapPane} where the layer is shown in
	 * @param mapLayer
	 *            the layer the features are taken from
	 * @param attrMetaDataMap
	 *            the attribute meta data for the features
	 * @param layerName
	 *            Translated name of the layer
	 * @throws IOException
	 *             if an error occurs during determining the features form the
	 *             layer
	 */
	public AtlasFeatureLayerFilterDialog(Component parent,
			StyledFeaturesInterface<?> styledFeatures,
			SelectableXMapPane mapPane, MapLayer mapLayer) {
		this(parent, styledFeatures, mapPane, mapLayer, true);
	}

	@Override
	public FeatureCollectionFilterPanel getFilterPanel() {
		if (filterPanel == null) {

			filterPanel = new FeatureCollectionFilterPanel(
					FILTER_PARSER,
					(FeatureCollection<SimpleFeatureType, SimpleFeature>) styedFeatures
							.getFeatureCollection(), false,
					new AttributeMetaDataAttributeTypeFilter(attrMetaDataMap)) {

				@Override
				protected void initGUI() {
					
					super.initGUI();
					
					// Changing a few things to get the AttributeMetaData into
					// the
					// Table
					attributeTableModel = new FeatureTypeTableModel() {

						@Override
						public String[] createColumnNames() {
							String[] superColNames = super.createColumnNames();

							superColNames[0] = AtlasViewer
									.R("AtlasFeatureLayerFilterDialog.AttributeTableHeader.VariableName");

							String[] extendedArray = LangUtil
									.extendArray(
											superColNames,
											AtlasViewer
													.R("AtlasFeatureLayerFilterDialog.AttributeTableHeader.Name"),
											AtlasViewer
													.R("AtlasFeatureLayerFilterDialog.AttributeTableHeader.Description"),
											AtlasViewer
													.R("AtlasFeatureLayerFilterDialog.AttributeTableHeader.Unit"));
							return extendedArray;
						}

						@Override
						public Object getValueAt(int row, int col) {

							final AttributeDescriptor lookingForAD = attrTypes
									.elementAt(row);
							// int attrIdx =
							// featureType.getAttributeDescriptors()
							// .indexOf(lookingForAD);

							AttributeMetadata metaData = attrMetaDataMap
									.get(lookingForAD.getName());
							if (col == getColumnCount() - 3
									&& attrMetaDataMap != null) {
								/**
								 * This column represents the translated title
								 * of the variable
								 */
								return metaData == null ? null : metaData
										.getTitle();
							} else if (col == getColumnCount() - 2
									&& attrMetaDataMap != null) {
								/**
								 * This column represents the translated
								 * description of the variable
								 */
								if (metaData == null
										|| I8NUtil.isEmpty(metaData.getDesc()))
									return null;

								return metaData.getDesc();
							} else if (col == getColumnCount() - 1
									&& attrMetaDataMap != null) {
								/**
								 * This column represents the translated UNIT of
								 * the variable
								 */
								if (metaData == null
										|| I8NUtil.isEmpty(metaData.getUnit()))
									return null;

								return metaData.getUnit();
							}
							return super.getValueAt(row, col);

						}
					};

					attributeTableModel
							.setAttributeFilter(new AttributeMetaDataAttributeTypeFilter(
									attrMetaDataMap));

					this.attributeTable.setModel(this.attributeTableModel);
					this.attributeTable.setTableHeader(new JTableHeader(
							attributeTable.getColumnModel()));
					this.attributeTable.getColumnModel().getColumn(0)
							.setMinWidth(150);
					this.attributeTable.getColumnModel().getColumn(1)
							.setMinWidth(100);
					this.attributeTable.getColumnModel().getColumn(2)
							.setPreferredWidth(200);
					this.attributeTable.getColumnModel().getColumn(3)
							.setPreferredWidth(200);
					this.attributeTable.getColumnModel().getColumn(4)
							.setPreferredWidth(60);
					
					SwingUtil.setMinimumHeight(this.attributeTable, 250); // has no effect :-(
					
				}

				@Override
				protected void resetComponentsAfterTest(Throwable err) {
					super.resetComponentsAfterTest(err);
					// dis/enable OK- and APPLY-Button on error
					okButton.setEnabled(err == null);
					applyButton.setEnabled(err == null);
				}
			};

		}
		return filterPanel;
	}

	/**
	 * Calls {@code super.initGUI()} and then replaces the {@code filterPanel}
	 * with a new one.
	 */
	protected void initGUI(final String layerNameTranslated) {
		// getFilterPanel().setAttributeFilter(
		// );

		super.initGUI();
		// replace attribute-table with a new one which shows the attribute
		// description from the meta data
		// remove(filterPanel);

		// add(filterPanel, BorderLayout.CENTER);

		// only the visible attributes are shown in attribute table
		// and filter preview
		// getFilterPanel().setAttributeFilter(
		// new AttributeMetaDataAttributeTypeFilter(attrMetaDataMap));

		if (layerNameTranslated != null) {
			String windowTitle = AtlasViewer.R(
					"AtlasFeatureLayerFilterDialog.WindowTitle",
					layerNameTranslated);
			setTitle(windowTitle);
		}
	}

	/**
	 * Since the registerKeyboardAction() method is part of the JComponent class
	 * definition, you must define the Escape keystroke and register the
	 * keyboard action with a JComponent, not with a JDialog. The JRootPane for
	 * the JDialog serves as an excellent choice to associate the registration,
	 * as this will always be visible. If you override the protected
	 * createRootPane() method of JDialog, you can return your custom JRootPane
	 * with the keystroke enabled:
	 */
	@Override
	protected JRootPane createRootPane() {
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		JRootPane rootPane = new JRootPane();
		rootPane.registerKeyboardAction(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}

		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		return rootPane;
	}

}
