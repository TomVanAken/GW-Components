package com.gw.common;

import java.util.Map;

import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import com.ibm.xsp.component.UIViewRootEx2;

/**
 * 
 * @author axeif
 *
 *Helper class which contains static functions to help us out
 *WARNING : This class differs from the one in the WEB INF or Java Classes Library.  This is because session information has to be retrieved
 *differently for XPages compared to Agents.
 */
public class Utilities {

	public static Object getBean(String expr){
		FacesContext context = FacesContext.getCurrentInstance();
		Application app = context.getApplication();
		ValueBinding binding = app.createValueBinding("#{" + expr + "}");
		Object value = binding.getValue(context);
		return value;
	}

	public static String getSessionContext() {
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			UIViewRootEx2 view = (UIViewRootEx2)context.getViewRoot();
			return view.getPageName();
		}
		catch(Exception e) {
			System.out.println("Utilities.getSessionContext: " + e.toString());
			return "";
		}

	}

	public static enum Scope{APPLICATION, SESSION, VIEW, REQUEST;};
	public static Map<?, ?> getScope(Scope scope) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		switch(scope) {
		case APPLICATION:
			return externalContext.getApplicationMap();
		case SESSION:
			return externalContext.getSessionMap();
		case VIEW:
			return facesContext.getViewRoot().getViewMap();
		case REQUEST:
			return externalContext.getRequestMap();
		default:
			return null;
		}
	}

}
