package org.geopublishing.geopublisher.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpUtil;

import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import skrueger.geotools.LogoPosition;
import skrueger.swing.AtlasDialog;
import skrueger.swing.SmallButton;

/**
 * This dialog allows to configure the images and icons used in the atlas.
 */
public class AtlasIconsDialog extends AtlasDialog {

	Logger log = Logger.getLogger(AtlasIconsDialog.class);

	private final AtlasConfigEditable ace;

	private final JTabbedPane tabbedPane = new JTabbedPane();

	private final Timer scanFontFolderTimer = new Timer();

	/**
	 * Caches the file sizes of the icon files. If their size changes, the GUI
	 * is rebuild.
	 */
	final HashMap<String, Long> cacheIconSizes = new HashMap<String, Long>();

	public AtlasIconsDialog(Component parentWindow,
			final AtlasConfigEditable ace) {
		super(parentWindow, GeopublisherGUI.R("AtlasIconsDialog.Title"));
		this.ace = ace;

		scanFontFolderTimer.schedule(new TimerTask() {

			@Override
			public void run() {

				// Check for changes of icon file sizes:

				Long maplogoSize = new File(ace.getAtlasDir(),
						AtlasConfig.MAPLOGO_RESOURCE_NAME).length();
				Long splashscreeniconSize = new File(ace.getAtlasDir(),
						AtlasConfig.SPLASHSCREEN_RESOURCE_NAME).length();
				Long iconSize = new File(ace.getAtlasDir(),
						AtlasConfig.JWSICON_RESOURCE_NAME).length();
				try {

					if (!maplogoSize.equals(cacheIconSizes
							.get(AtlasConfig.MAPLOGO_RESOURCE_NAME))) {
						initGui();
						return;
					}

					if (!iconSize.equals(cacheIconSizes
							.get(AtlasConfig.JWSICON_RESOURCE_NAME))) {
						initGui();
						return;
					}

					if (!splashscreeniconSize.equals(cacheIconSizes
							.get(AtlasConfig.SPLASHSCREEN_RESOURCE_NAME))) {
						initGui();
						return;
					}
				} finally {
					cacheIconSizes.put(AtlasConfig.MAPLOGO_RESOURCE_NAME,
							maplogoSize);
					cacheIconSizes.put(AtlasConfig.JWSICON_RESOURCE_NAME,
							iconSize);
					cacheIconSizes.put(AtlasConfig.SPLASHSCREEN_RESOURCE_NAME,
							splashscreeniconSize);
				}
			}
		}, 0, 1100);

		// initGui();

		SwingUtil.setRelativeFramePosition(this, parentWindow,
				SwingUtil.BOUNDS_INNER, SwingUtil.NORTHEAST);
		setVisible(true);
	}

	@Override
	public boolean close() {
		scanFontFolderTimer.cancel();
		return super.close();
	}

	protected void initGui() {
		int sel = tabbedPane.getSelectedIndex();
		tabbedPane.removeAll();

		tabbedPane.insertTab(GeopublisherGUI.R("AtlasIcons.MapLogo"), null,
				createMaplogoTab(), null, tabbedPane.getTabCount());

		tabbedPane.insertTab(GeopublisherGUI.R("AtlasIcons.AppIcon"), null,
				createAppiconTab(), null, tabbedPane.getTabCount());

		tabbedPane.insertTab(GeopublisherGUI.R("AtlasIcons.Splashscreen"),
				null, createSplashscreenTab(), null, tabbedPane.getTabCount());

		if (sel >= 0)
			tabbedPane.setSelectedIndex(sel);

		/**
		 * Building the content pane
		 */
		final JPanel contentPane = new JPanel(new MigLayout("wrap 1"));
		contentPane.add(tabbedPane);

		JPanel buttons = new JPanel(new MigLayout("", "[grow]"));
		buttons.add(getOkButton(), "tag ok");
		contentPane.add(buttons, "grow");

		setContentPane(contentPane);
		pack();
	}

