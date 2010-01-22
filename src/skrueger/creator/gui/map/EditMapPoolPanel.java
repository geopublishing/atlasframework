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
package skrueger.creator.gui.map;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import skrueger.atlas.map.Map;
import skrueger.atlas.map.MapPool;
import skrueger.atlas.map.MapRef;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;
import skrueger.creator.GPDialogManager;
import skrueger.swing.SmallButton;

/**
 * This {@link JPanel} allows editing
 * 
 * @author Stefan Alfons Krüger
 * 
 */
public class EditMapPoolPanel extends JPanel {
	final static private Logger LOGGER = Logger
			.getLogger(EditMapPoolPanel.class);

	private final DraggableMapPoolJTable mapPoolJTable;

	private final MapPool mapPool;

	private final AtlasConfigEditable ace;

	/**
	 * Constructs a {@link EditMapPoolPanel} with the features to delete, edit
	 * an dcreate new {@link Map}s,
	 * 
	 * @param draggable
	 *            Shall the {@link Map}s be allowed to dragged by Drag'n'Drop
	 *            (as {@link MapRef}s)?
	 */
	public EditMapPoolPanel(AtlasConfigEditable atlasConfig_) {
		super(new BorderLayout());
		this.mapPool = atlasConfig_.getMapPool();
		this.ace = atlasConfig_;

		mapPoolJTable = new DraggableMapPoolJTable(ace);

		add(new JScrollPane(getMapPoolJTable()), BorderLayout.CENTER);
		add(getBottomPanel(), BorderLayout.SOUTH);

		// Add a listener for mouse click
		// Double-click on mapPoolJList opens its the DesignMapView to edit.
		getMapPoolJTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2
						&& !SwingUtilities.isRightMouseButton(evt)) {
					MapPoolJTable mpTable = (MapPoolJTable) evt.getSource();

					final Map map = mapPool.get(mpTable
							.convertRowIndexToModel(mpTable.rowAtPoint(evt
									.getPoint())));

					GPDialogManager.dm_MapComposer.getInstanceFor(map,
							EditMapPoolPanel.this, map);
				}
			}
		});
	}


	private JPanel getBottomPanel() {
		JPanel bottom = new JPanel(new MigLayout());
		
		bottom.add(new JLabel(AtlasCreator.R("EditMappoolPanel.Explanation")),"growx 200");
		
		JButton addButton = new SmallButton(new MapPoolAddAction(getMapPoolJTable()), AtlasCreator
				.R("MapPoolWindow.Button_AddMap_tt"));
		
		bottom.add(addButton,"top, right");

		return bottom;
	}

	/**
	 * @return The {@link MapPoolJTable} representing the {@link MapPool} in this {@link JPanel}
	 */
	public DraggableMapPoolJTable getMapPoolJTable() {
		return mapPoolJTable;
	}


}
