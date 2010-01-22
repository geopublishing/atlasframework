package skrueger.creator.gui;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * The status bar of the {@link GpFrame}
 */
public class GpStatusBar extends JPanel {

	public GpStatusBar(GpFrame gpjFrame) {
		super(new MigLayout("nogrid, w 100%"));
		
		add(gpjFrame.getHeapBar(),"east");
	}

}
