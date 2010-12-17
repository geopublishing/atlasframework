package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import org.apache.log4j.Logger;

public class OpacityJComboBox extends JComboBox {

	final static Logger logger = Logger.getLogger(OpacityJComboBox.class);

	public OpacityJComboBox() {
		setRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				Component p = super.getListCellRendererComponent(list, value,
						index, isSelected, cellHasFocus);

				if (p instanceof JLabel && value instanceof Number)
					((JLabel) p).setText(NumberFormat.getPercentInstance()
							.format(value));
				else
					logger.warn(OpacityJComboBox.class.getSimpleName()
							+ " has an illegal element " + value);

				return p;
			}
		});
	}
}
