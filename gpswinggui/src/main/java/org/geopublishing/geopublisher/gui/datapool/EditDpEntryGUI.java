/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui.datapool;

import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.layer.DpLayerRaster;
import org.geopublishing.atlasViewer.dp.layer.DpLayerRaster_Reader;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.dp.media.DpMedia;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import de.schmitzm.i18n.Translation;
import de.schmitzm.jfree.chart.style.ChartStyle;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.CancellableTabbedDialogAdapter;
import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.SwingUtil;
import de.schmitzm.swing.TranslationEditJPanel;
import de.schmitzm.swing.TranslationsAskJPanel;

/**
 * Allows to edit all {@link Translation}s of this {@link DpEntry} and some more
 * depending on the parameters depending on the type of {@link DpEntry}.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
 * 
 */
public class EditDpEntryGUI extends CancellableTabbedDialogAdapter {

    private final DpEntry<? extends ChartStyle> dpe;

    public EditDpEntryGUI(final Component owner,
	    final DpEntry<? extends ChartStyle> dpe) {
	super(owner);
	this.dpe = dpe;

	setTitle(dpe.getTitle().toString());
	final ArrayList<Image> icons = new ArrayList<Image>();
	icons.add(dpe.getType().getIconBig().getImage());
	icons.add(dpe.getType().getIconSmall().getImage());
	setIconImages(icons);

	/**
	 * Prepare buttons
	 */
	final JPanel buttons = createButtons();

	/*** Build GUI ***/
	{
	    JTabbedPane tabbedPane = getTabbedPane();

	    /** A tab for name, desc and keywords... **/
	    tabbedPane.insertTab(
		    GeopublisherGUI.R("EditDpEntryGUI.labels.tab"), null,
		    createTranslationsTab(dpe), null, tabbedPane.getTabCount());

	    /** A tab with general settings **/
	    tabbedPane.insertTab(
		    GeopublisherGUI.R("EditDpEntryGUI.general.tab"), null,
		    new DpEntryJPanel(dpe), null, tabbedPane.getTabCount());

	    /** A tab with general settings **/
	    tabbedPane.insertTab(GeopublisherGUI.R("EditDpEntryGUI.usage.tab"),
		    null, new DpEntryUsageJPanel(dpe), null,
		    tabbedPane.getTabCount());

	    /** A tab with hints how to link to this DPE from HTML **/
	    if (dpe instanceof DpMedia) {
		tabbedPane.insertTab(
			GeopublisherGUI.R("EditDpEntryGUI.link.tab"), // i8n
			null, new DpEntryLinkJPanel(dpe), null,
			tabbedPane.getTabCount());
	    }

	    /** A tab with DpEntryType specific stuff **/
	    if (1 == 2) {

		// Arthur meint ds braucht man nicht ;-)

		Component typeSpecific = null;
		if (dpe instanceof DpLayerRaster)
		    typeSpecific = createRasterTab((DpLayerRaster) dpe);
		// else if (dpe instanceof DpLayerRasterPyramid)
		// typeSpecific = new DpLayerRasterPyramidJPanel(
		// (DpLayerRasterPyramid) dpe);

		if (typeSpecific != null)
		    tabbedPane.insertTab(dpe.getType().getLine2(), null,
			    typeSpecific, dpe.getType().getDesc(),
			    tabbedPane.getTabCount());
	    }

	    /** A tab with Table related settings **/
	    if (dpe instanceof DpLayerVectorFeatureSource) {
		tabbedPane.insertTab(GeopublisherGUI
			.R("EditDpEntryGUI.attributes.tab"), null,
			new DpLayerVectorAttributesJPanel(
				(DpLayerVectorFeatureSource) dpe), null,
			tabbedPane.getTabCount());
	    }

	    /**
	     * Building the content pane
	     */
	    final JPanel contentPane = new JPanel(new MigLayout("wrap 1"));
	    contentPane.add(tabbedPane);
	    contentPane.add(buttons);

	    setContentPane(contentPane);
	    pack();
	    SwingUtil.setRelativeFramePosition(this, owner, .5, .5);
	}

    }

    private JPanel createRasterTab(final DpLayerRaster dpRaster) {
	final JPanel rasterTab = new JPanel(new MigLayout());
	JPanel noDataValuesPanel = new JPanel(new MigLayout("width 100%"));
	noDataValuesPanel.setBorder(BorderFactory
		.createTitledBorder(GeopublisherGUI
			.R("EditDpEntryGUI.raster.nodata.border")));

	noDataValuesPanel.add(
		new JLabel(GeopublisherGUI
			.R("EditDpEntryGUI.raster.nodata.explanation")),
		"span 2");

	final JPanel noDataValuePanel = new JPanel(new MigLayout());
	// MS-01.sc: DpLayerRaster modified to use a GridReader instead of
	// GridCoverage2D
	// --> no data values can not be resolved like from GridCoverage2D
	// --> code temporary removed
	// final GridCoverage2D geoObject = dpRaster.getGeoObject();
	// final double[] noDataValues = geoObject.getSampleDimension(0)
	// .getNoDataValues();
	final double[] noDataValues = null;
	// MS-01.ec

	noDataValuePanel.add(new JLabel(GeopublisherGUI
		.R("EditDpEntryGUI.raster.nodata.label") + noDataValues));
	rasterTab.add(noDataValuePanel, "growx");

	return rasterTab;
    }

    /** A tab for name, desc and keywords... **/
    public static TranslationsAskJPanel createTranslationsTab(
	    final DpEntry<? extends ChartStyle> dpe) {
	TranslationsAskJPanel dpeTranslationTab;
	{
	    final List<String> langs = dpe.getAtlasConfig().getLanguages();
	    if (dpe.getTitle() == null)
		dpe.setTitle(new Translation(langs, "untitled"));
	    if (dpe.getDesc() == null)
		dpe.setDesc(new Translation());
	    if (dpe.getKeywords() == null)
		dpe.setKeywords(new Translation());

	    final TranslationEditJPanel a = new TranslationEditJPanel(
		    GeopublisherGUI.R("EditDPEDialog.TranslateTitle"),
		    dpe.getTitle(), langs);
	    final TranslationEditJPanel b = new TranslationEditJPanel(
		    GeopublisherGUI.R("EditDPEDialog.TranslateDescription"),
		    dpe.getDesc(), langs);
	    final TranslationEditJPanel c = new TranslationEditJPanel(
		    GeopublisherGUI.R("EditDPEDialog.TranslateKeywords"),
		    dpe.getKeywords(), langs);
	    TranslationEditJPanel[] translationPanels = { a, b, c };

	    if (dpe instanceof DpLayerRaster) {
		DpLayerRaster_Reader dplrr = (DpLayerRaster_Reader) dpe;
		if (dplrr.getBandCount() >1 ) {
		    int i = 1;
		    for (Translation t : dplrr.getBandNames()) {
			translationPanels = LangUtil.extendArray(
				translationPanels,
				new TranslationEditJPanel(GeopublisherGUI.R(
					"EditDPEDialog.TranslateBands", i), t,
					langs));
			i++;
		    }
		}
	    }
	    dpeTranslationTab = new TranslationsAskJPanel(translationPanels);

	}
	return dpeTranslationTab;
    }

    @Override
    public boolean okClose() {
	if (super.okClose()) {

	    /**
	     * Inform the DataPool about the changes
	     */
	    dpe.getAtlasConfig()
		    .getDataPool()
		    .fireChangeEvents(
			    org.geopublishing.atlasViewer.dp.DataPool.EventTypes.changeDpe);

	    return true;
	}
	return false;
    }

}
