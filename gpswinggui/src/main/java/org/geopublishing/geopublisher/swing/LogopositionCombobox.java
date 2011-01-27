package org.geopublishing.geopublisher.swing;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import de.schmitzm.geotools.LogoPosition;

public class LogopositionCombobox extends JComboBox {
	public LogopositionCombobox() {
		super(LogoPosition.values());

		setRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel proto = (JLabel) super.getListCellRendererComponent(
						list, value, index, isSelected, cellHasFocus);

				proto.setText(((LogoPosition) value).getDescription());

				return proto;
			}

		});
	}
}
