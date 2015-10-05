package ing.rbi.poc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * Created by tejas on 27/04/15.
 */
public class AppConnectionUtility {

    public static void loginToStartScreen(Activity activity, String user, String password) {
        SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String PrefURI = gprefs.getString("URI Address", "");
        String ResURI;
        ResURI = gprefs.getString("Results URI Address", "");
        if(user == null){
            user = gprefs.getString("User", "");
        }
        if(password == null){
            password = gprefs.getString("Password", "");
        }
        //Now make the Connection
        //Create a connection object
        Context context;
        ResolveConnection connection = new ResolveConnection(activity);
        connection.uri = PrefURI;
        connection.user = user;
        connection.password = password;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences( activity ).edit();
        editor.putString( "User", user );
        editor.putString( "Password", password);
        editor.commit();

        if (!PrefURI.equals("") & !ResURI.equals("")) {
            connection.execute();
            //Wait for the task to complete
            while (!connection.Completed){
                try{Thread.sleep(100);}
                catch (InterruptedException e) { e.printStackTrace(); }
            }

            //If the connection is OK then go to the Batch Create screen
            if (connection.ConnectOK) {
                //show the batch create screen
                //Intent intent = new Intent(this,CreateBatch.class);
                Intent intent = new Intent(activity,CreateBatchTabbed.class);
                String[] LoginCaptureFlows = connection.CaptureFlows;
                Bundle b = new Bundle();
                b.putStringArray(activity.getString(ing.rbi.poc.R.string.capture_flow_types), LoginCaptureFlows);
                intent.putExtras(b);
                //pass the ticket
                intent.putExtra("ticket", connection.ticket);
                //pass the URI
                intent.putExtra("URI", PrefURI);
                activity.startActivity(intent);
            }
            else {
                String ErrMessage = connection.StrResponse;

                //Create a dialogue
                //Display a message saying that a flow needs to be selected
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage(ing.rbi.poc.R.string.connection_error)
                        .setTitle(ErrMessage);
                builder.setPositiveButton(ing.rbi.poc.R.string.OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });
                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
        else {
            AlertUtility.ShowAlertDialog("No URI Specified", "Check Settings", activity);
        }
    }
}
