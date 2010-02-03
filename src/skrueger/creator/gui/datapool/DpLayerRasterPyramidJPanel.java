package skrueger.creator.gui.datapool;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;

import net.miginfocom.swing.MigLayout;
import schmitzm.swing.JPanel;
import skrueger.atlas.dp.layer.DpLayerRasterPyramid;
import skrueger.creator.AtlasCreator;
import skrueger.sld.ASUtil;
import skrueger.sld.AtlasStyler;
import skrueger.swing.Cancellable;
import skrueger.swing.ColorButton;

public class DpLayerRasterPyramidJPanel extends JPanel implements Cancellable {
	private final DpLayerRasterPyramid backup;
	private final DpLayerRasterPyramid pyr;

	public DpLayerRasterPyramidJPanel(final DpLayerRasterPyramid pyr) {
		super(new MigLayout());
		this.pyr = pyr;

		/** Make backup **/
		{
			backup = new DpLayerRasterPyramid(pyr.getAc());
			backup.setInputTransparentColor(pyr.getInputTransparentColor());
		}

		/** Create the GUI **/

		JPanel transparentColorPanel = new JPanel(new MigLayout());
		transparentColorPanel.setBorder(BorderFactory
				.createTitledBorder("Transparenz")); //i8n

		final ColorButton transparentColorButton = new ColorButton(pyr
				.getInputTransparentColor());
		final JButton resetTransparentColorButton = new JButton();

		/**
		 * One button to select a color
		 */
		transparentColorButton.setAction(new AbstractAction(AtlasCreator
				.R("DesignAtlasChartJDialog.TransparentColor.ChooseButton")) {

			@Override
			public void actionPerformed(final ActionEvent e) {
				pyr.setInputTransparentColor(ASUtil.showColorChooser(
						DpLayerRasterPyramidJPanel.this,
						AtlasCreator.R("DesignAtlasChartJDialog.TransparentColor.ChooseColorTitle"), pyr 
								.getInputTransparentColor()));
				transparentColorButton.setColor(pyr.getInputTransparentColor());

				resetTransparentColorButton.setEnabled(pyr
						.getInputTransparentColor() != null);

				// TODO Update any JMapPanes that contain that layer!?
				pyr.uncache();
			}

		});

		add(transparentColorButton);

		/**
		 * Second button to reset the color
		 */
		resetTransparentColorButton.setAction(new AbstractAction(AtlasCreator
				.R("DesignAtlasChartJDialog.TransparentColor.Reset")) {

			@Override
			public void actionPerformed(final ActionEvent e) {
				pyr.setInputTransparentColor(null);
				transparentColorButton.setColor(pyr.getInputTransparentColor());

				// TODO Update any JMapPanes that contain that layer!?
				pyr.uncache();

				resetTransparentColorButton.setEnabled(false);
			}

		});
		resetTransparentColorButton.setToolTipText(AtlasCreator
				.R("DesignAtlasChartJDialog.TransparentColor.Reset.TT"));
		transparentColorButton.setColor(pyr.getInputTransparentColor());

		resetTransparentColorButton
				.setEnabled(pyr.getInputTransparentColor() != null);
		add(resetTransparentColorButton);

	}

	@Override
	public void cancel() {
		pyr.setInputTransparentColor(backup.getInputTransparentColor());

		backup.dispose();
	}
}
