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
package org.geopublishing.atlasViewer.dp.layer;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.dp.AMLImport;
import org.geopublishing.atlasViewer.swing.AVDialogManager;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;
import org.geopublishing.atlasViewer.swing.MapLegend;
import org.geopublishing.atlasViewer.swing.internal.AtlasExportTask;
import org.geotools.data.DataAccess;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.NameImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import schmitzm.geotools.feature.CQLFilterParser;
import schmitzm.geotools.io.GeoImportUtil;
import schmitzm.geotools.io.GeoImportUtil.SHP_POSTFIXES;
import schmitzm.io.IOUtil;
import schmitzm.jfree.feature.style.FeatureChartStyle;
import skrueger.AttributeMetadataImpl;
import skrueger.geotools.AttributeMetadataImplMap;
import skrueger.geotools.AttributeMetadataMap;
import skrueger.geotools.StyledFeatureSourceInterface;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.geotools.StyledLayerUtil;
import skrueger.i8n.Translation;

public abstract class DpLayerVectorFeatureSource
		extends
		DpLayerVector<FeatureSource<SimpleFeatureType, SimpleFeature>, FeatureChartStyle>
		implements StyledFeatureSourceInterface {

	static private final Logger LOGGER = Logger
			.getLogger(DpLayerVectorFeatureSource.class);

	/**
	 * The {@link Name} of the {@link SimpleFeatureType} of the
	 * {@link DataStore} that is accessed.
	 */
	private Name typeName;

	/**
	 * Has the attribute meta-data been checked against the schema yet? this
	 * must be done to ensure that add/deleted columns are correctly presented.
	 */
	private boolean attribMetadataChecked = false;

	/**
	 * Returns the Name of the {@link SimpleFeatureType} of the
	 * {@link DataStore} that is accessed.
	 */
	public Name getTypeName() {
		return typeName;
	}

	/**
	 * The cached {@link FeatureSource} where the {@link SimpleFeature}s come
	 * from.
	 */
	protected FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;

	/**
	 * The cached {@link DataStore} where the {@link #featureSource} comes from.
	 */
	protected DataAccess<SimpleFeatureType, SimpleFeature> dataStore;

	private Filter filter = Filter.INCLUDE;

	private SimpleFeatureType schema;

	/**
	 * The constructor used when loading a {@link DpLayerVectorFeatureSource}
	 * description in {@link AMLImport}
	 * 
	 * @param ac
	 */
	public DpLayerVectorFeatureSource(AtlasConfig ac) {
		super(ac);
		attributeMetaDataMap = new AttributeMetadataImplMap(ac.getLanguages());
	}

	/**
	 * Releases the {@link FeatureSource}.
	 */
	@Override
	public void uncache() {
		super.uncache();

		attribMetadataChecked = false;

		schema = null;

		/** Close any open attribute table for this layer */
		AVDialogManager.dm_AttributeTable.disposeInstanceFor(this);

		/** Close any open atlas styler for this layer */
		AVDialogManager.dm_AtlasStyler.disposeInstanceFor(this);

		/** Close any open styler for this layer */
		AVDialogManager.dm_Styler.disposeInstanceFor(this);

		if (featureSource != null) {
			featureSource = null;
		}

		if (dataStore != null) {

			/**
			 * java.util.ConcurrentModificationException at
			 * java.util.AbstractList$Itr
			 * .checkForComodification(AbstractList.java:372) at
			 * java.util.AbstractList$Itr.next(AbstractList.java:343) at
			 * org.geotools
			 * .data.shapefile.ShpFiles.logCurrentLockers(ShpFiles.java:234) at
			 * org.geotools.data.shapefile.ShpFiles.dispose(ShpFiles.java:221)
			 * at org.geotools.data.shapefile.ShapefileDataStore.dispose(
			 * ShapefileDataStore.java:1075) at
			 * skrueger.atlas.dp.layer.DpLayerVectorFeatureSource
			 * .uncache(DpLayerVectorFeatureSource.java:127) at
			 * skrueger.atlas.map.Map.uncache(Map.java:218) at
			 * skrueger.creator.AtlasConfigEditable
			 * .uncache(AtlasConfigEditable.java:947) at
			 * skrueger.creator.UncacheAtlasAction$1
			 * .doInBackground(UncacheAtlasAction.java:71) at
			 * javax.swing.SwingWorker$1.call(SwingWorker.java:278) at
			 * java.util.
			 * concurrent.FutureTask$Sync.innerRun(FutureTask.java:303) at
			 * java.util.concurrent.FutureTask.run(FutureTask.java:138) at
			 * javax.swing.SwingWorker.run(SwingWorker.java:317)
			 * 
			 * But what can i do against it?
			 */
			dataStore.dispose();
			dataStore = null;
		}
	}


	@Override
	public GeometryDescriptor getDefaultGeometry() {
		return getGeoObject().getSchema().getGeometryDescriptor();
	}

	/**
	 * If crs == null, it will first try to parse a .prj file. If it doesn't
	 * exist, it calls {@link #getGeoObject()} and hopes that the crs will be
	 * set there
	 * 
	 * This is overwritten by DpLayerFeatureSource, because e.g. a WFS doens't
	 * not have a .prj file
	 * 
	 * @return <code>null</code> if not working
	 */
	@Override
	public final CoordinateReferenceSystem getCrs() {
		if (crs == null && !isBroken()) {
			crs = super.getCrs();

			// We have to call get getGeoObject to determine the type: line,
			// string, poly..
			getGeoObject();

			if (crs == null && !isBroken()) {
				/**
				 * Trying to reread the CRS by accessing the GeoObject
				 */
				try {
					crs = getGeoObject().getSchema().getGeometryDescriptor()
							.getCoordinateReferenceSystem();
				} catch (Exception ex) {
					setBrokenException(ex);
					return null;
				}
			}
		}
		return crs;
	}

	/**
	 * Returns the features of this {@link FeatureSource}. The {@link Filter}
	 * associated with the {@link StyledFeaturesInterface} is automatically
	 * applied.
	 */
	@Override
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollectionFiltered() {
		FeatureCollection<SimpleFeatureType, SimpleFeature> features;
		try {
			features = getGeoObject().getFeatures(getFilter());
		} catch (IOException e) {
			throw new RuntimeException(
					"Error getting the features of the  FeatureSource");
		}
		return features;
	}

	/**
	 * Returns the features of this {@link FeatureSource}. The {@link Filter}
	 * associated with the {@link StyledFeaturesInterface} is not automatically
	 * applied.
	 * 
	 * @see {@link StyledFeaturesInterface}
	 * @see #getFeatureCollectionFiltered()
	 */
	@Override
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollection() {
		FeatureCollection<SimpleFeatureType, SimpleFeature> features;
		try {
			features = getGeoObject().getFeatures();
		} catch (IOException e) {
			throw new RuntimeException(
					"Error getting the features of the  FeatureSource");
		}
		return features;
	}

	/**
	 * Same as {@link #getGeoObject()} method, but complies to the
	 * {@link StyledFeaturesInterface}
	 * 
	 * @see {@link StyledFeaturesInterface}
	 */
	@Override
	public FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource() {
		return getGeoObject();
	}

	/**
	 * Opens a ChartFrame. The selection buttons of the corresponding
	 * {@link MapLegend} are updated if not <code>null</code>.
	 * 
	 * @param chartID
	 *            The ID/Filename of the chart to display
	 * @param mapLegend
	 *            May be <code>null</code> if the chart is just opened without a
	 *            connection to a {@link JSMapPane}
	 */
	public void openChart(Component owner, String chartID,
			final MapLegend mapLegend) {

		FeatureChartStyle chart = getChartForID(chartID);

		AVDialogManager.dm_Charts.getInstanceFor(chart, owner, chart,
				mapLegend, this);

		// TODO How does a filter change affect the chart??
		// final MapLayerAdapter mapLayerChangedListener = new MapLayerAdapter()
		// {
		//
		// @Override
		// public void layerChanged(MapLayerEvent arg0) {
		//
		// if (arg0.getReason() == MapLayerEvent.FILTER_CHANGED) {
		//					
		//					
		// // TODO compare to old filter to see if anything has
		// // chnaged
		// // openTableDialog.getModel().getFilter().c
		//					
		// StyledLayerSelectionModel<?> styledLayerSelectionModel =
		// layerPanel.rememberSelection
		// .get(arg0.getSource());
		// if (styledLayerSelectionModel != null) {
		// // If there exists a SelectionModel for this MapLayer,
		// // we have two options: Clear all selections, or
		// // unselect the excluded elements.. We do the first
		// // here.
		// styledLayerSelectionModel.setValueIsAdjusting(true);
		// styledLayerSelectionModel.clearSelection();
		//
		// }
		// chartDialog.getModel().setFilter(
		// mapLayer.getQuery().getFilter());
		//
		// if (styledLayerSelectionModel != null) {
		// styledLayerSelectionModel.setValueIsAdjusting(false);
		// }
		// }
		// }
		// };
		//		
		// mapLayer.addMapLayerListener(mapLayerChangedListener);

	}

	public void setTypeName(Name typeName) {
		this.typeName = typeName;
	}

	@Override
	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	@Override
	public Filter getFilter() {
		return filter;
	}

	public void setFilterRule(String ruleString) {
		filter = new CQLFilterParser().parseFilter(ruleString);
	}

	/**
	 * Returns a cached schema...
	 */
	@Override
	public SimpleFeatureType getSchema() {
		if (schema == null && !isBroken()) {
			try {
				schema = getFeatureSource().getSchema();
			} catch (Exception e) {
				setBrokenException(e);
				return null;
			}
		}
		return schema;
	}

	@Override
	public DpLayer<FeatureSource<SimpleFeatureType, SimpleFeature>, FeatureChartStyle> copyTo(
			DpLayer<FeatureSource<SimpleFeatureType, SimpleFeature>, FeatureChartStyle> target) {
		DpLayerVectorFeatureSource copy = (DpLayerVectorFeatureSource) super
				.copyTo(target);

		// DuplicatingFilterVisitor dfv = new DuplicatingFilterVisitor();

		copy.setFilter(getFilter());

		copy.setTypeName(getTypeName());

		return copy;
	}

	/**
	 * @return A value between 0 and 1 which describes how good much metadata
	 *         has been provided. 1 is great. This method uses
	 *         super.getQuality() and "adds" the quality of the column
	 *         translations.
	 */
	@Override
	public Double getQuality() {
		if (isBroken())
			return 0.;
		Double result;

		final Double layerQM = super.getQuality();

		result = (layerQM * 4. + attributeMetaDataMap.getQuality(getAtlasConfig()
				.getLanguages()) * 1.) / 5.;

		return result;
	}

	/**
	 * A {@link Map} that holds the {@link AttributeMetadataImpl}, e.g.
	 * {@link Translation}s and unit
	 */
	final AttributeMetadataMap attributeMetaDataMap;

	/**
	 * @return a {@link Map} with {@link Name} -> {@link AttributeMetadataImpl}. 
	 */
	public AttributeMetadataMap<AttributeMetadataImpl> getAttributeMetaDataMap() {
		if (!attribMetadataChecked && !AtlasViewerGUI.isRunning()) {
			/**
			 * checkAttribMetaData requires all Geoobjects to be available.
			 * Exported atlases have more information in the atlas.xml than they
			 * need. So we may only do this when we are running from
			 * Geopublisher (!AtlasViewer.isRunning)
			 */
			try {
				StyledLayerUtil.checkAttribMetaData(attributeMetaDataMap, getSchema());
				attribMetadataChecked = true;
			} catch (Exception e) {
				String msg = "Error while validating the described attributes against the \"physical\" schema of the datafile "
					+ getFilename()
					+ "\n. Will continue without validation.";
				LOGGER.error(msg,e);
				
//				
//				ExceptionDialog
//						.show(
//								AtlasCreator.getInstance().getJFrame(),
//								new AtlasException(
//										"Error while validating the described attributes against the \"physical\" schema of the datafile "
//												+ getFilename()
//												+ "\n. Will continue without validation.",
//										e));
			}
		}

		// I don't know how "the_geom" is getting into it again and again
		if (attributeMetaDataMap.containsKey(new NameImpl("the_geom"))){
			attributeMetaDataMap.remove(new NameImpl("the_geom"));
		}
		
		return attributeMetaDataMap;
	}


	@Override
	public void exportWithGUI(Component owner) throws IOException {
		AtlasExportTask exportTask = new AtlasExportTask(owner, getTitle()
				.toString()) {

			@Override
			protected Boolean doInBackground() throws Exception {

				setPrefix("Exporting ");

				try {
					exportDir = AVSwingUtil.selectExportDir(owner, getAtlasConfig());

					if (exportDir == null) {
						return false;
					}

					URL url = AVSwingUtil.getUrl(DpLayerVectorFeatureSource.this, owner);
					final File file = new File(exportDir, getFilename());

					// ****************************************************************************
					// Copy main file and possibly throw an Exception
					// ****************************************************************************
					publish(file.getAbsolutePath());
					FileUtils.copyURLToFile(AVSwingUtil.getUrl(DpLayerVectorFeatureSource.this, owner), file);

					// Try to copy any pending world files...
					for (SHP_POSTFIXES pf : GeoImportUtil.SHP_POSTFIXES
							.values()) {
						final File changeFileExt = IOUtil.changeFileExt(file,
								pf.toString());
						publish(changeFileExt.getAbsolutePath());
						AtlasConfig.exportURLtoFileNoEx(IOUtil.changeUrlExt(
								url, pf.toString()), changeFileExt);
					}

					AtlasConfig.exportURLtoFileNoEx(IOUtil.changeUrlExt(url,
							"prj"), IOUtil.changeFileExt(file, "prj"));

					AtlasConfig.exportURLtoFileNoEx(IOUtil.changeUrlExt(url,
							"sld"), IOUtil.changeFileExt(file, "sld"));

					AtlasConfig.exportURLtoFileNoEx(IOUtil.changeUrlExt(url,
							"shp.xml"), IOUtil.changeFileExt(file, "shp.xml"));

					publish("done");

					success = true;
				} catch (Exception e) {
					done();
				}
				return success;
			}

		};

		exportTask.execute();
	}

}
