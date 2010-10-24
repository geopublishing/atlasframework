/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002 Ulrich Hilger
 * Copyright (C) 2006 Dimitri Polivaev
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.undo.CannotRedoException;

import schmitzm.swing.SwingUtil;

import com.lightdev.app.shtm.SHTMLPanelImpl.FontFamilyPicker;
import com.lightdev.app.shtm.SHTMLPanelImpl.FontSizePicker;
import com.sun.demo.ElementTreePanel;
import com.sun.demo.ExampleFileFilter;

import de.calcom.cclib.text.FindReplaceDialog;
import de.calcom.cclib.text.FindReplaceEvent;
import de.calcom.cclib.text.FindReplaceListener;

public class SHTMLEditorKitActions {

	/**
	 * action to set the style
	 */
	public static class SetStyleAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;
		private boolean ignoreActions = false;

		public SetStyleAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.setStyleAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			if (!ignoreActions) {
				StyleSelector styleSelector = (StyleSelector) ae.getSource();
				AttributeSet a = styleSelector.getValue();
				if (a != null) {
					// de.calcom.cclib.html.HTMLDiag hd = new
					// de.calcom.cclib.html.HTMLDiag();
					// hd.listAttributes(a, 2);
					this.panel.getEditor().applyAttributes(a, true);
				}
				this.panel.updateActions();
			}
		}

		public void setIgnoreActions(boolean ignore) {
			ignoreActions = ignore;
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * append a new table col
	 */
	public static class AppendTableColAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public AppendTableColAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.appendTableColAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			this.panel.getEditor().appendTableColumn();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			final SHTMLEditorPane editor = this.panel.getEditor();
			if (editor != null && editor.getCurTableCell() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * action to set the tag type
	 */
	public static class SetTagAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;
		private boolean ignoreActions = false;

		public SetTagAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.setTagAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			if (!ignoreActions) {
				String tag = this.panel.getTagSelector().getSelectedTag();
				this.panel.getEditor().applyTag(tag,
						this.panel.getTagSelector().getTags());
				this.panel.updateActions();
			}
		}

		public void setIgnoreActions(boolean ignore) {
			ignoreActions = ignore;
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * append a new table row
	 */
	public static class AppendTableRowAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public AppendTableRowAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.appendTableRowAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			this.panel.getEditor().appendTableRow();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if ((this.panel.getEditor() != null)
					&& (this.panel.getEditor().getCurTableCell() != null)) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/*
	 * Created on 20.08.2006 Copyright (C) 2006 Dimitri Polivaev
	 */
	public static class BoldAction extends StyledEditorKit.BoldAction implements
			SHTMLAction, AttributeComponent {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public BoldAction(SHTMLPanelImpl panel) {
			// Action act = new StyledEditorKit.BoldAction();
			super();
			this.panel = panel;
			putValue(Action.NAME, SHTMLPanelImpl.fontBoldAction);
			putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
					SHTMLPanelImpl.ACTION_UNSELECTED);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_B, InputEvent.CTRL_MASK));
			SHTMLPanelImpl.getActionProperties(this,
					SHTMLPanelImpl.fontBoldAction);
		}

		/**
		 * do the format change for the associated attribute
		 * 
		 * <p>
		 * This reverses the current setting for the associated attribute
		 * </p>
		 * 
		 * @param e
		 *            the ActionEvent describing the cause for this action
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			// System.out.println("ToggleAction getValue=" + getValue() +
			// "selectedValue=" + selectedValue);
			// editor.applyAttributes(getValue(), (unselectedValue == null));
			super.actionPerformed(e);
			// if(unselectedValue != null) {
			if (this.panel.getEditor() != null) {
				SHTMLDocument doc = (SHTMLDocument) this.panel.getEditor()
						.getDocument();
				if (doc != null) {
					AttributeSet a = doc.getCharacterElement(
							this.panel.getEditor().getSelectionStart())
							.getAttributes();
					boolean isBold = StyleConstants.isBold(a);
					// if(a.isDefined(attributeKey)) {
					// Object value = a.getAttribute(attributeKey);
					if (isBold) {
						putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
								SHTMLPanelImpl.ACTION_SELECTED);
					} else {
						putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
								SHTMLPanelImpl.ACTION_UNSELECTED);
					}
				}
			}
			/*
			 * } else { putValue(FrmMain.ACTION_SELECTED_KEY,
			 * FrmMain.ACTION_SELECTED); }
			 */
			this.panel.updateActions();
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					SHTMLPanelImpl.fontItalicAction);
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		/**
		 * set the value of this <code>AttributeComponent</code>
		 * 
		 * @param a
		 *            the set of attributes possibly having an attribute this
		 *            component can display
		 * 
		 * @return true, if the set of attributes had a matching attribute,
		 *         false if not
		 */
		public boolean setValue(AttributeSet a) {
			boolean success = false;
			boolean isBold = StyleConstants.isBold(a);
			if (a.isDefined(CSS.Attribute.FONT_WEIGHT)) {
				Object value = a.getAttribute(CSS.Attribute.FONT_WEIGHT);
				if (value.toString().equalsIgnoreCase(
						StyleConstants.Bold.toString())) {
					isBold = true;
				}
			}
			if (isBold) {
				putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
						SHTMLPanelImpl.ACTION_SELECTED);
			} else {
				putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
						SHTMLPanelImpl.ACTION_UNSELECTED);
			}
			success = true;
			return success;
		}

		/**
		 * get the value of this <code>AttributeComponent</code>
		 * 
		 * @return the value selected from this component
		 */
		public AttributeSet getValue() {
			// System.out.println("ToggleAction getValue getValue(FrmMain.ACTION_SELECTED_KEY)="
			// + getValue(FrmMain.ACTION_SELECTED_KEY));
			SimpleAttributeSet set = new SimpleAttributeSet();
			// if(unselectedValue != null) {
			if (getValue(SHTMLPanelImpl.ACTION_SELECTED_KEY).toString().equals(
					SHTMLPanelImpl.ACTION_SELECTED)) {
				Util.styleSheet().addCSSAttribute(set,
						CSS.Attribute.FONT_WEIGHT,
						StyleConstants.Bold.toString());
			} else {
				Util.styleSheet().addCSSAttribute(set,
						CSS.Attribute.FONT_WEIGHT,
						Util.CSS_ATTRIBUTE_NORMAL.toString());
			}
			/*
			 * } else { Util.styleSheet().addCSSAttribute(set, (CSS.Attribute)
			 * getAttributeKey(), selectedValue.toString()); }
			 */
			return set;
		}

		public AttributeSet getValue(boolean includeUnchanged) {
			return getValue();
		}

	}

	/**
	 * action to toggle an attribute
	 */
	public static class ToggleAction extends AbstractAction implements
			SHTMLAction, AttributeComponent {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		/** the attribute this action represents values for */
		Object attributeKey;

		/** the value for the attribute being selected */
		final private Object selectedValue;

		/**
		 * construct a ToggleFontAction
		 * 
		 * @param name
		 *            the name and command for this action
		 * @param key
		 *            the attribute this action represents values for
		 * @param sVal
		 *            the value for the attribute being selected
		 * @param uVal
		 *            the value for the attribute not being selected
		 * @param panel
		 *            TODO
		 */
		public ToggleAction(SHTMLPanelImpl panel, String name, Object key,
				Object sVal) {
			super(name);
			this.panel = panel;
			putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
					SHTMLPanelImpl.ACTION_UNSELECTED);
			attributeKey = key;
			selectedValue = sVal;
			getProperties();
		}

		/**
		 * do the format change for the associated attribute
		 * 
		 * <p>
		 * This reverses the current setting for the associated attribute
		 * </p>
		 * 
		 * @param e
		 *            the ActionEvent describing the cause for this action
		 */
		public void actionPerformed(ActionEvent e) {
			// System.out.println("ToggleAction getValue=" + getValue() +
			// "selectedValue=" + selectedValue);
			final JToggleButton btn = (JToggleButton) e.getSource();
			if (btn.isSelected()) {
				this.panel.getEditor().applyAttributes(getValue(), true);
				putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
						SHTMLPanelImpl.ACTION_SELECTED);
				this.panel.updateActions();
			}
		}

		/**
		 * get the attribute this action represents values for
		 * 
		 * @return the attribute this action represents values for
		 */
		public Object getAttributeKey() {
			return attributeKey;
		}

		/**
		 * set the value of this <code>AttributeComponent</code>
		 * 
		 * @param a
		 *            the set of attributes possibly having an attribute this
		 *            component can display
		 * 
		 * @return true, if the set of attributes had a matching attribute,
		 *         false if not
		 */
		public boolean setValue(AttributeSet a) {
			boolean success = false;
			if (a.isDefined(attributeKey)) {
				Object value = a.getAttribute(attributeKey);
				if (value.toString().equalsIgnoreCase(selectedValue.toString())) {
					putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
							SHTMLPanelImpl.ACTION_SELECTED);
				} else {
					putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
							SHTMLPanelImpl.ACTION_UNSELECTED);
				}
				success = true;
			} else {
				putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
						SHTMLPanelImpl.ACTION_UNSELECTED);
			}
			return success;
		}

		/**
		 * get the value of this <code>AttributeComponent</code>
		 * 
		 * @return the value selected from this component
		 */
		public AttributeSet getValue() {
			// System.out.println("ToggleAction getValue getValue(FrmMain.ACTION_SELECTED_KEY)="
			// + getValue(FrmMain.ACTION_SELECTED_KEY));
			SimpleAttributeSet set = new SimpleAttributeSet();
			Util.styleSheet()
					.addCSSAttribute(set, (CSS.Attribute) getAttributeKey(),
							selectedValue.toString());
			return set;
		}

		public AttributeSet getValue(boolean includeUnchanged) {
			return getValue();
		}

		/** update the action's state */
		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		/** get image, etc. from resource */
		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * delete a table col
	 */
	public static class DeleteTableColAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public DeleteTableColAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.deleteTableColAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			this.panel.getEditor().deleteTableCol();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if ((this.panel.getEditor() != null)
					&& (this.panel.getEditor().getCurTableCell() != null)) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * Action that brings up a JFrame with a JTree showing the structure of the
	 * document in the currently active DocumentPane.
	 * 
	 * will be hidden from menu if not in development mode (DEV_MODE = false)
	 */
	public static class ShowElementTreeAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;
		/** a frame for showing an element tree panel */
		private JFrame elementTreeFrame = null;

		public ShowElementTreeAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.elemTreeAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent e) {
			if (this.elementTreeFrame == null) {
				String title = Util.getResourceString("elementTreeTitle");
				this.elementTreeFrame = new JFrame(title);
				this.elementTreeFrame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent we) {
						ShowElementTreeAction.this.elementTreeFrame.dispose();
						ShowElementTreeAction.this.elementTreeFrame = null;
					}
				});
				Container fContentPane = this.elementTreeFrame.getContentPane();
				fContentPane.setLayout(new BorderLayout());
				ElementTreePanel elementTreePanel = new ElementTreePanel(
						this.panel.getEditor());
				fContentPane.add(elementTreePanel);
				this.elementTreeFrame.pack();
			}
			this.elementTreeFrame.setVisible(true);
			this.panel.updateActions();
		}

		public void update() {
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * delete a table row
	 */
	public static class DeleteTableRowAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public DeleteTableRowAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.deleteTableRowAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			this.panel.getEditor().deleteTableRow();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if ((this.panel.getEditor() != null)
					&& (this.panel.getEditor().getCurTableCell() != null)) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * toggle list formatting for a given type of list on/off
	 */
	public static class ToggleListAction extends AbstractAction implements
			SHTMLAction {

		/**
     *
     */
		private final SHTMLPanelImpl panel;
		private HTML.Tag listTag;

		public ToggleListAction(SHTMLPanelImpl panel, String name,
				HTML.Tag listTag) {
			super(name);
			this.panel = panel;
			this.listTag = listTag;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			this.panel.getEditor().toggleList(
					listTag.toString(),
					this.panel.getMaxAttributes(this.panel.getEditor(), listTag
							.toString()), false);
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * set the title of the currently active document
	 */
	public static class DocumentTitleAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public DocumentTitleAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.documentTitleAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			String newTitle;
			String currentTitle = this.panel.getSHTMLDocument()
					.getDocumentTitle();
			if (currentTitle != null) {
				newTitle = currentTitle;
			} else {
				newTitle = "";
			}
			newTitle = Util.nameInput(JOptionPane
					.getFrameForComponent(this.panel), newTitle, ".*",
					"docTitleTitle", "docTitleQuery");
			if (newTitle != null && newTitle.length() > 0) {
				this.panel.getSHTMLDocument().setDocumentTitle(newTitle);
			}
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/*
	 * Created on 20.08.2006 Copyright (C) 2006 Dimitri Polivaev
	 */

	public static class UnderlineAction extends StyledEditorKit.UnderlineAction
			implements SHTMLAction, AttributeComponent {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public UnderlineAction(SHTMLPanelImpl panel) {
			// Action act = new StyledEditorKit.BoldAction();
			super();
			this.panel = panel;
			putValue(Action.NAME, SHTMLPanelImpl.fontUnderlineAction);
			putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
					SHTMLPanelImpl.ACTION_UNSELECTED);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_U, InputEvent.CTRL_MASK));
			SHTMLPanelImpl.getActionProperties(this,
					SHTMLPanelImpl.fontUnderlineAction);
		}

		/**
		 * do the format change for the associated attribute
		 * 
		 * <p>
		 * This reverses the current setting for the associated attribute
		 * </p>
		 * 
		 * @param e
		 *            the ActionEvent describing the cause for this action
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			// System.out.println("ToggleAction getValue=" + getValue() +
			// "selectedValue=" + selectedValue);
			// editor.applyAttributes(getValue(), (unselectedValue == null));
			super.actionPerformed(e);
			// if(unselectedValue != null) {
			if (this.panel.getEditor() != null) {
				SHTMLDocument doc = (SHTMLDocument) this.panel.getEditor()
						.getDocument();
				if (doc != null) {
					AttributeSet a = doc.getCharacterElement(
							this.panel.getEditor().getSelectionStart())
							.getAttributes();
					boolean isUnderlined = StyleConstants.isUnderline(a);
					if (isUnderlined) {
						putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
								SHTMLPanelImpl.ACTION_SELECTED);
					} else {
						putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
								SHTMLPanelImpl.ACTION_UNSELECTED);
					}
				}
			}
			this.panel.updateActions();
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					SHTMLPanelImpl.fontUnderlineAction);
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		/**
		 * set the value of this <code>AttributeComponent</code>
		 * 
		 * @param a
		 *            the set of attributes possibly having an attribute this
		 *            component can display
		 * 
		 * @return true, if the set of attributes had a matching attribute,
		 *         false if not
		 */
		public boolean setValue(AttributeSet a) {
			boolean success = false;
			boolean isUnderlined = StyleConstants.isUnderline(a);
			if (a.isDefined(CSS.Attribute.TEXT_DECORATION)) {
				Object value = a.getAttribute(CSS.Attribute.TEXT_DECORATION);
				if (value.toString().equalsIgnoreCase(
						Util.CSS_ATTRIBUTE_UNDERLINE /*
													 * StyleConstants.Underline.toString
													 * ()
													 */)) {
					isUnderlined = true;
				}
			}
			if (isUnderlined) {
				putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
						SHTMLPanelImpl.ACTION_SELECTED);
			} else {
				putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
						SHTMLPanelImpl.ACTION_UNSELECTED);
			}
			success = true;
			return success;
		}

		/**
		 * get the value of this <code>AttributeComponent</code>
		 * 
		 * @return the value selected from this component
		 */
		public AttributeSet getValue() {
			SimpleAttributeSet set = new SimpleAttributeSet();
			if (getValue(SHTMLPanelImpl.ACTION_SELECTED_KEY).toString().equals(
					SHTMLPanelImpl.ACTION_SELECTED)) {
				Util.styleSheet().addCSSAttribute(set,
						CSS.Attribute.TEXT_DECORATION,
						Util.CSS_ATTRIBUTE_UNDERLINE);
			} else {
				Util.styleSheet().addCSSAttribute(set,
						CSS.Attribute.TEXT_DECORATION, Util.CSS_ATTRIBUTE_NONE);
			}
			return set;
		}

		public AttributeSet getValue(boolean includeUnchanged) {
			return getValue();
		}

	}

	public static class FontColorAction extends AbstractAction implements
			SHTMLAction {
		/**
         *
         */
		private final SHTMLPanelImpl panel;
		private ColorPanel hiddenColorPanel;

		public FontColorAction(SHTMLPanelImpl panel) {
			super();
			this.panel = panel;
			putValue(Action.NAME, SHTMLPanelImpl.fontColorAction);
			putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
					SHTMLPanelImpl.ACTION_UNSELECTED);
			getProperties();
			hiddenColorPanel = null;
		}

		public void actionPerformed(ActionEvent e) {
			SHTMLEditorPane editorPane = this.panel.getEditor();
			if (editorPane != null) {
				if (hiddenColorPanel == null) {
					hiddenColorPanel = new ColorPanel("Select Color",
							Color.BLACK, CSS.Attribute.COLOR);
				}
				hiddenColorPanel.setValue(this.panel.getMaxAttributes(
						editorPane, null));
				hiddenColorPanel.actionPerformed(null); // show the color
				// chooser
				editorPane.applyAttributes(hiddenColorPanel.getValue(), false); // apply
				// the
				// color
				// setting
				// to
				// the
				// editor
			}
			this.panel.updateActions();
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					SHTMLPanelImpl.fontColorAction);
		}

		public void update() {
		}
	}

	/**
	 * action to edit anchors inside a document
	 */
	public static class EditAnchorsAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public EditAnchorsAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.editAnchorsAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			Frame parent = JOptionPane.getFrameForComponent(this.panel);
			AnchorDialog dlg = new AnchorDialog(parent, Util
					.getResourceString("anchorDialogTitle"), this.panel
					.getSHTMLDocument());
			Util.center(parent, dlg);
			dlg.setModal(true);
			dlg.setVisible(true);
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * action to edit a link
	 */
	public static class EditLinkAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public EditLinkAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.editLinkAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			Frame parent = JOptionPane.getFrameForComponent(this.panel);
			LinkDialog dlg = new LinkDialog(parent, Util
					.getResourceString("linkDialogTitle"), this.panel
					.getSHTMLDocument(), this.panel.getEditor()
					.getSelectionStart(), this.panel.getEditor()
					.getSelectionEnd(), this.panel.getDocumentPane()
					.getImageDir()/*
								 * , renderMode
								 */);
			Util.center(parent, dlg);
			dlg.setModal(true);
			dlg.setVisible(true);
			if (dlg.getResult() == DialogShell.RESULT_OK) {
				// apply link here
				this.panel.getEditor().setLink(dlg.getLinkText(),
						dlg.getHref(), dlg.getStyleName(), dlg.getLinkImage(),
						dlg.getLinkImageSize());
			}
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				if ((this.panel.getEditor().getSelectionEnd() > this.panel
						.getEditor().getSelectionStart())
						|| (Util.findLinkElementUp(this.panel
								.getSHTMLDocument().getCharacterElement(
										this.panel.getEditor()
												.getSelectionStart())) != null)) {
					this.setEnabled(true);
				} else {
					this.setEnabled(false);
				}
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * UndoAction for the edit menu
	 */
	public static class UndoAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public UndoAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.undoAction);
			this.panel = panel;
			setEnabled(false);
			getProperties();
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			if (this.panel.getCurrentDocumentPane().getSelectedTab() != DocumentPane.VIEW_TAB_LAYOUT) {
				return;
			}
			try {
				this.panel.getUndo().undo();
				final SHTMLEditorPane editor = this.panel.getEditor();
			} catch (Exception ex) {
				Util.errMsg((Component) e.getSource(), Util
						.getResourceString("unableToUndoError")
						+ ex, ex);
			}
			this.panel.updateActions();

		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			setEnabled(this.panel.getUndo().canUndo());
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * action to change the paragraph style
	 */
	public static class EditNamedStyleAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public EditNamedStyleAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.editNamedStyleAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			Frame parent = JOptionPane.getFrameForComponent(this.panel);
			ParaStyleDialog dlg = new ParaStyleDialog(parent, Util
					.getResourceString("namedStyleDialogTitle"), this.panel
					.getSHTMLDocument());
			Util.center(parent, dlg);
			dlg.setModal(true);
			dlg.setValue(this.panel.getMaxAttributes(panel.getEditor(), null));
			dlg.setVisible(true);
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	public static class ClearFormatAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public ClearFormatAction(SHTMLPanelImpl panel) {
			super();
			this.panel = panel;
			putValue(Action.NAME, SHTMLPanelImpl.clearFormatAction);
			putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
					SHTMLPanelImpl.ACTION_UNSELECTED);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_R, InputEvent.CTRL_MASK));
			SHTMLPanelImpl.getActionProperties(this,
					SHTMLPanelImpl.clearFormatAction);
		}

		/**
		 * do the format change for the associated attribute
		 * 
		 * <p>
		 * This reverses the current setting for the associated attribute
		 * </p>
		 * 
		 * @param e
		 *            the ActionEvent describing the cause for this action
		 */
		public void actionPerformed(ActionEvent e) {
			final SHTMLEditorPane editor = this.panel.getEditor();
			if (editor != null) {
				if (editor.getSelectionStart() != editor.getSelectionEnd()) {
					editor.removeCharacterAttributes();
				} else {
					editor.removeParagraphAttributes();
				}
			}
			this.panel.updateActions();
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					SHTMLPanelImpl.clearFormatAction);
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			final SHTMLEditorPane editor = this.panel.getEditor();
			this.setEnabled(editor != null);
		}

	}

	/**
	 * action to find and replace a given text
	 */
	static class MultipleDocFindReplaceAction extends AbstractAction implements
			SHTMLAction, FindReplaceListener {
		/**
   *
   */
		private final SHTMLPanelMultipleDocImpl panel;

		public MultipleDocFindReplaceAction(SHTMLPanelMultipleDocImpl panel) {
			super(SHTMLPanelImpl.findReplaceAction);
			this.panel = panel;
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_F, InputEvent.CTRL_MASK));
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			currentTab = this.panel.getTabbedPaneForDocuments()
					.getSelectedIndex();
			caretPos = this.panel.getDocumentPane().getEditor()
					.getCaretPosition();
			if (this.panel.getTabbedPaneForDocuments().getTabCount() > 1) {
//				System.out
//						.println("FindReplaceAction.actionPerformed with Listener");
//				FindReplaceDialog frd = new FindReplaceDialog(this.panel
//						.getMainFrame(), this.panel.getEditor(), this);
			} else {
//				System.out
//						.println("FindReplaceAction.actionPerformed NO Listener");
//				FindReplaceDialog frd = new FindReplaceDialog(this.panel
//						.getMainFrame(), this.panel.getEditor());
			}
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getTabbedPaneForDocuments().getTabCount() > 0) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}

		public void getNextDocument(FindReplaceEvent e) {
			FindReplaceDialog frd = (FindReplaceDialog) e.getSource();
			int tabCount = this.panel.getTabbedPaneForDocuments().getTabCount();
			int curTab = this.panel.getTabbedPaneForDocuments()
					.getSelectedIndex();
			System.out.println("FindReplaceAction.getNextDocument curTab="
					+ curTab + ", tabCount=" + tabCount);
			if (++curTab < tabCount) {
				System.out
						.println("FindReplaceAction.getNextDocument next tab no="
								+ curTab);
				resumeWithNewEditor(frd, curTab);
			} else {
				frd.terminateOperation();
			}
		}

		public void getFirstDocument(FindReplaceEvent e) {
			FindReplaceDialog frd = (FindReplaceDialog) e.getSource();
			resumeWithNewEditor(frd, 0);
		}

		public void findReplaceTerminated(FindReplaceEvent e) {
			this.panel.getTabbedPaneForDocuments().setSelectedIndex(currentTab);
			DocumentPane docPane = (DocumentPane) this.panel
					.getTabbedPaneForDocuments().getSelectedComponent();
			JEditorPane editor = docPane.getEditor();
			editor.setCaretPosition(caretPos);
			editor.requestFocus();
		}

		private void resumeWithNewEditor(FindReplaceDialog frd, int tabNo) {
			this.panel.getTabbedPaneForDocuments().setSelectedIndex(tabNo);
			DocumentPane docPane = (DocumentPane) this.panel
					.getTabbedPaneForDocuments().getComponentAt(tabNo);
			JEditorPane editor = docPane.getEditor();
			editor.requestFocus();
			frd.setEditor(editor);
			frd.resumeOperation();
		}

		private int caretPos;
		private int currentTab;
	}

	/**
	 * action to find and replace a given text
	 */
	static class SingleDocFindReplaceAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public SingleDocFindReplaceAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.findReplaceAction);
			this.panel = panel;
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_F, InputEvent.CTRL_MASK));
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			currentDocumentPane = this.panel.getDocumentPane();
			if (currentDocumentPane != null) {
				caretPos = currentDocumentPane.getEditor().getCaretPosition();
				FindReplaceDialog frd = new FindReplaceDialog(this.panel
						.getMainFrame(), this.panel.getEditor());
			}
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getDocumentPane() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}

		public void findReplaceTerminated(FindReplaceEvent e) {
			if (currentDocumentPane.isVisible()) {
				JEditorPane editor = currentDocumentPane.getEditor();
				editor.setCaretPosition(caretPos);
				editor.requestFocus();
			}
		}

		private int caretPos;
		private DocumentPane currentDocumentPane;
	}

	/**
	 * Show a dialog to format fonts
	 */
	public static class FontAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public FontAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.fontAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			Frame parent = JOptionPane.getFrameForComponent(this.panel);
			this.panel.getEditor().requestFocus();

			/** create a modal FontDialog, center and show it */
			FontDialog fd = new FontDialog(parent, Util
					.getResourceString("fontDialogTitle"), this.panel
					.getMaxAttributes(this.panel.getEditor(), null));
			Util.center(parent, fd);
			fd.setModal(true);
			fd.setVisible(true);

			/** if the user made a selection, apply it to the document */
			if (fd.getResult() == DialogShell.RESULT_OK) {
				this.panel.getEditor().applyAttributes(fd.getAttributes(),
						false);
				this.panel.updateFormatControls();
			}
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * change a font family setting
	 */
	public static class FontFamilyAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public FontFamilyAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.fontFamilyAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			FontFamilyPicker ffp = ((FontFamilyPicker) ae.getSource());
			if (!ffp.ignore()) {
				this.panel.getEditor().applyAttributes(ffp.getValue(), false);
			}
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * change a font size setting
	 */
	public static class FontSizeAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public FontSizeAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.fontSizeAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			FontSizePicker fsp = ((FontSizePicker) ae.getSource());
			if (!fsp.ignore()) {
				this.panel.getEditor().applyAttributes(fsp.getValue(), false);
			}
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	public static class FormatImageAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public FormatImageAction(SHTMLPanelImpl panel) {
			super();
			this.panel = panel;
			putValue(Action.NAME, SHTMLPanelImpl.formatImageAction);
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			Frame parent = JOptionPane.getFrameForComponent(this.panel);
			ImageDialog dlg = new ImageDialog(parent, Util
					.getResourceString("imageDialogTitle"), this.panel
					.getDocumentPane().getImageDir(),
					(SHTMLDocument) this.panel.getDocumentPane().getDocument());
			Element img = this.panel.getSHTMLDocument().getCharacterElement(
					this.panel.getEditor().getCaretPosition());
			if (img.getName().equalsIgnoreCase(HTML.Tag.IMG.toString())) {
				Util.center(parent, dlg);
				dlg.setImageAttributes(img.getAttributes());
				dlg.setModal(true);
				dlg.setVisible(true);

				/** if the user made a selection, apply it to the document */
				if (dlg.getResult() == DialogShell.RESULT_OK) {
					try {
						this.panel.getSHTMLDocument().setOuterHTML(img,
								dlg.getImageHTML());
					} catch (Exception e) {
						Util.errMsg(null, e.getMessage(), e);
					}
				}
				this.panel.updateActions();
			}
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				Element img = this.panel.getSHTMLDocument()
						.getCharacterElement(
								this.panel.getEditor().getCaretPosition());
				if (img.getName().equalsIgnoreCase(HTML.Tag.IMG.toString())) {
					this.setEnabled(true);
				} else {
					this.setEnabled(false);
				}
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * Change list formatting
	 */
	public static class FormatListAction extends AbstractAction implements
			SHTMLAction {

		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public FormatListAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.formatListAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			Frame parent = JOptionPane.getFrameForComponent(this.panel);
			this.panel.getEditor().requestFocus();
			int pos = this.panel.getEditor().getSelectionStart();
			ListDialog dlg = new ListDialog(parent, Util
					.getResourceString("listDialogTitle"));
			SimpleAttributeSet set = new SimpleAttributeSet(this.panel
					.getMaxAttributes(this.panel.getEditor(), HTML.Tag.UL
							.toString()));
			set.addAttributes(this.panel.getMaxAttributes(this.panel
					.getEditor(), HTML.Tag.OL.toString()));
			dlg.setListAttributes(set);
			String currentTag = dlg.getListTag();
			Util.center(parent, dlg);
			dlg.setModal(true);
			dlg.setVisible(true);

			/** if the user made a selection, apply it to the document */
			if (dlg.getResult() == DialogShell.RESULT_OK) {
				AttributeSet a = dlg.getListAttributes();
				String newTag = dlg.getListTag();
				if (newTag == null) {
					this.panel.getEditor().toggleList(newTag, a, true);
				} else if (newTag.equalsIgnoreCase(currentTag)) {
					if (a.getAttributeCount() > 0) {
						this.panel.getEditor().applyListAttributes(a);
					}
				} else {
					this.panel.getEditor().toggleList(newTag, a, false);
				}
			}
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * action to change the paragraph style
	 */
	public static class FormatParaAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public FormatParaAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.formatParaAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			Frame parent = JOptionPane.getFrameForComponent(this.panel);
			ParaStyleDialog dlg = new ParaStyleDialog(parent, Util
					.getResourceString("paraStyleDialogTitle"));
			Util.center(parent, dlg);
			dlg.setModal(true);
			// SHTMLDocument doc = (SHTMLDocument) dp.getDocument();
			final int caretPosition = panel.getEditor().getCaretPosition();
			dlg.setValue(this.panel.getMaxAttributes(caretPosition));
			dlg.setVisible(true);

			/** if the user made a selection, apply it to the document */
			if (dlg.getResult() == DialogShell.RESULT_OK) {
				this.panel.getEditor().applyAttributes(dlg.getValue(), true);
			}
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * format table attributes
	 */
	public static class FormatTableAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public FormatTableAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.formatTableAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			Frame parent = JOptionPane.getFrameForComponent(this.panel);
			final SHTMLEditorPane editor = this.panel.getEditor();
			editor.requestFocus();
			int pos = editor.getSelectionStart();
			TableDialog td = new TableDialog(parent, Util
					.getResourceString("tableDialogTitle"));
			td.setTableAttributes(this.panel.getMaxAttributes(editor,
					HTML.Tag.TABLE.toString()));
			td.setCellAttributes(this.panel.getMaxAttributes(editor,
					HTML.Tag.TD.toString()));
			Util.center(parent, td);
			td.setModal(true);
			td.setVisible(true);

			/** if the user made a selection, apply it to the document */
			if (td.getResult() == DialogShell.RESULT_OK) {
				SHTMLDocument doc = (SHTMLDocument) editor.getDocument();
				doc.startCompoundEdit();
				AttributeSet a = td.getTableAttributes();
				if (a.getAttributeCount() > 0) {
					editor.applyTableAttributes(a);
				}
				a = td.getCellAttributes();
				if (a.getAttributeCount() > 0) {
					editor.applyCellAttributes(a, td.getCellRange());
				}
				doc.endCompoundEdit();
			}
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if ((this.panel.getEditor() != null)
					&& (this.panel.getEditor().getCurTableCell() != null)) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * force a garbage collection. This can be helpful to find out whether or
	 * not objects are properly disposed.
	 * 
	 * Without forcing a garbage collection, this would happen at random
	 * intervals so although an object might be properly disposed, it might
	 * still be around until the next GC.
	 * 
	 * will be hidden from menu if not in development mode (DEV_MODE = false)
	 */
	public static class GCAction extends AbstractAction implements SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public GCAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.gcAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent e) {
			System.gc();
			this.panel.updateActions();
		}

		public void update() {
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	public static class InsertImageAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public InsertImageAction(SHTMLPanelImpl panel) {
			super();
			this.panel = panel;
			putValue(Action.NAME, SHTMLPanelImpl.insertImageAction);
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			Frame parent = JOptionPane.getFrameForComponent(this.panel);
			ImageDialog dlg = new ImageDialog(parent, Util
					.getResourceString("imageDialogTitle"), this.panel
					.getDocumentPane().getImageDir());
//			Util.center(parent, dlg);
			SwingUtil.setRelativeFramePosition(dlg, .5, .5);
			dlg.setModal(true);
			dlg.setVisible(true);

			/** if the user made a selection, apply it to the document */
			if (dlg.getResult() == DialogShell.RESULT_OK) {
				try {
					this.panel.getSHTMLDocument().insertBeforeStart(
							this.panel.getSHTMLDocument().getCharacterElement(
									this.panel.getEditor().getSelectionEnd()),
							dlg.getImageHTML());
				} catch (Exception e) {
					Util.errMsg(null, e.getMessage(), e);
				}
			}
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * insert a new table
	 */
	public static class InsertTableAction extends AbstractAction implements
			SHTMLAction {

		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public InsertTableAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.insertTableAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			Frame parent = JOptionPane.getFrameForComponent(this.panel);
			Object input = Util.nameInput(parent, "3", "\\d+",
					"insertTableTitle", "insertTableMsg");
			if (input != null) {
				int choice = Integer.parseInt(input.toString());
				if (choice > 0) {
					this.panel.getEditor().insertTable(choice);
				}
			}
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * insert a new table column
	 */
	public static class InsertTableColAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public InsertTableColAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.insertTableColAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			this.panel.getEditor().insertTableColumn();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if ((this.panel.getEditor() != null)
					&& (this.panel.getEditor().getCurTableCell() != null)) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * insert a new table row
	 */
	public static class InsertTableRowAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public InsertTableRowAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.insertTableRowAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			this.panel.getEditor().insertTableRow();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if ((this.panel.getEditor() != null)
					&& (this.panel.getEditor().getCurTableCell() != null)) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	public static class ItalicAction extends StyledEditorKit.ItalicAction
			implements SHTMLAction, AttributeComponent {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public ItalicAction(SHTMLPanelImpl panel) {
			// Action act = new StyledEditorKit.BoldAction();
			super();
			this.panel = panel;
			putValue(Action.NAME, SHTMLPanelImpl.fontItalicAction);
			putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
					SHTMLPanelImpl.ACTION_UNSELECTED);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_I, InputEvent.CTRL_MASK));
			SHTMLPanelImpl.getActionProperties(this,
					SHTMLPanelImpl.fontItalicAction);
		}

		/**
		 * do the format change for the associated attribute
		 * 
		 * <p>
		 * This reverses the current setting for the associated attribute
		 * </p>
		 * 
		 * @param e
		 *            the ActionEvent describing the cause for this action
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			super.actionPerformed(e);
			if (this.panel.getEditor() != null) {
				SHTMLDocument doc = (SHTMLDocument) this.panel.getEditor()
						.getDocument();
				if (doc != null) {
					AttributeSet a = doc.getCharacterElement(
							this.panel.getEditor().getSelectionStart())
							.getAttributes();
					boolean isItalic = StyleConstants.isItalic(a);
					if (isItalic) {
						putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
								SHTMLPanelImpl.ACTION_SELECTED);
					} else {
						putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
								SHTMLPanelImpl.ACTION_UNSELECTED);
					}
				}
			}
			this.panel.updateActions();
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					SHTMLPanelImpl.fontItalicAction);
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		/**
		 * set the value of this <code>AttributeComponent</code>
		 * 
		 * @param a
		 *            the set of attributes possibly having an attribute this
		 *            component can display
		 * 
		 * @return true, if the set of attributes had a matching attribute,
		 *         false if not
		 */
		public boolean setValue(AttributeSet a) {
			boolean success = false;
			boolean isItalic = StyleConstants.isItalic(a);
			if (a.isDefined(CSS.Attribute.FONT_STYLE)) {
				Object value = a.getAttribute(CSS.Attribute.FONT_STYLE);
				if (value.toString().equalsIgnoreCase(
						StyleConstants.Italic.toString())) {
					isItalic = true;
				}
			}
			if (isItalic) {
				putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
						SHTMLPanelImpl.ACTION_SELECTED);
			} else {
				putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY,
						SHTMLPanelImpl.ACTION_UNSELECTED);
			}
			success = true;
			return success;
		}

		/**
		 * get the value of this <code>AttributeComponent</code>
		 * 
		 * @return the value selected from this component
		 */
		public AttributeSet getValue() {
			SimpleAttributeSet set = new SimpleAttributeSet();
			if (getValue(SHTMLPanelImpl.ACTION_SELECTED_KEY).toString().equals(
					SHTMLPanelImpl.ACTION_SELECTED)) {
				Util.styleSheet().addCSSAttribute(set,
						CSS.Attribute.FONT_STYLE,
						Util.CSS_ATTRIBUTE_NORMAL.toString());
			} else {
				Util.styleSheet().addCSSAttribute(set,
						CSS.Attribute.FONT_STYLE,
						StyleConstants.Italic.toString());
			}
			return set;
		}

		public AttributeSet getValue(boolean includeUnchanged) {
			return getValue();
		}

	}

	/**
	 * action to move to the next cell in a table
	 */
	public static class NextTableCellAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public NextTableCellAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.nextTableCellAction);
			this.panel = panel;
			getProperties();
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_TAB, 0));
		}

		public void actionPerformed(ActionEvent ae) {

			Element cell = this.panel.getEditor().getCurTableCell();
			if (cell != null) {
				this.panel.getEditor().goNextCell(cell);
				this.panel.updateActions();
			}

		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if ((this.panel.getEditor() != null)
					&& (this.panel.getEditor().getCurTableCell() != null)) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * action to move to the previous cell in a table
	 */
	public static class PrevTableCellAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public PrevTableCellAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.prevTableCellAction);
			this.panel = panel;
			getProperties();
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_TAB, InputEvent.SHIFT_MASK));
		}

		public void actionPerformed(ActionEvent ae) {
			Element cell = this.panel.getEditor().getCurTableCell();
			if (cell != null) {
				this.panel.getEditor().goPrevCell(cell);
				this.panel.updateActions();
			}
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if ((this.panel.getEditor() != null)
					&& (this.panel.getEditor().getCurTableCell() != null)) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * RedoAction for the edit menu
	 */
	public static class RedoAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public RedoAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.redoAction);
			this.panel = panel;
			setEnabled(false);
			getProperties();
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_Y, InputEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			if (this.panel.getCurrentDocumentPane().getSelectedTab() != DocumentPane.VIEW_TAB_LAYOUT) {
				return;
			}
			try {
				this.panel.getUndo().redo();
				final SHTMLEditorPane editor = this.panel.getEditor();
			} catch (CannotRedoException ex) {
				Util.errMsg((Component) e.getSource(), Util
						.getResourceString("unableToRedoError")
						+ ex, ex);
			}
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			setEnabled(this.panel.getUndo().canRedo());
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/** just adds a normal name to the superclasse's action */
	public static class SHTMLEditCopyAction extends DefaultEditorKit.CopyAction
			implements SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public SHTMLEditCopyAction(SHTMLPanelImpl panel) {
			super();
			this.panel = panel;
			putValue(Action.NAME, SHTMLPanelImpl.copyAction);
			getProperties();
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_C, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			super.actionPerformed(e);
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.getEditor() != null) {
				setEnabled(true);
			} else {
				setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/** just adds a normal name to the superclasse's action */
	public static class SHTMLEditCutAction extends DefaultEditorKit.CutAction
			implements SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public SHTMLEditCutAction(SHTMLPanelImpl panel) {
			super();
			this.panel = panel;
			putValue(Action.NAME, SHTMLPanelImpl.cutAction);
			getProperties();
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_X, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			super.actionPerformed(e);
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.getEditor() != null) {
				setEnabled(true);
			} else {
				setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/** just adds a normal name to the superclasse's action */
	public static class SHTMLEditPasteAction extends
			DefaultEditorKit.PasteAction implements SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public SHTMLEditPasteAction(SHTMLPanelImpl panel) {
			super();
			this.panel = panel;
			putValue(Action.NAME, SHTMLPanelImpl.pasteAction);
			getProperties();
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_V, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			super.actionPerformed(e);
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.getEditor() != null) {
				setEnabled(true);
			} else {
				setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	public static class SHTMLEditPrefsAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public SHTMLEditPrefsAction(SHTMLPanelImpl panel) {
			super();
			this.panel = panel;
			putValue(Action.NAME, SHTMLPanelImpl.editPrefsAction);
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			Frame parent = JOptionPane.getFrameForComponent(this.panel);
			PrefsDialog dlg = new PrefsDialog(parent, Util
					.getResourceString("prefsDialogTitle"));
			Util.center(parent, dlg);
			dlg.setModal(true);
			dlg.setVisible(true);

			/** if the user made a selection, apply it to the document */
			if (dlg.getResult() == DialogShell.RESULT_OK) {
			}
			this.panel.updateActions();
		}

		public void update() {
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	public static class SHTMLEditSelectAllAction extends AbstractAction
			implements SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public SHTMLEditSelectAllAction(SHTMLPanelImpl panel) {
			super();
			this.panel = panel;
			putValue(Action.NAME, SHTMLPanelImpl.selectAllAction);
			getProperties();
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_A, InputEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent ae) {
			if (this.panel.isHtmlEditorActive()) {
				this.panel.getDocumentPane().getHtmlEditor().selectAll();
			} else {
				this.panel.getEditor().selectAll();
				this.panel.updateActions();
			}
		}

		public void update() {
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * close a document.
	 * 
	 * <p>
	 * the action takes into account whether or not a document needs to be
	 * saved.
	 * </p>
	 * 
	 * <p>
	 * By having the actual closing task in a separate public method of this
	 * action, the close functionality can be shared with action 'close all' or
	 * others that might need it.
	 * </p>
	 */
	static class SHTMLFileCloseAction extends AbstractAction implements
			SHTMLAction {

		/**
     *
     */
		private final SHTMLPanelMultipleDocImpl panel;
		private boolean exitApp = false;

		/**
		 * constructor
		 * 
		 * @param panel
		 *            TODO
		 */
		public SHTMLFileCloseAction(SHTMLPanelMultipleDocImpl panel) {
			super(SHTMLPanelMultipleDocImpl.closeAction);
			this.panel = panel;
			getProperties();
		}

		/** close the currently active document, if there is one */
		public void actionPerformed(ActionEvent ae) {
			if (this.panel.getEditor() != null) { // if documents are open
				closeDocument(this.panel.getActiveTabNo(), ae, false); // close
				// the
				// active
				// one
			}
			this.panel.updateActions();
		}

		/**
		 * close a document by its tab index.
		 * 
		 * <p>
		 * The method takes care of saving the document if necessary prior to
		 * closing.
		 * </p>
		 * 
		 * @param the
		 *            tab index number of the document in the tabbed pane.
		 * @return true, if the document was closed successfully.
		 */
		public void closeDocument(final int index, ActionEvent ae,
				boolean ignoreChanges) {
			exitApp = ae.getActionCommand().indexOf(SHTMLPanelImpl.exitAction) > -1;
			final DocumentPane dp = (DocumentPane) this.panel
					.getTabbedPaneForDocuments().getComponentAt(index);
			if (!dp.saveInProgress()) { // if no save is going on and..
				if (ignoreChanges) {
					closeDoc(dp);
				} else {
					if (dp.needsSaving()) { // ..the document needs to be saved
						this.panel.selectTabbedPane(index);
						String docName = dp.getDocumentName();
						int choice = Util.msgChoice(
								JOptionPane.YES_NO_CANCEL_OPTION,
								"confirmClosing", "saveChangesQuery", docName,
								"\r\n\r\n");
						switch (choice) {
						case JOptionPane.YES_OPTION: // if the user wanted to
							// save
							if (dp.isNewDoc()) { // if the document is new
								panel.dynRes.getAction(
										SHTMLPanelMultipleDocImpl.saveAsAction)
										.actionPerformed(ae); // 'save as'
							} else { // else
								panel.dynRes.getAction(
										SHTMLPanelMultipleDocImpl.saveAction)
										.actionPerformed(ae); // 'save'
							}
							scheduleClose(dp); // ..and wait until it is
							// finshed, then close
							break;
						case JOptionPane.NO_OPTION: // if the user don't like to
							// save
							closeDoc(dp); // close the document without saving
							break;
						case JOptionPane.CANCEL_OPTION: // if the user cancelled
							break; // do nothing
						}
					} else { // if the document does not need to be saved
						closeDoc(dp); // close the document
					}
				}
			} else { // save was going on upon close request, so
				scheduleClose(dp); // wait for completion, then close
			}
		}

		/**
		 * schedule closing of a document.
		 * 
		 * <p>
		 * This creates a <code>Timer</code> thread for which a
		 * <code>TimerTask</code> is scheduled to peridically check whether or
		 * not the save process for respective document commenced successfully.
		 * </p>
		 * 
		 * <p>
		 * If yes, Timer and TimerTask are disposed and the document is closed.
		 * If not, the document remains open.
		 * </p>
		 * 
		 * @param dp
		 *            the document to close
		 * @param index
		 *            the number of the tab for that document
		 */
		private void scheduleClose(final DocumentPane dp) {
			final java.util.Timer timer = new java.util.Timer();
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					if (!dp.saveInProgress()) { // if done with saving
						if (dp.saveSuccessful) { // and all went fine
							closeDoc(dp); // close the document
							this.cancel(); // dispose the task
							timer.cancel(); // dispose the timer
						}
					}
				}
			};
			timer.schedule(task, 0, 400); // try to close every 400 milliseconds
		}

		/**
		 * convenience method for closing a document
		 */
		private void closeDoc(DocumentPane dp) {
			try {
				dp.deleteTempDir();
				this.panel.unregisterDocument();
				this.panel.getTabbedPaneForDocuments().remove(dp);
			} catch (IndexOutOfBoundsException e) { // if the tabs have changed
				// meanwhile
				catchCloseErr(dp);
			}
			if (exitApp) { // if the doc close was caused by a request to exit
				// the app
				if (this.panel.getTabbedPaneForDocuments().getTabCount() == 0) { // ..and
					// if
					// there
					// are
					// no
					// open
					// docs
					panel.firePropertyChange("closing", false, true);
				}
			}
		}

		private void catchCloseErr(DocumentPane dp) {
			try {
				int i = this.panel.getTabbedPaneForDocuments()
						.indexOfComponent(dp); // get the current tab index
				if (i < 0 && this.panel.getEditor() != null) {
					this.panel.setActiveTabNo(this.panel
							.getTabbedPaneForDocuments().getSelectedIndex());
					dp = (DocumentPane) this.panel.getTabbedPaneForDocuments()
							.getComponentAt(this.panel.getActiveTabNo());
					i = this.panel.getTabbedPaneForDocuments()
							.indexOfComponent(dp); // get the current tab index
					// again
					this.panel.unregisterDocument();
					this.panel.getTabbedPaneForDocuments().remove(i); // now
					// remove
					// it
				} else {
					while (i > 0
							&& i > this.panel.getTabbedPaneForDocuments()
									.getTabCount()) { // while its still wrong
						i = this.panel.getTabbedPaneForDocuments()
								.indexOfComponent(dp); // get the current tab
						// index again
					}
					this.panel.unregisterDocument();
					this.panel.getTabbedPaneForDocuments().remove(i); // now
					// remove
					// it
				}
			} catch (IndexOutOfBoundsException e) {
				catchCloseErr(dp);
			}
		}

		/** update the state of this action */
		public void update() {
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * close all documents currently shown.
	 * 
	 * <p>
	 * This action simply loops through all open documents and uses an instance
	 * of SHTMLFileCloseAction to perform the actual closing on each of them.
	 * </p>
	 */
	static class SHTMLFileCloseAllAction extends AbstractAction implements
			SHTMLAction {

		/**
     *
     */
		private final SHTMLPanelMultipleDocImpl panel;

		/**
		 * constructor
		 * 
		 * @param panel
		 *            TODO
		 */
		public SHTMLFileCloseAllAction(SHTMLPanelMultipleDocImpl panel) {
			super(SHTMLPanelMultipleDocImpl.closeAllAction);
			this.panel = panel;
			getProperties();
		}

		/** close all open documents */
		public void actionPerformed(ActionEvent ae) {
			SHTMLFileCloseAction a = (SHTMLFileCloseAction) panel.dynRes
					.getAction(SHTMLPanelMultipleDocImpl.closeAction);
			for (int i = this.panel.getTabbedPaneForDocuments().getTabCount(); i > 0; i--) {
				// System.out.println("CloseAll, close tab no " + i);
				a.closeDocument(i - 1, ae, false);
			}
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * exit the application.
	 * 
	 * <p>
	 * This will only exit the application, if
	 * <ul>
	 * <li>no documents are open or</li>
	 * <li>documents are open that do not need to be saved or</li>
	 * <li>documents are open and are saved successfully prior to close or</li>
	 * <li>documents are open for which the user explicitly opted not to save
	 * them</li>
	 * </ul>
	 * </p>
	 */
	public static class SHTMLFileExitAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelMultipleDocImpl panel;

		public SHTMLFileExitAction(SHTMLPanelMultipleDocImpl panel) {
			super(SHTMLPanelImpl.exitAction);
			this.panel = panel;
			getProperties();
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent ae) {
			saveRelevantPrefs();
			new SHTMLFileCloseAllAction(this.panel).actionPerformed(ae);
			if (this.panel.getTabbedPaneForDocuments().getTabCount() == 0) {
				panel.firePropertyChange("closing", false, true);
			}
			this.panel.updateActions();
		}

		public void saveRelevantPrefs() {

			/* ---- save splitpane sizes start -------------- */

			this.panel.savePrefs();

			/* ---- save splitpane sizes end -------------- */
		}

		public void update() {
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/** create a new empty document and show it */
	static class SHTMLFileNewAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelMultipleDocImpl panel;

		public SHTMLFileNewAction(SHTMLPanelMultipleDocImpl panel) {
			super(SHTMLPanelMultipleDocImpl.newAction);
			this.panel = panel;
			getProperties();
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_N, InputEvent.CTRL_MASK));
		}

		/** create a new empty document and show it */
		public void actionPerformed(ActionEvent ae) {
			this.panel.createNewDocumentPane(); // create a new empty document
			this.panel.getTabbedPaneForDocuments().setSelectedComponent( // add
					// the
					// document
					// to
					// the
					this.panel.getTabbedPaneForDocuments().add(
							this.panel.getDocumentPane().getDocumentName(),
							this.panel.getDocumentPane())); // tabbed pane for
			// display

			this.panel.registerDocument();

			this.panel.updateActions();
		}

		public void update() {
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/** open an existing document from file and show it */
	static class SHTMLFileOpenAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelMultipleDocImpl panel;

		public SHTMLFileOpenAction(SHTMLPanelMultipleDocImpl panel) {
			super(SHTMLPanelMultipleDocImpl.openAction);
			this.panel = panel;
			getProperties();
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_O, InputEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent ae) {
			Preferences prefs = Preferences.userNodeForPackage(this.panel
					.getClass());
			JFileChooser chooser = new JFileChooser(); // create a file chooser
			ExampleFileFilter filter = new ExampleFileFilter(); // create a
			// filter
			filter.addExtension("htm");
			filter.addExtension("html");
			filter.setDescription(Util.getResourceString("htmlFileDesc"));
			chooser.setFileFilter(filter); // apply the file filter
			String lastFileName = prefs.get(SHTMLPanelImpl.FILE_LAST_OPEN, "");
			if (lastFileName.length() > 0) {
				chooser.setCurrentDirectory(new File(lastFileName)
						.getParentFile());
			}
			int returnVal = // ..and show the file chooser
			chooser.showOpenDialog((Component) ae.getSource());
			if (returnVal == JFileChooser.APPROVE_OPTION) { // if a file was
				// selected
				File file = chooser.getSelectedFile();
				prefs
						.put(SHTMLPanelImpl.FILE_LAST_OPEN, file
								.getAbsolutePath());
				openDocument(file);
			}
			this.panel.updateActions();
		}

		public void openDocument(File file) {
			openDocument(file, null);
		}

		public void openDocument(File file,
				DocumentPane.DocumentPaneListener listener) {
			int openDocNo = -1;
			try {
				// XXX
				openDocNo = getOpenDocument(file.toURI().toURL().toString());
			} catch (MalformedURLException mue) {
			}
			if (openDocNo > -1) {
				this.panel.getTabbedPaneForDocuments().setSelectedIndex(
						openDocNo);
			} else {
				FileLoader loader = new FileLoader(file, null, listener);
				loader.start();
			}
		}

		public int getOpenDocument(String url) {
			int tabNo = -1;
			int openDocCount = this.panel.getTabbedPaneForDocuments()
					.getTabCount();
			int i = 0;
			while (i < openDocCount && tabNo < 0) {
				URL source = ((DocumentPane) this.panel
						.getTabbedPaneForDocuments().getComponentAt(i))
						.getSource();
				if (source != null) {
					if (source.toString().equalsIgnoreCase(url)) {
						tabNo = i;
					}
				}
				i++;
			}
			return tabNo;
		}

		/**
		 * get a FileLoader object for the document currently active
		 * 
		 * @param url
		 *            the url of the file to open
		 */
		public FileLoader createFileLoader(URL url) {
			return new FileLoader(new File(url.getFile()), null);
		}

		/**
		 * Helper class for being able to load a document in a separate thread.
		 * Using a separate thread will not cause the application to block
		 * during a lengthy load operation
		 */
		class FileLoader extends Thread {
			File file;
			Component owner;
			DocumentPane.DocumentPaneListener l;

			public FileLoader(File file, Component owner) {
				this.file = file;
				this.owner = owner;
			}

			public FileLoader(File file, Component owner,
					DocumentPane.DocumentPaneListener listener) {
				this(file, owner);
				this.l = listener;
			}

			@Override
			public void run() {
				try {
					Frame parent = JOptionPane.getFrameForComponent(panel);
					// XXX
					SHTMLFileOpenAction.this.panel
							.setDocumentPane(new DocumentPane(file.toURI()
									.toURL(), 0/*
												 * , renderMode
												 */));
					if (l != null) {
						SHTMLFileOpenAction.this.panel.getDocumentPane()
								.addDocumentPaneListener(l);
					}
					SHTMLFileOpenAction.this.panel
							.getTabbedPaneForDocuments()
							.setSelectedComponent(
									SHTMLFileOpenAction.this.panel
											.getTabbedPaneForDocuments()
											.add(
													SHTMLFileOpenAction.this.panel
															.getDocumentPane()
															.getDocumentName(),
													SHTMLFileOpenAction.this.panel
															.getDocumentPane()));
					SHTMLFileOpenAction.this.panel.registerDocument();
				} catch (Exception e) {
					Util.errMsg(owner, Util
							.getResourceString("unableToOpenFileError"), e);
				}
			}
		}

		public void update() {
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/** save a document */
	static class SHTMLFileSaveAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public SHTMLFileSaveAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelMultipleDocImpl.saveAction);
			this.panel = panel;
			getProperties();
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_S, InputEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent ae) {
			if (!this.panel.getDocumentPane().isNewDoc()) {
				FileSaver saver = new FileSaver(this.panel.getDocumentPane());
				saver.setName("FileSaver");
				saver.start();
			} else {
				panel.dynRes.getAction(SHTMLPanelMultipleDocImpl.saveAsAction)
						.actionPerformed(ae);
			}
			this.panel.updateActions();
		}

		/**
		 * Helper class for being able to save a document in a separate thread.
		 * Using a separate thread will not cause the application to block
		 * during a lengthy save operation
		 */
		class FileSaver extends Thread {
			DocumentPane dp;
			Component owner;

			FileSaver(DocumentPane dp) {
				setPriority(Thread.MIN_PRIORITY);
				this.dp = dp;
			}

			@Override
			public void run() {
				SHTMLFileSaveAction.this.panel.doSave(this.dp);
			}
		}

		public void update() {
			boolean isEnabled = this.panel.getEditor() != null;
			boolean saveInProgress = false;
			boolean needsSaving = false;
			if (isEnabled) {
				saveInProgress = this.panel.getDocumentPane().saveInProgress();
				needsSaving = this.panel.getDocumentPane().needsSaving();
			}
			this.setEnabled(isEnabled && needsSaving && !saveInProgress);
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	static class SHTMLFileSaveAllAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelMultipleDocImpl panel;

		public SHTMLFileSaveAllAction(SHTMLPanelMultipleDocImpl panel) {
			super(SHTMLPanelImpl.saveAllAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			int count = this.panel.getTabbedPaneForDocuments().getTabCount();
			for (int i = 0; i < count; i++) {
				this.panel.getTabbedPaneForDocuments().setSelectedIndex(i);
				this.panel.setDocumentPane((DocumentPane) this.panel
						.getTabbedPaneForDocuments().getSelectedComponent());
				if (this.panel.getDocumentPane().needsSaving()) {
					panel.dynRes
							.getAction(SHTMLPanelMultipleDocImpl.saveAction)
							.actionPerformed(ae);
				}
			}
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.getEditor() != null) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * save a document under a different name and/or location
	 * 
	 * <p>
	 * If a file already exists at the chosen location / name, the method will
	 * ask the user if the existing file shall be overwritten.
	 */
	static class SHTMLFileSaveAsAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelMultipleDocImpl panel;

		public SHTMLFileSaveAsAction(SHTMLPanelMultipleDocImpl panel) {
			super(SHTMLPanelMultipleDocImpl.saveAsAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			boolean canSave = true;
			Preferences prefs = Preferences.userNodeForPackage(this.panel
					.getClass());
			JFileChooser chooser = new JFileChooser();
			ExampleFileFilter filter = new ExampleFileFilter();
			filter.addExtension("htm");
			filter.addExtension("html");
			filter.setDescription(Util.getResourceString("htmlFileDesc"));
			chooser.setFileFilter(filter);
			String lastSaveFileName = prefs.get(SHTMLPanelImpl.FILE_LAST_SAVE,
					"");
			if (lastSaveFileName.length() > 0) {
				chooser.setCurrentDirectory(new File(lastSaveFileName)
						.getParentFile());
			}
			URL sourceUrl = this.panel.getDocumentPane().getSource();
			String fName;
			if (sourceUrl != null) {
				fName = sourceUrl.getFile();
			} else {
				fName = this.panel.getDocumentPane().getDocumentName();
				fName = Util.removeChar(fName, ' ');
			}
			if (fName.indexOf(Util.CLASS_SEPARATOR) < 0) {
				chooser.setSelectedFile(new File(fName + ".htm"));
			} else {
				chooser.setSelectedFile(new File(fName));
			}
			int result = chooser.showSaveDialog((Component) ae.getSource());
			if (result == JFileChooser.APPROVE_OPTION) {
				File selection = chooser.getSelectedFile();
				prefs.put(SHTMLPanelImpl.FILE_LAST_SAVE, selection
						.getAbsolutePath());
				if (selection.exists()) {
					String newName = selection.getName();
					canSave = Util.msg(JOptionPane.YES_NO_OPTION,
							"confirmSaveAs", "fileExistsQuery", newName, " ");
				}
				if (canSave) {
					try {
						NewFileSaver saver = new NewFileSaver(this.panel
								.getDocumentPane(), selection.toURI().toURL(),
						// XXX .getDocumentPane(), selection.toURL(),
								this.panel.getActiveTabNo());
						saver.setName("NewFileSaver");
						saver.start();
					} catch (Exception ex) {
						Util.errMsg((Component) ae.getSource(), Util
								.getResourceString("cantCreateURLError")
								+ selection.getAbsolutePath(), ex);
					}
				}
			}
			this.panel.updateActions();
		}

		/**
		 * Helper class for being able to save a document in a separate thread.
		 * Using a separate thread will not cause the application to block
		 * during a lengthy save operation
		 */
		class NewFileSaver extends Thread {
			DocumentPane dp;
			URL url;
			int activeTabNo;
			DocumentPane.DocumentPaneListener l;

			NewFileSaver(DocumentPane dp, URL url, int activeTabNo) {
				this.dp = dp;
				this.url = url;
				this.activeTabNo = activeTabNo;
			}

			NewFileSaver(DocumentPane dp, URL url, int activeTabNo,
					DocumentPane.DocumentPaneListener listener) {
				this(dp, url, activeTabNo);
				this.l = listener;
			}

			@Override
			public void run() {
				this.dp.setSource(url);
				SHTMLFileSaveAsAction.this.panel.doSave(this.dp);
				if (this.dp.saveSuccessful) {
					SHTMLFileSaveAsAction.this.panel
							.getTabbedPaneForDocuments().setTitleAt(
									SHTMLFileSaveAsAction.this.panel
											.getTabbedPaneForDocuments()
											.indexOfComponent(this.dp),
									this.dp.getDocumentName());
					if (l != null) {
						dp.addDocumentPaneListener(l);
					}
				}
			}
		}

		/**
		 * get a FileSaver object for the document currently active
		 * 
		 * @param url
		 *            the url of the file to save
		 */
		public NewFileSaver createNewFileSaver(URL url) {
			return new NewFileSaver(this.panel.getDocumentPane(), url,
					this.panel.getActiveTabNo());
		}

		/**
		 * get a FileSaver object for the document currently active
		 * 
		 * @param url
		 *            the url of the file to save
		 */
		public NewFileSaver createNewFileSaver(URL url,
				DocumentPane.DocumentPaneListener listener) {
			return new NewFileSaver(this.panel.getDocumentPane(), url,
					this.panel.getActiveTabNo(), listener);
		}

		public void update() {
			boolean isEnabled = this.panel.getEditor() != null;
			boolean saveInProgress = false;
			if (isEnabled) {
				saveInProgress = this.panel.getDocumentPane().saveInProgress();
			}
			this.setEnabled(isEnabled && !saveInProgress);
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * a slot for testing certain things conveniently during development
	 */
	public static class SHTMLTestAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public SHTMLTestAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.testAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			this.panel.getEditor().insertBreak();
		}

		public void update() {
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/** show information about SimplyHTML in a dialog */
	public static class SHTMLHelpAppInfoAction extends AbstractAction implements
			SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public SHTMLHelpAppInfoAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.aboutAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			Frame parent = JOptionPane.getFrameForComponent(this.panel);
			AboutBox dlg = new AboutBox(parent);
			Util.center(parent, dlg);
			dlg.setModal(true);
			dlg.setVisible(true);
			this.panel.repaint();
			this.panel.updateActions();
		}

		public void update() {
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}

	/**
	 * action to set a reference to the default style sheet (for being able to
	 * use an already existing style sheet without having to define named
	 * styles)
	 */
	public static class SetDefaultStyleRefAction extends AbstractAction
			implements SHTMLAction {
		/**
     *
     */
		private final SHTMLPanelImpl panel;

		public SetDefaultStyleRefAction(SHTMLPanelImpl panel) {
			super(SHTMLPanelImpl.setDefaultStyleRefAction);
			this.panel = panel;
			getProperties();
		}

		public void actionPerformed(ActionEvent ae) {
			this.panel.getSHTMLDocument().insertStyleRef();
			this.panel.updateActions();
		}

		public void update() {
			if (this.panel.isHtmlEditorActive()) {
				this.setEnabled(false);
				return;
			}
			if (this.panel.getEditor() != null
					&& !this.panel.getSHTMLDocument().hasStyleRef()) {
				this.setEnabled(true);
			} else {
				this.setEnabled(false);
			}
		}

		public void getProperties() {
			SHTMLPanelImpl.getActionProperties(this,
					(String) getValue(Action.NAME));
		}
	}
}
