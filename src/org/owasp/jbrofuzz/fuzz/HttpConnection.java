package org.owasp.jbrofuzz.fuzz;

import javax.net.ssl.*;
import javax.net.*;
import java.net.*;
import java.io.*;

public class HttpConnection {

	private String reply;
	private String message;


	URL url = null;

	// The maximum size for the socket I/O
	private final static int SEND_BUF_SIZE = 256 * 1024;
	private final static int RECV_BUF_SIZE = 256 * 1024;

	public HttpConnection(final String urlString, final String message) {

		this.reply = "";
		final byte[] recv = new byte[RECV_BUF_SIZE];
		this.message = message;

		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[]{
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					public void checkClientTrusted(
							java.security.cert.X509Certificate[] certs, String authType) {
					}
					public void checkServerTrusted(
							java.security.cert.X509Certificate[] certs, String authType) {
					}
				}
		};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			System.out.println("Could not install all-trusting certificates... " + e.getMessage());
		}

		// Now you can access an https URL without having the certificate in the truststore

		try {
			
			// URL
			//
			url = new URL(urlString);

			String protocol = url.getProtocol();    // http
			String host = url.getHost();            // hostname
			int port = url.getPort();               // 80
			String file = url.getFile();            // index.html
			String ref = url.getRef();              // _top_

			// Set default ports
			//
			if(protocol.equalsIgnoreCase("https") && (port == -1)) {
				port = 443;
			}
			if(protocol.equalsIgnoreCase("http") && (port == -1)) {
				port = 80;
			}

			// URL Connection
			URLConnection conn = url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			/*
			for(String rq : message.split("\r\n\r\n")) {
				if(rq.startsWith("POST") || rq.startsWith("GET") || rq.startsWith("HEAD")) {
					
				}
				else {
					conn.se
				}
			}
			#
			        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			#
			        urlConnection.setRequestProperty("Content-Length", ""+ body.length());
			*/
			// 
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(message);
			wr.flush();

			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				this.reply = line;
			}
			wr.close();
			rd.close();
		} 
		catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * <p>
	 * Return the message request sent on the Socket.
	 * </p>
	 * 
	 * @return StringBuffer
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * <p>
	 * Return the reply from the Connection that has been made, based on the
	 * message that has been transmitted during construction.
	 * </p>
	 * 
	 * @return String
	 */
	public String getReply() {
		return reply;
	}

	public int getPortText() {
		return url.getPort();
	}

}
