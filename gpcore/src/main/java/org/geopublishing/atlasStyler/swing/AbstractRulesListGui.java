package org.geopublishing.atlasStyler.swing;

import java.awt.Window;

import org.geopublishing.atlasStyler.AbstractRulesList;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.RulesListInterface;

import de.schmitzm.swing.JPanel;

/**
 * Parent of a Swing GUIs that are placed on the right side of
 * {@link AtlasStylerPane} and represent an {@link AbstractRulesList}.
 */
abstract public class AbstractRulesListGui<RLT extends RulesListInterface> extends JPanel implements
		ClosableSubwindows {

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
}
