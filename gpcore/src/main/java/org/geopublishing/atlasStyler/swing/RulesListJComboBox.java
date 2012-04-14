package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.rulesLists.AbstractRulesList.RulesListType;
import org.opengis.feature.type.FeatureType;

import de.schmitzm.swing.SwingUtil;

/**
 * A {@link JComboBox} which offers a selection of RulesList. Only RLs that
 * "make sense" for the given {@link FeatureType} are presented.
 * 
 */
public class RulesListJComboBox extends JComboBox<RulesListType> {

	private static final long serialVersionUID = -5625980802182080799L;
	
	private final DefaultComboBoxModel<RulesListType> model;

	public RulesListJComboBox(AtlasStyler as) {

		SwingUtil.addMouseWheelForCombobox(this, false);
		RulesListType[] valuesFor = RulesListType.valuesFor(as);
		Arrays.sort(valuesFor);
		model = new DefaultComboBoxModel<RulesListType>(valuesFor);
		setModel(model);

		setRenderer(new DefaultListCellRenderer() {

			private static final long serialVersionUID = -457353262119405038L;

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				JLabel proto = (JLabel) super.getListCellRendererComponent(
						list, value, index, isSelected, cellHasFocus);

				RulesListType rlt = (RulesListType) value;

				proto.setText("<html>" + rlt.getTitle() + "</html>");

				return proto;
			}
		});

	}

}
