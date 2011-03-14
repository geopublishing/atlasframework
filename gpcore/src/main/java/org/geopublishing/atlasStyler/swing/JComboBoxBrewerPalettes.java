package org.geopublishing.atlasStyler.swing;

import java.io.IOException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.apache.log4j.Logger;
import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.ColorBrewer;
import org.geotools.brewer.color.PaletteType;

import de.schmitzm.lang.LangUtil;

public class JComboBoxBrewerPalettes extends JComboBox {

	final static private Logger LOGGER = Logger
			.getLogger(JComboBoxBrewerPalettes.class);

	public JComboBoxBrewerPalettes(boolean disinct) {

		BrewerPalette[] palettes = new BrewerPalette[] {};
		// This code only the paletes usefull for unique values.
		final PaletteType paletteTypeUnique = new PaletteType(false, true);
		final PaletteType paletteTypeRanged = new PaletteType(true, false);
		try {
			ColorBrewer brewer1, brewer2;
			brewer1 = ColorBrewer.instance(paletteTypeUnique);
			brewer2 = ColorBrewer.instance(paletteTypeRanged);
			BrewerPalette[] palettes1 = brewer1.getPalettes(paletteTypeUnique);
			BrewerPalette[] palettes2 = brewer2.getPalettes(paletteTypeRanged);

			if (!disinct) {
				palettes = LangUtil.extendArray(palettes1, palettes2);
			} else
				palettes = LangUtil.extendArray(palettes2, palettes1);
		} catch (IOException e) {
			LOGGER.error("Error loading palettes", e);
			palettes = ColorBrewer.instance().getPalettes();
		}
		DefaultComboBoxModel aModel = new DefaultComboBoxModel(palettes);

		setModel(aModel);

		setRenderer(new PaletteCellRenderer());

	}

}
