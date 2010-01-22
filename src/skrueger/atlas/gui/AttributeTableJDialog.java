/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasViewer application - A GIS viewer application targeting at end-users with no GIS-experience. Its main purpose is to present the atlases created with the Geopublisher application.
 * http://www.geopublishing.org
 * 
 * AtlasViewer is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.atlas.gui;

import java.awt.Component;
import java.beans.PropertyChangeListener;

import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerEvent;

import schmitzm.geotools.feature.AttributeTypeFilter;
import schmitzm.geotools.gui.FeatureTablePane;
import schmitzm.geotools.gui.SelectableFeatureTablePane;
import schmitzm.geotools.gui.SelectableXMapPane;
import schmitzm.geotools.map.event.MapLayerAdapter;
import schmitzm.swing.SortableJTable;
import schmitzm.swing.SwingUtil;
import skrueger.atlas.AVUtil;
import skrueger.atlas.AtlasViewer;
import skrueger.geotools.StyledFeatureCollectionTableModel;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.geotools.StyledLayerInterface;
import skrueger.geotools.selection.StyledFeatureLayerSelectionModel;
import skrueger.geotools.selection.StyledLayerSelectionModel;
import skrueger.geotools.selection.StyledLayerSelectionModelSynchronizer;
import skrueger.geotools.selection.TableSelectionSynchronizer;
import skrueger.swing.AtlasDialog;

/**
 * A dialog to show the attribute table of a vector layer. This class implements
 * a {@link PropertyChangeListener} which is connected to the
 * {@link StyledLayerInterface StyledLayerInterface's}
 * {@link StyledLayerSelectionModel} to keep the table selection synchronized to
 * other component's selection (e.g. Map or chart).
 */
public class AttributeTableJDialog extends AtlasDialog {
	static final Logger LOGGER = Logger.getLogger(AttributeTableJDialog.class);
	
	/** If a table will contain more than that many cells, the user will be warned **/
	private static final int WARN_CELLS = 15000;
	//
	// /** A cache that manages maximum one instance of this class per layer **/
	// private static HashMap<String, AttributeTableJDialog> dialogCache = new
	// HashMap<String, AttributeTableJDialog>();

	private StyledFeatureCollectionTableModel model;

	/** Holds the table and preview of the dialog. */
	private FeatureTablePane featureTablePane;

	/**
	 * The external maplayer that shows the features. May be <code>null</code>
	 */
	protected final MapLayer mapLayer;
	private final MapLegend mapLegend;
	private StyledLayerSelectionModelSynchronizer synchronizer;
	private StyledFeatureLayerSelectionModel selectionModel;
	private MapLayerAdapter mapLayerChangedListener;
	private final Component owner;
	private final StyledFeaturesInterface<?> styledObj;

