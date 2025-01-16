/******************************************************************
*
* IBM Tivoli Event Generator
*
* IBM Confidential
* OCO Source Materials
*
* 5724-S45
*
* (C) Copyright IBM Corp. 2005, 2009
*
* The source code for this program is not published or otherwise
* divested of its trade secrets, irrespective of what has
* been deposited with the U.S. Copyright Office.
*
******************************************************************/
package generator;

import log.LogManager;
import log.LogHandle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Date;

import utils.SingleEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pimmy
 * Date: Apr 18, 2005
 * Time: 10:22:35 AM
 * To change this template use Options | File Templates.
 */
public class OSConnection
{
    private final int INT_NORMAL = 0;
    private final int INT_INCR = 1;
    private final int INT_RAND = 2;
    private final int INT_TIME = 3;
    private final int CHAR_NORMAL = 4;
    private final int CHAR_INCR = 5;
    private final int CHAR_RAND = 6;
    private final int CHAR_TIME = 7;
    private final int GIS = 8;

    // Connection URL
    private String fURL;
    // Connection to ObjectServer.
    private Connection fConnection;
    // ObjectServer connection properties.
    private volatile Properties fProperties;
    
    private long _timestamp;
    
    private String _name;
    private String _host;
    private String _port;
    private String _user;
    private String _pass;
    private boolean _secure;

    private String[] _eventColumnNames;
    private String[] _eventColumnValue;
    private int _type;
    private int aI;
    
    private StringBuffer _aQuery;
    private Statement _aStatement;
    
    public OSConnection(String name, String host, String port, String user, String pass, boolean secure)
    {
        _name = name;
        _host = host.trim();
        _port = port.trim();
        _user = user;
        _pass = pass.trim();
        _secure = secure;

        fProperties = new Properties();

        fProperties.put("HOSTNAME", _name);
        fProperties.put("objectserverhost", _host);
        fProperties.put("objectserverport", _port);
        fProperties.put("user", _user);
        fProperties.put("password", _pass);

        fProperties.put("APPLICATIONNAME", "EventGenerator");

        if (_secure)
        {
            /* object server may be running in secure mode
            override APPLICATIONNAME Property to be able to use JJELD backdoor.
            The password needs to be encrypted */

            fProperties.put("APPLICATIONNAME", "JJELD");
        }
    }

    public boolean connect()
    {
        fURL = "jdbc:sybase:Tds:" + _host + ":" + _port;
        LogManager.write("OS Connection        - INFO:     Connecting to [" +_name+ "@" +_host+ ":" +_port+ "].", LogHandle.ALL,LogManager.INFO);

        try {
            // Load the driver
            Class.forName("com.sybase.jdbc.SybDriver");

            // Attempt to connect to the ObjectServer.
            fConnection = DriverManager.getConnection(fURL, fProperties);
        } catch (SQLException e) {
            // Oh dear!! Problem with connection properties.
            LogManager.write("OS Connection        - MAJOR:    Connection to the Object Server [" +_name+ "@" +_host+ ":" +_port+ "] failed.", LogHandle.ALL,LogManager.MAJOR);
            return false;
        } catch (Exception e) {
            LogManager.write("OS Connection        - MAJOR:    Unexpected exception while connecting to [" +_name+ "@" +_host+ ":" +_port+ "].", LogHandle.ALL,LogManager.MAJOR);
            return false;
        }

        // Successful connection.
        return true;
    }

    public boolean testConnection()
    {
        if (connect())
        {
            if (disconnect())
            {
                return true;
            }
        }
        return false;
    }

