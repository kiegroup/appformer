package org.livespark.client;

import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Frame;

@WorkbenchScreen(identifier = "app")
public class AppScreen extends Composite {

	@OnStartup
	public void onStartup(final PlaceRequest place) {
		Frame appFrame = new Frame(place.getParameter("url",
				"http://erraiframework.org"));
		
		appFrame.setWidth("100%");
		appFrame.setHeight("800px");
		initWidget(appFrame);
	}

	@WorkbenchPartTitle
	public String title() {
		return "Your app";
	}

}