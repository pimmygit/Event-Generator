/******************************************************************
*
* IBM Tivoli Event Generator
*
* IBM Confidential
* OCO Source Materials
*
* 5724-S45
*
* (C) Copyright IBM Corp. 2005, 2011
*
* The source code for this program is not published or otherwise
* divested of its trade secrets, irrespective of what has
* been deposited with the U.S. Copyright Office.
*
******************************************************************/
package gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import utils.*;
import log.*;
import generator.OSConnection;

/*
* This is the main class for the Event Generator GUI
*/

public class Configurator implements ActionListener
{
    public JFrame frame;
    
    private static final String APP_TITLE = "NETCOOL / Event Generator";
    private static final String APP_VERSION = "1.0.1.b3";
    
    // Link to the OMNIbus Wiki page for the Event Generator tool
    private static final String WIKI_PAGE = "http://www.ibm.com/developerworks/wikis/x/CQB5Bg";
    
    // Properties section
    private GENProps _vars;
    private Properties _props;
    protected static String log_file;

    private String _messagelevel = null;
    public static String _genhome = null;

    // Menus section
    private GENMenu fileMenu;
    private GENMenuItem exitMenuItem;
    private GENMenuItem saveMenuItem;

    protected static GENMenu actionMenu;
    protected static GENMenuItem clearStatMenuItem;
    protected static GENMenuItem startMenuItem;
    protected static GENMenuItem stopMenuItem;

    private GENMenu tableMenu;
    private GENMenu addMenuItem;
    private GENMenuItem addRowMenuItem;
    private GENMenuItem addColMenuItem;
    private GENMenu removeMenuItem;
    private GENMenuItem remRowMenuItem;
    private GENMenuItem remColMenuItem;
    private GENMenu tableMenuItem;
    private GENMenuItem saveTableMenuItem;
    private GENMenuItem clearTableMenuItem;

    private GENMenu helpMenu;
    private GENMenuItem manualMenuItem;
    private GENMenuItem wikiMenuItem;
    private GENMenuItem aboutMenuItem;

    // Buttons section
    protected static JButton saveButton;
    protected static JButton startButton;
    protected static JButton stopButton;
    protected static JButton clearButton;

    // Desktop panel
    private JDesktopPane desktop;
    // Tabbed Frames Panel
    private JTabbedPane tabbedPane;
    
    // Event Table Scroll Panel
    private JScrollPane eventPane;

    private JInternalFrame eventsFrame;
    protected JTable statTable;

    // Thread Control Table
    private JPanel threadControlPanel;
    public static JTable threadControlTable;
    private JScrollPane threadPane;
    
    // Total number of events sent
    protected long _numEventsSent;
    
    // Status panel
    protected static JTextField _numThreads;
    protected static JTextField _actThreads;
    protected static JTextField _totalSent;
    protected static JTextField _eventPerMin;
    protected JTextField _cpuUsage;
    protected JTextField _memUsage;
    protected JTextField _netUsage;
    protected static JTextField _buffer;

    // Status Bar
    protected static JTextField _totalInfo;
    protected static JTextField _minuteInfo;
    protected JTextField _netInfo;
    protected JTextField _cpuInfo;
    protected JTextField _memInfo;

    // Object Server properties
    private static JTextField _name;
    private static JTextField _host;
    private static JTextField _port;
    private static JTextField _user;
    private static JTextField _pass;
    private static boolean _secureOS;
    private static JCheckBox _secureConnection;
    private static JButton _testOSButton;

    // Table selection
    protected static JComboBox _tableName;
    protected static JButton _addNewTable;
    protected static JButton _deleteTable;

    // Pop-up configuration windows
    private newColumnFrame colFrame;
    private newEventFrame rowFrame;

    // Currently loaded Event Table
    protected EventTable eventList;
    // Currently loaded Threads Table
    protected ThreadTable threadList;

    // Background colour
    private final Color _bgColour = new Color(195,220,235);
    
    public Configurator() {}

