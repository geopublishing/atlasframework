package org.geopublishing.atlasStyler.swing;

import org.geopublishing.atlasStyler.AbstractRulesList;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;

import de.schmitzm.swing.JPanel;

/**
 * Parent of a Swing GUIs that are placed on the right side of
 * {@link AtlasStylerPane} and represent an {@link AbstractRulesList}.
 */
abstract public class AbstractRuleListGui extends JPanel {

	public AbstractRuleListGui(AbstractRulesList rulesList) {
		listenerEnableDisableGUIwhenRLenabledDisabled = new RuleChangeListener_GuiEnabledDisabled(
				this, rulesList);

		rulesList.addListener(listenerEnableDisableGUIwhenRLenabledDisabled);
	}

	/**
	 * Every GUI for an {@link AbstractRulesList} should have such a listener as
	 * a field.
	 */
	final private RuleChangeListener_GuiEnabledDisabled listenerEnableDisableGUIwhenRLenabledDisabled;

	class RuleChangeListener_GuiEnabledDisabled implements RuleChangeListener {

		private final AbstractRulesList rulesList;
		private final AbstractRuleListGui gui;

		@Override
		public void changed(RuleChangedEvent e) {
			// If the enabled/disabled sate of the RL changes, change the
			// GUI enabled/disabled
			if (rulesList.isEnabled() != gui.isEnabled())
				gui.setEnabled(rulesList.isEnabled());
		}

		public RuleChangeListener_GuiEnabledDisabled(
				AbstractRuleListGui abstractRuleListGui,
				AbstractRulesList rulesList) {
			this.gui = abstractRuleListGui;

			this.rulesList = rulesList;

		}

	}

}
