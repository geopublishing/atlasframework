package org.geopublishing.atlasStyler.swing;

import java.awt.Dimension;

import javax.swing.JButton;

import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.rulesLists.SingleRuleList;

/**
 * This extension of the {@link SymbolButton} class automatically registers a
 * {@link RuleChangeListener} to the {@link SingleRuleList}
 */
public class UpdatingSymbolButton extends SymbolButton {

	private RuleChangeListener listener;

	/**
	 * Creates a {@link JButton} with a preview image of the given
	 * {@link SingleRuleList}. The button image will have the default size
	 * {@link AtlasStylerVector#DEFAULT_SYMBOL_PREVIEW_SIZE}.
	 */
	public UpdatingSymbolButton(final AtlasStylerVector asv, SingleRuleList singleSymbolRuleList) {
		this(asv, singleSymbolRuleList,
				AtlasStylerVector.DEFAULT_SYMBOL_PREVIEW_SIZE);
	}

	/**
	 * Creates a {@link JButton} with a preview image of the given
	 * {@link SingleRuleList}. The button image will have the given dimensions.
	 */
	public UpdatingSymbolButton(final AtlasStylerVector asv, final SingleRuleList singleSymbolRuleList,
			Dimension size) {
		super(asv, singleSymbolRuleList, size);
		if (singleSymbolRuleList != null)
			singleSymbolRuleList.addListener(listener);
	}

	@Override
	public void setSingleSymbolRuleList(
			final SingleRuleList singleSymbolRuleList) {
		super.setSingleSymbolRuleList(singleSymbolRuleList);
		if (singleSymbolRuleList != null) {
			if (listener == null) {
				listener = new RuleChangeListener() {

					@Override
					public void changed(RuleChangedEvent e) {
						setSingleSymbolRuleList(singleSymbolRuleList);
					}
				};
			}
			// If we add the same Listener again, thats not a problem. It's a
			// WeakHashSet we are adding it to.
			singleSymbolRuleList.addListener(listener);
		}
	}
}