    // Create the Main Frame for the Event Generator GUI
    public Configurator(String argument)
    {    	
        if (argument.equalsIgnoreCase("-v") || argument.matches("-version")) {
            System.out.println("\n**************************************************");
            System.out.println("*      NETCOOL / Event Generator   -   v1.0.1.b3 *");
            System.out.println("**************************************************\n\n");
            System.exit(0);
        }
        
        // Loading Event Generator variables
        _props = new Properties();
        
        try {
            // Several checks to determine that the program starts from the correct location
        	// First we try the System properties
        	//------------------------------------
        	
            // Get the property (EGEN_HOME environment variable must be set)
            _genhome = System.getProperty("EGEN_HOME");
            
            if (_genhome == null) {
            	
                System.out.println("\nConfigurator         - EGEN_HOME environment variable not set. Trying current directory..\n\n");
                
                // Second we try the current location
                //------------------------------------
                _genhome = ".";
                
            } else {
            	
                File home = new File(_genhome);
                
                if (!home.exists()) {
                    System.out.println("\nConfigurator         - Home Directory ["+_genhome+"] does not exist. Trying current one..\n\n");
                }
            }
            
            FileInputStream fStream = new FileInputStream(_genhome + "/props/generator.prp");
            _props.load(fStream);
            fStream.close();

        } catch(IOException e) {
            System.out.println("\nConfigurator         - Can't find the property file ["+_genhome+"/props/generator.prp\". ["+_genhome+"] is not a valid EGEN_HOME installation. Exit.\n\n");
            System.exit(-1);
        }
        
        //Setting logging level.
        _messagelevel = _props.getProperty("messagelevel");

        //Setting the default logging value
        if (_messagelevel == null)
        {
            _messagelevel = "MAJOR";
        }

        int message_level;
        if(_messagelevel.equals("debug") || _messagelevel.equals("DEBUG"))
        {
            message_level=LogManager.DEBUG;
        }
        else if(_messagelevel.equals("minor") || _messagelevel.equals("MINOR"))
        {
            message_level=LogManager.MINOR;
        }
        else if(_messagelevel.equals("major") || _messagelevel.equals("MAJOR"))
        {
            message_level=LogManager.MAJOR;
        }
        else if(_messagelevel.equals("critical") || _messagelevel.equals("CRITICAL"))
        {
            message_level=LogManager.CRITICAL;
        }
        else
        {
            message_level=LogManager.CRITICAL;
        }

        // Set the logfile
        log_file=_props.getProperty("messagelog");
        int sink_type;

        //If messagelog is not set into the generator._props,
        //set the default value to generator.log.
        if (log_file == null)
        {
            log_file = "generator.log";
        }

        if(log_file.equals("stdout") || log_file.equals("STDOUT"))
        {
            sink_type=LogManager.STDOUT;
        }
        else
        {
            sink_type=LogManager.FILE;
            try
            {
                log_file = _genhome + "/log/" + log_file;
                //System.out.println("Log File -> " +log_file);
                LogManager.setLog(LogHandle.ALL,log_file);
            }
            catch(Exception exit_log)
            {
                System.out.println("\nConfigurator         - ERROR: Failed to set the log file - ["+log_file+"]");
                System.exit(-1);
            }
        }
        LogManager.setSinkType(sink_type);
        LogManager.setLogLevel(message_level);

        LogManager.write("Starting Application - " + APP_TITLE + " - " + APP_VERSION + ".", LogHandle.ALL,LogManager.INFO);
        LogManager.write("Configurator         - DEBUG:    Home Directory set to: " +_genhome, LogHandle.ALL,LogManager.DEBUG);

        // Read all variables set at the previous session
        _vars = new GENProps();
        _vars.loadProps();

        // Initialise the Event Table
        eventList = new EventTable();
        // Initialise the Thread Table
        threadList = new ThreadTable();

        frame = new JFrame("  " + APP_TITLE);
        ImageIcon logo = new ImageIcon(_genhome + "/images/generator.gif");
        frame.setIconImage(logo.getImage());
        centerFrame(500,300,frame);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   //Set to exit on close

        desktop = new JDesktopPane();
        frame.getContentPane().add(desktop);

        LogManager.write("Configurator         - DEBUG:    Creating Menu bar.", LogHandle.ALL,LogManager.DEBUG);
        frame.setJMenuBar(GENMenuBar());
        LogManager.write("Configurator         - DEBUG:    Creating Tool bar.", LogHandle.ALL,LogManager.DEBUG);
        frame.getContentPane().add(createToolBar(), BorderLayout.NORTH);
        LogManager.write("Configurator         - DEBUG:    Creating Tabbed frames.", LogHandle.ALL,LogManager.DEBUG);
        frame.getContentPane().add(addTabbedFrames(), BorderLayout.CENTER);
        LogManager.write("Configurator         - DEBUG:    Creating Info bar.", LogHandle.ALL,LogManager.DEBUG);
        frame.getContentPane().add(addInfoBar(), BorderLayout.SOUTH);
        int width = 500;
        int height = 365;
        frame.setSize(width, height);
        frame.setVisible(true);

        stopMenuItem.setEnabled(false);
        stopButton.setEnabled(false);
        threadControlTable.setEnabled(false);
        tableMenu.setEnabled(false);
        
        WindowListener exitListener;

        exitListener = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Window window = e.getWindow();
                window.setVisible(false);
                window.dispose();
                System.exit(0);
            }
        };

        frame.addWindowListener(exitListener);

        LogManager.write("Configurator         - DEBUG:    GUI created.", LogHandle.ALL,LogManager.DEBUG);
    }

    // Create Event Generator MenuBar.
    private JMenuBar GENMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createEmptyBorder());

        // Create File menu & menu items;
        fileMenu = new GENMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        saveMenuItem = new GENMenuItem("  Save", new ImageIcon(_genhome + "/images/save.gif"));
        //exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.addActionListener(this);
        fileMenu.add(saveMenuItem);

        exitMenuItem = new GENMenuItem("  Exit", new ImageIcon(_genhome + "/images/exit.gif"));
        //exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
        exitMenuItem.setMnemonic(KeyEvent.VK_X);
        exitMenuItem.addActionListener(this);
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        // Create Action menu & menu items.
        actionMenu = new GENMenu("Action");
        actionMenu.setMnemonic(KeyEvent.VK_A);

        startMenuItem = new GENMenuItem(" Start", new ImageIcon(_genhome + "/images/start.gif"));
        startMenuItem.setMnemonic(KeyEvent.VK_A);
        startMenuItem.addActionListener(this);
        actionMenu.add(startMenuItem);

        stopMenuItem = new GENMenuItem(" Stop", new ImageIcon(_genhome + "/images/stop.gif"));
        stopMenuItem.setMnemonic(KeyEvent.VK_O);
        stopMenuItem.addActionListener(this);
        actionMenu.add(stopMenuItem);

        clearStatMenuItem = new GENMenuItem(" Clear", new ImageIcon(_genhome + "/images/stat_clear.gif"));
        clearStatMenuItem.setMnemonic(KeyEvent.VK_C);
        clearStatMenuItem.addActionListener(this);
        actionMenu.add(clearStatMenuItem);
        menuBar.add(actionMenu);

        // Create Table menu & menu items.
        tableMenu = new GENMenu("Table");
        tableMenu.setMnemonic(KeyEvent.VK_T);

        addMenuItem = new GENMenu(" Insert");
        addMenuItem.setMnemonic(KeyEvent.VK_I);
        addMenuItem.addActionListener(this);
        tableMenu.add(addMenuItem);
        addRowMenuItem = new GENMenuItem(" Row", new ImageIcon(_genhome + "/images/ins_row.gif"));
        addRowMenuItem.setMnemonic(KeyEvent.VK_R);
        addRowMenuItem.addActionListener(this);
        addMenuItem.add(addRowMenuItem);
        addColMenuItem = new GENMenuItem(" Column", new ImageIcon(_genhome + "/images/ins_col.gif"));
        addColMenuItem.setMnemonic(KeyEvent.VK_C);
        addColMenuItem.addActionListener(this);
        addMenuItem.add(addColMenuItem);

        removeMenuItem = new GENMenu(" Remove");
        removeMenuItem.setMnemonic(KeyEvent.VK_R);
        removeMenuItem.addActionListener(this);
        tableMenu.add(removeMenuItem);
        remRowMenuItem = new GENMenuItem(" Row", new ImageIcon(_genhome + "/images/rem_row.gif"));
        remRowMenuItem.setMnemonic(KeyEvent.VK_R);
        remRowMenuItem.addActionListener(this);
        removeMenuItem.add(remRowMenuItem);
        remColMenuItem = new GENMenuItem(" Column", new ImageIcon(_genhome + "/images/rem_col.gif"));
        remColMenuItem.setMnemonic(KeyEvent.VK_C);
        remColMenuItem.addActionListener(this);
        removeMenuItem.add(remColMenuItem);

        tableMenuItem = new GENMenu(" Table");
        tableMenuItem.setMnemonic(KeyEvent.VK_T);
        tableMenuItem.addActionListener(this);
        tableMenu.add(tableMenuItem);
        saveTableMenuItem = new GENMenuItem(" Save", new ImageIcon(_genhome + "/images/table_save.gif"));
        saveTableMenuItem.setMnemonic(KeyEvent.VK_S);
        saveTableMenuItem.addActionListener(this);
        tableMenuItem.add(saveTableMenuItem);
        clearTableMenuItem = new GENMenuItem(" Clear", new ImageIcon(_genhome + "/images/table_clear.gif"));
        clearTableMenuItem.setMnemonic(KeyEvent.VK_C);
        clearTableMenuItem.addActionListener(this);
        tableMenuItem.add(clearTableMenuItem);

        menuBar.add(tableMenu);

        // Create Help menu & menu items.
        helpMenu = new GENMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(helpMenu);

        manualMenuItem = new GENMenuItem(" Users Manual", new ImageIcon(_genhome + "/images/manual.gif"));
        manualMenuItem.setMnemonic(KeyEvent.VK_M);
        manualMenuItem.addActionListener(this);
        helpMenu.add(manualMenuItem);

        wikiMenuItem = new GENMenuItem(" Wiki Pages", new ImageIcon(_genhome + "/images/ac16_global-24.png"));
        wikiMenuItem.setMnemonic(KeyEvent.VK_A);
        wikiMenuItem.addActionListener(this);
        helpMenu.add(wikiMenuItem);

        aboutMenuItem = new GENMenuItem(" About...", new ImageIcon(_genhome + "/images/about.gif"));
        aboutMenuItem.setMnemonic(KeyEvent.VK_A);
        aboutMenuItem.addActionListener(this);
        helpMenu.add(aboutMenuItem);

        return menuBar;
    }

    // Create the Toolbar and all the buttons.
    private JToolBar createToolBar()
    {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createEtchedBorder());
        toolBar.add(Box.createRigidArea(new Dimension(5,0)));

        saveButton = new JButton(new ImageIcon(_genhome + "/images/save.gif"));
        saveButton.setToolTipText("Save Configuration.");
        saveButton.addActionListener(this);
        toolBar.add(saveButton);
        toolBar.add(Box.createRigidArea(new Dimension(10,0)));

        startButton = new JButton(new ImageIcon(_genhome + "/images/start.gif"));
        startButton.setToolTipText("Start Generator.");
        startButton.addActionListener(this);
        toolBar.add(startButton);
        toolBar.add(Box.createRigidArea(new Dimension(5,0)));

        stopButton = new JButton(new ImageIcon(_genhome + "/images/stop.gif"));
        stopButton.setEnabled(true);
        stopButton.setToolTipText("Stop Generator.");
        stopButton.addActionListener(this);
        toolBar.add(stopButton);
        toolBar.add(Box.createRigidArea(new Dimension(10,0)));

        clearButton = new JButton(new ImageIcon(_genhome + "/images/stat_clear.gif"));
        clearButton.setEnabled(true);
        clearButton.setToolTipText("Clear Statistic.");
        clearButton.addActionListener(this);
        toolBar.add(clearButton);
        toolBar.add(Box.createRigidArea(new Dimension(5,0)));

        return toolBar;
    }

    // Create the Tabbed panel.
    private JTabbedPane addTabbedFrames()
    {
        tabbedPane = new JTabbedPane();

        LogManager.write("Configurator         - DEBUG:    Creating \"Config\" frame.", LogHandle.ALL,LogManager.DEBUG);
        tabbedPane.addTab(" Config", new ImageIcon(_genhome + "/images/config.gif"), ConfigFrame());
        LogManager.write("Configurator         - DEBUG:    Creating \"Events\" frame.", LogHandle.ALL,LogManager.DEBUG);
        tabbedPane.addTab(" Events", new ImageIcon(_genhome + "/images/events.gif"), EventsFrame());
        LogManager.write("Configurator         - DEBUG:    Creating \"Status\" frame.", LogHandle.ALL,LogManager.DEBUG);
        tabbedPane.addTab(" Status", new ImageIcon(_genhome + "/images/status.gif"), StatusFrame());
        
        // Monitor the tab changing
        tabbedPane.addChangeListener(new ChangeListener() {
        	// This method is called whenever the tab changes
        	public void stateChanged(ChangeEvent evt) {
        		
        		JTabbedPane pane = (JTabbedPane)evt.getSource();

        		// Disable the Table menu item if the table tab is not active
        		if (pane.getSelectedIndex() == 1) {
        			tableMenu.setEnabled(true);
        		} else {
        			tableMenu.setEnabled(false);
        		}
        	}
        });

        
        return tabbedPane;
    }

    // Create Information Bar.
    private JPanel addInfoBar()
    {
        LogManager.write("Configurator         - DEBUG:    Creating \"Info\" bar.", LogHandle.ALL,LogManager.DEBUG);

        Dimension infoBoxSize = new Dimension(55, 17);
        //Dimension statusBoxSize = new Dimension(235, 17);

        // Create the main panel.
        JPanel infoBar = new JPanel();
        
        infoBar.setLayout(new BoxLayout(infoBar, 0));
        //infoBar.add(Box.createHorizontalGlue());

        // Create box with the number of the total events sent.
        infoBar.add(Box.createRigidArea(new Dimension(10, 20)));
        JLabel totalInfo = new JLabel("Total Sent: ");
        _totalInfo = new JTextField();
        _totalInfo.setBackground(new JButton().getBackground());
        _totalInfo.setMaximumSize(infoBoxSize);
        _totalInfo.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        _totalInfo.setMinimumSize(new Dimension(40, 17));
        _totalInfo.setPreferredSize(new Dimension(50, 17));
        _totalInfo.setMaximumSize(new Dimension(50, 17));
        _totalInfo.setEditable(false);
        infoBar.add(totalInfo);
        infoBar.add(_totalInfo);

        // Create box with the number of messages in the Sent directory.
        infoBar.add(Box.createRigidArea(new Dimension(10, 20)));
        JLabel minuteInfo = new JLabel("Events/Min: ");
        _minuteInfo = new JTextField();
        _minuteInfo.setBackground(new JButton().getBackground());
        _minuteInfo.setMaximumSize(infoBoxSize);
        _minuteInfo.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        _minuteInfo.setMinimumSize(new Dimension(30, 17));
        _minuteInfo.setPreferredSize(new Dimension(50, 17));
        _minuteInfo.setMaximumSize(new Dimension(50, 17));
        _minuteInfo.setEditable(false);
        infoBar.add(minuteInfo);
        infoBar.add(_minuteInfo);
/*
        // Create box with the number of messages in the Failed directory.
        infoBar.add(Box.createRigidArea(new Dimension(10, 20)));
        JLabel netInfo = new JLabel("LAN: ");
        _netInfo = new JTextField();
        _netInfo.setBackground(new JButton().getBackground());
        _netInfo.setMaximumSize(infoBoxSize);
        _netInfo.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        _netInfo.setMinimumSize(new Dimension(30, 17));
        _netInfo.setPreferredSize(new Dimension(40, 17));
        _netInfo.setMaximumSize(new Dimension(40, 17));
        _netInfo.setEditable(false);
        infoBar.add(netInfo);
        infoBar.add(_netInfo);

        // Create box with the number of characters left for SMS Message.
        infoBar.add(Box.createRigidArea(new Dimension(10, 20)));
        JLabel cpuInfo = new JLabel("CPU: ");
        _cpuInfo = new JTextField();
        _cpuInfo.setBackground(new JButton().getBackground());
        _cpuInfo.setMaximumSize(infoBoxSize);
        _cpuInfo.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        _cpuInfo.setMinimumSize(new Dimension(30, 17));
        _cpuInfo.setPreferredSize(new Dimension(40, 17));
        _cpuInfo.setMaximumSize(new Dimension(40, 17));
        _cpuInfo.setEditable(false);
        infoBar.add(cpuInfo);
        infoBar.add(_cpuInfo);

        // Create box with status messages.
        // Text length max 35 symbols.
        infoBar.add(Box.createRigidArea(new Dimension(10, 20)));
        JLabel memInfo = new JLabel("RAM: ");
        _memInfo = new JTextField();
        _memInfo.setBackground(new JButton().getBackground());
        _memInfo.setMaximumSize(statusBoxSize);
        _memInfo.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        _memInfo.setMinimumSize(new Dimension(30, 17));
        _memInfo.setPreferredSize(new Dimension(40, 17));
        _memInfo.setMaximumSize(new Dimension(40, 17));
        _memInfo.setEditable(false);
        infoBar.add(memInfo);
        infoBar.add(_memInfo);
*/
        return infoBar;
    }

    // Create the Configuration Panel.
    private JLayeredPane ConfigFrame()
    {
        JLayeredPane config = new JLayeredPane();
        config.setBounds(10, 150, 492, 200);

        config.add(serverProps());
        config.add(createTableList());

        frame.getContentPane().add(config);

        return config;
    }

    // Function to position a window in the middle of the screen.
    public void centerFrame(int w, int h, JFrame frame)
    {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds((d.width-w)/2, (d.height-h)/2, w, h);
    }

    // Create ObjectServer Properties Field.
    private JPanel serverProps()
    {
        LogManager.write("Configurator         - DEBUG:    Creating \"OS Properties\" panel (main).", LogHandle.ALL,LogManager.DEBUG);

        JPanel server = new JPanel();
        server.setBounds(10, 10, 230, 200);
        server.setLayout(new BoxLayout(server, BoxLayout.Y_AXIS));

        TitledBorder titledborder = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)),"   Object Server:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        server.setBorder(titledborder);
        server.setBackground(_bgColour);

        server.add(OSSettings());
        server.add(OSButtons());

        return server;
    }

    // Create "OS Properties" sub-panel for OS Settings
    private JPanel OSSettings()
    {
        LogManager.write("Configurator         - DEBUG:    Creating \"OS Properties\" fields.", LogHandle.ALL,LogManager.DEBUG);

        JPanel settings = new JPanel();

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constr = new GridBagConstraints();

        settings.setLayout(gridbag);
        settings.setBackground(_bgColour);

        constr.fill = GridBagConstraints.HORIZONTAL;

        constr.weightx = 1.0;
        JLabel l_name = new JLabel("Server Name:");
        l_name.setForeground(Color.black);
        l_name.setBounds(2, 5, 50, 20);
        gridbag.setConstraints(l_name, constr);
        settings.add(l_name);
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        _name = new JTextField(_vars.getOSName());
        _name.setMinimumSize(new Dimension(30, 5));
        _name.setPreferredSize(new Dimension(100, 20));
        _name.setMaximumSize(new Dimension(100, 20));
        gridbag.setConstraints(_name, constr);
        settings.add(_name);

        //This is to create space between the fields
        JLabel space1 = new JLabel("   ");
        space1.setFont(new Font("Helvetica", Font.PLAIN, 1));
        space1.setForeground(Color.black);
        space1.setBounds(2, 2, 50, 3);
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        gridbag.setConstraints(space1, constr);
        settings.add(space1);

        constr.gridwidth = GridBagConstraints.RELATIVE;
        JLabel l_host = new JLabel("Server Host:");
        l_host.setForeground(Color.black);
        l_host.setBounds(5, 5, 50, 20);
        gridbag.setConstraints(l_host, constr);
        settings.add(l_host);
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        _host = new JTextField(_vars.getOSHost());
        _host.setMinimumSize(new Dimension(30, 5));
        _host.setPreferredSize(new Dimension(100, 20));
        _host.setMaximumSize(new Dimension(100, 20));
        gridbag.setConstraints(_host, constr);
        settings.add(_host);

        //This is to create space between the fields
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        JLabel space2 = new JLabel("   ");
        space2.setFont(new Font("Helvetica", Font.PLAIN, 1));
        space2.setForeground(Color.black);
        space2.setBounds(2, 2, 50, 3);
        gridbag.setConstraints(space2, constr);
        settings.add(space2);

        constr.gridwidth = GridBagConstraints.RELATIVE;
        JLabel l_port = new JLabel("Server Port:");
        l_port.setForeground(Color.black);
        l_port.setBounds(5, 5, 50, 20);
        gridbag.setConstraints(l_port, constr);
        settings.add(l_port);
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        _port = new JTextField(_vars.getOSPort());
        _port.setMinimumSize(new Dimension(30, 5));
        _port.setPreferredSize(new Dimension(100, 20));
        _port.setMaximumSize(new Dimension(100, 20));
        gridbag.setConstraints(_port, constr);
        settings.add(_port);

        //This is to create space between the fields
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        JLabel space3 = new JLabel("   ");
        space3.setFont(new Font("Helvetica", Font.PLAIN, 1));
        space3.setForeground(Color.black);
        space3.setBounds(5, 5, 50, 3);
        gridbag.setConstraints(space3, constr);
        settings.add(space3);

        constr.gridwidth = GridBagConstraints.RELATIVE;
        JLabel l_user = new JLabel("Username:");
        l_user.setForeground(Color.black);
        l_user.setBounds(5, 5, 50, 20);
        gridbag.setConstraints(l_user, constr);
        settings.add(l_user);
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        _user = new JTextField(_vars.getOSUser());
        _user.setMinimumSize(new Dimension(30, 5));
        _user.setPreferredSize(new Dimension(100, 20));
        _user.setMaximumSize(new Dimension(100, 20));
        gridbag.setConstraints(_user, constr);
        settings.add(_user);

        //This is to create space between the fields
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        JLabel space4 = new JLabel("   ");
        space4.setFont(new Font("Helvetica", Font.PLAIN, 1));
        space4.setForeground(Color.black);
        space4.setBounds(2, 2, 50, 3);
        gridbag.setConstraints(space4, constr);
        settings.add(space4);

        constr.gridwidth = GridBagConstraints.RELATIVE;
        JLabel l_pass = new JLabel("Password:");
        l_pass.setForeground(Color.black);
        l_pass.setBounds(5, 5, 50, 20);
        gridbag.setConstraints(l_pass, constr);
        settings.add(l_pass);
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        _pass = new JPasswordField(_vars.getOSPass());
        _pass.setMinimumSize(new Dimension(30, 5));
        _pass.setPreferredSize(new Dimension(100, 20));
        _pass.setMaximumSize(new Dimension(100, 20));
        gridbag.setConstraints(_pass, constr);
        settings.add(_pass);

        return settings;
    }

    // Create "OS Properties" sub-panel for OS Buttons
    private JPanel OSButtons()
    {
        LogManager.write("Configurator         - DEBUG:    Creating \"OS Properties\" buttons.", LogHandle.ALL,LogManager.DEBUG);

        JPanel buttons = new JPanel();
        buttons.setBounds(10, 10, 230, 50);
        buttons.setBackground(_bgColour);
        
        // Create button and its action for adding an event
        Action test = new AbstractAction("Test"){
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){
                try
                {
                    OSConnection testOS = new OSConnection(
                            _name.getText(),
                            _host.getText(),
                            _port.getText(),
                            _user.getText(),
                            _pass.getText(),
                            _secureOS);

                    if (testOS.testConnection())
                    {
                        GENOptionPane.showMessageDialog(null, "Connection to \"" +_name.getText()+ "\" - OK. ", "  Test Object Server connection.  ", GENOptionPane.INFORMATION_MESSAGE);
                    }
                    else
                    {
                        GENOptionPane.showMessageDialog(null, "Could not connect to \"" +_name.getText()+ "\". ", "  Test Object Server connection.  ", GENOptionPane.ERROR_MESSAGE);
                    }
                }
                catch (Exception expt)
                {
                    GENOptionPane.showMessageDialog(null, "ERROR: Connecting to \"" +_name.getText()+ "\". ", "  Test Object Server connection.  ", GENOptionPane.ERROR_MESSAGE);
                }
            }
        };
        _testOSButton = new JButton(test);
        _testOSButton.setFont(new Font("Dialog", Font.BOLD, 11));
        _testOSButton.setBorder(BorderFactory.createRaisedBevelBorder());
        _testOSButton.setPreferredSize(new Dimension(55, 23));

        _secureConnection = new JCheckBox("  Secure Conn.", isOSSecure());
        _secureConnection.setBackground(_bgColour);
        _secureConnection.setEnabled(false);

        buttons.add(_testOSButton);
        buttons.add(Box.createRigidArea(new Dimension(15,5)));
        buttons.add(_secureConnection);

        return buttons;
    }

    // Create Panel for the Event Table list and buttons
    private JPanel createTableList()
    {
        LogManager.write("Configurator         - DEBUG:    Creating \"Event Table Configuration\" panel (main).", LogHandle.ALL,LogManager.DEBUG);

        JPanel tableMain = new JPanel();
        tableMain.setLayout(new BoxLayout(tableMain, BoxLayout.Y_AXIS));
        tableMain.setBounds(250, 10, 230, 100);

        TitledBorder titledborder = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Event Table:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        tableMain.setBorder(titledborder);
        tableMain.setBackground(_bgColour);

        tableMain.add(tableList());
        tableMain.add(tableButtons());

        return tableMain;
    }

    // Create Drop-down list Fieald.
    private JPanel tableList()
    {
        LogManager.write("Configurator         - DEBUG:    Creating \"Event Tables Configuration\" drop-down list.", LogHandle.ALL,LogManager.DEBUG);

        JPanel tableListPanel = new JPanel();
        tableListPanel.setBackground(_bgColour);
        tableListPanel.setBounds(10, 10, 200, 25);
        //TitledBorder titledborder = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Event Table:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        //tableListPanel.setBorder(titledborder);

        _tableName = new JComboBox(eventList.getTableList());
        _tableName.setMinimumSize(new Dimension(100, 20));
        _tableName.setPreferredSize(new Dimension(140,25));
        _tableName.setMaximumSize(new Dimension(140,25));
        _tableName.setForeground(Color.black);
        _tableName.setBackground(Color.white);
        _tableName.setFont(new Font("Dialog", Font.PLAIN, 12));
        _tableName.setBorder(BorderFactory.createCompoundBorder());
        _tableName.addActionListener(this);

        _tableName.setSelectedItem(_vars.getEventTable());

        JLabel l = new JLabel(new ImageIcon(_genhome + "/images/tables.gif"));
        l.setBounds(5, 10, 20, 20);
        tableListPanel.add(l);
        tableListPanel.add(Box.createRigidArea(new Dimension(10,5)));
        tableListPanel.add(_tableName);
        return tableListPanel;
    }

    // Create Buttons Field.
    private JPanel tableButtons()
    {
        LogManager.write("Configurator         - DEBUG:    Creating \"Event Tables Configuration\" buttons.", LogHandle.ALL,LogManager.DEBUG);

        JPanel tableButtonsPanel = new JPanel();
        tableButtonsPanel.setBackground(_bgColour);
        tableButtonsPanel.setBounds(10, 40, 200, 30);

        // Create button and its action for adding an event
        Action addTable = new AbstractAction("Add"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){
                try {
                    new newTableFrame(frame, eventList, "Create Table ", _tableName);
                } catch (Exception expt) {
                    GENOptionPane.showMessageDialog(null, "Failed to create table [" +_tableName.getItemAt(_tableName.getSelectedIndex()).toString()+ "]. ", "  Create Table  ", GENOptionPane.ERROR_MESSAGE);
                }
            }
        };
        _addNewTable = new JButton(addTable);
        _addNewTable.setFont(new Font("Dialog", Font.BOLD, 11));
        _addNewTable.setBorder(BorderFactory.createRaisedBevelBorder());
        _addNewTable.setPreferredSize(new Dimension(60, 23));

        // Create button and its action for adding an event
        Action remTable = new AbstractAction("Delete"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){

                if (!_tableName.getItemAt(_tableName.getSelectedIndex()).toString().equalsIgnoreCase("template"))
                {
                    int index = _tableName.getSelectedIndex();
                    try
                    {
                        if (eventList.deleteTable(_tableName.getItemAt(index).toString()))
                        {
                            // Remove table name from the list
                            _tableName.removeItemAt(index);

                            // Set the table selection to point at the first element from the column
                            if (_tableName.getItemCount() > 0)
                            {
                                _tableName.setSelectedIndex(0);
                            }
                        }
                        else
                        {
                            GENOptionPane.showMessageDialog(null, "Could not delete table \"" +_tableName.getItemAt(index).toString()+ "\" from the directory. ", "  Remove Table  ", GENOptionPane.ERROR_MESSAGE);
                        }
                    }
                    catch (Exception expt)
                    {
                        GENOptionPane.showMessageDialog(null, "Could not remove table \"" +_tableName.getItemAt(index).toString()+ "\" from the list. ", "  Remove Table  ", GENOptionPane.ERROR_MESSAGE);
                    }
                }
                else
                {
                    GENOptionPane.showMessageDialog(null, "Template table cannot be deleted. ", "  Remove Table  ", GENOptionPane.INFORMATION_MESSAGE);
                }
            }
        };
        _deleteTable = new JButton(remTable);
        _deleteTable.setFont(new Font("Dialog", Font.BOLD, 11));
        _deleteTable.setBorder(BorderFactory.createRaisedBevelBorder());
        _deleteTable.setPreferredSize(new Dimension(60, 23));

        tableButtonsPanel.add(Box.createRigidArea(new Dimension(30,5)));
        tableButtonsPanel.add(_addNewTable);
        tableButtonsPanel.add(Box.createRigidArea(new Dimension(5,5)));
        tableButtonsPanel.add(_deleteTable);

        return tableButtonsPanel;
    }

    // Create EventList Frame.
    public JLayeredPane EventsFrame()
    {
        LogManager.write("Configurator         - DEBUG:    Creating \"Events\" table panel (main).", LogHandle.ALL,LogManager.DEBUG);

        JLayeredPane config = new JLayeredPane();
        config.setBounds(10, 150, 492, 200);

        config.add(createTable(), BorderLayout.NORTH);
        config.add(createButtons(), BorderLayout.SOUTH);

        frame.getContentPane().add(config);

        return config;
    }

    public JInternalFrame createTable()
    {
        String eventsTitle = _vars.getEventTable();

        LogManager.write("Configurator         - DEBUG:    Creating \"Events\" Internal Frame - [" +eventsTitle+ "].", LogHandle.ALL,LogManager.DEBUG);
        eventPane = new JScrollPane(eventList.createEventTable(eventsTitle), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        eventsFrame = new JInternalFrame(eventsTitle);
        eventsFrame.setSize(492, 201);

        eventsFrame.setFrameIcon(new ImageIcon(_genhome + "/images/table.gif"));
        eventsFrame.getContentPane().add(eventPane);

        desktop.add(eventsFrame);
        eventsFrame.setVisible(true);

        return eventsFrame;
    }

    JPanel createButtons()
    {
        LogManager.write("Configurator         - DEBUG:    Creating \"Events\" buttons.", LogHandle.ALL,LogManager.DEBUG);

        JPanel eventButtons = new JPanel();
        eventButtons.setBackground(_bgColour);
        eventButtons.setBounds(2, 202, 485, 50);

        // Create button and its action for adding an event
        Action duplicateRow = new AbstractAction("Dupl."){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){
                try
                {
                    eventList.copyRow();
                }
                catch (Exception expt)
                {
                    GENOptionPane.showMessageDialog(null, "Cannot duplicate row [" +eventList.getSelectedRow()+ "].", "  Duplicate Row  ", GENOptionPane.ERROR_MESSAGE);
                }
            }
        };
        JButton dupRowButton = new JButton(duplicateRow);
        dupRowButton.setFont(new Font("Dialog", Font.BOLD, 11));
        dupRowButton.setToolTipText("Duplicate selected event.");
        dupRowButton.setBorder(BorderFactory.createRaisedBevelBorder());
        dupRowButton.setPreferredSize(new Dimension(60, 23));

        // Create button and its action for adding an event
        Action AddRow = new AbstractAction("Add Row"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){
                try
                {
                    rowFrame = new newEventFrame(frame, eventList, "Add event ");
                    rowFrame.newEventFrame(frame, eventList, "Add event ");
                }
                catch (Exception expt)
                {
                    GENOptionPane.showMessageDialog(null, "Could not create \"Add event\" window. ", "  Add event  ", GENOptionPane.ERROR_MESSAGE);
                }
            }
        };
        JButton addRowButton = new JButton(AddRow);
        addRowButton.setFont(new Font("Dialog", Font.BOLD, 11));
        addRowButton.setToolTipText("Add new event.");
        addRowButton.setBorder(BorderFactory.createRaisedBevelBorder());
        addRowButton.setPreferredSize(new Dimension(60, 23));

        // Create button and its action for adding an event
        Action AddCol = new AbstractAction("Add Col"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){
                try
                {
                    colFrame = new newColumnFrame(frame, eventList, "Add column ");
                    colFrame.newColumnFrame(frame, eventList, "Add column ");
                }
                catch (Exception expt)
                {
                    GENOptionPane.showMessageDialog(null, "Could not create \"Add column\" window. ", "  Add column  ", GENOptionPane.ERROR_MESSAGE);
                }
            }
        };
        JButton addColButton = new JButton(AddCol);
        addColButton.setFont(new Font("Dialog", Font.BOLD, 11));
        addColButton.setToolTipText("Add new column.");
        addColButton.setBorder(BorderFactory.createRaisedBevelBorder());
        addColButton.setPreferredSize(new Dimension(60, 23));

        // Create button and its action for deletin an event
        Action DelRow = new AbstractAction("Del Row"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){
                try
                {
                    eventList.removeRow();
                }
                catch (Exception expt)
                {
                    GENOptionPane.showMessageDialog(null, "Could not remove row. ", "  Remove event  ", GENOptionPane.ERROR_MESSAGE);
                }
            }
        };
        JButton delRowBut = new JButton(DelRow);
        delRowBut.setFont(new Font("Dialog", Font.BOLD, 11));
        delRowBut.setToolTipText("Delete selected event.");
        delRowBut.setBorder(BorderFactory.createRaisedBevelBorder());
        delRowBut.setPreferredSize(new Dimension(60, 23));

        // Create button and its action for deletin an event
        Action DelCol = new AbstractAction("Del Col"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){
                try
                {
                    eventList.removeColumn();
                }
                catch (Exception expt)
                {
                    GENOptionPane.showMessageDialog(null, "Could not remove column. ", "  Remove column  ", GENOptionPane.ERROR_MESSAGE);
                }
            }
        };
        JButton delColBut = new JButton(DelCol);
        delColBut.setFont(new Font("Dialog", Font.BOLD, 11));
        delColBut.setToolTipText("Delete selected column.");
        delColBut.setBorder(BorderFactory.createRaisedBevelBorder());
        delColBut.setPreferredSize(new Dimension(60, 23));

        // Create button and its action for deleting all events
        Action DeleteAll = new AbstractAction("Clear"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){
                try
                {
                    eventList.clearTable();
                }
                catch (Exception expt)
                {
                    GENOptionPane.showMessageDialog(null, "Could not clear table. ", "  Clear table  ", GENOptionPane.ERROR_MESSAGE);
                }
            }
        };
        JButton deleteAllButton = new JButton(DeleteAll);
        deleteAllButton.setFont(new Font("Dialog", Font.BOLD, 11));
        deleteAllButton.setToolTipText("Delete the entire table.");
        deleteAllButton.setBorder(BorderFactory.createRaisedBevelBorder());
        deleteAllButton.setPreferredSize(new Dimension(60, 23));

        // Create button and its action for saving and refreshing of the event list
        Action Save = new AbstractAction("Save"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){
                try
                {
                    eventList.saveTable();
                }
                catch (Exception expt)
                {
                    GENOptionPane.showMessageDialog(null, "Could not save table. ", "  Save table  ", GENOptionPane.ERROR_MESSAGE);
                }
            }
        };
        JButton saveButton = new JButton(Save);
        saveButton.setFont(new Font("Dialog", Font.BOLD, 11));
        saveButton.setToolTipText("Save current table.");
        saveButton.setBorder(BorderFactory.createRaisedBevelBorder());
        saveButton.setPreferredSize(new Dimension(60, 23));

        eventButtons.add(dupRowButton, BorderLayout.CENTER);
        eventButtons.add(Box.createRigidArea(new Dimension(5,5)));
        eventButtons.add(addRowButton, BorderLayout.CENTER);
        eventButtons.add(addColButton, BorderLayout.CENTER);
        eventButtons.add(Box.createRigidArea(new Dimension(5,5)));
        eventButtons.add(delRowBut, BorderLayout.CENTER);
        //eventButtons.add(Box.createRigidArea(new Dimension(5,5)));
        eventButtons.add(delColBut, BorderLayout.CENTER);
        eventButtons.add(Box.createRigidArea(new Dimension(5,5)));
        eventButtons.add(deleteAllButton, BorderLayout.CENTER);
        //eventButtons.add(Box.createRigidArea(new Dimension(5,5)));
        eventButtons.add(saveButton, BorderLayout.CENTER);

        return eventButtons;
    }

    private JLayeredPane StatusFrame()
    {
    	JLayeredPane statusTab = new JLayeredPane();
    	statusTab.setBounds(10, 150, 492, 200);
    	statusTab.setBackground(_bgColour);
    	statusTab.add(controlPanel());
    	statusTab.add(statusPanel());

        frame.getContentPane().add(statusTab);

        return statusTab;
    }

    private JPanel controlPanel()
    {
        LogManager.write("Configurator         - DEBUG:    Creating \"Status\" panel (main).", LogHandle.ALL,LogManager.DEBUG);

        String threadsList = _vars.getEventTable();

        threadControlPanel = new JPanel();
        threadControlPanel.setLayout(new BoxLayout(threadControlPanel, BoxLayout.Y_AXIS));
        threadControlPanel.setBounds(10, 10, 250, 220);

        //JTable eventControlTable = createStatusTable();
        threadControlTable = threadList.createThreadTable(threadsList);

        threadPane = new JScrollPane(threadControlTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        threadPane.setMinimumSize(new Dimension(240, 215));
        threadPane.setPreferredSize(new Dimension(250, 220));
        threadPane.setMaximumSize(new Dimension(250, 220));

        threadControlPanel.add(threadPane);

        return threadControlPanel;
    }

    // Create "Status" sub-panel
    private JPanel statusPanel()
    {
        LogManager.write("Configurator         - DEBUG:    Creating \"Status\" field.", LogHandle.ALL,LogManager.DEBUG);

        JPanel settings = new JPanel();
        settings.setBounds(270, 10, 220, 120);
        settings.setBackground(_bgColour);

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constr = new GridBagConstraints();

        settings.setLayout(gridbag);

        constr.fill = GridBagConstraints.HORIZONTAL;

        constr.weightx = 1.0;
        constr.gridwidth = GridBagConstraints.WEST;
        JLabel l_total = new JLabel("Total threads:");
        l_total.setForeground(Color.black);
        l_total.setBounds(2, 5, 35, 20);
        gridbag.setConstraints(l_total, constr);
        settings.add(l_total);
        constr.gridwidth = GridBagConstraints.LINE_END;
        _numThreads = new JTextField("0");
        _numThreads.setMinimumSize(new Dimension(30, 5));
        _numThreads.setPreferredSize(new Dimension(50, 20));
        _numThreads.setMaximumSize(new Dimension(50, 20));
        _numThreads.setBackground(settings.getBackground());
        _numThreads.setHorizontalAlignment(JTextField.RIGHT);
        _numThreads.setBorder(null);
        _numThreads.setEditable(false);
        gridbag.setConstraints(_numThreads, constr);
        settings.add(_numThreads);
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        JLabel m_total = new JLabel(" threads");
        m_total.setFont(new Font("Dialog", Font.PLAIN, 11));
        m_total.setForeground(Color.black);
        //m_total.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        m_total.setMinimumSize(new Dimension(40, 5));
        m_total.setPreferredSize(new Dimension(40, 20));
        m_total.setMaximumSize(new Dimension(40, 20));

        gridbag.setConstraints(m_total, constr);
        settings.add(m_total);

        //This is to create space between the fields
        JLabel space1 = new JLabel("   ");
        space1.setFont(new Font("Helvetica", Font.PLAIN, 3));
        space1.setForeground(Color.black);
        space1.setBounds(5, 5, 20, 10);
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        gridbag.setConstraints(space1, constr);
        settings.add(space1);

        constr.gridwidth = GridBagConstraints.WEST;
        JLabel l_active = new JLabel("Active threads:");
        l_active.setForeground(Color.black);
        l_active.setBounds(5, 5, 35, 20);
        gridbag.setConstraints(l_active, constr);
        settings.add(l_active);
        constr.gridwidth = GridBagConstraints.LINE_END; //end row
        _actThreads = new JTextField("0");
        _actThreads.setMinimumSize(new Dimension(30, 5));
        _actThreads.setPreferredSize(new Dimension(50, 20));
        _actThreads.setMaximumSize(new Dimension(50, 20));
        _actThreads.setBackground(settings.getBackground());
        _actThreads.setHorizontalAlignment(JTextField.RIGHT);
        _actThreads.setBorder(null);
        _actThreads.setEditable(false);
        gridbag.setConstraints(_actThreads, constr);
        settings.add(_actThreads);
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        JLabel m_active = new JLabel(" threads");
        m_active.setFont(new Font("Dialog", Font.PLAIN, 11));
        m_active.setForeground(Color.black);
        //m_active.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        //m_active.setBounds(5, 5, 15, 20);
        m_active.setMinimumSize(new Dimension(40, 5));
        m_active.setPreferredSize(new Dimension(40, 20));
        m_active.setMaximumSize(new Dimension(40, 20));

        gridbag.setConstraints(m_active, constr);
        settings.add(m_active);

        //This is to create space between the fields
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        JLabel space2 = new JLabel("   ");
        space2.setFont(new Font("Helvetica", Font.PLAIN, 3));
        space2.setForeground(Color.black);
        space2.setBounds(5, 5, 20, 10);
        gridbag.setConstraints(space2, constr);
        settings.add(space2);

        constr.gridwidth = GridBagConstraints.WEST;
        JLabel l_sent = new JLabel("Total sent:");
        l_sent.setForeground(Color.black);
        l_sent.setBounds(5, 5, 35, 20);
        gridbag.setConstraints(l_sent, constr);
        settings.add(l_sent);
        constr.gridwidth = GridBagConstraints.LINE_END; //end row
        _totalSent = new JTextField("0");
        _totalSent.setMinimumSize(new Dimension(30, 5));
        _totalSent.setPreferredSize(new Dimension(50, 20));
        _totalSent.setMaximumSize(new Dimension(50, 20));
        _totalSent.setBackground(settings.getBackground());
        _totalSent.setHorizontalAlignment(JTextField.RIGHT);
        _totalSent.setBorder(null);
        _totalSent.setEditable(false);
        gridbag.setConstraints(_totalSent, constr);
        settings.add(_totalSent);
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        JLabel m_sent = new JLabel(" events");
        m_sent.setFont(new Font("Dialog", Font.PLAIN, 11));
        m_sent.setForeground(Color.black);
        //m_sent.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        //m_sent.setBounds(5, 5, 15, 20);
        m_sent.setMinimumSize(new Dimension(40, 5));
        m_sent.setPreferredSize(new Dimension(40, 20));
        m_sent.setMaximumSize(new Dimension(40, 20));

        gridbag.setConstraints(m_sent, constr);
        settings.add(m_sent);

        //This is to create space between the fields
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        JLabel space3 = new JLabel("   ");
        space3.setFont(new Font("Helvetica", Font.PLAIN, 3));
        space3.setForeground(Color.black);
        space3.setBounds(5, 5, 20, 10);
        gridbag.setConstraints(space3, constr);
        settings.add(space3);

        constr.gridwidth = GridBagConstraints.WEST;
        JLabel l_min = new JLabel("Speed:");
        l_min.setForeground(Color.black);
        l_min.setBounds(5, 5, 35, 20);
        gridbag.setConstraints(l_min, constr);
        settings.add(l_min);
        constr.gridwidth = GridBagConstraints.LINE_END; //end row
        _eventPerMin = new JTextField("0");
        _eventPerMin.setMinimumSize(new Dimension(30, 5));
        _eventPerMin.setPreferredSize(new Dimension(50, 20));
        _eventPerMin.setMaximumSize(new Dimension(50, 20));
        _eventPerMin.setBackground(settings.getBackground());
        _eventPerMin.setHorizontalAlignment(JTextField.RIGHT);
        _eventPerMin.setBorder(null);
        _eventPerMin.setEditable(false);
        gridbag.setConstraints(_eventPerMin, constr);
        settings.add(_eventPerMin);
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        JLabel m_min = new JLabel(" e/min");
        m_min.setFont(new Font("Dialog", Font.PLAIN, 11));
        m_min.setForeground(Color.black);
        //m_min.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        //m_min.setBounds(5, 5, 15, 20);
        m_min.setMinimumSize(new Dimension(40, 5));
        m_min.setPreferredSize(new Dimension(40, 20));
        m_min.setMaximumSize(new Dimension(40, 20));

        gridbag.setConstraints(m_min, constr);
        settings.add(m_min);

        //This is to create space between the fields
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        JLabel space4 = new JLabel("   ");
        space4.setFont(new Font("Helvetica", Font.PLAIN, 3));
        space4.setForeground(Color.black);
        space4.setBounds(5, 5, 20, 10);
        gridbag.setConstraints(space4, constr);
        settings.add(space4);
/*
        constr.gridwidth = GridBagConstraints.WEST;
        JLabel l_cpu = new JLabel("CPU Usage:");
        l_cpu.setForeground(Color.black);
        l_cpu.setBounds(5, 5, 50, 20);
        gridbag.setConstraints(l_cpu, constr);
        settings.add(l_cpu);
        constr.gridwidth = GridBagConstraints.LINE_END; //end row
        _cpuUsage = new JTextField("0");
        _cpuUsage.setMinimumSize(new Dimension(30, 5));
        _cpuUsage.setPreferredSize(new Dimension(50, 20));
        _cpuUsage.setMaximumSize(new Dimension(50, 20));
        _cpuUsage.setBackground(settings.getBackground());
        _cpuUsage.setHorizontalAlignment(JTextField.RIGHT);
        _cpuUsage.setBorder(null);
        _cpuUsage.setEditable(false);
        gridbag.setConstraints(_cpuUsage, constr);
        settings.add(_cpuUsage);
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        JLabel m_cpu = new JLabel(" %");
        m_cpu.setFont(new Font("Dialog", Font.PLAIN, 11));
        m_cpu.setForeground(Color.black);
        //m_cpu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        //m_cpu.setBounds(5, 5, 15, 20);
        m_cpu.setMinimumSize(new Dimension(15, 5));
        m_cpu.setPreferredSize(new Dimension(15, 20));
        m_cpu.setMaximumSize(new Dimension(15, 20));

        gridbag.setConstraints(m_cpu, constr);
        settings.add(m_cpu);


        //This is to create space between the fields
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        JLabel space5 = new JLabel("   ");
        space5.setFont(new Font("Helvetica", Font.PLAIN, 3));
        space5.setForeground(Color.black);
        space5.setBounds(5, 5, 20, 10);
        gridbag.setConstraints(space5, constr);
        settings.add(space5);

        constr.gridwidth = GridBagConstraints.WEST;
        JLabel l_mem = new JLabel("MEM Usage:");
        l_mem.setForeground(Color.black);
        l_mem.setBounds(5, 5, 50, 20);
        gridbag.setConstraints(l_mem, constr);
        settings.add(l_mem);
        constr.gridwidth = GridBagConstraints.LINE_END; //end row
        _memUsage = new JTextField("0");
        _memUsage.setMinimumSize(new Dimension(30, 5));
        _memUsage.setPreferredSize(new Dimension(50, 20));
        _memUsage.setMaximumSize(new Dimension(50, 20));
        _memUsage.setBackground(settings.getBackground());
        _memUsage.setHorizontalAlignment(JTextField.RIGHT);
        _memUsage.setBorder(null);
        _memUsage.setEditable(false);
        gridbag.setConstraints(_memUsage, constr);
        settings.add(_memUsage);
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        JLabel m_mem = new JLabel(" MB");
        m_mem.setFont(new Font("Dialog", Font.PLAIN, 11));
        m_mem.setForeground(Color.black);
        //m_mem.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        //m_mem.setBounds(5, 5, 15, 20);
        m_mem.setMinimumSize(new Dimension(15, 5));
        m_mem.setPreferredSize(new Dimension(15, 20));
        m_mem.setMaximumSize(new Dimension(15, 20));

        gridbag.setConstraints(m_mem, constr);
        settings.add(m_mem);


        //This is to create space between the fields
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        JLabel space6 = new JLabel("   ");
        space6.setFont(new Font("Helvetica", Font.PLAIN, 3));
        space6.setForeground(Color.black);
        space6.setBounds(5, 5, 20, 10);
        gridbag.setConstraints(space6, constr);
        settings.add(space6);

        constr.gridwidth = GridBagConstraints.WEST;
        JLabel l_net = new JLabel("NET Usage:");
        l_net.setForeground(Color.black);
        l_net.setBounds(5, 5, 30, 20);
        gridbag.setConstraints(l_net, constr);
        settings.add(l_net);
        constr.gridwidth = GridBagConstraints.LINE_END;
        _netUsage = new JTextField("0");
        _netUsage.setMinimumSize(new Dimension(30, 5));
        _netUsage.setPreferredSize(new Dimension(30, 20));
        _netUsage.setMaximumSize(new Dimension(30, 20));
        _netUsage.setBackground(settings.getBackground());
        _netUsage.setHorizontalAlignment(JTextField.RIGHT);
        _netUsage.setBorder(null);
        _netUsage.setEditable(false);
        gridbag.setConstraints(_netUsage, constr);
        settings.add(_netUsage);
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        JLabel m_net = new JLabel(" kB/s");
        m_net.setFont(new Font("Dialog", Font.PLAIN, 11));
        m_net.setForeground(Color.black);
        //m_net.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        //m_net.setBounds(5, 5, 15, 20);
        m_net.setMinimumSize(new Dimension(15, 5));
        m_net.setPreferredSize(new Dimension(15, 20));
        m_net.setMaximumSize(new Dimension(15, 20));

        gridbag.setConstraints(m_net, constr);
        settings.add(m_net);

        //This is to create space between the fields
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        JLabel space7 = new JLabel("   ");
        space7.setFont(new Font("Helvetica", Font.PLAIN, 3));
        space7.setForeground(Color.black);
        space7.setBounds(5, 5, 20, 10);
        gridbag.setConstraints(space7, constr);
        settings.add(space7);
*/
        constr.gridwidth = GridBagConstraints.WEST;
        JLabel l_buf = new JLabel("Buffer:");
        l_buf.setForeground(Color.black);
        l_buf.setBounds(5, 5, 35, 20);
        gridbag.setConstraints(l_buf, constr);
        settings.add(l_buf);
        constr.gridwidth = GridBagConstraints.LINE_END;
        _buffer = new JTextField("0");
        _buffer.setMinimumSize(new Dimension(30, 5));
        _buffer.setPreferredSize(new Dimension(30, 20));
        _buffer.setMaximumSize(new Dimension(30, 20));
        _buffer.setBackground(settings.getBackground());
        _buffer.setHorizontalAlignment(JTextField.RIGHT);
        _buffer.setBorder(null);
        _buffer.setEditable(false);
        gridbag.setConstraints(_buffer, constr);
        settings.add(_buffer);
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        JLabel m_buf = new JLabel(" events");
        m_buf.setFont(new Font("Dialog", Font.PLAIN, 11));
        m_buf.setForeground(Color.black);
        //m_buf.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        //m_buf.setBounds(5, 5, 15, 20);
        m_buf.setMinimumSize(new Dimension(40, 5));
        m_buf.setPreferredSize(new Dimension(40, 20));
        m_buf.setMaximumSize(new Dimension(40, 20));

        gridbag.setConstraints(m_buf, constr);
        settings.add(m_buf);

        //This is to create space between the fields
        constr.gridwidth = GridBagConstraints.REMAINDER; //end row
        JLabel spaceFiller = new JLabel("   ");
        spaceFiller.setFont(new Font("Helvetica", Font.PLAIN, 3));
        spaceFiller.setForeground(Color.black);
        spaceFiller.setBackground(Color.black);
        spaceFiller.setBounds(5, 5, 20, 10);
        gridbag.setConstraints(spaceFiller, constr);
        settings.add(spaceFiller);

        return settings;
    }

    // Create Function for closing the previous window.
    // closeFrame is called before creating any of the messages windows in order
    // to have only one window on the screen at a time.
    public void closeFrame()
    {
        Component[] comp = desktop.getComponents();
        for (int i =0; i < comp.length; i++)
        {
            if (comp[i] instanceof JInternalFrame)
            {
                JInternalFrame frame = (JInternalFrame)comp[i];
                frame.dispose();
            }
        }
    }

    // Create Actions
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();

        // Buttons and Menu items Section
        if (source == saveMenuItem || source == saveButton)
        {
            // Save users settings
            saveUsersets();

            // Save current table
            eventList.saveTable();

            String EventsTitle = eventList.getTableName();

            eventPane = null;
            eventPane = new JScrollPane(eventList.createEventTable(EventsTitle), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

            eventsFrame.setTitle(EventsTitle);
            eventsFrame.setContentPane(eventPane);

            LogManager.write("Configurator         - DEBUG:    Rebuilding Thread Control Table.", LogHandle.ALL,LogManager.DEBUG);
            String newEventsTitle = _tableName.getSelectedItem().toString();
            threadList.setTableName(newEventsTitle);

            threadPane = null;
            threadPane = new JScrollPane(threadList.createThreadTable(newEventsTitle), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            threadPane.setMinimumSize(new Dimension(240, 215));
            threadPane.setPreferredSize(new Dimension(250, 220));
            threadPane.setMaximumSize(new Dimension(250, 220));

            threadControlPanel.removeAll();
            threadControlPanel.add(threadPane);

        }
        else if (source == exitMenuItem)
        {
            saveUsersets();
            System.exit(0);
        }
        else if (source == startMenuItem || source == startButton)
        {
            startMenuItem.setEnabled(false);
            startButton.setEnabled(false);
            stopMenuItem.setEnabled(true);
            stopButton.setEnabled(true);
            setEditableOSSettings(false);
            setEditableEventTable(false);

            threadControlTable.setEnabled(true);

            String oldEventsTitle = eventList.getTableName();
            String newEventsTitle = _tableName.getSelectedItem().toString();

            if (!oldEventsTitle.equalsIgnoreCase(newEventsTitle))
            {
                LogManager.write("Configurator         - DEBUG:    Overwriting table [" +oldEventsTitle+ "] with [" +newEventsTitle+ "] \"Events\" table.", LogHandle.ALL,LogManager.DEBUG);
                eventList.setTableName(newEventsTitle);

                eventPane = null;
                eventPane = new JScrollPane(eventList.createEventTable(newEventsTitle), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

                eventsFrame.setTitle(newEventsTitle);
                eventsFrame.setContentPane(eventPane);

                LogManager.write("Configurator         - DEBUG:    Rebuilding Thread Control Table.", LogHandle.ALL,LogManager.DEBUG);
                threadList.setTableName(newEventsTitle);

                threadPane = null;
                threadPane = new JScrollPane(threadList.createThreadTable(newEventsTitle), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                threadPane.setMinimumSize(new Dimension(240, 215));
                threadPane.setPreferredSize(new Dimension(250, 220));
                threadPane.setMaximumSize(new Dimension(250, 220));

                threadControlPanel.removeAll();
                threadControlPanel.add(threadPane);
            }

            if (!threadList.startGenerator())
            {
                LogManager.write("Configurator         - MAJOR:    Failed to start the Generator.", LogHandle.ALL,LogManager.MAJOR);

                startMenuItem.setEnabled(true);
                startButton.setEnabled(true);
                stopMenuItem.setEnabled(false);
                stopButton.setEnabled(false);
                setEditableOSSettings(true);
                setEditableEventTable(true);

                _numThreads.setText("0");
                _actThreads.setText("0");
                _eventPerMin.setText("0");
                _buffer.setText("0");
                _minuteInfo.setText("0");

                threadControlTable.setEnabled(false);
            }
        }
        else if (source == stopMenuItem || source == stopButton)
        {
            startMenuItem.setEnabled(true);
            startButton.setEnabled(true);
            stopMenuItem.setEnabled(false);
            stopButton.setEnabled(false);
            setEditableOSSettings(true);
            setEditableEventTable(true);

            _numThreads.setText("0");
            _actThreads.setText("0");
            _eventPerMin.setText("0");
            _buffer.setText("0");
            _minuteInfo.setText("0");

            threadControlTable.setEnabled(false);

            threadList.stopGenerator();
        }
        else if (source == clearStatMenuItem || source == clearButton)
        {
            threadList.resetTableStat();
            _totalSent.setText("0");
            _eventPerMin.setText("0");
            _numEventsSent = 0;
            _totalInfo.setText("0");
            _buffer.setText("0");
            _minuteInfo.setText("0");
        }
        else if (source == clearTableMenuItem || source == clearButton)
        {

        }
        else if (source == addRowMenuItem)
        {
            try
            {
                rowFrame = new newEventFrame(frame, eventList, "Add event ");
                rowFrame.newEventFrame(frame, eventList, "Add event ");
            }
            catch (Exception expt)
            {
                GENOptionPane.showMessageDialog(null, "Could not create \"Add event\" window. ", "  Add column  ", GENOptionPane.ERROR_MESSAGE);
            }
        }
        else if (source == addColMenuItem)
        {
            try
            {
                colFrame = new newColumnFrame(frame, eventList, "Add column ");
                colFrame.newColumnFrame(frame, eventList, "Add column ");
            }
            catch (Exception expt)
            {
                GENOptionPane.showMessageDialog(null, "Could not create \"Add column\" window. ", "  Add column  ", GENOptionPane.ERROR_MESSAGE);
            }
        }
        else if (source == remRowMenuItem)
        {
            try
            {
                eventList.removeRow();
            }
            catch (Exception expt)
            {
                GENOptionPane.showMessageDialog(null, "Could not remove row. ", "  Remove row  ", GENOptionPane.ERROR_MESSAGE);
            }
        }
        else if (source == remColMenuItem)
        {
            try
            {
                eventList.removeColumn();
            }
            catch (Exception expt)
            {
                GENOptionPane.showMessageDialog(null, "Could not remove column. ", "  Remove column  ", GENOptionPane.ERROR_MESSAGE);
            }
        }
        else if (source == saveTableMenuItem)
        {
            try
            {
                eventList.saveTable();
            }
            catch (Exception expt)
            {
                GENOptionPane.showMessageDialog(null, "Could not Save table. ", "  Save table  ", GENOptionPane.ERROR_MESSAGE);
            }
        }
        else if (source == clearTableMenuItem)
        {
            try
            {
                eventList.clearTable();
            }
            catch (Exception expt)
            {
                GENOptionPane.showMessageDialog(null, "Could not clear entire table. ", "  Clear table  ", GENOptionPane.ERROR_MESSAGE);
            }
        }
        else if (source == manualMenuItem)
        {
            try
            {
                Process p = Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler UsersGuide.pdf");
                p.waitFor();
            }
            catch (IOException ioe){}
            catch (InterruptedException inte){}
        }
        else if (source == wikiMenuItem)
        {
            try
            {
            	java.awt.Desktop.getDesktop().browse(new URI(WIKI_PAGE));
            }
            catch (IOException ioe){}
            catch (URISyntaxException use){}
        }
        else if (source == aboutMenuItem)
        {
            JLabel l = new JLabel(new ImageIcon(_genhome + "/images/splash.jpg"));
            GENOptionPane.showMessageDialog(frame, l, " NETCOOL / Event Generator", GENOptionPane.PLAIN_MESSAGE);
        }

        // Switching between different tables
        if(source == _tableName)
        {
            if (eventPane != null)
            {
                String oldEventsTitle = eventList.getTableName();
                String newEventsTitle = _tableName.getSelectedItem().toString();

                if (!oldEventsTitle.equalsIgnoreCase(newEventsTitle))
                {
                    LogManager.write("Configurator         - DEBUG:    Overwriting table [" +oldEventsTitle+ "] with [" +newEventsTitle+ "] \"Events\" table.", LogHandle.ALL,LogManager.DEBUG);
                    eventList.setTableName(newEventsTitle);

                    eventPane = null;
                    eventPane = new JScrollPane(eventList.createEventTable(newEventsTitle), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

                    eventsFrame.setTitle(newEventsTitle);
                    eventsFrame.setContentPane(eventPane);

                    LogManager.write("Configurator         - DEBUG:    Rebuilding Thread Control Table.", LogHandle.ALL,LogManager.DEBUG);
                    threadList.setTableName(newEventsTitle);

                    threadPane = null;
                    threadPane = new JScrollPane(threadList.createThreadTable(newEventsTitle), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                    threadPane.setMinimumSize(new Dimension(240, 215));
                    threadPane.setPreferredSize(new Dimension(250, 220));
                    threadPane.setMaximumSize(new Dimension(250, 220));

                    threadControlPanel.removeAll();
                    threadControlPanel.add(threadPane);
                }
            }
        }
    }

    private void saveUsersets()
    {
        _vars.setEventTable(eventList.getTableName());
        _vars.setOSName(_name.getText());
        _vars.setOSHost(_host.getText());
        _vars.setOSPort(_port.getText());
        _vars.setOSUser(_user.getText());
        _vars.setOSPass(_pass.getText());
        _vars.setOSSecure(_secureConnection.isSelected());
        _vars.saveProps();
    }

    protected static void setEditableOSSettings(boolean edit)
    {
        _name.setEnabled(edit);
        _host.setEnabled(edit);
        _port.setEnabled(edit);
        _user.setEnabled(edit);
        _pass.setEnabled(edit);
        //_secureConnection.setEnabled(edit);
        _testOSButton.setEnabled(edit);
    }

    protected static void setEditableEventTable(boolean edit)
    {
        _tableName.setEnabled(edit);
        _addNewTable.setEnabled(edit);
        _deleteTable.setEnabled(edit);
    }

    // Set the OS Connection to be secure
    public void setOSSecure(boolean sec)
    {
        _secureOS = sec;
    }

    public boolean isOSSecure()
    {
        return _secureOS;
    }

    public static void main(String[] args)
    {
        if (args.length <= 0) {
            new Configurator("run");
        } else {
            new Configurator(args[0]);
        }
    }
}