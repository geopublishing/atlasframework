package org.geopublishing.geopublisher.gui.settings;

import java.awt.Component;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.geopublishing.geopublisher.export.GpHosterServerSettings;

public class GpHosterServerListJComboBox extends JComboBox {

	private final GpHosterServerList dbList;

	public GpHosterServerListJComboBox(GpHosterServerList wfsList) {
		super(wfsList.toArray(new GpHosterServerSettings[0]));
		this.dbList = wfsList;

		setRenderer(new ListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				GpHosterServerSettings x = (GpHosterServerSettings) value;
				if (x != null)
					return new JLabel(x.getAlias());
				return new JLabel("");
			}
		});
	}

	public GpHosterServerList getDbList() {
		return dbList;
	}

	public void listChanged() {
		setModel(new DefaultComboBoxModel(
				dbList.toArray(new GpHosterServerSettings[0])));
	}

}
