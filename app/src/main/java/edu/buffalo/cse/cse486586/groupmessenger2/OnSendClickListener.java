package edu.buffalo.cse.cse486586.groupmessenger2;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


/**
 * Created by Molu on 14/2/17.
 */

class OnSendClickListener implements OnClickListener {

    private final EditText editTextBox;
    private GroupMessenger2 appInfo;

    OnSendClickListener(EditText _et, GroupMessenger2 variablesClass) {

        editTextBox = _et;
        this.appInfo = variablesClass;
    }

    @Override
    public void onClick(View v) {


        String msg = editTextBox.getText().toString() + "\n";
        editTextBox.setText(""); // This is one way to reset the input box.

        MessageContainer messageObject = new MessageContainer(msg);

        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, messageObject);

    }


    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     */
    private class ClientTask extends AsyncTask<MessageContainer, Void, Void> {

        MessageContainer messageToSend;

        void multicastBrokenPortInfo(String brokenPort)
        {
            ArrayList<String> remotePortList = appInfo.getRemotePortList();

            for (String remotePort : remotePortList) {

                if(!(remotePort.equals(brokenPort))) {
                    try {

                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(remotePort));

                        socket.setPerformancePreferences(0, 1, 0);

                        OutputStream outStream = socket.getOutputStream();
                        PrintWriter msgWriter = new PrintWriter(outStream);

                        Log.e("ClientTask","Sending broken port info");
                        msgWriter.println("BrokenPortInfo " + brokenPort);
                        msgWriter.flush();

                        InputStreamReader tempISR = new InputStreamReader(socket.getInputStream(), "UTF-8");
                        BufferedReader buffReader = new BufferedReader(tempISR);

                        char rec_ack;

                        rec_ack = (char) buffReader.read();

                        if (rec_ack == 'A') {
                            socket.close();
                        } else {
                            Log.e("ClientTask Cleanup", "Broken port ack failed!");
                        }

                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        }

        float multicastMsgAndDecideAgreedPriority()
        {
            ArrayList<String> remotePortList = appInfo.getRemotePortList();
            float maxProposedPriority = 0.0f;
            String brokenPort = null;

            if(remotePortList.remove(appInfo.getBrokenRemotePort()))
                Log.e("clienttask","Removed port " + appInfo.getBrokenRemotePort());

            for (String remotePort : remotePortList)
            {
                try {

                    String proposedPrio = unicastMsgAndReceiveProposedPriority(remotePort);

                    if(maxProposedPriority < Float.parseFloat(proposedPrio))
                        maxProposedPriority = Float.parseFloat(proposedPrio);

                } catch (Exception e) {
                    Log.e("ClientTask", "ClientTask socket NullException");
                    brokenPort = remotePort;
                }
            }

            if(brokenPort != null && appInfo.getBrokenRemotePort().equals("0"))
                multicastBrokenPortInfo(brokenPort);

            return maxProposedPriority;
        }

        String unicastMsgAndReceiveProposedPriority(String remotePort) throws IOException {
            int myPort = appInfo.getPortNumber();
            int msgId = appInfo.getMsgId();

            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                    Integer.parseInt(remotePort));

            OutputStream outStream = socket.getOutputStream();
            PrintWriter msgWriter = new PrintWriter(outStream);

            msgWriter.println("FirstTime" + " " + myPort + " " + msgId);
            msgWriter.println(messageToSend.getMsg());
            msgWriter.flush();


            InputStreamReader tempISR = new InputStreamReader(socket.getInputStream(), "UTF-8");
            BufferedReader buffReader = new BufferedReader(tempISR);

            String proposedPrio;

            proposedPrio = buffReader.readLine();

            socket.close();

            return proposedPrio;
        }

        void multicastAgreedPriority() {
            ArrayList<String> remotePortList = appInfo.getRemotePortList();
            int myPort = appInfo.getPortNumber();
            int msgId = appInfo.getMsgId();
            String brokenPort = null;

            if (remotePortList.remove(appInfo.getBrokenRemotePort()))
                Log.e("clienttask", "Removed port " + appInfo.getBrokenRemotePort());

            for (String remotePort : remotePortList) {

                try {

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort));

                    OutputStream outStream = socket.getOutputStream();
                    PrintWriter msgWriter = new PrintWriter(outStream);

                    msgWriter.println("AgreedPriority" + " " + myPort + " " + msgId);
                    msgWriter.println(messageToSend.getPriority());
                    msgWriter.println(messageToSend.getMsg());
                    msgWriter.flush();

                    InputStreamReader tempISR = new InputStreamReader(socket.getInputStream(), "UTF-8");
                    BufferedReader buffReader = new BufferedReader(tempISR);

                    char rec_ack;

                    rec_ack = (char) buffReader.read();

                    if (rec_ack == 'A') {
                        socket.close();
                    } else {
                        Log.e("ClientTask", "ack failed!");
                        brokenPort = remotePort;
                    }

                } catch (Exception e) {
                    Log.e("ClientTask", "ClientTask UnknownHostException");
                    brokenPort = remotePort;
                }
            }
            appInfo.setMsgId(msgId + 1);

            if(brokenPort != null && appInfo.getBrokenRemotePort().equals("0"))
                multicastBrokenPortInfo(brokenPort);

        }


        @Override
        protected Void doInBackground(MessageContainer... msgs) {
            
            messageToSend = msgs[0];

            float agreedPriority = multicastMsgAndDecideAgreedPriority();

            messageToSend.setPriority(agreedPriority);

            multicastAgreedPriority();

            return null;
        }


    }
}


