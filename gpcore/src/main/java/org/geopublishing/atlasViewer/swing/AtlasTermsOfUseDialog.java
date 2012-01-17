package org.geopublishing.atlasViewer.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.GpCoreUtil;

import de.schmitzm.swing.CancelButton;
import de.schmitzm.swing.OkButton;
import de.schmitzm.swing.SwingUtil;

public class AtlasTermsOfUseDialog extends javax.swing.JDialog {

	private final AtlasConfig atlasConfig;

	JLabel titleJLabel;
	JLabel logoJLabel;
	HTMLInfoPaneInterface htmlInfoJPane;
	OkButton okButton;
	boolean accepted = false;

	private CancelButton cancelButton;;

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public AtlasTermsOfUseDialog(Component parentGUI, AtlasConfig atlasConfig) {
		this(parentGUI, atlasConfig, ModalityType.MODELESS);
	}

	/** Creates new form AtlasAboutDialog2 */
	public AtlasTermsOfUseDialog(Component parentGUI, AtlasConfig atlasConfig,
			ModalityType modal) {
		super(SwingUtil.getParentWindow(parentGUI), modal);

		this.atlasConfig = atlasConfig;
		initGUI();
	}

	private void initGUI() {
		JComponent htmlComponent = getHtmlInfoJPane();
		Dimension fullScreen = getToolkit().getScreenSize();
		final Dimension dialogSize = new Dimension(
				(int) (fullScreen.width * 0.5), (int) (fullScreen.height * 0.5));

		setTitle(atlasConfig.getTitle().toString());

		JPanel contentPane = new JPanel(new MigLayout("wrap 2", "[]",
				"[top][top]"));

		contentPane.add(getTitleJLabel(), "growx, push");
		contentPane.add(getLogoJLabel(), "right");
		if (!htmlInfoJPane.hasScrollPane()) {
			htmlComponent = new JScrollPane(getHtmlInfoJPane());
		}
		contentPane.add(htmlComponent, "span 2, grow");
		contentPane.add(getCancelButton(), "tag cancel, skip 1, split 2");
		contentPane.add(getOkButton(), "tag ok");
		setContentPane(contentPane);

		setSize(dialogSize);
		SwingUtil.centerFrameOnScreen(this);

		setVisible(true);

	}

	private CancelButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new CancelButton();
			cancelButton.addKeyListener(keyEscDispose);
			cancelButton.requestFocus();
			cancelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// atlasConfig
					// .getProperties()
					// .set(org.geopublishing.atlasViewer.AVProps.Keys.termsOfUseAccepted,
					// "false");
					setAccepted(false);
					dispose();
				}
			});
		}
		return cancelButton;
	}

	// Pressing ESC disposes the Dialog
	KeyAdapter keyEscDispose = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();
			if (key == KeyEvent.VK_ESCAPE) {
				dispose();
			}
		}
	};

	public JLabel getTitleJLabel() {
		if (titleJLabel == null) {
			titleJLabel = new JLabel("<html><h1>" + atlasConfig.getTitle()
					+ "</h2></html>");
		}

		return titleJLabel;
	}

	public JLabel getLogoJLabel() {
		if (logoJLabel == null) {
			logoJLabel = new JLabel();

			if (atlasConfig.getIconURL() != null)
				logoJLabel.setIcon(new ImageIcon(atlasConfig.getIconURL()));
			logoJLabel.setText(null);

		}
		return logoJLabel;
	}

	public JComponent getHtmlInfoJPane() {
		if (htmlInfoJPane == null) {
			htmlInfoJPane = GpCoreUtil.createHTMLInfoPane(
					atlasConfig.getTermsOfUseHTMLURL(), atlasConfig);
		}
		return htmlInfoJPane.getComponent();
	}

	public OkButton getOkButton() {
		if (okButton == null) {
			okButton = new OkButton();
			okButton.addKeyListener(keyEscDispose);
			okButton.requestFocus();
			okButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// atlasConfig
					// .getProperties()
					// .set(org.geopublishing.atlasViewer.AVProps.Keys.termsOfUseAccepted,
					// "true");
					setAccepted(true);
					dispose();
				}
			});
		}
		return okButton;
	}

	/**
	 * Since the registerKeyboardAction() method is part of the JComponent class
	 * definition, you must define the Escape keystroke and register the
	 * keyboard action with a JComponent, not with a JDialog. The JRootPane for
	 * the JDialog serves as an excellent choice to associate the registration,
	 * as this will always be visible. If you override the protected
	 * createRootPane() method of JDialog, you can return your custom JRootPane
	 * with the keystroke enabled:
	 */
	@Override
	protected JRootPane createRootPane() {
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		JRootPane rootPane = new JRootPane();
		rootPane.registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}

		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		return rootPane;
	}

}
