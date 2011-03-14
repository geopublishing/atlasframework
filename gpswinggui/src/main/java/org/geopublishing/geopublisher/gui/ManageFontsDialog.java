package org.geopublishing.geopublisher.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.io.FileUtils;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpUtil;
import org.geopublishing.geopublisher.swing.GpSwingUtil;
import org.opengis.filter.expression.Literal;

import de.schmitzm.io.IOUtil;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.AtlasDialog;
import de.schmitzm.swing.SwingUtil;

/**
 * This dialog allows to manage the additional fonts usde in the atlas.
 */
public class ManageFontsDialog extends AtlasDialog {

	private final AtlasConfigEditable ace;
	private JList userFontsList;
	private final Timer scanFontFolderTimer;

	/**
	 * This dialog is initialized modal and visible
	 */
	public ManageFontsDialog(Component owner, AtlasConfigEditable ace) {
		super(owner, GpUtil.R("ManageFontsDialog.title"));
		this.ace = ace;

		scanFontFolderTimer = new Timer();
		scanFontFolderTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				updateUserFontsList();
			}
		}, 0, 1100);

		initGui();
		pack();
		setModal(true);
		// setVisible(true);
	}

	private void initGui() {
		setLayout(new MigLayout("wrap 1", "[,,400]"));

		add(new JLabel(GpSwingUtil.R("ManageFontsDialog.explanation.html",
				AtlasStylerVector.getDefaultFontFamilies().length,
				IOUtil.escapePath(ace.getFontsDir()))));
		add(new JLabel(GpSwingUtil.R("ManageFontsDialog.defaults.explanation")));
		add(new JScrollPane(getDefaultFontsList()), "grow");
		add(new JLabel(GpSwingUtil.R("ManageFontsDialog.userfonts.explanation")));

		add(new JScrollPane(getUserFontsList()), "grow");

		add(new JButton(new AbstractAction(
				GpSwingUtil.R("ManageFontsDialog.reloadFontFolder.label")) {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtil.openOSFolder(ace.getFontsDir());
			}
		}), "span 2, split 2");

		add(getOkButton(), "tag ok");
	}

	@Override
	public boolean close() {
		scanFontFolderTimer.cancel();
		ace.getFonts().clear();
		ace.getFonts().addAll(Arrays.asList(fontsInFolder));
		ace.registerFonts();
		return super.close();
	}

	private JList getDefaultFontsList() {
		JList defaultFontsList = new JList(AtlasStylerVector.getDefaultFontFamilies());

		defaultFontsList.setCellRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Component listCellRendererComponent = super
						.getListCellRendererComponent(list, value, index,
								isSelected, cellHasFocus);

				if (listCellRendererComponent instanceof JLabel) {
					JLabel jlabel = (JLabel) listCellRendererComponent;
					List<Literal> literals = (List<Literal>) value;
					String text = LangUtil.stringConcatWithSep(" => ",
							literals.toArray());
					jlabel.setText(text);
				}

				return listCellRendererComponent;
			}
		});

		return defaultFontsList;
	}

	private JList getUserFontsList() {
		if (userFontsList == null) {
			userFontsList = new JList(fontNamesInFolder);
		}
		return userFontsList;
	}

	public void updateUserFontsList() {
		scanFolderForFonts();
		getUserFontsList().setListData(fontNamesInFolder);
	}

	// List of only the successfully read fonts in font folder
	Font[] fontsInFolder = new Font[0];

	// One entry for every .ttf file in fonts folder, even if it could not be
	// read
	String[] fontNamesInFolder = new String[0];

	private void scanFolderForFonts() {
		fontsInFolder = new Font[0];
		fontNamesInFolder = new String[0];

		Collection<File> listTtfFiles = FileUtils.listFiles(ace.getFontsDir(),
				GpUtil.FontsFilesFilter, IOUtil.BlacklistedFoldersFilter);
		for (File f : listTtfFiles) {

			String relPath = f.getAbsolutePath().substring(
					ace.getFontsDir().getAbsolutePath().length() + 1);
			try {

				Font font = Font.createFont(Font.TRUETYPE_FONT, f);
				fontNamesInFolder = LangUtil.extendArray(fontNamesInFolder,
						font.getName());
				fontsInFolder = LangUtil.extendArray(fontsInFolder, font);
			} catch (Exception e) {
				String errorLine = relPath + ", ERROR: "
						+ e.getLocalizedMessage();
				fontNamesInFolder = LangUtil.extendArray(fontNamesInFolder,
						errorLine);
			}
		}
	}

}