	private JPanel createMaplogoTab() {
		JPanel p = new JPanel(new MigLayout("wrap 1", "[:500:550]"));
		p.add(new JLabel("<html>" + GpUtil.R("AtlasIcons.MapLogo.Explanation")));
		// p.add(new JLabel(IOUtil.escapePath(new File(ace.getAtlasDir(),
		// AtlasConfig.MAPLOGO_RESOURCE_NAME))));

		final String resLoc = AtlasConfig.MAPLOGO_RESOURCE_NAME;

		p.add(new IconPreview(resLoc), "grow");

		p.add(new OpenFolderButton(), "split 5");
		p.add(new ResetButton(
				AtlasConfigEditable.MAPLOGO_RESOURCE_NAME_FALLBACK, resLoc));
		p.add(new DeleteIconButton(resLoc));

		p.add(new JLabel(GpUtil.R("AtlasIcons.MapLogo.Position")), "gapx unrel");
		final LogopositionCombobox posCombobox = new LogopositionCombobox();
		posCombobox.setSelectedItem(ace.getMaplogoPosition());
		posCombobox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				ace.setMaplogoPosition((LogoPosition) e.getItem());
			}
		});
		p.add(posCombobox);

		return p;
	}

	private JPanel createAppiconTab() {
		JPanel p = new JPanel(new MigLayout("wrap 1", "[:500:550]"));
		p.add(new JLabel("<html>" + GpUtil.R("AtlasIcons.AppIcon.Explanation")));
		// p.add(new JLabel(IOUtil.escapePath(new File(ace.getAtlasDir(),
		// AtlasConfig.MAPLOGO_RESOURCE_NAME))));

		final String resLoc = AtlasConfig.JWSICON_RESOURCE_NAME;

		p.add(new IconPreview(resLoc), "grow");

		p.add(new OpenFolderButton(), "split 2");
		p.add(new ResetButton(
				AtlasConfigEditable.JWSICON_RESOURCE_NAME_FALLBACK, resLoc));
		// p.add(new DeleteIconButton(resLoc));

		return p;
	}

	private JPanel createSplashscreenTab() {
		JPanel p = new JPanel(new MigLayout("wrap 1", "[:500:550]"));
		p.add(new JLabel("<html>"
				+ GpUtil.R("AtlasIcons.Splashscreen.Explanation")));
		// p.add(new JLabel(IOUtil.escapePath(new File(ace.getAtlasDir(),
		// AtlasConfig.MAPLOGO_RESOURCE_NAME))));

		final String resLoc = AtlasConfig.SPLASHSCREEN_RESOURCE_NAME;

		p.add(new IconPreview(resLoc), "grow");

		p.add(new OpenFolderButton(), "split 2");
		p.add(new ResetButton(
				AtlasConfigEditable.SPLASHSCREEN_RESOURCE_NAME_FALLBACK, resLoc));
		// p.add(new DeleteIconButton(resLoc));

		return p;
	}

	class IconPreview extends JPanel {
		public IconPreview(String resLocation) {
			super(new MigLayout("", "[grow]"));
			setBorder(BorderFactory.createTitledBorder(resLocation));
			ImageIcon img = null;
			int width = 0, height = 0;
			BufferedImage bi = null;

			File imgFile = new File(ace.getAtlasDir(), resLocation);
			if (!imgFile.exists() || !imgFile.canRead()) {
				img = Icons.ICON_UNKOWN_BIG;
				add(new JLabel(GeopublisherGUI.R("iconMissing")));
				return;
			} else {
				try {
					bi = ImageIO.read(imgFile);
					img = new ImageIcon(bi);
					width = img.getIconWidth();
					height = img.getIconHeight();
				} catch (IOException e) {
					log.error(e);
				}
			}

			final JLabel imgLabel = new JLabel(img);
			add(imgLabel, "wrap");

			JPanel imgStats = new JPanel(new MigLayout(""));
			imgStats.add(new JLabel(width + "x" + height));
			if (bi != null) {
				imgStats.add(new JLabel(GeopublisherGUI.R("imgTransparent")
						+ ":" + (bi.getAlphaRaster() != null)));
			}

			add(imgStats);
		}
	}

	class ResetButton extends SmallButton {
		public ResetButton(final String resLoc, final String fileLocation) {
			super(new AbstractAction(
					GeopublisherGUI.R("AtlasIcons.ResetToDefault")) {

				@Override
				public void actionPerformed(ActionEvent e) {

					if (!SwingUtil.askYesNo(AtlasIconsDialog.this,
							GpUtil.R("AtlasIcons.ResetToDefault.Question")))
						return;

					try {
						URL fromUrl = GpUtil.class.getResource(resLoc);
						System.out.println(resLoc);
						File toFile = new File(ace.getAtlasDir(), fileLocation);
						FileUtils.copyURLToFile(fromUrl, toFile);
						cacheIconSizes.put(resLoc, toFile.length());
						initGui();
					} catch (IOException e1) {
						ExceptionDialog.show(AtlasIconsDialog.this, e1);
					} finally {
					}
				}
			});
		}
	}

	class DeleteIconButton extends SmallButton {
		public DeleteIconButton(final String fileLocation) {
			super(
					new AbstractAction(
							GeopublisherGUI.R("AtlasIcons.DeleteIcon")) {

						@Override
						public void actionPerformed(ActionEvent e) {

							if (!SwingUtil.askYesNo(AtlasIconsDialog.this,
									GpUtil.R("AtlasIcons.Delete.Question",
											fileLocation)))
								return;

							File delFile = new File(ace.getAtlasDir(),
									fileLocation);
							FileUtils.deleteQuietly(delFile);
							cacheIconSizes.put(fileLocation, delFile.length());
							initGui();
						}
					});
		}
	}

	class OpenFolderButton extends SmallButton {

		public OpenFolderButton() {
			super(new AbstractAction(
					GpUtil.R("PersonalizeImages_OpenADFolderButton_label")) {

				@Override
				public void actionPerformed(final ActionEvent e) {
					SwingUtil.openOSFolder(ace.getAd());
				}
			});
		}
	};
}
