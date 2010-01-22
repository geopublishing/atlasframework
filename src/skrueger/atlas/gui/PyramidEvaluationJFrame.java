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
