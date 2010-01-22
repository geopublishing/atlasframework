/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
 * http://www.geopublishing.org
 * 
 * Geopublisher is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.creator.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import schmitzm.swing.SwingUtil;
import skrueger.atlas.AVUtil;
import skrueger.creator.AtlasCreator;
import skrueger.i8n.I8NUtil;
import skrueger.i8n.Translation;
import skrueger.swing.CancelButton;
import skrueger.swing.CancellableDialogAdapter;
import skrueger.swing.OkButton;

/**
 * A modal dialog that allows to define the languages for the atlas.
 */
public class LanguageSelectionDialog extends CancellableDialogAdapter {

	Vector<String> availLangs = new Vector<String>();
	private final List<String> orig;
	private final List<String> aceLanguages = new ArrayList<String>();
	private JComboBox languageCombo;

	private boolean cancel = false;

	public LanguageSelectionDialog(Window owner, List<String> orig) {

		super(owner, AtlasCreator.R("LanguageSelectionDialog.Title"));

		this.orig = orig;
		aceLanguages.addAll(orig);

		initialize();

		SwingUtil.setRelativeFramePosition(this, owner, SwingUtil.BOUNDS_INNER,
				SwingUtil.NORTH);

		setModal(true);

	}

	/**
	 * The {@link JComboBox} will be filled with all languages that are not yet
	 * activated for the atlas.
	 */
	private void updateComboBox() {
		availLangs.clear();

		for (String l1 : I8NUtil.getLanguageCodes()) {
			if (!aceLanguages.contains(l1)) {
				Locale locale = new Locale(l1);
				availLangs.add(locale.getDisplayLanguage() + " / "
						+ locale.getDisplayLanguage(locale) + " " + l1);
			} else {
				// System.out.println("Wird ausgelassen: " + l1);
			}
		}

		Collections.sort(availLangs);

		languageCombo.setModel(new DefaultComboBoxModel(availLangs));
		languageCombo.setSelectedIndex(-1);
		languageCombo.repaint();
	}

	private void initialize() {

		Box cp = new Box(BoxLayout.Y_AXIS);

		JPanel installed = new JPanel(new BorderLayout());
		installed.setBorder(BorderFactory.createTitledBorder(AtlasCreator
				.R("LanguageSelectionDialog.ConfiguredLanguages")));

		final JTable installedTable = new JTable();
		installedTable.setModel(new DefaultTableModel() {
			@Override
			public int getColumnCount() {
				return 1;
			}

			@Override
			public Object getValueAt(int row, int column) {
				Locale locale = new Locale(aceLanguages.get(row));
				return locale.getDisplayLanguage() + " / "
						+ locale.getDisplayLanguage(locale) + " "
						+ aceLanguages.get(row);
			}

			@Override
			public int getRowCount() {
				return aceLanguages.size();
			}

			@Override
			public String getColumnName(int column) {
				return "";
			}

		});
		installedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane tableScroll = new JScrollPane(installedTable);
		installed.add(tableScroll, BorderLayout.CENTER);
		JPanel removeInstalled = new JPanel(new BorderLayout());
		final JButton removeInstalledButton = new JButton(new AbstractAction(
				AtlasCreator.R("LanguageSelectionDialog.Button.Remove")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				aceLanguages.remove(installedTable.getSelectedRow());

				updateComboBox();

				((DefaultTableModel) installedTable.getModel())
						.fireTableDataChanged();

				installedTable.repaint();
			}

		});
		removeInstalled.add(removeInstalledButton, BorderLayout.EAST);
		installed.add(removeInstalled, BorderLayout.SOUTH);
		cp.add(installed);
		installedTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {
						removeInstalledButton.setEnabled(installedTable
								.getSelectedColumn() != -1);
					}

				});
		removeInstalledButton.setEnabled(false);
		JPanel available = new JPanel(new BorderLayout());
		available.setBorder(BorderFactory.createTitledBorder(AtlasCreator
				.R("LanguageSelectionDialog.AvailableLanguages")));
		JPanel languageComboBox = new JPanel(new FlowLayout());

		languageCombo = new JComboBox();
		updateComboBox();

		languageComboBox.add(languageCombo);
		final JButton addLangButton = new JButton(new AbstractAction(
				AtlasCreator.R("LanguageSelectionDialog.Button.Add")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				String newLang = (String) languageCombo.getSelectedItem();
				aceLanguages.add(newLang.substring(newLang.length() - 2));
				updateComboBox();
				((DefaultTableModel) installedTable.getModel())
						.fireTableDataChanged();
			}

		});
		addLangButton.setEnabled(false);
		languageComboBox.add(addLangButton);
		languageCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				addLangButton
						.setEnabled(languageCombo.getSelectedIndex() != -1);
			}

		});
		available.add(languageComboBox);

		cp.add(available);

		/**
		 * OK and CANCEL Button
		 */
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(new CancelButton(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cancel();
				dispose();
			}

		}));
		buttons.add(new OkButton(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (aceLanguages.size() < 1) {
					AVUtil
							.showMessageDialog(
									LanguageSelectionDialog.this,
									AtlasCreator
											.R("LanguageSelectionDialog.ErrorMsg.YouNeedMinimumOneLanguage"));
					cancelClose();
				} else {
					okClose();
				}
				dispose();
			}

		}));
		cp.add(buttons);

		SwingUtil.setPreferredHeight(tableScroll, 100);

		setContentPane(cp);
		pack();

	}

	@Override
	public void cancel() {
		cancel = true;
	}

	public boolean isCancel() {
		return cancel;
	}

	@Override
	public boolean okClose() {
		orig.clear();
		orig.addAll(aceLanguages);

		// Switch the GP GUI to the first language supported by the GP gui
		for (String lang : orig) {
			if (lang.equalsIgnoreCase("de")) {
				Translation.setActiveLang("de");
				break;
			} else if (lang.equalsIgnoreCase("en")) {
				Translation.setActiveLang("en");
				break;
			} else if (lang.equalsIgnoreCase("fr")) {
				Translation.setActiveLang("fr");
				break;
			}
		}

		 Translation.fireLocaleChangeEvents();

		return super.okClose();
	}

}
