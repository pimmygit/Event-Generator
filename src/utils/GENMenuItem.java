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
import javax.swing.Icon;
import javax.swing.Action;
import javax.swing.JMenuItem;


public class GENMenuItem extends JMenuItem
{
    public GENMenuItem()
    {
        this("");
    }
    
    public GENMenuItem(String text)
    {
        super(text);
        
        setFont();
    }
    
    public GENMenuItem(Action action)
    {
        super(action);
        
        setFont();
    }

    public GENMenuItem(String text, Icon icon)
    {
        super(text, icon);
        
        setFont();
    }
    
    public GENMenuItem(String text, int mnemonic)
    {
        super(text, mnemonic);
        
        setFont();
    }
    
    private void setFont()
    {
        setFont(new Font("Dialog", Font.PLAIN, 12));       
    }
}