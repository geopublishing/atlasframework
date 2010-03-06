/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
///*******************************************************************************
// * Copyright (c) 2009 Stefan A. Krüger.
// * 
// * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
// * http://www.geopublishing.org
// * 
// * Geopublisher is part of the Geopublishing Framework hosted at:
// * http://wald.intevation.org/projects/atlas-framework/
// * 
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License
// * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// * 
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License (license.txt)
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
// * or try this link: http://www.gnu.org/licenses/gpl.html
// * 
// * Contributors:
// *     Stefan A. Krüger - initial API and implementation
// ******************************************************************************/
//package skrueger.creator;
//
//import java.awt.Window;
//import java.io.File;
//
//import javax.swing.SwingWorker;
//
//import org.apache.log4j.Logger;
//
//import skrueger.atlas.dp.AMLImport;
//import skrueger.atlas.gui.internal.AtlasTask;
//import skrueger.atlas.internal.ProgressListener;
//import skrueger.creator.gui.GpJSplitPane;
//
///**
// * This instance of {@link SwingWorker} loads a saved Atlas to a
// * {@link AtlasConfigEditable} and also inserts the {@link GpJSplitPane} into
// * the owners content pane;
// * 
// * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
// * 
// * @deprecated
// */
//public class AceLoader extends AtlasTask<AtlasConfigEditable> {
//	private static final Logger LOGGER = Logger.getLogger(AceLoader.class);
//
//	private final File atlasDir;
//
//	public AceLoader(File atlasDir, Window owner) {
//		super(owner, AtlasCreator.R("AtlasLoader.processinfo.loading", atlasDir
//				.getAbsolutePath()));
//		this.atlasDir = atlasDir;
//	}
//
//	@Override
//	protected AtlasConfigEditable doInBackground() throws Exception {
//		AMLImport.pl = new ProgressListener() {
//			@Override
//			public void info(String msg) {
//				publish(msg);
//			}
//		};
//		AtlasConfigEditable ace = AMLImportEd.parseAtlasConfig(atlasDir);
//
//		// Try to throw away as much memory as possible
//		System.gc();
//
//		return ace;
//	}
//}
