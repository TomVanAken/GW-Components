package com.gw.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import org.openntf.domino.*;
import org.openntf.domino.utils.*;
import org.openntf.domino.utils.Factory.SessionType;

import com.gw.common.log.OpenLog;

public class ConfigurationManager {
	/**
	 * 	Configuration Manager class
	 * 		- provides access to all configuration items and returns the relevant values
	 * 		- if a value is not found, than a 'null' is returned 
	 */	
	private HashMap<String, String> hmConfDocs = new HashMap<String, String>();
	private Calendar cacheTime;
	private static final int refreshMinutes=5;  //time (in minutes) cache is reset
	private static OpenLog oLog = new OpenLog();

	public ConfigurationManager() {
		init();
	}

	private synchronized void init()  {
		try {
			hmConfDocs.clear();
			//Get all cofig docs and put id in hashmap for faster retrieval
			Session session = Factory.getSession(SessionType.SIGNER);
			Database db = session.getCurrentDatabase();
			View vwConfig = db.getView("_vwLkupConfigsByID");
			vwConfig.setAutoUpdate(false);
			ViewNavigator vwNav = vwConfig.createViewNav();
			vwNav.setBufferMaxEntries(400);
			vwNav.setEntryOptions(ViewNavigator.VN_ENTRYOPT_NOCOUNTDATA);

			for(ViewEntry veConfig:vwNav)  {
				//Add key to HashMap
				String sConfigKey = veConfig.getColumnValues().elementAt(0).toString();
				String sUnid = veConfig.getUniversalID();
				hmConfDocs.put(sConfigKey, sUnid);
			}
			cacheTime=Calendar.getInstance();
		} catch (Exception e) {
			oLog.logException(e);
		}
	}


