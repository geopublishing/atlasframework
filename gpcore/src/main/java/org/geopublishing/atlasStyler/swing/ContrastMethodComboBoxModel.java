package org.geopublishing.atlasStyler.swing;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.opengis.style.ContrastMethod;

public class ContrastMethodComboBoxModel extends DefaultComboBoxModel implements
		ComboBoxModel {

	public ContrastMethodComboBoxModel() {
		super(ContrastMethod.values());
	}

}
