package org.geopublishing.geopublisher.gui.group;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.geopublishing.atlasViewer.GpCoreUtil;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpEntryType;
import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.dp.Group;
import org.geopublishing.atlasViewer.map.MapRef;
import org.geopublishing.atlasViewer.swing.Icons;

import de.schmitzm.jfree.chart.style.ChartStyle;

public class GroupTreeCellRenderer extends DefaultTreeCellRenderer {

	@Override
	public Component getTreeCellRendererComponent(final JTree tree,
			final Object value, final boolean sel, final boolean expanded,
			final boolean leaf, final int row, final boolean hasFocus) {

		final Component fromSuper = super.getTreeCellRendererComponent(tree,
				value, sel, expanded, leaf, row, hasFocus);

		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		if (leaf) {

			if (node instanceof MapRef) {
				// MapRef mapRef = (MapRef)unknown;
				setIcon(Icons.ICON_MAP_SMALL);
			} else if (node instanceof DpRef<?>) {
				final DpRef<? extends DpEntry<? extends ChartStyle>> dpr = (DpRef<?>) node;
				DpEntry<? extends ChartStyle> target = dpr.getTarget();
				if (target == null)
					setIcon(DpEntryType.UNKNOWN.getIconSmall());
				else
					setIcon(target.getType().getIconSmall());
			}
		} else {
			if (node instanceof Group) {
				/**
				 * Depending on the QM value, we paint some red/green color over
				 * the icon.
				 */
				final Group group2 = (Group) node;
				final Double d = group2.getQuality();
				final Float green = d.floatValue();
				final Float red = 1f - d.floatValue();
				final Color c = new Color(red, green, 0.1f);

				final Image im = new BufferedImage(16, 16,
						BufferedImage.TYPE_INT_RGB);
				final Graphics2D g2 = (Graphics2D) im.getGraphics();
				g2.setColor(getBackground());
				g2.fillRect(0, 0, 16, 16);

				if (getIcon() != null)
					getIcon().paintIcon(this, g2, 0, 0);

				g2.setColor(c);
				g2.fillRect(0, 1, 4, 15);

				setIcon(new ImageIcon(im));

				/**
				 * Mark the Group if it is a special Group
				 */
				if (group2.isFileMenu()) {
					setText(getText() + " (<-"
							+ GpCoreUtil.R("AtlasViewer.FileMenu") + ")");
				}
				if (group2.isHelpMenu()) {
					setText(getText() + " (<-"
							+ GpCoreUtil.R("AtlasViewer.HelpMenu") + ")");
				}

			}
		}

		return fromSuper;
	}

}