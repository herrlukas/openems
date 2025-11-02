package io.openems.edge.solplanet.common;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class Helpers {

	Helpers() {		
	}
	
	public static String buildUrl(String ip, String sn, Integer device) {
		String url = "https://" + ip + ":443/getdevdata.cgi?device=" + device + "&sn=" + sn;
		return url;
	}

	public static void disableCertificateValidation() {
	    try {
	        TrustManager[] trustAllCerts = new TrustManager[]{
	            new X509TrustManager() {
	                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
	                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
	                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
	            }
	        };

	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	        // Disable hostname verification
	        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
}
