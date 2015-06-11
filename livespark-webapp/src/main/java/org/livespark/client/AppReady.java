package org.livespark.client;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class AppReady {

	final String url;

	public AppReady(@MapsTo("url") String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}
	
	
}
