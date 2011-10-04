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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.rulesLists.AbstractRulesList;
import org.geopublishing.atlasStyler.svg.swing.SVGSelector;
import org.geotools.renderer.style.SVGGraphicFactory;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Graphic;

import de.schmitzm.geotools.FilterUtil;
import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.SwingUtil;

/**
 * Parent-class covering all GUI {@link JPanel}s allowing to edit
 * {@link AbstractRulesList}
 */
public abstract class AbstractStyleEditGUI extends JPanel {

	final public static Double[] GAMMA_VALUES = new Double[] { 0.1, .2, .3, .4, .5, .6, .7, .8, .9, 1., 1.1, 1.2, 1.3,
			1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2. };

	final public static Float[] OPACITY_VALUES = new Float[] { 0.1f, .2f, .3f, .4f, .5f, .6f, .7f, .8f, .9f, 1.f };

	/** Values used for JCOmboBoxes offering a Halo setting **/
	final public static Float[] HALO_RADIUS_VALUES = new Float[] { 0.f, .5f, 1f, 1.5f, 2.f, 2.5f, 3.f, 4f, 5f };

	/** Values used or JCOmboBoxes offering a "space-arround" setting **/
	final public static Integer[] SPACE_AROUND_VALUES = new Integer[] { 0, 1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 25,
			30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100 };

