package skrueger.atlas.gui.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.SwingWorker;

public class AtlasStatusDialogCloser implements PropertyChangeListener {
	private AtlasStatusDialog dialog;

	public AtlasStatusDialogCloser(AtlasStatusDialog dialog) {
		this.dialog = dialog;
	}

	public void propertyChange(PropertyChangeEvent event) {
		if ("state".equals(event.getPropertyName())
				&& SwingWorker.StateValue.DONE == event.getNewValue()) {
			dialog.complete();
		}
	}
}
