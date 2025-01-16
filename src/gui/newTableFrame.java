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
package gui;

import log.LogManager;
import log.LogHandle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import java.awt.event.ActionEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;
import java.io.File;

import utils.GENOptionPane;

/**
 * Created by IntelliJ IDEA.
 * User: pimmy
 * Date: Jan 28, 2005
 * Time: 8:55:26 AM
 * To change this template use Options | File Templates.
 */
public class newTableFrame extends JDialog
{
	private static final long serialVersionUID = 1L;

	private String _genhome;
    private Frame _frame;
    private newTableFrame _tableFrame;

    public DefaultTableModel tableModel;

    private JTextField _tableName;
    private JComboBox _tableList;

    private EventTable _eventTable;

    public newTableFrame(Frame frm, EventTable eventList, String title, JComboBox tableList)
    {
        super(frm, title);
        _tableFrame = this;
        _tableList = tableList;
        
        // Get the property (EGEN_HOME env var must be set)
        _genhome = System.getProperty("EGEN_HOME");
        // Several checks to determine that the Program starts from the correct location
        File home = new File(_genhome);
        if (_genhome == null)
        {
            System.out.println("Add Table Frame      - Home Directory not set. Exit.\n\n");
            System.exit(-1);
        }
        if (!home.exists())
        {
            System.out.println("Add Table Frame      - Home Directory ["+_genhome+"] does not exist. Exit.\n\n");
            System.exit(-1);
        }

        _eventTable = eventList;
        _frame = frm;
        _frame.setEnabled(false);
        Container contentPane = getContentPane();
        contentPane.add(emptyPanel(), BorderLayout.NORTH);
        contentPane.add(fieldsPanel(), BorderLayout.CENTER);
        pack();

        int w = 200;
        int h = 125;
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        _tableFrame.setBounds((d.width-w)/2, (d.height-h)/2, w, h);
        _tableFrame.setSize(w, h);
        _tableFrame.setResizable(false);
        _tableFrame.setVisible(true);

        WindowListener exitListener = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                _frame.setEnabled(true);
                _tableFrame = null;
                dispose();
            }
        };
        _tableFrame.addWindowListener(exitListener);


        LogManager.write("Add Table Frame      - Ready.",LogHandle.ALL,LogManager.DEBUG);
    }

    private JPanel emptyPanel()
    {
        JPanel space = new JPanel();
        space.setLayout(new BoxLayout(space, BoxLayout.X_AXIS));

        space.setMinimumSize(new Dimension(170, 2));
        space.setPreferredSize(new Dimension(190, 5));
        space.setMaximumSize(new Dimension(190,5));

        return space;
    }

    private JPanel fieldsPanel()
    {
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setMinimumSize(new Dimension(170, 110));
        fieldsPanel.setPreferredSize(new Dimension(190, 115));
        fieldsPanel.setMaximumSize(new Dimension(190,115));

        fieldsPanel.add(createName());
        fieldsPanel.add(buttonPanel());

        return fieldsPanel;
    }

    private JPanel createName()
    {
        JPanel panel1 = new JPanel();
        panel1.setLayout(null);
        panel1.setMinimumSize(new Dimension(170, 55));
        panel1.setPreferredSize(new Dimension(195, 60));
        panel1.setMaximumSize(new Dimension(195, 60));

        TitledBorder titledborder = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Table Name:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        panel1.setBorder(titledborder);

        _tableName = new JTextField();
        _tableName.setBorder(BorderFactory.createLoweredBevelBorder());
        _tableName.setBounds(50, 25, 125, 20);

        JLabel l = new JLabel(new ImageIcon(_genhome + "/images/read.gif"));
        l.setBounds(10, 25, 30, 25);

        panel1.add(l);
        panel1.add(_tableName);

        LogManager.write("Add Table Frame      - \"Table Name\" field created.",LogHandle.ALL,LogManager.DEBUG);

        return panel1;
    }

    private JPanel buttonPanel()
    {
        JPanel buttons = new JPanel();
        buttons.setMinimumSize(new Dimension(170, 22));
        buttons.setPreferredSize(new Dimension(190, 30));
        buttons.setMaximumSize(new Dimension(190,30));

        // Create button and its action for adding an event
        Action Add = new AbstractAction("Create"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){
                doneAction();
                _frame.setEnabled(true);
                _tableFrame = null;
                dispose();
            }
        };
        JButton addButton = new JButton(Add);
        addButton.setFont(new Font("Dialog", Font.BOLD, 11));
        addButton.setBorder(BorderFactory.createRaisedBevelBorder());
        addButton.setPreferredSize(new Dimension(60, 23));

        // Create button and its action for deletin an event
        Action Cancel = new AbstractAction("Cancel"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){
                _frame.setEnabled(true);
                _tableFrame = null;
                dispose();
            }
        };
        JButton cancelButton = new JButton(Cancel);
        cancelButton.setFont(new Font("Dialog", Font.BOLD, 11));
        cancelButton.setBorder(BorderFactory.createRaisedBevelBorder());
        cancelButton.setPreferredSize(new Dimension(60, 23));

        buttons.add(addButton, BorderLayout.CENTER);
        buttons.add(Box.createRigidArea(new Dimension(10,10)));
        buttons.add(cancelButton, BorderLayout.CENTER);

        LogManager.write("Add Table Frame      - \"Buttons\" field created.",LogHandle.ALL,LogManager.DEBUG);

        return buttons;
    }

    private void doneAction()
    {
        try
        {
            // Create the new table from our template file
            int result = _eventTable.createTable(_tableName.getText());

            if (result == 0)
            {
                // In case of successful creation we add the name of the table to the list
                _tableList.addItem(_tableName.getText());
                _tableList.setSelectedItem(_tableName.getText());
                LogManager.write("Add Table Frame      - Table [" +_tableName.getText()+ "] added to the \"Table List\".",LogHandle.ALL,LogManager.DEBUG);
            }
            else if (result == 1)
            {
                GENOptionPane.showMessageDialog(null, "Template table corrupt or does not exist. ", "  Create Table  ", GENOptionPane.ERROR_MESSAGE);
            }
            else if (result == 2)
            {
                GENOptionPane.showMessageDialog(null, "Table \"" +_tableName.getText()+ "\" already exist. ", "  Create Table  ", GENOptionPane.ERROR_MESSAGE);
            }
            else if (result == 3)
            {
                GENOptionPane.showMessageDialog(null, "Table \"" +_tableName.getText()+ "\" cannot be created. ", "  Create Table  ", GENOptionPane.ERROR_MESSAGE);
            }
            else if (result == 4)
            {
                GENOptionPane.showMessageDialog(null, "Error occurred while creating table \"" +_tableName.getText()+ "\". ", "  Create Table  ", GENOptionPane.ERROR_MESSAGE);
            }
            else if (result == 5)
            {
                GENOptionPane.showMessageDialog(null, "Error occurred while setting the table name \"" +_tableName.getText()+ "\". Please modify the table name manually in \"" +_genhome+ "/props/" +_tableName.getText()+ ".xml\"", "  Create Table  ", GENOptionPane.ERROR_MESSAGE);
            }
        }
        catch (Exception expt)
        {
            LogManager.write("Add Table Frame      - ERROR: Table [" +_tableName.getText()+ "] failed to be created.",LogHandle.ALL,LogManager.MAJOR);
            GENOptionPane.showMessageDialog(null, "Failed to create table [" +_tableName.getText()+ "]. ", "  Create Table  ", GENOptionPane.ERROR_MESSAGE);
        }
    }
}
