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

import log.LogManager;
import log.LogHandle;

import java.util.Properties;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: pimmy
 * Date: Feb 11, 2005
 * Time: 10:24:58 AM
 * To change this template use Options | File Templates.
 */
public class GENProps
{
    private String _genhome;
    //Object Server properties
    private String _osName;
    private String _osHost;
    private String _osPort;
    private String _osUser;
    private String _osPass;
    private boolean _osSecure;

    // Last loaded Event Table
    private String _eventTableName;

    // Main thread priority
    private String _priority;

    public GENProps()
    {
        // Get the property (EGEN_HOME env var must be set)
        _genhome = System.getProperty("EGEN_HOME");
        // Several checks to determine that the Program starts from the correct location
        File home = new File(_genhome);
        if (_genhome == null)
        {
            System.out.println("Property Manager     - Home Directory not set. Exit.\n\n");
            System.exit(-1);
        }
        if (!home.exists())
        {
            System.out.println("Property Manager     - Home Directory ["+_genhome+"] does not exist. Exit.\n\n");
            System.exit(-1);
        }

        setDefaultProps();
    }

    public GENProps(String name, String host, String port, String user, String pass, boolean secure, String table)
    {
        // Get the property (GEN_HOME env var must be set)
        _genhome = System.getProperty("EGEN_HOME");
        // Several checks to determine that the Program starts from the correct location
        File home = new File(_genhome);
        if (_genhome == null)
        {
            System.out.println("Property Manager     - Home Directory not set. Exit.\n\n");
            System.exit(-1);
        }
        if (!home.exists())
        {
            System.out.println("Property Manager     - Home Directory ["+_genhome+"] does not exist. Exit.\n\n");
            System.exit(-1);
        }

        _osName = name;
        _osHost = host;
        _osPort = port;
        _osUser = user;
        _osPass = pass;
        _osSecure = secure;

        _eventTableName = table;
    }

    // Load default Object Server properties
    private void setDefaultProps()
    {
        Properties genProps = new Properties();

        try
        {
            FileInputStream fStream = new FileInputStream(_genhome + "/props/generator.prp");
            genProps.load(fStream);
            fStream.close();
        }
        catch (Exception exptn)
        {
            LogManager.write("Property Manager     - CRITICAL: Cannot load main property file: Exit..", LogHandle.ALL,LogManager.CRITICAL);
            System.exit(1);
        }

        setOSName(genProps.getProperty("OS_Name", "NCOMS"));
        setOSHost(genProps.getProperty("OS_Host", "localhost"));
        setOSPort(genProps.getProperty("OS_Port", "4100"));
        setOSUser(genProps.getProperty("OS_User", "root"));
        setOSPass(genProps.getProperty("OS_Pass", ""));
        setOSSecure(Boolean.valueOf(genProps.getProperty("OS_Secure", "false").toString()).booleanValue());

        setEventTable(genProps.getProperty("Table", "template"));

        setThreadPriority(genProps.getProperty("ThreadPriority", "MAX"));

        //printProps();
    }

    // Load Event Generator Properties
    public void loadProps()
    {
        LogManager.write("Property Manager     - DEBUG:    Loading Event Generator Properties.", LogHandle.ALL,LogManager.DEBUG);

        Properties genProps = new Properties();
        try
        {
            FileInputStream fStream = new FileInputStream(_genhome + "/props/usersets.prp");
            genProps.load(fStream);
            fStream.close();

            setOSName(genProps.getProperty("OS_Name"));
            setOSHost(genProps.getProperty("OS_Host"));
            setOSPort(genProps.getProperty("OS_Port"));
            setOSUser(genProps.getProperty("OS_User"));
            setOSPass(genProps.getProperty("OS_Pass"));
            setOSSecure(Boolean.valueOf(genProps.getProperty("OS_Secure").toString()).booleanValue());

            setEventTable(genProps.getProperty("Table"));

            setThreadPriority(genProps.getProperty("ThreadPriority"));

            //printProps();
        }
        catch (IOException exptn)
        {
            LogManager.write("Property Manager     - MINOR:    Cannot load properties from file. Setting default properties.", LogHandle.ALL,LogManager.MAJOR);
            setDefaultProps();
        }
        catch (Exception excptn)
        {
            LogManager.write("Property Manager     - MINOR: Data format error in properties file. Setting default properties.", LogHandle.ALL,LogManager.MAJOR);
            setDefaultProps();
        }
    }

