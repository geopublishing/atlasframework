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
package org.geopublishing.atlasViewer;

import java.awt.Component;
import java.awt.FontMetrics;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.measure.unit.Unit;
import javax.swing.JDialog;
import javax.swing.JLabel;
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
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geotools.resources.CRSUtilities;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import schmitzm.io.IOUtil;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.jfree.feature.style.FeatureChartStyle;
import schmitzm.lang.ResourceProvider;
import schmitzm.swing.SwingUtil;
import skrueger.AttributeMetadataImpl;
import skrueger.AttributeMetadataInterface;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.i8n.I8NUtil;
import skrueger.i8n.Translation;


/**
 * Collection of Atlas related static methods.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
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

	/**
	 * {@link ResourceProvider}, der die Lokalisation fuer GUI-Komponenten des
	 * Package {@code skrueger.swing} zur Verfuegung stellt. Diese sind in
	 * properties-Datein unter {@code skrueger.swing.resource.locales}
	 * hinterlegt.
	 */
	private static ResourceProvider RESOURCE = ResourceProvider.newInstance(
			"locales.AtlasViewerTranslation", Locale.ENGLISH);

	/**
	 * Convenience method to access the {@link AtlasViewerGUI}s translation
	 * resources.
	 * 
	 * @param key
	 *            the key for the AtlasViewerTranslation.properties file
	 * @param values
	 *            optinal values
	 */
	public static String R(final String key, final Object... values) {
		return RESOURCE.getString(key, values);
	}

	public static final Logger LOGGER = Logger.getLogger(AVUtil.class);

	/**
	 * Creates a random number generator mainly used for IDs
	 */
	public final static Random RANDOM = new Random();

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
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
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
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
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
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
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
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
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
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
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
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
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
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
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
	// * Tzeggai</a>
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
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
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
			if (SystemUtils.IS_OS_WINDOWS) {
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
	 * Convenience method to update the series legend {@link ChartStyle} title
	 * and tooltip from given {@link AttributeMetadataImpl}.
	 * 
	 * @param styledLayer
	 * @param chartStyle
	 * @param rendererIndex
	 * @param seriesIdx
	 * @param languages
	 */
	public static void applyDefaultTitleAndTranslationToLegend(
			final StyledFeaturesInterface<?> styledLayer,
			final FeatureChartStyle chartStyle, final int rendererIndex,
			final int seriesIdx, final List<String> languages) {

		final Translation legendTooltipTranslation = chartStyle
				.getRendererStyle(rendererIndex).getSeriesLegendTooltip(
						seriesIdx).getLabelTranslation();

		final Translation legendTitleTranslation = chartStyle.getRendererStyle(
				rendererIndex).getSeriesLegendLabel(seriesIdx)
				.getLabelTranslation();

		/* First series = DOMAIN */
		final String attName = chartStyle.getAttributeName(seriesIdx + 1);

		// AttributeMetadata attMeta =
		// ASUtil.getAttributeMetadataFor(styledLayer,
		// attName);
		final AttributeMetadataImpl attMeta = styledLayer
				.getAttributeMetaDataMap().get(attName);

		/*
		 * This should trigger all listeners
		 */
		for (final String lang : languages) {
			String titleValue = attMeta.getTitle().get(lang);

			/* Instead on an empty title, we use the raw attribute name */
			if (I8NUtil.isEmpty(titleValue))
				titleValue = attName;

			legendTitleTranslation.put(lang, titleValue);
			legendTooltipTranslation.put(lang, attMeta.getDesc().get(lang));

			// Try to update the Y-Axis label
			if (chartStyle.getAxisStyle(seriesIdx + 1) != null)
				chartStyle.getAxisStyle(seriesIdx + 1).getLabelTranslation()
						.put(lang, titleValue);

		}

	}

	/**
	 * Convenience method to update the series legend {@link ChartStyle} title
	 * and tooltip from given {@link AttributeMetadataImpl}.
	 * 
	 * @param styledLayer
	 * @param chartStyle
	 * @param rendererIndex
	 * @param seriesIdx
	 * @param languages
	 */
	public static void applyDefaultTitleAndTranslationToAxis(
			final StyledFeaturesInterface styledLayer,
			final FeatureChartStyle chartStyle, final int axis,
			final int attribIdx, final List<String> languages) {

		final Translation legendTitleTranslation = chartStyle
				.getAxisStyle(axis).getLabelTranslation();

		/* First series = DOMAIN */
		final String attName = chartStyle.getAttributeName(attribIdx);

		final AttributeMetadataInterface attMeta = styledLayer
				.getAttributeMetaDataMap().get(attName);

		/*
		 * This should trigger all listeners
		 */
		for (final String lang : languages) {
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
	public static String formatCoord(final CoordinateReferenceSystem crs,
			final double value) {

		final StringBuffer sb = new StringBuffer(DecimalFormat
				.getNumberInstance().format(value));

		final Unit<?> unit = CRSUtilities.getUnit(crs.getCoordinateSystem());
		if (unit != null) {
			sb.append(unit.toString());
		}

		return sb.toString();
	}

	public static JDialog getWaitDialog(final Component owner, final String msg) {
		final JDialog waitFrame = new JDialog(SwingUtil.getParentWindow(owner));

		waitFrame.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		final JPanel cp = new JPanel(new MigLayout());
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

}
