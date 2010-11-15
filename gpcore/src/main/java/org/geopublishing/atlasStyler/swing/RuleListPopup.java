package org.geopublishing.atlasStyler.swing;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.geopublishing.atlasStyler.AbstractRulesList;
import org.geopublishing.atlasViewer.swing.Icons;
import org.opengis.filter.Filter;

public class RuleListPopup extends JPopupMenu {

	static Filter filterCopied = null;

	public RuleListPopup(final AbstractRulesList rulesList) {
		JMenuItem header = new JMenuItem(rulesList.getTitle());
		header.setEnabled(false);
		add(header);
		addSeparator();

		// Copy Filter
		final Filter rlFilter = rulesList.getRlFilter();
		if (rlFilter != null) {

			add(new AbstractAction("Copy filter", Icons.ICON_FILTER) {
				// i8n

				@Override
				public void actionPerformed(ActionEvent e) {
					filterCopied = rlFilter;
				}
			});
		}

		// Insert Filter
		if (filterCopied != null) {
			add(new AbstractAction("<html>Insert filter into <em>"
					+ rulesList.getTitle() + "</em></thml>", Icons.ICON_FILTER) { // i8n

				@Override
				public void actionPerformed(ActionEvent e) {
					rulesList.setRlFilter(filterCopied);
				}
			});
		}

		if (rlFilter != null) {
			add(new AbstractAction("remove filter", Icons.ICON_REMOVE_FILTER) {

				@Override
				public void actionPerformed(ActionEvent e) {
					rulesList.setRlFilter(null);
				}
			});
		}
		add("Copy SLD/XML");
	}

}
