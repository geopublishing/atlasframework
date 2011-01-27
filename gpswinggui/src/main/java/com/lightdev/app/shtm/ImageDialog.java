/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002 Ulrich Hilger
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.lightdev.app.shtm;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.io.FileUtils;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.geopublishing.geopublisher.swing.GpSwingUtil;

import com.sun.demo.ExampleFileFilter;

import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.SwingUtil;

/**
 * A dialog providing an image repository and a way to edit display options for
 * images from the repository.
 * 
 * @author Ulrich Hilger
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the GNU General Public
 *         License, for details see file gpl.txt in the distribution package of
 *         this software
 * 
 * 
 */

class ImageDialog extends DialogShell implements ActionListener,
		ListSelectionListener, ChangeListener {
	/** directory this ImageDialog maintains */
	private File imgDir;

	/** KeyListener for watching changes in the scale text field */
	private KeyHandler keyHandler = new KeyHandler();

	/** FocusListener for watching changes in the scale text field */
	private FocusHandler focusHandler = new FocusHandler();

	private SimpleAttributeSet originalAttributes = new SimpleAttributeSet();

	/**
	 * indicates whether or not changes in a SizeSelectorPanel are to be
	 * processed. Usually, changes caused by a method of this class are to be
	 * ignored
	 */
	private boolean ignoreChangeEvents = false;

	/** list with images in this image repository */
	private JList imgFileList;

	/** button to add an image file to the repository */
	private JButton addImgBtn;

	/** button to delete an image file from the repository */
	private JButton delImgBtn;

	/** text field for manipulating the scale of an image */
	private JTextField scale;

	/** component to manipulate the image width */
	private SizeSelectorPanel imgWidth;

	/** component to manipulate the image height */
	private SizeSelectorPanel imgHeight;

	/** component to display the original width of an image */
	private JLabel oWidth;

	/** component to display the original height of an image */
	private JLabel oHeight;

	/** component to preview an image */
	private ImagePreview preview;

	/** component to scroll an image inside the preview */
	private JScrollPane scPrev;

	/**
	 * contains all components having attributes for the image represented in
	 * this <code>ImageDialog</code>
	 */
	private Vector attributeComponents = new Vector();

	/** the help id for this dialog */
	private static final String helpTopicId = "item166";

	/** the document the image came from, if any */
	private SHTMLDocument doc;

	/**
	 * construct a new ImageDialog
	 * 
	 * @param parent
	 *            the parent frame of this ImageDialog
	 * @param title
	 *            the title of this ImageDialog
	 * @param imgDir
	 *            the directory of the image repository
	 */
	public ImageDialog(Dialog parent, String title, File imgDir) {
		super(parent, title, helpTopicId);
		initDialog(title, imgDir);
	}

	/**
	 * construct a new ImageDialog
	 * 
	 * @param parent
	 *            the parent frame of this ImageDialog
	 * @param title
	 *            the title of this ImageDialog
	 * @param imgDir
	 *            the directory of the image repository
	 */
	public ImageDialog(Frame parent, String title, File imgDir) {
		super(parent, title, helpTopicId);
		initDialog(title, imgDir);
	}

	public ImageDialog(Frame parent, String title, File imgDir,
			SHTMLDocument sourceDoc) {
		super(parent, title, helpTopicId);
		this.doc = sourceDoc;
		initDialog(title, imgDir);
	}

	/**
	 * build the dialog contents after construction
	 * 
	 * @param title
	 *            the title of this ImageDialog
	 * @param imgDir
	 *            the directory of the image repository
	 */
	private void initDialog(String title, File imgDir) {

		// System.out.println("ImageDialog.initDialog imgDir=" +
		// imgDir.getAbsolutePath());

		this.imgDir = imgDir;

		Dimension dim;

		// create an image directory panel
		JPanel dirPanel = new JPanel(new BorderLayout());
		dirPanel.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED), Util
				.getResourceString("imgDirPanelTitle")));

		// create a list to disply image files in
		imgFileList = new JList();
		dim = new Dimension(100, 100);
		imgFileList.setMinimumSize(dim);
		imgFileList.setPreferredSize(dim);
		imgFileList.addListSelectionListener(this);
		updateFileList();

		// create a panel with action buttons for image files
		JPanel dirBtnPanel = new JPanel();

		// create image directory action buttons
		addImgBtn = new JButton(Util.getResourceString("addImgBtnTitle"));
		addImgBtn.addActionListener(this);
		delImgBtn = new JButton(Util.getResourceString("delImgBtnTitle"));
		delImgBtn.addActionListener(this);

		// add action buttons to button panel
		dirBtnPanel.add(addImgBtn);
		dirBtnPanel.add(delImgBtn);

		// add components to image directory panel
		dirPanel.add(imgFileList, BorderLayout.CENTER);
		dirPanel.add(dirBtnPanel, BorderLayout.SOUTH);

		// create an image preview panel
		JPanel previewPanel = new JPanel(new BorderLayout());
		previewPanel.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED), Util
				.getResourceString("imgPreviewPanelTitle")));

		// add a new ImagePreview object to the preview panel
		preview = new ImagePreview();
		dim = new Dimension(250, 250);
		preview.setMinimumSize(dim);
		preview.setPreferredSize(dim);
		scPrev = new JScrollPane(preview);
		previewPanel.add(scPrev, BorderLayout.CENTER);

		// layout and constraints to use later on
		GridBagLayout g = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		// create an image properties panel
		JPanel eastPanel = new JPanel(new BorderLayout());
		JPanel propertiesPanel = new JPanel(g);
		eastPanel.add(propertiesPanel, BorderLayout.NORTH);
		eastPanel.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED), Util
				.getResourceString("imgPropertiesPanelTitle")));

		// add scale component
		Util.addGridBagComponent(propertiesPanel, new JLabel(Util
				.getResourceString("imgScaleLabel")), g, c, 0, 0,
				GridBagConstraints.EAST);
		scale = new JTextField();
		scale.addKeyListener(keyHandler);
		scale.addFocusListener(focusHandler);
		dim = new Dimension(50, 20);
		scale.setMinimumSize(dim);
		scale.setPreferredSize(dim);
		JPanel helperPanel = new JPanel();
		helperPanel.add(scale);
		helperPanel.add(new JLabel(SizeSelectorPanel.UNIT_PERCENT,
				SwingConstants.LEFT));
		Util.addGridBagComponent(propertiesPanel, helperPanel, g, c, 1, 0,
				GridBagConstraints.WEST);

		// add width component
		Util.addGridBagComponent(propertiesPanel, new JLabel(Util
				.getResourceString("imgWidthLabel")), g, c, 0, 1,
				GridBagConstraints.EAST);
		imgWidth = new SizeSelectorPanel(HTML.Attribute.WIDTH, null, false,
				SizeSelectorPanel.TYPE_LABEL);
		attributeComponents.addElement(imgWidth);
		imgWidth.getValueSelector().addChangeListener(this);
		Util.addGridBagComponent(propertiesPanel, imgWidth, g, c, 1, 1,
				GridBagConstraints.WEST);

		// add height component
		Util.addGridBagComponent(propertiesPanel, new JLabel(Util
				.getResourceString("imgHeightLabel")), g, c, 0, 2,
				GridBagConstraints.EAST);
		imgHeight = new SizeSelectorPanel(HTML.Attribute.HEIGHT, null, false,
				SizeSelectorPanel.TYPE_LABEL);
		attributeComponents.addElement(imgHeight);
		imgHeight.getValueSelector().addChangeListener(this);
		Util.addGridBagComponent(propertiesPanel, imgHeight, g, c, 1, 2,
				GridBagConstraints.WEST);

		// add hspace component
		Util.addGridBagComponent(propertiesPanel, new JLabel(Util
				.getResourceString("imgHSpaceLabel")), g, c, 0, 3,
				GridBagConstraints.EAST);
		SizeSelectorPanel hSpace = new SizeSelectorPanel(HTML.Attribute.HSPACE,
				null, false, SizeSelectorPanel.TYPE_LABEL);
		attributeComponents.addElement(hSpace);
		Util.addGridBagComponent(propertiesPanel, hSpace, g, c, 1, 3,
				GridBagConstraints.WEST);

		// add vspace component
		Util.addGridBagComponent(propertiesPanel, new JLabel(Util
				.getResourceString("imgVSpaceLabel")), g, c, 0, 4,
				GridBagConstraints.EAST);
		SizeSelectorPanel vSpace = new SizeSelectorPanel(HTML.Attribute.VSPACE,
				null, false, SizeSelectorPanel.TYPE_LABEL);
		attributeComponents.addElement(vSpace);
		Util.addGridBagComponent(propertiesPanel, vSpace, g, c, 1, 4,
				GridBagConstraints.WEST);

		// add alignment component
		Util.addGridBagComponent(propertiesPanel, new JLabel(Util
				.getResourceString("imgAlignLabel")), g, c, 0, 5,
				GridBagConstraints.EAST);
		String[] items = new String[] { Util.getResourceString("imgAlignTop"),
				Util.getResourceString("imgAlignMiddle"),
				Util.getResourceString("imgAlignBottom"),
				Util.getResourceString("imgAlignLeft"),
				Util.getResourceString("imgAlignCenter"),
				Util.getResourceString("imgAlignRight") };
		String[] names = new String[] { "top", "middle", "bottom", "left",
				"center", "right" };
		AttributeComboBox imgAlign = new AttributeComboBox(items, names, null,
				HTML.Attribute.ALIGN);
		attributeComponents.addElement(imgAlign);
		Util.addGridBagComponent(propertiesPanel, imgAlign, g, c, 1, 5,
				GridBagConstraints.WEST);

		// add original width component
		Util.addGridBagComponent(propertiesPanel, new JLabel(Util
				.getResourceString("oWidthLabel")), g, c, 0, 6,
				GridBagConstraints.EAST);
		oWidth = new JLabel("");
		Util.addGridBagComponent(propertiesPanel, oWidth, g, c, 1, 6,
				GridBagConstraints.WEST);

		// add original height component
		Util.addGridBagComponent(propertiesPanel, new JLabel(Util
				.getResourceString("oHeightLabel")), g, c, 0, 7,
				GridBagConstraints.EAST);
		oHeight = new JLabel("");
		Util.addGridBagComponent(propertiesPanel, oHeight, g, c, 1, 7,
				GridBagConstraints.WEST);

		// add border component
		Util.addGridBagComponent(propertiesPanel, new JLabel(Util
				.getResourceString("imgBorderLabel")), g, c, 0, 8,
				GridBagConstraints.EAST);
		SizeSelectorPanel imgBorder = new SizeSelectorPanel(
				HTML.Attribute.BORDER, null, false,
				SizeSelectorPanel.TYPE_LABEL);
		attributeComponents.addElement(imgBorder);
		Util.addGridBagComponent(propertiesPanel, imgBorder, g, c, 1, 8,
				GridBagConstraints.WEST);

		// add to content pane of DialogShell
		Container contentPane = super.getContentPane();
		contentPane.add(dirPanel, BorderLayout.WEST);
		contentPane.add(previewPanel, BorderLayout.CENTER);
		contentPane.add(eastPanel, BorderLayout.EAST);

		// cause optimal placement of all elements
		pack();

		scPrev.addComponentListener(new ResizeListener());
	}

	public Integer getImgWidth() {
		return imgWidth.getIntValue();
	}

	public Integer getImgHeight() {
		return imgHeight.getIntValue();
	}

	/**
	 * set dialog content from a given set of image attributes
	 * 
	 * @param a
	 *            the set of attributes to set dialog contents from
	 */
	public void setImageAttributes(AttributeSet a) {
		// System.out.println("ImageDialog.setImageAttributes");
		ignoreChangeEvents = true;
		originalAttributes.addAttributes(a);
		if (a.isDefined(HTML.Attribute.SRC)) {
			File imgFile = null;
			if (doc != null) {
				imgFile = new File(Util
						.resolveRelativePath(a.getAttribute(HTML.Attribute.SRC)
								.toString(), doc.getBase().getFile()));
			} else {
				imgFile = new File(a.getAttribute(HTML.Attribute.SRC)
						.toString());
			}
			// System.out.println("ImageDialog.setImageAttribute imgFile=" +
			// imgFile.getAbsolutePath());
			imgFileList.setSelectedValue(imgFile.getName().toLowerCase(), true);
		}
		for (int i = 0; i < attributeComponents.size(); i++) {
			((AttributeComponent) attributeComponents.get(i)).setValue(a);
		}
		if (a.isDefined(HTML.Attribute.WIDTH)) {
			preview.setPreviewWidth(Integer.parseInt(a.getAttribute(
					HTML.Attribute.WIDTH).toString()));
		}
		if (a.isDefined(HTML.Attribute.HEIGHT)) {
			preview.setPreviewHeight(Integer.parseInt(a.getAttribute(
					HTML.Attribute.HEIGHT).toString()));
		}
		int scalePct = preview.getScale();
		scale.setText(Integer.toString(scalePct));
		ignoreChangeEvents = false;
	}

	public void setImage(String fName, String w, String h) {
		// System.out.println("ImageDialog.setImage fName=" + fName);
		imgFileList.setSelectedValue(new File(fName).getName(), true);
		preview.setImage(new ImageIcon(fName));
		try {
			if (w != null && w.length() > 0) {
				preview.setPreviewWidth(Integer.parseInt(w));
			}
			if (h != null && h.length() > 0) {
				preview.setPreviewHeight(Integer.parseInt(h));
			}
		} catch (Exception e) {
			Util.errMsg(this, null, e);
		}
	}

	/**
	 * get the HTML representing the image selected in this
	 * <code>ImageDialog</code>
	 */
	public String getImageHTML() {
		SimpleAttributeSet set = new SimpleAttributeSet(originalAttributes);
		StringWriter sw = new StringWriter();
		SHTMLWriter w = new SHTMLWriter(sw, doc == null ? new HTMLDocument()
				: doc);
		for (int i = 0; i < attributeComponents.size(); i++) {
			set.addAttributes(((AttributeComponent) attributeComponents.get(i))
					.getValue());
		}
		set.addAttribute(HTML.Attribute.SRC, getImageSrc());
		try {
			w.writeStartTag(HTML.Tag.IMG.toString(), set);
		} catch (Exception e) {
			Util.errMsg(this, e.getMessage(), e);
		}
		return sw.getBuffer().toString();
	}

	/**
	 * get the value for the SRC attribute of an image tag
	 * 
	 * @return the value of the SRC attribute of an image tag
	 */
	public String getImageSrc() {
		StringBuffer buf = new StringBuffer();
		Object value = imgFileList.getSelectedValue();
		if (value != null) {
			buf.append(SHTMLPanelImpl.IMAGE_DIR);
			buf.append(Util.URL_SEPARATOR);
			buf.append(value.toString());
		}
		return buf.toString();
	}

	/**
	 * handle the event when the user pressed the 'Add...' button to add a new
	 * image to the repository
	 */
	private void handleAddImage() {
		try {
			JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(true);
			ExampleFileFilter filter = new ExampleFileFilter();
			filter.addExtension("gif");
			filter.addExtension("jpg");
			filter.addExtension("jpeg");
			filter.addExtension("png");
			filter.setDescription(Util.getResourceString("imageFileDesc"));
			chooser.setFileFilter(filter);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File[] sFiles = chooser.getSelectedFiles();
				if (!imgDir.exists()) {
					imgDir.mkdirs();
				}
				for (int i = 0; i < sFiles.length; i++) {

					/**
					 * Added by SK: normalize image file name with
					 * user-interaction
					 */
					String newName = GpSwingUtil.cleanFilenameWithUI(
							ImageDialog.this, sFiles[i].getName());
					if (newName == null)
						continue;

					File destFile = new File(imgDir, newName);

					if (destFile.exists()) {
						if (!SwingUtil
								.askYesNo(
										ImageDialog.this,
										GeopublisherGUI
												.R("SHTML.ImageDialog.ImportImagesFileExistsAndWillBeReplaced"))) {
							continue;
						} else {
							if (!destFile.delete()) {
								ExceptionDialog.show(ImageDialog.this,
										new IOException(
												"Can't replace the image?!"));
								continue;
							}
						}
					}

					// XXX
					FileUtils.copyFile(sFiles[i], destFile);

					updateFileList();
				}
			}
		} catch (Exception e) {
			Util.errMsg(this, e.getMessage(), e);
		}
	}

	/**
	 * handle the event occurring when the user pressed the 'Delete' button to
	 * remove an image from the repository
	 */
	private void handleDeleteImage() {
		String fName = imgFileList.getSelectedValue().toString();
		if (Util.msg(JOptionPane.YES_NO_OPTION, "confirmDelete",
				"deleteFileQuery", fName, "\r\n")) {
			File delFile = new File(imgDir.getAbsolutePath() + File.separator
					+ fName);
			delFile.delete();
			updateFileList();
		}
	}

	/**
	 * display all files found in the image directory
	 */
	private void updateFileList() {
		if (imgDir != null && imgFileList != null) {
			String[] files = imgDir.list();
			if (files != null && files.length > 0) {
				for (int i = 0; i < files.length; i++) {
					if (!new File(imgDir, files[i]).isDirectory()) {
						files[i] = files[i].toLowerCase();
					}
				}
				imgFileList.setListData(files);
			}
		}
	}

	/**
	 * update all image property displays to the current setting
	 */
	private void updateControls() {
		ignoreChangeEvents = true;
		int scalePct = preview.getScale();
		SimpleAttributeSet set = new SimpleAttributeSet();
		oWidth.setText(Integer.toString(preview.getOriginalWidth()));
		oHeight.setText(Integer.toString(preview.getOriginalHeight()));
		// System.out.println("updateControls origW=" +
		// preview.getOriginalWidth());
		// System.out.println("updateControls add WIDTH attr as " +
		// Integer.toString(
		// preview.getOriginalWidth() * scalePct / 100) +
		// SizeSelectorPanel.UNIT_PT);
		set.addAttribute(HTML.Attribute.WIDTH, Integer.toString(preview
				.getOriginalWidth()
				* scalePct / 100)
				+ SizeSelectorPanel.UNIT_PT);
		set.addAttribute(HTML.Attribute.HEIGHT, Integer.toString(preview
				.getOriginalHeight()
				* scalePct / 100)
				+ SizeSelectorPanel.UNIT_PT);
		imgWidth.setValue(set);
		imgHeight.setValue(set);
		scale.setText(Integer.toString(scalePct));
		ignoreChangeEvents = false;
	}

	/**
	 * apply a scale set by the user through respective text field and update
	 * all related image property displays
	 */
	private void applyPreviewScale() {
		// System.out.println("applyPreviewScale scale=" + scale.getText());
		ignoreChangeEvents = true;
		try {
			preview.setScale(Integer.parseInt(scale.getText()));
			updateControls();
		} catch (Exception e) {
		}
		ignoreChangeEvents = false;
	}

	/**
	 * apply a new width set by the user and update all related image property
	 * displays
	 */
	private void applyPreviewWidth() {
		// System.out.println("applyPreviewWidth width=" +
		// imgWidth.getIntValue().intValue());
		ignoreChangeEvents = true;
		preview.setPreviewWidth(imgWidth.getIntValue().intValue());
		int scalePct = preview.getScale();
		// System.out.println("applyPreviewWidth scale now " + scalePct);
		SimpleAttributeSet set = new SimpleAttributeSet();
		scale.setText(Integer.toString(scalePct));
		set.addAttribute(HTML.Attribute.HEIGHT, Integer.toString(preview
				.getOriginalHeight()
				* scalePct / 100)
				+ SizeSelectorPanel.UNIT_PT);
		// System.out.println("applyPreviewWidth, changing height to " +
		// Integer.toString(
		// preview.getOriginalHeight() * scalePct / 100) +
		// SizeSelectorPanel.UNIT_PT);
		imgHeight.setValue(set);
		ignoreChangeEvents = false;
	}

	/**
	 * apply a new height set by the user and update all related image property
	 * displays
	 */
	private void applyPreviewHeight() {
		// System.out.println("applyPreviewHeight height=" +
		// imgHeight.getIntValue().intValue());
		ignoreChangeEvents = true;
		preview.setPreviewHeight(imgHeight.getIntValue().intValue());
		int scalePct = preview.getScale();
		// System.out.println("applyPreviewHeight scale now " + scalePct);
		SimpleAttributeSet set = new SimpleAttributeSet();
		scale.setText(Integer.toString(scalePct));
		set.addAttribute(HTML.Attribute.WIDTH, Integer.toString(preview
				.getOriginalWidth()
				* scalePct / 100)
				+ SizeSelectorPanel.UNIT_PT);
		// System.out.println("applyPreviewHeight, changing width to " +
		// Integer.toString(
		// preview.getOriginalWidth() * scalePct / 100) +
		// SizeSelectorPanel.UNIT_PT);
		imgWidth.setValue(set);
		ignoreChangeEvents = false;
	}

	/* ---------------- event handling start ------------------------- */

	/**
	 * implements the ActionListener interface to be notified of clicks onto the
	 * file repository buttons.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == addImgBtn) {
			handleAddImage();
		} else if (src == delImgBtn) {
			handleDeleteImage();
		} else {
			super.actionPerformed(e);
		}
	}

	/**
	 * Listener for changes in the image list.
	 * 
	 * <p>
	 * updates the image preview and property displays according to the current
	 * selection (if any)
	 * </p>
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!imgFileList.isSelectionEmpty()) {
			preview.setImage(new ImageIcon(imgDir.getAbsolutePath()
					+ File.separator
					+ imgFileList.getSelectedValue().toString()));
			updateControls();
		} else {
			preview.setImage(null);
			int vWidth = scPrev.getWidth() - 5;
			int vHeight = scPrev.getHeight() - 5;
			preview.setPreferredSize(new Dimension(vWidth, vHeight));
			preview.revalidate();
		}
	}

	/**
	 * Listener for resize events.
	 * 
	 * <p>
	 * used on the JScrollPane holding the image preview to adjust the preview
	 * to size changes and to synchronize property displays accordingly.
	 * </p>
	 */
	private class ResizeListener extends ComponentAdapter {
		@Override
		public void componentResized(ComponentEvent e) {
			int vWidth = scPrev.getWidth() - 5;
			int vHeight = scPrev.getHeight() - 5;
			preview.setPreferredSize(new Dimension(vWidth, vHeight));
			preview.revalidate();
			updateControls();
		}
	}

	/**
	 * Listener for key events
	 * 
	 * <p>
	 * Used to adjust preview properties according to user settings in the scale
	 * text field
	 * </p>
	 */
	private class KeyHandler extends KeyAdapter {
		@Override
		public void keyReleased(KeyEvent e) {
			Object source = e.getSource();
			int keyCode = e.getKeyCode();
			if (source.equals(scale)) {
				if (keyCode == KeyEvent.VK_ENTER) {
					applyPreviewScale();
				}
			}
		}
	}

	/**
	 * Listener for focus events
	 * 
	 * <p>
	 * Used to adjust preview properties according to user settings in the scale
	 * text field
	 * </p>
	 */
	private class FocusHandler extends FocusAdapter {
		@Override
		public void focusLost(FocusEvent e) {
			Object source = e.getSource();
			if (source.equals(scale)) {
				applyPreviewScale();
			}
		}
	}

	/**
	 * Listener for change events
	 * 
	 * <p>
	 * Used to adjust preview properties according to user settings in
	 * SizeSelectorPanels
	 * </p>
	 */
	public void stateChanged(ChangeEvent e) {
		if (!ignoreChangeEvents) {
			Object source = e.getSource();
			if (source.equals(imgWidth.getValueSelector())) {
				applyPreviewWidth();
			} else if (source.equals(imgHeight.getValueSelector())) {
				applyPreviewHeight();
			}
		}
	}

	/* ---------------- event handling end ------------------------- */

}
