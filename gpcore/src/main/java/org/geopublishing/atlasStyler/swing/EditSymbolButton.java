package org.geopublishing.atlasStyler.swing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.rulesLists.SingleRuleList;
import org.geotools.styling.Symbolizer;

/**
 * This extension of the {@link UpdatingSymbolButton} will open a dialog to edit
 * the symbol when clicked.
 */
public class EditSymbolButton extends UpdatingSymbolButton {

	protected SingleRuleList<? extends Symbolizer> backup;

	/**
	 * Creates a {@link JButton} with a preview image of the given
	 * {@link SingleRuleList}. The button image will have the default size
	 * {@link AtlasStylerVector#DEFAULT_SYMBOL_PREVIEW_SIZE}.
	 */
	public EditSymbolButton(AtlasStylerVector asv, SingleRuleList singleSymbolRuleList) {
		this(asv, singleSymbolRuleList,
				AtlasStylerVector.DEFAULT_SYMBOL_PREVIEW_SIZE);
	}

	/**
	 * Listens to close/cancel of any {@link SymbolSelectorGUI}.
	 */
	PropertyChangeListener listenCancelOkForSelectionInSymbolSelectionGUI = new PropertyChangeListener() {

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {

			if (evt.getPropertyName().equals(
					SymbolSelectorGUI.PROPERTY_CANCEL_CHANGES)) {

				backup.copyTo(getSingleSymbolRuleList());
			}

			if (evt.getPropertyName().equals(SymbolSelectorGUI.PROPERTY_CLOSED)) {

			}
		}
	};

	/**
	 * Creates a {@link JButton} with a preview image of the given
	 * {@link SingleRuleList}. The button image will have the given dimensions.
	 */
	public EditSymbolButton(final AtlasStylerVector asv, final SingleRuleList singleSymbolRuleList,
			Dimension size) {
		super(asv, singleSymbolRuleList, size);

		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {

				final SingleRuleList<? extends Symbolizer> template = getSingleSymbolRuleList();

				if (template == null)
					return;

				backup = template.copy();

				final SymbolSelectorGUI gui = new SymbolSelectorGUI(
						EditSymbolButton.this, asv,
						ASUtil.R("SymbolSelector.ForTemplate.Title"), template);

				gui.addPropertyChangeListener(listenCancelOkForSelectionInSymbolSelectionGUI);

				gui.setModal(true);
				gui.setVisible(true);
			}

		});

	}
}
