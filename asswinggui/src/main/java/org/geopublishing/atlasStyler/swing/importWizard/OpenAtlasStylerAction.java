package org.geopublishing.atlasStyler.swing.importWizard;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.geopublishing.atlasStyler.swing.AtlasStylerGUI;
import org.geopublishing.atlasViewer.GpCoreUtil;
import org.geopublishing.atlasViewer.swing.AVDialogManager;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geopublishing.atlasViewer.swing.MapLegend;
import org.geotools.map.MapLayer;

import de.schmitzm.geotools.styling.StyledLayerInterface;

public class OpenAtlasStylerAction extends AbstractAction {

	private final AtlasStylerGUI asg;
	private final StyledLayerInterface<?> styledLayer;

	public OpenAtlasStylerAction(final AtlasStylerGUI asg,
			StyledLayerInterface<?> styledLayer) {
		super(GpCoreUtil.R("LayerToolMenu.style"), Icons.ICON_STYLE);
		this.asg = asg;
		this.styledLayer = styledLayer;

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// final StyledLayerInterface<?> styledLayer =
		// (StyledLayerInterface<?>) constArgs[0];
		// final MapLayer mapLayer = (MapLayer) constArgs[1];
		// final MapLegend mapLegend = (MapLegend) constArgs[2];

		MapLegend mapLegend = asg.getStylerMapView().getLayerManager();
		MapLayer mapLayer = mapLegend.getMapLayerFor(styledLayer.getId());
		AVDialogManager.dm_Styler.getInstanceFor(styledLayer, asg, styledLayer,
				mapLayer, mapLegend);
	}
}
