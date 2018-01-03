package org.minigui.eclipse.cdt.mstudio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.eclipse.cdt.utils.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.json.JSONArray;
import org.json.JSONObject;
import org.minigui.eclipse.cdt.mstudio.domain.VersionDesc;
import org.minigui.eclipse.cdt.mstudio.preferences.PreferenceConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class MStudioStartup implements IStartup {
	// private static long VERSION_CHECK_INTERVAL = 24 * 60 * 60 * 1000;
	private static long VERSION_CHECK_INTERVAL = 3 * 60 * 1000;

	@Override
	public void earlyStartup() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				doStat();
			}
		}).start();

		final long current = System.currentTimeMillis();
		final long lastCheck = getMStudioVersionLastCheck();

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				Shell shell = Display.getDefault().getActiveShell();
				VersionDesc verDesc = getLatestVersion();
				Version currVer = getCurrentVersion();

				if (verDesc == null
						|| currVer.compareTo(verDesc.getVersionInfo()) >= 0
						|| current - lastCheck < VERSION_CHECK_INTERVAL) {
					return;
				}

				boolean ret = MessageDialog.openQuestion(shell, "New version avaliable",
						"New version " + verDesc.getVersion() + " is avaliable. Do you want update now?");
				setMStudioVersionLastCheck(System.currentTimeMillis());
				if (ret) {
					Program.launch(verDesc.getDownloadUrl());
				}
			}
		});

	}

	public Version getCurrentVersion() {
		Bundle bundle = Platform.getBundle("org.minigui.eclipse.cdt.mstudio");
		return bundle.getVersion();
	}

	public String getOsName() {
		return System.getProperty("os.name");
	}

	public String getOsVersion() {
		return System.getProperty("os.version");
	}

	public String getSystemUUID() {
		String osname = System.getProperty("os.name").toLowerCase();

		if (osname.indexOf("window") >= 0) {
			return getWindowsUUID();
		} else if (osname.indexOf("nix") >= 0 || osname.indexOf("nux") >= 0) {
			return getLinuxUUID();
		}
		return null;
	}

	public String getWindowsUUID() {
		try {
			Process process = Runtime.getRuntime().exec(new String[] { "wmic", "cpu", "get", "ProcessorId" });
			process.getOutputStream().close();
			Scanner sc = new Scanner(process.getInputStream());
			sc.next();
			String uuid = sc.next().replace("-", "");
			sc.close();
			return uuid;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getLinuxUUID() {
		try {
			Process process = Runtime.getRuntime().exec(new String[] { "ls", "/dev/disk/by-uuid/" });
			process.getOutputStream().close();
			Scanner sc = new Scanner(process.getInputStream());
			String uuid = sc.next().replace("-", "");
			sc.close();
			return uuid;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String doPost(String url, String body) {
		try {
			URL restServiceURL = new URL(url);
			HttpURLConnection httpConnection = (HttpURLConnection) restServiceURL.openConnection();
			// param 输入小写，转换成 GET POST DELETE PUT
			httpConnection.setRequestMethod("POST");
			httpConnection.setRequestProperty("Authorization", "Bearer 3880E3cb6beeF5b8617C8d95cE1dFBd1");
			httpConnection.setRequestProperty("Content-Type", "application/json");
			httpConnection.setRequestProperty("User-Agent", "mstudio");

			httpConnection.setDoOutput(true);
			httpConnection.setDoInput(true);

			OutputStream outputStream = httpConnection.getOutputStream();
			outputStream.write(body.getBytes());
			outputStream.flush();

			if (httpConnection.getResponseCode() != 200) {
				return null;
			}
			BufferedReader responseBuffer = new BufferedReader(
					new InputStreamReader((httpConnection.getInputStream())));
			StringBuilder sb = new StringBuilder();
			String output;
			while ((output = responseBuffer.readLine()) != null) {
				sb.append(output);
			}
			httpConnection.disconnect();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String doGet(String url) {
		try {
			URL restServiceURL = new URL(url);
			HttpURLConnection httpConnection = (HttpURLConnection) restServiceURL.openConnection();

			httpConnection.setRequestMethod("GET");
			httpConnection.setRequestProperty("Authorization", "Bearer 3880E3cb6beeF5b8617C8d95cE1dFBd1");
			httpConnection.setRequestProperty("User-Agent", "mstudio");

			httpConnection.setDoOutput(false);
			httpConnection.setDoInput(true);

			// OutputStream outputStream = httpConnection.getOutputStream();
			// outputStream.write(body.getBytes());
			// outputStream.flush();

			if (httpConnection.getResponseCode() != 200) {
				return null;
			}
			BufferedReader responseBuffer = new BufferedReader(
					new InputStreamReader((httpConnection.getInputStream())));
			StringBuilder sb = new StringBuilder();
			String output;
			while ((output = responseBuffer.readLine()) != null) {
				sb.append(output);
			}
			httpConnection.disconnect();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String doStat() {
		// {"app": "mstudio", "ver": "1.0", "os": "windows", "os_ver": "1"}
		String url = "http://www.minigui.com/index.php/tools/packages/statistics/api/v1/stat_usages";
		StringBuilder sb = new StringBuilder();
		sb.append("{").append("\"app\"").append(":").append("\"mstudio\"").append(",").append("\"ver\"").append(":")
				.append("\"").append(this.getCurrentVersion().toString()).append("\"").append(",").append("\"os\"")
				.append(":").append("\"").append(this.getOsName()).append("\"").append(",").append("\"os_ver\"")
				.append(":").append("\"").append(this.getOsVersion()).append("\"").append(",").append("\"uuid\"")
				.append(":").append("\"").append(this.getSystemUUID()).append("\"").append("}");
		return this.doPost(url, sb.toString());
	}

	public VersionDesc getLatestVersion() {
		String url = "http://www.minigui.com/index.php/tools/packages/statistics/api/v1/stat_latest_apps?app=mstudio&os=windows&os_ver=7";
		StringBuilder sb = new StringBuilder();
		sb.append(url).append("?").append("app=mstudio").append("&").append("os=").append(this.getOsName()).append("&")
				.append("os_ver=").append(this.getOsVersion());
		String res = this.doGet(url);
		if (res == null || res.length() == 0) {
			return null;
		}

		JSONObject json = new JSONObject(res);
		int code = json.optInt("retCode");
		if (code != 0) {
			return null;
		}

		JSONArray verArray = json.optJSONArray("data");
		if (verArray == null) {
			return null;
		}

		JSONObject verInfo = verArray.optJSONObject(0);
		if (verInfo == null) {
			return null;
		}

		String version = verInfo.optString("ver");
		String dlUrl = verInfo.optString("dl_url");
		String desc = verInfo.optString("intro");

		VersionDesc verDesc = new VersionDesc();
		verDesc.setVersion(version);
		verDesc.setDownloadUrl(dlUrl);
		verDesc.setDesc(desc);

		return verDesc;
	}

	public long getMStudioVersionLastCheck() {
		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		if (!store.contains(PreferenceConstants.MSVERSION_LAST_CHECK)) {
			return 0;
		}
		return store.getLong(PreferenceConstants.MSVERSION_LAST_CHECK);
	}

	public void setMStudioVersionLastCheck(long lastCheck) {
		IPreferenceStore store = MStudioPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.MSVERSION_LAST_CHECK, lastCheck);
	}
}