	/**
	 * AtlasViewer.R("AttributeTable.dialog.title", styledObj .getTitle())
	 * 
	 * @param owner
	 *            Parent component
	 * @param mapLayer
	 *            may be <code>null</code>
	 * @param mapLegend
	 *            may be <code>null</code>
	 */
	public AttributeTableJDialog(Component owner,
			final StyledFeaturesInterface<?> styledObj,
			final MapLegend mapLegend) {
		super(owner, AtlasViewer.R("AttributeTable.dialog.title", styledObj
				.getTitle()));
		this.owner = owner;
		this.styledObj = styledObj;
		this.mapLegend = mapLegend;

		this.mapLayer = mapLegend == null ? null : mapLegend
				.getMapLayerFor(styledObj.getId());

		// make a check on howmany feateurs we have an print a warning if too
		// many
		int numCells = styledObj.getFeatureCollectionFiltered().size()
				* styledObj.getAttributeMetaDataMap().sortedValuesVisibleOnly()
						.size();
		if (numCells > WARN_CELLS) {
			if (AVUtil.askYesNo(owner, AtlasViewer.R(
					"AttributeTable.dialog.warnToManyCells", numCells)) == false){
				dispose();
				return;
			}
		}

		// SK: Sadly has no effect :-( .. Only for JFrames?
		// setIconImage(AtlasTaskPaneUI.ICON_TABLE.getImage());

		getModel().setAttributeFilter(AttributeTypeFilter.NO_GEOMETRY);

		if (mapLegend != null) {

			/**
			 * Add Synchronizer for selection id mapLegend != null. Will be
			 * removed in close()
			 */
			StyledLayerSelectionModel<?> anySelectionModel = mapLegend
					.getRememberSelection(mapLayer.getTitle());

			if (anySelectionModel instanceof StyledFeatureLayerSelectionModel) {
				selectionModel = (StyledFeatureLayerSelectionModel) anySelectionModel;

				// create a synchronizer to keep the feature table selection
				// synchronized with the other components connected to the
				// DpLayerSelectionModel

				final SortableJTable table = getFeatureTablePane().getTable();
				synchronizer = (StyledLayerSelectionModelSynchronizer<?>) new TableSelectionSynchronizer(
						selectionModel, table);

				selectionModel.addSelectionListener(synchronizer);

				table.getSelectionModel().addListSelectionListener(
						(ListSelectionListener) synchronizer);

				selectionModel.refreshSelection();
			}

			/**
			 * Listen for FILTER changes of the target layer and update the
			 * table accordingly
			 */
			mapLayerChangedListener = new MapLayerAdapter() {

				@Override
				public void layerChanged(MapLayerEvent arg0) {

					if (arg0.getReason() == MapLayerEvent.FILTER_CHANGED) {
						// TODO compare to old filter to see if anything has

						final StyledLayerSelectionModel<?> styledLayerSelectionModel = mapLegend.rememberSelectionModel
								.get(arg0.getSource());
						if (styledLayerSelectionModel != null) {
							// If there exists a SelectionModel for this
							// MapLayer,
							// we have two options: Clear all selections, or
							// unselect the excluded elements.. We do the first
							// here.
							styledLayerSelectionModel.setValueIsAdjusting(true);
							styledLayerSelectionModel.clearSelection();

						}

						// we can cast here, because we checked above
						getModel().setStyledFeatures(
								(StyledFeaturesInterface<?>) styledObj);

						if (styledLayerSelectionModel != null) {
							styledLayerSelectionModel
									.setValueIsAdjusting(false);
						}
					}
				}
			};

			mapLayer.addMapLayerListener(mapLayerChangedListener);
		} // (mapLegend != null)

		initialize();
	}

	@Override
	public boolean close() {

		/**
		 * Remove Synchronizer for selection id mapLegend != null
		 */
		if (selectionModel != null && synchronizer != null) {
			selectionModel.removeSelectionListener(synchronizer);
			getFeatureTablePane().getTable().getSelectionModel()
					.removeListSelectionListener(
							(ListSelectionListener) synchronizer);
		}

		if (mapLegend != null) {
			if (mapLayerChangedListener != null)
				mapLayer.removeMapLayerListener(mapLayerChangedListener);
		}

		return super.close();
	}

	private void initialize() {

		setContentPane(getFeatureTablePane());

		pack();

		SwingUtil.setRelativeFramePosition(this, owner, SwingUtil.BOUNDS_OUTER,
				SwingUtil.EAST);

	}

	private FeatureTablePane getFeatureTablePane() {
		if (featureTablePane == null) {
			// The attribute table can also be used without any
			// layerPanel or MapPane just from the DatapoolJTable
			SelectableXMapPane mapPane = mapLegend == null ? null : mapLegend
					.getGeoMapPane() != null ? mapLegend.getGeoMapPane()
					.getMapPane() : null;

			featureTablePane = new SelectableFeatureTablePane(getModel(), true,
					mapPane);

		}
		return featureTablePane;
	}

	public StyledFeatureCollectionTableModel getModel() {
		if (model == null) {
			model = new StyledFeatureCollectionTableModel(styledObj);
		}
		return model;
	}
	
	@Override
	public void dispose() {
		if (isDisposed) return;
		super.dispose();
	}

}
