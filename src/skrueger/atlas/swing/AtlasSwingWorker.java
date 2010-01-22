package skrueger.atlas.swing;

import java.awt.Component;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import skrueger.atlas.gui.internal.AtlasStatusDialog;
import skrueger.atlas.gui.internal.AtlasStatusDialogCloser;

public abstract class AtlasSwingWorker<K> extends SwingWorker<K, String> {

	protected final AtlasStatusDialog statusDialog;

	@Override
	protected void process(List<String> chunks) {
		for (String s : chunks)
			statusDialog.setDescription(s);

	}

	public AtlasSwingWorker(AtlasStatusDialog statusDialog) {
		this.statusDialog = statusDialog;
		addPropertyChangeListener(new AtlasStatusDialogCloser(statusDialog));
	}

	public AtlasSwingWorker(Component parentGUI) {
		this(new AtlasStatusDialog(parentGUI));
	}

	public K executeModal() throws InterruptedException, ExecutionException {
		execute();
		statusDialog.startModal();
		return get();
	}

}
