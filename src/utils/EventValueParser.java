/******************************************************************
*
* IBM Tivoli Event Generator
*
* IBM Confidential
* OCO Source Materials
*
* 5724-S45
*
* (C) Copyright IBM Corp. 2008
*
* The source code for this program is not published or otherwise
* divested of its trade secrets, irrespective of what has
* been deposited with the U.S. Copyright Office.
*
******************************************************************/
package utils;

/* 
** @desc		Determines the event value bounds, by the given cell value
**
** @package		Utils
** @version		1.0.1, 24.10.2008
** @author		Kliment Stefanov <stefanov@uk.ibm.com>
*/

import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import log.LogHandle;
import log.LogManager;

public class EventValueParser {

    private final int INT_NORMAL = 0;
    private final int INT_INCR = 1;
    private final int INT_RAND = 2;
    private final int INT_TIME = 3;
    private final int CHAR_NORMAL = 4;
    private final int CHAR_INCR = 5;
    private final int CHAR_RAND = 6;
    private final int CHAR_TIME = 7;
    private final int GIS = 8;

    private	Pattern pattern;
	private Matcher matcher;
	private boolean type;
	
    private String _cellValue = null;
	private String eventValue = null;
	private int valFrom = 0;
	private int valTo = 0;
	private int valStart = 0;
	private int valStep = 0;
	private int valMinLat = 0;
	private int valMaxLat = 0;
	private int valMinLon = 0;
	private int valMaxLon = 0;
	private String valPref = "";
	private String valSuff = "";
	
	private boolean simpleValue_1 = true;
	private boolean simpleValue_2 = true;
	private boolean simpleValue_3 = true;
	private boolean simpleValue_4 = true;

	public EventValueParser(String cellValue) {
		
		String cValue;
		_cellValue = cellValue;
		
		// First step is to strip the value part if there is any
		if (_cellValue.indexOf("Value:") > 0) {
			eventValue = _cellValue.substring(_cellValue.indexOf("Value:"));
			cValue = _cellValue.substring(0, _cellValue.indexOf("Value:") - 2);
		} else {
			cValue = _cellValue;
		}
		
		// Then break the cell value into tokens if any
		StringTokenizer cellTokens = new StringTokenizer(cValue, ",");
		
		while (cellTokens.hasMoreTokens()) {
			
			String tokenValue = cellTokens.nextToken().trim();
			
	        LogManager.write("Event Value Parser   - DEBUG:        - Token: [" +tokenValue+ "].",LogHandle.ALL,LogManager.DEBUG);
	        
	        if ( tokenValue.toLowerCase().startsWith("from:")) {
	        	try {
	        		valFrom = Integer.valueOf(tokenValue.substring(5)).intValue();
	        		simpleValue_1 = false;
	        	} catch(NumberFormatException nfe) {
	        		LogManager.write("Event Value Parser   - MINOR:    [" +tokenValue+ "] cannot be converted to type Integer. Setting to 0.",LogHandle.ALL,LogManager.MINOR);
	        	}
			} else if ( tokenValue.toLowerCase().startsWith("to:")) {
				try {
					valTo = Integer.valueOf(tokenValue.substring(3)).intValue();
					simpleValue_2 = false;
				} catch(NumberFormatException nfe) {
		            LogManager.write("Event Value Parser   - MINOR:    [" +tokenValue+ "] cannot be converted to type Integer. Setting to 0.",LogHandle.ALL,LogManager.MINOR);
		    	}
			} else if ( tokenValue.toLowerCase().startsWith("start:")) {
				try {
					valStart = Integer.valueOf(tokenValue.substring(6)).intValue();
					simpleValue_1 = false;
				} catch(NumberFormatException nfe) {
		            LogManager.write("Event Value Parser   - MINOR:    [" +tokenValue+ "] cannot be converted to type Integer. Setting to 0.",LogHandle.ALL,LogManager.MINOR);
		    	}
			} else if ( tokenValue.toLowerCase().startsWith("step:")) {
				try {
					valStep = Integer.valueOf(tokenValue.substring(5)).intValue();
					simpleValue_2 = false;
				} catch(NumberFormatException nfe) {
		            LogManager.write("Event Value Parser   - MINOR:    [" +tokenValue+ "] cannot be converted to type Integer. Setting to 0.",LogHandle.ALL,LogManager.MINOR);
		    	}
			} else if ( tokenValue.toLowerCase().startsWith("minlat:")) {
				try {
					valMinLat = Integer.valueOf(tokenValue.substring(7)).intValue();
					simpleValue_1 = false;
				} catch(NumberFormatException nfe) {
		            LogManager.write("Event Value Parser   - MINOR:    [" +tokenValue+ "] cannot be converted to type Integer. Setting to 0.",LogHandle.ALL,LogManager.MINOR);
				}
			} else if ( tokenValue.toLowerCase().startsWith("maxlat:")) {
				try {
					valMaxLat = Integer.valueOf(tokenValue.substring(7)).intValue();
					simpleValue_2 = false;
				} catch(NumberFormatException nfe) {
		            LogManager.write("Event Value Parser   - MINOR:    [" +tokenValue+ "] cannot be converted to type Integer. Setting to 0.",LogHandle.ALL,LogManager.MINOR);
		    	}
			} else if ( tokenValue.toLowerCase().startsWith("minlong:")) {
				try {
					valMinLon = Integer.valueOf(tokenValue.substring(8)).intValue();
					simpleValue_3 = false;
				} catch(NumberFormatException nfe) {
		            LogManager.write("Event Value Parser   - MINOR:    [" +tokenValue+ "] cannot be converted to type Integer. Setting to 0.",LogHandle.ALL,LogManager.MINOR);
		    	}
			} else if ( tokenValue.toLowerCase().startsWith("maxlong:")) {
				try {
					valMaxLon = Integer.valueOf(tokenValue.substring(8)).intValue();
					simpleValue_4 = false;
				} catch(NumberFormatException nfe) {
		            LogManager.write("Event Value Parser   - MINOR:    [" +tokenValue+ "] cannot be converted to type Integer. Setting to 0.",LogHandle.ALL,LogManager.MINOR);
		    	}
			} else if ( tokenValue.toLowerCase().startsWith("pref:")) {
					valPref = tokenValue.substring(5);
			} else if ( tokenValue.toLowerCase().startsWith("suff:")) {
					valSuff = tokenValue.substring(5);
			}
		}
	}
	
