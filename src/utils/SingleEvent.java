/******************************************************************
*
* IBM Tivoli Event Generator
*
* IBM Confidential
* OCO Source Materials
*
* 5724-S45
*
* (C) Copyright IBM Corp. 2005
*
* The source code for this program is not published or otherwise
* divested of its trade secrets, irrespective of what has
* been deposited with the U.S. Copyright Office.
*
******************************************************************/
package utils;

import java.util.Properties;
import java.util.Enumeration;

import log.LogHandle;
import log.LogManager;

/**
 * Created by IntelliJ IDEA.
 * User: pimmy
 * Date: Mar 15, 2005
 * Time: 2:34:45 PM
 * To change this template use Options | File Templates.
 */
public class SingleEvent
{
    // Event ID number
    private int _ID;
    // Target database.table
    private String _targetDb;
    // Is the event activated
    private boolean _active;
    // Frequence of the sending of the event
    private int _poll;
    // Maximum number events to be sent
    private int _numEvents;

    // Dynamic array containing event data pairs
    private Properties _eventPairs;
    // Event value types -> [Col_name]:[val_type], val_type -> INT_NORMAL, INT_INCR, INT_RAND, INT_TIME, CHAR_NORMAL,...
    private Properties _valTypes;
    // Column types -> [Col_name]:[col_type], col_type -> 0/1 = int/char
    private Properties _colTypes;
    
    public SingleEvent()
    {
    	_targetDb = "";
        _eventPairs = new Properties();
        _valTypes = new Properties();
        _colTypes = new Properties();

        _ID = 0;
        _numEvents = 0;
    }

    public SingleEvent(int ID, int numFields)
    {
    	_targetDb = "";
    	_eventPairs = new Properties();
        _valTypes = new Properties();
        _colTypes = new Properties();

        _ID = ID;
        _numEvents = numFields;
    }

    public int getID()
    {
        return _ID;
    }

    public void setTargetDb(String targetDb)
    {
    	_targetDb = targetDb;
    }

    public String getTargetDb()
    {
        return _targetDb;
    }

    public void setID(int ID)
    {
        _poll = ID;
    }

    public boolean isActive()
    {
        return _active;
    }

    public void setActive(boolean active)
    {
        _active = active;
    }

    public int getPoll()
    {
        return _poll;
    }

    public void setPoll(int poll)
    {
        _poll = poll;
    }

    public int getEventsNumber()
    {
        return _numEvents;
    }

    public void setEventsNumber(int numEvents)
    {
        _numEvents = numEvents;
    }

    public String[] getEventColumns()
    {
        if (_eventPairs.isEmpty())
        {
            LogManager.write("Single Event         - CRITICAL: System error -> No columns available for this event.", LogHandle.ALL,LogManager.MAJOR);
            return null;
        }

        String[] columnNames = new String[_eventPairs.size()];
        int i = 0;

        Enumeration eventFields = _eventPairs.propertyNames();
        while (eventFields.hasMoreElements())
        {
            columnNames[i] = (String)eventFields.nextElement();
            i++;
        }
        
        return columnNames;
    }

    public String[] getEventValues() {
    	
        if (_eventPairs.isEmpty())
        {
            LogManager.write("Single Event         - CRITICAL: System error -> No values available for this event.", LogHandle.ALL,LogManager.MAJOR);
            return null;
        }

        String[] columnValue = new String[_eventPairs.size()];
        int i = 0;

        Enumeration eventFields = _eventPairs.propertyNames();
        while (eventFields.hasMoreElements())
        {
            String name=(String)eventFields.nextElement();
            columnValue[i] = _eventPairs.getProperty(name);
            i++;
        }

        return columnValue;
    }

	/*
	* @desc		Sets the value for the particular field/column of this event
	*
	* @param	String	field	Name of the column
	* @param	String	value	Value of this field
    */
    public void setEventValue(String field, String value) {
        _eventPairs.setProperty(field, value);
    }
    
	/*
	* @desc		Returns the value types for all fields by column name
	* 			Value types are:	<li>
	* 									<ul>0 -> Static Integer</ul>
	* 									<ul>1 -> Incremented Integer</ul>
	* 									<ul>2 -> Random generated Integer</ul>
	* 									<ul>3 -> Current time as integer</ul>
	* 									<ul>4 -> Static String</ul>
	* 									<ul>5 -> Incremented Integer with prefix or suffix as String</ul>
	* 									<ul>6 -> Random generated Integer with prefix or suffix as String</ul>
	* 									<ul>7 -> Current time as String</ul>
	* 									<ul>8 -> GIS coordinates as String</ul>
	* 								</li>
	*
	* @return	Properties			Value types by column name
    */
    public Properties getEventValueTypes() {
        return _valTypes;
    }

