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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.rulesLists.SingleRuleList;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geotools.styling.Symbolizer;

import de.schmitzm.i18n.Translation;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.SwingUtil;
import de.schmitzm.swing.TranslationAskJDialog;
import de.schmitzm.swing.TranslationEditJPanel;

public class SingleSymbolGUI extends AbstractRulesListGui<SingleRuleList<? extends Symbolizer>> implements
		ClosableSubwindows {
	protected static final Logger LOGGER = LangUtil
			.createLogger(SingleSymbolGUI.class);

	private EditSymbolButton jButtonSymbolSelector = null;

	/**
	 * This is the default constructor
	 * 
	 * @param singleSymbolRuleList
	 * @param openWindows
	 */
	public SingleSymbolGUI(AtlasStylerVector asv, final SingleRuleList<?> singleSymbolRuleList) {
		super(singleSymbolRuleList);
		this.asv = asv;
		if (singleSymbolRuleList == null)
			throw new IllegalStateException(
					"A GUI can not be created if no RuleList is provided.");
		initialize();
	}

	/**
	 * Adding a listener that will update the Button-Image when the rulelist has
	 * been altered *
	 */
	final RuleChangeListener listenToChangesInTheRulesToUpdateButton = new RuleChangeListener() {

		@Override
		public void changed(RuleChangedEvent e) {
			jButtonSymbolSelector.setIcon(new ImageIcon(rulesList
					.getImage()));
		}

	};

	final protected AtlasStylerVector asv;

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		JLabel jLabelSymbol = new JLabel(
				ASUtil.R("SingleSymbolGUI.Symbol.Label"));
		jLabelSymbol.setToolTipText(ASUtil
				.R("SingleSymbolGUI.Symbol.TT"));

		JLabel jLabelHeading = new JLabel(
				ASUtil.R("SingleSymbolGUI.Heading.Label"));
		jLabelHeading.setFont(jLabelHeading.getFont().deriveFont(
				AVSwingUtil.HEADING_FONT_SIZE));

		this.setLayout(new MigLayout());
		this.add(jLabelHeading, "span 2, wrap");
		this.add(jLabelSymbol);
		this.add(getJButtonSymbol(), "wrap");

		JLabel jLabelTranslation = new JLabel(
				ASUtil.R("SingleSymbolGUI.Label.Label"));
		jLabelTranslation.setToolTipText(ASUtil
				.R("SingleSymbolGUI.Label.TT"));
		this.add(jLabelTranslation);
		this.add(getjLabelTranslationEdit(), "wrap");
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private EditSymbolButton getJButtonSymbol() {
		if (jButtonSymbolSelector == null) {
			jButtonSymbolSelector = new EditSymbolButton(asv, rulesList);
			jButtonSymbolSelector.setToolTipText(ASUtil
					.R("SingleSymbolGUI.Symbol.TT"));
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
		jLabelTranslationEdit.setToolTipText(AtlasStylerVector
				.R("SingleSymbolGUI.Label.TT"));

		/*******************************************************************
		 * The Translation JLabel can be edited
		 */
		jLabelTranslationEdit.setAction(new AbstractAction() {

			private TranslationAskJDialog ask;

			@Override
			public void actionPerformed(ActionEvent e) {

				String oldTitle = rulesList.getLabel();

				if (AtlasStylerVector.getLanguageMode() == AtlasStylerVector.LANGUAGE_MODE.ATLAS_MULTILANGUAGE) {
					/*******************************************************
					 * AtlasStyler.LANGUAGE_MODE.ATLAS_MULTILANGUAGE
					 */
					final Translation translation = new Translation();
					translation.fromOneLine(oldTitle);

					if (ask == null) {
						TranslationEditJPanel transLabel = new TranslationEditJPanel(
								AtlasStylerVector
										.R("SingleSymbolGUI.EnterLabel"),
								translation, AtlasStylerVector.getLanguages());

						ask = new TranslationAskJDialog(SingleSymbolGUI.this,
								transLabel);
						ask.addPropertyChangeListener(new PropertyChangeListener() {

							@Override
							public void propertyChange(PropertyChangeEvent evt) {
								if (evt.getPropertyName()
										.equals(TranslationAskJDialog.PROPERTY_CANCEL_AND_CLOSE)) {
									ask = null;
								}
								if (evt.getPropertyName()
										.equals(TranslationAskJDialog.PROPERTY_APPLY_AND_CLOSE)) {

									rulesList
											.setRuleTitle(translation);
									jLabelTranslationEdit.setText(translation
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
						rulesList.setLabel(newTitle);
						jLabelTranslationEdit.setText(newTitle);
					}
				}
			}
		});

		Translation translation = new Translation();
		try {
			String firstTitle = rulesList.getLabel();
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
		rulesList
				.removeListener(listenToChangesInTheRulesToUpdateButton);
		
		super.dispose();
	}

}
