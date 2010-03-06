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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;

/**
 * Dialog to create and edit links.
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

class LinkDialog extends DialogShell implements ActionListener {

	/** table for link types: name -> type */
	private Hashtable linkTypes;

	/** table for link types: type -> name */
	private Hashtable linkTypeNames;

	/** cache for link address */
	private String addressCache = null;

	/** the document this dialog was constructed with */
	private Document doc;

	/** dialog components */
	private JComboBox linkStyle;
	private JComboBox linkType;
	private JTextField linkAddress;
	private JButton browseAddress;
	private JTextField linkAnchor;
	private JButton browseAnchor;
	private JTextField linkText;
	private JRadioButton showAsText;
	private JRadioButton showAsImage;
	private String linkImageFileName;
	private ImagePreview linkImage;
	private JButton setImage;
	private JTextField linkImgWidth;
	private JTextField linkImgHeight;
	private JPanel linkTextPanel;
	private JPanel linkImagePanel;

	/** some constants */
	private String LINK_TYPE_KEY = "linkType";
	private String LINK_TYPE_NAME_KEY = "linkTypeName";
	private String LINK_TYPE_RELATIVE_KEY = Util.getResourceString("linkType1");
	private String LINK_TYPE_NEWS_KEY = Util.getResourceString("linkType7");
	private String LINK_TYPE_MAILTO_KEY = Util.getResourceString("linkType8");
	private String LINK_TYPE_RELATIVE = Util.getResourceString("linkTypeName1");
	private String LINK_TYPE_LOCAL = Util.getResourceString("linkTypeName2");
	private String LINK_TYPE_NEWS = Util.getResourceString("linkTypeName7");
	private String LINK_TYPE_MAILTO = Util.getResourceString("linkTypeName8");

	/** indicates, whether or not action handlers should react on events */
	private boolean ignoreActions = false;

	/**
	 * the image directory for the document links are edited from in this dialog
	 */
	private File imgDir;

	/** the currently selected image file for this link */
	private String imgFile = null;

	/** the help id for this dialog */
	private static final String helpTopicId = "item164";

	// private int renderMode;

	/**
	 * construct a new LinkDialog
	 * 
	 * If the selection (selectionStart and selectionEnd) has an existing link,
	 * edit this link Create a link for the selected text otherwise.
	 * 
	 * @param parent
	 *            the parent frame for the dialog
	 * @param title
	 *            the dialog title
	 * @param doc
	 *            the document to edit link settings for
	 */
	public LinkDialog(Frame parent, String title, Document doc,
			int selectionStart, int selectionEnd, File imgDir/* , int renderMode */) {

		// initialize DialogShell
		super(parent, title, helpTopicId);

		// save document for later use
		this.doc = doc;
		this.imgDir = imgDir;
		// this.renderMode = renderMode;

		// layout and constraints to use later on
		GridBagLayout g = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		// create link style selector
		JPanel p = new JPanel(g);
		JLabel lb = new JLabel(Util.getResourceString("linkStyleLabel"));
		Util.addGridBagComponent(p, lb, g, c, 0, 0, GridBagConstraints.EAST);
		Vector styleNames = Util.getStyleNamesForTag(((SHTMLDocument) doc)
				.getStyleSheet(), HTML.Tag.A.toString());
		String standardStyleName = Util.getResourceString("standardStyleName");
		styleNames.insertElementAt(standardStyleName, 0);
		linkStyle = new JComboBox(styleNames);
		Util.addGridBagComponent(p, linkStyle, g, c, 1, 0,
				GridBagConstraints.WEST);

		// create link type selector
		lb = new JLabel(Util.getResourceString("linkTypeLabel"));
		Util.addGridBagComponent(p, lb, g, c, 0, 1, GridBagConstraints.EAST);
		buildLinkTypes();
		linkType = new JComboBox(linkTypeNames.values().toArray());
		linkType.addActionListener(this);
		Util.addGridBagComponent(p, linkType, g, c, 1, 1,
				GridBagConstraints.WEST);

		// create link address field
		lb = new JLabel(Util.getResourceString("linkAddressLabel"));
		Util.addGridBagComponent(p, lb, g, c, 0, 2, GridBagConstraints.EAST);
		linkAddress = new JTextField();
		linkAddress.setPreferredSize(new Dimension(300, 20));
		linkAddress.setMaximumSize(new Dimension(500, 20));
		linkAddress.addActionListener(this);
		Util.addGridBagComponent(p, linkAddress, g, c, 1, 2,
				GridBagConstraints.WEST, 2, 1, GridBagConstraints.HORIZONTAL,
				1, 0);
		browseAddress = new JButton(Util.getResourceString("linkBrowseLabel"));
		browseAddress.addActionListener(this);
		Util.addGridBagComponent(p, browseAddress, g, c, 3, 2,
				GridBagConstraints.WEST);

		// create link anchor field
		lb = new JLabel(Util.getResourceString("linkAnchorLabel"));
		Util.addGridBagComponent(p, lb, g, c, 0, 3, GridBagConstraints.EAST);
		linkAnchor = new JTextField();
		linkAnchor.setPreferredSize(new Dimension(150, 20));
		linkAnchor.setMaximumSize(new Dimension(500, 20));
		Util.addGridBagComponent(p, linkAnchor, g, c, 1, 3,
				GridBagConstraints.WEST, 1, 1, GridBagConstraints.HORIZONTAL,
				1, 0);
		browseAnchor = new JButton(Util.getResourceString("linkBrowseLabel"));
		browseAnchor.addActionListener(this);
		Util.addGridBagComponent(p, browseAnchor, g, c, 2, 3,
				GridBagConstraints.WEST);

		// create link display selector
		lb = new JLabel(Util.getResourceString("linkDisplayLabel"));
		Util.addGridBagComponent(p, lb, g, c, 0, 4, GridBagConstraints.EAST);
		showAsText = new JRadioButton(Util.getResourceString("showAsTextLabel"));
		showAsText.addActionListener(this);
		showAsImage = new JRadioButton(Util
				.getResourceString("showAsImageLabel"));
		showAsImage.addActionListener(this);
		JPanel helpPanel = new JPanel();
		helpPanel.add(showAsText);
		helpPanel.add(showAsImage);
		Util.addGridBagComponent(p, helpPanel, g, c, 1, 4,
				GridBagConstraints.WEST);
		ButtonGroup bg = new ButtonGroup();
		bg.add(showAsText);
		bg.add(showAsImage);

		// create link text panel
		linkTextPanel = new JPanel(new BorderLayout());
		linkTextPanel
				.setBorder(new TitledBorder(new EtchedBorder(
						EtchedBorder.LOWERED), Util
						.getResourceString("linkTextLabel")));
		linkText = new JTextField();
		linkText.setPreferredSize(new Dimension(400, 20));
		linkText.setMaximumSize(new Dimension(500, 20));
		linkTextPanel.add(linkText, BorderLayout.CENTER);
		Util.addGridBagComponent(p, linkTextPanel, g, c, 1, 5,
				GridBagConstraints.WEST, 2, 1, GridBagConstraints.HORIZONTAL,
				1, 0);
		// linkTextPanel.setVisible(false);

		// create link image panel
		linkImagePanel = new JPanel(new BorderLayout(5, 5));
		linkImagePanel
				.setBorder(new TitledBorder(new EtchedBorder(
						EtchedBorder.LOWERED), Util
						.getResourceString("linkImageLabel")));
		linkImage = new ImagePreview();
		linkImage.setPreferredSize(new Dimension(70, 70));
		linkImagePanel.add(new JScrollPane(linkImage), BorderLayout.CENTER);
		helpPanel = new JPanel(g);
		lb = new JLabel(Util.getResourceString("imgWidthLabel"));
		Util.addGridBagComponent(helpPanel, lb, g, c, 0, 0,
				GridBagConstraints.EAST);
		linkImgWidth = new JTextField();
		linkImgWidth.setPreferredSize(new Dimension(50, 20));
		linkImgWidth.setMinimumSize(new Dimension(50, 20));
		linkImgWidth.setEditable(false);
		Util.addGridBagComponent(helpPanel, linkImgWidth, g, c, 1, 0,
				GridBagConstraints.WEST);
		lb = new JLabel(Util.getResourceString("imgHeightLabel"));
		Util.addGridBagComponent(helpPanel, lb, g, c, 0, 1,
				GridBagConstraints.EAST);
		linkImgHeight = new JTextField();
		linkImgHeight.setPreferredSize(new Dimension(50, 20));
		linkImgHeight.setMinimumSize(new Dimension(50, 20));
		linkImgHeight.setEditable(false);
		Util.addGridBagComponent(helpPanel, linkImgHeight, g, c, 1, 1,
				GridBagConstraints.WEST);
		setImage = new JButton(Util.getResourceString("setImageLabel"));
		setImage.addActionListener(this);
		Util.addGridBagComponent(helpPanel, setImage, g, c, 1, 2,
				GridBagConstraints.WEST);
		JPanel helpPanel2 = new JPanel(new BorderLayout());
		helpPanel2.add(helpPanel, BorderLayout.NORTH);
		linkImagePanel.add(helpPanel2, BorderLayout.EAST);
		Util.addGridBagComponent(p, linkImagePanel, g, c, 1, 5,
				GridBagConstraints.WEST, 2, 1, GridBagConstraints.BOTH, 1, 1);

		// get content pane of DialogShell to add components to
		Container contentPane = super.getContentPane();

		// add panels to content pane of DialogShell
		contentPane.add(p, BorderLayout.CENTER);

		// cause optimal placement of all elements
		pack();

		// init dialog with existing link (if any)
		if (!setExistingLink(selectionStart, selectionEnd)) {
			setLinkText(selectionStart, selectionEnd);
		}

	}

	/**
	 * set the link text component of this dialog from the document this dialog
	 * is associated to
	 * 
	 * @param start
	 *            the start position of the link text in the document
	 * @param end
	 *            the end position of the link text in the document
	 */
	private void setLinkText(int start, int end) {
		try {
			linkText.setText(doc.getText(start, end - start));
			// System.out.println("showAsText = true");
			showAsText.setSelected(true);
			linkTextPanel.setVisible(true);
			linkImagePanel.setVisible(false);
		} catch (BadLocationException ble) {
			Util.errMsg(this, ble.getLocalizedMessage(), ble);
		}
	}

	/**
	 * set components of this dialog from an exisiting link in the associated
	 * document (if any).
	 * 
	 * @param selStart
	 *            the start position of the text currently selected in the
	 *            document
	 * @param selEnd
	 *            the end position of the text currently selected in the
	 *            document
	 * 
	 * @return ture, if a link was found, false if not
	 */
	private boolean setExistingLink(int selStart, int selEnd) {
		setIgnoreActions(true);
		Element e = Util.findLinkElementUp(((SHTMLDocument) doc)
				.getCharacterElement(selStart));
		boolean foundLink = (e != null);
		if (foundLink) {
			AttributeSet elemAttrs = e.getAttributes();
			Object linkAttr = elemAttrs.getAttribute(HTML.Tag.A);
			Object href = ((AttributeSet) linkAttr)
					.getAttribute(HTML.Attribute.HREF);
			if (href != null) {
				try {
					setDialogFromUrl(new URL(href.toString()));
				} catch (Exception ex) {
					setDialogFromRelative(href.toString());
				}
				Object img = elemAttrs.getAttribute(HTML.Attribute.SRC);
				if (img != null) {
					setLinkImage(img, elemAttrs);
				} else {
					setLinkText(e.getStartOffset(), e.getEndOffset());
				}
			}
		} else {
			linkType.setSelectedItem(LINK_TYPE_LOCAL);
			setLinkText(selStart, selEnd);
		}
		setIgnoreActions(false);
		return foundLink;
	}

	/**
	 * set the link image to be shown in this dialog from a given image file
	 * name and AttributeSet
	 * 
	 * @param imgAttr
	 *            the file name of the image to be shown
	 * @param attrSet
	 *            the set of attributes having width and height of the image (if
	 *            any)
	 */
	public void setLinkImage(Object imgAttr, AttributeSet attrSet) {
		String wStr = null;
		String hStr = null;
		if (imgAttr != null) {
			imgFile = Util.resolveRelativePath(imgAttr.toString(),
					((SHTMLDocument) doc).getBase().getPath()).replace(
					Util.URL_SEPARATOR_CHAR, File.separatorChar);
			while (imgFile.startsWith(File.separator)) {
				imgFile = imgFile.substring(1);
			}
		}
		Object width = attrSet.getAttribute(HTML.Attribute.WIDTH);
		if (width != null) {
			wStr = width.toString();
		}
		Object height = attrSet.getAttribute(HTML.Attribute.HEIGHT);
		if (height != null) {
			hStr = height.toString();
		}
		setImageSpecs(wStr, hStr);
		showAsImage.setSelected(true);
		linkTextPanel.setVisible(false);
		linkImagePanel.setVisible(true);
	}

	/**
	 * get the text to be displayed for the link
	 * 
	 * @return the link text
	 */
	public String getLinkText() {
		return linkText.getText();
	}

	/**
	 * get the style name (attribute 'class') to be used for a link
	 * 
	 * @return the style name
	 */
	public String getStyleName() {
		return linkStyle.getSelectedItem().toString();
	}

	/**
	 * set this dialog to ignore actions
	 * 
	 * @param ignore
	 *            indicator whether or not to ignore actions
	 */
	public void setIgnoreActions(boolean ignore) {
		ignoreActions = ignore;
	}

	/**
	 * set the components of this dialog from a given URL
	 * 
	 * @param url
	 *            the url to set link components from
	 */
	private void setDialogFromUrl(URL url) {
		if (url != null) {
			String protName;
			String protocol = url.getProtocol();
			if (protocol != null) {
				protName = (String) linkTypeNames.get(protocol);
			} else {
				protName = (String) linkTypeNames.get(LINK_TYPE_RELATIVE_KEY);
			}
			if (protName != null) {
				linkType.setSelectedItem(protName);
			}
			setLinkAddress(getPathFromUrl(url, protocol));
			linkAnchor.setText(url.getRef());
		}
	}

	/**
	 * extract the path from a URL
	 * 
	 * @param url
	 *            the url to get the path from
	 * @param protocol
	 *            the protocol of the url
	 * 
	 * @return the path of the URL
	 */
	private String getPathFromUrl(URL url, String protocol) {
		String path = "";
		String urlStr = url.toString();
		int pos = urlStr.indexOf(protocol);
		if (pos > -1) {
			path = urlStr.substring(protocol.length());
			while (path.startsWith(Util.URL_SEPARATOR)
					|| path.startsWith(Util.PROTOCOL_SEPARATOR)) {
				path = path.substring(1);
			}
		}
		pos = path.indexOf(Util.ANCHOR_SEPARATOR);
		if (pos > -1) {
			path = path.substring(0, pos);
		}
		return path;
	}

	/**
	 * set components of this dialog from a relative link path
	 * 
	 * @param hrefStr
	 *            the relative link to show in the dialog
	 */
	private void setDialogFromRelative(String hrefStr) {
		linkType.setSelectedItem(LINK_TYPE_RELATIVE);
		int pos = hrefStr.indexOf(Util.ANCHOR_SEPARATOR);
		if (pos > -1) {
			linkAnchor.setText(hrefStr.substring(pos + 1));
			hrefStr = hrefStr.substring(0, pos);
		}
		setLinkAddress(hrefStr);
	}

	/**
	 * build link type tables to match type names by types and vice versa
	 */
	private void buildLinkTypes() {
		String name;
		String type;
		linkTypes = new Hashtable(); // key = type name -> value = type
		linkTypeNames = new Hashtable(); // key = type -> value = type name
		for (int i = 1; i < 9; i++) {
			type = Util.getResourceString(LINK_TYPE_KEY + Integer.toString(i));
			name = Util.getResourceString(LINK_TYPE_NAME_KEY
					+ Integer.toString(i));
			linkTypes.put(name, type);
			linkTypeNames.put(type, name);
		}
	}

	/**
	 * get the chosen protocol
	 */
	private String getProtocol() {
		String prot = null;
		try {
			String protName = linkType.getSelectedItem().toString();
			if (!protName.equalsIgnoreCase(LINK_TYPE_RELATIVE)) {
				prot = transformProtocol(linkTypes.get(protName).toString());
			}
		} catch (Exception e) {
		}
		return prot;
	}

	/**
	 * transform a given protocol to be shown in the correct notation
	 */
	private String transformProtocol(String protName) {
		StringBuffer prot = new StringBuffer(protName);
		if (protName.equalsIgnoreCase(LINK_TYPE_MAILTO_KEY)
				|| protName.equalsIgnoreCase(LINK_TYPE_NEWS_KEY)) {
			prot.append(Util.PROTOCOL_SEPARATOR);
		} else {
			if (!protName.equalsIgnoreCase(LINK_TYPE_RELATIVE_KEY)) {
				prot.append(Util.PROTOCOL_SEPARATOR + Util.URL_SEPARATOR);
			}
		}
		return prot.toString();
	}

	/**
	 * get a file from a file chooser
	 * 
	 * @return the chosen file, or null, if none has been chosen or cancel has
	 *         benn pressed
	 */
	private File chooseFile() {
		File file = null;
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setSelectedFile(new File(((SHTMLDocument) doc).getBase()
				.getFile()));
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile();
		}
		return file;
	}

	/**
	 * set the address field of this <code>LinkDialog</code>
	 * 
	 * @param address
	 *            the address to be set
	 */
	private void setLinkAddress(String address) {
		addressCache = getLinkAddress();
		linkAddress.setText(address);
	}

	/**
	 * get a set of attributes to represent the link defined in this dialog
	 * 
	 * @return the set of attributes defining this link
	 */
	public AttributeSet getLinkAttribute() {
		SimpleAttributeSet aSet = new SimpleAttributeSet();
		aSet.addAttribute(HTML.Attribute.HREF, getHref());
		SimpleAttributeSet set = new SimpleAttributeSet();
		if (showAsImage.isSelected()) {
			SimpleAttributeSet imgSet = new SimpleAttributeSet();
			imgSet.addAttribute(HTML.Attribute.SRC, imgFile);
			set.addAttribute(HTML.Tag.IMG, imgSet);
		}
		set.addAttribute(HTML.Tag.A, aSet);
		return set;
	}

	/**
	 * get the file name of the image to be taken for the link defined in this
	 * dialog
	 * 
	 * @return the image file name
	 */
	public String getLinkImage() {
		if (showAsImage.isSelected()) {
			return imgFile;
		} else {
			return null;
		}
	}

	/**
	 * get the size of the image to be taken for the link defined in this dialog
	 * 
	 * @return the image size
	 */
	public Dimension getLinkImageSize() {
		if (showAsImage.isSelected()) {
			try {
				return new Dimension(Integer.parseInt(linkImgWidth.getText()),
						Integer.parseInt(linkImgHeight.getText()));
			} catch (Exception e) {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * get the currently selected link address
	 * 
	 * @return the link address
	 */
	public String getLinkAddress() {
		String link = linkAddress.getText();
		String prot = getProtocol();
		if (prot != null
				&& !prot
						.equalsIgnoreCase(transformProtocol(LINK_TYPE_RELATIVE_KEY))
				&& !prot
						.equalsIgnoreCase(transformProtocol(LINK_TYPE_MAILTO_KEY))
				&& !link.startsWith(Util.URL_SEPARATOR)) {
			link = Util.URL_SEPARATOR + link;
		}
		return link;
	}

	/**
	 * get the link reference
	 * 
	 * @return the object this link refers to
	 */
	public String getHref() {
		StringBuffer href = new StringBuffer();
		String prot = getProtocol();

		String linkAddrText = linkAddress.getText();
		String linkAnchText = linkAnchor.getText();

		if ((linkAddrText == null || linkAddrText.length() < 1)
				&& (linkAnchText != null && linkAnchText.length() > 0)) {
			// link to an anchor inside this document
			href.append(Util.ANCHOR_SEPARATOR);
			href.append(linkAnchText);
		} else {
			if (prot != null) {
				href.append(prot);
			}
			href.append(getLinkAddress());
			String anchor = linkAnchor.getText();
			if (anchor.length() > 0) {
				href.append(Util.ANCHOR_SEPARATOR);
				href.append(anchor);
			}
		}
		return href.toString();
	}

	/**
	 * get the file this link refers to (if any)
	 * 
	 * @return the file this link refers to, or null if no file is referenced
	 */
	private File getLinkedFile() {
		File file = null;
		try {
			String prot = linkType.getSelectedItem().toString();
			if (prot.equalsIgnoreCase(LINK_TYPE_LOCAL)) {
				file = new File(getLinkAddress().replace(
						Util.URL_SEPARATOR_CHAR, File.separatorChar));
			} else if (prot.equalsIgnoreCase(LINK_TYPE_RELATIVE)) {
				File from = new File(((SHTMLDocument) doc).getBase().getPath());
				String toStr = getLinkAddress();
				File to = new File(toStr);
				file = new File(Util.resolveRelativePath(getLinkAddress(),
						((SHTMLDocument) doc).getBase().getPath()));
			}
		} catch (Exception e) {
		}
		return file;
	}

	/**
	 * -------- ActionListener implementation start (including additional
	 * handling methods) ----------
	 */

	/**
	 * actionListener implementation to control dialog components
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (!ignoreActions) {
			Object src = e.getSource();
			if (src.equals(showAsText)) {
				linkTextPanel.setVisible(true);
				linkImagePanel.setVisible(false);
			} else if (src.equals(showAsImage)) {
				linkTextPanel.setVisible(false);
				linkImagePanel.setVisible(true);
			} else if (src.equals(browseAddress)) {
				File file = chooseFile();
				if (file != null) {
					setLinkAddress(file.getPath().replace(File.separatorChar,
							Util.URL_SEPARATOR_CHAR));
				}
			} else if (src.equals(linkType)) {
				handleLinkTypeAction();
			} else if (src.equals(browseAnchor)) {
				handleBrowseAnchorAction();
			} else if (src.equals(setImage)) {
				handleLinkImageAction();
			} else {
				super.actionPerformed(e);
			}
		}
	}

	/**
	 * handle an action performed by the component that allows selection of a
	 * link image
	 */
	private void handleLinkImageAction() {
		ImageDialog dlg = new ImageDialog(this, Util
				.getResourceString("imageDialogTitle"), imgDir);
		if (imgFile != null) {
			dlg.setImage(imgFile, linkImgWidth.getText(), linkImgHeight
					.getText());
		}
		Util.center(this, dlg);
		dlg.setModal(true);
		dlg.setVisible(true);

		/** if the user made a selection, apply it to the document */
		if (dlg.getResult() == DialogShell.RESULT_OK) {
			imgFile = Util.resolveRelativePath(dlg.getImageSrc(),
					((SHTMLDocument) doc).getBase().getPath()).replace(
					Util.URL_SEPARATOR_CHAR, File.separatorChar);
			while (imgFile.startsWith(File.separator)) {
				imgFile = imgFile.substring(1);
			}
			setImageSpecs(dlg.getImgWidth().toString(), dlg.getImgHeight()
					.toString());
		}
	}

	/**
	 * set the properties of the image to be shown for the link defined ni this
	 * dialog
	 * 
	 * @param width
	 *            image width
	 * @param height
	 *            image height
	 */
	private void setImageSpecs(String width, String height) {
		ImageIcon icon = new ImageIcon(imgFile);
		linkImage.setImage(icon);
		linkImage.setScale(100);
		if (width != null) {
			linkImgWidth.setText(width);
			linkImage.setPreviewWidth(Integer.parseInt(width));
		}
		if (height != null) {
			linkImgHeight.setText(height);
			linkImage.setPreviewHeight(Integer.parseInt(height));
		}
	}

	/**
	 * handle an action performed by the component that allows selection of a
	 * link protocol ('link type' on the GUI)
	 */
	private void handleLinkTypeAction() {
		String type = linkType.getSelectedItem().toString();
		browseAddress.setEnabled(type.equalsIgnoreCase(LINK_TYPE_LOCAL));
		browseAnchor.setEnabled(type.equalsIgnoreCase(LINK_TYPE_LOCAL)
				|| type.equalsIgnoreCase(LINK_TYPE_RELATIVE));
		if (type.equalsIgnoreCase(LINK_TYPE_RELATIVE)) {
			try {
				File from = new File(((SHTMLDocument) doc).getBase().getPath());
				String toStr = getLinkAddress();
				File to = new File(toStr);
				setLinkAddress(Util.getRelativePath(from, to));
			} catch (Exception ex) {
				Util.errMsg(this, null, ex);
			}
		} else if (type.equalsIgnoreCase(LINK_TYPE_LOCAL)) {
			try {
				String absPath = ((SHTMLDocument) doc).getBase().getFile()
						.substring(1);
				String relPath = getLinkAddress();
				setLinkAddress(Util.URL_SEPARATOR
						+ Util.resolveRelativePath(relPath, absPath));
			} catch (Exception ex) {
				Util.errMsg(this, ex.getMessage(), ex);
			}
		}
	}

	/**
	 * handle an action performed by the button used to browse anchors of a
	 * given file
	 */
	private void handleBrowseAnchorAction() {
		// System.out.println("LinkDialog actionPerformed browseAnchor file=" +
		// getLinkedFile().getAbsolutePath());
		try {
			AnchorDialog dlg;
			File file = getLinkedFile();
			String linkAddrText = linkAddress.getText();
			if (linkAddrText == null || linkAddrText.length() < 1) {
				dlg = new AnchorDialog(this, Util
						.getResourceString("anchorDialogTitle"), doc);
			} else {
				dlg = new AnchorDialog(this, Util
						.getResourceString("anchorDialogTitle"), file.toURI()
						.toURL());
			}
			Util.center(this, dlg);
			dlg.setModal(true);
			dlg.setVisible(true);
			if (dlg.getResult() == DialogShell.RESULT_OK) {
				linkAnchor.setText(dlg.getAnchor());
			}
		} catch (MalformedURLException ex) {
			Util.errMsg(this, ex.getMessage(), ex);
		}
	}

	/**
	 * -------- ActionListener implementation end (including additional handling
	 * methods) ----------
	 */

}
