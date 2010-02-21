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
package skrueger.atlas.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import schmitzm.geotools.gui.SelectableXMapPane;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.SwingUtil;
import skrueger.atlas.AVProps;
import skrueger.atlas.AtlasViewer;
import skrueger.atlas.gui.map.AtlasMapView;

public class AtlasScreenScreenshotsDialog extends JDialog {

	private static final int MAP_UPSCALE_FAKTOR = 1;
	private static final int PREVIEW_HEIGHT = 160;
	private final AtlasMapView mapView;
	private BufferedImage[] screenshots;
	private BufferedImage[] previews = new BufferedImage[3];

	public AtlasScreenScreenshotsDialog(final AtlasMapView mapView) {
		this.mapView = mapView;

		setTitle(AtlasViewer.R("Screenshot.DialogTitle"));

		screenshots = makeScreenshots();

		final JRadioButton[] radioButtons = new JRadioButton[3];
		radioButtons[0] = new JRadioButton(AtlasViewer
				.R("Screenshot.Version.WithLegend"));
		radioButtons[0].setSelected(true);
		radioButtons[1] = new JRadioButton(AtlasViewer
				.R("Screenshot.Version.WithGrid"));
		radioButtons[2] = new JRadioButton(AtlasViewer
				.R("Screenshot.Version.MapOnly"));

		/**
		 * Create previews of the screenshots
		 */
		for (int i = 0; i < 3; i++) {

			double ratio = PREVIEW_HEIGHT / (double) screenshots[i].getHeight();

			previews[i] = new BufferedImage(
					(int) (screenshots[i].getWidth() * ratio),
					(int) (screenshots[i].getHeight() * ratio), screenshots[i]
							.getType());
			Graphics2D g = previews[i].createGraphics();

			// double r = previews[i].getWidth() / screenshots[i].getWidth();
			g.drawRenderedImage(screenshots[i], AffineTransform
					.getScaleInstance(ratio, ratio));
		}

		Container cp = getContentPane();

		Panel shots = new Panel(new FlowLayout());

		ButtonGroup buttonGroup = new ButtonGroup();

		for (int i = 0; i < 3; i++) {
			final int idx = i;
			JLabel pvl = new JLabel("", new ImageIcon(previews[i]),
					SwingConstants.CENTER);
			pvl.setBorder(BorderFactory.createLineBorder(Color.black));

			pvl.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
					radioButtons[idx].setSelected(true);
				}

			});

			JPanel oneOption = new JPanel(new BorderLayout());
			oneOption.add(pvl, BorderLayout.NORTH);
			oneOption.add(radioButtons[i], BorderLayout.CENTER);
			radioButtons[i].addKeyListener(keyEscDispose);

			buttonGroup.add(radioButtons[i]);

			oneOption.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			shots.add(oneOption);
		}

		cp.setLayout(new BorderLayout());
		JLabel topLIne = new JLabel(AtlasViewer
				.R("Screenshot.PleaseChooseAnImageText"));
		topLIne.setBorder(BorderFactory.createEmptyBorder(5, 6, 0, 5));
		cp.add(topLIne, BorderLayout.NORTH);
		cp.add(shots, BorderLayout.CENTER);

		JPanel buttonsPanel = new JPanel(new FlowLayout());
		buttonsPanel.setAlignmentX(FlowLayout.RIGHT);

		JButton saveButton = new JButton(new AbstractAction(AtlasViewer
				.R("Screenshot.SaveImageButton")) {

			@Override
			public void actionPerformed(ActionEvent e) {

				// Hihi, don't laugh ;-)
				int idx = 0;
				// if (radioButtons[0].isSelected())
				// idx = 0;
				if (radioButtons[1].isSelected())
					idx = 1;
				if (radioButtons[2].isSelected())
					idx = 2;

				// Opens File save dialog

				final File startWith = new File(mapView.getAtlasConfig()
						.getProperties()
						.get(AVProps.Keys.LastExportFolder, "."));

				JFileChooser fc = new JFileChooser(startWith);
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

				FileFilter imageFilesFilter = new FileFilter() {

					@Override
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith("png")
								|| f.getName().toLowerCase().endsWith("jpg")
								|| f.isDirectory()
								|| f.getName().toLowerCase().endsWith("jpeg");
					}

					@Override
					public String getDescription() {
						return AtlasViewer.R("ImageFileChooser.description");
					}

				};
				fc.setFileFilter(imageFilesFilter);
				fc.setDialogTitle(AtlasViewer
						.R("Screenshot.ChooseImagenameDialogTitle"));

				int showSaveDialog = fc
						.showSaveDialog(AtlasScreenScreenshotsDialog.this);
				if (showSaveDialog != JFileChooser.APPROVE_OPTION)
					return;

				File selectedFile = fc.getSelectedFile();

				// Check for a correct ending
				if (!(selectedFile.getName().toLowerCase().endsWith("png")
						|| selectedFile.getName().toLowerCase().endsWith("jpg") || selectedFile
						.getName().toLowerCase().endsWith("jpeg"))) {

					selectedFile = new File(selectedFile.getAbsolutePath()
							+ ".png");
				}

				if (selectedFile.exists()) {
					int showConfirmDialog = JOptionPane.showConfirmDialog(
							AtlasScreenScreenshotsDialog.this, AtlasViewer
									.R("Screenshot.OverwriteFileQuestion"));
					if (showConfirmDialog != JOptionPane.OK_OPTION)
						return;
				}

				try {
					String TYP;

					if (selectedFile.getName().toLowerCase().endsWith("png"))
						TYP = "PNG";
					else
						TYP = "JPEG";

					ImageIO.write(screenshots[idx], TYP, selectedFile);
					mapView.getAtlasConfig().getProperties().set(mapView,
							AVProps.Keys.LastExportFolder,
							selectedFile.getParentFile().getAbsolutePath());
				} catch (IOException e1) {
					ExceptionDialog.show(AtlasScreenScreenshotsDialog.this, e1);
				}

				dispose();

			}

		});
		saveButton.addKeyListener(keyEscDispose);
		buttonsPanel.add(saveButton);

		cp.add(buttonsPanel, BorderLayout.SOUTH);

		pack();

		SwingUtil.centerFrameOnScreenRandom(this);
		setModal(true);

	}

	@Override
	public void dispose() {
		if (screenshots != null && previews != null) {
			for (int i = 0; i < 3; i++) {
				if (screenshots[i] != null) {
					screenshots[i].flush();
					screenshots[i] = null;
				}

				if (previews[i] != null) {
					previews[i].flush();
					previews[i] = null;
				}
			}
		}
		super.dispose();
	}

	public BufferedImage[] makeScreenshots() {
		Dimension dim;
		Graphics2D g2g;

		// **********************************************************
		// Creating screenshot of the whole AtlasMapView
		// **********************************************************
		dim = mapView.getSize();
		BufferedImage fullImage = new BufferedImage((dim.width), (dim.height),
				BufferedImage.TYPE_INT_RGB);
		g2g = fullImage.createGraphics();

		// Always use AntiAliasing!
		g2g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		mapView.print(g2g);

		// **********************************************************
		// Creating screenshot of the GeoMapPane only (without
		// buttons)
		// **********************************************************
		dim = mapView.getGeoMapPane().getSize();
		BufferedImage gmpImage = new BufferedImage((dim.width), (dim.height),
				BufferedImage.TYPE_INT_RGB);
		g2g = gmpImage.createGraphics();

		// Always use AntiAliasing!
		g2g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		mapView.getGeoMapPane().print(g2g);

		// **********************************************************
		// Creating screenshot of the JMapPane only (without
		// buttons)
		// **********************************************************
		final SelectableXMapPane mp = mapView.getGeoMapPane().getMapPane();

		// Removed the following line when removing setState from XMapPane...
		// WHat was it good for?
		// mp.setState(SelectableXMapPane.SELECT_ONE_FROM_TOP);

		dim = mp.getSize();

		BufferedImage mapImage = new BufferedImage(
				(dim.width * MAP_UPSCALE_FAKTOR),
				(dim.height * MAP_UPSCALE_FAKTOR), BufferedImage.TYPE_INT_RGB);
		g2g = mapImage.createGraphics();
		// Always use AntiAliasing!
		g2g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		AffineTransform at = new AffineTransform();
		at.scale(MAP_UPSCALE_FAKTOR, MAP_UPSCALE_FAKTOR);
		g2g.transform(at);

		mp.print(g2g);

		return new BufferedImage[] { fullImage, gmpImage, mapImage };
	}

	// Pressing ESC disposes the Dialog
	KeyAdapter keyEscDispose = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();
			if (key == KeyEvent.VK_ESCAPE) {
				dispose();
			}
		}
	};

}
