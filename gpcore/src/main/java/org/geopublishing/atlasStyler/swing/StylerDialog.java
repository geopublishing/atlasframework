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
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geotools.data.FeatureSource;
import org.geotools.styling.Style;
import org.geotools.util.WeakHashSet;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.geotools.gui.XMapPane;
import schmitzm.geotools.gui.XMapPaneEvent;
import schmitzm.geotools.map.event.JMapPaneListener;
import schmitzm.geotools.map.event.ScaleChangedEvent;
import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import skrueger.i8n.Translation;
import skrueger.swing.CancelButton;
import skrueger.swing.CancellableDialogAdapter;
import skrueger.swing.OkButton;

/**
 * This {@link JDialog} can be used to edit a {@link Style}.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 */
public class StylerDialog extends CancellableDialogAdapter {
	protected Logger LOGGER = ASUtil.createLogger(this);

	private JPanel jContentPane = null;

	final private AtlasStyler atlasStyler;

	private final AtlasStylerPane tabbedPane;

	private JCheckBox jCheckboxPreview;

	private JButton jButtonUpdatePreview;
	/**
	 * Listens to scaleChanges in the preview {@link XMapPane} and passes the
	 * listeners to
	 */
	private final JMapPaneListener scaleChangeListenerInPreviewXMapPanePassedToInternalListeners = new JMapPaneListener() {

		@Override
		public void performMapPaneEvent(XMapPaneEvent e) {
			if (e instanceof ScaleChangedEvent) {
				fireStyleChangeInPreviewEvents(((ScaleChangedEvent) e));
			}

		}
	};

	/**
	 * Stores the listener WEAKLY, keep a reference!
	 */
	public void addScaleChangeListener(PropertyChangeListener listener) {
		scaleChangeListeners.add(listener);
	}

	protected WeakHashSet<PropertyChangeListener> scaleChangeListeners = new WeakHashSet<PropertyChangeListener>(
			PropertyChangeListener.class);

	private final XMapPane previewMapPane;

	/**
	 * If <code>true</code> the GUI hides the more compilcated parts.
	 */
	private boolean easy;

	/**
	 * Creates an AtlasStyler {@link JDialog} which allows to create a
	 * SymbologyEncoding(SE) and StyledLayerDescriptor(SLD) for a
	 * {@link FeatureSource}.
	 * 
	 * @param owner
	 *            <code>null</code> or a {@link Window} component that shall be
	 *            used as a parent window for the {@link StylerDialog}.
	 * @param previewMapPane
	 *            a preview {@link XMapPane} or <code>null</code>.
	 */
	public StylerDialog(Component owner, AtlasStyler atlasStyler,
			XMapPane previewMapPane) {
		super(owner);
		this.atlasStyler = atlasStyler;
		this.previewMapPane = previewMapPane;
		atlasStyler.setOwner(this);
		this.tabbedPane = new AtlasStylerPane(this);
		initialize();

		pack();

		/**
		 * Position left outside of the actual parent frame
		 */
		SwingUtil.setRelativeFramePosition(this, owner, SwingUtil.BOUNDS_OUTER,
				SwingUtil.WEST);

		if (previewMapPane != null) {
			// Add a listener to the
			previewMapPane
					.addMapPaneListener(scaleChangeListenerInPreviewXMapPanePassedToInternalListeners);
		}

	}

	public void fireStyleChangeInPreviewEvents(ScaleChangedEvent sce) {

		Double newDenominator = sce.getNewScaleDenominator();

		LOGGER.info("new scale in preview XMapPane  = " + newDenominator);
		for (PropertyChangeListener l : scaleChangeListeners) {
			l.propertyChange(new PropertyChangeEvent(sce,
					"scale change in preview", null, newDenominator));
		}
	}

