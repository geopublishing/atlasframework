/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasStyler SLD/SE Editor application - A Java Swing-based GUI to create OGC Styled Layer Descriptor (SLD 1.0) / OGC Symbology Encoding 1.1 (SE) XML files.
 * http://www.geopublishing.org
 * 
 * AtlasStyler SLD/SE Editor is part of the Geopublishing Framework hosted at:
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
package skrueger.sld.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import skrueger.sld.ASUtil;
import skrueger.sld.AtlasStyler;
import skrueger.sld.RuleChangeListener;
import skrueger.sld.RuleChangedEvent;
import skrueger.sld.SinglePolygonSymbolRuleList;
import skrueger.sld.SingleRuleList;
import skrueger.swing.AtlasDialog;
import skrueger.swing.CancelButton;
import skrueger.swing.ColorButton;
import skrueger.swing.OkButton;

public class SymbolSelectorGUI extends AtlasDialog {
	protected Logger LOGGER = ASUtil.createLogger(this);

	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_CANCEL_CHANGES = "CANCEL CHANGES";

	// public static final String PROPERTY_PREVIEW_UPDATE = "PREVIEW UPDATE"; //

	public static final String PROPERTY_CLOSED = "CLOSED";

	private static final String DIALOG_TITLE = AtlasStyler
			.R("SymbolSelector.Title");

	private static final Dimension DEFAULT_PREVIEW_ICON_SIZE = new Dimension(
			80, 60);

	private JPanel jContentPane = null;

	private JPanel jPanelPreview = null;

	private final JLabel jLabelPreviewIcon = new JLabel("");

	private JPanel jPanelOptions = null;

	private ColorButton jButtonColor = null;

	private JLabel jLabelColor = null;

	private JLabel jLabelSize = null;

	private JComboBox jComboBoxSize = null;

	private JLabel jLabelRotation = null;

	private JComboBox jComboBoxRotation = null;

	private JButton jButtonOK = null;

	private JButton jButtonCancel = null;

	private JButton jButtonProperties = null;

	private JButton jButtonSave = null;

	private JButton jButtonReset = null;

	private JPanel jPanel = null;

	private SingleRuleList<?> singleSymbolRuleList;

	private JTabbedPane jTabbedPane = null;

	private JScrollPaneSymbolsLocal symbolsLocal;

	private JScrollPaneSymbolsOnline symbolsOnline;

	/**
	 * We have to remember the last loaded RulesList, so we can use the reset
	 * button. *
	 */
	private SingleRuleList<?> lastSelectedRuleList;

	private RuleChangeListener listenToRuleChangesAndUpdatePreview = new RuleChangeListener() {

		public void changed(RuleChangedEvent e) {
			jLabelPreviewIcon
					.setIcon(new ImageIcon(
							singleSymbolRuleList
									.getImage(DEFAULT_PREVIEW_ICON_SIZE)));
		}

	};