	/*
	* @desc		Returns the value type by given column column name
	* 
	* @param	String	key		Name of the column
	* 
	* @return	Integer			Value types by column name:
	* 								<li>
	* 									<ul>0 -> Static Integer</ul>
	* 									<ul>1 -> Incremented Integer</ul>
	* 									<ul>2 -> Random generated Integer</ul>
	* 									<ul>3 -> Current time as integer</ul>
	* 									<ul>4 -> Static String</ul>
	* 									<ul>5 -> Incremented Integer with prefix or suffix as String</ul>
	* 									<ul>6 -> Random generated Integer with prefix or suffix as String</ul>
	* 									<ul>7 -> Current time as String</ul>
	* 									<ul>8 -> GIS coordinates as String</ul>
	* 								</li>
    */
    public int getValueType(String key) {
    	
    	int type = 4; //Value type is Static String by default

        if (_valTypes.isEmpty()) {
            LogManager.write("Single Event         - CRITICAL: System error -> Value properties are empty.",LogHandle.ALL,LogManager.MAJOR);
            return type;
        }
        
        String tmp_type = _valTypes.getProperty(key);
        
        try {
            type = Integer.valueOf(tmp_type).intValue();
            LogManager.write("Single Event         - DEBUG:    Value for column [" +key+ "] is of type [" + type + "].",LogHandle.ALL,LogManager.DEBUG);	
        } catch (Exception e) {
            LogManager.write("Single Event         - MAJOR:    Field for column [" +key+ "] has invalid value type [" +tmp_type+ "]. Setting to 4 -> Static String.", LogHandle.ALL,LogManager.MAJOR);
        }

        return type;
    }
   
	/*
	* @desc		Sets type of the value for particular field by its column name
	*
	* @param	String	colName		Name of the column
	* @param	String	type		Type of the value:
	* 								<li>
	* 									<ul>0 -> Static Integer</ul>
	* 									<ul>1 -> Incremented Integer</ul>
	* 									<ul>2 -> Random generated Integer</ul>
	* 									<ul>3 -> Current time as integer</ul>
	* 									<ul>4 -> Static String</ul>
	* 									<ul>5 -> Incremented Integer with prefix or suffix as String</ul>
	* 									<ul>6 -> Random generated Integer with prefix or suffix as String</ul>
	* 									<ul>7 -> Current time as String</ul>
	* 									<ul>8 -> GIS coordinates as String</ul>
	* 								</li>
	*/
    public void setEventValueType(String colName, String type) {
    	_valTypes.setProperty(colName, type);
    }

	/*
	* @desc		Returns the type of the column by it's name
	* 
	* @param	String	key		Name of the column
	*
	* @return	Integer			Column type: 0->Integer, 1->String
    */
    public int getColumnType(String key) {
    	
        int type = 1; //Column type is String by default
        
        if (_colTypes.isEmpty()) {
            LogManager.write("Single Event         - CRITICAL: System error -> Column properties are empty.",LogHandle.ALL,LogManager.MAJOR);
            return type;
        }
        
        try {
        	type = Integer.valueOf(_colTypes.getProperty(key)).intValue();
        	String typeStr = (type != 0) ? "Char" : "Int";
            LogManager.write("Single Event         - DEBUG:    Column [" +key+ "] is of type [" + typeStr + "].",LogHandle.ALL,LogManager.DEBUG);	
        } catch(NumberFormatException nfe) {
            LogManager.write("Single Event         - MAJOR:    Column [" +key+ "] has invalid type defined. Please check the column type in the table XML file.",LogHandle.ALL,LogManager.MAJOR);
		}

        return type;
    }
    
	/*
	* @desc		Sets type of a column by it's name
	*
	* @param	String	colName		Name of the column
	* @param	String	type		Type of the value:
	* 								<li>
	* 									<ul>0 -> Integer</ul>
	* 									<ul>1 -> String</ul>
	* 								</li>
	*/
    public void setColumnType(String colName, String type) {
    	_colTypes.setProperty(colName, type);
    }

	/*
	* @desc		Stores the types of all column types
	*
	* @param	Properties	types	Column_name:Column_type pairs
	*/
    public void setColumnsTypes(Properties types) {
        _colTypes = types;
    }
}
