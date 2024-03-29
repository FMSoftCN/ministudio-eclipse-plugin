/*********************************************************************
 * Copyright (C) 2002 ~ 2010, Beijing FMSoft Technology Co., Ltd.
 * Room 902, Floor 9, Taixing, No.11, Huayuan East Road, Haidian
 * District, Beijing, P. R. CHINA 100191.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Beijing FMSoft Technology Co., Ltd. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance you entered into with FMSoft.
 *
 *			http://www.minigui.com
 *
 *********************************************************************/

package org.eclipse.cdt.fmsoft.hybridos.mstudio.wizards;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;

import java.sql.Timestamp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

//import MStudioParserIniFile.MStudioParserIniProperty;
//import MStudioParseIniFile.MStudioParserIniSection;


public class MStudioParserIniFile {

	/** Variable to represent the date format */
	private String mstrDateFmt = "yyyy-MM-dd";

	/** Variable to represent the timestamp format */
	private String mstrTimeStampFmt = "yyyy-MM-dd HH:mm:ss";

	/** Variable to denote the successful load operation. */
	@SuppressWarnings("unused")
	private boolean mblnLoaded = false;

	/** Variable to hold the ini file name and full path */
	private String mstrFile = null;

	/** Variable to hold the sections in an ini file. */
	private LinkedHashMap<String, MStudioParserIniSection> mhmapSections = null;

	/** Variable to hold environment variables **/
	private Properties mpropEnv = null;

	/**
	 * Create a iniFile object from the file named in the parameter.
	 * @param iniFile The full path and name of the ini file to be used.
	 */
	public MStudioParserIniFile(String iniFile) {
		// TODO Auto-generated constructor stub
		this.mpropEnv = getEnvVars();
		this.mhmapSections = new LinkedHashMap<String, MStudioParserIniSection>();
		this.mstrFile = iniFile;
		// Load the specified INI file.
		if (checkFile(iniFile)) loadFile();
	}

	/*------------------------------------------------------------------------------
	 * Getters
	 ------------------------------------------------------------------------------*/
	/**
	 * Returns the ini file name being used.
	 * @return the INI file name.
	 */
	public String getFileName() {
		return this.mstrFile;
	}

	/**
	 * Returns the specified string property from the specified section.
	 * @param pstrSection the INI section name.
	 * @param pstrProp the property to be retrieved.
	 * @return the string property value.
	 */
	public String getStringProperty(String pstrSection, String pstrProp) {
		String strRet = null;
		MStudioParserIniProperty objProp = null;
		MStudioParserIniSection objSec =
			(MStudioParserIniSection) this.mhmapSections.get(pstrSection);

		if (objSec != null) {
			objProp = objSec.getProperty(pstrProp);
			if (objProp != null) {
				strRet = objProp.getPropValue();
				objProp = null;
			}
			objSec = null;
		}

		return strRet;
	}

	/**
	 * Returns the specified boolean property from the specified section.
	 * This method considers the following values as boolean values.
	 * <ol>
	 *      <li>YES/yes/Yes - boolean true</li>
	 *      <li>NO/no/No  - boolean false</li>
	 *      <li>1 - boolean true</li>
	 *      <li>0 - boolean false</li>
	 *      <li>TRUE/True/true - boolean true</li>
	 *      <li>FALSE/False/false - boolean false</li>
	 * </ol>
	 * @param pstrSection the INI section name.
	 * @param pstrProp the property to be retrieved.
	 * @return the boolean value
	 */
	public Boolean getBooleanProperty(String pstrSection, String pstrProp) {

		boolean blnRet = false;
		String strVal = null;
		MStudioParserIniProperty objProp = null;
		MStudioParserIniSection objSec =
			(MStudioParserIniSection) this.mhmapSections.get(pstrSection);

		if (objSec != null) {
			objProp = objSec.getProperty(pstrProp);
			if (objProp != null) {
				strVal = objProp.getPropValue().toUpperCase();
				if (strVal.equals("YES") || strVal.equals("TRUE")
						|| strVal.equals("1")) {
					blnRet = true;
				}
				objProp = null;
			}
			objSec = null;
		}

		return new Boolean(blnRet);
	}

	/**
	 * Returns the specified integer property from the specified section.
	 * @param pstrSection the INI section name.
	 * @param pstrProp the property to be retrieved.
	 * @return the integer property value.
	 */
	public Integer getIntegerProperty(String pstrSection, String pstrProp) {

		Integer intRet = null;
		String strVal = null;
		MStudioParserIniProperty objProp = null;
		MStudioParserIniSection objSec =
			(MStudioParserIniSection) this.mhmapSections.get(pstrSection);

		if (objSec != null) {
			objProp = objSec.getProperty(pstrProp);
			try {
				if (objProp != null) {
					strVal = objProp.getPropValue();
					if (strVal != null) intRet = new Integer(strVal);
				}
			} catch (NumberFormatException NFExIgnore) {
			} finally {
				if (objProp != null)
					objProp = null;
			}
			objSec = null;
		}

		return intRet;
	}