    public boolean sendEvent(SingleEvent event)
    {
        //LogManager.write("OS Connection        - Sending event ID [" +event.getID()+ "].", LogHandle.ALL,LogManager.DEBUG);

        // Get current date in milliseconds, then change it to seconds
        _timestamp = new Date().getTime() / 1000;
        //LogManager.write("OS Connection        - DEBUG:    Time now in seconds is [" +timestamp+ "].", LogHandle.ALL,LogManager.DEBUG);

        _eventColumnNames = event.getEventColumns();
        _eventColumnValue = event.getEventValues();

        _aQuery = new StringBuffer("INSERT INTO ");
        _aQuery.append(event.getTargetDb());
        _aQuery.append(" (");

        //LogManager.write("OS Connection        - Sending event [" +event.getID()+ "].", LogHandle.ALL,LogManager.DEBUG);
        //LogManager.write("OS Connection        ------------------------------------->", LogHandle.ALL,LogManager.DEBUG);
        //LogManager.write("OS Connection        - Column name -- Cell Value --------->", LogHandle.ALL,LogManager.DEBUG);
        for (aI = 0; aI < _eventColumnValue.length; aI++)
        {
            //LogManager.write("OS Connection        - " +eventColumnNames[aI]+ "\t" +eventColumnValue[aI], LogHandle.ALL,LogManager.DEBUG);
            if (aI == _eventColumnValue.length - 1)
            {
                _aQuery.append(_eventColumnNames[aI]);
                _aQuery.append(") VALUES (");
            }
            else
            {
                _aQuery.append(_eventColumnNames[aI]);
                _aQuery.append(", ");
            }
        }
        for (aI = 0; aI < _eventColumnValue.length; aI++)
        {
            _type = event.getValueType(_eventColumnNames[aI]);

            switch (_type)
            {
                case INT_NORMAL :
                    if (aI == _eventColumnValue.length - 1)
                    {
                        _aQuery.append(_eventColumnValue[aI]);
                        _aQuery.append(");");
                    }
                    else
                    {
                        _aQuery.append(_eventColumnValue[aI]);
                        _aQuery.append(", ");
                    }
                    break;

                case INT_INCR :
                    if (aI == _eventColumnValue.length - 1)
                    {
                        if (_eventColumnValue[aI].trim().equalsIgnoreCase(""))
                        {
                            _aQuery.append("0");
                            _aQuery.append(");");
                        }
                        else
                        {
                            _aQuery.append(_eventColumnValue[aI].substring(_eventColumnValue[aI].indexOf("Value:")+6));
                            _aQuery.append(");");
                        }
                    }
                    else
                    {
                        if (_eventColumnValue[aI].trim().equalsIgnoreCase(""))
                        {
                            _aQuery.append("0");
                            _aQuery.append(", ");
                        }
                        else
                        {
                            _aQuery.append(_eventColumnValue[aI].substring(_eventColumnValue[aI].indexOf("Value:")+6));
                            _aQuery.append(", ");
                        }
                    }
                    break;

                case INT_RAND :
                    if (aI == _eventColumnValue.length - 1)
                    {
                        if (_eventColumnValue[aI].trim().equalsIgnoreCase(""))
                        {
                            _aQuery.append("0");
                            _aQuery.append(");");
                        }
                        else
                        {
                            _aQuery.append(_eventColumnValue[aI].substring(_eventColumnValue[aI].indexOf("Value:")+6));
                            _aQuery.append(");");
                        }
                    }
                    else
                    {
                        if (_eventColumnValue[aI].trim().equalsIgnoreCase(""))
                        {
                            _aQuery.append("0");
                            _aQuery.append(", ");
                        }
                        else
                        {
                            _aQuery.append(_eventColumnValue[aI].substring(_eventColumnValue[aI].indexOf("Value:")+6));
                            _aQuery.append(", ");
                        }
                    }
                    break;

                case INT_TIME :
                    if (aI == _eventColumnValue.length - 1)
                    {
                        _aQuery.append(_timestamp);
                        _aQuery.append(");");
                    }
                    else
                    {
                        _aQuery.append(_timestamp);
                        _aQuery.append(", ");
                    }
                    break;

                case CHAR_NORMAL :
                    if (aI == _eventColumnValue.length - 1)
                    {
                        _aQuery.append("'");
                        _aQuery.append(_eventColumnValue[aI]);
                        _aQuery.append("');");
                    }
                    else
                    {
                        _aQuery.append("'");
                        _aQuery.append(_eventColumnValue[aI]);
                        _aQuery.append("', ");
                    }
                    break;

                case CHAR_INCR :
                    if (aI == _eventColumnValue.length - 1)
                    {
                        if (_eventColumnValue[aI].trim().equalsIgnoreCase(""))
                        {
                            _aQuery.append("' ');");
                        }
                        else
                        {
                            _aQuery.append("'");
                            _aQuery.append(_eventColumnValue[aI].substring(_eventColumnValue[aI].indexOf("Value:")+6));
                            _aQuery.append("');");
                        }
                    }
                    else
                    {
                        if (_eventColumnValue[aI].trim().equalsIgnoreCase(""))
                        {
                            _aQuery.append("' ', ");
                        }
                        else
                        {
                            _aQuery.append("'");
                            _aQuery.append(_eventColumnValue[aI].substring(_eventColumnValue[aI].indexOf("Value:")+6));
                            _aQuery.append("', ");
                        }
                    }
                    break;

                case CHAR_RAND :
                    if (aI == _eventColumnValue.length - 1)
                    {
                        if (_eventColumnValue[aI].trim().equalsIgnoreCase(""))
                        {
                            _aQuery.append("' ');");
                        }
                        else
                        {
                            _aQuery.append("'");
                            _aQuery.append(_eventColumnValue[aI].substring(_eventColumnValue[aI].indexOf("Value:")+6));
                            _aQuery.append("');");
                        }
                    }
                    else
                    {
                        if (_eventColumnValue[aI].trim().equalsIgnoreCase(""))
                        {
                            _aQuery.append("' ', ");
                        }
                        else
                        {
                            _aQuery.append("'");
                            _aQuery.append(_eventColumnValue[aI].substring(_eventColumnValue[aI].indexOf("Value:")+6));
                            _aQuery.append("', ");
                        }
                    }
                    break;

                case CHAR_TIME :
                    if (aI == _eventColumnValue.length - 1)
                    {
                        _aQuery.append("'");
                        _aQuery.append(_timestamp);
                        _aQuery.append("');");
                    }
                    else
                    {
                        _aQuery.append("'");
                        _aQuery.append(_timestamp);
                        _aQuery.append("', ");
                    }
                    break;

                case GIS :
                    if (aI == _eventColumnValue.length - 1)
                    {
                        if (_eventColumnValue[aI].trim().equalsIgnoreCase(""))
                        {
                            _aQuery.append("' ');");
                        }
                        else
                        {
                            _aQuery.append("'");
                            _aQuery.append(_eventColumnValue[aI].substring(_eventColumnValue[aI].indexOf("Value:")+6));
                            _aQuery.append("');");
                        }
                    }
                    else
                    {
                        if (_eventColumnValue[aI].trim().equalsIgnoreCase(""))
                        {
                            _aQuery.append("' ', ");
                        }
                        else
                        {
                            _aQuery.append("'");
                            _aQuery.append(_eventColumnValue[aI].substring(_eventColumnValue[aI].indexOf("Value:")+6));
                            _aQuery.append("', ");
                        }
                    }
                    break;

                default :
                    if (aI == _eventColumnValue.length - 1)
                    {
                        _aQuery.append("'");
                        _aQuery.append(_eventColumnValue[aI]);
                        _aQuery.append("');");
                    }
                    else
                    {
                        _aQuery.append("'");
                        _aQuery.append(_eventColumnValue[aI]);
                        _aQuery.append("', ");
                    }
                    break;
            }
        }

        LogManager.write("OS Connection        - INFO:     Executing statement: [" + _aQuery.toString()+ "].", LogHandle.ALL,LogManager.INFO);

        try
        {
            _aStatement = fConnection.createStatement();

            _aStatement.execute(_aQuery.toString());
            
            //And if it does not exit with exception we return true at the end of the function
        } catch (SQLException e) {
            LogManager.write("OS Connection        - MAJOR:    [" +e+ "].", LogHandle.ALL,LogManager.MAJOR);
            return false;
        } finally {
        	try {
        		_aStatement.close();
        	} catch (SQLException sqle) {
        		LogManager.write("OS Connection        - MAJOR:    [" +sqle+ "].", LogHandle.ALL,LogManager.MAJOR);
        	}
        }

        return true;
    }

    public boolean disconnect()
    {
        LogManager.write("OS Connection        - INFO:     Closing connection to [" +_name+ "@" +_host+ ":" +_port+ "].", LogHandle.ALL,LogManager.INFO);

        try
        {
            fConnection.close();
        }
        catch (SQLException e)
        {
            LogManager.write("OS Connection        - MINOR:    While disconnecting from the Object Server.", LogHandle.ALL,LogManager.MINOR);
            return false;
        }
// Connection closed OK
        return true;
    }
}
