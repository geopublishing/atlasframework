package skrueger.sld.gui;

import java.awt.Dimension;

import javax.swing.JButton;

import skrueger.sld.AtlasStyler;
import skrueger.sld.RuleChangeListener;
import skrueger.sld.RuleChangedEvent;
import skrueger.sld.SingleRuleList;

/**
 * This extension of the {@link SymbolButton} class automatically registers a {@link RuleChangeListener} to the {@link SingleRuleList}
 */
public class UpdatingSymbolButton extends SymbolButton {
 
	private RuleChangeListener listener = null;

	/**
	 * Creates a {@link JButton} with a preview image of the given
	 * {@link SingleRuleList}. The button image will have the default size
	 * {@link AtlasStyler#DEFAULT_SYMBOL_PREVIEW_SIZE}.
	 */
	public UpdatingSymbolButton(SingleRuleList singleSymbolRuleList) {
		this(singleSymbolRuleList, AtlasStyler.DEFAULT_SYMBOL_PREVIEW_SIZE);
	}

	/**
	 * Creates a {@link JButton} with a preview image of the given
	 * {@link SingleRuleList}. The button image will have the given dimensions.
	 */
	public UpdatingSymbolButton(final SingleRuleList singleSymbolRuleList, Dimension size) {
		super(singleSymbolRuleList, size);
		
		listener = new RuleChangeListener() {
			
			@Override
			public void changed(RuleChangedEvent e) {
				setSingleSymbolRuleList(singleSymbolRuleList);
			}
		};
		
		if (singleSymbolRuleList != null)
			singleSymbolRuleList.addListener(listener);
	}

	@Override
	public void setSingleSymbolRuleList(SingleRuleList singleSymbolRuleList) {
		super.setSingleSymbolRuleList(singleSymbolRuleList);
		if (singleSymbolRuleList != null)
			singleSymbolRuleList.addListener(listener);
	}
}