	/**
	 * Returns the specified long property from the specified section.
	 * @param pstrSection the INI section name.
	 * @param pstrProp the property to be retrieved.
	 * @return the long property value.
	 */
	public Long getLongProperty(String pstrSection, String pstrProp) {

		Long lngRet  = null;
		String strVal  = null;
		MStudioParserIniProperty objProp = null;
		MStudioParserIniSection objSec =
			(MStudioParserIniSection) this.mhmapSections.get(pstrSection);

		if (objSec != null) {
			objProp = objSec.getProperty(pstrProp);
			try {
				if (objProp != null) {
					strVal = objProp.getPropValue();
					if (strVal != null) lngRet = new Long(strVal);
				}
			} catch (NumberFormatException NFExIgnore) {
			} finally {
				if (objProp != null)
					objProp = null;
			}
			objSec = null;
		}

		return lngRet;
	}

	/**
	 * Returns the specified double property from the specified section.
	 * @param pstrSection the INI section name.
	 * @param pstrProp the property to be retrieved.
	 * @return the double property value.
	 */
	public Double getDoubleProperty(String pstrSection, String pstrProp) {

		Double dblRet = null;
		String strVal = null;
		MStudioParserIniProperty objProp = null;
		MStudioParserIniSection objSec =
			(MStudioParserIniSection) this.mhmapSections.get(pstrSection);

		if (objSec != null) {
			objProp = objSec.getProperty(pstrProp);
			try {
				if (objProp != null) {
					strVal = objProp.getPropValue();
					if (strVal != null) dblRet = new Double(strVal);
				}
			} catch (NumberFormatException NFExIgnore) {
			} finally {
				if (objProp != null)
					objProp = null;
			}
			objSec = null;
		}

		return dblRet;
	}

	/**
	 * Returns the specified date property from the specified section.
	 * @param pstrSection the INI section name.
	 * @param pstrProp the property to be retrieved.
	 * @return the date property value.
	 */
	public Date getDateProperty(String pstrSection, String pstrProp) {

		Date dtRet = null;
		String strVal = null;
		DateFormat dtFmt = null;
		MStudioParserIniProperty objProp = null;
		MStudioParserIniSection objSec =
			(MStudioParserIniSection) this.mhmapSections.get(pstrSection);

		if (objSec != null) {
			objProp = objSec.getProperty(pstrProp);
			try {
				if (objProp != null)
					strVal = objProp.getPropValue();
				if (strVal != null) {
					dtFmt = new SimpleDateFormat(this.mstrDateFmt);
					dtRet = dtFmt.parse(strVal);
				}
			} catch (ParseException PExIgnore) {
			} catch (IllegalArgumentException IAEx) {
			} finally {
				if (objProp != null)
					objProp = null;
			}
			objSec = null;
		}

		return dtRet;
	}

	/**
	 * Returns the specified date property from the specified section.
	 * @param pstrSection the INI section name.
	 * @param pstrProp the property to be retrieved.
	 * @return the date property value.
	 */
	public Date getTimestampProperty(String pstrSection, String pstrProp) {

		Timestamp tsRet = null;
		Date dtTmp = null;
		String strVal = null;
		DateFormat dtFmt = null;
		MStudioParserIniProperty objProp = null;
		MStudioParserIniSection objSec =
			(MStudioParserIniSection) this.mhmapSections.get(pstrSection);

		if (objSec != null) {
			objProp = objSec.getProperty(pstrProp);
			try {
				if (objProp != null)
					strVal = objProp.getPropValue();
				if (strVal != null) {
					dtFmt = new SimpleDateFormat(this.mstrDateFmt);
					dtTmp = dtFmt.parse(strVal);
					tsRet = new Timestamp(dtTmp.getTime());
				}
			} catch (ParseException PExIgnore) {
			} catch (IllegalArgumentException IAEx) {
			} finally {
				if (objProp != null)
					objProp = null;
			}
			objSec = null;
		}

		return tsRet;
	}

	/*------------------------------------------------------------------------------
	 * Setters
	 ------------------------------------------------------------------------------*/
	/**
	 * Sets the comments associated with a section.
	 * @param pstrSection the section name
	 * @param pstrComments the comments.
	 */
	public void addSection(String pstrSection, String pstrComments) {

		MStudioParserIniSection objSec =
			(MStudioParserIniSection) this.mhmapSections.get(pstrSection);

		if (objSec == null) {
			objSec = new MStudioParserIniSection(pstrSection);
			this.mhmapSections.put(pstrSection, objSec);
		}

		objSec.setSecComments(delRemChars(pstrComments));
		objSec = null;
	}

