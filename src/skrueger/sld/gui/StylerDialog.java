/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasStyler SLD/SE Editor application - A Java Swing-based GUI to create OGC Styled Layer Descriptor (SLD 1.0) / OGC Symbology Encoding 1.1 (SE) XML files.
 * http://www.geopublishing.org
 * 
 * AtlasStyler SLD/SE Editor is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.sld.gui;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geotools.data.FeatureSource;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import skrueger.i8n.Translation;
import skrueger.sld.ASUtil;
import skrueger.sld.AtlasStyler;
import skrueger.swing.CancelButton;
import skrueger.swing.CancellableDialogAdapter;
import skrueger.swing.OkButton;

/**
 * This {@link JDialog} can be used to edit a {@link Style}.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */
public class StylerDialog extends CancellableDialogAdapter {
	protected Logger LOGGER = ASUtil.createLogger(this);

	// public final static String PROPERTY_CANCEL_AND_CLOSE =
	// "AtlasStylerDialog_CANCEL";
	//
	// public final static String PROPERTY_CLOSE_AND_APPLY =
	// "AtlasStylerDialog_APPLY";

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	final private AtlasStyler atlasStyler;

	private final AtlasStylerTabbedPane tabbedPane;

	private JCheckBox jCheckboxPreview;

	private JButton jButtonUpdatePreview;

	/**
	 * Creates an AtlasStyler {@link JDialog} which allows to create a
	 * SymbologyEncoding(SE) and StyledLayerDescriptor(SLD) for a
	 * {@link FeatureSource}.
	 * 
	 * @param owner
	 *            <code>null</code> or a {@link Window} component that shall be
	 *            used as a parent window for the {@link StylerDialog}.
	 * @param featureSource
	 */
	public StylerDialog(Component owner, AtlasStyler atlasStyler) {
		super(SwingUtil.getParentWindow(owner));
		this.atlasStyler = atlasStyler;
		this.tabbedPane = new AtlasStylerTabbedPane(atlasStyler);
		initialize();

		pack();

		/**
		 * Position left outside of the actual parent frame
		 */
		SwingUtil.setRelativeFramePosition(this, owner, SwingUtil.BOUNDS_OUTER,
				SwingUtil.WEST);
	}

	/**
	 * This method initializes the {@link JDialog}.
	 * 
	 * @throws IOException
	 */
	private void initialize() {

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
			jContentPane = new JPanel(new MigLayout("nogrid"));
			jContentPane.add(tabbedPane, "grow, wrap");

			jContentPane.add(getJCheckboxPreviewl(), "left");
			jContentPane.add(getJButtonUpdatePreview(), "left");

			jContentPane.add(getJButtonOk(), "tag ok");
			jContentPane.add(getJButtonCancel(), "tag cancel");
		}
		return jContentPane;
	}

	private JButton getJButtonUpdatePreview() {
		if (jButtonUpdatePreview == null) {
			jButtonUpdatePreview = new JButton(new AbstractAction(AtlasStyler
					.R("AtlasStylerGUI.UpdatePreview.Button")) {

				public void actionPerformed(ActionEvent e) {
					atlasStyler.fireStyleChangedEvents(true);
				}

			});
			jButtonUpdatePreview.setEnabled(!atlasStyler.isAutomaticPreview());
			getJCheckboxPreviewl().getModel().addChangeListener(
					new ChangeListener() {

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
			jCheckboxPreview = new JCheckBox(new AbstractAction(AtlasStyler
					.R("AtlasStylerGUI.UpdatePreviewAutomatically.CheckBox")) {

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
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
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
		if (isDisposed)return;
		tabbedPane.dispose();		
		super.dispose();
	}
	
}
