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

import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: pimmy
 * Date: Apr 15, 2005
 * Time: 2:53:39 PM
 * To change this template use Options | File Templates.
 */
public class EventQueue
{
    LinkedList<Object> queue;

    public EventQueue()
    {
        queue = new LinkedList<Object>();

        // If the queue is to be used by multiple threads,
        // the queue must be wrapped with code to synchronize the methods
        //queue = (LinkedList)Collections.synchronizedList(queue);
    }

    // Add work to the work queue
    public synchronized void addEvent(SingleEvent event)
    {
        queue.addLast(event);
        notify();
    }

    // Retrieve work from the work queue; block if the queue is empty
    public synchronized Object getEvent() throws InterruptedException
    {
        while (queue.isEmpty()) {
            wait();
        }
        return queue.removeFirst();
    }

    // Returns the number of elements in the queue
    public synchronized int getSize()
    {
        return queue.size();
    }

    // Test if the queue is empty
    public synchronized boolean isEmpty()
    {
        return queue.isEmpty();
    }

    // Removes all elements from the queue
    public synchronized void clearQueue()
    {
        queue.clear();
    }
}

