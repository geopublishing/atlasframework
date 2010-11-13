package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.AbstractRulesList.RulesListType;
import org.geopublishing.atlasStyler.AtlasStyler;

import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;

public class RulesListJComboBox extends JComboBox {

	private final DefaultComboBoxModel model;

	public RulesListJComboBox(GeometryForm gf, AtlasStyler as) {

		SwingUtil.addMouseWheelForCombobox(this, false);
		RulesListType[] valuesFor = RulesListType.valuesFor(gf, as
				.getStyledFeatures().getSchema());
		Arrays.sort(valuesFor);
		model = new DefaultComboBoxModel(valuesFor);
		setModel(model);

		setRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				RulesListType rlt = (RulesListType) value;

				JLabel proto = (JLabel) super.getListCellRendererComponent(
						list, value, index, isSelected, cellHasFocus);

				// proto.setText("<html>" + rlt.getTitle() + "</html>");

				proto.setText(proto.getText() + " " + rlt.getImageResLocation());

				JPanel label = new JPanel(new MigLayout(
						"gapx 0, gapy 0, wrap 2", "[grow]"));

				label.add(proto, "gapy 0");
				label.add(new JLabel(rlt.getImage()),
						"gapx 0, gapy 0, align right");

				return label;
			}
		});
	}

}
