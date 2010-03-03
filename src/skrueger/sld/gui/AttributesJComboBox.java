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
package skrueger.sld.gui;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import org.apache.log4j.Logger;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.swing.SwingUtil;
import skrueger.AttributeMetadata;
import skrueger.geotools.AttributeMetadataMap;
import skrueger.i8n.I8NUtil;
import skrueger.i8n.Translation;
import skrueger.sld.ASUtil;
import skrueger.sld.AtlasStyler;

/**
 * This extension of a {@link JComboBox} is specialized on the visualization of
 * a selection of attribute. If {@link AttributeMetadata} is stored in the
 * {@link AtlasStyler}, it's used for lables and tooltips.<br/>
 * {@link AttributesJComboBox} only sends {@link ItemEvent} of type SELECTED. UNSELETED is ignored. 
 * 
 * @author Stefan A. Krüger
 */
public class AttributesJComboBox extends JComboBox {
	static final private Logger LOGGER = ASUtil.createLogger(AttributesJComboBox.class);

	private List<String> numericalAttribs;

	public AttributesJComboBox(final AtlasStyler atlasStyler,
			List<String> attributes) {
		this(atlasStyler, new DefaultComboBoxModel(attributes
				.toArray(new String[] {})));
	}

	public AttributesJComboBox(final SimpleFeatureType schema,
			final AttributeMetadataMap attributeMetaDataMap,
			List<String> attributes) {
		this(schema, attributeMetaDataMap, new DefaultComboBoxModel(attributes
				.toArray(new String[] {})));
	}

	public AttributesJComboBox(final AtlasStyler atlasStyler,
			ComboBoxModel comboBoxModel) {
		this(atlasStyler.getStyledFeatures().getSchema(), atlasStyler
				.getAttributeMetaDataMap(), comboBoxModel);
	}

	public AttributesJComboBox(final SimpleFeatureType schema_,
			final AttributeMetadataMap attributeMetaDataMap_,
			ComboBoxModel comboBoxModel_) {
		setValues(schema_, attributeMetaDataMap_, comboBoxModel_);
		SwingUtil.setMaximumWidth(this, 350);
	}
	
	/**
	 * This {@link JComboBox} is only sending {@link ItemEvent} of thype SELECTED. UNSELETED is omittet. 
	 */
	@Override
	protected void fireItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.DESELECTED) return;
		super.fireItemStateChanged(e);
	}

	/**
	 * Should only be called once! Either by the constructor, or when the
	 * default constructor was used by the API user.
	 * 
	 * @param schema
	 * @param attributeMetaDataMap
	 * @param comboBoxModel
	 */
	public void setValues(final SimpleFeatureType schema,
			final AttributeMetadataMap attributeMetaDataMap,
			ComboBoxModel comboBoxModel) {
		setModel(comboBoxModel);

		/*
		 * Caching the list of numerical attributes, so we can quickly determine
		 * the type of a selected attribute without accessing the schema.
		 */
		numericalAttribs = FeatureUtil.getNumericalFieldNames(schema, false);

		SwingUtil.addMouseWheelForCombobox(this, false);

		/**
		 * Use the AttributeMetaData (if available) for label+tooltip
		 */
		setRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				JLabel prototype = (JLabel) super.getListCellRendererComponent(
						list, value, index, isSelected, cellHasFocus);

				// This list may contain null or ""
				if (value == null || value instanceof String
						&& ((String) value).trim().isEmpty() || value instanceof String
						&& ((String) value).equalsIgnoreCase("-")) {
					prototype.setText("-");					
					return prototype;
				}

				AttributeMetadata attributeMetadataFor = attributeMetaDataMap != null ? attributeMetaDataMap
						.get(prototype.getText())
						: null;

				prototype.setToolTipText(null);
				if (attributeMetadataFor != null) {

					final String metaTitle = attributeMetadataFor.getTitle()
							.toString();
					if (!I8NUtil.isEmpty(metaTitle))
						prototype.setText("<html>" + metaTitle);
					else
						prototype.setText("<html>" + prototype.getText());

					final Translation metaDesc = attributeMetadataFor.getDesc();
					if (!I8NUtil.isEmpty(metaDesc))
						prototype.setToolTipText(metaDesc.toString());
				} else
					prototype.setText("<html>" + prototype.getText());

				/**
				 * Adding the Attribute-Type to the Label
				 */
				if (attributeMetadataFor != null) {
					AttributeDescriptor attributeDesc = schema.getDescriptor(attributeMetadataFor.getName());
					if (attributeDesc != null) {
						prototype.setText(prototype.getText()
								+ " <i>("
								+ attributeDesc.getType().getBinding()
								.getSimpleName() + ")</i>");
					} else {
						LOGGER.warn("No attributedesc for "+attributeDesc+" found.");
					}
				}

				prototype.setText(prototype.getText() + "</html>");

				return prototype;
			}
		});
	}

	/**
	 * @return <code>true</code> if an attribute is selected, and it is a
	 *         numerical attribute type. Calling this method is cheap and
	 *         doesn't access the schema.
	 */
	public boolean isNumericalAttribSelected() {
		final String selectedItem = (String) getSelectedItem();
		return (selectedItem != null && numericalAttribs.contains(selectedItem));
	}
}
