package org.geopublishing.atlasStyler.swing;

import java.awt.event.ActionEvent;
import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.rulesLists.RulesListInterface;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geotools.styling.FeatureTypeStyle;
import org.opengis.filter.Filter;

import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.swingworker.AtlasStatusDialog;
import de.schmitzm.swing.swingworker.AtlasSwingWorker;

public class RulesListPopup extends JPopupMenu {

	static Filter filterCopied = null;
	static final Double[] scalesCopied = new Double[2];
	private final StylerDialog asd;

	public RulesListPopup(final RulesListInterface rulesList, StylerDialog asd) {
		this.asd = asd;

		// Write the name if the layer as a header of the menu:
		JMenuItem header = new JMenuItem(rulesList.getTitle());

		header.setEnabled(false);

		add(header);

		if (!asd.isEasy()) {
			addSeparator();
			addScaleMenuItems(rulesList);

			addSeparator();
			addFilterMenuItems(rulesList);

			// if (AtlasStyler.getLanguageMode() ==
			// LANGUAGE_MODE.OGC_SINGLELANGUAGE) {
			// Only in AtlasStyler do we offer to access the XML directly.
			addSeparator();
			addXMLMenuItems(rulesList);
			// }
		}
	}

	private void addXMLMenuItems(final RulesListInterface rulesList) {

		add(new AbstractAction(ASUtil.R("RulesListPopup.copyXML"),
				Icons.ICON_XML) {

			@Override
			public void actionPerformed(final ActionEvent e) {
				// Copy XML to ClipBoard
				FeatureTypeStyle fts = rulesList.getFTS();
				try {
					String xmlString = StylingUtil.toXMLString(fts);
					LangUtil.copyToClipboard(xmlString);
					JOptionPane.showMessageDialog(RulesListPopup.this,
							ASUtil.R("RulesListPopup.copyXML.done",
									xmlString.length()));
				} catch (Exception ee) {
					ExceptionDialog.show(RulesListPopup.this, ee);
				}
			}
		});
		
//		add(new AbstractAction("Paste from Clipboard",
//				Icons.ICON_XML) {
//
//			@Override
//			public void actionPerformed(final ActionEvent e) {
//				// Copy XML from ClipBoard
//				FeatureTypeStyle fts = rulesList.getFTS();
//				try {
//					String xmlString = LangUtil.pasteFromClipboard();
//					
//					JOptionPane.showMessageDialog(RulesListPopup.this,
//							ASUtil.R("RulesListPopup.copyXML.done",
//									xmlString.length()));
//				} catch (Exception ee) {
//					ExceptionDialog.show(RulesListPopup.this, ee);
//				}
//			}
//		});

	}

	private void addFilterMenuItems(final RulesListInterface rulesList) {
		// Copy Filter
		final Filter rlFilter = rulesList.getRlFilter();
		if (rlFilter != null) {

			add(new AbstractAction(ASUtil.R("RulesListPopup.CopyFilter"),
					Icons.ICON_FILTER) {

				@Override
				public void actionPerformed(final ActionEvent e) {
					filterCopied = rlFilter;
				}
			});
		}

		// Insert Filter
		if (filterCopied != null) {
			add(new AbstractAction(ASUtil.R(
					"RulesListPopup.InsertFilterIntoRulesList",
					rulesList.getTitle()), Icons.ICON_FILTER) {

				@Override
				public void actionPerformed(final ActionEvent e) {
					rulesList.setRlFilter(filterCopied);
				}
			});
		}

		if (rlFilter != null) {
			add(new AbstractAction(ASUtil.R("RulesListPopup.RemoveFilter"),
					Icons.ICON_REMOVE_FILTER) {

				@Override
				public void actionPerformed(final ActionEvent e) {
					rulesList.setRlFilter(null);
				}
			});
		}
	}

