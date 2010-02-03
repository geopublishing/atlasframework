/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
 * http://www.geopublishing.org
 * 
 * Geopublisher is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.creator.gui.datapool;

import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;

import org.geotools.coverage.grid.GridCoverage2D;

import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.DataPool.EventTypes;
import skrueger.atlas.dp.layer.DpLayerRaster;
import skrueger.atlas.dp.layer.DpLayerRasterPyramid;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.creator.AtlasCreator;
import skrueger.i8n.Translation;
import skrueger.swing.CancellableTabbedDialogAdapter;
import skrueger.swing.TranslationEditJPanel;
import skrueger.swing.TranslationsAskJPanel;

/**
 * Allows to edit all {@link Translation}s of this {@link DpEntry} and some more
 * depending on the parameters depending on the type of {@link DpEntry}.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
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
			tabbedPane.insertTab(AtlasCreator.R("EditDpEntryGUI.labels.tab"),
					null, createTranslationsTab(dpe), null, tabbedPane
							.getTabCount());

			/** A tab with general settings **/
			tabbedPane.insertTab(AtlasCreator.R("EditDpEntryGUI.general.tab"),
					null, new DpEntryJPanel(dpe), null, tabbedPane
							.getTabCount());

			/** A tab with DpEntryType specific stuff **/
			{
				Component typeSpecific = null;
				if (dpe instanceof DpLayerRaster)
					typeSpecific = createRasterTab((DpLayerRaster) dpe);
				else if (dpe instanceof DpLayerRasterPyramid)
					typeSpecific = new DpLayerRasterPyramidJPanel(
							(DpLayerRasterPyramid) dpe);

				if (typeSpecific != null)
					tabbedPane.insertTab(dpe.getType().getLine2(), null,
							typeSpecific, dpe.getType().getDesc(), tabbedPane
									.getTabCount());
			}

			/** A tab with Table related settings **/
			if (dpe instanceof DpLayerVectorFeatureSource) {
				tabbedPane.insertTab(AtlasCreator
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
			SwingUtil.setRelativeFramePosition(this, owner, .5,.5);
		}

	}


	private JPanel createRasterTab(final DpLayerRaster dpRaster) {
		final JPanel rasterTab = new JPanel(new MigLayout());

		{

			JPanel noDataValuesPanel = new JPanel(new MigLayout("width 100%"));
			noDataValuesPanel.setBorder(BorderFactory
					.createTitledBorder(AtlasCreator
							.R("EditDpEntryGUI.raster.nodata.border")));

			noDataValuesPanel.add(new JLabel(AtlasCreator
					.R("EditDpEntryGUI.raster.nodata.explanation")), "span 2");

			final JPanel noDataValuePanel = new JPanel(new MigLayout());
			final GridCoverage2D geoObject = dpRaster.getGeoObject();
			final double[] noDataValues = geoObject.getSampleDimension(0)
					.getNoDataValues();

			noDataValuePanel.add(new JLabel(AtlasCreator
					.R("EditDpEntryGUI.raster.nodata.label")
					+ noDataValues));
			rasterTab.add(noDataValuePanel, "growx");
		}

		return rasterTab;
	}

	/** A tab for name, desc and keywords... **/
	public static TranslationsAskJPanel createTranslationsTab(
			final DpEntry<? extends ChartStyle> dpe) {
		TranslationsAskJPanel dpeTranslationTab;
		{
			final List<String> langs = dpe.getAc().getLanguages();
			if (dpe.getTitle() == null)
				dpe.setTitle(new Translation(langs, "untitled"));
			if (dpe.getDesc() == null)
				dpe.setDesc(new Translation());
			if (dpe.getKeywords() == null)
				dpe.setKeywords(new Translation());

			final TranslationEditJPanel a = new TranslationEditJPanel(
					AtlasCreator.R("EditDPEDialog.TranslateTitle"), dpe
							.getTitle(), langs);
			final TranslationEditJPanel b = new TranslationEditJPanel(
					AtlasCreator.R("EditDPEDialog.TranslateDescription"), dpe
							.getDesc(), langs);
			final TranslationEditJPanel c = new TranslationEditJPanel(
					AtlasCreator.R("EditDPEDialog.TranslateKeywords"), dpe
							.getKeywords(), langs);

			dpeTranslationTab = new TranslationsAskJPanel(a, b, c);
		}
		return dpeTranslationTab;
	}

	@Override
	public boolean okClose() {
		if (super.okClose()) {

			/**
			 * Inform the DataPool about the changes
			 */
			dpe.getAc().getDataPool().fireChangeEvents(EventTypes.changeDpe);

			return true;
		}
		return false;
	}

}