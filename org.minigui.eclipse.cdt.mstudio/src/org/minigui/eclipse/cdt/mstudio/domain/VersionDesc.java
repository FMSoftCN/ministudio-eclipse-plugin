package org.minigui.eclipse.cdt.mstudio.domain;

import org.osgi.framework.Version;

public class VersionDesc {
	
	private String version;
	private String desc;
	private String downloadUrl;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	
	public Version getVersionInfo() {
		return Version.parseVersion(this.getVersion());
	}

	@Override
	public String toString() {
		return "VersionDesc [version=" + version + ", desc=" + desc + ", downloadUrl=" + downloadUrl + "]";
	}
	
	
	

}
