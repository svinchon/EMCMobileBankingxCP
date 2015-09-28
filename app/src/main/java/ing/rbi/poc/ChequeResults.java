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

public class ChequeResults extends Activity {
	String[] LoginCaptureFlows = null;
	String PrefURI = "";
	String BatchID = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Populate the field results
		Intent intent = getIntent();
		String ChequeReference;
		String AccountNumber; 
		String ChequeAmount;
		String ChequeDate;
		String ChequeNumber;
		String SortCode;
		BatchID = intent.getStringExtra("BatchID");
		ChequeReference = intent.getStringExtra("ChequeReference");
		AccountNumber = intent.getStringExtra("AccountNumber");
		ChequeAmount = intent.getStringExtra("ChequeAmount");
		ChequeDate = intent.getStringExtra("ChequeDate");
		ChequeNumber = intent.getStringExtra("ChequeNumber");
		SortCode = intent.getStringExtra("SortCode");
		setContentView(ing.rbi.poc.R.layout.activity_cheque_results);
		
		EditText CR = (EditText) findViewById(ing.rbi.poc.R.id.txt_Cheque_Reference);
		EditText CA = (EditText) findViewById(ing.rbi.poc.R.id.txt_ChequeAmount);
		EditText CD = (EditText) findViewById(ing.rbi.poc.R.id.txt_chequedate);
		EditText CN = (EditText) findViewById(ing.rbi.poc.R.id.txt_ChequeNumber);
		EditText BC = (EditText) findViewById(ing.rbi.poc.R.id.txt_BranchCode);
		EditText AN = (EditText) findViewById(ing.rbi.poc.R.id.txt_AccountNumber);
		
		CR.setText(ChequeReference);
		CD.setText(ChequeDate);
		CA.setText(ChequeAmount);
		CN.setText(ChequeNumber);
		BC.setText(SortCode);
		AN.setText(AccountNumber);
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(ing.rbi.poc.R.menu.cheque_results, menu);
		return true;
	}
	public void onAccept(View view) {
		SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this);
		EditText CR = (EditText) findViewById(ing.rbi.poc.R.id.txt_Cheque_Reference);
		EditText CA = (EditText) findViewById(ing.rbi.poc.R.id.txt_ChequeAmount);
		EditText CD = (EditText) findViewById(ing.rbi.poc.R.id.txt_chequedate);
		EditText CN = (EditText) findViewById(ing.rbi.poc.R.id.txt_ChequeNumber);
		EditText BC = (EditText) findViewById(ing.rbi.poc.R.id.txt_BranchCode);
		EditText AN = (EditText) findViewById(ing.rbi.poc.R.id.txt_AccountNumber);
		
		String xCPURI = "";
		xCPURI = gprefs.getString("xCP BPS URI Address", "");
		xCPURI = xCPURI + "/bps/http/Update_Cheque?";
		
		//Now add all of the variables
		
		String tBatchID = "";
		Integer pos = 0;
		pos = BatchID.lastIndexOf("/") + 1;
		
		tBatchID = BatchID.substring(pos);
		String CaptivaReference = "captiva_reference=" + tBatchID;
		//Sortcode
		String sortcode = "";
		sortcode = "&sortcode=" + BC.getText().toString();
		sortcode = sortcode.replace(" ", "%20");
		sortcode = sortcode.replace("?", "%20");
		
		String account_number = "&account_number=" + AN.getText().toString();
		account_number = account_number.replace(" ", "%20");
		account_number = account_number.replace("?", "");
		
		String cheque_amount = "";
		cheque_amount = "&cheque_amount=" + CA.getText().toString();
		cheque_amount = cheque_amount.replace(" ","%20");
		cheque_amount = cheque_amount.replace("?","%20");
		
		String cheque_number = "";
		cheque_number = "&cheque_number=" + CN.getText().toString();
		cheque_number = cheque_number.replace(" ","%20");
		cheque_number = cheque_number.replace("?","%20");
		
		String cheque_reference = "";
		cheque_reference = "&cheque_reference=" + CR.getText().toString();
		cheque_reference = cheque_reference.replace(" ", "%20");
		cheque_reference = cheque_reference.replace("?", "%20");
		
		String cheque_date = "";
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		Date tdate = null;
		try {
			tdate = new SimpleDateFormat("dd MMMM yyyy").parse(CD.getText().toString());
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (tdate == null) {
			
		}
		else {
			cheque_date = "&cheque_date=" + df.format(tdate);
		}
		
		//This resets all of the batch options and goes back to the upload batch create screen
		xCPURI = xCPURI + CaptivaReference + sortcode + account_number + cheque_amount + cheque_number + cheque_reference + cheque_date;
		// Now post the response
		UpdatexCP update = new UpdatexCP(this);
		update.UpdateURI = xCPURI;
		update.execute();
		while (!update.Completed == true){
			try{Thread.sleep(100);}
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		//Go back to the batch select screen
		
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

