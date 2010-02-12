/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.sld.gui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geotools.styling.Symbolizer;

import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import skrueger.i8n.Translation;
import skrueger.sld.ASUtil;
import skrueger.sld.AtlasStyler;
import skrueger.sld.RuleChangeListener;
import skrueger.sld.RuleChangedEvent;
import skrueger.sld.SingleRuleList;
import skrueger.swing.TranslationAskJDialog;
import skrueger.swing.TranslationEditJPanel;

public class SingleSymbolGUI extends JPanel implements ClosableSubwindows {
	protected Logger LOGGER = ASUtil.createLogger(this);

	private static final long serialVersionUID = 1L;

	private JToggleButton jButtonSymbolSelector = null;

	private final SingleRuleList<? extends Symbolizer> singleSymbolRuleList;

	/**
	 * This is the default constructor
	 * 
	 * @param singleSymbolRuleList
	 * @param openWindows
	 */
	public SingleSymbolGUI(final SingleRuleList<?> singleSymbolRuleList) {
		super();
		if (singleSymbolRuleList == null)
			throw new IllegalStateException("may not be null");
		this.singleSymbolRuleList = singleSymbolRuleList;
		initialize();
		singleSymbolRuleList.fireEvents(new RuleChangedEvent("GUI selected",
				singleSymbolRuleList));
	}

	/**
	 * Adding a listener that will update the Button-Image when the rulelist has
	 * been altered *
	 */
	final RuleChangeListener listenToChangesInTheRulesToUpdateButton = new RuleChangeListener() {

		public void changed(RuleChangedEvent e) {
			jButtonSymbolSelector.setIcon(new ImageIcon(singleSymbolRuleList
					.getImage()));
		}

	};

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		JLabel jLabelSymbol = new JLabel(AtlasStyler
				.R("SingleSymbolGUI.Symbol.Label"));
		jLabelSymbol.setToolTipText(AtlasStyler.R("SingleSymbolGUI.Symbol.TT"));

		JLabel jLabelHeading = new JLabel(AtlasStyler
				.R("SingleSymbolGUI.Heading.Label"));
		jLabelHeading.setFont(jLabelHeading.getFont().deriveFont(
				AtlasStylerTabbedPane.HEADING_FONT_SIZE));

		this.setLayout(new MigLayout());
		this.add(jLabelHeading, "span 2, wrap");
		this.add(jLabelSymbol);
		this.add(getJButtonSymbol(), "wrap");

		JLabel jLabelTranslation = new JLabel(AtlasStyler
				.R("SingleSymbolGUI.Label.Label"));
		jLabelTranslation.setToolTipText(AtlasStyler
				.R("SingleSymbolGUI.Label.TT"));
		this.add(jLabelTranslation);
		this.add(getjLabelTranslationEdit(), "wrap");
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JToggleButton getJButtonSymbol() {
		if (jButtonSymbolSelector == null) {
			jButtonSymbolSelector = new JToggleButton();
			jButtonSymbolSelector.setToolTipText(AtlasStyler
					.R("SingleSymbolGUI.Symbol.TT"));

			singleSymbolRuleList
					.addListener(listenToChangesInTheRulesToUpdateButton);

			ImageIcon icon = new ImageIcon(singleSymbolRuleList.getImage());
			jButtonSymbolSelector.setAction(new AbstractAction("", icon) {

				private SymbolSelectorGUI symbolSelectorDialog;

				public void actionPerformed(ActionEvent e) {
					if (symbolSelectorDialog == null) {
						final SingleRuleList<?> backup = singleSymbolRuleList
								.clone(false);

						symbolSelectorDialog = new SymbolSelectorGUI(
								SingleSymbolGUI.this, null,
								// AtlasStyler.R("SymbolSelector.Title.OneForAll"),
								singleSymbolRuleList);

						symbolSelectorDialog
								.addPropertyChangeListener(new PropertyChangeListener() {

									public void propertyChange(
											PropertyChangeEvent evt) {

										/**
										 * Listen for CANCEL to copy back the
										 * backuped values
										 */
										if (evt
												.getPropertyName()
												.equals(
														SymbolSelectorGUI.PROPERTY_CANCEL_CHANGES)) {

											backup.copyTo(singleSymbolRuleList);
										}

										/** Listen for CLOSE* */
										if (evt
												.getPropertyName()
												.equals(
														SymbolSelectorGUI.PROPERTY_CLOSED)) {
											jButtonSymbolSelector
													.setSelected(false);

											symbolSelectorDialog.dispose();
											symbolSelectorDialog = null;
										}
									}

								});

						SwingUtil.setRelativeFramePosition(
								symbolSelectorDialog, SingleSymbolGUI.this, .5,
								.5);
					}

					symbolSelectorDialog.setVisible(jButtonSymbolSelector
							.isSelected());
				}

			});

			/**
			 * Change the Icon when the Rule changes
			 */
			singleSymbolRuleList.addListener(new RuleChangeListener() {

				public void changed(RuleChangedEvent e) {
					ImageIcon icon = new ImageIcon(singleSymbolRuleList
							.getImage());
					jButtonSymbolSelector.setIcon(icon);
				}

			});
		}
		return jButtonSymbolSelector;
	}

