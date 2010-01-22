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
package skrueger.creator;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import skrueger.atlas.dp.DataPool.EventTypes;
import skrueger.atlas.gui.internal.AtlasTask;

/**
 * When this action is performed, it shows a progress bar while the atlas is
 * uncached and reread.
 * 
 */
public class UncacheAtlasAction extends AbstractAction {

	private final Component parentGUI;
	private final AtlasConfigEditable ace;

	public UncacheAtlasAction(Component parentGUI, AtlasConfigEditable ace) {
		super(AtlasCreator.R("MenuBar.OptionsMenu.ClearCaches"), new ImageIcon(GPProps.class
				.getResource("resource/uncache.png")));
		this.parentGUI = parentGUI;
		this.ace = ace;
	}

	/**
	 * All cached information will be dropped and the atlas will be reread (will
	 * it really ?)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		final AtlasTask<Object> uncacheTask = new AtlasTask<Object>(parentGUI,
				AtlasCreator.R("ClearCaches.process.WaitMsg")) {

			@Override
			protected void done() {
				super.done();
				ace.getDataPool().fireChangeEvents(EventTypes.changeDpe);
			}

			@Override
			protected Object doInBackground() throws Exception {
				if (ace != null)
					ace.uncacheAndReread();

				return null;
			}

		};
		uncacheTask.execute();

	}

}
