package ing.rbi.poc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

public class PIDResults extends Activity {
	String[] LoginCaptureFlows = null;
	String PrefURI = "";
	String BatchID = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(ing.rbi.poc.R.layout.activity_pidresults);
		//Get the values back
		Intent intent = getIntent();
		BatchID = intent.getStringExtra("BatchID");
		String Address = intent.getStringExtra("Address");
		String DOB = intent.getStringExtra("DOB");
		String DocumentNumber = intent.getStringExtra("DocumentNumber");
		String DocumentType = intent.getStringExtra("DocumentType");
		String ExpirationDate = intent.getStringExtra("ExpirationDate");
		String Forename = intent.getStringExtra("Forename");
		String Surname = intent.getStringExtra("Surname");
		
		//Send the values to the UI
		EditText SN = (EditText) findViewById(ing.rbi.poc.R.id.txt_Invoice_Number);
		EditText FN = (EditText) findViewById(ing.rbi.poc.R.id.txt_Invoice_Date);
		EditText DB = (EditText) findViewById(ing.rbi.poc.R.id.txt_Invoice_Amount);
		EditText DT = (EditText) findViewById(ing.rbi.poc.R.id.txt_doctype);
		EditText ED = (EditText) findViewById(ing.rbi.poc.R.id.txt_expdate);
		EditText DN = (EditText) findViewById(ing.rbi.poc.R.id.txt_docnumber);
		//EditText AD = (EditText) findViewById(ing.rbi.poc.R.id.txt_address);
		
		SN.setText(Surname);
		FN.setText(Forename);
		DB.setText(DOB);
		DT.setText(DocumentType);
		ED.setText(ExpirationDate);
		DN.setText(DocumentNumber);
		//AD.setText(Address);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(ing.rbi.poc.R.menu.pidresults, menu);
		return true;
	}
	
	public void sendAccept(View View) {
		SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this);
		//Send the updated information to xCP
		EditText SN = (EditText) findViewById(ing.rbi.poc.R.id.txt_Invoice_Number);
		EditText FN = (EditText) findViewById(ing.rbi.poc.R.id.txt_Invoice_Date);
		EditText DB = (EditText) findViewById(ing.rbi.poc.R.id.txt_Invoice_Amount);
		EditText DT = (EditText) findViewById(ing.rbi.poc.R.id.txt_doctype);
		EditText ED = (EditText) findViewById(ing.rbi.poc.R.id.txt_expdate);
		EditText DN = (EditText) findViewById(ing.rbi.poc.R.id.txt_docnumber);
		//EditText AD = (EditText) findViewById(ing.rbi.poc.R.id.txt_address);
		String xCPURI = "";
		xCPURI = gprefs.getString("xCP BPS URI Address", "");
		xCPURI = xCPURI + "/bps/http/Update_PID?";
		//Now add all of the variables
		String tBatchID = "";
		Integer pos = 0;
		pos = BatchID.lastIndexOf("/") + 1;
		
		tBatchID = BatchID.substring(pos);
		String CaptivaReference = "captiva_reference=" + tBatchID;
		String Surname = "&surname=" + SN.getText().toString().replace(" ", "%20");
		Surname = Surname.replace("?", "");
		String Forename = "&forename=" + FN.getText().toString().replace(" ", "%20");
		Forename = Forename.replace("?", "");
		String dob = "";
		DB.setText(DB.getText().toString().replace(".", "/"));
		DB.setText(DB.getText().toString().replace("?", ""));
		Date tdate = null;
		Integer tDay = null;
		Integer tMonth = null;
		Integer tYear = null;
		Integer DLen = null;
		String tdString = "";
		tdString = DB.getText().toString();
		tdString = tdString.replace("/", "");
		tdString = tdString.replace(".", "");
		tdString = tdString.replace(".", "");
		DLen = tdString.length();
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		
		try {
			if (DLen == 6) {
				tdate = new SimpleDateFormat("dd/MM/yy").parse(DB.getText().toString());
			}
			else if (DLen == 8) {
				tdate = new SimpleDateFormat("dd/MM/yyyy").parse(DB.getText().toString());
			}
		    
		} catch (ParseException ex) {
		    ex.printStackTrace();
		}
		if (tdate == null) {
		    // Invalid date format
		} else {
		    // Valid date format
			
			dob = "&dob=" + df.format(tdate);
		}
		
		String ExpDate = "";
		ED.setText(ED.getText().toString().replace(".", "/"));
		ED.setText(ED.getText().toString().replace("?", ""));
		tdString = ED.getText().toString();
		tdString = tdString.replace("/", "");
		tdString = tdString.replace(".", "");
		tdString = tdString.replace(".", "");
		tdate = null;
		DLen = tdString.length();
		try {
			if (DLen == 6){
				tdate = new SimpleDateFormat("dd/MM/yy").parse(ED.getText().toString());
			}
			else if (DLen == 8) {
				tdate = new SimpleDateFormat("dd/MM/yyyy").parse(ED.getText().toString());
			}
		    
		} catch (ParseException ex) {
		    ex.printStackTrace();
		}
		if (tdate == null) {
		    // Invalid date format
		} else {
		    // Valid date format
			
			ExpDate = "&exp_date=" + df.format(tdate);
		}
		
		String DocType = "&document_type=" + DT.getText().toString().replace(" ", "%20");
		DocType = DocType.replace("?", "");
		String DocNumber = "&document_number=" + DN.getText().toString().replace(" ","&20");
		DocNumber = DocNumber.replace("?", "");
		//String Address = "&address=" + AD.getText().toString().replace(" ","%20");
		String Address = "&address=";
		Address = Address.replace("?", "");
		Address = Address.replaceAll("[\n\r]", "%20");
		//This resets all of the batch options and goes back to the upload batch create screen
		xCPURI = xCPURI + CaptivaReference + Surname + Forename + dob + ExpDate + DocType + DocNumber + Address;
		// Now post the response
		UpdatexCP update = new UpdatexCP(this);
		update.UpdateURI = xCPURI;
		update.execute();
		while (!update.Completed == true){
			try{Thread.sleep(100);}
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		//Now go back to the main screen
		
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
				Intent intent = new Intent(this,CreateBatchTabbed.class);
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
