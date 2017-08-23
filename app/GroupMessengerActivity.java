package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

//, "11116", "11120", "11124"
        ArrayList<String> remotePortList = new ArrayList<String>(Arrays.asList("11108", "11112", "11116", "11120", "11124"));

        int processPriority = remotePortList.indexOf(myPort);

        GroupMessenger2 variablesObject = (GroupMessenger2)this.getApplication();

        variablesObject.setPortNumber(Integer.parseInt(myPort));
        variablesObject.setThisProcessPriority(processPriority);
        variablesObject.setLatestPriority(0.0f);
        variablesObject.setRemotePortList(remotePortList);
        variablesObject.setMessagesDelivered(0);
        variablesObject.setContentResolver(getContentResolver());

        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(10000);
            new ServerTask(variablesObject).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final EditText editText = (EditText) findViewById(R.id.editText1);

        findViewById(R.id.button4).setOnClickListener(
                new OnSendClickListener(editText, variablesObject));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
}

/*References:
        https://developer.android.com/index.html
        https://docs.oracle.com/javase/7/docs/api/java/util/PriorityQueue.html
*/