	private void addScaleMenuItems(final RulesListInterface rulesList) {
		if (asd.getPreviewMapPane() != null) {

			final double previewScaleDenominator = asd.getPreviewMapPane()
					.getScaleDenominator();
			final String pScaleFormatted = NumberFormat.getIntegerInstance()
					.format(previewScaleDenominator);

			add(new AbstractAction(ASUtil.R(
					"RulesListPopup.UseScaleOfPreviewForMin", pScaleFormatted),
					Icons.ICON_MINSCALE_SMALL) {

				@Override
				public void actionPerformed(ActionEvent e) {
					rulesList.setMinScaleDenominator(previewScaleDenominator);
				}
			});

			add(new AbstractAction(ASUtil.R(
					"RulesListPopup.UseScaleOfPreviewForMax", pScaleFormatted),
					Icons.ICON_MAXSCALE_SMALL) {

				@Override
				public void actionPerformed(ActionEvent e) {
					rulesList.setMaxScaleDenominator(previewScaleDenominator);
				}
			});
		}

		final Double minScale = rulesList.getMinScaleDenominator();
		final Double maxScale = rulesList.getMaxScaleDenominator();

		if (minScale > 0)
			add(new AbstractAction(
					ASUtil.R("RulesListPopup.ZoomPreviewToMinPlus1"),
					Icons.ICON_MINSCALE_SMALL) {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (asd.getPreviewMapPane() != null)
						asd.getPreviewMapPane().zoomToScaleDenominator(
								rulesList.getMinScaleDenominator() + 1);
				}
			});

		add(new AbstractAction(
				ASUtil.R("RulesListPopup.ZoomPreviewToMaxMinus1"),
				Icons.ICON_MAXSCALE_SMALL) {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (asd.getPreviewMapPane() != null)
					asd.getPreviewMapPane().zoomToScaleDenominator(
							rulesList.getMaxScaleDenominator() - 1);
			}
		});

		add(new AbstractAction(ASUtil.R("RulesListPopup.CopyMinMaxScale"),
				Icons.ICON_MINMAXSCALE_SMALL) {

			@Override
			public void actionPerformed(final ActionEvent e) {
				scalesCopied[0] = minScale;
				scalesCopied[1] = maxScale;
			}
		});

		if (scalesCopied[0] != null && scalesCopied[1] != null) {
			add(new AbstractAction(ASUtil.R(
					"RulesListPopup.InsertMinMaxScaleIntoRulesList",
					rulesList.getTitle()), Icons.ICON_MINMAXSCALE_SMALL) {

				@Override
				public void actionPerformed(final ActionEvent e) {
					rulesList.setMinScaleDenominator(scalesCopied[0]);
					rulesList.setMaxScaleDenominator(scalesCopied[1]);
				}
			});
		}

		if (minScale != null) {
			add(new AbstractAction(
					ASUtil.R("RulesListPopup.ResetMinMaxScaleToZeroAndInfinite"),
					Icons.ICON_MINMAXSCALE_SMALL) {

				@Override
				public void actionPerformed(final ActionEvent e) {
					rulesList.setMinScaleDenominator(0.0);
					rulesList.setMaxScaleDenominator(Double.MAX_VALUE);
				}
			});
		}

		if (!asd.isEasy() && asd.isVector())
			add(new AbstractAction(ASUtil.R("RulesListPopup.GuessMaxScale"),
					Icons.ICON_MINMAXSCALE_SMALL) {

				@Override
				public void actionPerformed(final ActionEvent e) {

					// try {

					final StyledFeaturesInterface<?> styledFeatures = asd
							.getAtlasStylerVector().getStyledFeatures();

					AtlasStatusDialog waitDialog = new AtlasStatusDialog(
							RulesListPopup.this);
					new AtlasSwingWorker<Void>(waitDialog) {

						@Override
						protected Void doInBackground() throws Exception {
							Double calcAvgNN = FeatureUtil
									.calcAvgNN(styledFeatures);

							double maxScaleDenominator = StylingUtil
									.getMaxScaleDenominator(calcAvgNN,
											styledFeatures.getGeometryForm());

							rulesList.setMinScaleDenominator(0.);
							rulesList
									.setMaxScaleDenominator(maxScaleDenominator);

							return null;
						}
					}.executeModalNoEx();

				}
			});
	}
}
