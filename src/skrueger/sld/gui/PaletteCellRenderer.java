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
package skrueger.sld.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.geotools.brewer.color.BrewerPalette;

public class PaletteCellRenderer extends JLabel implements ListCellRenderer {

	public PaletteCellRenderer() {
		setOpaque(true);
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		if (list.getModel().getSize() == 0) {
			return this;
		}

		Color background;
		Color foreground;

		// check if this cell represents the current DnD drop location
		JList.DropLocation dropLocation = list.getDropLocation();
		if (dropLocation != null && !dropLocation.isInsert()
				&& dropLocation.getIndex() == index) {

			background = Color.BLUE;
			foreground = Color.WHITE;

			// check if this cell is selected
		} else if (isSelected) {
			background = Color.RED;
			foreground = Color.WHITE;

			// unselected, and not the DnD drop location
		} else {
			background = Color.WHITE;
			foreground = Color.BLACK;
		}

		setBackground(background);
		setForeground(foreground);

		BrewerPalette a = (BrewerPalette) value;
		if (a == null) {
			this.setText("too many classes");
			return this;
		} else {
			this.setText("");
		}

		setIcon(new ImageIcon(palette(a.getColors())));

		return this;
	}

	/**
	 * Icon for grid data, small grid made up of provided colors. Layout:
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *    0 1 2 3 4 5 6 7 8 9 101112131415
	 *  0  
	 *  1 AABBCDEEFfGgHhIiJjKkllmmnnoopp           
	 *  2 AABBCDEEFfGgHhIiJjKkllmmnnoopp
	 *  3 AABBCDEEFfGgHhIiJjKkllmmnnoopp                 
	 *  4 AABBCDEEFfGgHhIiJjKkllmmnnoopp
	 *  5 AABBCDEEFfGgHhIiJjKkllmmnnoopp
	 *  6 AABBCDEEFfGgHhIiJjKkllmmnnoopp
	 *  7 AABBCDEEFfGgHhIiJjKkllmmnnoopp
	 *  8 AABBCDEEFfGgHhIiJjKkllmmnnoopp
	 *  9 AABBCDEEFfGgHhIiJjKkllmmnnoopp
	 * 10 AABBCDEEFfGgHhIiJjKkllmmnnoopp
	 * 11 AABBCDEEFfGgHhIiJjKkllmmnnoopp
	 * 12 AABBCDEEFfGgHhIiJjKkllmmnnoopp
	 * 14 AABBCDEEFfGgHhIiJjKkllmmnnoopp
	 * 15
	 * &lt;/code&gt;
	 * &lt;pre&gt;
	 * &lt;/p&gt;
	 * &#064;param c palette of colors
	 * @return Icon representing a palette
	 * 
	 */
	public static Image palette(Color c[]) {
		int WIDTH = 130;
		int HEIGHT = 13;

		final Color[] colors = new Color[WIDTH];
		Color color = Color.GRAY;
		if (c == null) {
			for (int i = 0; i < WIDTH; i++)
				color = Color.GRAY;
		} else {
			for (int i = 0; i < WIDTH; i++) {
				int lookup = (i * c.length) / WIDTH;
				if (c[lookup] != null)
					color = c[lookup];
				colors[i] = color;
			}
		}

		BufferedImage swtImage = new BufferedImage(WIDTH, HEIGHT,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D gc = swtImage.createGraphics();
		Color swtColor = null;

		for (int i = 0; i < WIDTH; i++) {
			swtColor = colors[i];
			gc.setColor(swtColor);
			gc.drawLine(i, 0, i, HEIGHT);
		}
		gc.setColor(Color.GRAY);
		gc.drawRect(0, 0, WIDTH, HEIGHT);

		return swtImage;
	}
}
