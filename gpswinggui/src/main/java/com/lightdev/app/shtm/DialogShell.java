/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002 Ulrich Hilger
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.lightdev.app.shtm;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * Base class for other dialogs of application SimplyHTML.
 * 
 * @author Ulrich Hilger
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the GNU General Public
 *         License, for details see file gpl.txt in the distribution package of
 *         this software
 * 
 * 
 */

class DialogShell extends JDialog implements ActionListener {

	/** panel containing dialog buttons */
	protected JPanel buttonPanel;

	/** button to confirm the operation */
	protected AbstractButton okButton;

	/** button to cancel the operation */
	protected AbstractButton cancelButton;

	/** button to display context sensitive help */
	protected AbstractButton helpButton;

	/**
	 * the result of the operation, one of RESULT_CANCEL and RESULT_OK
	 */
	private int result;

	/** result value for a cancelled operation */
	public static int RESULT_CANCEL = 1;

	/** result value for a confirmed operation */
	public static int RESULT_OK = 0;

	/** id of associated help topic (if any) */
	protected String helpTopicId = null;

	/**
	 * constructor
	 * 
	 * @param parent
	 *            the parent dialog
	 * @param title
	 *            the title for this dialog
	 */
	public DialogShell(Dialog parent, String title) {
		super(parent, title);
		buildDialog();
	}

	/**
	 * constructor
	 * 
	 * @param parent
	 *            the parent frame
	 * @param title
	 *            the title for this dialog
	 */
	public DialogShell(Frame parent, String title) {
		super(parent, title);
		buildDialog();
	}

	/**
	 * constructor
	 * 
	 * @param parent
	 *            the parent frame
	 * @param title
	 *            the title for this dialog
	 * @param helpTopicId
	 *            the id of the help topic to display for this dialog
	 */
	public DialogShell(Frame parent, String title, String helpTopicId) {
		super(parent, title);
		this.helpTopicId = helpTopicId;
		buildDialog();
	}

	/**
	 * constructor
	 * 
	 * @param parent
	 *            the parent dialog
	 * @param title
	 *            the title for this dialog
	 * @param helpTopicId
	 *            the id of the help topic to display for this dialog
	 */
	public DialogShell(Dialog parent, String title, String helpTopicId) {
		super(parent, title);
		this.helpTopicId = helpTopicId;
		buildDialog();
	}

	/**
	 * create dialog components
	 */
	private void buildDialog() {

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		// construct dialog buttons
		okButton = new JButton(Util.getResourceString("okBtnName"));
		cancelButton = new JButton(Util.getResourceString("cancelBtnName"));
		cancelButton.addActionListener(this);
		okButton.addActionListener(this);

		// construct button panel
		buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		//
		// // construct help button
		// if (helpTopicId != null) {
		// try {
		// helpButton = SHTMLHelpBroker.createHelpButton(helpTopicId);
		// helpButton.setText(Util.getResourceString("helpLabel"));
		// buttonPanel.add(helpButton);
		// } catch (NoClassDefFoundError e) {
		// helpTopicId = null;
		// }
		// }

		// add all to content pane of dialog
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout(5, 5));
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
	}

	/**
	 * dispose the dialog properly in case of window close events
	 */
	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancel();
		}
		super.processWindowEvent(e);
	}

	/**
	 * cancel the operation
	 */
	protected void cancel() {
		result = RESULT_CANCEL;
		dispose();
	}

	/**
	 * confirm the operation
	 */
	protected void confirm() {
		result = RESULT_OK;
		dispose();
	}

	/**
	 * get the result of the operation performed in this dialog
	 * 
	 * @return the result, one of RESULT_OK and RESULT_CANCEL
	 */
	public int getResult() {
		return result;
	}

	/**
	 * implements the ActionListener interface to be notified of clicks onto the
	 * ok and cancel button.
	 */
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == cancelButton) {
			cancel();
		} else if (src == okButton) {
			confirm();
		}
	}
}
