package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.ASProps;
import org.geopublishing.atlasStyler.ASProps.Keys;

import skrueger.swing.AtlasDialog;
import skrueger.swing.CancelButton;
import skrueger.swing.OkButton;

public class SelectPostgisLayerJDialog extends AtlasDialog {

	private JTextField hostInput;
	private JTextField databaseInput;
	private JTextField layerInput;
	private JPasswordField passwordInput;
	private JTextField usernameInput;
	private JTextField portInput;

	public SelectPostgisLayerJDialog(Component parentWindowComponent) {
		super(parentWindowComponent, "Add layer from PostGIS server"); // i8n

		initGUI();
	}

	private void initGUI() {
		// Container cP = getContentPane();

		setModal(true);

		setLayout(new MigLayout("wrap 2", "grow"));
		JLabel explanation = new JLabel(
				"<html>PostGIS layer support is very basic in version 1.5. This dialog will become much nicer<br/> in AtlasStyler version 1.6. Note: The tables you want to import have to be descibed in <i>geometry_columns<i>!<br/>The .sld is stored at "
						+ System.getProperty("user.home") + "</html>"); // i8n
		add(explanation, "span 2");

		/*
		 * String host = "localhost"; String port = "5432"; String database =
		 * "keck"; String username = "postgres"; String password =
		 * "secretIRI69."; String layer = "bundeslaender_2008";
		 */

		add(new JLabel("Host (e.g. 'localhost')"));
		add(getHostInputField());

		add(new JLabel("Database (e.g. 'spatial')"));
		add(getDatabaseInputField());

		add(new JLabel("Port (e.g. '5432')"));
		add(getPortInputField());

		add(new JLabel("Username (e.g. 'joe')"));
		add(getUsernameInputField());

		add(new JLabel("Password (e.g. 'secret')"));
		add(getPasswordInputField());

		add(new JLabel("Layer/table name (e.g. 'borders')"));
		add(getLayerInputField());

		// add(new
		// JLabel("The resulting postgis connection URL looks like:"),"span 2");
		// add(getPreviewUrlLabel());

		OkButton okButton = new OkButton();
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				storeInProps();
				close();
			}

		});
		add(okButton, "span 2, split 2, tag ok");

		CancelButton cancelButton = new CancelButton();
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cancelled = true;
				close();
			}
		});
		add(cancelButton, "tag cancel");
		
		loadFromProps();

		pack();
	}
	
	/**
	 * Set initial values from the {@link ASProps}
	 */
	private void loadFromProps() {
		getDatabaseInputField().setText(ASProps.get(Keys.lastPgDatabase, "spatial"));
		
		getUsernameInputField().setText(ASProps.get(Keys.lastPgUsername, "postgres"));
		
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

	private boolean cancelled = false;

	// private JLabel getPreviewUrlLabel() {
	// if (previewLabel == null) {
	// previewLabel = new JLabel();
	// updatePreviewUrl();
	// }
	// return previewLabel;
	// }
	//
	// private void updatePreviewUrl() {
	// String url =
	// "postgresql://"+getDatabaseInputField().getText()+":"+getPortInputField().getText()+"/"+getDatabaseInputField()
	// getPreviewUrlLabel().setText(url);
	// }

	private JTextField getLayerInputField() {
		if (layerInput == null) {
			layerInput = new JTextField(25);
		}
		return layerInput;
	}

	private JPasswordField getPasswordInputField() {
		if (passwordInput == null) {
			passwordInput = new JPasswordField(25);
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

	public boolean isCancelled() {
		return cancelled;
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

	public String getPassword() {
		return getPasswordInputField().getText();
	}

	public String getLayer() {
		return getLayerInputField().getText();
	}

}