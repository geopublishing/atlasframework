package skrueger.atlas.gui.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.SwingWorker;

public class SwingWorkerCompletionWaiter implements PropertyChangeListener {
	private JDialog dialog;

	public SwingWorkerCompletionWaiter(JDialog dialog) {
		this.dialog = dialog;
	}

	public void propertyChange(PropertyChangeEvent event) {
		if ("state".equals(event.getPropertyName())
				&& SwingWorker.StateValue.DONE == event.getNewValue()) {
			dialog.setVisible(false);
			dialog.dispose();
		}
	}
}
