package ing.rbi.poc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import emc.captiva.mobile.sdk.CaptureImage;
import emc.captiva.mobile.sdk.PictureCallback;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AddBill extends Activity implements PictureCallback {
private String URI;
private String ticket;
private String BatchID;
private String BillValue;
private String DocReference;

private static String TAG = AddBill.class.getSimpleName();
static boolean _newLoad = true;
private final int CHOOSE_IMAGE = 1;
private final int ENH_OPER = 2;

private String FileName = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(ing.rbi.poc.R.layout.activity_add_bill);
		Intent intent = getIntent();
		URI = intent.getStringExtra("URI");
		ticket = intent.getStringExtra("ticket");
		BatchID = intent.getStringExtra("BatchID");
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(ing.rbi.poc.R.menu.add_bill, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ing.rbi.poc.R.id.action_settings:
			// Launch the preference settings activity.
			Intent intent = new Intent(this, SettingsActivity.class);
	        startActivity(intent);
	        return true;
		default:
	        return super.onOptionsItemSelected(item);
		}
	}
	
	
	public void onTakePicture(View view) {
        // Obtain our picture parameters from the preferences.
		TextView bValue = (TextView) findViewById(ing.rbi.poc.R.id.BillValue);
		BillValue = bValue.getText().toString();
		TextView bRef = (TextView) findViewById(ing.rbi.poc.R.id.BillReference);
		DocReference = bRef.getText().toString();
		//Check to see if the value is valid
		if (isNumeric(BillValue)) {
			 HashMap<String, Object> parameters = CoreHelper.getTakePictureParametersFromPrefs(this);
			//Check to see if there's a reference
				if (DocReference == "") {
					ShowDLG("Invalid Reference", "Document Reference Required");
				}
				else {
					 // Launch the camera to take a picture.
			        CaptureImage.takePicture(this, parameters);	
				}
		}
		else {
			ShowDLG("Invalid Amount", "Valid Amount Required");
		}
    }
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}

	@Override
	public void onPictureCanceled(int reason) {
		// TODO Auto-generated method stub
		 // This callback will be called if the take picture operation was canceled. 
        if (reason == PictureCallback.REASON_OPTIMAL_CONDITIONS) {
            CoreHelper.displayError(this, "The optimal conditions were not met and the picture was canceled.");
        } else if (reason == PictureCallback.REASON_CAMERA_ERROR) {
        	CoreHelper.displayError(this, "An error occurred while accessing the camera.");
        }
	}

	@Override
	public void onPictureTaken(byte[] imageData) {
		// TODO Auto-generated method stub
		 // Use our utility functions to obtain a unique filename to store into the image gallery.
        File fullpath = new File(CoreHelper.getImageGalleryPath(), CoreHelper.getUniqueFilename("Img", ".JPG"));
        try {
            // Use our utility function to save this JPG encoded byte array to storage.
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);       
            CoreHelper.saveFile(inputStream, fullpath);
            
            // Get a URI to broadcast and let Android know there is a new image in the gallery.
            Uri uri = Uri.fromFile(fullpath);
            //This is a security issue in KitKat
            //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, uri));
            galleryAddPic(fullpath.toString());
            // Send the new picture taken to the enhancement screen so that users can modify it if necessary.
            gotoEnhanceImage(uri);
        } 
        catch (IOException e) {
            // Log a message and display an error using our utility function.
            Log.e(TAG, e.getMessage(), e);            
            CoreHelper.displayError(this, "Could not save the image to the gallery.");
        }
	}
	private void galleryAddPic(String mCurrentPhotoPath) {
	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	    File f = new File(mCurrentPhotoPath);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    this.sendBroadcast(mediaScanIntent);
	}
	
	private void gotoEnhanceImage(Uri uri) {
		 // If we have a file, then send it to enhancement.
    	if (uri != null) {    
    	    // Send the file path to enhancement so that it can load in the screen.
            String filepath = CoreHelper.getFilePathFromContentUri(this, uri);
            FileName = filepath;
            Intent intent = new Intent(this, EnhanceImageActivity.class);
         
            intent.putExtra("Filename", filepath);
            _newLoad = true;
            startActivityForResult(intent, ENH_OPER); 
            //Clear the Intent
            intent.setAction("");
        }
	}
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	// Handle results for activities launched by this activity.
        try {            
            if(requestCode == CHOOSE_IMAGE && data != null && data.getData() != null) {         
                // The user picked an image from the gallery.                
                // Send the new picture taken to the enhancement screen so that users can modify it if necessary.
                Uri uri = data.getData();
                gotoEnhanceImage(uri);
            }
            else if (requestCode == ENH_OPER) {                
                // The enhancement screen has finished. So, let Android know that a change was made 
                // to the gallery to ensure it gets refreshed.
                //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
            	galleryAddPic("file://" + Environment.getExternalStorageDirectory());
               FileName = data.getStringExtra("SavedFile");
                
                //Now update the batch
               setContentView(ing.rbi.poc.R.layout.activity_add_bill);
               //Show the progress bar
               ProgressBar pBar = (ProgressBar) findViewById(ing.rbi.poc.R.id.bill_progressBar);
		        pBar.setVisibility(0);
                BatchUpdate();
               
            }
        }
        catch (Exception e) {
            // Log a message and display the error to the user using our utility function.
            Log.e(TAG, e.getMessage(), e);
            CoreHelper.displayError(this, e);
        }
    }
	private void BatchUpdate () {
		UpdateBatch Batch = new UpdateBatch(this);
		Batch.BatchID = BatchID;
		Batch.ticket = ticket;
		Batch.URI = URI;
		Batch.FileName = FileName;
		//Batch.chequeValue = Double.parseDouble(ChequeValue);
		Batch.docValue = BillValue;
		Batch.docReference = DocReference;
		//Get the preferences
		SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this);
		Batch.DefaultCustomerName = gprefs.getString("Default Customer Name", "");
		Batch.DefaultAccountName = gprefs.getString("Default Account Name", ""); 
		if (Batch.DefaultAccountName == "" || Batch.DefaultCustomerName == "" ) {
			ShowDLG("Default Account Settings not Set", "Check Preferences");
		}
		else {
			ProgressDialog dialog = new ProgressDialog(this);
		    dialog.setMessage("Please wait...");
		    dialog.show();
			Batch.execute();
			while (!Batch.Completed == true){
				try{Thread.sleep(100);}
				catch (InterruptedException e) { e.printStackTrace(); }
			}
			if (Batch.BatchUpdated = true) {
				//Now wait for the results
				GetBillResults BResults = new GetBillResults(this);
				BResults.BatchID = BatchID;
				BResults.execute();
				while (!BResults.Completed == true){
					try{Thread.sleep(100);}
					catch (InterruptedException e) { e.printStackTrace(); }
				}
				//Get the Values back
				String CustomerAccount = BResults.CustomerAccount;
				String CreditAccount = BResults.CreditAccount;
				String AmountDue = BResults.AmountDue ;
				String AmountPaid = BResults.AmountPaid;
				String Currency = BResults.Currency;
				Intent intent = new Intent(this,BillResults.class);
				intent.putExtra("CustomerAccount", CustomerAccount);
				intent.putExtra("CreditAccount", CreditAccount);
				intent.putExtra("AmountDue", AmountDue);
				intent.putExtra("AmountPaid", AmountPaid);
				intent.putExtra("Currency", Currency);
				startActivity(intent);
				ProgressBar pBar = (ProgressBar) findViewById(ing.rbi.poc.R.id.bill_progressBar);
		        pBar.setVisibility(1);
			}
			else {
				ShowDLG("Batch Not Updated", "Unable to Add Bill");
				//Now go back to the select CF screen
				//super.onBackPressed();
			}
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