    public void saveProps()
    {
        LogManager.write("Property Manager     - DEBUG:    Saving Event Generator Properties.", LogHandle.ALL,LogManager.DEBUG);

        Properties genProps = new Properties();

        genProps.setProperty("OS_Name", getOSName());
        genProps.setProperty("OS_Host", getOSHost());
        genProps.setProperty("OS_Port", getOSPort());
        genProps.setProperty("OS_User", getOSUser());
        genProps.setProperty("OS_Pass", getOSPass());
        genProps.setProperty("OS_Secure", String.valueOf(isOSSecure()));

        genProps.setProperty("Table", getEventTable());

        genProps.setProperty("ThreadPriority", getThreadPriority());

        //printProps();
        try
        {
            FileOutputStream stream=new FileOutputStream(_genhome + "/props/usersets.prp");
            genProps.store(new PrintStream(stream), "Event Generator Properties. Warning - Do not edit this file!!!");
            stream.close();
            LogManager.write("Property Manager     - DEBUG:    Event Generator properties saved.", LogHandle.ALL,LogManager.DEBUG);
        }
        catch (IOException e)
        {
            LogManager.write("Property Manager     - MAJOR:    Saving Event Generator properties failed.", LogHandle.ALL,LogManager.MAJOR);
        }
    }

    // Set the Object Server Name
    public void setOSName(String name)
    {
        _osName = name;
    }

    // Get the Object Server Name
    public String getOSName()
    {
        return _osName;
    }

    // Set the Object Server Host
    public void setOSHost(String host)
    {
        _osHost = host;
    }

    // Get the Object Server Host
    public String getOSHost()
    {
        return _osHost;
    }

    // Set the Object Server Port
    public void setOSPort(String port)
    {
        _osPort = port;
    }

    // Get the Object Server Port
    public String getOSPort()
    {
        return _osPort;
    }

    // Set the Object Server Username
    public void setOSUser(String user)
    {
        _osUser = user;
    }

    // Get the Object Server Username
    public String getOSUser()
    {
        return _osUser;
    }

    // Set the Object Server Password
    public void setOSPass(String pass)
    {
        _osPass = pass;
    }

    // Get the Object Server Password
    public String getOSPass()
    {
        return _osPass;
    }

    // Set the Object Server secure connection
    public void setOSSecure(boolean secure)
    {
        _osSecure = secure;
    }

    // Check if the Object Server connection is secure
    public boolean isOSSecure()
    {
        return _osSecure;
    }

    // Set the last selected Event Table
    public void setEventTable(String tableName)
    {
        _eventTableName = tableName;
    }

    // Get the last opened Event Table
    public String getEventTable()
    {
        return _eventTableName;
    }

    public void setThreadPriority(String priority)
    {
        _priority = priority;
    }

    public String getThreadPriority()
    {
        return _priority;
    }

    private void printProps()
    {
        LogManager.write("Property Manager     - DEBUG:    ********************************", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Property Manager     - DEBUG:    * Event Generator properties:", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Property Manager     - DEBUG:    ********************************", LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Property Manager     - DEBUG:    * OS Name: " +getOSName(), LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Property Manager     - DEBUG:    * OS Host: " +getOSHost(), LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Property Manager     - DEBUG:    * OS Port: " +getOSPort(), LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Property Manager     - DEBUG:    * OS User: " +getOSUser(), LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Property Manager     - DEBUG:    * OS Pass: " +getOSPass(), LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Property Manager     - DEBUG:    * OS Secu: " +isOSSecure(), LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Property Manager     - DEBUG:    * E-Table: " +getEventTable(), LogHandle.ALL,LogManager.DEBUG);
        LogManager.write("Property Manager     - DEBUG:    ********************************", LogHandle.ALL,LogManager.DEBUG);

    }
}
