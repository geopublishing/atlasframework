package org.geopublishing.geopublisher.export.gphoster;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.geopublishing.gpsync.AtlasFingerprint;
import org.jfree.util.Log;

import sun.misc.BASE64Encoder;

import com.enterprisedt.net.ftp.FTPClient;

import de.schmitzm.io.IOUtil;

/**
 * Manages talking to the gp-hoster-jsf HTTP API
 */
public class GpHosterClient {

	private static final String EXISTS_USER_PATH = "userExists/";
	private static final String CREATE_USER_PATH = "userCreate/";
	private static final String DELETE_USER_PATH = "userDelete/";
	private static final String INFORM_UPLOADED_ATlAS_PATH = "atlasUploaded/";
	private static final String CHECKFREE_ATLASNAME_PATH = "atlasnameFree/";
	private static final String FINGERPRINT_ATLAS_PATH = "atlasFingerprint/";

	private static final String MASTUSER = "w2";
	private static final String MASTPASSWD = "32894013";
	private static final int SC_OK = 200;

	// TODO Make switchable!

	public static final String DEFAULT_GPHOSTER_REST_URL = "http://hoster.geopublishing.org:8088/gp-hoster-jsf/";
	// public static final String GPHOSTER_REST_URL =
	// "http://localhost:8080/gp-hoster-jsf/";
	public static final String DEFAULT_GPHOSTER_FTP_HOSTNAME = "ftp.geopublishing.org";
	public static final String DEFAULt_GPHOSTER_FTP_URL = "ftp://"
			+ DEFAULT_GPHOSTER_FTP_HOSTNAME;

	private final String restUrl;

	/**
	 * 
	 * @param url
	 *            URL to the hoster servlet, e.g.
	 *            http://localhost:8080/gp-hoster-jsf/
	 */
	public GpHosterClient(String url) {
		if (!url.endsWith("/"))
			url += "/";
		this.restUrl = url;
	}

