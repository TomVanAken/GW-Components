package com.gw.common.log;

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;

import org.openntf.domino.*;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.utils.Factory.SessionType;

import com.gw.common.ApplicationSetup;
import com.gw.common.ConfigurationManager;


public class LogItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5017589527597722721L;
	public static enum EventType{DEBUG, EVENT, ERROR;}
	public static final int MAXSTACK = 25;
	private static Database logDatabase = null;;

	static {
		//Get the default Log database
		ConfigurationManager configMgr = ApplicationSetup.getConfigurationManager();
		logDatabase = configMgr.getDatabase("Applications&&OpenLog Database");
		if(logDatabase==null) {
			//try default log path
			Session s = Factory.getSession(SessionType.CURRENT);
			logDatabase = s.getDatabase(s.getCurrentDatabase().getServer(), "log\\openLog.nsf");

		}
		if(logDatabase != null && !logDatabase.isOpen()) logDatabase = null;
	}

	protected EventType logEventType;
	protected Date logEventTime;
	protected Date logEventStartTime;
	protected Level logSeverity;
	protected String logUserName;
	protected String logEffectiveName;
	protected String logAccessLevel;
	protected Vector<Object> logUserRoles;
	protected Vector<String> logClientVersion;
	protected String logFromServer;
	protected String logFromDatabase;
	protected String logFromDatabaseTitle;
	protected String logFromAgent;
	protected String logAgentLanguage;
	protected String logFromClass;
	protected String logFromMethod;
	protected Integer logErrorLine;
	protected String logExceptionId;
	protected String logErrorMessage;
	protected String logMessage;
	protected Document docToLog;
	protected Vector<String> logStackTrace;

	public LogItem(String logEventType, Date startTime) {
		this(EventType.valueOf(logEventType), startTime);
	}

	public LogItem(EventType logEventType, Date startTime) {
		this(logEventType);
		logEventStartTime = startTime;
	}


	public LogItem(String logEventType) {
		this(EventType.valueOf(logEventType));
	}

	public LogItem(EventType logEventType) {
		this.logEventType = logEventType;
		this.logEventStartTime = new Date();
		this.logEventTime = new Date();
		this.logSeverity = getDefaultLevel(logEventType);

		Session session = Factory.getSession(SessionType.CURRENT);
		this.logUserName = session.getUserName();
		this.logEffectiveName = session.getEffectiveUserName();
		setClientVersion(session.getNotesVersion());

		Database dbCurrent = session.getCurrentDatabase();
		setLogAccessLevel(dbCurrent.getCurrentAccessLevel());
		this.logUserRoles = session.evaluate("@UserRoles");
		this.logFromServer = dbCurrent.getServer();
		this.logFromDatabase = dbCurrent.getFilePath();
		this.logFromDatabaseTitle = dbCurrent.getTitle();

	}


	/**
	 * @return the logEventType
	 */
	public String getLogEventType() {
		return logEventType.toString();
	}


	/**
	 * @return the logEventTime
	 */
	public Date getLogEventTime() {
		return logEventTime;
	}




	/**
	 * @return the logEventStartTime
	 */
	public Date getLogEventStartTime() {
		return logEventStartTime;
	}

	/**
	 * @return the logSeverity
	 */
	public Level getLogSeverity() {
		return logSeverity;
	}




	/**
	 * @param logSeverity the logSeverity to set
	 */
	public void setLogSeverity(Level logSeverity) {
		this.logSeverity = logSeverity;
	}

	/**
	 * @return the logUserName
	 */
	public String getLogUserName() {
		return logUserName;
	}

	/**
	/**
	 * @return the logEffectiveName
	 */
	public String getLogEffectiveName() {
		return logEffectiveName;
	}


	/**
	 * @return the logAccessLevel
	 */
	public String getLogAccessLevel() {
		return logAccessLevel;
	}

	/**
	 * @param logAccessLevel the logAccessLevel to set (int converted to level text)
	 */
	private void setLogAccessLevel(int level) {
		String accessLevel = ""; 
		switch(level) {
		case 0:	accessLevel = "0. No Access";	break;
		case 1:	accessLevel = "1. Depositor";	break;
		case 2:	accessLevel = "2. Reader";	break;
		case 3:	accessLevel = "3. Author";	break;
		case 4:	accessLevel = "4. Editor";	break;
		case 5:	accessLevel = "5. Designer";	break;
		case 6:	accessLevel = "6. Manager";	break;
		default:accessLevel = "? UNKNOWN";	break;
		};
		logAccessLevel = accessLevel;
	}


	/**
	 * @return the logUserRoles
	 */
	public Vector<Object> getLogUserRoles() {
		return logUserRoles;
	}


	/**
	 * @return the logClientVersion
	 */
	public Vector<String> getLogClientVersion() {
		return logClientVersion;
	}




	/**
	 * @param logClientVersion the logClientVersion to set
	 */
	private void setClientVersion(String notesVersion) {
		Vector<String> clientVersion = new Vector<String>();
		if (notesVersion != null) {
			if (notesVersion.indexOf("|") > 0) {
				clientVersion.addElement(notesVersion.substring(0, notesVersion.indexOf("|")));
				clientVersion.addElement(notesVersion.substring(notesVersion.indexOf("|") + 1));
			} else {
				clientVersion.addElement(notesVersion);
			}
		}
		logClientVersion = clientVersion;
	}




	/**
	 * @return the logFromServer
	 */
	public String getLogFromServer() {
		return logFromServer;
	}




	/**
	 * @return the logFromDatabase
	 */
	public String getLogFromDatabase() {
		return logFromDatabase;
	}



	/**
	 * @return the logFromDatabaseTitle
	 */
	public String getLogFromDatabaseTitle() {
		return logFromDatabaseTitle;
	}


	/**
	 * @return the logFromClass
	 */
	public String getLogFromClass() {
		return logFromClass;
	}




	/**
	 * @param logFromAgent the logFromAgent to set
	 */
	public void setLogFromAgent(String logFromAgent) {
		this.logFromAgent = logFromAgent;
	}

	/**
	 * @return the logFromAgent
	 */
	public String getLogFromAgent() {
		return logFromAgent;
	}




	/**
	 * @param AgentLanguage the AgentLanguage to set
	 */
	public void setLogAgentLanguage(String AgentLanguage) {
		this.logAgentLanguage = AgentLanguage;
	}

	/**
	 * @return the AgentLanguage
	 */
	public String getLogAgentLanguage() {
		return logAgentLanguage;
	}




	/**
	 * @param logFromClass the logFromClass to set
	 */
	public void setLogFromClass(String logFromClass) {
		this.logFromClass = logFromClass;
	}




	/**
	 * @return the logFromMethod
	 */
	public String getLogFromMethod() {
		return logFromMethod;
	}




	/**
	 * @param logFromMethod the logFromMethod to set
	 */
	public void setLogFromMethod(String logFromMethod) {
		this.logFromMethod = logFromMethod;
	}




	/**
	 * @return the logErrorLine
	 */
	public Integer getLogErrorLine() {
		return logErrorLine;
	}




	/**
	 * @param logErrorLine the logErrorLine to set
	 */
	public void setLogErrorLine(Integer logErrorLine) {
		this.logErrorLine = logErrorLine;
	}




	/**
	 * @return the logExceptionId
	 */
	public String getLogExceptionId() {
		return logExceptionId;
	}




	/**
	 * @param logExceptionId the logExceptionId to set
	 */
	public void setLogExceptionId(String logExceptionId) {
		this.logExceptionId = logExceptionId;
	}




	/**
	 * @return the logErrorMessage
	 */
	public String getLogErrorMessage() {
		return logErrorMessage;
	}




	/**
	 * @param logErrorMessage the logErrorMessage to set
	 */
	public void setLogErrorMessage(String logErrorMessage) {
		this.logErrorMessage = logErrorMessage;
	}




	/**
	 * @return the logMessage
	 */
	public String getLogMessage() {
		return logMessage;
	}




	/**
	 * @param logMessage the logMessage to set
	 */
	public void setLogMessage(String logMessage) {
		this.logMessage = logMessage;
	}


	/**
	 * @return the logStackTrace
	 */
	public Vector<String> getLogStackTrace() {
		return logStackTrace;
	}




	/**
	 * @param logStackTrace the logStackTrace to set
	 */
	public void setLogStackTrace(Vector<String> logStackTrace) {
		this.logStackTrace = logStackTrace;
	}


	public Document getDocToLog() {
		return docToLog;
	}

	public void setDocToLog(Document docToLog) {
		this.docToLog = docToLog;
	}

	public boolean save() {
		try {
			if(logDatabase==null) throw(new Exception("Cannot retrieve log database."));


			//Create log document and set default data		
			Document logDoc = logDatabase.createDocument();
			logDoc.replaceItemValue("Form", "LogEvent");
			logDoc.replaceItemValue("LogEventType", logEventType.name());
			logDoc.replaceItemValue("LogUserName", logUserName);
			logDoc.replaceItemValue("LogEffectiveName", logEffectiveName);

			logDoc.replaceItemValue("LogEventTime", logEventTime);
			logDoc.replaceItemValue("LogAgentStartTime", logEventStartTime);

			logDoc.replaceItemValue("LogSeverity", logSeverity.toString());
			logDoc.replaceItemValue("LogSeverityNbr", logSeverity.intValue());

			logDoc.replaceItemValue("LogAccessLevel", logAccessLevel);
			logDoc.replaceItemValue("LogUserRoles", logUserRoles);

			logDoc.replaceItemValue("logClientVersion", logClientVersion);

			//Database info
			logDoc.replaceItemValue("LogFromServer", logFromServer);
			logDoc.replaceItemValue("LogFromDatabase", logFromDatabase);
			logDoc.replaceItemValue("LogFromDatabaseTitle", logFromDatabaseTitle);

			logDoc.replaceItemValue("LogFromAgent", logFromAgent);
			logDoc.replaceItemValue("LogAgentLanguage", logAgentLanguage);

			//Starting point
			logDoc.replaceItemValue("LogFromAgent", logFromAgent);
			logDoc.replaceItemValue("LogAgentLanguage", logAgentLanguage);
			logDoc.replaceItemValue("LogFromClass", logFromClass);
			logDoc.replaceItemValue("LogFromMethod", logFromMethod);


			//Error
			logDoc.replaceItemValue("logExceptionId", logExceptionId);
			logDoc.replaceItemValue("LogErrorLine", logErrorLine==null || logErrorLine<0?"-unknown-":String.valueOf(logErrorLine));
			logDoc.replaceItemValue("LogErrorMessage", limitLength(logErrorMessage));

			logDoc.replaceItemValue("LogMessage", limitLength(logMessage));

			if(logStackTrace!=null) {
				logDoc.replaceItemValue("logStackTrace", logStackTrace.subList(0, logStackTrace.size()>MAXSTACK?MAXSTACK+1:logStackTrace.size()));
			}

			if (docToLog != null && docToLog.isValid()) {
				Database dbDocToLog = docToLog.getParentDatabase();
				RichTextItem rtitem = logDoc.createRichTextItem("LogDocInfo");
				rtitem.appendText("The document associated with this event is:");
				rtitem.addNewLine(1);
				rtitem.appendText("Server: " + dbDocToLog.getServer());
				rtitem.addNewLine(1);
				rtitem.appendText("Database: " + dbDocToLog.getFilePath());
				rtitem.addNewLine(1);
				rtitem.appendText("UNID: " + docToLog.getUniversalID());
				rtitem.addNewLine(1);
				rtitem.appendText("Note ID: " + docToLog.getNoteID());
				rtitem.addNewLine(1);
				rtitem.appendText("DocLink: ");
				rtitem.appendDocLink(docToLog, docToLog.getUniversalID());
			}

			// make sure Depositor-level users can add documents too
			logDoc.appendItemValue("$PublicAccess", "1");
			logDoc.save(true, true);

		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private String limitLength(String input) {
		String output;
		if(input==null || input.length()<=500) {
			output = input;
		} else {
			output = input.substring(1, 400) + "...";
			output += input.substring(input.length()-100);
		}
		return output;
	}

	protected static Level getDefaultLevel(EventType type) {
		switch (type) {
		case DEBUG: return Level.FINEST;
		case EVENT: return Level.INFO;
		case ERROR: return Level.WARNING;
		default: return Level.INFO;
		}
	}
}
