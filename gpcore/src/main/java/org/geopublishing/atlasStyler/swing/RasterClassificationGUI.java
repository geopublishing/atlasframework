package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerRaster;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.classification.ClassificationChangeEvent;
import org.geopublishing.atlasStyler.classification.ClassificationChangeEvent.CHANGETYPES;
import org.geopublishing.atlasStyler.classification.RasterClassification;

import de.schmitzm.swing.JPanel;

public class RasterClassificationGUI extends ClassificationGUI {

	private JPanel noDataValueBox;

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
		try {
			return classifier.createHistogramImage(getJCheckBoxShowMean()
					.isSelected(), getJCheckBoxShowSD().isSelected(),
					histogramBins, "values"); // i8n
		} catch (Exception e) {
			LOGGER.error("Error creating histogram image", e);
			return ERROR_IMAGE;
		}
	}

	@Override
	protected JPanel getJPanelData() {
		if (noDataValueBox == null) {
			noDataValueBox = new JPanel(new MigLayout());

			noDataValueBox.add(new JLabel(ASUtil.R("NoDataValueLabel") + ":"));

			final JTextField tf = new JTextField(10);
			tf.setText(String.valueOf(getClassifier().getStyledRaster()
					.getNodataValue()));
			noDataValueBox.add(tf, "gap rel");
			tf.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					doit(tf);
				}

			});

			tf.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					doit(tf);
				}
			});

		}
		return noDataValueBox;
	}

	@Override
	public RasterClassification getClassifier() {
		return (RasterClassification) classifier;
	}

	public void doit(JTextField tf) {

		try {
			String text = tf.getText();
			
			if ("".equals(text) || "null".equals(text)) {
				getClassifier().getStyledRaster().setNodataValue(null);
				classifier.fireEvent(new ClassificationChangeEvent(
						CHANGETYPES.NODATAVALUE_CHANGED));
				tf.setText("");
				return;
			}

			final Double parsed = Double.valueOf(text);
			if (parsed != null
					&& !parsed.equals(getClassifier().getStyledRaster()
							.getNodataValue())) {
				getClassifier().getStyledRaster().setNodataValue(parsed);
				classifier.fireEvent(new ClassificationChangeEvent(
						CHANGETYPES.NODATAVALUE_CHANGED));
			}

		} catch (Exception ee) {
			LOGGER.warn("Entering NODATA value in GUI", ee);
		}
	}

}
