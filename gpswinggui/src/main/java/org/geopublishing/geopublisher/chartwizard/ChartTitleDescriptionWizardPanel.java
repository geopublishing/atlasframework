/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.chartwizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JDialog;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.geotools.data.FeatureSource;
import org.netbeans.spi.wizard.WizardPage;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import schmitzm.jfree.JFreeChartUtil;
import schmitzm.jfree.chart.style.ChartType;
import skrueger.geotools.AttributeMetadataMap;
import skrueger.i8n.I8NUtil;
import skrueger.i8n.Translation;
import skrueger.swing.TranslationEditJPanel;

/**
 * This {@link WizardPage} allows to define Title and Description/sub-title of
 * the chart in form of {@link TranslationEditJPanel}s
 * 
 * @author Stefan A. Tzeggai
 */

public class ChartTitleDescriptionWizardPanel extends WizardPage {
	private static final String validationTitleFailedMsg = GeopublisherGUI
			.R("ChartWizardPanel.titleAndDescription.ValidationError.Title");
	final static Logger LOGGER = Logger
			.getLogger(ChartTitleDescriptionWizardPanel.class);

	/* The short description label that appears on the left side of the wizard */
	static String desc = GeopublisherGUI.R("ChartWizardPanel.titleAndDescription");

	TranslationEditJPanel titleTranslationEditPanel;

	TranslationEditJPanel descTranslationEditPanel;

	private ChartType chartType;

	private void initGUI() {
		setLayout(new MigLayout("wrap 1"));

		add(getTitleTranslationEditPanel(), "sgx");
		add(getDescTranslationEditPanel(), "sgx");
	}

	public TranslationEditJPanel getTitleTranslationEditPanel() {
		if (titleTranslationEditPanel == null) {

			Translation translation = (Translation) getWizardData(ChartWizard.TITLE);
			if (translation == null) {
				// Create a default translation for which describes roughly what
				// is shown
				translation = new Translation();

				final String resourceKey = ChartType.class.getSimpleName()
						+ "_" + chartType.toString() + ".Title";

				// TODO This doesn't work :-( I need martins help.
				for (final String lang : getLanguages()) {
					final Locale locale = new Locale(lang);
					final String chartTypetitle = JFreeChartUtil.RESOURCE
							.getString(resourceKey, locale);

					translation.put(lang, chartTypetitle);
				}
			}

			titleTranslationEditPanel = new TranslationEditJPanel(translation,
					getLanguages());
			titleTranslationEditPanel.setName(ChartWizard.TITLE);
		}
		titleTranslationEditPanel.setBorder(BorderFactory
				.createTitledBorder(GeopublisherGUI
						.R("ManageChartsForMapDialog.ColumnName.2")));
		return titleTranslationEditPanel;
	}

	@SuppressWarnings("unchecked")
	private List<String> getLanguages() {
		final List<String> wizardDataLanguages = (List<String>) getWizardData(ChartWizard.LANGUAGES);
		return wizardDataLanguages;
	}

