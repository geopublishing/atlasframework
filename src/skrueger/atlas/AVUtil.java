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
package skrueger.atlas;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FontMetrics;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.measure.unit.Unit;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geotools.data.DataUtilities;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.resources.CRSUtilities;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import schmitzm.io.IOUtil;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.jfree.feature.style.FeatureChartStyle;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.SwingUtil;
import skrueger.AttributeMetadata;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.gui.HTMLBrowserWindow;
import skrueger.atlas.gui.internal.AtlasExportTask;
import skrueger.atlas.gui.internal.AtlasStatusDialog;
import skrueger.atlas.resource.icons.Icons;
import skrueger.atlas.swing.AtlasSwingWorker;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.i8n.I8NUtil;
import skrueger.i8n.Translation;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Collection of Atlas related static methods.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 */
public class AVUtil {
	/***************************************************************************
	 * This string is used to identify the temp files of the AV. Any files and
	 * folders starting with this string in the temp folder will be deleted when
	 * the Atlas ends.
	 * 
	 * @see DpEntry#cleanupTemp
	 **************************************************************************/
	public static final String ATLAS_TEMP_FILE_ID = "AtlasTempFile_";

	static final Logger LOGGER = Logger.getLogger(AVUtil.class);

	/**
	 * {@link Enum} of all recognizable Operating Systems
	 */
	public static enum OSfamiliy {
		linux, mac, windows, unknown
	}

	/**
	 * @return the {@link OSfamiliy} the client is running on
	 */
	public static OSfamiliy getOSType() {
		final String osname = System.getProperty("os.name").toLowerCase();
		// LOGGER.debug("OS Name: " + osname);

		// log.debug("OS Architecture: " + System.getProperty("os.arch"));
		// log.debug("OS Version: " + System.getProperty("os.version"));

		if (osname.contains("win"))
			return OSfamiliy.windows;

		if (osname.contains("mac"))
			return OSfamiliy.mac;

		if (osname.contains("linux"))
			return OSfamiliy.linux;

		return OSfamiliy.unknown;
	}

	/**
	 * Creates a random number generator mainly used for IDs
	 */
	public final static Random RANDOM = new Random();

	/**
	 * The method
	 * {@link #createLocalCopyFromURL(Component, URL, String, String)} uses this
	 * hashMap to remember which {@link URL}s have already been stored locally.
	 */
	private static HashMap<URL, File> cachedLocalCopiedFiles = new HashMap<URL, File>();

	public static void setTabs(final JTextPane textPane,
			final int charactersPerTab) {
		final FontMetrics fm = textPane.getFontMetrics(textPane.getFont());
		final int charWidth = fm.charWidth('w');
		final int tabWidth = charWidth * charactersPerTab;

		final TabStop[] tabs = new TabStop[10];

		for (int j = 0; j < tabs.length; j++) {
			final int tab = j + 1;
			tabs[j] = new TabStop(tab * tabWidth);
		}

		final TabSet tabSet = new TabSet(tabs);
		final SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setTabSet(attributes, tabSet);
		final int length = textPane.getDocument().getLength();
		textPane.getStyledDocument().setParagraphAttributes(0, length,
				attributes, false);
	}

	/**
	 * e1027. Converting a {@link TreeNode} in a {@link JTree} Component to a
	 * {@link TreePath} Returns a {@link TreePath} containing the specified
	 * node.
	 */
	public static final TreePath getPath(TreeNode node) {
		final List<TreeNode> list = new ArrayList<TreeNode>();

		// Add all nodes to list
		while (node != null) {
			list.add(node);
			node = node.getParent();
		}
		Collections.reverse(list);

		// Convert array of nodes to TreePath
		return new TreePath(list.toArray());
	}

	/**
	 * If expand is true, expands all nodes in the tree. Otherwise, collapses
	 * all nodes in the tree.
	 */
	public final static void expandAll(final JTree tree, final boolean expand) {
		final TreeNode root = (TreeNode) tree.getModel().getRoot();

		// Traverse tree from root
		expandAll(tree, new TreePath(root), expand);
	}