	/*
	* @desc		Determines the type of the value for the correct data generation
	*
	* @param	int		colType		Column type - [int/char : 0/1]
	* 
	* @return	int					Type of the value:
	* 								<li>
	* 									<ul>INT_NORM = 0</ul>
	* 									<ul>INT_INCR = 1</ul>
	* 									<ul>INT_RAND = 2</ul>
	* 									<ul>INT_TIME = 3</ul>
	* 									<ul>CHAR_NORM = 4</ul>
	* 									<ul>CHAR_INCR = 5</ul>
	* 									<ul>CHAR_RAND = 6</ul>
	* 									<ul>CHAR_TIME = 7</ul>
	* 									<ul>GIS = 8</ul>
	* 								</li>
	*/
    public int getDataType(int colType) {
    	
    	// Convert from INT to BOOLEAN
    	type = (colType != 0);
    	
    	if (isIncremental(_cellValue)) {
    		return type ? CHAR_INCR : INT_INCR;
    	}
    	
    	if (isRandom(_cellValue)) {
            return type ? CHAR_RAND : INT_RAND;
    	}
    	
    	if (isTime(_cellValue)) {
    		return type ? CHAR_TIME : INT_TIME;
    	}
    	
    	if (isGIS(_cellValue)) {
    		return GIS;
    	}
    	
    	if (!type) {
    		
    		try {
    			if ( Integer.valueOf(_cellValue).intValue() >= 0 ) {
    				return INT_NORMAL;
    			}
    		} catch(NumberFormatException nfe) {
                LogManager.write("Event Value Parser   - MAJOR:    Value [" +_cellValue+ "] cannot be converted to type Integer. Setting to Char.",LogHandle.ALL,LogManager.MAJOR);
    		}
    	}
    	
    	return CHAR_NORMAL;
    }
    
	/*
	* @desc		Check if the value matches the patterns:
	* 							<li><ul>[Start:1000, Step:10]</ul>
	* 							<ul>[Start:1000, Step:10, Pref:prefix]</ul>
	* 							<ul>[Start:1000, Step:10, Suff:suffix]</ul>
	* 							<ul>[Start:1000, Step:10, Pref:prefix, Suff:suffix]</ul></li>
	*
	* @param	String	value	Value from the event table cell
	* 
	* @return	boolean			TRUE if matches, FALSE otherwise
    */
    public boolean isIncremental(String value) {
    	
    	pattern = Pattern.compile("^ *Start:[0-9]+ *, *Step:[0-9]+ *(, *Value:.*)?$");
    	matcher = pattern.matcher(value);
    	if (matcher.matches()) {
    		return true;
    	}
    	
    	pattern = Pattern.compile("^ *Start:[0-9]+ *, *Step:[0-9]+ *, *Pref:[A-Za-z0-9_-]+ *(, *Value:.*)?$");
    	matcher = pattern.matcher(value);
    	if (matcher.matches()) {
    		return true;
    	}
    	
    	pattern = Pattern.compile("^ *Start:[0-9]+ *, *Step:[0-9]+ *, *Suff:[A-Za-z0-9_-]+ *(, *Value:.*)?$");
    	matcher = pattern.matcher(value);
    	if (matcher.matches()) {
    		return true;
    	}
    	
    	pattern = Pattern.compile("^ *Start:[0-9]+ *, *Step:[0-9]+ *, *Pref:[A-Za-z0-9_-]+ *, *Suff:[A-Za-z0-9_-]+ *(, *Value:.*)?$");
    	matcher = pattern.matcher(value);
    	if (matcher.matches()) {
    		return true;
    	}
    	
    	return false;
    }

