package org.geopublishing.atlasViewer.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVProps;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.AtlasStatusDialogInterface;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.layer.DpLayerRaster;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVector;
import org.geopublishing.atlasViewer.exceptions.AtlasFatalException;
import org.geopublishing.atlasViewer.swing.internal.AtlasExportTask;
import org.geopublishing.atlasViewer.swing.internal.AtlasStatusDialog;
import org.geotools.data.DataUtilities;
import org.jdesktop.swingx.color.EyeDropperColorChooserPanel;

import schmitzm.geotools.GTUtil;
import schmitzm.io.IOUtil;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.SwingUtil;

public class AVSwingUtil extends AVUtil {
	static final Logger LOGGER = Logger.getLogger(AVSwingUtil.class);

	static HashMap<URL, File> cachedLocalCopiedFiles = new HashMap<URL, File>();

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
				AVUtil.R("GeneralQuestionDialogTitle"),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null);
		return result == JOptionPane.YES_OPTION;
	}

	/**
	 * We use a single JColorChooser in the whole application to share the list
	 * of "last used colors"
	 */
	private static JColorChooser jcolorchooser = null;

	public static JColorChooser getJcolorChooser() {
		if (jcolorchooser == null) {
			jcolorchooser = new JColorChooser();
			jcolorchooser.addChooserPanel(new EyeDropperColorChooserPanel());
		}
		return jcolorchooser;
	}

	/**
	 * Using this method the JColorChooser remembers the colors selected
	 * earlier. A big help!
	 * 
	 * @param component
	 * 
	 * @param title
	 *            Title {@link String} to use for this {@link JDialog}
	 * 
	 * @param initialColor
	 *            {@link Color} to start with
	 * 
	 * @return The sleceted {@link Color} or <code>null</code> if canceled.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public static Color showColorChooser(final Component component,
			final String title, final Color initialColor) {
		getJcolorChooser().setColor(initialColor);
		if (colorDialog == null) {
			colorDialog = JColorChooser.createDialog(component, title, true,
					getJcolorChooser(), null, new ActionListener() {

						@Override
						public void actionPerformed(final ActionEvent e) {
							jcolorchooserHasBeenCanceled = true;
						}

					});
		}

		jcolorchooserHasBeenCanceled = false;
		colorDialog.setTitle(title);
		colorDialog.setVisible(true);

		if (jcolorchooserHasBeenCanceled)
			return initialColor;
		else
			return getJcolorChooser().getColor();
	}

	/**
	 * We use a single JColorChooser in the whole application to share the list
	 * of "last used colors". This flag manages a cancel on the dialog.
	 */
	private static boolean jcolorchooserHasBeenCanceled = false;

	private static JDialog colorDialog;

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
					AVUtil.R("GeneralQuestionDialogTitle"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
					null);
			return result == JOptionPane.OK_OPTION;
		} else {

			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						resultAskOkCancel = JOptionPane.showConfirmDialog(
								owner, question,
								AVUtil.R("GeneralQuestionDialogTitle"),
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE, null);

					}
				});
			} catch (Exception e) {
				LOGGER.error(e);
			}

			return resultAskOkCancel == JOptionPane.OK_OPTION;

		}

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
			LOGGER.debug("Not copying the URL to temp file because we are local and not in a JAR.");
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
				+ IOUtil.cleanFilename(title), postFix);

		new AtlasExportTask(owner, AVUtil.R("dialog.title.wait")) {

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
	public static void copyHTMLInfoFiles(
			final AtlasStatusDialogInterface statusDialog, final File file,
			final AtlasConfig ac, final File targetDir, final Logger log) {

		// ****************************************************************************
		// Trying to copy HTML info files if they exist.
		// ****************************************************************************
		final String path = file.getAbsolutePath();

		for (final String lang : ac.getLanguages()) {

			final File source = new File((path.substring(0,
					path.lastIndexOf('.'))
					+ "_" + lang + ".html"));

			if (source.exists()) {
				try {
					IOUtil.copyFile(log, source, targetDir, true);
				} catch (final IOException e) {
					if (statusDialog != null)
						statusDialog.warningOccurred(e.getLocalizedMessage(),
								null, file + "  " + targetDir);
				}
			}
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
				LOGGER.debug("Special case here.. we expect the PDF to lay uncompressed next to atlas.jar or atlas.gpa");
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

				LOGGER.debug("Special case here.. we expect the PDF to lay uncompressed next to atlas.jar or atlas.gpa");
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
				LOGGER.debug("Using Desktop.getDesktop().open to open the following canonical file:\n"
						+ pdfFile);
				// HARDCRE CRASH ON arthurs computer!
				Desktop.getDesktop().open(pdfFile);
			} catch (Exception exWhileDesktop) {
				LOGGER.info("Can't use Desktop ?! :-( ", exWhileDesktop);

				final List<String> command = new ArrayList<String>();

				String pdfPath = pdfFile.getAbsolutePath();

				if (SystemUtils.IS_OS_WINDOWS) {
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

				} else if (SystemUtils.IS_OS_MAC) {
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

				else if (SystemUtils.IS_OS_LINUX) {
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
	 * Shows a waiting dialog while the EPSG data is cached and extended on
	 * another thread.
	 */
	public static void initEPSG(Component parent) {
		SwingUtil.checkOnEDT();

		AtlasStatusDialog statusDialog = new AtlasStatusDialog(parent, null,
				AVUtil.R("AtlasViewer.process.EPSG_codes_caching"));
		AtlasSwingWorker<Void> swingWorker = new AtlasSwingWorker<Void>(
				statusDialog) {

			@Override
			protected Void doInBackground() throws Exception {
				GTUtil.initEPSG();
				return null;
			}

		};

		try {
			swingWorker.executeModal();
		} catch (CancellationException e) {
		} catch (ExecutionException e) {
			throw new RuntimeException(
					AVUtil.R("AtlasViewer.process.EPSG_codes_caching"), e);
		} catch (InterruptedException e) {
			throw new RuntimeException(
					AVUtil.R("AtlasViewer.process.EPSG_codes_caching"), e);
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
				if (SystemUtils.IS_OS_MAC) {

					final Class fileMgr = Class
							.forName("com.apple.eio.FileManager");
					final java.lang.reflect.Method openURL = fileMgr
							.getDeclaredMethod("openURL",
									new Class[] { String.class });
					openURL.invoke(null, new Object[] { uri.toASCIIString() });
				} else if (SystemUtils.IS_OS_WINDOWS) {

					Runtime.getRuntime().exec(
							"rundll32 url.dll,FileProtocolHandler "
									+ uri.toASCIIString());
				} else if (SystemUtils.IS_OS_LINUX) {

					final String[] browsers = { "firefox", "opera",
							"konqueror", "epiphany", "mozilla", "netscape" };
					String browser = null;
					for (int count = 0; count < browsers.length
							&& browser == null; count++)
						if (Runtime
								.getRuntime()
								.exec(new String[] { "which", browsers[count] })
								.waitFor() == 0)
							browser = browsers[count];
					if (browser != null) {
						final Process exec = Runtime.getRuntime().exec(
								new String[] { browser, uri.toASCIIString() });

						// TODO ???? final Process exec

						success = true;
					}
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

	//
	// /**
	// * Tries many different ways to open a HTML {@link URI}.
	// *
	// * @param URI
	// * An URI describing the HTML to open in a browser.
	// * @param owner
	// * the parent GUI component. May be <code>null</code>. Only used
	// * when fallback to {@link HTMLBrowserWindow} is used.
	// */
	// public static void lauchHTMLviewer(final Component owner, final URI uri)
	// {
	// boolean success = false;
	//
	// /**
	// * 1. We try to use the Java Desktop feature Before more Desktop API is
	// * used, first check whether the API is supported by this particular
	// * virtual machine (VM) on this particular host.
	// */
	// if (Desktop.isDesktopSupported()
	// && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
	// final Desktop desktop = Desktop.getDesktop();
	//
	// try {
	// desktop.browse(uri);
	// success = true;
	// } catch (final Exception e) {
	// LOGGER.error("Failed to open URL = " + uri
	// + " with the Java Desktop extension", e);
	// }
	//
	// }
	//
	// if (!success)
	// /**
	// * 2. Try to use more traditional ways to start a browser
	// */
	// try {
	// switch (getOSType()) {
	// case mac:
	// final Class fileMgr = Class
	// .forName("com.apple.eio.FileManager");
	// final java.lang.reflect.Method openURL = fileMgr
	// .getDeclaredMethod("openURL",
	// new Class[] { String.class });
	// openURL.invoke(null, new Object[] { uri.toASCIIString() });
	// break;
	// case windows:
	// Runtime.getRuntime().exec(
	// "rundll32 url.dll,FileProtocolHandler "
	// + uri.toASCIIString());
	// break;
	// case linux:
	// final String[] browsers = { "firefox", "opera",
	// "konqueror", "epiphany", "mozilla", "netscape" };
	// String browser = null;
	// for (int count = 0; count < browsers.length
	// && browser == null; count++)
	// if (Runtime.getRuntime().exec(
	// new String[] { "which", browsers[count] })
	// .waitFor() == 0)
	// browser = browsers[count];
	// if (browser != null) {
	// final Process exec = Runtime.getRuntime().exec(
	// new String[] { browser, uri.toASCIIString() });
	//
	// // TODO ???? final Process exec
	//
	// success = true;
	// }
	// break;
	// }
	// } catch (final Exception e) {
	// LOGGER.warn("Failed to open the URL = " + uri
	// + " using the second approach", e);
	// success = false;
	// }
	//
	// if (!success) {
	// /**
	// * 3. Fallback is to use the internal Java HTML BrowserPane
	// */
	// // TODO Suboptimal..
	// // .HTMLBrowserWindow should
	// // become a Facory/singleton
	// // pattern
	// // (getInstanceFor(URL))
	// HTMLBrowserWindow htmlWindow;
	// try {
	// htmlWindow = new HTMLBrowserWindow(owner, uri.toURL(),
	// new File(uri.getPath()).getName(), null);
	// htmlWindow.setVisible(true);
	// } catch (final MalformedURLException mue) {
	// throw new RuntimeException(
	// "Could not open internal HTMLBrowserWindow.", mue);
	// }
	// }
	// }
	//
	//

	/**
	 * Copies ONLY the {@link #getFilename()} to a {@link File} in the
	 * temp-folder
	 * 
	 * @see #cleanupTemp() which is responsible to remove these files again.
	 * 
	 * @return {@link File} in temp
	 * @throws IOException
	 */
	public static File getLocalCopy(DpEntry<? extends ChartStyle> dpe,
			Component owner) throws IOException {

		if (dpe.getLocalTempFile() == null || !dpe.getLocalTempFile().exists()) {

			String postFix = IOUtil.getFileExt(new File(dpe.getFilename()));

			dpe.setLocalTempFile(createLocalCopyFromURL(owner,
					getUrl(dpe, owner), dpe.getTitle().toString(),
					postFix.equals("") ? null : postFix));

		}
		return dpe.getLocalTempFile();
	}

	/**
	 * Returns a URL for this {@link CopyOfDpEntry}. This references the "main"
	 * file, e.g. the .shp for a {@link DpLayerVector} or the .tiff for a
	 * {@link DpLayerRaster} etc. All other {@link URL}s (e.g. .prj) can be
	 * generated using {@link IOUtil}.changeUrlExt
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 * 
	 * @throws IOException
	 * @throws AtlasFatalException
	 * @return <code>null</code> or the main URL for this Layer, usually
	 *         pointing to a .tif or .gif (or WMS or shp or GML etc)
	 * 
	 */
	public static final URL getUrl(DpEntry<?> dpe, Component comp) {
		if (comp == null)
			return getUrl(dpe, (AtlasStatusDialog) null);
		return getUrl(dpe, new AtlasStatusDialog(comp));
	}

	/**
	 * Returns a URL for this {@link CopyOfDpEntry}. This references the "main"
	 * file, e.g. the .shp for a {@link DpLayerVector} or the .tiff for a
	 * {@link DpLayerRaster} etc. All other {@link URL}s (e.g. .prj) can be
	 * generated using {@link IOUtil}.changeUrlExt
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 * 
	 * @throws IOException
	 * @throws AtlasFatalException
	 * @return <code>null</code> or the main URL for this Layer, usually
	 *         pointing to a .tif or .gif (or WMS or shp or GML etc)
	 * 
	 */
	public static final URL getUrl(DpEntry dpe,
			AtlasStatusDialogInterface statusDialog) {
		if (dpe.url == null) {

			if (JNLPSwingUtil.isAtlasDataFromJWS(dpe.getAtlasConfig())) {
				try {
					JNLPSwingUtil.loadPart(dpe.getId(), statusDialog);
					// TODO was ist wenn man abbricht?!
				} catch (IOException e) {
					LOGGER.error("loading part failed", e);
				}
			}

			// Yes, we call the deplrecated one here!
			dpe.getUrl();
		}
		return dpe.url;
	}

	/**
	 * Exports only the {@link #getFilename()} file to a Directory Overwrite
	 * this function, if you need to export more files.
	 * 
	 * @throws IOException
	 * @throws AtlasFatalException
	 */
	public static void exportTo(DpEntry<? extends ChartStyle> dpe,
			File targetDir, Component owner) throws IOException,
			AtlasFatalException {

		String ending = dpe.getFilename().substring(
				dpe.getFilename().lastIndexOf('.'));
		File targetFile = new File(targetDir, dpe.getTitle().toString()
				.replace(' ', '_')
				+ ending);

		FileUtils.copyURLToFile(getUrl(dpe, owner), targetFile);
	}

	/**
	 * Asks the user to select a Directory for export of this layer.
	 * 
	 * @see #isExportable()
	 * @param owner
	 *            GUI {@link Frame}
	 * @return The selected directory or null if canceled.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public static File selectExportDir(Component owner, AtlasConfig atlasConfig) {

		final File startWith = new File(atlasConfig.getProperties().get(
				AVProps.Keys.LastExportFolder, "."));

		JFileChooser fc = new JFileChooser(startWith);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		fc.setToolTipText("Select the folder where to put the files");

		int showSaveDialog = fc.showSaveDialog(owner);
		if (showSaveDialog != JFileChooser.APPROVE_OPTION)
			return null;

		File exportDir = fc.getSelectedFile();

		if (exportDir == null)
			return null;

		atlasConfig.getProperties().set(owner, AVProps.Keys.LastExportFolder,
				exportDir.getAbsolutePath());

		return exportDir;
	}

	/**
	 * 
	 * @param runnable
	 * @return
	 * @throws Exception 
	 */
	public static Object runWaiting(Component owner,
			final RunnableFuture<Object> runnable) throws Exception {

		AtlasStatusDialog statusDialog = new AtlasStatusDialog(owner);

		AtlasSwingWorker<Object> openFileWorker = new AtlasSwingWorker<Object>(
				statusDialog) {

			@Override
			protected Object doInBackground() throws IOException,
					InterruptedException {
				runnable.run();
				try {
					return runnable.get();
				} catch (ExecutionException e) {
					return e;
				}
			}

		};
		try {
			openFileWorker.executeModal();
			return runnable.get();
		} catch (Exception e1) {
			throw (e1);
		}
	}

}