	/**
	 * Sets the specified string property.
	 * @param pstrSection the INI section name.
	 * @param pstrProp the property to be set.
	 * @pstrVal the string value to be persisted
	 */
	public void setStringProperty(String pstrSection, String pstrProp,
			String pstrVal, String pstrComments) {

		MStudioParserIniSection objSec =
			(MStudioParserIniSection) this.mhmapSections.get(pstrSection);

		if (objSec == null) {
			objSec = new MStudioParserIniSection(pstrSection);
			this.mhmapSections.put(pstrSection, objSec);
		}

		objSec.setProperty(pstrProp, pstrVal, pstrComments);
	}

	/**
	 * Sets the specified boolean property.
	 * @param pstrSection the INI section name.
	 * @param pstrProp the property to be set.
	 * @param pblnVal the boolean value to be persisted
	 */
	public void setBooleanProperty(String pstrSection, String pstrProp,
			boolean pblnVal, String pstrComments) {

		MStudioParserIniSection objSec =
			(MStudioParserIniSection) this.mhmapSections.get(pstrSection);

		if (objSec == null) {
			objSec = new MStudioParserIniSection(pstrSection);
			this.mhmapSections.put(pstrSection, objSec);
		}

		if (pblnVal)
			objSec.setProperty(pstrProp, "TRUE", pstrComments);
		else
			objSec.setProperty(pstrProp, "FALSE", pstrComments);
	}

	/**
	 * Sets the specified integer property.
	 * @param pstrSection the INI section name.
	 * @param pstrProp the property to be set.
	 * @param pintVal the int property to be persisted.
	 */
	public void setIntegerProperty(String pstrSection, String pstrProp,
			int pintVal, String pstrComments) {

		MStudioParserIniSection objSec =
			(MStudioParserIniSection) this.mhmapSections.get(pstrSection);

		if (objSec == null) {
			objSec = new MStudioParserIniSection(pstrSection);
			this.mhmapSections.put(pstrSection, objSec);
		}

		objSec.setProperty(pstrProp, Integer.toString(pintVal), pstrComments);
	}

	/**
	 * Sets the specified long property.
	 * @param pstrSection the INI section name.
	 * @param pstrProp the property to be set.
	 * @param plngVal the long value to be persisted.
	 */
	public void setLongProperty(String pstrSection, String pstrProp,
			long plngVal, String pstrComments) {

		MStudioParserIniSection objSec =
			(MStudioParserIniSection) this.mhmapSections.get(pstrSection);

		if (objSec == null) {
			objSec = new MStudioParserIniSection(pstrSection);
			this.mhmapSections.put(pstrSection, objSec);
		}

		objSec.setProperty(pstrProp, Long.toString(plngVal), pstrComments);
	}

	/**
	 * Sets the specified double property.
	 * @param pstrSection the INI section name.
	 * @param pstrProp the property to be set.
	 * @param pdblVal the double value to be persisted.
	 */
	public void setDoubleProperty(String pstrSection, String pstrProp,
			double pdblVal, String pstrComments) {

		MStudioParserIniSection objSec =
			(MStudioParserIniSection) this.mhmapSections.get(pstrSection);

		if (objSec == null) {
			objSec = new MStudioParserIniSection(pstrSection);
			this.mhmapSections.put(pstrSection, objSec);
		}

		objSec.setProperty(pstrProp, Double.toString(pdblVal), pstrComments);
	}

	/**
	 * Sets the specified java.util.Date property.
	 * @param pstrSection the INI section name.
	 * @param pstrProp the property to be set.
	 * @param pdtVal the date value to be persisted.
	 */
	public void setDateProperty(String pstrSection, String pstrProp,
			Date pdtVal, String pstrComments) {

		MStudioParserIniSection objSec =
			(MStudioParserIniSection) this.mhmapSections.get(pstrSection);

		if (objSec == null) {
			objSec = new MStudioParserIniSection(pstrSection);
			this.mhmapSections.put(pstrSection, objSec);
		}

		objSec.setProperty(pstrProp, utilDateToStr(pdtVal, this.mstrDateFmt), pstrComments);
	}

	/**
	 * Sets the specified java.sql.Timestamp property.
	 * @param pstrSection the INI section name.
	 * @param pstrProp the property to be set.
	 * @param ptsVal the timestamp value to be persisted.
	 */
	public void setTimestampProperty(String pstrSection, String pstrProp,
			Timestamp ptsVal, String pstrComments) {

		MStudioParserIniSection objSec =
			(MStudioParserIniSection) this.mhmapSections.get(pstrSection);

		if (objSec == null) {
			objSec = new MStudioParserIniSection(pstrSection);
			this.mhmapSections.put(pstrSection, objSec);
		}

		objSec.setProperty(pstrProp, timeToStr(ptsVal, this.mstrTimeStampFmt), pstrComments);
	}

