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
package skrueger.creator;

import skrueger.atlas.AVUtil;
import skrueger.atlas.BugReportmailer;

public class GPBugReportmailer extends BugReportmailer {

	public GPBugReportmailer() {
		super("geopublisher.log");
	}

	@Override
	protected String getBody() {
		return AtlasCreator.R("SendLogToAuthor.Email.Body", logFileLocation);
	}

	@Override
	protected String getSubject() {
		return AtlasCreator.R("SendLogToAuthor.Email.Subject", AVUtil
				.getVersionInfo());
	}

}