	public String getText( String strConfig){
		/**
		 * 
		 */
		String strReturn = "";

		try{
			Document docConfig = getDocument(strConfig);
			if(docConfig==null) return "";
			// return value (field) depends on type
			String strType = docConfig.getItemValueString("$fldType");
			// text
			if (strType.equals("TXT"))
				strReturn = docConfig.getItemValueString("$fldValueTxt");
			else 
				// radio
				if (strType.equals("RBN")) 
					strReturn = docConfig.getItemValueString("$fldValueRbn");
		}catch(Exception e){
			oLog.logException(e);			
		}
		return strReturn;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<String> getTextList( String strConfig){
		ArrayList<String> returnValue = new ArrayList<String>();

		try {
			Document docConfig = getDocument(strConfig);
			if(docConfig==null) return returnValue;

			// return value (field) depends on type
			String strType = docConfig.getItemValueString("$fldType");

			if(strType.equals("CBX")) 
				// checkbox
				returnValue = docConfig.getItemValue("$fldValueCbx", returnValue.getClass());
			else if(strType.equals("NAM")) {
				Session s = Factory.getSession(SessionType.CURRENT);
				ArrayList<String> arrNames = docConfig.getItemValue("$fldValueNam", returnValue.getClass());
				for(String n:arrNames) {
					Name name = s.createName(n);
					returnValue.add(name.getAbbreviated());
				}
			}
			else if(strType.equals("TXL")) {
				// text list
				returnValue = docConfig.getItemValue("$fldValueTxl", returnValue.getClass());
			}
		}catch(Exception e){
			oLog.logException(e);
		}

		return returnValue;
	}


	public synchronized String getNextSequence(String strConfig) {
		StringBuilder sb = new StringBuilder();
		try{
			Document docConfig = getDocument( strConfig);
			if(docConfig==null) return "";
			sb.append(docConfig.getItemValueString("$fldPrefix"));
			if(docConfig.getItemValueString("$fldResetYearly").equals("1")) {
				Integer year = Calendar.getInstance().get(Calendar.YEAR); //Current year
				//Add Year as Prefix
				if(!docConfig.hasItem("$fldCurrentYear") || !Integer.valueOf(docConfig.getItemValueInteger("$fldCurrentYear")).equals(year)) {
					//Set next year;
					docConfig.replaceItemValue("$fldCurrentYear", year);
					docConfig.replaceItemValue("$fldValueSequence", 0);
				}
				sb.append(year);
				sb.append("-");
			}

			//Get next sequence
			Integer value = docConfig.getItemValueInteger("$fldValueSequence");
			value++;
			docConfig.replaceItemValue("$fldValueSequence", value);
			String sValue = String.valueOf(value);

			//Mininum Characters
			int minChars = docConfig.getItemValueInteger("$fldMinChars");
			if(minChars > sValue.length()) {
				char[] chars = new char[minChars - sValue.length()];
				Arrays.fill(chars, '0');
				String fill = new String(chars);
				sValue = fill + sValue;
			}

			sb.append(sValue);
			sb.append(docConfig.getItemValueString("$fldSuffix"));
			docConfig.save(true, true);
		}catch(Exception e){
			oLog.logException(e);
		}
		return sb.toString();
	}


	public DateTime getDateTime( String strConfig){
		DateTime dtReturn = null;

		try{
			Document docConfig = getDocument(strConfig);
			if(docConfig==null)return dtReturn;
			Item itm = docConfig.getFirstItem("$fldValueDateTime");
			dtReturn = itm.getDateTimeValue();
		}catch(Exception e){
			oLog.logException(e);		
		}		
		return dtReturn;
	}

	public Calendar getCalendar( String strConfig){
		DateTime dtReturn = null;

		try{
			dtReturn = getDateTime(strConfig);
			Calendar cal = Calendar.getInstance();
			cal.setTime(dtReturn.toJavaDate());
			return cal;
		}catch(Exception e){
			oLog.logException(e);
			return null;
		}
	}		


	public Database getDatabase( String strConfig){
		return getDatabase(strConfig, false);
	}


	public Database getDatabase( String strConfig, boolean asSigner){
		Database dbReturn = null;

		try{
			Document docConfig =  getDocument( strConfig);
			if(docConfig==null) return null;
			String strServer = docConfig.getItemValueString("$fldValueDbrServer");
			String strFilePath = docConfig.getItemValueString("$fldValueDbrFile");
			Session session = Factory.getSession(asSigner?SessionType.FULL_ACCESS:SessionType.CURRENT);
			dbReturn = session.getDatabase(strServer, strFilePath);

		}catch(Exception e){
			oLog.logException(e);			
		}	

		return dbReturn;
	}

	
	public Double getDouble(String strConfig) {
		Double returnVal = 0.0;
		try {
			Document docConfig = getDocument(strConfig);
			if(docConfig!=null) {
				@SuppressWarnings("unchecked")
				Vector vectValues = docConfig.getItemValue("$fldValueNum");
				returnVal = (Double) vectValues.get(0);
			}
		} catch (Exception e) {
			oLog.logException(e);		
		}

		return returnVal;
	}

	public Integer getInteger(String strConfig) {
		return getDouble(strConfig).intValue();
	}

	public Document getDocument(String strConfig) {
		String strKey = "";
		try {
			//Make sure cache is still valid; if not initialize
			Calendar testCache = Calendar.getInstance();
			testCache.add(Calendar.MINUTE, -refreshMinutes);
			if(testCache.after(cacheTime) || !hmConfDocs.containsKey(strConfig))  init();

			//if config doc is not in 
			Session session = Factory.getSession(SessionType.SIGNER);
			Database db = session.getCurrentDatabase();
			Document doc = null;
			//retrieve configuration via cached id
			strKey = hmConfDocs.get(strConfig);
			doc = db.getDocumentByUNID(strKey);
			if(doc==null) throw(new ConfigNotFoundException(strConfig));

			return doc;
		} catch(ConfigNotFoundException ce) {
			System.out.println(ce.toString());
			return null;
		} catch(Exception e) {
			System.out.println(e.toString());
			System.out.println("strConfig: " + strConfig);
			return null;
		}
	}

	class ConfigNotFoundException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4636189300082178311L;

		private ConfigNotFoundException(String strConfig) {
			super("Configuration document for " + strConfig + " not found.");
		}
	}
}