	/**
	 * Not cached any more...
	 */
	public TranslationEditJPanel getDescTranslationEditPanel() {

		Translation translation = null;

		if (translation == null) {
			// Create a default translation which describes roughly what
			// is shown.
			translation = new Translation();

			final AttributeMetadataMap amdm = (AttributeMetadataMap) getWizardData(ChartWizard.ATTRIBUTEMETADATAMAP);
			final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = (FeatureSource<SimpleFeatureType, SimpleFeature>) getWizardData(ChartWizard.FEATURESOURCE);

			final String domainAttribName = (String) getWizardData(ChartWizard.ATTRIBUTE_
					+ "0");

			
//			final Translation domainTitleTranslation = ASUtil
//					.getAttributeMetadataFor(amdm, domainAttribName)
//					.getTitle();
			final Translation domainTitleTranslation = amdm.get(domainAttribName).getTitle();
			
			
			final Translation[] seriesTitleTranslations = new Translation[100];

			for (int i = 1; i < chartType.getMaxDimensions()
					|| chartType.getMaxDimensions() < 0; i++) {
				final String seriesAttribName = (String) getWizardData(ChartWizard.ATTRIBUTE_
						+ i);
				if (seriesAttribName == null)
					break;
//				seriesTitleTranslations[i - 1] = ASUtil
//						.getAttributeMetadataFor(amdm, seriesAttribName)
//						.getTitle();
				seriesTitleTranslations[i - 1] = amdm.get(seriesAttribName).getTitle();
			}

			for (final String lang : getLanguages()) {

				final String domainTitle = domainTitleTranslation.get(lang) != null ? domainTitleTranslation
						.get(lang)
						: domainAttribName;
				String descTranslated = domainTitle + " ";
				
				String firstSeriesTitle  = null;
				if (seriesTitleTranslations[0] != null) {
					firstSeriesTitle = seriesTitleTranslations[0]
							.get(lang) != null ? seriesTitleTranslations[0]
							.get(lang)
							: (String) getWizardData(ChartWizard.ATTRIBUTE_ + 1);
							
					if (chartType ==  ChartType.SCATTER) {
						descTranslated += "vs. " + firstSeriesTitle; // i8n
					} // else später
					
				}
				for (int i = 1; i < chartType.getMaxDimensions()
						|| chartType.getMaxDimensions() < 0; i++) {
					if (seriesTitleTranslations[i] == null)
						break;
					final String attribTitle = seriesTitleTranslations[i]
							.get(lang) != null ? seriesTitleTranslations[i]
							.get(lang)
							: (String) getWizardData(ChartWizard.ATTRIBUTE_
									+ (i + 1));
					descTranslated += ", " + attribTitle;
				}
				
				 if (chartType == ChartType.BAR) {
					 if (firstSeriesTitle != null)
						 descTranslated = GeopublisherGUI.R("ChartWizardPanel.DefaultBarChartSubTitle",firstSeriesTitle,descTranslated);
				} // scatter früher

				translation.put(lang, descTranslated);
			}
		}

		descTranslationEditPanel = new TranslationEditJPanel(translation,
				getLanguages());
		descTranslationEditPanel.setName(ChartWizard.DESC);
		// }
		descTranslationEditPanel.setBorder(BorderFactory
				.createTitledBorder(GeopublisherGUI
						.R("ManageChartsForMapDialog.ColumnName.3")));
		return descTranslationEditPanel;
	}

	@Override
	protected void renderingPage() {
		chartType = (ChartType) getWizardData(ChartWizard.CHARTTYPE);

		removeAll();
		initGUI();
	}

	public static String getDescription() {
		return desc;
	}

	@Override
	protected String validateContents(final Component component,
			final Object event) {

		if (I8NUtil.isEmpty(titleTranslationEditPanel.getTranslation()))
			return validationTitleFailedMsg;

		// if (I8NUtil.isEmpty(descTranslationEditPanel.getTranslation()))
		// return "Desc empty!"; // i8n

		return null;
	}

	@Override
	protected CustomComponentListener createCustomComponentListener() {
		return new CCL();
	}

	private static final class CCL extends CustomComponentListener implements
			ActionListener {
		private CustomComponentNotifier notifier;

		@Override
		public boolean accept(final Component c) {
			return c instanceof TranslationEditJPanel;
		}

		@Override
		public void startListeningTo(final Component c,
				final CustomComponentNotifier n) {
			notifier = n;
			((TranslationEditJPanel) c).addTranslationChangeListener(this);
		}

		@Override
		public void stopListeningTo(final Component c) {
			((TranslationEditJPanel) c).removeTranslationChangeListener(this);
		}

		@Override
		public Object valueFor(final Component c) {
			return ((TranslationEditJPanel) c).getTranslation();
		}

		public void actionPerformed(final ActionEvent e) {
			notifier.userInputReceived((Component) e.getSource(), e);
		}
	}

	public static void main(final String[] args) {
		final JDialog d = new JDialog();
		d.setContentPane(new ChartTitleDescriptionWizardPanel());
		d.pack();
		d.setVisible(true);
	}

}