	/**
	 * Sets the format to be used to interpreat date values.
	 * @param pstrDtFmt the format string
	 * @throws IllegalArgumentException if the if the given pattern is invalid
	 */
	public void setDateFormat(String pstrDtFmt)
		throws IllegalArgumentException {

		if (!checkDateTimeFormat(pstrDtFmt))
			throw new IllegalArgumentException("The specified date pattern is invalid!");
		this.mstrDateFmt = pstrDtFmt;
	}

	/**
	 * Sets the format to be used to interpreat timestamp values.
	 * @param pstrTSFmt the format string
	 * @throws IllegalArgumentException if the if the given pattern is invalid
	 */
	public void setTimeStampFormat(String pstrTSFmt) {
		if (!checkDateTimeFormat(pstrTSFmt))
			throw new IllegalArgumentException("The specified timestamp pattern is invalid!");
		this.mstrTimeStampFmt = pstrTSFmt;
	}

	/*------------------------------------------------------------------------------
	 * Public methods
	 ------------------------------------------------------------------------------*/
	public int getTotalSections() {
		return this.mhmapSections.size();
	}

	/**
	 * Returns a string array containing names of all sections in INI file.
	 * @return the string array of section names
	 */
	public String[] getAllSectionNames() {

		int iCntr = 0;
		Iterator<String> iter = null;
		String[] arrRet = null;

		try {
			if (this.mhmapSections.size() > 0) {
				arrRet = new String[this.mhmapSections.size()];
				for (iter = this.mhmapSections.keySet().iterator(); ; iter.hasNext()) {
					arrRet[iCntr] = (String) iter.next();
					iCntr++;
				}
			}
		} catch (NoSuchElementException NSEExIgnore) {
		} finally {
			if (iter != null)
				iter = null;
		}

		return arrRet;
	}

	/**
	 * Returns a string array containing names of all the properties under specified section.
	 * @param pstrSection the name of the section for which names of properties is to be retrieved.
	 * @return the string array of property names.
	 */
	public String[] getPropertyNames(String pstrSection) {

		String[] arrRet = null;
		MStudioParserIniSection objSec =
			(MStudioParserIniSection) this.mhmapSections.get(pstrSection);

		if (objSec != null) {
			arrRet = objSec.getPropNames();
			objSec = null;
		}

		return arrRet;
	}

	/**
	 * Returns a map containing all the properties under specified section.
	 * @param pstrSection the name of the section for which properties are to be retrieved.
	 * @return the map of properties.
	 */
	public Map<String, MStudioParserIniProperty> getProperties(String pstrSection) {

		Map<String, MStudioParserIniProperty> hmRet = null;
		MStudioParserIniSection objSec =
			(MStudioParserIniSection) this.mhmapSections.get(pstrSection);

		if (objSec != null) {
			hmRet = objSec.getProperties();
			objSec = null;
		}

		return hmRet;
	}

	/**
	 * Removed specified property from the specified section. If the specified
	 * section or the property does not exist, does nothing.
	 * @param pstrSection the section name.
	 * @param pstrProp the name of the property to be removed.
	 */
	public void removeProperty(String pstrSection, String pstrProp) {

		MStudioParserIniSection objSec =
			(MStudioParserIniSection) this.mhmapSections.get(pstrSection);

		if (objSec != null) {
			objSec.removeProperty(pstrProp);
			objSec = null;
		}
	}

	/**
	 * Removes the specified section if one exists, otherwise does nothing.
	 * @param pstrSection the name of the section to be removed.
	 */
	public void removeSection(String pstrSection) {
		if (this.mhmapSections.containsKey(pstrSection))
			this.mhmapSections.remove(pstrSection);
	}

	/**
	 * Flush changes back to the disk file. If the disk file does not exists then
	 * creates the new one.
	 */
	public boolean save() {

		boolean blnRet = false;
		File objFile = null;
		String strName = null;
		String strTemp = null;
		Iterator<String> itrSec = null;
		MStudioParserIniSection objSec = null;
		FileWriter objWriter = null;

		try {
			if (this.mhmapSections.size() == 0)
				return false;
			objFile = new File(this.mstrFile);
			if (objFile.exists())
				objFile.delete();

			objWriter = new FileWriter(objFile);
			itrSec = this.mhmapSections.keySet().iterator();

			while (itrSec.hasNext()) {
				strName = (String) itrSec.next();
				objSec = (MStudioParserIniSection) this.mhmapSections.get(strName);
				strTemp = objSec.toString();
				objWriter.write(strTemp);
				objWriter.write(System.getProperty("line.separator"));
				objSec = null;
			}
			blnRet = true;
		} catch (IOException IOExIgnore) {
		} finally {
			if (objWriter != null) {
				closeWriter(objWriter);
				objWriter = null;
			}
			if (objFile != null)
				objFile = null;
			if (itrSec != null)
				itrSec = null;
		}

		return blnRet;
	}

