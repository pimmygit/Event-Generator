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

import java.awt.Font;
import javax.swing.JMenu;


public class GENMenu extends JMenu
{
    public GENMenu()
    {
        this("");
    }
    
    public GENMenu(String text)
    {
        super(text);
        
        setFont(new Font("Dialog", Font.PLAIN, 12));
    }
}