	private final static void expandAll(final JTree tree,
			final TreePath parent, final boolean expand) {
		// Traverse children
		final TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (final Enumeration<TreeNode> e = node.children(); e
					.hasMoreElements();) {
				final TreeNode n = e.nextElement();
				final TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path, expand);
			}
		}

		// Expansion or collapse must be done bottom-up
		if (expand) {
			tree.expandPath(parent);
		} else {
			tree.collapsePath(parent);
		}
	}

	/**
	 * Expands a tree to the
	 * 
	 * @param tree
	 * @param droppedNode
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public final static void expandToNode(final JTree tree,
			final MutableTreeNode droppedNode) {

		TreeNode lookAt = droppedNode;
		final List<TreeNode> parents = new ArrayList<TreeNode>();
		parents.add(lookAt);

		while (lookAt.getParent() != null) {
			lookAt = lookAt.getParent();
			parents.add(lookAt);
		}

		Collections.reverse(parents);
		for (final TreeNode node : parents) {
			final TreePath path = getPath(node);
			tree.expandPath(path);
		}
	}

	/**
	 * Copy file/folder to file/folder but doesn't throw an Exception
	 * 
	 * @param source
	 *            File or directory / Wildcard to copy
	 * @param destination
	 *            Directory or filename...
	 * @param cleanFilenames
	 *            Convert target filenames to lowercase
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * @param cleanFilenames
	 */
	public final static void copyURLNoException(final URL source,
			final File destination, final boolean cleanFilenames) {
		try {
			copyUrl(source, destination, cleanFilenames);
		} catch (final Exception e) {

		}
	}

	/**
	 * Copy file/folder to file/folder but doesn't throw an Exception
	 * 
	 * @param source
	 *            File or directory / Wildcard to copy
	 * @param destination
	 *            Directory or filename...
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public final static void copyFileNoException(final Logger log,
			final File source, final File destination,
			final boolean cleanFilenames) {
		try {
			copyFile(log, source, destination, cleanFilenames);
		} catch (final Exception e) {

		}
	}

	/**
	 * Copy file or folder recursively to file or folder. All filenames are
	 * turned to lower case!
	 * 
	 * @param source
	 *            File or directory or wildcard to copy
	 * @param destination
	 *            Directory or filename...
	 * @param cleanFilenames
	 *            Convert target filenames to clean, lowercase filenames
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * 
	 * @throws URISyntaxException
	 * @throws IOException
	 * 
	 * @throws IOException
	 */
	public final static void copyUrl(final URL source, File destination,
			final Boolean cleanFilenames) throws IOException,
			URISyntaxException {

		final String sourceName = IOUtil.urlToFile(source).getName();
		final String cleanName = cleanFilenames ? cleanFilename(sourceName)
				: sourceName;

		if (destination.isDirectory()) {
			destination = new File(destination, cleanName);
		} else {
			destination = new File(destination.getParentFile(), cleanName);
		}

		FileUtils.copyURLToFile(source, destination);
	}

	/**
	 * Copy file or folder recursivly to file or folder. All filenames are
	 * turned to lower case!
	 * 
	 * @param source
	 *            File or directory or wildcard to copy
	 * @param destination
	 *            Directory or filename...
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * @throws URISyntaxException
	 * @throws IOException
	 * 
	 * @deprecated Use celanFilenames !
	 */
	public final static void copyURL(final Logger log, final URL source,
			final URL destination) throws IOException, URISyntaxException {
		copyFile(log, new File(source.getFile()), new File(destination
				.getFile()));
	}

	/**
	 * Copy file or folder recursivly to file or folder. All filenames are
	 * turned to lower case!
	 * 
	 * @param source
	 *            File or directory or wildcard to copy
	 * @param destination
	 *            Directory or filename...
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * 
	 * @deprecated Use cleanFilenames !
	 * 
	 * @throws IOException
	 */
	public final static void copyFile(final Logger log, final File source,
			final File destination) throws IOException {
		copyFile(log, source, destination, false);
	}

	/**
	 * Copy file or folder recursively to file or folder. All filenames are
	 * turned to lower case!
	 * 
	 * @param source
	 *            File or directory or wildcard to copy
	 * @param destination
	 *            Directory or filename...
	 * @param cleanFileanames
	 *            Converts filenames to lower-case and removes special
	 *            characters by calling {@link #cleanFilename(String)}
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * 
	 * @throws IOException
	 */
	public final static void copyFile(Logger log, final File source,
			final File destination, final Boolean cleanFileanames)
			throws IOException {

		if (log == null)
			log = LOGGER;

		if (!source.exists()) {
			final String msg = "Source file '" + source
					+ "' for copy doesn't exist.";
			log.warn(msg);
			throw new FileNotFoundException(msg);
		}

		if (source.isDirectory()) {

			if (source.isDirectory()) {
				if (!destination.exists()) {
					destination.mkdirs();
				}

				final String[] children = source.list();
				for (int i = 0; i < children.length; i++) {
					copyFile(log, new File(source, children[i]), new File(
							destination, children[i]), cleanFileanames);
				}
			} else {
				copyFile(log, source, destination, cleanFileanames);
			}
		} else if (source.isFile()) {
			FileChannel srcChannel = null, dstChannel = null;

			try {
				// Create channel on the source
				srcChannel = new FileInputStream(source).getChannel();

				// Create channel on the destination

				/* Optionally clean the target filename */
				File target;
				if (destination.isDirectory()) {
					String filename = source.getName();
					if (cleanFileanames == true)
						filename = cleanFilename(filename);

					target = new File(destination, filename);
				} else {
					String filename = destination.getName();
					if (cleanFileanames == true) {
						filename = cleanFilename(filename);
					}
					target = new File(destination.getParentFile(), filename);
				}

				dstChannel = new FileOutputStream(target).getChannel();

				dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
				// Happy end

				return;
			} finally {
				// Cleanup, close the channels
				if (srcChannel != null)
					try {
						srcChannel.close();
					} catch (final IOException e) {
						log.error("Exception while cleaning up", e);
					}

				if (dstChannel != null)
					try {
						dstChannel.close();
					} catch (final IOException e) {
						log.error("Exception while cleaning up", e);
					}
			} // finally
		} else {
			log.warn("copyFile(Logger log, source " + source + ", destination "
					+ destination + "): Can't copy this!?");
		}

	}

	/**
	 * A convenience wrapper for {@link JOptionPane}.showMessageDialog. This
	 * wrapper checks if we are on the EDT.
	 * 
	 * @throws a
	 *             RuntimeException ("Not on EDT!") if we are not on EDT.
	 */
	public final static void showMessageDialog(final Component owner,
			final String message) {
		if (!SwingUtilities.isEventDispatchThread()) {

			// final IllegalStateException illegalStateException = new
			// IllegalStateException(
			// "GUI action while not on EDT.");
			// LOGGER.error(illegalStateException);
			// ExceptionDialog.show(owner, illegalStateException);
			// return;

			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						JOptionPane.showMessageDialog(owner, message);
					}
				});
			} catch (InterruptedException e) {
				LOGGER.error(e);
			} catch (InvocationTargetException e) {
				LOGGER.error(e);
			}

		} else {
			JOptionPane.showMessageDialog(owner, message);
		}

	}

	/**
	 * Convenience method to ask a simple Yes/No question.
	 */
	public static boolean askYesNo(final Component owner, final String question) {
		if (!SwingUtilities.isEventDispatchThread()) {
			final IllegalStateException illegalStateException = new IllegalStateException(
					"GUI action while not on EDT.");
			LOGGER.error(illegalStateException);
			ExceptionDialog.show(owner, illegalStateException);
			return false;
		}

		final int result = JOptionPane.showConfirmDialog(owner, question,
				AtlasViewer.R("GeneralQuestionDialogTitle"),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null);
		return result == JOptionPane.YES_OPTION;
	}

	static Integer resultAskOkCancel;
	
	/**
	 * Convenience method to ask a simple OK/Cancel question. If this is not
	 * executed on the EDT, it will be exectued on EDT via invokeAndWait
	 * 
	 */
	public static boolean askOKCancel(final Component owner,
			final String question) {
		
		if (SwingUtilities.isEventDispatchThread()) {
			final int result = JOptionPane.showConfirmDialog(owner, question,
					AtlasViewer.R("GeneralQuestionDialogTitle"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
					null);
			return result == JOptionPane.OK_OPTION;
		} else {
			
			
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					
					@Override
					public void run() {
						resultAskOkCancel = JOptionPane.showConfirmDialog(owner, question,
								AtlasViewer.R("GeneralQuestionDialogTitle"),
								JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
								null);
						
					}
				});
			} catch (Exception e) {
				LOGGER.error(e);
			}
			
			return resultAskOkCancel == JOptionPane.OK_OPTION;
			