	/*------------------------------------------------------------------------------
	 * Helper functions
	 *----------------------------------------------------------------------------*/
	/**
	 * Procedure to read environment variables.
	 * Thanx to http://www.rgagnon.com/howto.html for this implementation.
	 */
	private Properties getEnvVars() {

		Process p = null;
		Properties envVars = new Properties();

		try {
			Runtime r = Runtime.getRuntime();
			String OS = System.getProperty("os.name").toLowerCase();

			if (OS.indexOf("windows 9") > -1) {
				p = r.exec("command.com /c set");
			} else if ((OS.indexOf("nt") > -1) || (OS.indexOf("windows 2000") > -1)
					|| (OS.indexOf("windows xp") > -1)) {
				p = r.exec("cmd.exe /c set");
			} else {
				// our last hope, we assume Unix (thanks to H. Ware for the fix)
				p = r.exec("env");
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;

			while ((line = br.readLine()) != null) {
				int idx = line.indexOf('=');
				String key = line.substring(0, idx);
				String value = line.substring(idx + 1);
				envVars.setProperty(key, value);
			}
		} catch (Exception ExIgnore) {
		}

		return envVars;
	}

	/**
	 * Helper function to check the date time formats.
	 * @param pstrDtFmt the date time format string to be checked.
	 * @return true for valid date/time format, false otherwise.
	 */
	private boolean checkDateTimeFormat(String pstrDtFmt) {

		boolean blnRet = false;
		DateFormat objFmt = null;

		try {
			objFmt = new SimpleDateFormat(pstrDtFmt);
			blnRet = true;
		} catch (NullPointerException NPExIgnore) {
		} catch (IllegalArgumentException IAExIgnore) {
		} finally {
			if (objFmt != null)
				objFmt = null;
		}

		return blnRet;
	}

	/**
	 * Reads the INI file and load its contentens into a section collection after
	 * parsing the file line by line.
	 */
	private void loadFile() {

		int iPos = -1;
		String strLine = null;
		String strSection = null;
		String strRemarks = null;
		BufferedReader objBRdr = null;
		FileReader objFRdr = null;
		MStudioParserIniSection objSec = null;

		try {
			objFRdr = new FileReader(this.mstrFile);
			if (objFRdr != null) {
				objBRdr = new BufferedReader(objFRdr);
				if (objBRdr != null) {
					while (objBRdr.ready()) {
						iPos = -1;
						strLine = null;
						strLine = objBRdr.readLine().trim();
						if (strLine == null) {
						}
						else if (strLine.length() == 0) {
						} else if (strLine.substring(0, 1).equals(";")) {
							if (strRemarks == null) {
								strRemarks = strLine.substring(1);
							} else if (strRemarks.length() == 0) {
								strRemarks = strLine.substring(1);
							} else {
								strRemarks = strRemarks
									+ System.getProperty("line.separator") + strLine.substring(1);
							}
						} else if (strLine.startsWith("[") && strLine.endsWith("]")) {
							// Section start reached create new section
							if (objSec != null)
								this.mhmapSections.put(strSection.trim(), objSec);
							objSec = null;
							strSection = strLine.substring(1, strLine.length() - 1);
							objSec = new MStudioParserIniSection(strSection.trim(), strRemarks);
							strRemarks = null;
						} else if ((iPos = strLine.indexOf("=")) > 0 && objSec != null) {
							// read the key value pair 012345=789
							objSec.setProperty(strLine.substring(0, iPos).trim(),
									strLine.substring(iPos + 1).trim(),
									strRemarks);
							strRemarks = null;
						}
					}
					if (objSec != null)
						this.mhmapSections.put(strSection.trim(), objSec);
					this.mblnLoaded = true;
				}
			}
		} catch (FileNotFoundException FNFExIgnore) {
			this.mhmapSections.clear();
		} catch (IOException IOExIgnore) {
			this.mhmapSections.clear();
		} catch (NullPointerException NPExIgnore) {
			this.mhmapSections.clear();
		} finally {
			if (objBRdr != null) {
				closeReader(objBRdr);
				objBRdr = null;
			}
			if (objFRdr != null) {
				closeReader(objFRdr);
				objFRdr = null;
			}
			if (objSec != null)
				objSec = null;
		}
	}

	/**
	 * Helper function to close a reader object.
	 * @param pobjRdr the reader to be closed.
	 */
	private void closeReader(Reader pobjRdr) {
		if (pobjRdr == null)
			return;

		try {
			pobjRdr.close();
		} catch (IOException IOExIgnore) {
		}
	}

	/**
	 * Helper function to close a writer object.
	 * @param pobjWriter the writer to be closed.
	 */
	private void closeWriter(Writer pobjWriter) {
		if (pobjWriter == null)
			return;

		try {
			pobjWriter.close();
		} catch (IOException IOExIgnore) {
		}
	}

	/**
	 * Helper method to check the existance of a file.
	 * @param the full path and name of the file to be checked.
	 * @return true if file exists, false otherwise.
	 */
	private boolean checkFile(String pstrFile) {
		boolean blnRet = false;
		File objFile = null;

		try {
			objFile = new File(pstrFile);
			blnRet = (objFile.exists() && objFile.isFile());
		} catch (Exception e) {
			blnRet = false;
		} finally {
			if (objFile != null)
				objFile = null;
		}

		return blnRet;
	}

	/**
	 * Converts a java.util.date into String
	 * @param pd Date that need to be converted to String
	 * @param pstrFmt The date format pattern.
	 * @return String
	 */
	private String utilDateToStr(Date pdt, String pstrFmt) {
		String strRet = null;
		SimpleDateFormat dtFmt = null;

		try {
			dtFmt = new SimpleDateFormat(pstrFmt);
			strRet = dtFmt.format(pdt);
		} catch (Exception e) {
			strRet = null;
		} finally {
			if (dtFmt != null)
				dtFmt = null;
		}

		return strRet;
	}

	/**
	 * Converts the given sql timestamp object to a string representation. The format
	 * to be used is to be obtained from the configuration file.
	 *
	 * @param pobjTS the sql timestamp object to be converted.
	 * @param pblnGMT If true formats the string using GMT  timezone
	 * otherwise using local timezone.
	 * @return the formatted string representation of the timestamp.
	 */
	private String timeToStr(Timestamp pobjTS, String pstrFmt) {
		String strRet = null;
		SimpleDateFormat dtFmt = null;

		try {
			dtFmt = new SimpleDateFormat(pstrFmt);
			strRet = dtFmt.format(pobjTS);
		} catch (IllegalArgumentException  iae) {
			strRet = "";
		} catch (NullPointerException npe) {
			strRet = "";
		} finally {
			if (dtFmt != null)
				dtFmt = null;
		}

		return strRet;
	}

	/**
	 * This function deletes the remark characters ';' from source string
	 * @param pstrSrc the source  string
	 * @return the converted string
	 */
	private String delRemChars(String pstrSrc) {
		if (pstrSrc == null)
			return null;

		int intPos = 0;

		while ((intPos = pstrSrc.indexOf(";")) >= 0) {
			if (intPos == 0)
				pstrSrc = pstrSrc.substring(intPos + 1);
			else if (intPos > 0)
				pstrSrc = pstrSrc.substring(0, intPos) + pstrSrc.substring(intPos + 1);
		}

		return pstrSrc;
	}

	/**
	 * This function adds a remark character ';' in source string.
	 * @param pstrSrc source string
	 * @return converted string.
	 */
	private String addRemChars(String pstrSrc) {
		if (pstrSrc == null)
			return null;

		int intLen  = 2;
		int intPos  = 0;
		int intPrev = 0;
		String strLeft  = null;
		String strRight = null;

		while (intPos >= 0) {
			intLen = 2;
			intPos = pstrSrc.indexOf(System.getProperty("line.separator"), intPrev);
			if (intPos < 0) {
				intLen = 1;
				intPos = pstrSrc.indexOf("\n", intPrev);
				if (intPos < 0) intPos = pstrSrc.indexOf("\r", intPrev);
			}
			if (intPos == 0) {
				pstrSrc = "#" + System.getProperty("line.separator") + pstrSrc.substring(intPos + intLen);
				intPrev = intPos + intLen + 1;
			} else if (intPos > 0) {
				strLeft = pstrSrc.substring(0, intPos);
				strRight = pstrSrc.substring(intPos + intLen);
				if (strRight == null)
					pstrSrc = strLeft;
				else if (strRight.length() == 0)
					pstrSrc = strLeft;
				else
					pstrSrc = strLeft + System.getProperty("line.separator") + "#" + strRight;
				intPrev = intPos + intLen + 1;
			}
		}

		if (!pstrSrc.substring(0, 1).equals("#"))
			pstrSrc = "#" + pstrSrc;
		pstrSrc = pstrSrc + System.getProperty("line.separator");

		return pstrSrc;
	}
	/*------------------------------------------------------------------------------
	 * Main entry point to test the functionality.
	 *----------------------------------------------------------------------------*/
	/**
	 * The main entry point for testing.
	 * @param pstrArgs the command line arguments array if any.
	 */
	/*
		public static void main(String[] pstrArgs)
		{
		INIFile objINI = null;
		String  strFile = null;

		if (pstrArgs.length == 0) return;

		strFile = pstrArgs[0];
	// Following call will load the strFile if one exists.
	objINI = new INIFile(strFile);

	// objINI.addSection("QADatabase", "QA database connection details\nUsed for QA Testing");
	// objINI.setStringProperty("QADatabase", "SID", "ORCL", null);
	// objINI.setStringProperty("QADatabase", "UserId", "System", null);
	// objINI.setStringProperty("QADatabase", "Password", "Manager", null);
	// objINI.setStringProperty("QADatabase", "HostName", "DBServer", null);
	// objINI.setIntegerProperty("QADatabase", "Port", 1521, null);
	// objINI.setStringProperty("QADatabase", "OracleHome", "%ORACLE_HOME%", null);

	objINI.setSectionComments("Folders", "Directories where generated files are stored");
	objINI.setStringProperty("Folders", "folder1", "G:\\Temp", null);
	objINI.setStringProperty("Folders", "folder2", "G:\\Temp\\Backup", null);

	// Save changes back to strFile.
	objINI.save();
	objINI = null;
	}*/

	/*------------------------------------------------------------------------------
	 * Private class representing the INI Section.
	 *----------------------------------------------------------------------------*/
	/**
	 * Class to represent the individual ini file section.
	 * @author Prasad P. Khandekar
	 * @version 1.0
	 * @since 1.0
	 */
	private class MStudioParserIniSection {

		/** Variable to hold any comments associated with this section */
		private String mstrComment = null;

		/** Variable to hold the section name. */
		private String mstrName = null;

		/** Variable to hold the properties falling under this section. */
		private LinkedHashMap<String, MStudioParserIniProperty> mhmapProps = null;

		/**
		 * Construct a new section object identified by the name specified in
		 * parameter.
		 * @param pstrSection The new sections name.
		 */
		public MStudioParserIniSection(String pstrSection) {
			this.mstrName = pstrSection;
			this.mhmapProps = new LinkedHashMap<String, MStudioParserIniProperty>();
		}

		/**
		 * Construct a new section object identified by the name specified in
		 * parameter and associated comments.
		 * @param pstrSection The new sections name.
		 * @param pstrComments the comments associated with this section.
		 */
		public MStudioParserIniSection(String pstrSection, String pstrComments) {
			this.mstrName =  pstrSection;
			this.mstrComment = delRemChars(pstrComments);
			this.mhmapProps = new LinkedHashMap<String, MStudioParserIniProperty>();
		}

		/**
		 * Returns any comments associated with this section
		 * @return the comments
		 */
		/*
		public String getSecComments() {
			return this.mstrComment;
		}
		*/

		/**
		 * Returns name of the section.
		 * @return Name of the section.
		 */
		/*
		public String getSecName() {
			return this.mstrName;
		}
		*/
		/**
		 * Sets the comments associated with this section.
		 * @param pstrComments the comments
		 */
		public void setSecComments(String pstrComments) {
			this.mstrComment = delRemChars(pstrComments);
		}

		/**
		 * Sets the section name.
		 * @param pstrName the section name.
		 */
		/*
		public void setSecName(String pstrName) {
			this.mstrName = pstrName;
		}
		*/
		/**
		 * Removes specified property value from this section.
		 * @param pstrProp The name of the property to be removed.
		 */
		public void removeProperty(String pstrProp) {
			if (this.mhmapProps.containsKey(pstrProp))
				this.mhmapProps.remove(pstrProp);
		}

		/**
		 * Creates or modifies the specified property value.
		 * @param pstrProp The name of the property to be created or modified.
		 * @param pstrValue The new value for the property.
		 * @param pstrComments the associated comments
		 */
		public void setProperty(String pstrProp, String pstrValue, String pstrComments) {
			this.mhmapProps.put(pstrProp, new MStudioParserIniProperty(pstrProp, pstrValue, pstrComments));
		}

		/**
		 * Returns a map of all properties.
		 * @return a map of all properties
		 */
		public Map<String, MStudioParserIniProperty> getProperties() {
			return Collections.unmodifiableMap(this.mhmapProps);
		}

		/**
		 * Returns a string array containing names of all the properties under
		 * this section.
		 * @return the string array of property names.
		 */
		public String[] getPropNames() {
			int iCntr  = 0;
			String[] arrRet = null;
			Iterator<String> iter = null;

			try {
				if (this.mhmapProps.size() > 0) {
					arrRet = new String[this.mhmapProps.size()];
					for (iter = this.mhmapProps.keySet().iterator();iter.hasNext();) {
						arrRet[iCntr] = (String) iter.next();
						iCntr++;
					}
				}
			} catch (NoSuchElementException NSEExIgnore) {
				arrRet = null;
			}

			return arrRet;
		}

		/**
		 * Returns underlying value of the specified property.
		 * @param pstrProp the property whose underlying value is to be etrieved.
		 * @return the property value.
		 */
		public MStudioParserIniProperty getProperty(String pstrProp) {
			MStudioParserIniProperty objRet = null;

			if (this.mhmapProps.containsKey(pstrProp))
				objRet = (MStudioParserIniProperty) this.mhmapProps.get(pstrProp);

			return objRet;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {

			Set<String> colKeys = null;
			String strRet = "";
			Iterator<String> iter = null;
			MStudioParserIniProperty objProp = null;
			StringBuffer objBuf = new StringBuffer();

			objBuf.append(System.getProperty("line.separator"));
			if (this.mstrComment != null)
				objBuf.append(addRemChars(this.mstrComment));
			objBuf.append("[" + this.mstrName + "]" + System.getProperty("line.separator"));

			colKeys = this.mhmapProps.keySet();
			if (colKeys != null) {
				iter = colKeys.iterator();
				if (iter != null) {
					while (iter.hasNext()) {
						objProp = (MStudioParserIniProperty) this.mhmapProps.get(iter.next());
						objBuf.append(objProp.toString());
						objBuf.append(System.getProperty("line.separator"));
						objProp = null;
					}
				}
			}

			strRet = objBuf.toString();

			objBuf = null;
			iter = null;
			colKeys = null;

			return strRet;
		}
	}

	/*------------------------------------------------------------------------------
	 * Private class representing the INI Property.
	 *----------------------------------------------------------------------------*/
	/**
	 * This class represents a key value pair called property in an INI file.
	 * @author Prasad P. Khandekar
	 * @version 1.0
	 * @since 1.0
	 */
	private class MStudioParserIniProperty {

		/** Variable to hold name of this property */
		private String mstrName = null;
		/** Variable to hold value of this property */
		private String mstrValue = null;
		/** Variable to hold comments associated with this property */
		private String mstrComments = null;

		/**
		 * Constructor
		 * @param pstrName the name of this property.
		 * @param pstrValue the value of this property.
		 */
		/*
		public MStudioParserIniProperty(String pstrName, String pstrValue) {
			this.mstrName = pstrName;
			this.mstrValue = pstrValue;
		}
		*/
		/**
		 * Constructor
		 * @param pstrName the name of this property.
		 * @param pstrValue the value of this property.
		 * @param pstrComments the comments associated with this property.
		 */
		public MStudioParserIniProperty(String pstrName, String pstrValue, String pstrComments) {
			this.mstrName = pstrName;
			this.mstrValue = pstrValue;
			this.mstrComments = delRemChars(pstrComments);
		}

		/**
		 * Returns the string identifier (key part) of this property.
		 * @return the string identifier of this property.
		 */
		/*
		public String getPropName() {
			return this.mstrName;
		}
		*/
		/**
		 * Returns value of this property. If value contains a reference to
		 * environment avriable then this reference is replaced by actual value
		 * before the value is returned.
		 * @return the value of this property.
		 */
		public String getPropValue() {
			int intStart = 0;
			int intEnd = 0;
			String strVal = null;
			String strVar = null;
			String strRet = null;

			strRet = this.mstrValue;
			intStart = strRet.indexOf("%");

			if (intStart >= 0) {
				intEnd = strRet.indexOf("%", intStart + 1);
				strVar = strRet.substring(intStart + 1, intEnd);
				strVal = mpropEnv.getProperty(strVar);
				if (strVal != null) {
					strRet = strRet.substring(0, intStart) + strVal +
						strRet.substring(intEnd + 1);
				}
			}

			return strRet;
		}

		/**
		 * Returns comments associated with this property.
		 * @return the associated comments if any.
		 */
		/*
		public String getPropComments() {
			return this.mstrComments;
		}
		*/
		/**
		 * Sets the string identifier (key part) of a property
		 * @param pstrName the string identifier of a property
		 */
		/*
		public void setPropName(String pstrName) {
			this.mstrName = pstrName;
		}
		*/
		/**
		 * Sets the property value
		 * @param pstrValue the value for the property
		 */
		/*
		public void setPropValue(String pstrValue) {
			this.mstrValue = pstrValue;
		}
		*/
		/**
		 * Sets the comments for a property
		 * @param pstrComments the comments
		 */
		/*
		public void setPropComments(String pstrComments) {
			this.mstrComments = delRemChars(pstrComments);
		}
		*/
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			String strRet = "";

			if (this.mstrComments != null)
				strRet = addRemChars(mstrComments);
			strRet = strRet + this.mstrName + "=" + this.mstrValue;

			return strRet;
		}
	}
}

