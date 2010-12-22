package org.geopublishing.atlasStyler.swing;

import java.awt.event.ActionEvent;
import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.geopublishing.atlasStyler.AbstractRulesList;
import org.geopublishing.atlasViewer.swing.Icons;
import org.opengis.filter.Filter;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.styling.StylingUtil;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.swing.swingworker.AtlasStatusDialog;
import skrueger.swing.swingworker.AtlasSwingWorker;

public class RulesListPopup extends JPopupMenu {

	static Filter filterCopied = null;
	static final Double[] scalesCopied = new Double[2];
	private final StylerDialog asd;

	public RulesListPopup(final AbstractRulesList rulesList, StylerDialog asd) {
		this.asd = asd;
		JMenuItem header = new JMenuItem(rulesList.getTitle());
		header.setEnabled(false);
		add(header);

		addSeparator();
		addScaleMenuItems(rulesList);

		addSeparator();
		addFilterMenuItems(rulesList);

		addSeparator();
		add("Copy SLD/XML to clipboard (not yet)");
	}

	private void addFilterMenuItems(final AbstractRulesList rulesList) {
		// Copy Filter
		final Filter rlFilter = rulesList.getRlFilter();
		if (rlFilter != null) {

			add(new AbstractAction("Copy filter", Icons.ICON_FILTER) {
				// i8n

				@Override
				public void actionPerformed(final ActionEvent e) {
					filterCopied = rlFilter;
				}
			});
		}

		// Insert Filter
		if (filterCopied != null) {
			add(new AbstractAction("<html>Insert filter into <em>"
					+ rulesList.getTitle() + "</em></thml>", Icons.ICON_FILTER) { // i8n

				@Override
				public void actionPerformed(final ActionEvent e) {
					rulesList.setRlFilter(filterCopied);
				}
			});
		}

		if (rlFilter != null) {
			add(new AbstractAction("remove filter", Icons.ICON_REMOVE_FILTER) {

				@Override
				public void actionPerformed(final ActionEvent e) {
					rulesList.setRlFilter(null);
				}
			});
		}
	}

	private void addScaleMenuItems(final AbstractRulesList rulesList) {
		if (asd.getPreviewMapPane() != null) {

			final double previewScaleDenominator = asd.getPreviewMapPane()
					.getScaleDenominator();
			add(new AbstractAction("<html>Use preview scale ("
					+ NumberFormat.getIntegerInstance().format(
							previewScaleDenominator)
					+ ") for <em>MIN</em></html>") {

				@Override
				public void actionPerformed(ActionEvent e) {
					rulesList.setMinScaleDenominator(previewScaleDenominator);
				}
			});

			add(new AbstractAction("<html>Use preview scale ("
					+ NumberFormat.getIntegerInstance().format(
							previewScaleDenominator)
					+ ") for <em>MAX</em></html>") {

				@Override
				public void actionPerformed(ActionEvent e) {
					rulesList.setMaxScaleDenominator(previewScaleDenominator);
				}
			});
		}

		final Double minScale = rulesList.getMinScaleDenominator();
		final Double maxScale = rulesList.getMaxScaleDenominator();

		add(new AbstractAction(
				"<html>Zoom preview to <em>MIN scale</em>+1</html>") {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (asd.getPreviewMapPane() != null)
					asd.getPreviewMapPane().zoomToScaleDenominator(
							rulesList.getMinScaleDenominator() + 1);
			}
		});
		add(new AbstractAction(
				"<html>Zoom preview to <em>MAX scale</em>-1</html>") {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (asd.getPreviewMapPane() != null)
					asd.getPreviewMapPane().zoomToScaleDenominator(
							rulesList.getMaxScaleDenominator() - 1);
			}
		});

		add(new AbstractAction("Copy min/max scale",
				Icons.ICON_MINMAXSCALE_SMALL) {

			// i8n

			@Override
			public void actionPerformed(final ActionEvent e) {
				scalesCopied[0] = minScale;
				scalesCopied[1] = maxScale;
			}
		});

		if (scalesCopied[0] != null && scalesCopied[1] != null) {
			add(new AbstractAction("<html>Insert min/max scales into <em>"
					+ rulesList.getTitle() + "</em></thml>",
					Icons.ICON_MINMAXSCALE_SMALL) { // i8n

				@Override
				public void actionPerformed(final ActionEvent e) {
					rulesList.setMinScaleDenominator(scalesCopied[0]);
					rulesList.setMaxScaleDenominator(scalesCopied[1]);
				}
			});
		}

		if (minScale != null) {
			add(new AbstractAction("Reset min/max scales",
					Icons.ICON_MINMAXSCALE_SMALL) {

				@Override
				public void actionPerformed(final ActionEvent e) {
					rulesList.setMinScaleDenominator(0.0);
					rulesList.setMaxScaleDenominator(Double.MAX_VALUE);
				}
			});
		}

		add(new AbstractAction("Guess max-scale", Icons.ICON_MINMAXSCALE_SMALL) {

			@Override
			public void actionPerformed(final ActionEvent e) {

				// try {

				final StyledFeaturesInterface<?> styledFeatures = asd
						.getAtlasStyler().getStyledFeatures();

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
						rulesList.setMaxScaleDenominator(maxScaleDenominator);

						return null;
					}
				}.executeModalNoEx();

				// } catch (IOException e1) {
				// ExceptionDialog.show(RulesListPopup.this, e1);
				// }

			}
		});

	}
}