	/** A renderer designed to visualize the Halo Radius values properly **/
	final public static DefaultListCellRenderer HALO_RADIUS_VALUES_RENDERER = new DefaultListCellRenderer() {

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			final JLabel prototype = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			if (value.equals(0f)) {
				prototype.setText("<html>" + prototype.getText() + " <i><font size='-2'>("
						+ ASUtil.R("AtlasStyler.DropDownSelection.Option.Fastest") + ")</font></i></html>");
			} else if (value.equals(1f)) {
				prototype.setText("<html>" + prototype.getText() + " <i><font size='-2'>("
						+ ASUtil.R("AtlasStyler.DropDownSelection.Option.Faster") + ")</font></i></html>");
			} else if (value.equals(2f)) {
				prototype.setText("<html>" + prototype.getText() + " <i><font size='-2'>("
						+ ASUtil.R("AtlasStyler.DropDownSelection.Option.Fast") + ")</font></i></html>");
			} else if (value.equals(3f)) {
				prototype.setText("<html>" + prototype.getText() + " <i><font size='-2'>("
						+ ASUtil.R("AtlasStyler.DropDownSelection.Option.Fast") + ")</font></i></html>");
			}
			return prototype;
		}
	};

	final public static Float[] DISPLACEMENT_VALUES = new Float[] { 0f, 0.5f, 1.f, 1.5f, 2.f, 3.f, 4.f, 5.f, 6.f, 7.f,
			8.f, 9.f, 10.f, 11.f, 12.f, 13.f, 14.f, 15.f, 16.f, 17.f, 18.f, 19.f, 20.f };
	/**
	 * A renderer designed to visualize thePOINTDISPLACEMENT_VALUES_RENDERER
	 * nicely
	 **/
	final public static DefaultListCellRenderer DISPLACEMENT_VALUES_RENDERER = new DefaultListCellRenderer() {

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			final JLabel prototype = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			prototype.setText(NumberFormat.getIntegerInstance().format(value) + "px");

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
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			final JLabel prototype = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			prototype.setText(NumberFormat.getIntegerInstance().format(value) + "\u00b0");

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
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			final JLabel prototype = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			prototype.setText(NumberFormat.getIntegerInstance().format(value) + "px");

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
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			final JLabel prototype = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

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
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			final JLabel prototype = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			if (value.equals(1f)) {
				prototype.setText("<html>" + prototype.getText() + " <i><font size='-2'>("
						+ ASUtil.R("AtlasStyler.DropDownSelection.Option.Fastest") + ")</font></i></html>");
			} else if (value.equals(2f) || value.equals(3f) || value.equals(4f)) {
				prototype.setText("<html>" + prototype.getText() + " <i><font size='-2'>("
						+ ASUtil.R("AtlasStyler.DropDownSelection.Option.Fast") + ")</font></i></html>");
			}
			return prototype;

		}
	};

	protected static final String PROPERTY_UPDATED = "UPDATE_PREVIEWS";

	/**
	 * Pro AbstarctStyleEditGUI kann/darf nur ein SVG selector aufgemacht
	 * werden.
	 */
	private SVGSelector selectExternalGraphicDialog;

	protected void openExternalGraphicSelector(final GeometryForm geometryForm, final Graphic graphic) {
		try {
			if (selectExternalGraphicDialog == null) {

				selectExternalGraphicDialog = new SVGSelector(SwingUtil.getParentWindow(AbstractStyleEditGUI.this),
						geometryForm, graphic.getExternalGraphics());

				selectExternalGraphicDialog.setModal(true);
				selectExternalGraphicDialog.addPropertyChangeListener(new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {

						if (evt.getPropertyName().equals(SVGSelector.PROPERTY_UPDATED)) {

							ExternalGraphic[] egs = (ExternalGraphic[]) evt.getNewValue();

							graphic.graphicalSymbols().clear();

							if (egs != null) {
								graphic.graphicalSymbols().addAll(Arrays.asList(egs));
							}

							AbstractStyleEditGUI.this.firePropertyChange(AbstractStyleEditGUI.PROPERTY_UPDATED, null,
									null);

							updateExternalGraphicButton(graphic);
						}
					}

				});

			}
			selectExternalGraphicDialog.setVisible(true);

		} catch (Exception e1) {
			ExceptionDialog.show(SwingUtil.getParentWindowComponent(AbstractStyleEditGUI.this), e1);
		}

	}

	protected Logger LOGGER = LangUtil.createLogger(this);
	public static final String OPENMAPSYMBOLS_SVG_SERVERBASENAME = "http://http://freemapsymbols.org/svg";

	public static final String SVG_MIMETYPE = "image/svg+xml";
	public static final String PNG_MIMETYPE = "image/png";

	private static final int EXT_GRAPHIC_BUTTON_HEIGHT = 34;

	private static final int EXT_GRAPHIC_BUTTON_WIDTH = 34;

	protected void updateExternalGraphicButton(final Graphic graphic) {
		// Update the Button Icon
		SVGGraphicFactory svgFactory = new SVGGraphicFactory();
		Icon icon = null;

		if (graphic != null) {

			if ((graphic.getExternalGraphics() != null) && (graphic.getExternalGraphics().length > 0)) {
				ExternalGraphic externalGraphic = graphic.getExternalGraphics()[0];
				if (externalGraphic == null) {
					throw new IllegalArgumentException("ExternalGraphicsArray contains null");
				}

				try {
					URL url = externalGraphic.getLocation();

					icon = svgFactory.getIcon(null, FilterUtil.FILTER_FAC2.literal(url.toExternalForm()),
							externalGraphic.getFormat(), EXT_GRAPHIC_BUTTON_HEIGHT);
					// if (renderedImage != null)
					// icon = new ImageIcon(renderedImage);
				} catch (Exception e) {
					LOGGER.error("Creating SVG icon failed", e);
				}
			}
		}

		if (icon == null) {
			// Generating the icon failed, use an empty default
			icon = new ImageIcon(new BufferedImage(EXT_GRAPHIC_BUTTON_WIDTH, EXT_GRAPHIC_BUTTON_HEIGHT,
					BufferedImage.TYPE_INT_ARGB));
		}

		jButtonExtGraphic.setIcon(icon);
		jButtonExtGraphic.setSize(EXT_GRAPHIC_BUTTON_WIDTH, EXT_GRAPHIC_BUTTON_HEIGHT);

		// Repack the window if it makes sense
		final Window parentWindow = SwingUtil.getParentWindow(this);
		if (parentWindow != null)
			parentWindow.pack();

	}

	/**
	 */
	protected JButton getJButtonExtGraphic(final GeometryForm geometryForm, final Graphic graphic) {
		if (jButtonExtGraphic == null) {
			jButtonExtGraphic = new JButton();

			jButtonExtGraphic.setAction(new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					openExternalGraphicSelector(geometryForm, graphic);
				}

			});

			jButtonExtGraphic.setSize(EXT_GRAPHIC_BUTTON_WIDTH, EXT_GRAPHIC_BUTTON_HEIGHT);

			updateExternalGraphicButton(graphic);

		}
		return jButtonExtGraphic;
	}

	private JButton jButtonExtGraphic;
}
