package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

import static android.content.ContentValues.TAG;

/**
 * Created by Molu on 5/3/17.
 */

public class GroupMessenger2 extends Application {

    private int latestPriority;
    private int portNumber;
    private ArrayList<String> remotePortList;
    private PriorityQueue<MessageContainer> priorityQueueOfMsgs = new PriorityQueue<MessageContainer>(30);
    private float processPriority;
    private ContentResolver mContentResolver;

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    private int msgId = 0;

    public String getBrokenRemotePort() {
        return brokenRemotePort;
    }

    public void setBrokenRemotePort(String brokenRemotePort) {
        this.brokenRemotePort = brokenRemotePort;
    }

    private String brokenRemotePort = "0";


    public void setContentResolver(ContentResolver _cr)
    {
        mContentResolver = _cr;
    }


    public void setMessagesDelivered(int messagesDelivered) {
        this.messagesDelivered = messagesDelivered;
    }

    private int messagesDelivered;

    public int getLatestPriority() {
        return latestPriority;
    }

    public void setLatestPriority(Float latestPriority) {
        this.latestPriority = latestPriority.intValue();
    }

    public float generateProposedPriority(){
        return this.getLatestPriority() + this.getThisProcessPriority() + 1;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public ArrayList<String> getRemotePortList() {
        return remotePortList;
    }

    public void setRemotePortList(ArrayList<String> remotePortList) {
        this.remotePortList = remotePortList;
    }

    public float getThisProcessPriority() {
        return processPriority;
    }

    public void setThisProcessPriority(int processPriority) {
        this.processPriority = (float)processPriority/10;
    }

    public void insertMsg(MessageContainer msgObject)
    {

        if(priorityQueueOfMsgs.contains(msgObject)) {

            Log.d("insertIntoPrioQ","Updating: " + msgObject.getMsg() + " with priority: " + msgObject.getPriority()  + " marked as deliverable");
            priorityQueueOfMsgs.remove(msgObject);
            priorityQueueOfMsgs.add(msgObject);
            Log.d("insertIntoPrioQ","Size of prioQ: " + String.valueOf(priorityQueueOfMsgs.size()));
        }
        else
        {
            Log.d("insertIntoPrioQ","first time addition of: " + msgObject.getMsg() + " with priority: " + msgObject.getPriority());
            priorityQueueOfMsgs.add(msgObject);
            Log.d("insertIntoPrioQ","FirstTime - Size of prioQ: " + String.valueOf(priorityQueueOfMsgs.size()));
        }

        popPriorityQueue();

    }

    public void cleanupPriorityQueue()
    {

        String portToRemove = getBrokenRemotePort();
        Log.e("cleanup", "Failed port: " + portToRemove);

        for (MessageContainer temp : priorityQueueOfMsgs) {
            if (temp.getMsgSource().equals(portToRemove)) {
                Log.e("cleanup", "Removed message: " + temp.getMsg());
                priorityQueueOfMsgs.remove(temp);
            }
        }

    }

    public void popPriorityQueue()
    {
        while(priorityQueueOfMsgs.peek() != null && priorityQueueOfMsgs.peek().isDeliverable()) {

            Log.d("popPrioQ","Popping!");
            MessageContainer temp = priorityQueueOfMsgs.poll();

            Uri mUri = BuildUri.build("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");

            ContentValues mContentValue = new ContentValues();
            mContentValue.put("key", Integer.toString(messagesDelivered));
            mContentValue.put("value", temp.getMsg());

            messagesDelivered++;
            mContentResolver.insert(mUri, mContentValue);
        }
    }

}
