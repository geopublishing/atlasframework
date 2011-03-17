package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.awt.image.BufferedImage;

import org.geopublishing.atlasStyler.AtlasStylerRaster;
import org.geopublishing.atlasStyler.classification.RasterClassification;

import de.schmitzm.swing.JPanel;

public class RasterClassificationGUI extends ClassificationGUI {

	public RasterClassificationGUI(Component owner,
			RasterClassification classifier, AtlasStylerRaster atlasStyler,
			String title) {
		super(owner, classifier, atlasStyler, title);
	}

	protected AtlasStylerRaster getASR() {
		return (AtlasStylerRaster) atlasStyler;
	}

	@Override
	protected BufferedImage getHistogramImage() {
		return ERROR_IMAGE;
	}

	@Override
	protected JPanel getJPanelData() {
		return new JPanel();
	}

}