//			final IllegalStateException illegalStateException = new IllegalStateException(
//			"GUI action while not on EDT.");
//			LOGGER.error(illegalStateException);
//			ExceptionDialog.show(owner, illegalStateException);
//			return false;
			
		}

	}

	/**
	 * Tries to open a file or folder with a system dependent program
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public static void openOSFolder(final File exportDir) {

		String cmd;

		String dir = exportDir.getAbsolutePath();
		// LOGGER.info("opening folder " + exportDir.getAbsolutePath() + "
		// ...");
		try {

			if (getOSType() == OSfamiliy.windows) {
				// ****************************************************************************
				// Trying to open a folder on Windows
				// ****************************************************************************
				dir = dir.replace('/', '\\');
				final String[] cmdList = { "cmd.exe", "/c explorer " + dir }; // TODO
				// wie
				// gehts
				// mit
				// luecken
				// im
				// string?
				Runtime.getRuntime().exec(cmdList);
			}

			else if (getOSType() == OSfamiliy.linux) {
				// ****************************************************************************
				// Trying to open a folder on Linux
				// ****************************************************************************

				try {
					final List<String> command = new ArrayList<String>();
					command.add("nautilus");
					command.add(dir);
					// LOGGER.info("running " + cmd + dir);
					// Runtime.getRuntime().exec(cmd + dir);
					final ProcessBuilder builder = new ProcessBuilder(command);
					builder.start();

				} catch (final Exception e) {
					try {
						final List<String> command = new ArrayList<String>();
						command.add("dolphin");
						command.add(dir);
						// LOGGER.info("running " + cmd + dir);
						// Runtime.getRuntime().exec(cmd + dir);
						final ProcessBuilder builder = new ProcessBuilder(
								command);
						builder.start();
					} catch (final Exception ee) {
						final List<String> command = new ArrayList<String>();
						command.add("konqueror");
						command.add(dir);
						// LOGGER.info("running " + cmd + dir);
						// Runtime.getRuntime().exec(cmd + dir);
						final ProcessBuilder builder = new ProcessBuilder(
								command);
						builder.start();
					}
				}
			}

			else if (getOSType() == OSfamiliy.mac) {
				// ****************************************************************************
				// Trying to open a folder on Mac
				// ****************************************************************************
				cmd = "open ";
				LOGGER.info("running  " + cmd + dir);
				Runtime.getRuntime().exec(cmd + dir);
			}

		} catch (final IOException e) {
			LOGGER.info("failed", e);
		}
	}

	/**
	 * Tries to open a PDF on the client's system.
	 * 
	 * TODO 1. Add a progress bar while copying the PDF to the temp dir.
	 * 
	 * @param url
	 *            Where to find the PDF?
	 * @param title
	 *            The localized title of the docuemnt. Its converted to a valid
	 *            filename when creating the local copy.
	 * @return <code>null</code> or any exception catched
	 */
	public static Exception launchPDFViewer(final Component owner, URL url,
			String title) {

		LOGGER.debug("Calling launchPDFViewer with url= " + url);

		if (title == null) {
			title = url.getFile();
		}

		/**
		 * Let a wait cursor appear for 3 seconds (while the PDF is opening)
		 */
		if (owner != null)
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					final Cursor backup = owner.getCursor();
					owner.setCursor(Cursor
							.getPredefinedCursor(Cursor.WAIT_CURSOR));
					try {
						Thread.sleep(3000);
					} catch (final InterruptedException e) {
					} finally {
						owner.setCursor(backup);
					}
				}
			});

		File pdfFile = null; // = DataUtilities.urlToFile(url);
		try {

			/**
			 * If the path links to a file that in not in a JAR, but rather lies
			 * directly on the Medium, we do not have to copy it to the TEMP
			 * dir.
			 */
			if (url.getFile().contains("ad/html/about/../../../")) {
				LOGGER
						.debug("Special case here.. we expect the PDF to lay uncompressed next to atlas.jar or atlas.gpa");
				String urlString = url.toExternalForm();
				String replaced = urlString.replace("ad/html/about/../../../",
						"");
				url = new URL(replaced);
			}

			if (url.getProtocol().equals("jar")
					&& url.toExternalForm().contains("atlas_resources.jar!/")
					&& !url.toExternalForm()
							.contains("atlas_resources.jar!/ad")) {
				// we have something like
				// jar:http://www.geopublishing.org/iida2.5/atlas_resources.jar!/impetus_atlas_benin_foreword_en.pdf
				// and we transform it to
				// http://www.geopublishing.org/iida2.5/impetus_atlas_benin_foreword_en.pdf
				// becuase files located next to atlas.gpa / atlas.jar are not
				// put into JARs

				LOGGER
						.debug("Special case here.. we expect the PDF to lay uncompressed next to atlas.jar or atlas.gpa");
				String urlString = url.toExternalForm();
				String replaced = urlString.substring(4);
				replaced = replaced.replace("atlas_resources.jar!/", "");
				url = new URL(replaced);
			}

			pdfFile = createLocalCopyFromURL(owner, url, title, ".pdf");

			try {

				System.out.println("pdfFile to open  = " + pdfFile);

				if (!pdfFile.exists()) {
					System.out.println("pdfFile to open  does not exist");
				}
				LOGGER
						.debug("Using Desktop.getDesktop().open to open the following canonical file:\n"
								+ pdfFile);
				// HARDCRE CRASH ON arthurs computer!
				Desktop.getDesktop().open(pdfFile);
			} catch (Exception exWhileDesktop) {
				LOGGER.info("Can't use Desktop ?! :-( ", exWhileDesktop);

				final List<String> command = new ArrayList<String>();

				String pdfPath = pdfFile.getAbsolutePath();

				if (AVUtil.getOSType() == AVUtil.OSfamiliy.windows) {
					// ****************************************************************************
					// We are running on Windows, yeah!
					// ****************************************************************************
					// ****************************************************************************
					// Trying cmd.exe /c start
					// ****************************************************************************
					try {

						pdfPath = pdfFile.getCanonicalPath();
						pdfPath = pdfPath.replace('/', '\\');

						command.clear();
						command.add("cmd");
						command.add("/c");
						command.add("start");
						command.add(pdfFile.getName());

						final ProcessBuilder builder = new ProcessBuilder(
								command);
						builder.directory(pdfFile.getParentFile());
						LOGGER.debug("Trying " + command);
						builder.start();
						//
						// String cmdline = "cmd.exe /c start '" + pdfPath+"'";
						// LOGGER.debug("Trying '" + cmdline + "'");
						// Runtime.getRuntime().exec(cmdline);

					} catch (final IOException e) {
						ExceptionDialog.show(owner, e);
					}

				} else if (AVUtil.getOSType() == AVUtil.OSfamiliy.mac) {
					// ****************************************************************************
					// We are running on a Mac, yeah!
					// ****************************************************************************

					// ****************************************************************************
					// Trying NeXTSTEP open
					// ****************************************************************************
					try {
						command.clear();
						command.add("open");
						command.add(pdfPath);

						final ProcessBuilder builder = new ProcessBuilder(
								command);
						LOGGER.debug("Trying " + command);
						builder.start();

						// cmdline = "open " + pdfPath;
						// LOGGER.debug("Trying '" + cmdline + "'");
						// Runtime.getRuntime().exec(cmdline);
						// if (showMsgAfterLaunch)
						// JOptionPane.showMessageDialog(owner, successMsg);
					} catch (final IOException e) {
						ExceptionDialog.show(owner, e);
					}
				}

				else if (AVUtil.getOSType() == AVUtil.OSfamiliy.linux) {
					// ****************************************************************************
					// We are running on Linux, yeah!
					// ****************************************************************************
					try {
						// ****************************************************************************
						// Trying evince
						// ****************************************************************************
						command.clear();
						command.add("evince");
						command.add(pdfPath);
						final ProcessBuilder builder = new ProcessBuilder(
								command);
						LOGGER.debug("Trying " + command);
						builder.start();

						// cmdline = "evince " + pdfPath;
						// Runtime.getRuntime().exec(cmdline);
					}

					catch (final IOException e) {
						// ****************************************************************************
						// Trying kpdf
						// ****************************************************************************
						try {
							command.clear();
							command.add("kpdf");
							command.add(pdfPath);
							final ProcessBuilder builder = new ProcessBuilder(
									command);
							LOGGER.debug("Trying " + command);
							builder.start();

							// Runtime.getRuntime().exec(cmdline);
						}

						catch (final IOException e1) {
							// ****************************************************************************
							// Trying acroread
							// ****************************************************************************
							try {
								command.clear();
								command.add("acroread");
								command.add(pdfPath);
								final ProcessBuilder builder = new ProcessBuilder(
										command);
								LOGGER.debug("Trying " + command);
								builder.start();

								// cmdline = "acroread " + pdfPath;
								// LOGGER.debug("Trying '" + cmdline + "'");
								// Runtime.getRuntime().exec(cmdline);
							}

							catch (final IOException e2) {
								// ****************************************************************************
								// Trying epdfview
								// ****************************************************************************
								try {
									command.clear();
									command.add("epdfview");
									command.add(pdfPath);
									final ProcessBuilder builder = new ProcessBuilder(
											command);
									LOGGER.debug("Trying " + command);
									builder.start();

									// cmdline = "epdfview " + pdfPath;
									// LOGGER.debug("Trying '" + cmdline + "'");
									// Runtime.getRuntime().exec(cmdline);
								} catch (final IOException e3) {
									// ****************************************************************************
									// Trying xpdf
									// ****************************************************************************
									try {
										command.clear();
										command.add("xpdf");
										command.add(pdfPath);
										final ProcessBuilder builder = new ProcessBuilder(
												command);
										LOGGER.debug("Trying " + command);
										builder.start();

										// cmdline = "xpdf " + pdfPath;
										// LOGGER.debug("Trying '" + cmdline +
										// "'");
										// Runtime.getRuntime().exec(cmdline);
									} catch (final IOException e4) {
										ExceptionDialog.show(owner, e4);
									}
								}

							}
						}
					}
				}

				else {
					final String failMsg = "Unable to determine the type of operating system.\n"
							+ "Open the PDF yourself from " + pdfPath;
					LOGGER.info(failMsg);
					JOptionPane.showMessageDialog(owner, failMsg);
				}
			}// If Desktop didn't work
		} catch (final Exception e) {
			ExceptionDialog.show(owner, e);
			return e;
		}
		return null;
	}

	/**
	 * Copies the given {@link URL} to the local temp directory.
	 * 
	 * @param url
	 *            The {@link URL} to copy to local
	 * 
	 * @param postFix
	 *            The postfix for the temp file. Usefull, if we want windows
	 *            start command to determine the type. E.g. ".pdf" - if null
	 *            ".tmp" is used.
	 * 
	 * @return A {@link File} to a local Copy of the URL
	 * 
	 * @throws IOException
	 */
	public static File createLocalCopyFromURL(final Component owner,
			final URL url, String title, final String postFix)
			throws IOException {

		// Always copy to temp file, beacuse we want to simulate the change of
		// the filename when we preview withing the GP
		// //
		// ****************************************************************************
		// // Do not create local copy if we are not working on JARs or via http
		// //
		// ****************************************************************************
		if (!url.toString().contains("jar:")
				&& (url.toString().contains("file"))) {
			LOGGER
					.debug("Not copying the URL to temp file because we are local and not in a JAR.");
			return DataUtilities.urlToFile(url);
		}

		// ****************************************************************************
		// See, if we have already created a local copy
		// ****************************************************************************
		if (cachedLocalCopiedFiles.containsKey(url)) {
			return cachedLocalCopiedFiles.get(url);
		}

		if (title == null)
			title = "";

		// if ((titleHint == null) || (titleHint.equals("")))
		// titleHint = fileNameHint;

		// String fuerWindows = fileNameHint.lastIndexOf('.') > 0 ? fileNameHint
		// .substring(fileNameHint.lastIndexOf('.')) : fileNameHint;

		// LOGGER
		// .debug("Adding a Postfix to the temp file so Windows will recognize it: "
		// + fuerWindows);
		final File localTempFile = File.createTempFile(ATLAS_TEMP_FILE_ID
				+ cleanFilename(title), postFix);

		new AtlasExportTask(owner, AtlasViewer.R("dialog.title.wait")) {

			@Override
			protected Boolean doInBackground() throws Exception {
				FileUtils.copyURLToFile(url, localTempFile);
				localTempFile.deleteOnExit();
				System.out.println("downloaded to " + localTempFile);
				return true;
			}

		}.run();

		cachedLocalCopiedFiles.put(url, localTempFile.getCanonicalFile());

		return localTempFile.getCanonicalFile();
	}

	/**
	 * Tries to copy any existing language-specific .HTML files. e.g.
	 * soils_de.html. If they don't exist don't bother.
	 * 
	 * @param file
	 *            The base file, e.g. <code>cities.shp</code> or
	 *            <code>mountain.gml</code>. The postfix is not important, as it
	 *            will be replaced by <code>_en.html</code> etc.
	 * @param log
	 *            The {@link Logger} to use.
	 * @param ac
	 *            {@link AtlasConfig} to determine the different languages to
	 *            expect.
	 * 
	 * @throws IOException
	 *             If something goes wrong.
	 */
	public static void copyHTMLInfoFiles(final AtlasStatusDialog statusDialog,
			final File file, final AtlasConfig ac, final File targetDir,
			final Logger log) {

		// ****************************************************************************
		// Trying to copy HTML info files if they exist.
		// ****************************************************************************
		final String path = file.getAbsolutePath();

		for (final String lang : ac.getLanguages()) {

			final File source = new File((path.substring(0, path
					.lastIndexOf('.'))
					+ "_" + lang + ".html"));

			if (source.exists()) {
				try {
					AVUtil.copyFile(log, source, targetDir, true);
				} catch (final IOException e) {
					if (statusDialog != null)
						statusDialog.warningOccurred(e.getLocalizedMessage(),
								null, file + "  " + targetDir);
				}
			}
		}

	}

	/**
	 * Makes GeoTools cache the EPSG database (we assume that we use epsg-hsql).
	 * This may take a few moments and pauses the thread.<br/>
	 * The method also checks for a broken, cached HSAL database in
	 * C:\temp\Getools.... Broken HSQL databases caused ugly bugs on multiple
	 * windows computers.
	 * 
	 * @throws FactoryException
	 * @throws TransformException
	 */
	public static void cacheEPSG() throws FactoryException, TransformException {

		checkThatWeAreNotOnEDT();

		// The following outcommented stuff was valid for the old GT2.4 database
		// stucture. We might have to rewrite it for the new cached espg databse
		// one day...
		/*
		 * Check for a broken cached HSQL database.. if it is broken, delete it!
		 */
		// File hsqlespgCacheDir = new File(IOUtil.getTempDir(),
		// "Geotools/Databases/HSQL");
		// if (hsqlespgCacheDir != null && hsqlespgCacheDir.exists()) {
		// File fileData = new File(hsqlespgCacheDir, "EPSG.data");
		// File fileProperties = new File(hsqlespgCacheDir, "EPSG.properties");
		// File fileScript = new File(hsqlespgCacheDir, "EPSG.script");
		//
		// if (!fileData.exists() || !fileProperties.exists()
		// || !fileScript.exists()) {
		// LOGGER
		// .warn("The cached HSQL EPSG database is broken. It will be deleted and later recreated.");
		//
		// try {
		// FileUtils.deleteDirectory(hsqlespgCacheDir);
		// } catch (IOException e) {
		// LOGGER.error("Error deleting a broken cache dir "
		// + hsqlespgCacheDir.getAbsolutePath(), e);
		// }
		//
		// }
		//
		// }

		final CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
		final CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:32631");

		final Envelope envelope = new Envelope(0, 10, 0, 10);
		final MathTransform transform = CRS.findMathTransform(sourceCRS,
				targetCRS, true);

		@SuppressWarnings("unused")
		final Envelope result = JTS.transform(envelope, transform);

	}

	/**
	 * Throws a {@link RuntimeException} if this method is called on the EDT.
	 */
	public static void checkThatWeAreNotOnEDT() {
		if (SwingUtilities.isEventDispatchThread())
			throw new RuntimeException("On EDT!");
	}

	/**
	 * Throws a {@link RuntimeException} if this method is NOT called on the
	 * EDT.
	 */
	public static void checkThatWeAreOnEDT() {
		if (!SwingUtilities.isEventDispatchThread()) {
			LOGGER.error("Not on EDT");
			throw new RuntimeException("Not on EDT!");
		}
	}

	// /**
	// * Provocates caching of the EPSG database.
	// *
	// * @param progressWindow_
	// * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	// * Kr&uuml;ger</a>
	// */
	// public static void cacheEPSG() {
	// // if (progressWindow_ != null)
	// //
	// // progressWindow_.setDescription(AtlasViewer
	// // .R("AtlasViewer.process.EPSG_codes_caching"));
	// try {
	// cacheEPSG();
	// } catch (final Exception e) {
	// progressWindow_.exceptionOccurred(exception)
	// SwingUtilities.invokeLater(new Runnable() {
	//
	// @Override
	// public void run() {
	// ExceptionDialog.show(e);
	// }
	// });
	// }
	// // if (progressWindow_ != null)
	// // progressWindow_.exceptionOccurred(e);
	// // if (progressWindow_ != null)
	// // progressWindow_.complete();
	// }

	/**
	 * Checks if the URL points to an existsing file.
	 * 
	 * @return
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public static boolean exists(final URL url) {
		try {
			final InputStream openStream = url.openStream();
			openStream.close();
			return true;
		} catch (final Exception e) {
			return false;
		}
	}

	/**
	 * @return List of missing language Strings
	 * @param ac
	 *            {@link AtlasConfig} to determine the languages to expect.
	 * @param trans
	 *            The {@link Translation} to check.
	 */
	public static List<String> getMissingLanguages(final AtlasConfig ac,
			final Translation trans) {
		if (trans == null)
			return ac.getLanguages();

		final ArrayList<String> result = new ArrayList<String>();
		for (final String l : ac.getLanguages()) {
			final String t = trans.get(l);
			if (I8NUtil.isEmpty(t)) {
				result.add(l);
			}
		}
		return result;
	}

	/**
	 * Fix an ugly bug that disables the "Create Folder" button on Windows for
	 * the MyDocuments
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4847375
	 * 
	 * @see http://code.google.com/p/winfoldersjava/
	 */
	public static void fixBug4847375() {

		try {
			if (getOSType() == OSfamiliy.windows) {
				final String myDocumentsDirectoryName = javax.swing.filechooser.FileSystemView
						.getFileSystemView().getDefaultDirectory().getName();
				Runtime.getRuntime().exec(
						"attrib -r \"%USERPROFILE%\\"
								+ myDocumentsDirectoryName + "\"");
			}

		} catch (final Throwable e) {
			LOGGER.error("While fixing bug 4847375: ", e);
		}

		// try {
		// Runtime.getRuntime().exec(
		// "attrib -r \"%USERPROFILE%\\My Documents\"");
		// } catch (IOException e) {
		// }
		// try {
		// Runtime.getRuntime().exec(
		// "attrib -r \"%USERPROFILE%\\Mes documents\"");
		// } catch (IOException e) {
		// }
		// try {
		// String cmd = "attrib -r \"%USERPROFILE%\\Eigene Dateien\"";
		// Runtime.getRuntime().exec(cmd);
		// } catch (IOException e) {
		// }

	}

	/**
	 * Converts any filename to a String that is more Linux/Windows/Charset
	 * resistant. Used when importing DataPoolEntries.
	 * 
	 * TODO Improve
	 * 
	 * @param dirty
	 *            filename
	 * @return Clean filename
	 * @author SK
	 */
	public static String cleanFilename(String filename) {

		final String orig = filename;
		filename = filename.toLowerCase();
		filename = filename.replace(" ", "_");

		filename = filename.replace("&", "and");
		filename = filename.replace("$", "s");
		filename = filename.replace("\"", "x");
		filename = filename.replace("/", "_");
		filename = filename.replace("*", "x");
		filename = filename.replace("%", "p");
		filename = filename.replace("!", "i");
		filename = filename.replace("?", "q");
		filename = filename.replace("#", "x");

		filename = filename.replace("á", "a");
		filename = filename.replace("à", "a");
		filename = filename.replace("â", "a");
		filename = filename.replace("ä", "ae");

		filename = filename.replace("é", "e");
		filename = filename.replace("è", "e");
		filename = filename.replace("ê", "e");

		filename = filename.replace("ú", "u");
		filename = filename.replace("ù", "u");
		filename = filename.replace("û", "u");
		filename = filename.replace("ü", "ue");

		filename = filename.replace("í", "i");
		filename = filename.replace("ì", "i");
		filename = filename.replace("î", "i");

		filename = filename.replace("ó", "o");
		filename = filename.replace("ò", "o");
		filename = filename.replace("ô", "o");
		filename = filename.replace("ö", "oe");

		filename = filename.replace("ß", "s");

		filename = filename.replace("[", "_");
		filename = filename.replace("]", "_");
		filename = filename.replace("(", "_");
		filename = filename.replace(")", "_");
		filename = filename.replace("{", "_");
		filename = filename.replace("}", "_");

		filename = filename.replaceAll("[^\\p{ASCII}]", "x");

		/*
		 * Remove any leading numbers, because for the XML we have such a
		 * constraint:<br/>
		 * 
		 * [4] NCName ::= (Letter | '_') (NCNameChar)
		 * 
		 * Here it goes... looks like IDREF's (and ID's) have to start with a
		 * letter or an underscore.
		 */
		if (filename.length() > 0)
			filename = filename.replaceAll("^(\\d)", "n"
					+ filename.substring(0, 1));

		if (!orig.equals(filename))
			LOGGER.debug("Cleaned a filename from " + orig + " to " + filename);
		return filename;
	}

	/**
	 * Tries many different ways to open a HTML {@link URI}.
	 * 
	 * @param URI
	 *            An URI describing the HTML to open in a browser.
	 * @param owner
	 *            the parent GUI component. May be <code>null</code>. Only used
	 *            when fallback to {@link HTMLBrowserWindow} is used.
	 * 
	 * @throws NoSuchMethodException
	 */
	public static void lauchHTMLviewer(final Component owner, final URL url) {
		try {
			lauchHTMLviewer(owner, url.toURI());
		} catch (final URISyntaxException use) {
			throw new RuntimeException("Could not open HTML.", use);
		}
	}

	/**
	 * Tries many different ways to open a HTML {@link URI}.
	 * 
	 * @param URI
	 *            An URI describing the HTML to open in a browser.
	 * @param owner
	 *            the parent GUI component. May be <code>null</code>. Only used
	 *            when fallback to {@link HTMLBrowserWindow} is used.
	 */
	public static void lauchHTMLviewer(final Component owner, final URI uri) {
		boolean success = false;

		/**
		 * 1. We try to use the Java Desktop feature Before more Desktop API is
		 * used, first check whether the API is supported by this particular
		 * virtual machine (VM) on this particular host.
		 */
		if (Desktop.isDesktopSupported()
				&& Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			final Desktop desktop = Desktop.getDesktop();

			try {
				desktop.browse(uri);
				success = true;
			} catch (final Exception e) {
				LOGGER.error("Failed to open URL = " + uri
						+ " with the Java Desktop extension", e);
			}

		}

		if (!success)
			/**
			 * 2. Try to use more traditional ways to start a browser
			 */
			try {
				switch (getOSType()) {
				case mac:
					final Class fileMgr = Class
							.forName("com.apple.eio.FileManager");
					final java.lang.reflect.Method openURL = fileMgr
							.getDeclaredMethod("openURL",
									new Class[] { String.class });
					openURL.invoke(null, new Object[] { uri.toASCIIString() });
					break;
				case windows:
					Runtime.getRuntime().exec(
							"rundll32 url.dll,FileProtocolHandler "
									+ uri.toASCIIString());
					break;
				case linux:
					final String[] browsers = { "firefox", "opera",
							"konqueror", "epiphany", "mozilla", "netscape" };
					String browser = null;
					for (int count = 0; count < browsers.length
							&& browser == null; count++)
						if (Runtime.getRuntime().exec(
								new String[] { "which", browsers[count] })
								.waitFor() == 0)
							browser = browsers[count];
					if (browser != null) {
						final Process exec = Runtime.getRuntime().exec(
								new String[] { browser, uri.toASCIIString() });

						// TODO ???? final Process exec

						success = true;
					}
					break;
				}
			} catch (final Exception e) {
				LOGGER.warn("Failed to open the URL = " + uri
						+ " using the second approach", e);
				success = false;
			}

		if (!success) {
			/**
			 * 3. Fallback is to use the internal Java HTML BrowserPane
			 */
			// TODO Suboptimal..
			// .HTMLBrowserWindow should
			// become a Facory/singleton
			// pattern
			// (getInstanceFor(URL))
			HTMLBrowserWindow htmlWindow;
			try {
				htmlWindow = new HTMLBrowserWindow(owner, uri.toURL(),
						new File(uri.getPath()).getName(), null);
				htmlWindow.setVisible(true);
			} catch (final MalformedURLException mue) {
				throw new RuntimeException(
						"Could not open internal HTMLBrowserWindow.", mue);
			}
		}
	}

	/**
	 * @return The major.minor version, build number and build date
	 */
	public static String getVersionInfo() {
		/**
		 * Release properties einlesen
		 */
		try {
			return "v" + getVersionMaj() + "." + getVersionMin() + ", b"
					+ getVersionBuild() + " (" + getVersionBuildDate() + ")";
		} catch (final Exception e) {
			LOGGER.warn("Trying to read version information failed", e);
			return "unknown version";
		}
	}

	/**
	 * Return the major part of the software version of GP/AV/AS.
	 * 
	 * @throws Exception
	 *             if release.properties not found
	 */
	public static int getVersionMin() {
		try {

			final URL releasePropsURL = AtlasViewer.class
					.getResource("/release.properties");

			final Properties releaseProps = new Properties();
			final InputStream openStream = releasePropsURL.openStream();
			releaseProps.load(openStream);
			openStream.close();
			final String str = releaseProps.getProperty("min.version", "0");

			return Integer.parseInt(str);
		} catch (Exception e) {
			throw new RuntimeException(
					"/release.properties could not be read!", e);
		}

	}

	/**
	 * Return the major part of the software version of GP/AV/AS.
	 * 
	 * @throws Exception
	 *             if release.properties not found
	 */
	public static int getVersionMaj() {
		try {
			final URL releasePropsURL = AtlasViewer.class
					.getResource("/release.properties");

			final Properties releaseProps = new Properties();
			final InputStream openStream = releasePropsURL.openStream();
			releaseProps.load(openStream);
			openStream.close();
			final String str = releaseProps.getProperty("maj.version", "0");

			return Integer.parseInt(str);
		} catch (Exception e) {
			throw new RuntimeException(
					"/release.properties could not be read!", e);
		}
	}

	/**
	 * Return the major part of the software version of GP/AV/AS.
	 * 
	 * @throws Exception
	 *             if release.properties not found
	 */
	public static int getVersionBuild() {
		try {
			final URL releasePropsURL = AtlasViewer.class
					.getResource("/release.properties");

			final Properties releaseProps = new Properties();
			final InputStream openStream = releasePropsURL.openStream();
			releaseProps.load(openStream);
			openStream.close();
			final String str = releaseProps.getProperty("build", "0");

			return Integer.parseInt(str);
		} catch (Exception e) {
			throw new RuntimeException(
					"/release.properties could not be read!", e);
		}

	}

	/**
	 * Return the major part of the software version of GP/AV/AS.
	 * 
	 * @throws Exception
	 *             if release.properties not found
	 */
	public static Date getVersionBuildDate() {
		try {
			final URL releasePropsURL = AtlasViewer.class
					.getResource("/release.properties");
			if (releasePropsURL == null)
				throw new RuntimeException("/release.properties not found!");

			final Properties releaseProps = new Properties();
			final InputStream openStream = releasePropsURL.openStream();
			releaseProps.load(openStream);
			openStream.close();
			final String str = releaseProps.getProperty("datetime", "0");

			return new Date(Date.parse(str));
		} catch (Exception e) {
			throw new RuntimeException(
					"/release.properties could not be read!", e);
		}

	}

	/**
	 * @return The major.minor version + build number - no build date
	 * @see #getVersionInfo()
	 */
	public static String getVersionInfoShort() {
		/**
		 * Release properties einlesen
		 */
		try {
			return "v" + getVersionMaj() + "." + getVersionMin() + " b"
					+ getVersionBuild();
		} catch (final Exception e) {
			LOGGER.warn("Trying to read version information failed", e);
			return "unknown version";
		}
	}

	/**
	 * Print the GPL disclaimer to the given {@link Logger} as on INFO level.
	 */
	public static void logGPLCopyright(Logger logger) {

		logger
				.info("\nThis program is free software: you can redistribute it and/or modify\n"
						+ "it under the terms of the GNU General Public License as published by\n"
						+ "the Free Software Foundation, either version 3 of the License, or\n"
						+ "(at your option) any later version.\n"
						+ "\n"
						+ "This program is distributed in the hope that it will be useful,\n"
						+ "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
						+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
						+ "GNU General Public License for more details.\n");
	}

	/**
	 * Print the LGPL disclaimer to the given {@link Logger} as on INFO level.
	 */
	public static void logLGPLCopyright(Logger logger) {

		logger
				.info("\nThis program is free software: you can redistribute it and/or modify\n"
						+ "it under the terms of the GNU Lesser General Public License as published by\n"
						+ "the Free Software Foundation, either version 3 of the License, or\n"
						+ "(at your option) any later version.\n"
						+ "\n"
						+ "This program is distributed in the hope that it will be useful,\n"
						+ "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
						+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
						+ "GNU Lesser General Public License for more details.\n");

	}

	/**
	 * Convenience method to update the series legend {@link ChartStyle} title
	 * and tooltip from given {@link AttributeMetadata}.
	 * 
	 * @param styledLayer
	 * @param chartStyle
	 * @param rendererIndex
	 * @param seriesIdx
	 * @param languages
	 */
	public static void applyDefaultTitleAndTranslationToLegend(
			StyledFeaturesInterface<?> styledLayer,
			FeatureChartStyle chartStyle, int rendererIndex, int seriesIdx,
			List<String> languages) {

		Translation legendTooltipTranslation = chartStyle.getRendererStyle(
				rendererIndex).getSeriesLegendTooltip(seriesIdx)
				.getLabelTranslation();

		Translation legendTitleTranslation = chartStyle.getRendererStyle(
				rendererIndex).getSeriesLegendLabel(seriesIdx)
				.getLabelTranslation();

		/* First series = DOMAIN */
		String attName = chartStyle.getAttributeName(seriesIdx + 1);

		// AttributeMetadata attMeta =
		// ASUtil.getAttributeMetadataFor(styledLayer,
		// attName);
		AttributeMetadata attMeta = styledLayer.getAttributeMetaDataMap().get(
				attName);

		/*
		 * This should trigger all listeners
		 */
		for (String lang : languages) {
			String titleValue = attMeta.getTitle().get(lang);

			/* Instead on an empty title, we use the raw attribute name */
			if (I8NUtil.isEmpty(titleValue))
				titleValue = attName;

			legendTitleTranslation.put(lang, titleValue);
			legendTooltipTranslation.put(lang, attMeta.getDesc().get(lang));
			
			// Try to update the Y-Axis label
			if (chartStyle.getAxisStyle(seriesIdx+1) != null)
				chartStyle.getAxisStyle(seriesIdx+1).getLabelTranslation().put(lang, titleValue);
			
		}
		
	}

	/**
	 * Convenience method to update the series legend {@link ChartStyle} title
	 * and tooltip from given {@link AttributeMetadata}.
	 * 
	 * @param styledLayer
	 * @param chartStyle
	 * @param rendererIndex
	 * @param seriesIdx
	 * @param languages
	 */
	public static void applyDefaultTitleAndTranslationToAxis(
			StyledFeaturesInterface styledLayer, FeatureChartStyle chartStyle,
			int axis, int attribIdx, List<String> languages) {

		Translation legendTitleTranslation = chartStyle.getAxisStyle(axis)
				.getLabelTranslation();

		/* First series = DOMAIN */
		String attName = chartStyle.getAttributeName(attribIdx);

		AttributeMetadata attMeta = styledLayer.getAttributeMetaDataMap().get(
				attName);

		/*
		 * This should trigger all listeners
		 */
		for (String lang : languages) {
			String titleValue = attMeta.getTitle().get(lang);

			/* Instead on an empty title, we use the raw attribute name */
			if (I8NUtil.isEmpty(titleValue))
				titleValue = attName;

			legendTitleTranslation.put(lang, titleValue);
		}
	}

	/**
	 * @param crs
	 * @param value
	 */
	public static String formatCoord(CoordinateReferenceSystem crs, double value) {

		StringBuffer sb = new StringBuffer(DecimalFormat.getNumberInstance()
				.format(value));

		Unit<?> unit = CRSUtilities.getUnit(crs.getCoordinateSystem());
		if (unit != null) {
			sb.append(unit.toString());
		}

		return sb.toString();
	}

	public static JDialog getWaitDialog(Component owner, String msg) {
		final JDialog waitFrame = new JDialog(SwingUtil.getParentWindow(owner));

		waitFrame.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		JPanel cp = new JPanel(new MigLayout());
		final JLabel label = new JLabel(msg, Icons.ICON_TASKRUNNING_BIG,
				JLabel.LEADING);
		cp.add(label);
		waitFrame.setContentPane(cp);

		waitFrame.setAlwaysOnTop(true);
		waitFrame.pack();
		SwingUtil.centerFrameOnScreen(waitFrame);
		waitFrame.setVisible(true);

		return waitFrame;
	}

	public static void cacheEPSG(Component parent) throws InterruptedException,
			ExecutionException {
		// checkThatWeAreOnEDT();

		AtlasStatusDialog statusDialog = new AtlasStatusDialog(parent, null,
				AtlasViewer.R("AtlasViewer.process.EPSG_codes_caching"));
		AtlasSwingWorker<Void> swingWorker = new AtlasSwingWorker<Void>(
				statusDialog) {

			@Override
			protected Void doInBackground() throws Exception {
				cacheEPSG();
				return null;
			}

		};

		swingWorker.executeModal();
	}

}
