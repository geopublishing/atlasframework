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
package org.geopublishing.atlasViewer.dp;

import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.ExportableLayer;
import org.geopublishing.atlasViewer.JNLPUtil;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;
import org.geopublishing.atlasViewer.swing.JNLPSwingUtil;

import rachel.ResourceManager;
import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.i8n.I8NUtil;
import skrueger.i8n.Translation;

public abstract class DpEntry<CHART_STYLE_IMPL extends ChartStyle> implements
		Comparable<DpEntry<? extends ChartStyle>>, Serializable,
		ExportableLayer {
	static private Logger LOGGER = Logger.getLogger(DpEntry.class);

	/**
	 * An internal flag, that remembers if the entry could not be read. If set
	 * to not <code>null</code>, the system will not aggressively try to get the
	 * entry every time it requests the data/crs.. , but just return
	 * <code>null</code> instead.
	 */
	private Exception brokenException = null;

	/**
	 * If <code>true</code>, determining the CRS has already failed and is not
	 * tried again until {@link #geoObjBroken} is <code>false</code>.
	 */
	public boolean isBroken() {
		return brokenException != null;
	}

	/**
	 * If <code>true</code>, determining the CRS has already failed and is not
	 * tried again until {@link #geoObjBroken} is <code>false</code>.
	 */
	public void setBrokenException(Exception e) {
		LOGGER.info(getId() + " is broken ", e);
		brokenException = e;
	}

	/**
	 * Returns the Exception that was the reason why the layer could not be
	 * read.
	 */
	public Exception getBrokenException() {
		return brokenException;
	}

	/**
	 * List of charts for this DpEntry
	 */
	private List<CHART_STYLE_IMPL> charts = new ArrayList<CHART_STYLE_IMPL>();

	/**
	 * Returns the list of charts for this layer. Never <code>null</code>.
	 * manipulating this list directly affects the numer of charts.
	 */
	public List<CHART_STYLE_IMPL> getCharts() {
		return charts;
	}

	/**
	 * Returns the ChartStyle with the given ID or <code>null</code>
	 */
	public CHART_STYLE_IMPL getChartForID(String chartId) {
		for (CHART_STYLE_IMPL ls : getCharts()) {
			if (ls.getID().equals(chartId))
				return ls;
		}
		return null;

	}

	protected Translation title = new Translation(), desc = new Translation(),
			keywords = new Translation();

	/** An {@link ImageIcon} for this {@link CopyOfDpEntry}. May be null * */
	protected ImageIcon imageIcon;

	/**
	 * The main URL, pointing to the main file, e.g. .shp or .tiff or
	 * .properties or .avi
	 */
	public URL url;

	/**
	 * Directory where data file(s) are stored (target for copy when importing)
	 * its the atlasXY/ad/data/dataDirname/ folder
	 */
	protected String dataDirname;

	/**
	 * Filename of the main file, e.g. .shp or .tiff associated files need the
	 * same base name
	 */
	private String filename;

	/**
	 * The {@link AtlasConfig} that the parent {@link DataPool} belongs to
	 */
	protected AtlasConfig ac;

	/**
	 * The ID is also the name of the corresponding JAR file and the dataDirname
	 */
	private String id;

	/**
	 * Indicating, if this {@link CopyOfDpEntry} has already been seen (
	 * {@link #seeJAR()} ), and therefor already is visible to the
	 * {@link ResourceManager} and is already cached by JWS.
	 * 
	 * if true, than we are using JWS
	 */
	private boolean downloadedAndVisible = false;

	/**
	 * Has this {@link CopyOfDpEntry} already been disposed? If true, further
	 * use is undefined.*
	 */
	protected boolean disposed = false;

	private File localTempFile;

	/** By default, the entries are not exportable * */
	private Boolean exportable = false;

	/**
	 * Caches the charset assigned for this {@link CopyOfDpEntry}. Note: not all
	 * {@link CopyOfDpEntry}s value a charset definition!
	 */
	protected Charset charset;

	/**
	 * Caches the quality calculated for this layer for a while
	 */
	private Double quality;

	/**
	 * Last time the quality was calculated
	 */
	long qualityLastTimeCalculated;

	// DnD Stuff...
	final public static DataFlavor INFO_FLAVOR = new DataFlavor(DpEntry.class,
			"DatapoolEntry Information");

	static DataFlavor flavors[] = { INFO_FLAVOR };

	/**
	 * Constructs a DatapoolEntry
	 * 
	 * @param ac
	 *            {@link AtlasConfig} this {@link CopyOfDpEntry} belongs to
	 */
	public DpEntry(AtlasConfig ac) {
		this.ac = ac;
	}

	/**
	 * Clears all memory-intensive or cached objects
	 */
	public void uncache() {

		// Do not set charset to null... even though if could be changed on
		// disk, we want to keep the GUI change
		// charset = null;

		downloadedAndVisible = false;
		brokenException = null;
		url = null;
		quality = null;
	}

	/**
	 * Set the sub directory where data of this {@link CopyOfDpEntry} is saved
	 * e.g. /Atlas2.0/ad/data/R12 would result in a setDataDirname("R12")
	 */
	public void setDataDirname(String folder) {
		dataDirname = folder;
	}

	/**
	 * @return the subdirectory's name where data of this {@link CopyOfDpEntry}
	 *         is saved
	 */
	public String getDataDirname() {
		return dataDirname;
	}

	/**
	 * @return an i8ned name for this {@link CopyOfDpEntry}
	 */
	@Override
	public String toString() {
		if (title != null)
			return title.toString();
		if (id != null)
			return id.toString();
		if (desc != null)
			return desc.toString();
		return super.toString();
	}

	/**
	 * Responsible for the order when sorting {@link CopyOfDpEntry}s
	 */
	@Override
	public int compareTo(DpEntry dpe2) {

		if (dpe2.getId() == null) {
			return -1;
		}
		if (getId() == null) {
			return +1;
		}

		return getId().compareTo(dpe2.getId());
	}

	/**
	 * True if the {@link CopyOfDpEntry} can be presented by a {@link JSMapPane}
	 * . When isLayer() == true, then the DatapooleEnty can be casted to a
	 * {@link DpLayer} and {@link DpLayer}.getGT() returns an object that
	 * {@link JSMapPane} can present.
	 */
	public abstract boolean isLayer();

	public final String getId() {
		return id;
	}

	public final void setId(String id) {
		this.id = id;
	}

	/**
	 * @return a {@link Translation} as human-readable title for this
	 *         {@link CopyOfDpEntry} or <code>null</code> is not title has been
	 *         set so far.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public final Translation getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            Defines a {@link Translation} as human-readable title for this
	 *            {@link CopyOfDpEntry}
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public final void setTitle(Translation title) {
		this.title = title;
	}

	public final Translation getDesc() {
		return desc;
	}

	public final void setDesc(Translation desc) {
		this.desc = desc;
	}

	/**
	 * //TODO what will happen if we have an online source one day?
	 * 
	 * @return Filename as String
	 */
	public final String getFilename() {
		return filename;
	}

	/**
	 * Set the {@link String} filename by giving a {@link File}. Only the last
	 * part of the File url will be used.
	 */
	public final void setFilename(File file) {
		setFilename(file.getName());
	}

	/**
	 * Set the filename of the main file for this {@link CopyOfDpEntry}. e.g.
	 * "strassen.shp" *
	 */
	public final void setFilename(String filename) {
		this.filename = filename;
	}

	public final AtlasConfig getAtlasConfig() {
		return ac;
	}

	public final void setAtlasConfig(AtlasConfig ac) {
		this.ac = ac;
	}

	/**
	 * Returns the internationalised keywords. As <code>keywords</code> are
	 * optional, null is never returned, but instead an empty
	 * {@link Translation}
	 */
	public Translation getKeywords() {
		if (keywords == null) {
			keywords = new Translation();
		}
		return keywords;
	}

	public void setKeywords(Translation keywords) {
		this.keywords = keywords;
	}

	/**
	 * Has this {@link CopyOfDpEntry} already been disposed? If true, further
	 * use is undefined.*
	 */
	public boolean isDisposed() {
		return disposed;
	}

	/** An {@link ImageIcon} for this {@link CopyOfDpEntry}. May be null. * */
	public ImageIcon getImageIcon() {
		return imageIcon;
	}

	/** An {@link ImageIcon} for this {@link CopyOfDpEntry}. May be null * */
	public void setImageIcon(ImageIcon imageIcon) {
		this.imageIcon = imageIcon;
	}

	/*
	 * @see skrueger.atlas.ExportableLayer#isExportable()
	 */
	@Override
	public Boolean isExportable() {
		return exportable;
	}

	/**
	 * 
	 */
	public void setExportable(boolean b) {
		exportable = b;
	}

	/**
	 * This will delete any temp files that were created by the
	 * {@link AtlasViewerGUI} software. Tempfiles are created when contents from
	 * JARs are shown that have to be extracted first.
	 * <p>
	 * The temporary files of the AtlasViewer are identified by their filename
	 * starting with {@link AVUtil#ATLAS_TEMP_FILE_INSTANCE_ID}
	 */
	public static void cleanupTemp() {

		AVUtil.cleanupTempDir(AVUtil.ATLAS_TEMP_FILE_INSTANCE_ID,
				AVUtil.ATLAS_TEMP_FILE_BASE_ID);

	}

	public boolean isDownloadedAndVisible() {
		return downloadedAndVisible;
	}

	/**
	 * @return A value between 0 and 1 which describes how good much metadata
	 *         has been provided. 1 is great. If the entry is broken returns 0.
	 */
	public Double getQuality() {

		if (isBroken())
			return 0.;

		if (quality != null
				|| (System.currentTimeMillis() - qualityLastTimeCalculated) > 500) {
			qualityLastTimeCalculated = System.currentTimeMillis();

			final List<String> languages = getAtlasConfig().getLanguages();
			Double averageChartQuality = 1.;
			if (getCharts().size() > 0) {
				averageChartQuality = getAverageChartQuality();
			}
			quality = (I8NUtil.qmTranslation(languages, getTitle()) * 4.
					+ I8NUtil.qmTranslation(languages, getDesc()) * 2.
					+ I8NUtil.qmTranslation(languages, getKeywords()) * 1. + averageChartQuality * 3.) / 10.;
		}
		return quality;
	}

	/**
	 * @return the average quality index of the charts in this {@link DpLayer}
	 */
	public Double getAverageChartQuality() {
		if (getCharts().size() == 0)
			return null;
		Double averageChartQM = 0.;
		for (final CHART_STYLE_IMPL chart : getCharts()) {
			averageChartQM += getChartQuality(chart);
		}
		averageChartQM /= getCharts().size();
		return averageChartQM;
	}

	/**
	 * @param Chart
	 *            The Chart to check
	 * @return A value between 0 and 1 describing how much metadata is provided
	 *         for given Chart.
	 */
	public Double getChartQuality(ChartStyle Chart) {
		final List<String> languages = getAtlasConfig().getLanguages();
		final Double result = (I8NUtil.qmTranslation(languages, Chart
				.getTitleStyle().getLabelTranslation()) * 4 + I8NUtil
				.qmTranslation(languages, Chart.getDescStyle()
						.getLabelTranslation()) * 2) / 6;
		return result;
	}

	/**
	 * Caches the {@link DpEntryType} of this layer when queried by
	 * {@link #getType()}
	 */
	private DpEntryType type;

	/**
	 * Set the type of {@link DpEntryType}.
	 * 
	 * @param type
	 */
	public final void setType(DpEntryType type) {
		this.type = type;
	}

	/**
	 * Has to be implemented by the children of {@link CopyOfDpEntry}. Icons are
	 * generated upon this information. Never returns <code>null</code>.
	 * 
	 * @return A constant from {@link DpEntryType}.
	 */
	public final DpEntryType getType() {
		if (type == null)
			return DpEntryType.UNKNOWN;
		return type;
	}

	/**
	 * This default implementation returns the JVM's default charset determined
	 * by: {@link Charset#defaultCharset()}. The method may be overwritten by
	 * subclasses of {@link CopyOfDpEntry}.
	 */
	public Charset getCharset() {
		if (charset == null) {
			charset = Charset.defaultCharset();
		}
		return charset;
	}

	/**
	 * Allows to define a special charset.
	 * 
	 * @param newCharset
	 *            if <code>null</code> the default charset will be used
	 */
	public void setCharset(Charset newCharset) {

		if (charset == newCharset)
			return;

		if (charset != null && charset.compareTo(newCharset) == 0)
			return;

		// /** A new charset has been set for this layer. Uncache ! */
		// uncache();
		charset = newCharset;
	}

	/**
	 * @deprecated attention, do you want to use the AVSwingUtil.getUrl...
	 *             method?
	 */
	@Deprecated
	public URL getUrl() {
		if (url == null) {

			if (JNLPUtil.isAtlasDataFromJWS(getAtlasConfig())) {
				JNLPSwingUtil.loadPartAndCreateDialogForIt(getId());
			}

			String location = getAtlasConfig().getResouceBasename()
					+ getDataDirname() + "/" + getFilename();
			url = getAtlasConfig().getResource(location);
			// Testing if we really can see it in the resources now...
			try {
				InputStream openStream = url.openStream();
				openStream.close();
			} catch (Exception e) {
				setBrokenException(new AtlasException(
						"Trying to open and close a stream to URL " + url
								+ " failed: ", e));
				url = null;
				return null;
			}
		}
		return url;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DpEntry) {
			return ((DpEntry<?>) obj).getId().equals(id);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {

		int sum = 123;
		for (int i = 0; i < id.length(); i++) {
			sum += id.codePointAt(i);
		}

		return super.hashCode() * sum;
	}

	public void setLocalTempFile(File localTempFile) {
		this.localTempFile = localTempFile;
	}

	public File getLocalTempFile() {
		return localTempFile;
	}

}
