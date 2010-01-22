package skrueger.sld;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import schmitzm.geotools.styling.StylingUtil;
import schmitzm.io.IOUtil;
import schmitzm.swing.ExceptionDialog;
import skrueger.atlas.AVUtil;
import skrueger.atlas.gui.plaf.BasicMapLayerLegendPaneUI;
import skrueger.geotools.StyledFS;

public class AtlasStylerSaveLayerToSLDAction extends AbstractAction {
	static private final Logger LOGGER = Logger
			.getLogger(AtlasStylerSaveLayerToSLDAction.class);;

	private final StyledFS styledShp;

	private final Component owner;

	public AtlasStylerSaveLayerToSLDAction(Component owner, StyledFS styledShp) {
		super(AtlasStyler.R("AtlasStylerGUI.saveToSLDFile"),
				BasicMapLayerLegendPaneUI.ICON_EXPORT);
		this.owner = owner;
		this.styledShp = styledShp;

		setEnabled(StylingUtil.isStyleDifferent(styledShp.getStyle(), styledShp
				.getSldFile()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		boolean backup = false;

		if (styledShp.getSldFile().exists()) {
			backup = true;
			try {
				FileUtils.copyFile(styledShp.getSldFile(), IOUtil
						.changeFileExt(styledShp.getSldFile(), "sld.bak"));
			} catch (IOException e1) {
				ExceptionDialog.show(owner, e1);
				return;
			}
		}

		try {
			StylingUtil.saveStyleToSLD(styledShp.getStyle(), styledShp
					.getSldFile());

			if (backup)
				AVUtil.showMessageDialog(owner, AtlasStyler.R(
						"AtlasStylerGUI.saveToSLDFileSuccessAndBackedUp",
						styledShp.getSldFile().getAbsolutePath()));
			else
				AVUtil.showMessageDialog(owner, AtlasStyler.R(
						"AtlasStylerGUI.saveToSLDFileSuccess", styledShp
								.getSldFile().getAbsolutePath()));

		} catch (Exception e1) {
			LOGGER.error(e1);
			ExceptionDialog.show(owner, e1);
			return;
		}
	}
}
