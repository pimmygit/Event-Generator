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
import java.util.regex.Pattern;

import utils.GENOptionPane;

/**
 * Created by IntelliJ IDEA.
 * User: pimmy
 * Date: Jan 28, 2005
 * Time: 8:55:26 AM
 * To change this template use Options | File Templates.
 */
public class newColumnFrame extends JDialog
{
	private static final long serialVersionUID = 1L;
	
	private final int VAL_INT = 0;
    private final int VAL_CHAR = 1;

    protected int _valueType = VAL_CHAR;

    private String _columnName;
    private String _defaultValue;

    private Frame frame;
    private newColumnFrame columnFrame;

    public DefaultTableModel tableModel;
    public boolean eventFrameOpen;

    private JPanel namePanel;
    private JPanel valuePanel1;
    private JPanel valuePanel2;
    private JPanel startFromPanel;
    private JPanel stepToPanel;

    private JTextField name;
    private JTextField defValue1;
    private JTextField defValue2;
    private JTextField startFromValue;
    private JTextField stepToValue;

    private TitledBorder nullLabel;
    private TitledBorder defLabel1;
    private TitledBorder prefLabel;
    private TitledBorder suffLabel;
    private TitledBorder startLabel;
    private TitledBorder stepLabel;
    private TitledBorder fromLabel;
    private TitledBorder toLabel;
    private TitledBorder latMinLabel;
    private TitledBorder latMaxLabel;
    private TitledBorder longMinLabel;
    private TitledBorder longMaxLabel;

    private JRadioButton charButton;
    private JRadioButton intButton;
    private JRadioButton timeButton;
    private JRadioButton incrButton;
    private JRadioButton randButton;
    private JRadioButton gisButton;
    private JRadioButton normalButton;

    private EventTable eventTable;

