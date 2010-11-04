/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.swing.internal;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasStatusDialogInterface;
import org.geopublishing.atlasViewer.exceptions.AtlasImportException;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;
import org.geotools.resources.SwingUtilities;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.util.SimpleInternationalString;
import org.opengis.util.InternationalString;

import schmitzm.io.IOUtil;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.SwingUtil;
import skrueger.swing.OkButton;
import skrueger.swing.formatter.MbDecimalFormatter;

// TODO move to schmitzm
public class AtlasStatusDialog implements AtlasStatusDialogInterface {

	static public String CANCEL_PROPERTY = "atlas status dialog cancelled";
	final static private Logger LOGGER = Logger
			.getLogger(AtlasStatusDialog.class);

	/**
	 * If <code>false</code>, the dialog has no cancel button.
	 */
	private boolean cancelAllowed;

	@Override
	public void setCancelAllowed(boolean cancelAllowed) {
		this.cancelAllowed = cancelAllowed;
		cancelButton.setEnabled(cancelAllowed);
		cancelButton.setVisible(cancelAllowed);
	}

	@Override
	public boolean isCancelAllowed() {
		return cancelAllowed;
	}

	/**
	 * Initial width for the progress window, in pixels.
	 */
	private static final int WIDTH = 460;

	/**
	 * Initial height for the progress window, in pixels. Increase this value if
	 * some component (e.g. the "Cancel" button) seems truncated. The current
	 * value has been tested for Metal look and feel.
	 */
	private static final int HEIGHT = 160;

	/**
	 * The height of the text area containing the warning messages (if any).
	 */
	private static final int WARNING_HEIGHT = 120;

	/**
	 * Horizontal margin width, in pixels.
	 */
	private static final int HMARGIN = 8;

	/**
	 * Vertical margin height, in pixels.
	 */
	private static final int VMARGIN = 6;

	/**
	 * Amount of spaces to put in the margin of the warning messages window.
	 */
	private static final int WARNING_MARGIN = 6;

	/**
	 * The progress window as a {@link JDialog} or a {@link JInternalFrame},
	 * depending of the parent component.
	 */
	private final JDialog window;

	/**
	 * The container where to add components like the progress bar.
	 */
	private final JComponent content;

	/**
	 * The progress bar. Values ranges from 0 to 100.
	 */
	private final JProgressBar progressBar;

	/**
	 * A description of the undergoing operation. Examples: "Reading header",
	 * "Reading data", <cite>etc.</cite>
	 */
	private final JLabel description;

	/**
	 * The cancel button.
	 */
	private final JButton cancelButton;

	/**
	 * Component where to display warnings. The actual component class is
	 * {@link JTextArea}. But we declare {@link JComponent} here in order to
	 * avoid class loading before needed.
	 */
	private JComponent warningArea;

	/**
	 * The source of the last warning message. Used in order to avoid to repeat
	 * the source for all subsequent warning messages, if the source didn't
	 * changed.
	 */
	private String lastSource;

	/**
	 * {@code true} if the action has been canceled.
	 */
	private volatile boolean canceled;

	private Window parentWindow;

	private boolean warningOccured;

	private JButton ok;

	private HashSet<ActionListener> listeners = new HashSet<ActionListener>();

