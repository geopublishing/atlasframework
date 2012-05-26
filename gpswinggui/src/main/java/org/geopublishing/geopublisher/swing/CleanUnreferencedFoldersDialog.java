package org.geopublishing.geopublisher.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;
import de.schmitzm.swing.CancelButton;
import de.schmitzm.swing.OkButton;
import de.schmitzm.swing.SwingUtil;

public class CleanUnreferencedFoldersDialog extends JDialog {

	private File[] unrefDirs;
	boolean accepted = false;
	private final JLabel explanationLabel = new JLabel(GpSwingUtil.R("UnreferencedDirectoryFound_AskIfItShouldBeDeleted"));
	private JScrollPane unreferencedDirsListScrollPane;
	private OkButton okButton;
	private CancelButton cancelButton;

	public CleanUnreferencedFoldersDialog(File[] dirs, Component owner) {
		super(SwingUtil.getParentWindow(owner), ModalityType.APPLICATION_MODAL);
		this.unrefDirs = dirs;
		initGui();
	}

	private void initGui() {
		JPanel contentPane = new JPanel(new MigLayout("wrap 2"));
		contentPane.add(explanationLabel, "span 2");
		contentPane.add(getUnreferencedDirsList(), "span 2, grow");
		contentPane.add(getCancelButton(), "right, skip 1, split 2, tag cancel");
		contentPane.add(getOkButton(),"right, tag ok");
		setContentPane(contentPane);
		setTitle(GpSwingUtil.R("UnreferencedDirectoryFound_Title"));
		SwingUtil.centerFrameOnScreen(this);

		pack();
		setVisible(true);
	}

	protected OkButton getOkButton() {
		if (okButton == null) {
			okButton = new OkButton(new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					setAccepted(true);
					dispose();
				}
			});
		}
		return okButton;
	}

	protected CancelButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new CancelButton(new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					setAccepted(false);
					dispose();
				}
			});
		}
		return cancelButton;
	}

	private JScrollPane getUnreferencedDirsList() {
		if (unreferencedDirsListScrollPane == null) {
			JTextArea liste = new JTextArea(5, 30);
			unreferencedDirsListScrollPane = new JScrollPane(liste);
			liste.setEditable(false);
			for (File file : unrefDirs) {
				liste.append(file.toString() + "\n");
			}
			unreferencedDirsListScrollPane.setPreferredSize(new Dimension(450,100));
		}
		return unreferencedDirsListScrollPane;
	}

	public boolean isAccepted() {
		return this.accepted;
	}

	public void setAccepted(boolean value) {
		this.accepted = value;
	}
}