    public newColumnFrame(Frame frm, EventTable eventList, String title)
    {
        super(frm, title);
        columnFrame = this;

        eventTable = eventList;
        frame = frm;
        frame.setEnabled(false);
        Container contentPane = getContentPane();
        contentPane.add(emptyPanel(), BorderLayout.NORTH);
        contentPane.add(allFieldsPanel(), BorderLayout.CENTER);
        contentPane.add(buttonPanel(), BorderLayout.SOUTH);

        pack();

        int w = 285;
        int h = 380;
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        columnFrame.setBounds((d.width-w)/2, (d.height-h)/2, w, h);
        columnFrame.setSize(w, h);
        columnFrame.setResizable(false);
        columnFrame.setVisible(true);

        WindowListener exitListener = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                frame.setEnabled(true);
                columnFrame = null;
                dispose();
            }
        };
        columnFrame.addWindowListener(exitListener);

        nullLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  N/A:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        defLabel1 = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Default Value:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        prefLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Prefix:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        suffLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Suffix:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        startLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Start:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        stepLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Step:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        fromLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  From:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        toLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  To:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        latMinLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Min Latitude:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        latMaxLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Max Latitude:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        longMinLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Min Longitude:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        longMaxLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Max Longitude:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);

        LogManager.write("Add Column Frame     - Ready.",LogHandle.ALL,LogManager.DEBUG);
    }

    public void newColumnFrame(Component cmp, EventTable eventList, String title)
    {
        if (columnFrame == null)
        {
            columnFrame = new newColumnFrame(JOptionPane.getFrameForComponent(cmp), eventList, title);

            int w = 285;
            int h = 380;
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            columnFrame.setBounds((d.width-w)/2, (d.height-h)/2, w, h);
            columnFrame.setSize(w, h);
            columnFrame.setResizable(false);
            columnFrame.setVisible(true);

            WindowListener exitListener = new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    frame.setEnabled(true);
                    columnFrame = null;
                    dispose();
                }
            };
            columnFrame.addWindowListener(exitListener);

            nullLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  N/A:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            defLabel1 = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Default Value:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            prefLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Prefix:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            suffLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Suffix:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            startLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Start:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            stepLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Step:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            fromLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  From:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            toLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  To:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            latMinLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Min Latitude:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            latMaxLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Max Latitude:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            longMinLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Min Longitude:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            longMaxLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Max Longitude:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        }
    }

    public void newColumnFrame(Component cmp, EventTable eventList, String title, DefaultTableModel tModel)
    {
        if (columnFrame == null)
        {
            columnFrame = new newColumnFrame(JOptionPane.getFrameForComponent(cmp), eventList, title);

            int w = 285;
            int h = 380;
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            columnFrame.setBounds((d.width-w)/2, (d.height-h)/2, w, h);
            columnFrame.setSize(w, h);
            columnFrame.setResizable(false);
            columnFrame.setVisible(true);

            WindowListener exitListener = new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    frame.setEnabled(true);
                    columnFrame = null;
                    dispose();
                }
            };
            columnFrame.addWindowListener(exitListener);

            nullLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  N/A:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            defLabel1 = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Default Value:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            prefLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Prefix:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            suffLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Suffix:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            startLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Start:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            stepLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Step:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            fromLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  From:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            toLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  To:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            latMinLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Min Latitude:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            latMaxLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Max Latitude:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            longMinLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Min Longitude:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
            longMaxLabel = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Max Longitude:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        }

        tableModel = tModel;

        if (tableModel != null)
        {
            eventFrameOpen = true;
            LogManager.write("Add Column Frame     - Event Frame Open: Adding column to both tables.",LogHandle.ALL,LogManager.DEBUG);
        }
    }

    private JPanel emptyPanel()
    {
        JPanel space = new JPanel();
        space.setLayout(new BoxLayout(space, BoxLayout.X_AXIS));

        space.setMinimumSize(new Dimension(280, 2));
        space.setPreferredSize(new Dimension(285, 5));
        space.setMaximumSize(new Dimension(385,5));

        return space;
    }

    private JPanel allFieldsPanel()
    {
        JPanel allFieldsPanel = new JPanel();
        allFieldsPanel.setLayout(new BoxLayout(allFieldsPanel, BoxLayout.X_AXIS));
        allFieldsPanel.setMinimumSize(new Dimension(280, 250));
        allFieldsPanel.setPreferredSize(new Dimension(285, 322));
        allFieldsPanel.setMaximumSize(new Dimension(285, 322));

        allFieldsPanel.add(textFieldsPanel());
        allFieldsPanel.add(radioFieldsPanel());

        return allFieldsPanel;
    }

    private JPanel textFieldsPanel()
    {
        JPanel textFieldsPanel = new JPanel();
        textFieldsPanel.setLayout(new BoxLayout(textFieldsPanel, BoxLayout.Y_AXIS));
        textFieldsPanel.setMinimumSize(new Dimension(170, 260));
        textFieldsPanel.setPreferredSize(new Dimension(185, 330));
        textFieldsPanel.setMaximumSize(new Dimension(185,330));

        textFieldsPanel.add(createName());
        textFieldsPanel.add(createValue1());
        textFieldsPanel.add(createValue2());
        textFieldsPanel.add(createStartFrom());
        textFieldsPanel.add(createStepTo());

        return textFieldsPanel;
    }

    private JPanel radioFieldsPanel()
    {
        JPanel radioFieldsPanel = new JPanel();
        radioFieldsPanel.setLayout(new BoxLayout(radioFieldsPanel, BoxLayout.Y_AXIS));
        radioFieldsPanel.setMinimumSize(new Dimension(110, 300));
        radioFieldsPanel.setPreferredSize(new Dimension(120, 315));
        radioFieldsPanel.setMaximumSize(new Dimension(120,315));

        radioFieldsPanel.add(createIntChar());
        radioFieldsPanel.add(createOptions());

        return radioFieldsPanel;
    }

    private JPanel createIntChar()
    {
        JPanel radioIntCharPanel = new JPanel();
        radioIntCharPanel.setLayout(new BoxLayout(radioIntCharPanel, BoxLayout.Y_AXIS));
        radioIntCharPanel.setMinimumSize(new Dimension(110, 100));
        radioIntCharPanel.setPreferredSize(new Dimension(120, 103));
        radioIntCharPanel.setMaximumSize(new Dimension(120,103));

        TitledBorder titledborder = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Value Type:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        radioIntCharPanel.setBorder(titledborder);

        // Create radio buttons and their actions
        Action charAction = new AbstractAction(" Char"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){

                if (incrButton.isSelected() || randButton.isSelected())
                {
                    defValue1.setText("");
                    defValue1.setEnabled(true);
                    defValue1.setBackground(Color.WHITE);
                    valuePanel1.setBorder(prefLabel);

                    defValue2.setText("");
                    defValue2.setEnabled(true);
                    defValue2.setBackground(Color.WHITE);
                    valuePanel2.setBorder(suffLabel);
                }
            }
        };
        charButton = new JRadioButton(charAction);
        charButton.setSelected(true);

        Action intAction = new AbstractAction(" Integer"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){

                if (incrButton.isSelected() || randButton.isSelected())
                {
                    defValue1.setText("");
                    defValue1.setEnabled(false);
                    defValue1.setBackground(Color.LIGHT_GRAY);
                    valuePanel1.setBorder(nullLabel);

                    defValue2.setText("");
                    defValue2.setEnabled(false);
                    defValue2.setBackground(Color.LIGHT_GRAY);
                    valuePanel2.setBorder(nullLabel);
                }
            }
        };
        intButton = new JRadioButton(intAction);

        ButtonGroup group = new ButtonGroup();
        group.add(charButton);
        group.add(intButton);

        radioIntCharPanel.add(charButton);
        radioIntCharPanel.add(Box.createRigidArea(new Dimension(5,12)));
        radioIntCharPanel.add(intButton);

        return radioIntCharPanel;
    }

    private JPanel createOptions()
    {
        JPanel radioOptionsPanel = new JPanel();
        radioOptionsPanel.setLayout(new BoxLayout(radioOptionsPanel, BoxLayout.Y_AXIS));
        radioOptionsPanel.setMinimumSize(new Dimension(110, 200));
        radioOptionsPanel.setPreferredSize(new Dimension(120, 212));
        radioOptionsPanel.setMaximumSize(new Dimension(120, 212));

        TitledBorder titledborder = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Options:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        radioOptionsPanel.setBorder(titledborder);

        // Create radio buttons and their actions
        Action normalAction = new AbstractAction(" Normal"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){

                defValue1.setText("");
                defValue1.setEnabled(true);
                defValue1.setBackground(Color.WHITE);
                valuePanel1.setBorder(defLabel1);

                defValue2.setText("");
                defValue2.setEnabled(false);
                defValue2.setBackground(Color.LIGHT_GRAY);
                valuePanel2.setBorder(nullLabel);

                startFromValue.setText("");
                startFromValue.setEnabled(false);
                startFromValue.setBackground(Color.LIGHT_GRAY);
                startFromPanel.setBorder(nullLabel);

                stepToValue.setText("");
                stepToValue.setEnabled(false);
                stepToValue.setBackground(Color.LIGHT_GRAY);
                stepToPanel.setBorder(nullLabel);
            }
        };
        normalButton = new JRadioButton(normalAction);
        normalButton.setSelected(true);

        Action timeAction = new AbstractAction(" Time"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){

                defValue1.setText("Current time.");
                defValue1.setEnabled(false);
                defValue1.setBackground(Color.LIGHT_GRAY);
                valuePanel1.setBorder(nullLabel);

                defValue2.setText("Current time.");
                defValue2.setEnabled(false);
                defValue2.setBackground(Color.LIGHT_GRAY);
                valuePanel2.setBorder(nullLabel);

                startFromValue.setText("Current time.");
                startFromValue.setEnabled(false);
                startFromValue.setBackground(Color.LIGHT_GRAY);
                startFromPanel.setBorder(nullLabel);

                stepToValue.setText("Current time.");
                stepToValue.setEnabled(false);
                stepToValue.setBackground(Color.LIGHT_GRAY);
                stepToPanel.setBorder(nullLabel);
            }
        };
        timeButton = new JRadioButton(timeAction);

        Action incrAction = new AbstractAction(" Increment"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){

                if (charButton.isSelected())
                {
                    defValue1.setText("");
                    defValue1.setEnabled(true);
                    defValue1.setBackground(Color.WHITE);
                    valuePanel1.setBorder(prefLabel);

                    defValue2.setText("");
                    defValue2.setEnabled(true);
                    defValue2.setBackground(Color.WHITE);
                    valuePanel2.setBorder(suffLabel);
                }
                else
                {
                    defValue1.setText("");
                    defValue1.setEnabled(false);
                    defValue1.setBackground(Color.LIGHT_GRAY);
                    valuePanel1.setBorder(nullLabel);

                    defValue2.setText("");
                    defValue2.setEnabled(false);
                    defValue2.setBackground(Color.LIGHT_GRAY);
                    valuePanel2.setBorder(nullLabel);
                }

                startFromValue.setText("");
                startFromValue.setEnabled(true);
                startFromValue.setBackground(Color.WHITE);
                startFromPanel.setBorder(startLabel);

                stepToValue.setText("");
                stepToValue.setEnabled(true);
                stepToValue.setBackground(Color.WHITE);
                stepToPanel.setBorder(stepLabel);
            }
        };
        incrButton = new JRadioButton(incrAction);

        Action randAction = new AbstractAction(" Random"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){

                if (charButton.isSelected())
                {
                    defValue1.setText("");
                    defValue1.setEnabled(true);
                    defValue1.setBackground(Color.WHITE);
                    valuePanel1.setBorder(prefLabel);

                    defValue2.setText("");
                    defValue2.setEnabled(true);
                    defValue2.setBackground(Color.WHITE);
                    valuePanel2.setBorder(suffLabel);
                }
                else
                {
                    defValue1.setText("");
                    defValue1.setEnabled(false);
                    defValue1.setBackground(Color.LIGHT_GRAY);
                    valuePanel1.setBorder(nullLabel);

                    defValue2.setText("");
                    defValue2.setEnabled(false);
                    defValue2.setBackground(Color.LIGHT_GRAY);
                    valuePanel2.setBorder(nullLabel);
                }

                startFromValue.setText("");
                startFromValue.setEnabled(true);
                startFromValue.setBackground(Color.WHITE);
                startFromPanel.setBorder(fromLabel);

                stepToValue.setText("");
                stepToValue.setEnabled(true);
                stepToValue.setBackground(Color.WHITE);
                stepToPanel.setBorder(toLabel);
            }
        };
        randButton = new JRadioButton(randAction);

        Action gisAction = new AbstractAction(" GIS"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){

                defValue1.setText("");
                defValue1.setEnabled(true);
                defValue1.setBackground(Color.WHITE);
                valuePanel1.setBorder(latMinLabel);

                defValue2.setText("");
                defValue2.setEnabled(true);
                defValue2.setBackground(Color.WHITE);
                valuePanel2.setBorder(latMaxLabel);

                startFromValue.setText("");
                startFromValue.setEnabled(true);
                startFromValue.setBackground(Color.WHITE);
                startFromPanel.setBorder(longMinLabel);

                stepToValue.setText("");
                stepToValue.setEnabled(true);
                stepToValue.setBackground(Color.WHITE);
                stepToPanel.setBorder(longMaxLabel);
            }
        };
        gisButton = new JRadioButton(gisAction);

        ButtonGroup group = new ButtonGroup();
        group.add(normalButton);
        group.add(incrButton);
        group.add(randButton);
        group.add(timeButton);
        group.add(gisButton);

        radioOptionsPanel.add(normalButton);
        radioOptionsPanel.add(Box.createRigidArea(new Dimension(5,12)));
        radioOptionsPanel.add(incrButton);
        radioOptionsPanel.add(Box.createRigidArea(new Dimension(5,12)));
        radioOptionsPanel.add(randButton);
        radioOptionsPanel.add(Box.createRigidArea(new Dimension(5,12)));
        radioOptionsPanel.add(timeButton);
        radioOptionsPanel.add(Box.createRigidArea(new Dimension(5,12)));
        radioOptionsPanel.add(gisButton);

        return radioOptionsPanel;
    }

    private JPanel createName()
    {
        namePanel = new JPanel();
        namePanel.setLayout(null);
        namePanel.setMinimumSize(new Dimension(160, 60));
        namePanel.setPreferredSize(new Dimension(185, 65));
        namePanel.setMaximumSize(new Dimension(185, 65));

        TitledBorder titledborder = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Column name:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        namePanel.setBorder(titledborder);

        name = new JTextField();
        name.setBorder(BorderFactory.createLoweredBevelBorder());
        name.setBounds(15, 25, 135, 20);

        namePanel.add(name);

        LogManager.write("Add Column Frame     - \"Column name\" field created.",LogHandle.ALL,LogManager.DEBUG);

        return namePanel;
    }

    private JPanel createValue1()
    {
        valuePanel1 = new JPanel();
        valuePanel1.setLayout(null);
        valuePanel1.setMinimumSize(new Dimension(160, 60));
        valuePanel1.setPreferredSize(new Dimension(185, 65));
        valuePanel1.setMaximumSize(new Dimension(185, 65));

        TitledBorder titledborder = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  Default Value:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        valuePanel1.setBorder(titledborder);

        defValue1 = new JTextField();
        defValue1.setBorder(BorderFactory.createLoweredBevelBorder());
        defValue1.setBounds(15, 25, 135, 20);

        valuePanel1.add(defValue1);

        LogManager.write("Add Column Frame     - \"Column value\" field created.",LogHandle.ALL,LogManager.DEBUG);

        return valuePanel1;
    }

    private JPanel createValue2()
    {
        valuePanel2 = new JPanel();
        valuePanel2.setLayout(null);
        valuePanel2.setMinimumSize(new Dimension(160, 60));
        valuePanel2.setPreferredSize(new Dimension(185, 65));
        valuePanel2.setMaximumSize(new Dimension(185, 65));

        TitledBorder titledborder = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  N/A:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        valuePanel2.setBorder(titledborder);

        defValue2 = new JTextField();
        defValue2.setBorder(BorderFactory.createLoweredBevelBorder());
        defValue2.setBounds(15, 25, 135, 20);
        defValue2.setEnabled(false);
        defValue2.setBackground(Color.LIGHT_GRAY);

        valuePanel2.add(defValue2);

        LogManager.write("Add Column Frame     - \"Column value\" field created.",LogHandle.ALL,LogManager.DEBUG);

        return valuePanel2;
    }

    private JPanel createStartFrom()
    {
        startFromPanel = new JPanel();
        startFromPanel.setLayout(null);
        startFromPanel.setMinimumSize(new Dimension(160, 60));
        startFromPanel.setPreferredSize(new Dimension(185, 65));
        startFromPanel.setMaximumSize(new Dimension(185, 65));

        TitledBorder titledborder = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  N/A:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        startFromPanel.setBorder(titledborder);

        startFromValue = new JTextField();
        startFromValue.setBorder(BorderFactory.createLoweredBevelBorder());
        startFromValue.setBounds(15, 25, 135, 20);
        startFromValue.setEnabled(false);
        startFromValue.setBackground(Color.LIGHT_GRAY);

        startFromPanel.add(startFromValue);

        LogManager.write("Add Column Frame     - \"Start/From\" field created.",LogHandle.ALL,LogManager.DEBUG);

        return startFromPanel;
    }

    private JPanel createStepTo()
    {
        stepToPanel = new JPanel();
        stepToPanel.setLayout(null);
        stepToPanel.setMinimumSize(new Dimension(160, 60));
        stepToPanel.setPreferredSize(new Dimension(185, 65));
        stepToPanel.setMaximumSize(new Dimension(185, 65));

        TitledBorder titledborder = new TitledBorder(new TitledBorder(new EtchedBorder(0, Color.darkGray, Color.white)), "  N/A:  ", 1, 2, new Font("Dialog", 0, 12), Color.black);
        stepToPanel.setBorder(titledborder);

        stepToValue = new JTextField();
        stepToValue.setBorder(BorderFactory.createLoweredBevelBorder());
        stepToValue.setBounds(15, 25, 135, 20);
        stepToValue.setEnabled(false);
        stepToValue.setBackground(Color.LIGHT_GRAY);

        stepToPanel.add(stepToValue);

        LogManager.write("Add Column Frame     - \"Step/To\" field created.",LogHandle.ALL,LogManager.DEBUG);

        return stepToPanel;
    }


    private JPanel buttonPanel()
    {
        JPanel buttons = new JPanel();
        buttons.setMinimumSize(new Dimension(280, 32));
        buttons.setPreferredSize(new Dimension(290, 35));
        buttons.setMaximumSize(new Dimension(295, 35));

        // Create button and its action for adding an event
        Action Add = new AbstractAction("Add"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){
                if (verifyFields())
                {
                    doneAction();
                    frame.setEnabled(true);
                    columnFrame = null;
                    dispose();
                }
            }
        };
        JButton addButton = new JButton(Add);
        addButton.setBorder(BorderFactory.createRaisedBevelBorder());
        addButton.setPreferredSize(new Dimension(70, 25));

        // Create button and its action for deletin an event
        Action Cancel = new AbstractAction("Cancel"){

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e){
                frame.setEnabled(true);
                columnFrame = null;
                dispose();
            }
        };
        JButton cancelButton = new JButton(Cancel);
        cancelButton.setBorder(BorderFactory.createRaisedBevelBorder());
        cancelButton.setPreferredSize(new Dimension(70, 25));

        buttons.add(addButton, BorderLayout.CENTER);
        buttons.add(Box.createRigidArea(new Dimension(10,10)));
        buttons.add(cancelButton, BorderLayout.CENTER);

        LogManager.write("Add Column Frame     - \"Buttons\" field created.",LogHandle.ALL,LogManager.DEBUG);

        return buttons;
    }

    private boolean verifyFields()
    {
        if (name.getText().trim().equalsIgnoreCase("_ID")  ||
                name.getText().trim().equalsIgnoreCase("_Act") ||
                name.getText().trim().equalsIgnoreCase("_Poll") ||
                name.getText().trim().equalsIgnoreCase("_Stop"))
        {
            GENOptionPane.showMessageDialog(null, "Column name cannot be set to: "+name.getText()+". ", "  New Column  ", GENOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (name.getText().trim().equalsIgnoreCase(""))
        {
            GENOptionPane.showMessageDialog(null, "Column name field cannot be empty. ", "  New Column  ", GENOptionPane.ERROR_MESSAGE);
            return false;
        }

        // The start value of this variable must be always "", as we concatenate the other parameters.
        _defaultValue = "";

        if (intButton.isSelected() && !timeButton.isSelected()) {
            if (!defValue1.getText().trim().equalsIgnoreCase("")) {
            	
                int test = -1;
                try {
                    test = Integer.valueOf(defValue1.getText()).intValue();
                    
                    if (test < 0) {
                        GENOptionPane.showMessageDialog(null, "Column value type error.\n\"Default\" value cannot be negative. ", "  New Column  ", GENOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                } catch (Exception e) {
                    GENOptionPane.showMessageDialog(null, "Column value type error.\n\"Default\" value must be only digits. ", "  New Column  ", GENOptionPane.ERROR_MESSAGE);
                    return false;
                }
	                
                _defaultValue = defValue1.getText();                
            } else {
                _defaultValue = "0";
            }
            
	        _columnName = name.getText();
	        _valueType = VAL_INT;
	        
            LogManager.write("Add Column Frame     - Column value type set to [Integer].",LogHandle.ALL,LogManager.DEBUG);
        } else if (incrButton.isSelected()) {
            if (!startFromValue.getText().trim().equalsIgnoreCase("")) {
            	
                int test = -1;
                try
                {
                    test = Integer.valueOf(startFromValue.getText()).intValue();
                }
                catch (Exception e)
                {
                    GENOptionPane.showMessageDialog(null, "Column value type error.\n\"Start\" value must be only digits and positive value. ", "  New Column  ", GENOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (test < 0)
                {
                    GENOptionPane.showMessageDialog(null, "Column value type error.\n\"Start\" value must be only digits and positive value. ", "  New Column  ", GENOptionPane.ERROR_MESSAGE);
                    return false;
                }
                _defaultValue = _defaultValue.concat("Start:"+startFromValue.getText());
            }
            else
            {
                _defaultValue = "Start:0";
            }

            if (!stepToValue.getText().trim().equalsIgnoreCase("") && charButton.isSelected())
            {
                int test = -1;
                try
                {
                    test = Integer.valueOf(stepToValue.getText()).intValue();
                }
                catch (Exception e)
                {
                    GENOptionPane.showMessageDialog(null, "Column value type error.\n\"Step\" value must be only digits and positive value. ", "  New Column  ", GENOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (test < 0)
                {
                    GENOptionPane.showMessageDialog(null, "Column value type error.\n\"Step\" value must be only digits and positive value. ", "  New Column  ", GENOptionPane.ERROR_MESSAGE);
                    return false;
                }
                _defaultValue = _defaultValue.concat(", Step:"+stepToValue.getText());
            }
            else
            {
                _defaultValue = _defaultValue.concat(", Step:1");
            }

            // Add the prefix if exists
            if (!defValue1.getText().trim().equalsIgnoreCase("") && charButton.isSelected())
            {
                _defaultValue = _defaultValue.concat(", Pref:"+defValue1.getText());
            }
            // Add the suffix if exists
            if (!defValue2.getText().trim().equalsIgnoreCase(""))
            {
                _defaultValue = _defaultValue.concat(", Suff:"+defValue2.getText());
            }

            _columnName = name.getText();

            if (intButton.isSelected()) {
                _valueType = VAL_INT;
            } else {
                _valueType = VAL_CHAR;
            }

            LogManager.write("Add Column Frame     - Column value type set to [Incremened Int].",LogHandle.ALL,LogManager.DEBUG);
        }
        else if (randButton.isSelected())
        {
            if (!startFromValue.getText().trim().equalsIgnoreCase(""))
            {
                int test = -1;
                try
                {
                    test = Integer.valueOf(startFromValue.getText()).intValue();
                }
                catch (Exception e)
                {
                    GENOptionPane.showMessageDialog(null, "Column value type error.\n\"From\" value must be only digits. ", "  New Column  ", GENOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (test < 0)
                {
                    GENOptionPane.showMessageDialog(null, "Column value type error.\n\"From\" value must be only digits. ", "  New Column  ", GENOptionPane.ERROR_MESSAGE);
                    return false;
                }
                _defaultValue = _defaultValue.concat("From:"+startFromValue.getText());
            }
            else
            {
                _defaultValue = "From:0";
            }

            if (!stepToValue.getText().trim().equalsIgnoreCase("") && charButton.isSelected())
            {
                int test = -1;
                try
                {
                    test = Integer.valueOf(stepToValue.getText()).intValue();
                }
                catch (Exception e)
                {
                    GENOptionPane.showMessageDialog(null, "Column value type error.\n\"To\" value must be only digits. ", "  New Column  ", GENOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (test < 0)
                {
                    GENOptionPane.showMessageDialog(null, "Column value type error.\n\"To\" value must be only digits. ", "  New Column  ", GENOptionPane.ERROR_MESSAGE);
                    return false;
                }
                _defaultValue = _defaultValue.concat(", To:"+stepToValue.getText());
            }
            else
            {
                _defaultValue = _defaultValue.concat(", To:1");
            }

            // Add the prefix if exists
            if (!defValue1.getText().trim().equalsIgnoreCase(""))
            {
                _defaultValue = _defaultValue.concat(", Pref:"+defValue1.getText());
            }
            // Add the suffix if exists
            if (!defValue2.getText().trim().equalsIgnoreCase(""))
            {
                _defaultValue = _defaultValue.concat(", Suff:"+defValue2.getText());
            }

            _columnName = name.getText();

            if (intButton.isSelected()) {
                _valueType = VAL_INT;
            } else {
                _valueType = VAL_CHAR;
            }

            LogManager.write("Add Column Frame     - Column value type set to [Random Int].",LogHandle.ALL,LogManager.DEBUG);
        }
        else if (timeButton.isSelected()) {
        	
            _columnName = name.getText();
            _defaultValue = defValue1.getText();

            if (intButton.isSelected()) {
                _valueType = VAL_INT;
            } else {
                _valueType = VAL_CHAR;
            }

            LogManager.write("Add Column Frame     - Column value type set to [UNIX Time].",LogHandle.ALL,LogManager.DEBUG);
        } else if (gisButton.isSelected()) {
            if (!defValue1.getText().trim().equalsIgnoreCase("")) {
            	
                boolean tst = Pattern.matches("[-+]*[0-9]+[.]?[0-9]*",defValue1.getText());
                if (tst)
                {
                    _defaultValue = _defaultValue.concat("minLat:"+defValue1.getText()+", ");
                }
                else
                {
                    GENOptionPane.showMessageDialog(null, "Column value type error.\n\"Min Latitude\" value must be real number. ", "  New Column  ", GENOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            else
            {
                _defaultValue = "minLat:-90, ";
            }

            if (!defValue2.getText().trim().equalsIgnoreCase(""))
            {
                boolean tst = Pattern.matches("[-+]*[0-9]+[.]?[0-9]*",defValue2.getText());
                if (tst)
                {
                    _defaultValue = _defaultValue.concat("maxLat:"+defValue2.getText()+", ");
                }
                else
                {
                    GENOptionPane.showMessageDialog(null, "Column value type error.\n\"Max Latitude\" value must be real number. ", "  New Column  ", GENOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            else
            {
                _defaultValue = _defaultValue.concat("maxLat:90");
            }

            if (!startFromValue.getText().trim().equalsIgnoreCase(""))
            {
                boolean tst = Pattern.matches("[-+]*[0-9]+[.]?[0-9]*",startFromValue.getText());
                if (tst)
                {
                    _defaultValue = _defaultValue.concat("minLong:"+startFromValue.getText()+", ");
                }
                else
                {
                    GENOptionPane.showMessageDialog(null, "Column value type error.\n\"Min Longitude\" value must be real number. ", "  New Column  ", GENOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            else
            {
                _defaultValue = "minLong:-180, ";
            }

            if (!stepToValue.getText().trim().equalsIgnoreCase(""))
            {
                boolean tst = Pattern.matches("[-+]*[0-9]+[.]?[0-9]*",stepToValue.getText());
                if (tst)
                {
                    _defaultValue = _defaultValue.concat("maxLong:"+stepToValue.getText());
                }
                else
                {
                    GENOptionPane.showMessageDialog(null, "Column value type error.\n\"Max Longitude\" value must be real number. ", "  New Column  ", GENOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            else
            {
                _defaultValue = _defaultValue.concat("maxLong:180");
            }
            _columnName = name.getText();
            _valueType = VAL_CHAR;

            LogManager.write("Add Column Frame     - Column value type set to [GIS Coordinates].",LogHandle.ALL,LogManager.DEBUG);
        }
        else
        {
            _columnName = name.getText();
            _defaultValue = defValue1.getText();
            _valueType = VAL_CHAR;

            LogManager.write("Add Column Frame     - Column value type set to [Var Char].",LogHandle.ALL,LogManager.DEBUG);
        }

        return true;
    }

    private void doneAction()
    {
        try
        {
            if (eventTable.appendColumn(_columnName, _defaultValue, _valueType))
            {
                LogManager.write("Add Column Frame     - Column [" +name.getText()+ "] added to the \"Main Event Table\".",LogHandle.ALL,LogManager.DEBUG);

                // If the "New Event Frame" window is open, we have to add the column to it as well as to the main Event Table.
                if (eventFrameOpen)
                {
                    String nm;
                    Object vl;
                    nm = eventTable.eventTableModel.getColumnName(eventTable.eventTableModel.getColumnCount()-1);
                    vl = eventTable.eventTableModel.getValueAt(eventTable.eventTableModel.getRowCount()-1, eventTable.eventTableModel.getColumnCount()-1);
                    Object[] newCol = {nm, vl};
                    tableModel.addRow(newCol);

                    LogManager.write("Add Column Frame     - Column [" +name.getText()+ "] added to the \"New Event Table\".",LogHandle.ALL,LogManager.DEBUG);
                }
            }
            else
            {
                LogManager.write("Add Column Frame     - ERROR: Column [" +name.getText()+ "] FAILED be added to the \"Main Event Table\".",LogHandle.ALL,LogManager.MAJOR);
                GENOptionPane.showMessageDialog(null, "Failed to add column. ", "  Add Column  ", GENOptionPane.ERROR_MESSAGE);
            }
        }
        catch (Exception expt)
        {
            LogManager.write("Add Column Frame     - Column [" +name.getText()+ "] FAILED be added to the \"Main Event Table\".",LogHandle.ALL,LogManager.MAJOR);
            GENOptionPane.showMessageDialog(null, "Failed to add column. ", "  Add Column  ", GENOptionPane.ERROR_MESSAGE);
        }
    }
}
