package org.geopublishing.atlasStyler.swing;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.rulesLists.AbstractRulesList;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesListColormap;
import org.geopublishing.atlasStyler.rulesLists.RulesListInterface;
import org.geopublishing.atlasViewer.swing.Icons;

import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.SmallButton;
import de.schmitzm.swing.SwingUtil;

/**
 * Parent of a Swing GUIs that are placed on the right side of
 * {@link AtlasStylerPane} and represent an {@link AbstractRulesList}.
 */
abstract public class AbstractRulesListGui<RLT extends RulesListInterface>
		extends JPanel implements ClosableSubwindows {

	public AbstractRulesListGui(RLT rulesList) {
		listenerEnableDisableGUIwhenRLenabledDisabled = new RuleChangeListener_GuiEnabledDisabled(
				this, rulesList);

		this.rulesList = rulesList;

		rulesList.addListener(listenerEnableDisableGUIwhenRLenabledDisabled);
	}

	final RLT rulesList;

	/**
	 * Every GUI for an {@link AbstractRulesList} should have such a listener as
	 * a field.
	 */
	final private RuleChangeListener_GuiEnabledDisabled listenerEnableDisableGUIwhenRLenabledDisabled;



	class RuleChangeListener_GuiEnabledDisabled implements RuleChangeListener {

		private final RulesListInterface rulesList;
		private final AbstractRulesListGui<RLT> gui;

		@Override
		public void changed(RuleChangedEvent e) {
			// If the enabled/disabled sate of the RL changes, change the
			// GUI enabled/disabled
			if (rulesList.isEnabled() != gui.isEnabled())
				gui.setEnabled(rulesList.isEnabled());
		}

		public RuleChangeListener_GuiEnabledDisabled(
				AbstractRulesListGui<RLT> abstractRuleListGui,
				RulesListInterface rulesList) {
			this.gui = abstractRuleListGui;

			this.rulesList = rulesList;

		}

	}

	@Override
	public void dispose() {
		for (Window w : openWindows) {
			if (w instanceof ClosableSubwindows) {
				((ClosableSubwindows) w).dispose();
			}
			w.dispose();
		}
	}

	public RLT getRulesList() {
		return rulesList;
	}

	/**
	 * A button to invert the order of the applied colors
	 */
	public SmallButton getInvertColorsButton(final List<Color> colors) {
		final SmallButton button = new SmallButton(new AbstractAction("",
				Icons.AS_REVERSE_COLORORDER) {

			@Override
			public void actionPerformed(final ActionEvent e) {
				Collections.reverse(colors);
				rulesList.fireEvents(new RuleChangedEvent(
						"Color order has been reversed", rulesList));
			}

		});

		button.setMargin(new Insets(1, 1, 1, 1));

		button.setToolTipText(ASUtil.R("ColorPalette.RevertButton.TT"));

		return button;
	}
	

	/**
	 * Actually belongs to a new abstract class 
	 */
	private JPanel bandModeSelectionPanel;
	protected JPanel getBandModeCombobox(final RasterRulesListColormap rlcm) {
		if (bandModeSelectionPanel == null) {
			bandModeSelectionPanel = new JPanel(new MigLayout());

			// i8n
			bandModeSelectionPanel.add(new JLabel("Band:"));

			// i8n
			String[] items = new String[0];
			for (int b = 0; b < rlcm.getStyledRaster().getBandCount(); b++) {
				items = LangUtil.extendArray(items, String.valueOf(b + 1));
			}
			final JComboBox bandModeSelectionJCombobox = new JComboBox(items);
			SwingUtil.addMouseWheelForCombobox(bandModeSelectionJCombobox);

			bandModeSelectionPanel.add(bandModeSelectionJCombobox);
			bandModeSelectionJCombobox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rlcm.setBand(bandModeSelectionJCombobox.getSelectedIndex());
				}
			});

			// Initialiseren der JComboBox
			bandModeSelectionJCombobox.setSelectedIndex(rlcm.getBand());

		}
		return bandModeSelectionPanel;
	}

}
