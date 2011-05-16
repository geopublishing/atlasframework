package org.geopublishing.geopublisher.export.gphoster;

import org.geopublishing.geopublisher.GpUtil;

/**
 * these enums describe the different states of the geopublishing service
 */
public enum SERVICE_STATUS {
	GPHOSTER_FTP_DOWN, GPHOSTER_REST_DOWN, SYSTEM_OFFLINE, OK;

	/**
	 * Returns <code>null</code> if all is is ok.
	 */
	public String validationValue() {

		if (this == OK)
			return null;

		return GpUtil.R("ExportWizard.Ftp.ValidationError_" + this.toString());
	}
}
