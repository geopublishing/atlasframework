/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import org.geopublishing.atlasStyler.ASUtil;

import schmitzm.swing.JPanel;

public abstract class AbstractEditGUI extends JPanel {

	final public static Float[] OPACITY_VALUES = new Float[] { 0.1f, .2f, .3f,
			.4f, .5f, .6f, .7f, .8f, .9f, 1.f };

	/** Values used for JCOmboBoxes offering a Halo setting **/
	final public static Float[] HALO_RADIUS_VALUES = new Float[] { 0.f, .5f,
			1f, 1.5f, 2.f, 2.5f, 3.f, 4f, 5f };

	/** A renderer designed to visualize the Halo Radius values properly **/
	final public static DefaultListCellRenderer HALO_RADIUS_VALUES_RENDERER = new DefaultListCellRenderer() {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			final JLabel prototype = (JLabel) super
					.getListCellRendererComponent(list, value, index,
							isSelected, cellHasFocus);
			if (value.equals(0f)) {
				prototype
						.setText("<html>"
								+ prototype.getText()
								+ " <i><font size='-2'>("
								+ ASUtil.R("AtlasStyler.DropDownSelection.Option.Fastest")
								+ ")</font></i></html>");
			} else if (value.equals(1f)) {
				prototype
						.setText("<html>"
								+ prototype.getText()
								+ " <i><font size='-2'>("
								+ ASUtil.R("AtlasStyler.DropDownSelection.Option.Faster")
								+ ")</font></i></html>");
			} else if (value.equals(2f)) {
				prototype.setText("<html>" + prototype.getText()
						+ " <i><font size='-2'>("
						+ ASUtil.R("AtlasStyler.DropDownSelection.Option.Fast")
						+ ")</font></i></html>");
			} else if (value.equals(3f)) {
				prototype.setText("<html>" + prototype.getText()
						+ " <i><font size='-2'>("
						+ ASUtil.R("AtlasStyler.DropDownSelection.Option.Fast")
						+ ")</font></i></html>");
			}
			return prototype;
		}
	};

	final public static Float[] DISPLACEMENT_VALUES = new Float[] { 0f, 0.5f,
			1.f, 1.5f, 2.f, 3.f, 4.f, 5.f, 6.f, 7.f, 8.f, 9.f, 10.f, 11.f,
			12.f, 13.f, 14.f, 15.f, 16.f, 17.f, 18.f, 19.f, 20.f };
	/**
	 * A renderer designed to visualize thePOINTDISPLACEMENT_VALUES_RENDERER
	 * nicely
	 **/
	final public static DefaultListCellRenderer DISPLACEMENT_VALUES_RENDERER = new DefaultListCellRenderer() {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			final JLabel prototype = (JLabel) super
					.getListCellRendererComponent(list, value, index,
							isSelected, cellHasFocus);

			prototype.setText(NumberFormat.getIntegerInstance().format(value)
					+ "px");

			return prototype;

		}
	};

	final public static Double[] ROTATION_VALUES;
	static {
		ROTATION_VALUES = new Double[360 / 5];
		for (int i = 0; i < 360 / 5; i++) {
			ROTATION_VALUES[i] = i * 5.;
		}
	}

	final public static DefaultListCellRenderer ROTATION_VALUES_RENDERER = new DefaultListCellRenderer() {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			final JLabel prototype = (JLabel) super
					.getListCellRendererComponent(list, value, index,
							isSelected, cellHasFocus);

			prototype.setText(NumberFormat.getIntegerInstance().format(value)
					+ "\u00b0");

			return prototype;

		}
	};

	/**
	 * Used for moving the displacement
	 */
	public static Float[] POINTDISPLACEMENT_VALUES = new Float[41];
	static {
		for (Integer i = -20; i <= 20; i++) {
			POINTDISPLACEMENT_VALUES[i + 20] = i.floatValue();
		}
	}

	/**
	 * A renderer designed to visualize thePOINTDISPLACEMENT_VALUES_RENDERER
	 * nicely
	 **/
	final public static DefaultListCellRenderer POINTDISPLACEMENT_VALUES_RENDERER = new DefaultListCellRenderer() {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			final JLabel prototype = (JLabel) super
					.getListCellRendererComponent(list, value, index,
							isSelected, cellHasFocus);

			prototype.setText(NumberFormat.getIntegerInstance().format(value)
					+ "px");

			return prototype;

		}
	};

	/**
	 * Used for moving the displacement
	 */
	public static Float[] ANCHORVALUES = new Float[21];
	static {
		for (Integer i = -10; i <= 10; i++) {
			ANCHORVALUES[i + 10] = i.floatValue() / 10f;
		}
	}
	/** A renderer designed to visualize the Halo Radius values properly **/
	final public static DefaultListCellRenderer ANCHORVALUES_RENDERER = new DefaultListCellRenderer() {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			final JLabel prototype = (JLabel) super
					.getListCellRendererComponent(list, value, index,
							isSelected, cellHasFocus);

			prototype.setAlignmentX(1f);
			prototype.setText(NumberFormat.getPercentInstance().format(value));

			return prototype;

		}
	};

	final public static Float[] SIZE_VALUES;
	static {
		SIZE_VALUES = new Float[100];
		for (int i = 0; i < 48; i++) {
			SIZE_VALUES[i] = i + 3f;
		}
		for (int i = 48; i < 60; i++) {
			SIZE_VALUES[i] = SIZE_VALUES[i - 1] + 10;
		}
		for (int i = 60; i < 70; i++) {
			SIZE_VALUES[i] = SIZE_VALUES[i - 1] + 30;
		}
		for (int i = 70; i < 90; i++) {
			SIZE_VALUES[i] = SIZE_VALUES[i - 1] + 50;
		}
		for (int i = 90; i < 100; i++) {
			SIZE_VALUES[i] = SIZE_VALUES[i - 1] + 100;
		}

	}

	final public static Float[] WIDTH_VALUES;
	static {
		WIDTH_VALUES = new Float[15];
		for (int i = 1; i < 16; i++) {
			WIDTH_VALUES[i - 1] = i * 0.5f;
		}
	}
	final static public DefaultListCellRenderer WIDTH_VALUES_RENDERER = new DefaultListCellRenderer() {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			final JLabel prototype = (JLabel) super
					.getListCellRendererComponent(list, value, index,
							isSelected, cellHasFocus);
			if (value.equals(1f)) {
				prototype
						.setText("<html>"
								+ prototype.getText()
								+ " <i><font size='-2'>("
								+ ASUtil.R("AtlasStyler.DropDownSelection.Option.Fastest")
								+ ")</font></i></html>");
			} else if (value.equals(2f) || value.equals(3f) || value.equals(4f)) {
				prototype.setText("<html>" + prototype.getText()
						+ " <i><font size='-2'>("
						+ ASUtil.R("AtlasStyler.DropDownSelection.Option.Fast")
						+ ")</font></i></html>");
			}
			return prototype;

		}
	};

	protected static final String PROPERTY_UPDATED = "UPDATE_PREVIEWS";

}
