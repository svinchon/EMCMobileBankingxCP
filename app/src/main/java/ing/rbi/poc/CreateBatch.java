package ing.rbi.poc;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import android.widget.AdapterView;

public class CreateBatch extends Activity {
private ListView mylistview;	
private String FlowSelected = "";
private String URI;
private String ticket = "";

public String[] CreateBatchCaptureFlows = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b=this.getIntent().getExtras();
		CreateBatchCaptureFlows=b.getStringArray("CaptureFlows");
		setContentView(ing.rbi.poc.R.layout.activity_create_batch);
		//ListView mylistview;
		ArrayAdapter<String> listAdapter;
		mylistview = (ListView) findViewById(ing.rbi.poc.R.id.CaptureFlowList);
		listAdapter = new ArrayAdapter<String>(CreateBatch.this,android.R.layout.simple_expandable_list_item_1,CreateBatchCaptureFlows);
		mylistview.setAdapter(listAdapter);	
		//Now get the other values from the intent
		Intent intent = getIntent();
		URI = intent.getStringExtra("URI");
		// Set a listener
		mylistview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
				FlowSelected = mylistview.getItemAtPosition(position).toString();
				//Update the message
				TextView Message = (TextView) findViewById(ing.rbi.poc.R.id.textSelectedFlow);
				Message.setText(FlowSelected);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(ing.rbi.poc.R.menu.create_batch, menu);
		return true;
	}
	
	public void sendFindDocuments(View view) {
		//Now Get all of the documentum documents for that account
		QueryCustomerFolder QFolder = new QueryCustomerFolder(this);
		SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this);
		String BaseURI = "";
		BaseURI = gprefs.getString("xCP BPS URI Address", "");
		QFolder.CustomerName = gprefs.getString("Default Customer Name", "");
		QFolder.URI = gprefs.getString("xCP BPS URI Address", "");
		QFolder.execute();
		//Loop until it's completed OK
		while (!QFolder.Completed == true) {
			try{Thread.sleep(100);}
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		Integer ItemCount =0;
		ItemCount = QFolder.ItemCount;
		String[] ItemID = new String[ItemCount];
		String[] ItemName = new String[ItemCount];
		ItemID = QFolder.ItemID;
		ItemName = QFolder.ItemName;
		if (ItemCount != 0) {
			// Show the list of documents
			//Now we need to star another activity to show the list of documents to the user
			Intent intent = new Intent(this,ViewDocuments.class);
			intent.putExtra("ItemID", ItemID);
			intent.putExtra("ItemName", ItemName);
			intent.putExtra("BaseURI", BaseURI);
			startActivity(intent);
		}
		else {
			//Show a message
			ShowDLG("No Documents Found", "No Documents Found");
		}
		
	}
	
	public void sendCreateBatch(View view){
		if (FlowSelected != ""){
			//Create a new Batch
			String BatchID = "";
			String BatchName = "";
			AsyncCreateBatch NewBatch = new AsyncCreateBatch(this);
			NewBatch.ticket = ticket;
			NewBatch.FlowSelected = FlowSelected;
			NewBatch.URI = URI;
			NewBatch.execute();
			//We need to wait for a bit for this to complete
			while (!NewBatch.Completed == true){
				try{Thread.sleep(100);}
				catch (InterruptedException e) { e.printStackTrace(); }
			}
			
			BatchID = NewBatch.BatchID;
			BatchName = NewBatch.BatchName;
			if (BatchID != null) {
				//We now have a batch so we can start taking pictures
				//See which type of process was created - Cheque or Bill
				
				if (FlowSelected.contains("Bill")) {
					//Add a bill
					Intent intent = new Intent(this,AddBill.class);
					intent.putExtra("ticket", ticket);
					intent.putExtra("URI", URI);
					intent.putExtra("BatchID", BatchID);
					startActivity(intent);
				}
				else if (FlowSelected.contains("ID")){
					//Proof of ID
					Intent intent = new Intent(this,AddPID.class);
					intent.putExtra("ticket", ticket);
					intent.putExtra("URI", URI);
					intent.putExtra("BatchID", BatchID);
					startActivity(intent);
				}
				else {
					//Add a cheque
					Intent intent = new Intent(this,AddCheque.class);
					intent.putExtra("ticket", ticket);
					intent.putExtra("URI", URI);
					intent.putExtra("BatchID", BatchID);
					startActivity(intent);
				}
			}
			else{
				//There was an error
				ShowDLG(NewBatch.dlgTitle, NewBatch.dlgMessage);
			}
		}
		else {
			ShowDLG("Select Capture Flow","No Capture Flow Selected");
			
		}
	}
	
	private void ShowDLG(String Title, String msg) {
		//Display a message saying that a flow needs to be selected
		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		// 2. Chain together various setter methods to set the dialog characteristics
		builder.setMessage(msg)
		       .setTitle(Title);
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
