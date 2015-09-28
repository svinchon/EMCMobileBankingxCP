package ing.rbi.poc;



import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class BillResults extends Activity {
	String[] LoginCaptureFlows = null;
	String PrefURI = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(ing.rbi.poc.R.layout.activity_bill_results);
		//Populate the String results
		Intent intent = getIntent();
		String CustomerAccount = intent.getStringExtra("CustomerAccount");
		String CreditAccount = intent.getStringExtra("CreditAccount");
		String AmountDue = intent.getStringExtra("AmountDue");
		String AmountPaid = intent.getStringExtra("AmountPaid");
		String Currency = intent.getStringExtra("Currency");
		
		EditText CA = (EditText) findViewById(ing.rbi.poc.R.id.txt_CustomerAccount);
		EditText CR = (EditText) findViewById(ing.rbi.poc.R.id.txt_CreditAccount);
		EditText AD = (EditText) findViewById(ing.rbi.poc.R.id.txt_AmountDue);
		EditText AP = (EditText) findViewById(ing.rbi.poc.R.id.txt_AmountPaid);
		EditText CU = (EditText) findViewById(ing.rbi.poc.R.id.txt_Currency);
		
		CA.setText(CustomerAccount);
		CR.setText(CreditAccount);
		AD.setText(AmountDue);
		AP.setText(AmountPaid);
		CU.setText(Currency);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(ing.rbi.poc.R.menu.bill_results, menu);
		return true;
	}
	public void onAccept(View view) {
		//Go back to the batch select screen
		SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this);
		PrefURI = gprefs.getString("URI Address", "");
		String ResURI;
		ResURI = gprefs.getString("Results URI Address", "");
		String userString = gprefs.getString("User", "");
		String passwordString = gprefs.getString("Password", "");
		//Now make the Connection
		//Create a connection object
		Context context;
		
		
		ResolveConnection connection = new ResolveConnection(this);
		connection.uri = PrefURI;
		connection.user = userString;
		connection.password = passwordString;
		
		if (PrefURI != "" & ResURI !="") {
			connection.execute();
			//Wait for the task to complete
			while (!connection.Completed == true){
				try{Thread.sleep(100);}
				catch (InterruptedException e) { e.printStackTrace(); }
			}
			
			//If the connection is OK then go to the Batch Create screen
			if (connection.ConnectOK == true) {
				//show the batch create screen
				Intent intent = new Intent(this,CreateBatch.class);
				LoginCaptureFlows = connection.CaptureFlows;
				Bundle b = new Bundle();
				b.putStringArray("CaptureFlows", LoginCaptureFlows);
				intent.putExtras(b);
				//pass the ticket
				intent.putExtra("ticket", connection.ticket);
				//pass the URI
				intent.putExtra("URI", PrefURI);
				startActivity(intent);
			}
			else {
				String ErrMessage = connection.StrResponse;
				
				//Create a dialogue
				//Display a message saying that a flow needs to be selected
				// 1. Instantiate an AlertDialog.Builder with its constructor
				AlertDialog.Builder builder = new AlertDialog.Builder(this);

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
	}

}