	/**
	 * Creates a window for reporting progress. The window will not appears
	 * immediately. It will appears only when the {@link #started} method will
	 * be invoked.
	 * 
	 * @param parent
	 *            The parent component, or {@code null} if none.
	 * 
	 *            TODO make working in no X11 environment!
	 */
	public AtlasStatusDialog(final Component parent) {
		/*
		 * Creates the window containing the components.
		 */
		Dimension parentSize;
		final Vocabulary resources = Vocabulary
				.getResources(parent != null ? parent.getLocale() : null);
		final String title = resources.getString(VocabularyKeys.PROGRESSION);

		parentWindow = SwingUtil.getParentWindow(parent);
		if (parentWindow != null)
			parentSize = parentWindow.getSize();
		else
			parentSize = Toolkit.getDefaultToolkit().getScreenSize();
		window = new JDialog(parentWindow, title);
		content = (JComponent) window.getContentPane();
		window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		window.setResizable(false);

		window.setBounds((parentSize.width - WIDTH) / 2,
				(parentSize.height - HEIGHT) / 2, WIDTH, HEIGHT);
		/*
		 * Creates the label that is going to display the undergoing operation.
		 * This label is initially empty.
		 */
		description = new JLabel();
		description.setHorizontalAlignment(SwingConstants.CENTER);
		/*
		 * Creates the progress bar.
		 */
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(3, 6, 3, 6),
				progressBar.getBorder()));
		/*
		 * Creates the ok button.
		 */
		ok = new OkButton();
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		ok.setEnabled(false);
		/*
		 * Creates the cancel button.
		 */
		cancelButton = new JButton(resources.getString(VocabularyKeys.CANCEL));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setCanceled(true);
			}
		});
		cancelButton.setEnabled(cancelAllowed);
		cancelButton.setVisible(cancelAllowed);

		final Box cancelBox = Box.createHorizontalBox();
		cancelBox.add(Box.createGlue());
		cancelBox.add(cancelButton);
		cancelBox.add(Box.createGlue());
		cancelBox.add(ok);
		cancelBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
		/*
		 * Layout the elements inside the window. An empty border is created in
		 * order to put some space between the window content and the window
		 * border.
		 */
		final JPanel panel = new JPanel(new GridLayout(2, 1));
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(VMARGIN, HMARGIN, VMARGIN, HMARGIN),
				BorderFactory.createEtchedBorder()));
		panel.add(description);
		panel.add(progressBar);
		content.setLayout(new BorderLayout());
		content.add(panel, BorderLayout.NORTH);
		content.add(cancelBox, BorderLayout.SOUTH);

		setTitle(AtlasViewerGUI.R("dialog.title.wait"));

		setDescription(AtlasViewerGUI.R("dialog.title.wait"));
	}

	public AtlasStatusDialog(Component owner, String title, String startText) {
		this(owner);
		if (title != null)
			setTitle(title);
		if (startText != null)
			setDescription(startText);
	}

	/**
	 * Returns a localized string for the specified key.
	 * 
	 * @param key
	 *            an integer key
	 * @return the associated string
	 */
	private String getString(final int key) {
		return Vocabulary.getResources(window.getLocale()).getString(key);
	}

	/**
	 * Returns the window title. The default title is "Progress" localized in
	 * current locale.
	 * 
	 * @return the window title
	 */
	@Override
	public String getTitle() {
		return (String) get(Caller.TITLE);
	}

	/**
	 * Set the window title. A {@code null} value reset the default title.
	 * 
	 * @param title
	 *            the window title
	 */
	@Override
	public void setTitle(String title) {
		if (title == null) {
			title = getString(VocabularyKeys.PROGRESSION);
		}
		set(Caller.TITLE, title);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return (String) get(Caller.LABEL);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDescription(final String description) {
		set(Caller.LABEL, description);
	}

	/**
	 * Notifies that the operation begins. This method display the windows if it
	 * was not already visible.
	 */
	@Override
	public void started() {
		call(Caller.STARTED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void progress(final float percent) {
		int p = (int) percent; // round toward 0
		if (p < 0)
			p = 0;
		if (p > 100)
			p = 100;
		set(Caller.PROGRESS, new Integer(p));
	}

	@Override
	public float getProgress() {
		BoundedRangeModel model = progressBar.getModel();
		float progress = (model.getValue() - model.getMinimum());
		float limit = model.getMaximum();

		return progress / limit;
	}

	/**
	 * Notifies that the operation has finished. The window will disaspears,
	 * except if it contains warning or exception stack traces.
	 */
	@Override
	public void complete() {
		call(Caller.COMPLETE);
	}

	/**
	 * Releases any resource holds by this window. Invoking this method destroy
	 * the window.
	 */
	@Override
	public void dispose() {
		listeners.clear();
		call(Caller.DISPOSE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param stop
	 *            true to stop; false otherwise
	 */
	@Override
	public void setCanceled(final boolean stop) {
		if (stop != canceled) {
			canceled = stop;

			for (ActionListener l : listeners) {
				l.actionPerformed(new ActionEvent(AtlasStatusDialog.this, 0,
						CANCEL_PROPERTY));
			}
		}
	}

	@Override
	public void addCancelListener(ActionListener l) {
		listeners.add(l);
	}

	/**
	 * Display a warning message under the progress bar. The text area for
	 * warning messages appears only the first time this method is invoked.
	 * 
	 * @param source
	 *            DOCUMENT ME
	 * @param margin
	 *            DOCUMENT ME
	 * @param warning
	 *            DOCUMENT ME
	 */
	@Override
	public synchronized void warningOccurred(final String source,
			String margin, String warning) {

		warningOccured = true;

		if (warning == null)
			warning = "";

		final StringBuffer buffer = new StringBuffer(warning.length() + 16);
		if (!source.equals(lastSource)) {
			lastSource = source;
			if (warningArea != null) {
				buffer.append('\n');
			}
			buffer.append(source != null ? source
					: getString(VocabularyKeys.UNTITLED));
			buffer.append('\n');
		}
		int wm = WARNING_MARGIN;
		if (margin != null) {
			margin = trim(margin);
			if (margin.length() != 0) {
				wm -= (margin.length() + 3);
				for (int i = 0; i < wm; i++) {
					buffer.append(' ');
				}
				buffer.append('(');
				buffer.append(margin);
				buffer.append(')');
				wm = 1;
			}
		}
		for (int i = 0; i < wm; i++) {
			buffer.append(' ');
		}
		buffer.append(warning);
		if (buffer.charAt(buffer.length() - 1) != '\n') {
			buffer.append('\n');
		}
		set(Caller.WARNING, buffer.toString());
	}

	/**
	 * Display an exception stack trace.
	 * 
	 * @param exception
	 *            the exception to display
	 */
	@Override
	public void exceptionOccurred(final Throwable exception) {
		dispose();
		ExceptionDialog.show(parentWindow, exception);
	}

	/**
	 * Returns the s// public void warningOccurred(Exception e) { //
	 * warningOccurred("asdsad", null, e.getLocalizedMessage() != null ? e //
	 * .getLocalizedMessage() : e.getMessage()); // cancel // }tring
	 * {@code margin} without the parenthesis (if any).
	 * 
	 * @param margin
	 *            DOCUMENT ME
	 * @return DOCUMENT ME
	 */
	private static String trim(String margin) {
		margin = margin.trim();
		int lower = 0;
		int upper = margin.length();
		while (lower < upper && margin.charAt(lower + 0) == '(')
			lower++;
		while (lower < upper && margin.charAt(upper - 1) == ')')
			upper--;
		return margin.substring(lower, upper);
	}

	/**
	 * Queries one of the components in the progress window. This method doesn't
	 * need to be invoked from the <cite>Swing</cite> thread.
	 * 
	 * @param task
	 *            // public void warningOccurred(Exception e) { //
	 *            warningOccurred("asdsad", null, e.getLocalizedMessage() !=
	 *            null ? e // .getLocalizedMessage() : e.getMessage()); //
	 *            cancel // } The desired value as one of the
	 *            {@link Caller#TITLE} or {@link Caller#LABEL} constants.
	 * @return The value.
	 */
	private Object get(final int task) {
		final Caller caller = new Caller(-task);
		SwingUtilities.invokeAndWait(caller);
		return caller.value;
	}

	/**
	 * Sets the state of one of the components in the progress window. This
	 * method doesn't need to be invoked from the <cite>Swing</cite> thread.
	 * 
	 * @param task
	 *            The value to change as one of the {@link Caller#TITLE} or
	 *            {@link Caller#LABEL} constants.
	 * @param value
	 *            The new value.
	 */
	private void set(final int task, final Object value) {
		final Caller caller = new Caller(task);
		caller.value = value;
		EventQueue.invokeLater(caller);
	}

	/**
	 * Invokes a <cite>Swing</cite> method without arguments.
	 * 
	 * @param task
	 *            The method to invoke: {@link Caller#STARTED} or
	 *            {@link Caller#DISPOSE}.
	 */
	private void call(final int task) {
		EventQueue.invokeLater(new Caller(task));
	}

	/**
	 * Task to run in the <cite>Swing</cite> thread. Tasks are identified by a
	 * numeric constant. The {@code get} operations have negative identifiers
	 * and are executed by the {@link EventQueue#invokeAndWait} method. The
	 * {@code set} operations have positive identifiers and are executed by the
	 * {@link EventQueue#invokeLater} method.
	 * 
	 * @author Martin Desruisseaux (PMO, IRD)
	 */
	private class Caller implements Runnable {
		/** For getting or setting the window title. */
		public static final int TITLE = 1;

		/** For getting or setting the progress label. */
		public static final int LABEL = 2;

		/** For getting or setting the progress bar value. */
		public static final int PROGRESS = 3;

		/** For adding a warning message. */
		public static final int WARNING = 4;

		/** Notify that an action started. */
		public static final int STARTED = 5;

		/** Notify that an action is completed. */
		public static final int COMPLETE = 6;

		/** Notify that the window can be disposed. */
		public static final int DISPOSE = 7;

		/**
		 * The task to execute, as one of the {@link #TITLE}, {@link #LABEL},
		 * <cite>etc.</cite> constants or their negative counterpart.
		 */
		private final int task;

		/**
		 * The value to get (negative value {@link #task}) or set (positive
		 * value {@link #task}).
		 */
		public Object value;

		/**
		 * Creates an action. {@code task} must be one of {@link #TITLE},
		 * {@link #LABEL} <cite>etc.</cite> constants or their negative
		 * counterpart.
		 * 
		 * @param task
		 *            the task key
		 */
		public Caller(final int task) {
			this.task = task;
		}

		/**
		 * Run the task.
		 */
		@Override
		public void run() {
			final BoundedRangeModel model = progressBar.getModel();
			switch (task) {
			case -LABEL: {
				value = description.getText();
				return;
			}
			case +LABEL: {
				description.setText((String) value);
				return;
			}
			case PROGRESS: {
				model.setValue(((Integer) value).intValue());
				progressBar.setIndeterminate(false);
				return;
			}
			case STARTED: {
				model.setRangeProperties(0, 1, 0, 100, false);
				SwingUtil
						.setRelativeFramePosition(window, parentWindow, .5, .5);

				window.setVisible(true);

				break; // Need further action below.
			}
			case COMPLETE: {
				progressBar.setIndeterminate(false);
				model.setRangeProperties(100, 1, 0, 100, false);
				cancelButton.setEnabled(false);
				if (warningArea != null) {
					ok.setEnabled(true);
				} else {
					window.setVisible(false);
					window.dispose();
				}
				break; // Need further action below.
			}
			}
			/*
			 * Some of the tasks above requires an action on the window, which
			 * may be a JDialog or a JInternalFrame. We need to determine the
			 * window type before to apply the action.
			 */
			synchronized (AtlasStatusDialog.this) {
				final JDialog window = AtlasStatusDialog.this.window;
				switch (task) {
				case -TITLE: {
					value = window.getTitle();
					return;
				}
				case +TITLE: {
					window.setTitle((String) value);
					return;
				}
				case STARTED: {
					window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
					return;
				}
				case COMPLETE: {
					window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					return;
				}
				case DISPOSE: {
					window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					window.dispose();
					return;
				}
				}

				if (warningArea == null) {
					final JTextArea warningArea = new JTextArea();
					final JScrollPane scroll = new JScrollPane(warningArea);
					final JPanel namedArea = new JPanel(new BorderLayout());
					AtlasStatusDialog.this.warningArea = warningArea;
					warningArea.setFont(Font.getFont("Monospaced"));
					warningArea.setEditable(false);
					namedArea.setBorder(BorderFactory.createEmptyBorder(0,
							HMARGIN, VMARGIN, HMARGIN));
					namedArea.add(
							new JLabel(getString(VocabularyKeys.WARNING)),
							BorderLayout.NORTH);
					namedArea.add(scroll, BorderLayout.CENTER);
					content.add(namedArea, BorderLayout.CENTER);
					window.setResizable(true);
					window.setSize(WIDTH, HEIGHT + WARNING_HEIGHT);
					window.setVisible(true); // Seems required in order to force
					// relayout.
				}
				final JTextArea warningArea = (JTextArea) AtlasStatusDialog.this.warningArea;
				warningArea.append((String) value);
			}
		}
	}

	@Override
	public void setTask(InternationalString task) {
		setDescription(task.toString());
	}

	@Override
	public InternationalString getTask() {
		return new SimpleInternationalString(getDescription());
	}

	@Override
	public void startModal() {
		if (javax.swing.SwingUtilities.isEventDispatchThread()) {
			final BoundedRangeModel model = progressBar.getModel();
			model.setRangeProperties(0, 1, 0, 100, false);
			SwingUtil.setRelativeFramePosition(window, parentWindow, .5, .5);
			window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			window.setModal(true); // CALL zeug?!
			window.setVisible(true);
		} else {
			started();
			window.setModal(true); // CALL zeug?!
		}
	}

	@Override
	public boolean isWarningOccured() {
		return warningOccured;
	}

	@Override
	public void downloadFailed(URL arg0, String arg1) {
		// i8n
		LOGGER.error("downloadFailed " + arg0 + " " + arg1);
		setDescription(arg1);
		exceptionOccurred(new AtlasImportException(arg1));
	}

	long lastPercentageUpdate = System.currentTimeMillis() - 10000;
	private final static MbDecimalFormatter mbdf = new MbDecimalFormatter();

	@Override
	public void progress(URL url, String urlString, long doneSoFar, long full,
			int percentage) {

		String filename;
		
		if (url != null) {
			filename = IOUtil.getFilename(url);
		} else {
			filename = "";
		}

		if (percentage < 0) {
			setDescription("Downloading " + filename); // i8n
			return;
		}

		// Not too many updates
		if (System.currentTimeMillis() - lastPercentageUpdate < 500)
			return;

		lastPercentageUpdate = System.currentTimeMillis();
		// i8n
		LOGGER.debug("progress " + url + " " + urlString + " " + doneSoFar
				+ " " + full + " " + percentage);

		String fullSize = mbdf.format(full);

		setDescription("Downloading " + filename + " " + percentage + "% of "
				+ fullSize); // i8n
	}

	@Override
	public void upgradingArchive(URL url, String version, int patchPercent,
			int overallPercent) {

		// Not too many updates
		if (System.currentTimeMillis() - lastPercentageUpdate < 500)
			return;

		// i8n
		LOGGER.debug("upgrading " + url + " " + version + " " + patchPercent
				+ " " + overallPercent);
		String filename = IOUtil.getFilename(url);
		setDescription("Upgrading " + filename + " " + overallPercent + "%");
	}

	@Override
	public void validating(URL url, String version, long entry, long total,
			int overallPercent) {

		// Not too many updates
		if (System.currentTimeMillis() - lastPercentageUpdate < 500)
			return;

		String filename = IOUtil.getFilename(url);

		// i8n
		LOGGER.debug("validating " + url + " " + version + " " + entry + " "
				+ total + " " + overallPercent);
		setDescription("Validating " + filename + " " + overallPercent + "%");
	}

}
