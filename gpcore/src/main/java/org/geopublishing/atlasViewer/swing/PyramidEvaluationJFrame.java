/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.swing;

import java.text.DecimalFormat;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

public class PyramidEvaluationJFrame extends JFrame {
	private static PyramidEvaluationJFrame instance;

	static private final Logger log = Logger
			.getLogger(PyramidEvaluationJFrame.class);

	final PyramidEvaluationPanel pyramidEvaluationPanel = new PyramidEvaluationPanel();

	private double[] selectedRes;

	public PyramidEvaluationJFrame() {
		setContentPane(pyramidEvaluationPanel);
	}

	class PyramidEvaluationPanel extends JPanel {
		JLabel zoomScaleLabel = new JLabel("unset");

		JLabel tilesNeededLabel = new JLabel("unset");

		JLabel selectedResLabel = new JLabel("unset");

		JLabel selectedLabel = new JLabel("unset");

		public PyramidEvaluationPanel() {
			Box vbox = Box.createVerticalBox();

			Box hbox = Box.createHorizontalBox();
			hbox.add(new JLabel("Zoom "));
			hbox.add(zoomScaleLabel);
			vbox.add(hbox);

			hbox = Box.createHorizontalBox();
			hbox.add(new JLabel("Tiles needed "));
			hbox.add(tilesNeededLabel);
			vbox.add(hbox);

			add(vbox);

			hbox = Box.createHorizontalBox();
			hbox.add(new JLabel("Selected:"));
			hbox.add(selectedLabel);
			vbox.add(hbox);

			hbox = Box.createHorizontalBox();
			hbox.add(new JLabel("sel res:"));
			hbox.add(selectedResLabel);
			vbox.add(hbox);

			add(vbox);
		}

		public JLabel getZoomScaleLabel() {
			return zoomScaleLabel;
		}

		public JLabel getTilesNeededLabel() {
			return tilesNeededLabel;
		}

		public JLabel getSelectedResLabel() {
			return selectedResLabel;
		}

		public JLabel getSelectedLabel() {
			return selectedLabel;
		}
	}

	public void setZoomScale(Double zoomScale) {
		DecimalFormat formatter = new DecimalFormat("#.#");
		pyramidEvaluationPanel.getZoomScaleLabel().setText(
				formatter.format(zoomScale));
		// pack();
		// setVisible(true);
	}

	public void setImageChoice(Integer imageChoice) {
		pyramidEvaluationPanel.getSelectedLabel().setText(
				imageChoice.toString());
		// pack();
		// setVisible(true);
	}

	public void setTilesNeeded(String tilesNeeded) {
		pyramidEvaluationPanel.getTilesNeededLabel().setText(tilesNeeded);
		// pack();
		// setVisible(true);
	}

	public void setImageChoiceRes(double[] ds) {
		DecimalFormat f = new DecimalFormat("#.#");
		pyramidEvaluationPanel.getSelectedResLabel().setText(
				f.format(ds[0]) + " x " + f.format(ds[1]));
		// pack();
		// setVisible(true);
	}

	public static PyramidEvaluationJFrame getInstance() {
		if (instance == null) {
			instance = new PyramidEvaluationJFrame();
		}
		return instance;

	}

}
