package org.geopublishing.atlasStyler.swing;

import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.ASProps;
import org.geopublishing.atlasStyler.ASProps.Keys;
import org.geopublishing.atlasStyler.ASUtil;

import schmitzm.swing.ManualInputOption;
import schmitzm.swing.ManualInputOption.PasswordViewable;
import schmitzm.swing.SwingUtil;
import skrueger.swing.CancelButton;
import skrueger.swing.CancellableDialogAdapter;
import skrueger.swing.OkButton;

/**
 * This dloag asks the user for all paramters needed to add a PostGIS layer to
 * the map.
 */
public class SelectPostgisLayerJDialog extends CancellableDialogAdapter {

	private JTextField hostInput;
	private JTextField databaseInput;
	private JTextField layerInput;
	private PasswordViewable passwordInput;
	private JTextField usernameInput;
	private JTextField portInput;

	public SelectPostgisLayerJDialog(Component parentWindowComponent) {
		super(parentWindowComponent, ASUtil
				.R("AtlasStyler.SelectPostgisLayerDialog.title"));

		initGUI();
	}

	private void initGUI() {
		// Container cP = getContentPane();

		setModal(true);

		setLayout(new MigLayout("wrap 2", "grow"));
		JLabel explanation = new JLabel(ASUtil.R(
				"AtlasStyler.SelectPostgisLayerDialog.explanation.html", System
						.getProperty("user.home")));
		add(explanation, "span 2");

		/*
		 * String host = "localhost"; String port = "5432"; String database =
		 * "keck"; String username = "postgres"; String password =
		 * "secretIRI69."; String layer = "bundeslaender_2008";
		 */

		add(new JLabel(ASUtil
				.R("AtlasStyler.SelectPostgisLayerDialog.host.label")));
		add(getHostInputField());

		add(new JLabel(ASUtil
				.R("AtlasStyler.SelectPostgisLayerDialog.database.label")));
		add(getDatabaseInputField());

		add(new JLabel(ASUtil
				.R("AtlasStyler.SelectPostgisLayerDialog.port.label")));
		add(getPortInputField());

		add(new JLabel(ASUtil
				.R("AtlasStyler.SelectPostgisLayerDialog.username.label")));
		add(getUsernameInputField());

		add(new JLabel(ASUtil
				.R("AtlasStyler.SelectPostgisLayerDialog.password.label")));
		add(getPasswordInputField());

		add(new JLabel(ASUtil
				.R("AtlasStyler.SelectPostgisLayerDialog.table.label")));
		add(getLayerInputField());

		
		OkButton okButton = getOkButton();
//		OkButton okButton = new OkButton();
//		okButton.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				storeInProps();
//				okClose();
//			}
//
//		});
		add(okButton, "span 2, split 2, tag ok");

//		CancelButton cancelButton = new CancelButton();
//		cancelButton.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				cancelled = true;
//				close();
//			}
//		});
		CancelButton cancelButton = getCancelButton();
		add(cancelButton, "tag cancel");

		loadFromProps();

		pack();
		
		SwingUtil.centerFrameOnScreen(this);
	}

	/**
	 * Set initial values from the {@link ASProps}
	 */
	private void loadFromProps() {
		getDatabaseInputField().setText(
				ASProps.get(Keys.lastPgDatabase, "spatial"));

		getUsernameInputField().setText(
				ASProps.get(Keys.lastPgUsername, "postgres"));

		getPortInputField().setText(ASProps.get(Keys.lastPgPort, "5432"));

		getLayerInputField().setText(ASProps.get(Keys.lastPgTable, ""));

		getHostInputField().setText(ASProps.get(Keys.lastPgHost, "localhost"));
	}

	/**
	 * Stores the values entered in the properties file
	 */
	private void storeInProps() {
		ASProps.set(Keys.lastPgDatabase, getDb());
		ASProps.set(Keys.lastPgUsername, getUsername());
		ASProps.set(Keys.lastPgPort, getPort());
		ASProps.set(Keys.lastPgTable, getLayer());
		ASProps.set(Keys.lastPgHost, getHost());
	}
	
	@Override
	public boolean okClose() {
		storeInProps();
		return super.okClose();
	}

	private JTextField getLayerInputField() {
		if (layerInput == null) {
			layerInput = new JTextField(25);
		}
		return layerInput;
	}

	private PasswordViewable getPasswordInputField() {
		if (passwordInput == null) {
//			passwordInput = new JPasswordField(25);
			passwordInput = new ManualInputOption.PasswordViewable(null, true);
			((JTextField)passwordInput.getInputComponent()).setColumns(25);
		}
		return passwordInput;
	}

	private JTextField getUsernameInputField() {
		if (usernameInput == null) {
			usernameInput = new JTextField(25);
		}
		return usernameInput;
	}

	private JTextField getPortInputField() {
		if (portInput == null) {
			portInput = new JTextField(5);
		}
		return portInput;
	}

	private JTextField getDatabaseInputField() {
		if (databaseInput == null) {
			databaseInput = new JTextField(25);
		}
		return databaseInput;
	}

	private JTextField getHostInputField() {
		if (hostInput == null) {
			hostInput = new JTextField(25);
		}
		return hostInput;
	}

	/**
	 * Return the host selected by the user
	 */
	public String getHost() {
		return getHostInputField().getText();
	}

	public String getPort() {
		return getPortInputField().getText();
	}

	public String getDb() {
		return getDatabaseInputField().getText();
	}

	public String getUsername() {
		return getUsernameInputField().getText();
	}

	public String getLayer() {
		return getLayerInputField().getText();
	}

	public String getPassword() {
		return new String(getPasswordInputField().getValue());
	}

	@Override
	public void cancel() {
	}
	

	/**
	 * Allows to close the {@link JDialog} from "outside". The user will be
	 * asked and she may cancel the close process.
	 */
	public boolean close() {
		cancelClose();
		return true;
	}

}
