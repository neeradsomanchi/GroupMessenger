package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static android.content.ContentValues.TAG;

/**
 * Created by Molu on 21/2/17.
 */

class ServerTask extends AsyncTask<ServerSocket, String, Void> {

    public GroupMessenger2 appInfo;
    public enum headerEnum {FirstTime, AgreedPriority, BrokenPortInfo}
    public String msg = null,sourceOfMessage = null;
    public Float priority = null;
    public boolean deliver = false;
    public Integer msgId = null;

    public void resetVariables()
    {
        msg = null;
        sourceOfMessage = null;
        priority = null;
        msgId = null;
        deliver = false;
    }

    public void createMessageAndInsert()
    {
        if(sourceOfMessage != null && msg != null) {

            MessageContainer messageToInsert = new MessageContainer(msg);

            messageToInsert.setDeliverable(deliver);
            messageToInsert.setMsgSource(sourceOfMessage);
            messageToInsert.setPriority(priority);
            messageToInsert.setMsgId(msgId);

            appInfo.insertMsg(messageToInsert);
        }
        else
        {
            Log.e(TAG,"SERVER SOMETHING IS NULL!");
        }
    }


    ServerTask(GroupMessenger2 variablesObject) {
        this.appInfo = variablesObject;
    }

    @Override
    protected Void doInBackground(ServerSocket... sockets) {
        ServerSocket serverSocket = sockets[0];
        int count = 0;



        while (count < 99999999) {
            try {

                this.resetVariables();

                Socket receiverSocket = serverSocket.accept();

                InputStreamReader tempISR = new InputStreamReader(receiverSocket.getInputStream(), "UTF-8");
                BufferedReader buffReader = new BufferedReader(tempISR);

                OutputStreamWriter outStream = new OutputStreamWriter(receiverSocket.getOutputStream(), "UTF-8");
                PrintWriter printWriter = new PrintWriter(outStream);


                String []header = buffReader.readLine().split(" ");

                if(header!=null) switch (headerEnum.valueOf(header[0])) {
                    case FirstTime:
                        Log.e("FirstTimeEnum", header[0]);

                        priority = appInfo.generateProposedPriority();
                        appInfo.setLatestPriority(priority);

                        msg = buffReader.readLine();

                        printWriter.println(priority);
                        printWriter.flush();


                        deliver = false;
                        sourceOfMessage = header[1];
                        msgId = Integer.parseInt(header[2]);

                        createMessageAndInsert();
                        break;

                    case AgreedPriority:
                        Log.e("AgreedPriorityEnum", header[0]);

                        String agreedPriority = buffReader.readLine();
                        priority = Float.parseFloat(agreedPriority);

                        msg = buffReader.readLine();

                        deliver = true;
                        sourceOfMessage = header[1];
                        msgId = Integer.parseInt(header[2]);

                        printWriter.write('A');
                        printWriter.flush();

                        if (appInfo.getLatestPriority() < priority.intValue())
                            appInfo.setLatestPriority(priority);

                        createMessageAndInsert();
                        break;

                    case BrokenPortInfo:
                        Log.e("BrokenPortInfoEnum", header[0]);

                        printWriter.write('A');
                        printWriter.flush();

                        if (appInfo.getBrokenRemotePort().equals("0")) {
                            appInfo.setBrokenRemotePort(header[1]);
                            appInfo.cleanupPriorityQueue();
                            appInfo.popPriorityQueue();
                        } else
                            Log.e("BrokenPortInfo", "Already Removed!");
                        break;

                    default:
                        Log.e("Server", "NO MATCHING CASE!");
                }
                else
                {
                    Log.e(TAG,"Header is Null!");
                }


            } catch (IOException e) {
                Log.e("ServerTask", "Failed to create receiver Socket!");
            }
            count++;
        }

        return null;
    }
}