	/**
	 * USes the default URL of the Geopublishing Server
	 */
	public GpHosterClient() {
		this.restUrl = DEFAULT_GPHOSTER_REST_URL;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	private String userName;
	private String password;
	private SERVICE_STATUS serviceStatus;

	public boolean userExists() throws IOException {
		return userExists(getUserName());
	}

	/**
	 * Returns <code>true</code> if the passed atlasBaseName is not
	 * used/registered yet.
	 */
	public boolean atlasBasenameFree(String baseName) throws IOException {
		return SC_OK == sendRESTint(METHOD.GET.toString(),
				CHECKFREE_ATLASNAME_PATH + baseName, null, null, null);
	}

	public boolean userDelete(String delUser, String delUserPassword)
			throws IOException {
		return SC_OK == sendRESTint(METHOD.GET.toString(), DELETE_USER_PATH
				+ delUser, null, delUser, delUserPassword);
	}

	/**
	 * Delete the user defined wit {@link #setUserName(String)} and
	 * {@link #getPassword()}.
	 */
	public boolean userDelete() throws IOException {
		if (getUserName() == null || getUserName().equals(MASTUSER)) {
			Log.info("Will not delete the master user!");
			return false;
		}
		return userDelete(getUserName(), getPassword());
	}

	public boolean userCreate(String username, String email) throws IOException {
		return SC_OK == sendRESTint(METHOD.GET.toString(), CREATE_USER_PATH
				+ username + "?email=" + email, null, MASTUSER, MASTPASSWD);
	}

	/**
	 * Informs the REST Servlet about the new file. The username and password
	 * used are the username and password set with get/set. This REST call my
	 * take a second or two, since the zip is unpacked and a Fingerprint
	 * generated directly.
	 * 
	 * @throws IOException
	 */
	public AtlasFingerprint informAboutUploadedZipFile(String atlasBasename,
			File zipFile) throws IOException {
		return informAboutUploadedZipFile(atlasBasename, zipFile,
				getUserName(), getPassword());

	}

	/**
	 * Informs the REST Servlet about the new file. The username and password
	 * used are the username and password set with get/set. This REST call my
	 * take a second or two, since the zip is unpacked and a Fingerprint
	 * generated directly.
	 * 
	 * @throws IOException
	 */
	public AtlasFingerprint informAboutUploadedZipFile(String atlasBasename,
			File zipFile, String username, String password) throws IOException {
		return new AtlasFingerprint(sendRESTstring(METHOD.GET.toString(),
				INFORM_UPLOADED_ATlAS_PATH + atlasBasename + "?filename="
						+ zipFile.getName(), null, username, password));
	}

	public String sendRESTstring(String method, String url,
			String xmlPostContent, String username, String password)
			throws IOException {
		return sendRESTstring(method, url, xmlPostContent, "application/xml",
				"application/xml", username, password);
	}

	/**
	 * Sends a REST request and return the answer as a String
	 * 
	 * @param method
	 *            e.g. 'POST', 'GET', 'PUT' or 'DELETE'
	 * @param urlEncoded
	 *            e.g. '/workspaces' or '/workspaces.xml'
	 * @param contentType
	 *            format of postData, e.g. null or 'text/xml'
	 * @param accept
	 *            format of response, e.g. null or 'text/xml'
	 * @param postData
	 *            e.g. xml data
	 * @throws IOException
	 * @return null, or response of server
	 */
	public String sendRESTstring(String method, String urlEncoded,
			String postData, String contentType, String accept,
			String username, String password) throws IOException {
		HttpURLConnection connection = sendREST(method, urlEncoded, postData,
				contentType, accept, username, password);

		// Read response
		InputStream in = connection.getInputStream();
		try {

			int len;
			byte[] buf = new byte[1024];
			StringBuffer sbuf = new StringBuffer();
			while ((len = in.read(buf)) > 0) {
				sbuf.append(new String(buf, 0, len));
			}
			return sbuf.toString();
		} finally {
			in.close();
		}
	}

	public boolean userExists(String checkUsername) throws IOException {
		if (checkUsername == null)
			return false;
		return SC_OK == sendRESTint(METHOD.GET.toString(), EXISTS_USER_PATH
				+ checkUsername, null, MASTUSER, MASTPASSWD);
	}

	private int sendRESTint(String method, String url, String xmlPostContent,
			String username, String password) throws IOException {
		return sendRESTint(method, url, xmlPostContent, "application/xml",
				"application/xml", username, password);
	}

	public AtlasFingerprint atlasFingerprint(String atlasBasename)
			throws IOException {
		return atlasFingerprint(atlasBasename, getUserName(), getPassword());
	}

	private AtlasFingerprint atlasFingerprint(String atlasBasename,
			String username, String password) throws IOException {

		if (atlasBasenameFree(atlasBasename))
			return null;

		System.out
				.println("Username = " + username + " password = " + password);

		return new AtlasFingerprint(sendRESTstring(METHOD.GET.toString(),
				FINGERPRINT_ATLAS_PATH + atlasBasename, null, username,
				password));
	}

	/**
	 * @param method
	 *            e.g. 'POST', 'GET', 'PUT' or 'DELETE'
	 * @param urlEncoded
	 *            e.g. '/workspaces' or '/workspaces.xml'
	 * @param contentType
	 *            format of postData, e.g. null or 'text/xml'
	 * @param accept
	 *            format of response, e.g. null or 'text/xml'
	 * @param postData
	 *            e.g. xml data
	 * @throws IOException
	 * @return null, or response of server
	 */
	private int sendRESTint(String method, String urlEncoded, String postData,
			String contentType, String accept, String username, String password)
			throws IOException {
		HttpURLConnection connection = sendREST(method, urlEncoded, postData,
				contentType, accept, username, password);

		return connection.getResponseCode();
	}

	private HttpURLConnection sendREST(String method, String urlEncoded,
			String postData, String contentType, String accept,
			String username, String password) throws MalformedURLException,
			IOException {
		StringReader postDataReader = postData == null ? null
				: new StringReader(postData);
		return sendREST(method, urlEncoded, postDataReader, contentType,
				accept, username, password);
	}

	enum METHOD {
		DELETE, GET, POST, PUT
	}

	private HttpURLConnection sendREST(String method, String urlAppend,
			Reader postDataReader, String contentType, String accept,
			String username, String password) throws MalformedURLException,
			IOException {
		boolean doOut = !METHOD.DELETE.toString().equals(method)
				&& postDataReader != null;

		String link = restUrl + urlAppend;
		URL url = new URL(link);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(doOut);
		// uc.setDoInput(false);
		if (contentType != null && !"".equals(contentType)) {
			connection.setRequestProperty("Content-type", contentType);
			connection.setRequestProperty("Content-Type", contentType);
		}
		if (accept != null && !"".equals(accept)) {
			connection.setRequestProperty("Accept", accept);
		}

		connection.setRequestMethod(method.toString());

		if (username != null && password != null) {
			String userPasswordEncoded = new BASE64Encoder().encode((username
					+ ":" + password).getBytes());
			connection.setRequestProperty("Authorization", "Basic "
					+ userPasswordEncoded);
		}

		connection.connect();
		if (connection.getDoOutput()) {
			Writer writer = new OutputStreamWriter(connection.getOutputStream());
			char[] buffer = new char[1024];

			Reader reader = new BufferedReader(postDataReader);
			int n;
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}

			writer.flush();
			writer.close();
		}
		return connection;
	}

	/**
	 * Does tests to check whether the service is available. Unless the system
	 * is completely offline, the result is cached.
	 */
	public SERVICE_STATUS checkService() {
		if (serviceStatus == null
				|| serviceStatus == SERVICE_STATUS.SYSTEM_OFFLINE) {

			// Check generally
			if (!IOUtil.urlExists("http://www.denic.de/"))
				return SERVICE_STATUS.SYSTEM_OFFLINE;

			try {
				// Check FTP
				final FTPClient ftpClient = new FTPClient();
				ftpClient.setTimeout(5000);
				ftpClient.setRemoteHost(DEFAULT_GPHOSTER_FTP_HOSTNAME);
				ftpClient.connect();
				ftpClient.quit();
			} catch (Exception e) {
				return serviceStatus = SERVICE_STATUS.GPHOSTER_FTP_DOWN;
			}

			if (!IOUtil.urlExists(DEFAULT_GPHOSTER_REST_URL + "index.html"))
				return serviceStatus = SERVICE_STATUS.GPHOSTER_REST_DOWN;

			serviceStatus = SERVICE_STATUS.OK;
		}
		return serviceStatus;
	}

}
