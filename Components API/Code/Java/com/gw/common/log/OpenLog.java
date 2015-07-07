package com.gw.common.log;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import com.gw.common.ConfigurationManager;
import com.gw.common.ApplicationSetup;
import com.gw.common.Utilities;
import com.gw.common.log.LogItem.EventType;

import org.openntf.domino.*;


/**
 * @author Group-wave
 *
 * @since com.gw.common.log 1.0.0
 */
public class OpenLog implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean debugMode = false;
	private Date startLogDate = new Date();

	public OpenLog() {
		ConfigurationManager configMgr = ApplicationSetup.getConfigurationManager();
		if(configMgr.getText( "Application Settings&&Log Debug Items").equals("Yes")) debugMode=true;
	}

	public void setDebugMode(final Boolean mode) {
		if(mode==null) {
			ConfigurationManager configMgr = ApplicationSetup.getConfigurationManager();
			if(configMgr.getText( "Application Settings&&Log Debug Items").equals("Yes")) debugMode=true;
		} else {
			debugMode = mode;
		}
	}

	public void logDebug(Object message) {
		if(!debugMode) return; //Only debug when debugMode is on
		logDebug(message, null);
	}

	public void logDebug(Object message, Document doc) {
		if(!debugMode) return; //Only debug when debugMode is on
		createLogItem(EventType.DEBUG, message, null, doc, null);
	}

	public void logEvent(Object message) {
		logEvent(null, message, null, null);

	}

	public void logEvent(Object message, Level level, Document doc) {
		logEvent(null, message, level, doc);
	}

	public void logEvent(Throwable t, Object message, Level level, Document doc) {
		createLogItem(EventType.EVENT, message, level, doc, t);
	}

	public void logException(Throwable throwable) {
		logException(throwable, "", null, null);
	}

	public void logException(Throwable throwable, Object message, Level level, Document doc) {
		createLogItem(EventType.ERROR, message, level, doc, throwable);
	}

	protected void createLogItem(final EventType type, final Object message, final Level severity, final Document doc, final Throwable t) {
		try {
			LogItem it = new LogItem(type, startLogDate);
			it.setLogAgentLanguage("JAVA");
			it.setLogMessage(message.toString());
			if(severity!=null) it.setLogSeverity(severity);
			it.setDocToLog(doc);
			it.setLogFromAgent(Utilities.getSessionContext());

			//Error info
			if(t!=null) {
				String exceptionMessage = t.getLocalizedMessage();
				it.setLogErrorMessage(exceptionMessage==null||exceptionMessage.isEmpty()?t.toString():exceptionMessage);
				it.setLogExceptionId(t.toString());
			}
			

			Throwable exception = t!=null?t:new Exception("Dummy");  //Create dummy exception to scan stacktrace
			//Scan stackTrace
			Integer lineNumber=0;
			String methodName="";
			String className="";
			List<StackTraceElement> stackElements = Arrays.asList(exception.getStackTrace());
			Vector<String> stackTrace = new Vector<String>();
			for(StackTraceElement trace:stackElements) {
				if(t!=null || !trace.getClassName().equals(this.getClass().getName())) { //ignore trace in this class (to skip trace of Dummy exception)
					stackTrace.add(trace.toString());
					if(methodName.isEmpty()) {
						methodName = trace.getMethodName();
						className = trace.getClassName();
						lineNumber = trace.getLineNumber();
					}
				}
			}

			it.setLogStackTrace(stackTrace);
			it.setLogErrorLine(lineNumber);
			it.setLogFromClass(className);
			it.setLogFromMethod(methodName);

			if(!it.save()) System.out.println("Unable to save logItem " + it.getLogMessage() + " - " + it.getLogErrorMessage());
		} catch(Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		
	}
}
