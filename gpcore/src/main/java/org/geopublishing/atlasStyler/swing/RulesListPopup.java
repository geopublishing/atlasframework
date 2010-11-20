package org.geopublishing.atlasStyler.swing;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.geopublishing.atlasStyler.AbstractRulesList;
import org.geopublishing.atlasViewer.swing.Icons;
import org.opengis.filter.Filter;

public class RulesListPopup extends JPopupMenu {

	static Filter filterCopied = null;
	static final Double[] scalesCopied = new Double[2];

	public RulesListPopup(final AbstractRulesList rulesList) {
		JMenuItem header = new JMenuItem(rulesList.getTitle());
		header.setEnabled(false);
		add(header);
		addSeparator();

		addFilterMenuItems(rulesList);
		addScaleMenuItems(rulesList);

		add("Copy SLD/XML (not yet)");
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
		// Copy Filter
		final Double minScale = rulesList.getMinScaleDenominator();
		final Double maxScale = rulesList.getMaxScaleDenominator();
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
					rulesList.setMinScaleDenominator(Double.MIN_VALUE);
					rulesList.setMaxScaleDenominator(Double.MAX_VALUE);
				}
			});
		}
	}

}