	/*
	* @desc		Check if the value matches the patterns:
	* 							<li><ul>[From:10, To:1000]</ul>
	* 							<ul>[From:10, To:1000, Pref:prefix]</ul>
	* 							<ul>[From:10, To:1000, Suff:suffix]</ul>
	* 							<ul>[From:10, To:1000, Pref:prefix, Suff:suffix]</ul></li>
	*
	* @param	String	value	Value from the event table cell
	* 
	* @return	boolean			TRUE if matches, FALSE otherwise
    */
    public boolean isRandom(String value) {
    	    	
    	pattern = Pattern.compile("^ *From:[0-9]+ *, *To:[0-9]+ *(, *Value:.*)?$");
    	matcher = pattern.matcher(value);
    	if (matcher.matches()) {
    		return true;
    	}
    	
    	pattern = Pattern.compile("^ *From:[0-9]+ *, *To:[0-9]+ *, *Pref:[A-Za-z0-9_-]+ *(, *Value:.*)?$");
    	matcher = pattern.matcher(value);
    	if (matcher.matches()) {
    		return true;
    	}
    	
    	pattern = Pattern.compile("^ *From:[0-9]+ *, *To:[0-9]+ *, *Suff:[A-Za-z0-9_-]+ *(, *Value:.*)?$");
    	matcher = pattern.matcher(value);
    	if (matcher.matches()) {
    		return true;
    	}
    	
    	pattern = Pattern.compile("^ *From:[0-9]+ *, *To:[0-9]+ *, *Pref:[A-Za-z0-9_-]+ *, *Suff:[A-Za-z0-9_-]+ *(, *Value:.*)?$");
    	matcher = pattern.matcher(value);
    	if (matcher.matches()) {
    		return true;
    	}
    	
    	return false;
    }

	/*
	* @desc		Check if the value matches the String: [Current time.]
	*
	* @param	String	value	Value from the event table cell
	* 
	* @return	boolean			TRUE if matches, FALSE otherwise
    */
    public boolean isTime(String value) {
    	
    	if (value.equalsIgnoreCase("Current time.")) {
    		return true;
    	} else {
    		return false;
    	}
    }

	/*
	* @desc		Check if the value matches the pattern: [minLat:43.00, maxLat:45.00, minLong:5, maxLong:7]
	*
	* @param	String	value	Value from the event table cell
	* 
	* @return	boolean			TRUE if matches, FALSE otherwise
    */
    public boolean isGIS(String value) {
    	
    	pattern = Pattern.compile("^ *minLat:[0-9]+.?[0-9]+ *, *maxLat:[0-9]+.?[0-9]+ *, *minLong:[0-9]+.?[0-9]+ *, *maxLong:[0-9]+.?[0-9]+ *(, *Value:.*)?$");
    	matcher = pattern.matcher(value);
    	if (matcher.matches()) {
    		return true;
    	}
    	
    	return false;
    }

	public boolean isSimple () {
		if (simpleValue_1 && simpleValue_2 && simpleValue_3 & simpleValue_4) {
			return true;
		} else {
			return false;
		}
	}
	
	public int getValueFrom () {
		return valFrom;
	}
	
	public int getValueTo () {
		return valTo;
	}
	
	public int getValueStart () {
		return valStart;
	}
	
	public int getValueStep () {
		return valStep;
	}
	
	public int getValueMinLat () {
		return valMinLat;
	}
	
	public int getValueMaxLat () {
		return valMaxLat;
	}
	
	public int getValueMinLong () {
		return valMinLon;
	}
	
	public int getValueMaxLong () {
		return valMaxLon;
	}
	
	public String getValuePref () {
		return valPref;
	}
	
	public String getValueSuff () {
		return valSuff;
	}
	
	public String getValue() {
		return eventValue;
	}
}