	/**
	 * @param title
	 *            May be <code>null</code> to use default title
	 */
	public SymbolSelectorGUI(Component owner, String title,
			final SingleRuleList singleSymbolRuleList) {
		super(owner);
		if (title != null)
			setTitle(title);
		else
			setTitle(DIALOG_TITLE);

		this.singleSymbolRuleList = singleSymbolRuleList;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(510, 320);
		this.setContentPane(getJContentPane());

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cancel();
			}
		});

		SwingUtil.setRelativeFramePosition(this, getParent(),
				SwingUtil.BOUNDS_OUTER, SwingUtil.NORTHEAST);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.fill = GridBagConstraints.BOTH;
			gridBagConstraints16.gridy = 1;
			gridBagConstraints16.weightx = 1.0;
			gridBagConstraints16.weighty = 1.0;
			gridBagConstraints16.gridheight = 4;
			gridBagConstraints16.gridx = 0;
			GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
			gridBagConstraints51.gridx = 3;
			gridBagConstraints51.gridwidth = 3;
			gridBagConstraints51.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints51.anchor = GridBagConstraints.SOUTH;
			gridBagConstraints51.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints51.gridy = 4;
			GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
			gridBagConstraints61.gridx = 2;
			gridBagConstraints61.anchor = GridBagConstraints.NORTH;
			gridBagConstraints61.gridwidth = 4;
			gridBagConstraints61.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints61.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints61.gridy = 2;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.anchor = GridBagConstraints.NORTH;
			gridBagConstraints5.gridx = 0;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 2;
			gridBagConstraints4.gridheight = 2;
			gridBagConstraints4.anchor = GridBagConstraints.NORTH;
			gridBagConstraints4.gridwidth = 4;
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints4.gridy = 0;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getJPanelPreview(), gridBagConstraints4);
			jContentPane.add(getJPanelOptions(), gridBagConstraints61);
			jContentPane.add(getJPanel(), gridBagConstraints51);
			jContentPane.add(getJTabbedPane(), gridBagConstraints16);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelPreview() {
		if (jPanelPreview == null) {
			jPanelPreview = new JPanel(new MigLayout("align center"), AtlasStyler
					.R("SymbolSelector.Preview.BorderTitle"));
			jLabelPreviewIcon.setOpaque(false);
			jPanelPreview.add(jLabelPreviewIcon,"align center");
			singleSymbolRuleList
					.addListener(listenToRuleChangesAndUpdatePreview );
			jLabelPreviewIcon.setIcon(new ImageIcon(singleSymbolRuleList
					.getImage(DEFAULT_PREVIEW_ICON_SIZE)));
		}
		return jPanelPreview;
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelOptions() {
		if (jPanelOptions == null) {
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints11.gridy = 2;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.anchor = GridBagConstraints.WEST;
			gridBagConstraints11.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints11.gridx = 1;
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 0;
			gridBagConstraints10.anchor = GridBagConstraints.EAST;
			gridBagConstraints10.gridy = 2;
			jLabelRotation = new JLabel(AtlasStyler.R("RotationLabel"));
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints9.gridy = 1;
			gridBagConstraints9.weightx = 1.0;
			gridBagConstraints9.anchor = GridBagConstraints.WEST;
			gridBagConstraints9.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints9.gridx = 1;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.anchor = GridBagConstraints.EAST;
			gridBagConstraints8.gridy = 1;
			jLabelSize = new JLabel(AtlasStyler.R("SizeLabel"));
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.anchor = GridBagConstraints.EAST;
			gridBagConstraints7.gridy = 0;
			jLabelColor = new JLabel(AtlasStyler.R("ColorLabel"));
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints6.gridy = 0;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.anchor = GridBagConstraints.WEST;
			gridBagConstraints6.insets = new Insets(5, 5, 5, 0);
			gridBagConstraints6.gridx = 1;
			jPanelOptions = new JPanel();
			jPanelOptions.setLayout(new GridBagLayout());
			jPanelOptions.setBorder(BorderFactory
					.createEmptyBorder(5, 15, 5, 5));
			jPanelOptions.add(getJButtonColor(), gridBagConstraints6);
			jPanelOptions.add(jLabelColor, gridBagConstraints7);
			jPanelOptions.add(jLabelSize, gridBagConstraints8);
			jPanelOptions.add(getJComboBoxSize(), gridBagConstraints9);

			if (singleSymbolRuleList.getRotation() != null) {
				jPanelOptions.add(jLabelRotation, gridBagConstraints10);
				jPanelOptions.add(getJComboBoxAngle(), gridBagConstraints11);
			}
		}
		return jPanelOptions;
	}

	/**
	 * This method initializes a {@link JButton} that allows to "Intelligently"
	 * change a color in the {@link SingleRuleList}
	 * 
	 * @return javax.swing.JComboBox
	 */
	private ColorButton getJButtonColor() {
		if (jButtonColor == null) {
			jButtonColor = new ColorButton();

			jButtonColor.setAction(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					Color color = null;

					color = singleSymbolRuleList.getColor();

					Color newColor = ASUtil.showColorChooser(
							SymbolSelectorGUI.this, AtlasStyler
									.R("SymbolSelector.Preview.ChooseColor"),
							color);

					if (newColor != null) {
						singleSymbolRuleList.setColor(newColor);

					}

				}

			});

			jButtonColor.setColor(singleSymbolRuleList.getColor());

			singleSymbolRuleList.addListener(colorButtonReactToStyleChangesListener);

			boolean enabled = singleSymbolRuleList.hasColor();
			jButtonColor.setEnabled(enabled);
			jLabelColor.setEnabled(enabled);
		}
		return jButtonColor;
	}
	

	/** Listen for RuleChanges to update the button */
	RuleChangeListener colorButtonReactToStyleChangesListener = new RuleChangeListener() {

		public void changed(RuleChangedEvent e) {
			boolean enabled = singleSymbolRuleList.hasColor();

			if (enabled) {
				// System.out.println("Should update the button");
				jButtonColor.setColor(singleSymbolRuleList.getColor());
			}

			jButtonColor.setEnabled(enabled);
			jLabelColor.setEnabled(enabled);
		}

	};

	/**
	 * This method initializes jComboBox2
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxSize() {
		if (jComboBoxSize == null) {
			jComboBoxSize = new JComboBox();

			if (FeatureUtil.getGeometryForm((singleSymbolRuleList
					.getGeometryDescriptor())) == GeometryForm.LINE) {
				/***************************************************************
				 * This is about Stroke WIDTH
				 */
				jComboBoxSize.setModel(new DefaultComboBoxModel(
						AbstractEditGUI.WIDTH_VALUES));

				// jComboBoxSize.setRenderer(AbstractEditGUI.WIDTH_VALUES_RENDERER);

				if (!Arrays.asList(AbstractEditGUI.WIDTH_VALUES).contains(
						singleSymbolRuleList.getSizeBiggest())) {
				}

			} else {
				/***************************************************************
				 * This is about Graphic Sizes
				 */
				jComboBoxSize.setModel(new DefaultComboBoxModel(
						AbstractEditGUI.SIZE_VALUES));

				if (!Arrays.asList(AbstractEditGUI.SIZE_VALUES).contains(
						singleSymbolRuleList.getSizeBiggest())) {
					// TODO Value not in list!
				}

			}

			singleSymbolRuleList.addListener(sizeComboBoxReactsToStyleChangesListener);

			jComboBoxSize.setEnabled(singleSymbolRuleList.hasSize());
			jLabelSize.setEnabled(singleSymbolRuleList.hasSize());

			ASUtil.selectOrInsert(jComboBoxSize, singleSymbolRuleList
					.getSizeBiggest());
			// jComboBoxSize
			// .setSelectedItem(singleSymbolRuleList.getSizeBiggest());

			/** Change in the COmboBox * */
			jComboBoxSize.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						Float size = (Float) jComboBoxSize.getSelectedItem();

						// System.out.println("do set from
						// "+singleSymbolRuleList.getSizeBiggest()+ " to
						// "+size);
						singleSymbolRuleList.setSizeBiggest(size);

					}

				}

			});

			ASUtil.addMouseWheelForCombobox(jComboBoxSize);

		}
		return jComboBoxSize;
	}


	/** Listen for RuleChanges to update the button */
	RuleChangeListener sizeComboBoxReactsToStyleChangesListener = new RuleChangeListener() {

		public void changed(RuleChangedEvent e) {

			boolean enabled = singleSymbolRuleList.hasSize();

			if (enabled) {
				// LOGGER.info("Should update the size to
				// "+singleSymbolRuleList
				// .getSizeBiggest());
				if (((DefaultComboBoxModel) jComboBoxSize.getModel())
						.getIndexOf(singleSymbolRuleList
								.getSizeBiggest()) < 0) {
					((DefaultComboBoxModel) jComboBoxSize.getModel())
							.addElement(singleSymbolRuleList
									.getSizeBiggest());
				}
				ASUtil.selectOrInsert(jComboBoxSize,
						singleSymbolRuleList.getSizeBiggest());
				// jComboBoxSize.setSelectedItem(singleSymbolRuleList
				// .getSizeBiggest());
			}

			jComboBoxSize.setEnabled(enabled);
			jLabelSize.setEnabled(enabled);

			jComboBoxSize.setEnabled(singleSymbolRuleList.hasSize());
			jLabelSize.setEnabled(singleSymbolRuleList.hasSize());
		}

	};
	
	/**
	 * This method initializes jComboBox3
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxAngle() {
		if (jComboBoxRotation == null) {
			jComboBoxRotation = new JComboBox(new DefaultComboBoxModel(
					AbstractEditGUI.ROTATION_VALUES));
			jComboBoxRotation
					.setRenderer(AbstractEditGUI.ROTATION_VALUES_RENDERER);

			// // if (!Arrays.asList(AbstractEditGUI.ROTATION_VALUES).contains(
			// // singleSymbolRuleList.getRotation())) {
			// // // TODO Value not in list!
			// // // Need ASUtil.insertOrAdd for double, int etc..
			// // }
			//
			// jComboBoxRotation.setSelectedItem(singleSymbolRuleList
			// .getRotation());

			ASUtil.selectOrInsert(jComboBoxRotation, singleSymbolRuleList
					.getRotation());

			jComboBoxRotation.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						Double size = (Double) jComboBoxRotation
								.getSelectedItem();

						singleSymbolRuleList.setRotation(size);
						singleSymbolRuleList.fireEvents(new RuleChangedEvent(
								"rotation changed", singleSymbolRuleList));
						// firePropertyChange(PROPERTY_PREVIEW_UPDATE, null,
						// null);
					}

				}

			});

			singleSymbolRuleList.addListener(rotationComboBoxReactsToStyleChangeListener);

			ASUtil.addMouseWheelForCombobox(jComboBoxRotation);

			jComboBoxRotation.setEnabled(singleSymbolRuleList.hasRotation());
			jLabelRotation.setEnabled(singleSymbolRuleList.hasRotation());
		}
		return jComboBoxRotation;
	}


	RuleChangeListener rotationComboBoxReactsToStyleChangeListener = new RuleChangeListener() {

		public void changed(RuleChangedEvent e) {
			jComboBoxRotation.setEnabled(singleSymbolRuleList
					.hasRotation());
			jLabelRotation.setEnabled(singleSymbolRuleList
					.hasRotation());
			jComboBoxRotation.setSelectedItem(singleSymbolRuleList
					.getRotation());
		}

	};
	
	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonOK() {
		if (jButtonOK == null) {
			jButtonOK = new OkButton(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					SymbolSelectorGUI.this.firePropertyChange(PROPERTY_CLOSED,
							null, null);
					setVisible(false);
					dispose();
				}

			});
		}
		return jButtonOK;
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new CancelButton(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					cancel();
				}

			});
		}
		return jButtonCancel;
	}

	protected void cancel() {
		// TODO Make compliant with AtlasDialog

		SymbolSelectorGUI.this.firePropertyChange(PROPERTY_CANCEL_CHANGES,
				null, null);

		SymbolSelectorGUI.this.firePropertyChange(PROPERTY_CLOSED, null, null);
		setVisible(false);
		dispose();
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonProperties() {
		if (jButtonProperties == null) {
			jButtonProperties = new JButton(new AbstractAction(AtlasStyler
					.R("SymbolSelector.EditSymbol")) {

				public void actionPerformed(ActionEvent e) {
					
					/**
					 * Opens a modal dialog. TODO replace with DialogManager with RuleListe as a KEY
					 */
					SymbolEditorGUI symbolEditorGUI = new SymbolEditorGUI(
							SymbolSelectorGUI.this, singleSymbolRuleList);
					symbolEditorGUI.setModal(true);
					symbolEditorGUI.setVisible(true);
				}

			});
		}
		return jButtonProperties;
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonSave() {
		if (jButtonSave == null) {
			jButtonSave = new JButton(new AbstractAction(AtlasStyler
					.R("SymbolSelector.SaveToFile")) {

				public void actionPerformed(ActionEvent e) {

					if (singleSymbolRuleList.getSymbolizers().size() == 0) {
						JOptionPane
								.showMessageDialog(
										SymbolSelectorGUI.this,
										AtlasStyler
												.R("SymbolSelector.SaveToFileDialog.Error.NothingToSave"));
						return;
					}

					File dir = AtlasStyler.getSymbolsDir(singleSymbolRuleList
							.getGeometryDescriptor());
					JFileChooser chooser = new JFileChooser(dir);
					chooser.setDialogTitle(AtlasStyler
							.R("SymbolSelector.SaveToFileDialog.Title"));
					chooser.setDialogType(JFileChooser.SAVE_DIALOG);
					chooser.addChoosableFileFilter(new FileNameExtensionFilter(
							"SLD", "sld"));

					String styleName = singleSymbolRuleList.getStyleName();
					if (styleName != null)
						chooser.setSelectedFile(new File(dir, styleName));

					int i = chooser.showSaveDialog(SymbolSelectorGUI.this);
					if (i == JFileChooser.APPROVE_OPTION) {
						// TODO SwingWorker?!
						try {
							File selectedFile = chooser.getSelectedFile();
							if (selectedFile == null)
								return;

							if ((!selectedFile.getName().endsWith("sld"))
									&& (!selectedFile.getName().endsWith("SLD"))) {
								selectedFile = new File(selectedFile
										.getParentFile(), selectedFile
										.getName()
										+ ".sld");
							}

							/*******************************************
							 * Ask the user to enter AuthorName and Description
							 */
							String newerAuthor = ASUtil
									.askForString(
											SymbolSelectorGUI.this,
											singleSymbolRuleList
													.getStyleTitle(),
											AtlasStyler
													.R("SymbolSelector.SaveToFileDialog.Author"));
							if (newerAuthor == null)
								return;
							singleSymbolRuleList.setStyleTitle(newerAuthor);

							String newerDesc = ASUtil
									.askForString(
											SymbolSelectorGUI.this,
											singleSymbolRuleList
													.getStyleAbstract(),
											AtlasStyler
													.R("SymbolSelector.SaveToFileDialog.Desc"));
							if (newerDesc == null)
								return;
							singleSymbolRuleList.setStyleAbstract(newerDesc);

							singleSymbolRuleList.saveSymbolToFile(selectedFile);
						} catch (Exception e1) {
							LOGGER
									.error("Error saving symbol " + styleName,
											e1);
							JOptionPane
									.showMessageDialog(
											SymbolSelectorGUI.this,
											AtlasStyler
													.R("SymbolSelector.SaveToFileDialog.Error.FailedToSave")
													+ "\n"
													+ e1.getLocalizedMessage());
						}

						symbolsLocal.rescan(true);
					}
				}

			});
			jButtonSave.setToolTipText(AtlasStyler
					.R("SymbolSelector.SaveToFile.TT"));
		}
		return jButtonSave;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonReset() {
		if (jButtonReset == null) {
			jButtonReset = new JButton();
			jButtonReset.setText(AtlasStyler.R("SymbolSelector.Reset"));
			jButtonReset.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					if (lastSelectedRuleList == null
							|| singleSymbolRuleList == null)
						return;

					lastSelectedRuleList.copyTo(singleSymbolRuleList);

				}

			});

			jButtonReset.setEnabled(false);
			jButtonReset.setToolTipText(AtlasStyler
					.R("SymbolSelector.Reset.TT"));
		}
		return jButtonReset;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.anchor = GridBagConstraints.EAST;
			gridBagConstraints15.gridx = 2;
			gridBagConstraints15.gridy = 2;
			gridBagConstraints15.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints15.insets = new Insets(0, 5, 2, 5);
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.anchor = GridBagConstraints.WEST;
			gridBagConstraints14.insets = new Insets(0, 5, 2, 0);
			gridBagConstraints14.gridx = 1;
			gridBagConstraints14.gridy = 2;
			gridBagConstraints14.fill = GridBagConstraints.HORIZONTAL;
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.anchor = GridBagConstraints.EAST;
			gridBagConstraints13.gridx = 2;
			gridBagConstraints13.gridy = 1;
			gridBagConstraints13.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints13.insets = new Insets(5, 6, 5, 5);
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.anchor = GridBagConstraints.WEST;
			gridBagConstraints12.gridx = 1;
			gridBagConstraints12.gridy = 1;
			gridBagConstraints12.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints12.insets = new Insets(5, 5, 5, 0);
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.gridwidth = 3;
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.insets = new Insets(5, 5, 0, 5);
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getJButtonProperties(), gridBagConstraints2);
			jPanel.add(getJButtonSave(), gridBagConstraints12);
			jPanel.add(getJButtonReset(), gridBagConstraints13);
			jPanel.add(getJButtonOK(), gridBagConstraints14);
			jPanel.add(getJButtonCancel(), gridBagConstraints15);
		}
		return jPanel;
	}

	/**
	 * This method initializes jTabbedPane
	 * 
	 * @return javax.swing.JTabbedPane
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			/**
			 * All JScrollPaneSymbols fire PropertyChangeEvents when a symbol
			 * has been selected
			 */
			PropertyChangeListener listener = new PropertyChangeListener() {

				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals(
							JScrollPaneSymbols.PROPERTY_SYMBOL_SELECTED)) {

						/**
						 * Nasty bug was here: The old RuleChangeListeners have
						 * to be preserved
						 */

						lastSelectedRuleList = (SingleRuleList) evt
								.getNewValue();

						lastSelectedRuleList.copyTo(singleSymbolRuleList);

					}
				}
			};

			/**
			 * Local Symbols
			 */
			symbolsLocal = new JScrollPaneSymbolsLocal(singleSymbolRuleList
					.getGeometryDescriptor());
			symbolsLocal.addPropertyChangeListener(listener);
			jTabbedPane.addTab(symbolsLocal.getDesc(), symbolsLocal.getIcon(),
					symbolsLocal, symbolsLocal.getToolTip());

			symbolsOnline = new JScrollPaneSymbolsOnline(singleSymbolRuleList
					.getGeometryDescriptor());
			symbolsOnline.addPropertyChangeListener(listener);
			jTabbedPane.addTab(symbolsOnline.getDesc(),
					symbolsOnline.getIcon(), symbolsOnline, symbolsOnline
							.getToolTip());

		}
		return jTabbedPane;
	}

	public static void main(String[] args) {
		// Setting up the logger from a XML configuration file (actually the
		// file is in the av.jar)
		URL log4jXmlUrl = AtlasStyler.class.getResource("as_log4j.xml");
		DOMConfigurator.configure(log4jXmlUrl);

		SymbolSelectorGUI selectorGUI = new SymbolSelectorGUI(null, null,
				new SinglePolygonSymbolRuleList(""));
		selectorGUI.setVisible(true);
	}

}