	/**
	 * This method initializes the {@link JDialog}.
	 * 
	 * @throws IOException
	 */
	private void initialize() {
		// setLayout(new MigLayout("width :800:850"));
		setMaximumSize(new Dimension(850, 400));
		setPreferredSize(new Dimension(850, 400));

		final SimpleFeatureType schema = atlasStyler.getStyledFeatures()
				.getSchema();

		String typeName = schema.getTypeName();

		// String typeName = atlasStyler.getFeatureSource().getDataStore()
		// .getTypeNames()[0];

		String geomTyp = schema.getGeometryDescriptor().getType().getBinding()
				.getSimpleName();

		final Translation title = atlasStyler.getTitle();
		setTitle("AtlasStyler for: " + title != null ? title.toString()
				: typeName + "  (" + geomTyp + ")");

		this.setContentPane(getJContentPane());

	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel(new MigLayout("gap 1, inset 1, wrap 1",
					"[grow]", "[grow, growprio 2000][]"));
			jContentPane.add(tabbedPane, "growx, growy 2000"); // The
																// textSymbolizer
			// pane is
			// making the big height

			jContentPane.add(getJCheckboxPreviewl(),
					"shrinky, bottom, split 4, left");
			jContentPane.add(getJButtonUpdatePreview(), "bottom, left");

			jContentPane.add(getJButtonOk(), "bottom, tag ok");
			jContentPane.add(getJButtonCancel(), "bottom, tag cancel");
		}
		return jContentPane;
	}

	private JButton getJButtonUpdatePreview() {
		if (jButtonUpdatePreview == null) {
			jButtonUpdatePreview = new JButton(new AbstractAction(
					AtlasStyler.R("AtlasStylerGUI.UpdatePreview.Button")) {

				@Override
				public void actionPerformed(ActionEvent e) {
					atlasStyler.fireStyleChangedEvents(true);
				}

			});
			jButtonUpdatePreview.setEnabled(!atlasStyler.isAutomaticPreview());
			getJCheckboxPreviewl().getModel().addChangeListener(
					new ChangeListener() {

						@Override
						public void stateChanged(ChangeEvent e) {
							jButtonUpdatePreview.setEnabled(!atlasStyler
									.isAutomaticPreview());
						}

					});

		}
		return jButtonUpdatePreview;
	}

	private JCheckBox getJCheckboxPreviewl() {
		if (jCheckboxPreview == null) {
			jCheckboxPreview = new JCheckBox(
					new AbstractAction(
							AtlasStyler
									.R("AtlasStylerGUI.UpdatePreviewAutomatically.CheckBox")) {

						@Override
						public void actionPerformed(ActionEvent e) {
							atlasStyler.setAutomaticPreview(jCheckboxPreview
									.isSelected());

							// Fire it directly when the checkbox has been
							// activated
							if (jCheckboxPreview.isSelected())
								atlasStyler.fireStyleChangedEvents();
						}
					});

			jCheckboxPreview.setSelected(atlasStyler.isAutomaticPreview());
			jCheckboxPreview
					.setToolTipText(AtlasStyler
							.R("AtlasStylerGUI.UpdatePreviewAutomatically.CheckBox.TT"));
		}
		return jCheckboxPreview;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonCancel() {
		CancelButton jButtonCancel = new CancelButton();

		jButtonCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cancelClose();
			}
		});
		return jButtonCancel;
	}

	/***************************************************************************
	 * Pressing Cancel or CLosing the Window fires a PROPERTY_CANCEL_AND_CLOSE
	 * Property Change Event and disposes the Dialog.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	@Override
	public void cancel() {
		atlasStyler.cancel();
	}

	@Override
	public boolean okClose() {
		// If not automatic update is enabled, we have to fire our
		// changes now!
		if (!atlasStyler.isAutomaticPreview()) {
			atlasStyler.fireStyleChangedEvents(true);
		}

		return super.okClose();
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonOk() {
		OkButton jButtonOk = new OkButton(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {

				okClose();
			}
		});
		return jButtonOk;
	}

	/**
	 * Returns the {@link AtlasStyler} backing this GUI
	 */
	public AtlasStyler getAtlasStyler() {
		return atlasStyler;
	}

	@Override
	public void dispose() {
		if (isDisposed)
			return;
		tabbedPane.dispose();
		if (previewMapPane != null) {
			// Not really needed, but anyways..
			previewMapPane
					.removeMapPaneListener(scaleChangeListenerInPreviewXMapPanePassedToInternalListeners);
		}
		super.dispose();
	}

	/**
	 * @return An {@link XMapPane} (or better an interface that has getScale,
	 *         addScaleChangelIstener and setSCale) which is a preview for what
	 *         is happening in
	 */
	public XMapPane getPreviewMapPane() {
		if (previewMapPane == null)
			return null;
		return previewMapPane;
	}

	/**
	 * If <code>true</code> the GUI hides the more compilcated parts.
	 */
	public void setEasy(boolean easy) {
		this.easy = easy;
	}

	public boolean isEasy() {
		return easy;
	}

}
