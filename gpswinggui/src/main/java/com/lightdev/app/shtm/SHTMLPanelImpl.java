/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2006 Ulrich Hilger, Dimitri Polivaev
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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TimerTask;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import javax.swing.undo.UndoManager;

import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import schmitzm.swing.ExceptionDialog;
import skrueger.i8n.Translation;

import com.lightdev.app.shtm.SHTMLEditorKitActions.SetStyleAction;
import com.lightdev.app.shtm.SHTMLEditorKitActions.SetTagAction;

/**
 * Main component of application SimplyHTML.
 * 
 * <p>
 * This class constructs the main panel and all of its GUI elements such as
 * menus, etc.
 * </p>
 * 
 * <p>
 * It defines a set of inner classes creating actions which can be connected to
 * menus, buttons or instantiated individually.
 * </p>
 * 
 * @author Ulrich Hilger
 * @author Dimitri Polivaev
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the GNU General Public
 *         License, for details see file gpl.txt in the distribution package of
 *         this software
 * 
 * 
 */

public class SHTMLPanelImpl extends SHTMLPanel implements CaretListener {

	// private int renderMode = SHTMLEditorKit.RENDER_MODE_JAVA;

	/* some public constants */
	public static final String APP_TEMP_DIR = "temp";
	public static final String IMAGE_DIR = "images";
	public static final String ACTION_SELECTED_KEY = "selected";
	public static final String ACTION_SELECTED = "true";
	public static final String ACTION_UNSELECTED = "false";
	public static final String FILE_LAST_OPEN = "lastOpenFileName";
	public static final String FILE_LAST_SAVE = "lastSaveFileName";

	/** single instance of a dynamic resource for use by all */
	public DynamicResource dynRes = new DynamicResource();

	/** SimplyHTML's main resource bundle (plug-ins use their own) */
	public static TextResources textResources = null;

	public static TextResources getResources() {
		if (textResources == null)
			textResources = readDefaultResources();
		return textResources;
	}

