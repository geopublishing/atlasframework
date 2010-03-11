/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.atlas.gui.internal;

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

import org.geotools.resources.SwingUtilities;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.util.SimpleInternationalString;
import org.opengis.util.InternationalString;

import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.SwingUtil;
import skrueger.atlas.AtlasViewer;
import skrueger.swing.OkButton;

public class AtlasStatusDialog {
	
	static public String CANCEL_PROPERTY = "atlas status dialog cancelled";
	
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
	private final JButton cancel;

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
		window.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		window.setResizable(false);

		window.setBounds((parentSize.width - WIDTH) / 2,
				(parentSize.height - HEIGHT) / 2, WIDTH, HEIGHT);
		/*
		 * Creates the label that is going to display the undergoing operation.
		 * This label is initially empty.
		 */
		description = new JLabel();
		description.setHorizontalAlignment(JLabel.CENTER);
		/*
		 * Creates the progress bar.
		 */
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(3, 6, 3, 6), progressBar.getBorder()));
		/*
		 * Creates the ok button.
		 */
		ok = new OkButton();
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		ok.setEnabled(false);
		/*
		 * Creates the cancel button.
		 */
		cancel = new JButton(resources.getString(VocabularyKeys.CANCEL));
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCanceled(true);
			}
		});
		final Box cancelBox = Box.createHorizontalBox();
		cancelBox.add(Box.createGlue());
		cancelBox.add(cancel);
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

		setTitle(AtlasViewer.R("dialog.title.wait"));

		setDescription(AtlasViewer.R("dialog.title.wait"));
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
	public String getTitle() {
		return (String) get(Caller.TITLE);
	}

	/**
	 * Set the window title. A {@code null} value reset the default title.
	 * 
	 * @param title
	 *            the window title
	 */
	public void setTitle(String title) {
		if (title == null) {
			title = getString(VocabularyKeys.PROGRESSION);
		}
		set(Caller.TITLE, title);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription() {
		return (String) get(Caller.LABEL);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDescription(final String description) {
		set(Caller.LABEL, description);
	}

	/**
	 * Notifies that the operation begins. This method display the windows if it
	 * was not already visible.
	 */
	public void started() {
		call(Caller.STARTED);
	}

	/**
	 * {@inheritDoc}
	 */
	public void progress(final float percent) {
		int p = (int) percent; // round toward 0
		if (p < 0)
			p = 0;
		if (p > 100)
			p = 100;
		set(Caller.PROGRESS, new Integer(p));
	}

	public float getProgress() {
		BoundedRangeModel model = progressBar.getModel();
		float progress = (float) (model.getValue() - model.getMinimum());
		float limit = (float) model.getMaximum();

		return progress / limit;
	}

	/**
	 * Notifies that the operation has finished. The window will disaspears,
	 * except if it contains warning or exception stack traces.
	 */
	public void complete() {
		call(Caller.COMPLETE);
	}

	/**
	 * Releases any resource holds by this window. Invoking this method destroy
	 * the window.
	 */
	public void dispose() {
		listeners.clear();
		call(Caller.DISPOSE);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param stop
	 *            true to stop; false otherwise
	 */
	public void setCanceled(final boolean stop) {
		if (stop != canceled) {
			canceled = stop;
			
			for (ActionListener l : listeners ) {
				l.actionPerformed(new ActionEvent(AtlasStatusDialog.this, 0, CANCEL_PROPERTY));
			}
		}
	}
	
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
	public synchronized void warningOccurred(final String source,
			String margin, String warning) {

		warningOccured = true;
		
		if (warning == null) warning = "";

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
	public void exceptionOccurred(final Throwable exception) {
		dispose();
		ExceptionDialog.show(parentWindow, exception);
	}

	/**
	 * Returns the s//	public void warningOccurred(Exception e) {
//		warningOccurred("asdsad", null, e.getLocalizedMessage() != null ? e
//				.getLocalizedMessage() : e.getMessage());
//		cancel
//	}tring {@code margin} without the parenthesis (if any).
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
	 * @param task//	public void warningOccurred(Exception e) {
//		warningOccurred("asdsad", null, e.getLocalizedMessage() != null ? e
//				.getLocalizedMessage() : e.getMessage());
//		cancel
//	}
	 *            The desired value as one of the {@link Caller#TITLE} or
	 *            {@link Caller#LABEL} constants.
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
		public void run() {
			final BoundedRangeModel model = progressBar.getModel();
			switch (task) {
			case -LABEL: {
				value = description.getText();
				return;
			}
			case +LABEL: {
				description.setText((String) value);
				// if ( ((String) value).length() > maxLength)
				// window.pack();
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
				cancel.setEnabled(false);
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
				// if (window instanceof JDialog) {
				final JDialog window = (JDialog) AtlasStatusDialog.this.window;
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
					window
							.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
					return;
				}
				case COMPLETE: {
					window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					return;
				}
				case DISPOSE: {
					window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					// if (warningArea == null || !window.isVisible()) {
					window.dispose();
					// }

					return;
				}
				}
				/*
				 * Si la tâche spécifiée n'est aucune des tâches énumérées
				 * ci-haut, on supposera que l'on voulait afficher un message
				 * d'avertissement.
				 */
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
					// if (window instanceof JDialog) {
					// final JDialog window = (JDialog)
					// AtlasStatusDialog.this.window;
					window.setResizable(true);
					// } else {
					// final JInternalFrame window = (JInternalFrame)
					// AtlasStatusDialog.this.window;
					// window.setResizable(true);
					// }
					window.setSize(WIDTH, HEIGHT + WARNING_HEIGHT);
					window.setVisible(true); // Seems required in order to force
					// relayout.
				}
				final JTextArea warningArea = (JTextArea) AtlasStatusDialog.this.warningArea;
				warningArea.append((String) value);
			}
		}
	}

	public void setTask(InternationalString task) {
		setDescription(task.toString());
	}

	public InternationalString getTask() {
		return new SimpleInternationalString(getDescription());
	}

	public void startModal() {
		if (javax.swing.SwingUtilities.isEventDispatchThread()) {
			final BoundedRangeModel model = progressBar.getModel();
			model.setRangeProperties(0, 1, 0, 100, false);
			SwingUtil.setRelativeFramePosition(window, parentWindow, .5, .5);
			window.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			window.setModal(true); // CALL zeug?!
			window.setVisible(true);
		} else {
			started();
			window.setModal(true); // CALL zeug?!
		}
	}

	public boolean isWarningOccured() {
		return warningOccured;
	}


}
