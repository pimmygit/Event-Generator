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

import java.awt.Frame;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;

public class GENOptionPane extends JOptionPane
{
    private String _genhome;

    public GENOptionPane( )
    {
        // Get the property (EGEN_HOME env var must be set)
        _genhome = System.getProperty("EGEN_HOME");
        // Several checks to determine that the Program starts from the correct location
        File home = new File(_genhome);
        if (_genhome == null)
        {
            System.out.println("Option Panel         - Home Directory not set. Exit.\n\n");
            System.exit(-1);
        }
        if (!home.exists())
        {
            System.out.println("Option Panel         - Home Directory ["+_genhome+"] does not exist. Exit.\n\n");
            System.exit(-1);
        }

	    Frame frame = new Frame();
        ImageIcon img = new ImageIcon(_genhome + "/images/euro.gif");
        frame.setIconImage(img.getImage());
        setRootFrame(frame);
    }
}