	/**
	 * This method initializes jPanel11
	 * 
	 * @return javax.swing.JPanel
	 */
	private JButton getjLabelTranslationEdit() {
		final JButton jLabelTranslationEdit = new JButton();
		jLabelTranslationEdit.setToolTipText(AtlasStyler
				.R("SingleSymbolGUI.Label.TT"));

		/*******************************************************************
		 * The Translation JLabel can be editited
		 */
		jLabelTranslationEdit.setAction(new AbstractAction() {

			private TranslationAskJDialog ask;

			public void actionPerformed(ActionEvent e) {

				String oldTitle = singleSymbolRuleList.getTitle();

				if (AtlasStyler.getLanguageMode() == AtlasStyler.LANGUAGE_MODE.ATLAS_MULTILANGUAGE) {
					/*******************************************************
					 * AtlasStyler.LANGUAGE_MODE.ATLAS_MULTILANGUAGE
					 */
					final Translation translation = new Translation();
					translation.fromOneLine(oldTitle);

					if (ask == null) {
						TranslationEditJPanel transLabel = new TranslationEditJPanel(
								AtlasStyler.R("SingleSymbolGUI.EnterLabel"),
								translation, AtlasStyler.getLanguages());

						ask = new TranslationAskJDialog(SingleSymbolGUI.this,
								transLabel);
						ask
								.addPropertyChangeListener(new PropertyChangeListener() {

									public void propertyChange(
											PropertyChangeEvent evt) {
										if (evt
												.getPropertyName()
												.equals(
														TranslationAskJDialog.PROPERTY_CANCEL_AND_CLOSE)) {
											ask = null;
										}
										if (evt
												.getPropertyName()
												.equals(
														TranslationAskJDialog.PROPERTY_APPLY_AND_CLOSE)) {

											singleSymbolRuleList
													.setTitle(translation);
											jLabelTranslationEdit
													.setText(translation
															.toString());
										}
										ask = null;
									}

								});

					}
					SwingUtil.setRelativeFramePosition(ask,
							SingleSymbolGUI.this, .5, .5);
					ask.setVisible(true);

				} else {
					/*******************************************************
					 * AtlasStyler.LANGUAGE_MODE.OGC_SINGLELANGUAGE
					 */
					String newTitle = ASUtil.askForString(SingleSymbolGUI.this,
							oldTitle, null);
					if (newTitle != null) {
						singleSymbolRuleList.setTitle(newTitle);
						jLabelTranslationEdit.setText(newTitle);
					}
				}
			}
		});

		Translation translation = new Translation();
		try {
			String firstTitle = singleSymbolRuleList.getTitle();
			translation.fromOneLine(firstTitle);
			jLabelTranslationEdit.setText(translation.toString());
		} catch (Exception e) {
			jLabelTranslationEdit.setText("interpretation error");
		}

		return jLabelTranslationEdit;
	}

	@Override
	public void dispose() {
		// Not needed because its a weak listener list, but can't be bad:
		singleSymbolRuleList
				.removeListener(listenToChangesInTheRulesToUpdateButton);

	}

}
