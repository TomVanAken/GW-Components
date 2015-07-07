package com.gw.common.log;

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.gw.common.ConfigurationManager;
import com.gw.common.Utilities;
import com.gw.common.ApplicationSetup;
import com.gw.common.log.LogItem.EventType;
import com.ibm.jscript.InterpretException;
import com.ibm.xsp.component.xp.XspEventHandler;

import org.openntf.domino.*;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.utils.Factory.SessionType;

/**
 * This class can be used as view scoped managed bean in faces-config.xml to log errors/events in log database
 * @author Group-wave
 *
 * @since com.gw.common.log 1.0.0
 */
public class XspOpenLog implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean debugMode = false;
	private Date startLogDate = new Date();
	protected OpenLog oLog = new OpenLog();

	public XspOpenLog() {
		ConfigurationManager configMgr = ApplicationSetup.getConfigurationManager();
		if(configMgr.getText( "Application Settings&&Log Debug Items").equals("Yes")) debugMode=true;
	}

	public void setDebugMode(Boolean mode) {
		if(mode==null) {
			ConfigurationManager configMgr = ApplicationSetup.getConfigurationManager();
			if(configMgr.getText( "Application Settings&&Log Debug Items").equals("Yes")) debugMode=true;
		} else {
			debugMode = mode;
		}
	}

	public void logDebug(Object message, Object event) {
		if(!debugMode) return; //Only debug when debugMode is on
		logDebug(message, null, event);
	}

	public void logDebug(Object message, String docId, Object event) {
		if(!debugMode) return; //Only debug when debugMode is on
		createLogItem(EventType.DEBUG, message, null, docId, null, event);
	}

	public void logEvent(Object message, Object event) {
		logEvent(null, message, null, null, event);
	}

	public void logEvent(Object message, Integer level, String docId, Object event) {
		logEvent(null, message, level, docId, event);
	}

	public void logEvent(Object t, Object message, Integer level, String docId, Object event) {
		createLogItem(EventType.EVENT, message, level, docId, t, event);
	}

	public void logException(Object throwable, Object event) {
		logException(throwable, "", null, null,event);
	}

	public void logException(Object throwable, Object message, Integer level, String docId, Object event) {
		createLogItem(EventType.ERROR, message, level, docId, throwable, event);
	}

	public void createFromErrorPage(final Object t, final String fromPage, final String componentId, final String propertyId) {
	try {
		LogItem it = new LogItem(EventType.ERROR, startLogDate);
		it.setLogAgentLanguage("SSJS");
		it.setLogSeverity(Level.SEVERE);
		it.setDocToLog(null);
		it.setLogFromAgent(fromPage);

		//Event Info
		it.setLogFromClass(componentId);
		it.setLogFromMethod(propertyId);


		//Error info
		if(t!=null) {
			InterpretException ie = getInterpretException(t);
			String exceptionMessage = ie.getMessage();
			it.setLogErrorMessage(exceptionMessage==null||exceptionMessage.isEmpty()?ie.toString():exceptionMessage);
			it.setLogErrorLine(ie.getErrorLine());
			Vector<String> stackInfo = new Vector<String>();
			stackInfo.add(ie.getExpressionText());
			it.setLogStackTrace(stackInfo);
		}

		if(!it.save()) System.out.println("Unable to save logItem " + it.getLogMessage() + " - " + it.getLogErrorMessage());
	} catch(Exception e) {
		oLog.logException(e);
	}
	}
	
	protected void createLogItem(final EventType type, final Object message, final Integer severity, final String docId, final Object t, final Object event) {
			
		try {
			Throwable cause  = null;
			LogItem it = new LogItem(type, startLogDate);	
			it.setLogAgentLanguage("SSJS");
			if(message!=null) it.setLogMessage(message.toString());
			if(severity!=null) it.setLogSeverity(getLevelFromInt(severity));
			Document doc = getDocument(docId);
			it.setDocToLog(doc);
			it.setLogFromAgent(Utilities.getSessionContext());

			//Event Info
			String[] eventInfo = getEventInfo(event);
			it.setLogFromClass(eventInfo[0] + " - " + eventInfo[1]);
			it.setLogFromMethod(eventInfo[2]);


			//Error info
			if(t!=null) {
				InterpretException ie = getInterpretException(t);
				String exceptionMessage = ie.getMessage();
				it.setLogErrorMessage(exceptionMessage==null||exceptionMessage.isEmpty()?ie.toString():exceptionMessage);
				it.setLogErrorLine(ie.getErrorLine());
				Vector<String> stackInfo = new Vector<String>();
				stackInfo.add(ie.getExpressionText());

				if(ie.getCause()!=null) cause = ie.getCause();
				it.setLogStackTrace(stackInfo);
				
			}

			if(!it.save()) System.out.println("Unable to save logItem " + it.getLogMessage() + " - " + it.getLogErrorMessage());
			if(cause != null ) oLog.logException(cause, "SSJS Exception cause for " + it.getLogFromClass(), it.getLogSeverity(), null);
		} catch(Exception e) {
			oLog.logException(e);
		}

	}

	/**
	 * Gets a Document from a notes database, based on the document Id
	 * @param docId Id of the document to retrieve, must be in on of these formats: "<server>:<dbPath>:<UNID>", "<dbPath>:<UNID>" or "<UNID>"
	 * @return The document if found, null if not
	 */
	private Document getDocument(final String docId) {
		if(docId==null || docId.isEmpty()) return null;
		//We assume docId has one of these formats: "<server>:<dbPath>:<UNID>", "<dbPath>:<UNID>" or "<UNID>"
		//If server is blank, server of the current db is used
		String server = "";
		String dbPath = "";
		String unid = "";
		Document doc;
		try {
			Session session = Factory.getSession(SessionType.CURRENT);
			String[] docInfo = docId.split(":");
			Database dbCurrent = session.getCurrentDatabase();

			switch(docInfo.length) {
			case 1:
				unid = docInfo[0];
				break;
			case 2:
				server = dbCurrent.getServer();
				dbPath = docInfo[0];
				unid = docInfo[1];
				break;
			case 3:
				server = docInfo[0];
				dbPath = docInfo[1];
				unid = docInfo[2];
			}

			if(server.isEmpty() && dbPath.isEmpty()) {
				//Use current database to retrieve document
				doc = dbCurrent.getDocumentByUNID(unid);
			} else {
				//Try to find document db to retrieve document
				Database dbDoc = session.getDatabase(server, dbPath);
				doc = dbDoc.getDocumentByUNID(unid);
			}

			if(doc==null) return null;
			return (doc!=null && doc.isValid() && !doc.isDeleted())?doc:null;

		} catch(Exception e) {
			oLog.logException(e);
			return null;
		}
	}

	/**
	 * Gets a component name and event name based on the object passed in. Should be an instance of UIComponent or XspEventHandler
	 * 
	 * @param event
	 *            Object instance of UIComponent (e.g. XspOutputText) or XspEventHandler or null
	 * @return String array, first element is Component Type, second element is Component id, third element is Event Name
	 * 
	 */
	private String[] getEventInfo(final Object event) {
		String[] ids = new String[3];

		ids[0] = "";
		ids[1] = "";
		ids[2] = "";

		try {
			if (null == event) {
				return ids;
			}
			if(event instanceof com.ibm.xsp.component.xp.XspEventHandler) {
				XspEventHandler handler = (XspEventHandler) event;
				UIComponent parent = handler.getParent();
				ids[0] = parent.getClass().getCanonicalName();
				String parentId = parent.getId();
				String parentClientId = parent.getClientId(FacesContext.getCurrentInstance());
				ids[1] = (parentId==null||parentId.isEmpty()?"<No Id>":parentId) + " - " + parentClientId;
				ids[2] = handler.getEvent();
			} else if(event instanceof UIComponent) {
				UIComponent parent = (UIComponent)event;
				ids[0] = parent.getClass().getCanonicalName();
				String parentId = parent.getId();
				String parentClientId = parent.getClientId(FacesContext.getCurrentInstance());
				ids[1] = (parentId==null||parentId.isEmpty()?"<No Id>":parentId) + " - " + parentClientId;
			} else
			{
				//This is not a component nor an event
				ids[0] = event.getClass().toString() + " <No Component or Event>";
			}
		} catch (Exception e) {
			// We've got something I wasn't expecting, an exception
			String message = "WARNING: invalid object passed in by developer. Should be a component (not component id) or eventHandler. Found "
				+ (event == null ? null : event.getClass().getName());
			oLog.logException(e, message, null, null);
		}
		return ids;
	}

	/**
	 * Returns  if je is or contains an InterPretException, and returns this exception if available
	 * @param je Object
	 * @return The InterpretException retrieved from je
	 */
	protected InterpretException getInterpretException(final Object je) {
		try {
			System.out.println(je.getClass().toString());
			if(je == null ) {
				Throwable t = new Throwable("Dummy Exception");
				InterpretException ie = new InterpretException(t);
				ie.setExpressionText(t.getMessage());
				return ie;
			} else if (je instanceof com.ibm.jscript.InterpretException) {
				return (InterpretException) je;
			} else if (je instanceof Throwable) {
				InterpretException ie = new InterpretException((Throwable)je);
				ie.setExpressionText(((Throwable)je).getMessage());
				return ie;
			} else {
				Throwable t = new Throwable(je.toString());
				InterpretException ie = new InterpretException(t);
				ie.setExpressionText(t.getMessage());
				return ie;
			}
		} catch (Throwable t) {
			oLog.logException(t);
			return new InterpretException(new Throwable(t.getMessage()));
		}
	}
	
	protected Level getLevelFromInt(Integer levelId) {
		switch(levelId) {
		case 7: return Level.SEVERE;
		case 6: return Level.WARNING;
		case 5: return Level.INFO;
		case 4: return Level.CONFIG;
		case 3: return Level.FINE;
		case 2: return Level.FINER;
		case 1: return Level.FINEST;
		default: return Level.OFF;
		}
	}
}