	/**
	 * Listen to changes of the {@link Locale}
	 */
	static PropertyChangeListener localChangeListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(
					Translation.LOCALECHANGE_PROPERTY)) {
				textResources = readDefaultResources();
			}
		}
	};
	/**
	 * When the Locale changes, reread the resources for the SimplyHTML editor
	 */
	static {
		Translation.addLocaleChangeListener(localChangeListener);
	}

	/** the plug-in manager of SimplyHTML */
	public static PluginManager pluginManager; // = new

	// PluginManager(mainFrame);

	public static void setTextResources(TextResources textResources) {
		if (SHTMLPanelImpl.textResources != null)
			return;
		SHTMLPanelImpl.textResources = textResources != null ? textResources
				: readDefaultResources();
	}

	private static TextResources readDefaultResources() {
		String resLocationName = "/com/lightdev/app/shtm/resources/SimplyHTML_common.properties";
		try {
//			String propsLoc = "com/lightdev/app/shtm/resources/SimplyHTML_common.properties";
			// URL defaultPropsURL = ClassLoader.getSystemResource(propsLoc);
//			in = AtlasConfig.getResLoMan().getResourceAsStream(propsLoc);
			
			
			InputStream in = null;
//			in = new ClassResourceLoader(SHTMLPanelImpl.class).getResourceAsStream("com/lightdev/app/shtm/resources/SimplyHTML_common.properties");
			in = SHTMLPanelImpl.class.getResourceAsStream(resLocationName);
//			in = SHTMLPanelImpl.class.getClassLoader().getSystemResourceAsStream("/com/lightdev/app/shtm/resources/SimplyHTML_common.properties");
			
			final Properties props = new Properties();
			props.load(in);
			in.close();
			
			final ResourceBundle resourceBundle = ResourceBundle.getBundle(
					"com.lightdev.app.shtm.resources.SimplyHTML", Locale
							.getDefault());
			return new DefaultTextResources(resourceBundle, props);
		} catch (Exception ex) {
			throw new RuntimeException("can load resoucre '"+resLocationName+"'", ex);
		}
	}

	private SHTMLMenuBar menuBar;
	/** currently active DocumentPane */
	private DocumentPane documentPane;

	/** currently active SHTMLEditorPane */
	private SHTMLEditorPane editorPane;

	/** currently active SHTMLDocument */
	protected SHTMLDocument doc;

	/** tool bar for formatting commands */
	private JToolBar formatToolBar;

	/** tool bar for formatting commands */
	private JToolBar paraToolBar;

	/** plugin menu ID */
	public final String pluginMenuId = "plugin";

	/** help menu ID */
	public final String helpMenuId = "help";

	/** id in TextResources for a relative path to an empty menu icon */
	private String emptyIcon = "emptyIcon";

	/** watch for repeated key events */
	private RepeatKeyWatcher rkw = new RepeatKeyWatcher(40);

	/** counter for newly created documents */
	int newDocCounter = 0;

	/** reference to applicatin temp directory */
	private static File appTempDir;

	/** tool bar selector for certain tags */
	private TagSelector tagSelector;

	/** panel for plug-in display */
	SplitPanel splitPanel;

	/** indicates, whether document activation shall be handled */
	boolean ignoreActivateDoc = false;
	private JPopupMenu editorPopup;

	/**
	 * action names
	 * 
	 * these have to correspond with the keys in the resource bundle to allow
	 * for dynamic menu creation and control
	 */
	public static final String exitAction = "exit";
	public static final String undoAction = "undo";
	public static final String redoAction = "redo";
	public static final String cutAction = "cut";
	public static final String copyAction = "copy";
	public static final String pasteAction = "paste";
	public static final String selectAllAction = "selectAll";
	public static final String clearFormatAction = "clearFormat";
	public static final String fontAction = "font";
	public static final String fontFamilyAction = "fontFamily";
	public static final String fontSizeAction = "fontSize";
	public static final String fontBoldAction = "fontBold";
	public static final String fontItalicAction = "fontItalic";
	public static final String fontUnderlineAction = "fontUnderline";
	public static final String fontColorAction = "fontColor";
	public static final String helpTopicsAction = "helpTopics";
	public static final String aboutAction = "about";
	public static final String gcAction = "gc";
	public static final String elemTreeAction = "elemTree";
	public static final String testAction = "test";
	public static final String insertTableAction = "insertTable";
	public static final String formatTableAction = "formatTable";
	public static final String insertTableColAction = "insertTableCol";
	public static final String insertTableRowAction = "insertTableRow";
	public static final String appendTableRowAction = "appendTableRow";
	public static final String appendTableColAction = "appendTableCol";
	public static final String deleteTableRowAction = "deleteTableRow";
	public static final String deleteTableColAction = "deleteTableCol";
	public static final String nextTableCellAction = "nextTableCell";
	public static final String prevTableCellAction = "prevTableCell";
	// public static final String nextCellAction = "nextCell";
	// public static final String prevCellAction = "prevCell";
	public static final String toggleBulletsAction = "toggleBullets";
	public static final String toggleNumbersAction = "toggleNumbers";
	public static final String formatListAction = "formatList";
	public static final String editPrefsAction = "editPrefs";
	public static final String insertImageAction = "insertImage";
	public static final String formatImageAction = "formatImage";
	public static final String formatParaAction = "formatPara";
	public static final String editNamedStyleAction = "editNamedStyle";
	public static final String paraAlignLeftAction = "paraAlignLeft";
	public static final String paraAlignCenterAction = "paraAlignCenter";
	public static final String paraAlignRightAction = "paraAlignRight";
	public static final String insertLinkAction = "insertLink";
	public static final String editLinkAction = "editLink";
	public static final String setTagAction = "setTag";
	public static final String editAnchorsAction = "editAnchors";
	public static final String saveAllAction = "saveAll";
	public static final String documentTitleAction = "documentTitle";
	public static final String setDefaultStyleRefAction = "setDefaultStyleRef";
	public static final String findReplaceAction = "findReplace";
	public static final String setStyleAction = "setStyle";

	public static SHTMLPanelImpl getOwnerSHTMLPanel(Component c) {
		for (;;) {
			if (c == null) {
				return null;
			}
			if (c instanceof SHTMLPanelImpl) {
				return (SHTMLPanelImpl) c;
			}
			c = c.getParent();
		}
	}

	/** construct a new main application frame */
	public SHTMLPanelImpl() {
		super(new BorderLayout());
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		initActions();
		menuBar = dynRes.createMenubar(textResources, "menubar");
		editorPopup = dynRes.createPopupMenu(textResources, "popup");
		setJMenuBar(menuBar);
		customizeFrame();
		initAppTempDir();
		initPlugins();
		initDocumentPane();
		updateActions();
		initJavaHelp();
	}

	private void setJMenuBar(JMenuBar bar) {
		add(bar, BorderLayout.NORTH);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#processKeyBinding(javax.swing.KeyStroke,
	 * java.awt.event.KeyEvent, int, boolean)
	 */
	@Override
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
			int condition, boolean pressed) {
		if (super.processKeyBinding(ks, e, condition, pressed))
			return true;
		return menuBar.handleKeyBinding(ks, e, condition, pressed);
	}

	/**
	 * get the DynamicResource used in this instance of FrmMain
	 * 
	 * @return the DynamicResource
	 */
	DynamicResource getDynRes() {
		return dynRes;
	}

	/**
	 * get the temporary directory of SimplyHTML
	 * 
	 * @return the temp dir
	 */
	static File getAppTempDir() {
		return appTempDir;
	}

	/**
	 * get the file object for the document shown in the currently open
	 * DocumentPane
	 * 
	 * @return the document file
	 */
	File getCurrentFile() {
		File file = null;
		URL url = getDocumentPane().getSource();
		if (url != null) {
			file = new File(url.getFile());
		}
		return file;
	}

	/**
	 * get the name of the file for the document shown in the currently open
	 * DocumentPane
	 * 
	 * @return the document name
	 */
	String getCurrentDocName() {
		return getDocumentPane().getDocumentName();
	}

	/**
	 * Convenience method for obtaining the document text
	 * 
	 * @return returns the document text as string.
	 */
	@Override
	public String getDocumentText() {
		return getDocumentPane().getDocumentText();
	}

	Document getCurrentDocument() {
		return getDocumentPane().getDocument();
	}

	/**
	 * indicates whether or not the document needs to be saved.
	 * 
	 * @return true, if changes need to be saved
	 */
	@Override
	public boolean needsSaving() {
		return getDocumentPane().needsSaving();
	}

	/**
	 * Convenience method for clearing out the UndoManager
	 */
	void purgeUndos() {
		if (undo != null) {
			undo.discardAllEdits();
			dynRes.getAction(undoAction).putValue("enabled", Boolean.FALSE);
			dynRes.getAction(redoAction).putValue("enabled", Boolean.FALSE);
			updateFormatControls();
		}
	}

	/**
	 * Convenience method for setting the document text
	 */
	@Override
	public void setCurrentDocumentContent(String sText) {
		getDocumentPane().setDocumentText(sText);
		purgeUndos();
	}

	@Override
	public void setContentPanePreferredSize(Dimension prefSize) {
		getDocumentPane().setContentPanePreferredSize(prefSize);
	}

	/**
	 * @return returns the currently used ExtendedHTMLDocument Object
	 */
	@Override
	public HTMLDocument getDocument() {
		return doc;
	}

	/**
	 * get the DocumentPane object that is currently active
	 * 
	 * @return the active DocumentPane
	 */
	DocumentPane getCurrentDocumentPane() {
		return getDocumentPane();
	}

	/**
	 * add a DocumentPaneListener from the currently active DocumentPane (if
	 * any)
	 */
	void addDocumentPaneListener(DocumentPane.DocumentPaneListener listener) {
		if (getDocumentPane() != null) {
			// System.out.println("FrmMain.addDocumentPaneListener documentPane.source="
			// + documentPane.getSource());
			getDocumentPane().addDocumentPaneListener(listener);
		} else {
			// System.out.println("FrmMain.addDocumentPaneListener documentPane is null, did not add");
		}
	}

	/**
	 * remove a DocumentPaneListener from the currently active DocumentPane (if
	 * any)
	 */
	void removeDocumentPaneListener(DocumentPane.DocumentPaneListener listener) {
		if (getDocumentPane() != null) {
			getDocumentPane().removeDocumentPaneListener(listener);
		}
	}

	/**
	 * initialize SimplyHTML's temporary directory
	 */
	private void initAppTempDir() {
		appTempDir = new File(System.getProperty("user.home") + File.separator
				+ "." + FrmMain.APP_NAME.toLowerCase() + File.separator
				+ APP_TEMP_DIR + File.separator);
	}

	/**
	 * find plug-ins and load them accordingly, i.e. display / dock components
	 * and add plug-in menus.
	 */
	void initPlugins() {
		pluginManager = new PluginManager(this);
		JMenu pMenu = dynRes.getMenu(pluginMenuId);
		JMenu hMenu;
		if (pMenu != null) {
			Container contentPane = SHTMLPanelImpl.this;
			pluginManager.loadPlugins();
			Enumeration plugins = pluginManager.plugins();
			SHTMLPlugin pi;
			JComponent pc;
			JMenuItem pluginMenu;
			JMenuItem helpMenu;
			while (plugins.hasMoreElements()) {
				pi = (SHTMLPlugin) plugins.nextElement();
				if (pi.isActive()) {
					refreshPluginDisplay(pi);
				}
			}
		}
		adjustDividers();
	}

	/**
	 * adjust the divider sizes of SimplyHTML's SplitPanel according to
	 * visibility
	 */
	public void adjustDividers() {
		splitPanel.adjustDividerSizes();
	}

	/**
	 * watch for key events that are automatically repeated due to the user
	 * holding down a key.
	 * 
	 * <p>
	 * When a key is held down by the user, every keyPressed event is followed
	 * by a keyTyped event and a keyReleased event although the key is actually
	 * still down. I.e. it can not be determined by a keyReleased event if a key
	 * actually is released, which is why this implementation is necessary.
	 * </p>
	 */
	class RepeatKeyWatcher implements KeyListener {

		/** timer for handling keyReleased events */
		private java.util.Timer releaseTimer = new java.util.Timer();

		/** the next scheduled task for a keyReleased event */
		private ReleaseTask nextTask;

		/** time of the last keyPressed event */
		private long lastWhen = 0;

		/** time of the current KeyEvent */
		private long when;

		/** delay to distinguish between single and repeated events */
		private long delay;

		/** indicates whether or not a KeyEvent currently occurs repeatedly */
		private boolean repeating = false;

		/**
		 * construct a <code>RepeatKeyWatcher</code>
		 * 
		 * @param delay
		 *            the delay in milliseconds until a keyReleased event should
		 *            be handled
		 */
		RepeatKeyWatcher(long delay) {
			super();
			this.delay = delay;
		}

		/**
		 * handle a keyPressed event by cancelling the previous release task (if
		 * any) and indicating repeated key press as applicable.
		 */
		public void keyPressed(KeyEvent e) {
			if (nextTask != null) {
				nextTask.cancel();
			}
			when = e.getWhen();
			if ((when - lastWhen) <= delay) {
				repeating = true;
			} else {
				repeating = false;
			}
			lastWhen = when;
		}

		/**
		 * handle a keyReleased event by scheduling a <code>ReleaseTask</code>.
		 */
		public void keyReleased(KeyEvent e) {
			nextTask = new ReleaseTask();
			releaseTimer.schedule(nextTask, delay);
		}

		public void keyTyped(KeyEvent e) {
		}

		/**
		 * indicate whether or not a key is being held down
		 * 
		 * @return true if a key is being held down, false if not
		 */
		boolean isRepeating() {
			return repeating;
		}

		/**
		 * Task to be executed when a key is released
		 */
		private class ReleaseTask extends TimerTask implements Runnable {
			@Override
			public void run() {
				if (EventQueue.isDispatchThread()) {
					repeating = false;
					updateFormatControls();
				} else {
					try {
						EventQueue.invokeAndWait(this);
					} catch (InterruptedException e) {
					} catch (InvocationTargetException e) {
					}
				}
			}
		}
	}

	public void clearDockPanels() {
		splitPanel.removeAllOuterPanels();
	}

	/**
	 * refresh the display for a given plug-in
	 * 
	 * @param pi
	 *            the plug-in to refresh
	 */
	public void refreshPluginDisplay(SHTMLPlugin pi) {
		JMenu pMenu = dynRes.getMenu(pluginMenuId);
		JMenu hMenu = dynRes.getMenu(helpMenuId);
		JMenuItem pluginMenu = pi.getPluginMenu();
		JMenuItem helpMenu = pi.getHelpMenu();
		JTabbedPane p = null;
		Preferences prefs;
		if (pi.isActive()) {
			JComponent pc = pi.getComponent();
			if (pc != null) {
				int panelNo = SplitPanel.WEST;
				double loc = 0.3;
				switch (pi.getDockLocation()) {
				case SHTMLPlugin.DOCK_LOCATION_LEFT:
					break;
				case SHTMLPlugin.DOCK_LOCATION_RIGHT:
					panelNo = SplitPanel.EAST;
					loc = 0.7;
					break;
				case SHTMLPlugin.DOCK_LOCATION_BOTTOM:
					panelNo = SplitPanel.SOUTH;
					loc = 0.7;
					break;
				case SHTMLPlugin.DOCK_LOCATION_TOP:
					panelNo = SplitPanel.NORTH;
					break;
				}
				p = splitPanel.getPanel(panelNo);
				p.setVisible(true);
				p.add(pi.getGUIName(), pc);
				if (((panelNo == SplitPanel.WEST) && splitPanel
						.getDivLoc(panelNo) < this.getWidth() / 10)
						|| ((panelNo == SplitPanel.NORTH) && splitPanel
								.getDivLoc(panelNo) < this.getHeight() / 10)
						|| ((panelNo == SplitPanel.EAST) && splitPanel
								.getDivLoc(panelNo) > this.getWidth()
								- (this.getWidth() / 10))
						|| ((panelNo == SplitPanel.SOUTH) && splitPanel
								.getDivLoc(panelNo) > this.getHeight()
								- (this.getHeight() / 10))) {
					splitPanel.setDivLoc(panelNo, loc);
				}
			}
			if (pluginMenu != null) {
				Icon menuIcon = pluginMenu.getIcon();
				if (menuIcon == null) {
					URL url = DynamicResource.getResource(textResources,
							emptyIcon);
					if (url != null) {
						menuIcon = new ImageIcon(url);
						pluginMenu.setIcon(new ImageIcon(url));
					}
				}
				pMenu.add(pluginMenu);
			}
			if (helpMenu != null) {
				// System.out.println("FrmMain.refreshPluginDisplay insert helpMenu");
				if (helpMenu.getSubElements().length > 0) {
					Icon menuIcon = helpMenu.getIcon();
					if (menuIcon == null) {
						URL url = DynamicResource.getResource(textResources,
								emptyIcon);
						if (url != null) {
							menuIcon = new ImageIcon(url);
							helpMenu.setIcon(new ImageIcon(url));
						}
					}
				}
				hMenu.insert(helpMenu, hMenu.getItemCount() - 2);
			}
			SwingUtilities.invokeLater(new PluginInfo(pi));
		} else {
			if (pluginMenu != null) {
				pMenu.remove(pluginMenu);
			}
			if (helpMenu != null) {
				hMenu.remove(helpMenu);
			}
		}
	}

	class PluginInfo implements Runnable {
		SHTMLPlugin pi;

		PluginInfo(SHTMLPlugin pi) {
			this.pi = pi;
		}

		public void run() {
			pi.showInitialInfo();
		}
	}

	/**
	 * get a <code>HelpBroker</code> for our application, store it for later use
	 * and connect it to the help menu.
	 */
	private void initJavaHelp() {
		// try {
		// JMenuItem mi = dynRes.getMenuItem(helpTopicsAction);
		// if (mi == null)
		// return;
		// SHTMLHelpBroker.initJavaHelpItem(mi, "item15");
		// } catch (Throwable e) {
		// System.err.println("Simply HTML : Warning : loading help failed.");
		// // --Dan
		// // Util.errMsg(this,
		// // Util.getResourceString("helpNotFoundError"),
		// // e);
		// }
	}

	protected void initDocumentPane() {
		// TODO
	}

	/**
	 * instantiate Actions and put them into the commands Hashtable for later
	 * use along with their action commands.
	 * 
	 * This is hard coded as Actions need to be instantiated hard coded anyway,
	 * so we do the storage in <code>commands</code> right away.
	 */
	protected void initActions() {
		dynRes.addAction(setDefaultStyleRefAction,
				new SHTMLEditorKitActions.SetDefaultStyleRefAction(this));
		dynRes.addAction(documentTitleAction,
				new SHTMLEditorKitActions.DocumentTitleAction(this));
		dynRes.addAction(editAnchorsAction,
				new SHTMLEditorKitActions.EditAnchorsAction(this));
		dynRes.addAction(setTagAction, new SHTMLEditorKitActions.SetTagAction(
				this));
		dynRes.addAction(editLinkAction,
				new SHTMLEditorKitActions.EditLinkAction(this));
		dynRes.addAction(prevTableCellAction,
				new SHTMLEditorKitActions.PrevTableCellAction(this));
		dynRes.addAction(nextTableCellAction,
				new SHTMLEditorKitActions.NextTableCellAction(this));
		dynRes.addAction(editNamedStyleAction,
				new SHTMLEditorKitActions.EditNamedStyleAction(this));
		dynRes.addAction(clearFormatAction,
				new SHTMLEditorKitActions.ClearFormatAction(this));
		dynRes.addAction(formatParaAction,
				new SHTMLEditorKitActions.FormatParaAction(this));
		dynRes.addAction(formatImageAction,
				new SHTMLEditorKitActions.FormatImageAction(this));
		dynRes.addAction(insertImageAction,
				new SHTMLEditorKitActions.InsertImageAction(this));
		dynRes.addAction(editPrefsAction,
				new SHTMLEditorKitActions.SHTMLEditPrefsAction(this));
		dynRes.addAction(toggleBulletsAction,
				new SHTMLEditorKitActions.ToggleListAction(this,
						toggleBulletsAction, HTML.Tag.UL));
		dynRes.addAction(toggleNumbersAction,
				new SHTMLEditorKitActions.ToggleListAction(this,
						toggleNumbersAction, HTML.Tag.OL));
		dynRes.addAction(formatListAction,
				new SHTMLEditorKitActions.FormatListAction(this));
		dynRes.addAction(ManagePluginsAction.managePluginsAction,
				new ManagePluginsAction());
		dynRes.addAction(elemTreeAction,
				new SHTMLEditorKitActions.ShowElementTreeAction(this));
		dynRes.addAction(gcAction, new SHTMLEditorKitActions.GCAction(this));
		dynRes
				.addAction(undoAction, new SHTMLEditorKitActions.UndoAction(
						this));
		dynRes
				.addAction(redoAction, new SHTMLEditorKitActions.RedoAction(
						this));
		dynRes.addAction(cutAction,
				new SHTMLEditorKitActions.SHTMLEditCutAction(this));
		dynRes.addAction(copyAction,
				new SHTMLEditorKitActions.SHTMLEditCopyAction(this));
		dynRes.addAction(pasteAction,
				new SHTMLEditorKitActions.SHTMLEditPasteAction(this));
		dynRes.addAction(selectAllAction,
				new SHTMLEditorKitActions.SHTMLEditSelectAllAction(this));
		dynRes.addAction(aboutAction,
				new SHTMLEditorKitActions.SHTMLHelpAppInfoAction(this));
		dynRes
				.addAction(fontAction, new SHTMLEditorKitActions.FontAction(
						this));
		dynRes.addAction(fontFamilyAction,
				new SHTMLEditorKitActions.FontFamilyAction(this));
		dynRes.addAction(fontSizeAction,
				new SHTMLEditorKitActions.FontSizeAction(this));
		dynRes.addAction(insertTableAction,
				new SHTMLEditorKitActions.InsertTableAction(this));
		dynRes.addAction(insertTableRowAction,
				new SHTMLEditorKitActions.InsertTableRowAction(this));
		dynRes.addAction(insertTableColAction,
				new SHTMLEditorKitActions.InsertTableColAction(this));
		dynRes.addAction(appendTableColAction,
				new SHTMLEditorKitActions.AppendTableColAction(this));
		dynRes.addAction(appendTableRowAction,
				new SHTMLEditorKitActions.AppendTableRowAction(this));
		dynRes.addAction(deleteTableRowAction,
				new SHTMLEditorKitActions.DeleteTableRowAction(this));
		dynRes.addAction(deleteTableColAction,
				new SHTMLEditorKitActions.DeleteTableColAction(this));
		dynRes.addAction(formatTableAction,
				new SHTMLEditorKitActions.FormatTableAction(this));
		dynRes.addAction(fontBoldAction, new SHTMLEditorKitActions.BoldAction(
				this));
		dynRes.addAction(fontItalicAction,
				new SHTMLEditorKitActions.ItalicAction(this));
		dynRes.addAction(fontUnderlineAction,
				new SHTMLEditorKitActions.UnderlineAction(this));
		dynRes.addAction(fontColorAction,
				new SHTMLEditorKitActions.FontColorAction(this));
		dynRes.addAction(paraAlignLeftAction,
				new SHTMLEditorKitActions.ToggleAction(this,
						paraAlignLeftAction, CSS.Attribute.TEXT_ALIGN,
						Util.CSS_ATTRIBUTE_ALIGN_LEFT));
		dynRes.addAction(paraAlignCenterAction,
				new SHTMLEditorKitActions.ToggleAction(this,
						paraAlignCenterAction, CSS.Attribute.TEXT_ALIGN,
						Util.CSS_ATTRIBUTE_ALIGN_CENTER));
		dynRes.addAction(paraAlignRightAction,
				new SHTMLEditorKitActions.ToggleAction(this,
						paraAlignRightAction, CSS.Attribute.TEXT_ALIGN,
						Util.CSS_ATTRIBUTE_ALIGN_RIGHT));
		dynRes.addAction(testAction, new SHTMLEditorKitActions.SHTMLTestAction(
				this));
	}

	/**
	 * update all actions
	 */
	public void updateActions() {
		Action action;
		Enumeration actions = dynRes.getActions();
		while (actions.hasMoreElements()) {
			action = (Action) actions.nextElement();
			if (action instanceof SHTMLAction) {
				((SHTMLAction) action).update();
			}
		}
	}

	/** customize the frame to our needs */
	protected void customizeFrame() {
		Container contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());

		splitPanel = new SplitPanel();
		for (int i = 0; i < 4; i++) {
			JTabbedPane p = new JTabbedPane();
			p.setVisible(false);
			splitPanel.addComponent(p, i);
		}

		JPanel toolBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		toolBarPanel.add(createToolBar("toolBar"));
		formatToolBar = createToolBar("formatToolBar");
		paraToolBar = createToolBar("paraToolBar");
		toolBarPanel.add(formatToolBar);
		toolBarPanel.add(paraToolBar);
		if (Util.getPreference("show_toolbars", "true")
				.equalsIgnoreCase("true")) // --Dan
			contentPane.add(toolBarPanel, BorderLayout.NORTH);

		// contentPane.add(workPanel, BorderLayout.CENTER);
		contentPane.add(splitPanel, BorderLayout.CENTER);
		// contentPane.add(workPanel);
		add(contentPane, BorderLayout.CENTER);
	}

	/**
	 * Create a tool bar. This reads the definition of a tool bar from the
	 * associated resource file.
	 * 
	 * @param nm
	 *            the name of the tool bar definition in the resource file
	 * 
	 * @return the created tool bar
	 */
	JToolBar createToolBar(String nm) {
		String[] itemKeys = Util.tokenize(Util.getResourceString(textResources,
				nm), " ");
		JToolBar toolBar = new JToolBar();
		toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
		for (int i = 0; i < itemKeys.length; i++) {
			/** special handling for separators */
			final String itemKey = itemKeys[i];
			createToolbarItem(toolBar, itemKey);
		}
		return toolBar;
	}

	protected void createToolbarItem(JToolBar toolBar, final String itemKey) {
		ToggleBorderListener tbl = new ToggleBorderListener();
		Action action;
		AbstractButton newButton;
		final Dimension buttonSize = new Dimension(24, 24);
		final Dimension comboBoxSize = new Dimension(300, 24);
		final Dimension separatorSize = new Dimension(3, 24);
		JSeparator separator;
		if (itemKey.equals(DynamicResource.menuSeparatorKey)) {
			separator = new JSeparator(SwingConstants.VERTICAL);
			separator.setMaximumSize(separatorSize);
			toolBar.add(separator);
		}
		/**
		 * special handling for list elements in the tool bar
		 */
		else if (itemKey.equalsIgnoreCase(fontFamilyAction)) {
			FontFamilyPicker fontFamily = new FontFamilyPicker();
			fontFamily.setPreferredSize(new Dimension(180, 23));
			fontFamily.setAction(dynRes.getAction(fontFamilyAction));
			fontFamily.setMaximumSize(comboBoxSize);
			toolBar.add(fontFamily);
		} else if (itemKey.equalsIgnoreCase(fontSizeAction)) {
			FontSizePicker fontSize = new FontSizePicker();
			fontSize.setPreferredSize(new Dimension(50, 23));
			fontSize.setAction(dynRes.getAction(fontSizeAction));
			fontSize.setMaximumSize(comboBoxSize);
			toolBar.add(fontSize);
		} else if (itemKey.equalsIgnoreCase(setTagAction)) {
			tagSelector = new TagSelector();
			tagSelector.setAction(dynRes.getAction(setTagAction));
			/*
			 * styleSelector = new StyleSelector(HTML.Attribute.CLASS);
			 * styleSelector.setPreferredSize(new Dimension(110, 23));
			 * styleSelector.setAction(dynRes.getAction(setStyleAction));
			 * styleSelector.setMaximumSize(comboBoxSize);
			 * jtpDocs.addChangeListener(styleSelector);
			 */
			toolBar.add(tagSelector);
		} else if (itemKey.equalsIgnoreCase(helpTopicsAction)) {
			// try {
			// newButton = SHTMLHelpBroker.createHelpButton("item15");
			// final Icon icon = DynamicResource.getIconForCommand(
			// getResources(), helpTopicsAction);
			// newButton.setIcon(icon);
			// newButton.setToolTipText(Util
			// .getResourceString(helpTopicsAction
			// + dynRes.toolTipSuffix));
			// toolBar.add(newButton);
			// } catch (Exception ex) {
			// } catch (java.lang.NoClassDefFoundError e) {
			// } // When one of the help components is not there
		} else {
			action = dynRes.getAction(itemKey);
			/**
			 * special handling for JToggleButtons in the tool bar
			 */
			if (action instanceof AttributeComponent) {
				newButton = new JToggleButton("", (Icon) action
						.getValue(Action.SMALL_ICON));
				newButton.addMouseListener(tbl);
				newButton.setAction(action);
				newButton.setText("");
				// newButton.setActionCommand("");
				newButton.setBorderPainted(false);
				action
						.addPropertyChangeListener(new ToggleActionChangedListener(
								(JToggleButton) newButton));
				Icon si = DynamicResource.getIconForName(textResources, action
						.getValue(action.NAME)
						+ DynamicResource.selectedIconSuffix);
				if (si != null) {
					newButton.setSelectedIcon(si);
				}
				newButton.setMargin(new Insets(0, 0, 0, 0));
				newButton.setIconTextGap(0);
				newButton.setContentAreaFilled(false);
				newButton.setHorizontalAlignment(SwingConstants.CENTER);
				newButton.setVerticalAlignment(SwingConstants.CENTER);
				toolBar.add(newButton);
			}
			/**
			 * this is the usual way to add tool bar buttons finally
			 */
			else {
				newButton = toolBar.add(action);
			}
			newButton.setMinimumSize(buttonSize);
			newButton.setPreferredSize(buttonSize);
			newButton.setMaximumSize(buttonSize);
			newButton.setFocusPainted(false);
			newButton.setRequestFocusEnabled(false);
		}
	}

	/**
	 * displays or removes an etched border around JToggleButtons this listener
	 * is registered with.
	 */
	private class ToggleBorderListener implements MouseListener {
		private EtchedBorder border = new EtchedBorder(EtchedBorder.LOWERED);
		private JToggleButton button;

		public void mouseClicked(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
			Object src = e.getSource();
			if (src instanceof JToggleButton) {
				button = (JToggleButton) src;
				if (button.isEnabled()) {
					((JToggleButton) src).setBorder(border);
				}
			}
		}

		public void mouseExited(MouseEvent e) {
			Object src = e.getSource();
			if (src instanceof JToggleButton) {
				((JToggleButton) src).setBorder(null);
			}
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}
	}

	/**
	 * register FrmMain as an object which has interest in events from a given
	 * document pane
	 */
	protected void registerDocument() {
		doc.addUndoableEditListener(undoHandler);
		getEditor().addCaretListener(this);
		getEditor().addKeyListener(rkw);
	}

	/**
	 * remove FrmMain as a registered object from a given document pane and its
	 * components
	 * 
	 * remove all plug-ins owned by this FrmMain from SimplyHTML objects too
	 */
	protected void unregisterDocument() {
		if (getEditor() == null) return;
		
		getEditor().removeCaretListener(this);
		getEditor().removeKeyListener(rkw);
		
		if (doc != null) {
			doc.removeUndoableEditListener(undoHandler);
		}
		getDocumentPane().removeAllListeners(); // for plug-in removal from any
		// documentPane that is about to
		// close
		// System.out.println("FrmMain unregister document documentPane.name=" +
		// documentPane.getDocumentName());
	}

	/**
	 * save a document and catch possible errors
	 * 
	 * this is shared by save and saveAs so we put it here to avoid redundancy
	 * 
	 * @param documentPane
	 *            the document pane containing the document to save
	 */
	void doSave(DocumentPane documentPane) {
		try {
			documentPane.saveDocument();
		}
		/**
		 * this exception should never happen as the menu allows to save a
		 * document only if a name has been set. For new documents, whose name
		 * is not set, only save as is enabled anyway.
		 * 
		 * Just in case this is changed without remembering why it was designed
		 * that way, we catch the exception here.
		 */
		catch (DocNameMissingException e) {
			Util.errMsg(this, Util.getResourceString(textResources,
					"docNameMissingError"), e);
		}
	}

	boolean isHtmlEditorActive() {
		return getDocumentPane() != null
				&& getDocumentPane().getSelectedTab() == DocumentPane.VIEW_TAB_HTML;
	}

	/**
	 * get action properties from the associated resource bundle
	 * 
	 * @param action
	 *            the action to apply properties to
	 * @param cmd
	 *            the name of the action to get properties for
	 */
	public static void getActionProperties(Action action, String cmd) {
		Icon icon = DynamicResource.getIconForCommand(textResources, cmd);
		if (icon != null) {
			action.putValue(Action.SMALL_ICON, icon);
		}
		/*
		 * else { action.putValue(Action.SMALL_ICON, emptyIcon); }
		 */
		String toolTip = Util.getResourceString(textResources, cmd
				+ DynamicResource.toolTipSuffix);
		if (toolTip != null) {
			action.putValue(Action.SHORT_DESCRIPTION, toolTip);
		}
	}

	/* ---------- undo/redo implementation ----------------------- */

	/** Listener for edits on a document. */
	private UndoableEditListener undoHandler = new UndoHandler();

	/** UndoManager that we add edits to. */
	private UndoManager undo = new UndoManager();

	/** inner class for handling undoable edit events */
	class UndoHandler implements UndoableEditListener {
		/**
		 * Messaged when the Document has created an edit, the edit is added to
		 * <code>undo</code>, an instance of UndoManager.
		 */
		public void undoableEditHappened(UndoableEditEvent e) {
			// ignore all events happened when the html source code pane is open
			if (getCurrentDocumentPane().getSelectedTab() != DocumentPane.VIEW_TAB_LAYOUT) {
				return;
			}
			getUndo().addEdit(e.getEdit());
		}
	}

	/**
	 * caret listener implementation to track format changes
	 */
	public void caretUpdate(CaretEvent e) {
		if (!rkw.isRepeating()) {
			EventQueue.invokeLater(new Runnable() {

				public void run() {
					updateFormatControls();
				}
			});
		}
	}

	/**
	 * update any controls that relate to formats at the current caret position
	 */
	void updateFormatControls() {
		updateAToolBar(formatToolBar);
		updateAToolBar(paraToolBar);
		if (tagSelector != null) {
			SetTagAction sta = (SetTagAction) tagSelector.getAction();
			sta.setIgnoreActions(true);
			Element e = doc.getParagraphElement(getEditor().getCaretPosition());
			tagSelector.setSelectedTag(e.getName());
			sta.setIgnoreActions(false);
		}
	}

	private void updateAToolBar(JToolBar bar) {
		Component c;
		Action action;
		int count = bar.getComponentCount();
		AttributeSet a = getMaxAttributes(getEditor(), null);
		for (int i = 0; i < count; i++) {
			c = bar.getComponentAtIndex(i);
			if (c instanceof AttributeComponent) {
				if (c instanceof StyleSelector) {
					SetStyleAction ssa = (SetStyleAction) ((StyleSelector) c)
							.getAction();
					final AttributeSet oldAttibuteSet = ((AttributeComponent) c)
							.getValue();
					if (!a.isEqual(oldAttibuteSet)) {
						ssa.setIgnoreActions(true);
						((AttributeComponent) c).setValue(a);
						ssa.setIgnoreActions(false);
					}
				} else {
					((AttributeComponent) c).setValue(a);
				}
			} else if (c instanceof AbstractButton) {
				action = ((AbstractButton) c).getAction();
				if ((action != null) && (action instanceof AttributeComponent)) {
					((AttributeComponent) action).setValue(a);
				}
			}
		}
	}

	/**
	 * a JComboBox for selecting a font family names from those available in the
	 * system.
	 */
	class FontFamilyPicker extends JComboBox implements AttributeComponent {

		/** switch for the action listener */
		private boolean ignoreActions = false;

		FontFamilyPicker() {

			/**
			 * add the font family names available in the system to the combo
			 * box
			 */
			super(GraphicsEnvironment.getLocalGraphicsEnvironment()
					.getAvailableFontFamilyNames());
		}

		boolean ignore() {
			return ignoreActions;
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
			ignoreActions = true;
			final String newSelection = Util.styleSheet().getFont(a)
					.getFamily();
			setSelectedItem(newSelection);
			ignoreActions = false;
			return true;
		}

		/**
		 * get the value of this <code>AttributeComponent</code>
		 * 
		 * @return the value selected from this component
		 */
		public AttributeSet getValue() {
			SimpleAttributeSet set = new SimpleAttributeSet();
			Util.styleSheet().addCSSAttribute(set, CSS.Attribute.FONT_FAMILY,
					(String) getSelectedItem());
			set.addAttribute(HTML.Attribute.FACE, (String) getSelectedItem());
			return set;
		}

		public AttributeSet getValue(boolean includeUnchanged) {
			return getValue();
		}
	}

	/**
	 * a JComboBox for selecting a font size
	 */
	static final String[] FONT_SIZES = new String[] { "8", "10", "12", "14",
			"18", "24" };

	class FontSizePicker extends JComboBox implements AttributeComponent {
		private boolean ignoreActions = false;
		final private Object key;

		FontSizePicker() {
			/**
			 * add font sizes to the combo box
			 */
			super(FONT_SIZES);
			this.key = CSS.Attribute.FONT_SIZE;
		}

		boolean ignore() {
			return ignoreActions;
		}

		/**
		 * set the value of this combo box
		 * 
		 * @param a
		 *            the set of attributes possibly having a font size
		 *            attribute this pick list could display
		 * 
		 * @return true, if the set of attributes had a font size attribute,
		 *         false if not
		 */
		public boolean setValue(AttributeSet a) {
			ignoreActions = true;
			final int size = Util.styleSheet().getFont(a).getSize();
			String newSelection = Integer.toString(size);
			setSelectedItem(newSelection);
			ignoreActions = false;
			return true;
		}

		/**
		 * get the value of this <code>AttributeComponent</code>
		 * 
		 * @return the value selected from this component
		 */
		public AttributeSet getValue() {
			SimpleAttributeSet set = new SimpleAttributeSet();
			final String relativeSize = Integer
					.toString(getSelectedIndex() + 1);
			set.addAttribute(HTML.Attribute.SIZE, relativeSize);
			Util.styleSheet().addCSSAttributeFromHTML(set,
					CSS.Attribute.FONT_SIZE, relativeSize /* + "pt" */);
			return set;
		}

		public AttributeSet getValue(boolean includeUnchanged) {
			return getValue();
		}
	}

	/**
	 * a listener for property change events on ToggleFontActions
	 */
	private class ToggleActionChangedListener implements PropertyChangeListener {

		JToggleButton button;

		ToggleActionChangedListener(JToggleButton button) {
			super();
			this.button = button;
		}

		public void propertyChange(PropertyChangeEvent e) {
			String propertyName = e.getPropertyName();
			if (e.getPropertyName().equals(SHTMLPanelImpl.ACTION_SELECTED_KEY)) {
				// System.out.println("propertyName=" + propertyName +
				// " newValue=" + e.getNewValue());
				if (e.getNewValue().toString().equals(
						SHTMLPanelImpl.ACTION_SELECTED)) {
					button.setSelected(true);
				} else {
					button.setSelected(false);
				}
			}
		}
	}

	public AttributeSet getMaxAttributes(final int caretPosition) {
		final Element paragraphElement = getSHTMLDocument()
				.getParagraphElement(caretPosition);
		final StyleSheet styleSheet = getSHTMLDocument().getStyleSheet();
		return SHTMLPanelImpl.getMaxAttributes(paragraphElement, styleSheet);
	}

	/**
	 * Get all attributes that can be found in the element tree starting at the
	 * highest parent down to the character element at the current position in
	 * the document. Combine element attributes with attributes from the style
	 * sheet.
	 * 
	 * @param editorPane
	 *            the editor pane to combine attributes from
	 * 
	 * @return the resulting set of combined attributes
	 */
	AttributeSet getMaxAttributes(SHTMLEditorPane editorPane, String elemName) {
		Element e = doc.getCharacterElement(editorPane.getSelectionStart());
		StyleSheet s = doc.getStyleSheet();
		if (elemName != null && elemName.length() > 0) {
			e = Util.findElementUp(elemName, e);
			return getMaxAttributes(e, s);
		}
		final MutableAttributeSet maxAttributes = (MutableAttributeSet) getMaxAttributes(
				e, s);
		final StyledEditorKit editorKit = (StyledEditorKit) editorPane
				.getEditorKit();
		final MutableAttributeSet inputAttributes = editorKit
				.getInputAttributes();
		maxAttributes.addAttributes(inputAttributes);
		return maxAttributes;
	}

	Frame getMainFrame() {
		return JOptionPane.getFrameForComponent(SHTMLPanelImpl.this);
	}

	protected static AttributeSet getMaxAttributes(Element e, StyleSheet s) {
		SimpleAttributeSet a = new SimpleAttributeSet();
		Element cElem = e;
		AttributeSet attrs;
		Vector elements = new Vector();
		Object classAttr;
		String styleName;
		String elemName;
		while (e != null) {
			elements.insertElementAt(e, 0);
			e = e.getParentElement();
		}
		for (int i = 0; i < elements.size(); i++) {
			e = (Element) elements.elementAt(i);
			classAttr = e.getAttributes().getAttribute(HTML.Attribute.CLASS);
			elemName = e.getName();
			styleName = elemName;
			if (classAttr != null) {
				styleName = elemName + "." + classAttr.toString();
				a.addAttribute(HTML.Attribute.CLASS, classAttr);
			}
			// System.out.println("getMaxAttributes name=" + styleName);
			attrs = s.getStyle(styleName);
			if (attrs != null) {
				a.addAttributes(Util.resolveAttributes(attrs));
			} else {
				attrs = s.getStyle(elemName);
				if (attrs != null) {
					a.addAttributes(Util.resolveAttributes(attrs));
				}
			}
			a.addAttributes(Util.resolveAttributes(e.getAttributes()));
		}
		if (cElem != null) {
			// System.out.println("getMaxAttributes cElem.name=" +
			// cElem.getName());
			a.addAttributes(cElem.getAttributes());
		}
		// System.out.println(" ");
		// de.calcom.cclib.html.HTMLDiag hd = new
		// de.calcom.cclib.html.HTMLDiag();
		// hd.listAttributes(a, 4);
		return new AttributeMapper(a)
				.getMappedAttributes(AttributeMapper.toJava);
	}

	/**
	 * @param documentPane
	 *            The documentPane to set.
	 */
	void setDocumentPane(DocumentPane documentPane) {
		this.documentPane = documentPane;
	}

	/**
	 * @return Returns the documentPane.
	 */
	DocumentPane getDocumentPane() {
		return documentPane;
	}

	protected void setEditorPane(SHTMLEditorPane editorPane) {
		if (editorPane != null) {
			editorPane.setPopup(editorPopup);
		}
		this.editorPane = editorPane;
	}

	/**
	 * @return Returns the editorPane.
	 */
	SHTMLEditorPane getEditor() {
		return (SHTMLEditorPane) getEditorPane();
	}

	public JEditorPane getEditorPane() {
		return editorPane;
	}

	public JEditorPane getSourceEditorPane() {
		return (JEditorPane) getDocumentPane().getHtmlEditor();
	}

	/**
	 * @return Returns the doc.
	 */
	SHTMLDocument getSHTMLDocument() {
		return doc;
	}

	/**
	 * @param undo
	 *            The undo to set.
	 */
	void setUndo(UndoManager undo) {
		this.undo = undo;
	}

	/**
	 * @return Returns the undo.
	 */
	UndoManager getUndo() {
		return undo;
	}

	/**
	 * @param tagSelector
	 *            The tagSelector to set.
	 */
	void setTagSelector(TagSelector tagSelector) {
		this.tagSelector = tagSelector;
	}

	/**
	 * @return Returns the tagSelector.
	 */
	TagSelector getTagSelector() {
		return tagSelector;
	}

	void savePrefs() {
		splitPanel.savePrefs();
	}

	boolean close() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#requestFocus()
	 */
	public JEditorPane getMostRecentFocusOwner() {
		if (getDocumentPane() != null) {
			return getDocumentPane().getMostRecentFocusOwner();
		}
		return null;
	}

	/* ---------- font manipulation code end ------------------ */
	public int getCaretPosition() {
		return getEditor().getCaretPosition();
	}

	public JMenuBar getMenuBar() {
		return menuBar;
	}

	public void switchViews() {
		getDocumentPane().switchViews();
	}

}
