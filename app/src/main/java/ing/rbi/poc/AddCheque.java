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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AddCheque extends Activity implements PictureCallback {
    private String URI;
    private String ticket;
    private String BatchID;
    private String ChequeReference;
    private static String TAG = AddCheque.class.getSimpleName();
    static boolean _newLoad = true;
    private final int CHOOSE_IMAGE = 1;
    private final int ENH_OPER = 2;
    private final int ADD_Second_Page = 3;
    private String FileName = null;
    private String FileName1 = null;
    private String FileName2 = null;
    private Boolean FrontTaken = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ing.rbi.poc.R.layout.activity_add_cheque);
        Intent intent = getIntent();
        URI = intent.getStringExtra("URI");
        ticket = intent.getStringExtra("ticket");
        BatchID = intent.getStringExtra("BatchID");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(ing.rbi.poc.R.menu.add_cheque, menu);
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
        TextView cRef = (TextView) findViewById(ing.rbi.poc.R.id.txt_input_cheque_reference);
        ChequeReference = cRef.getText().toString();
        //Check to see if the value is valid
        if (!ChequeReference.isEmpty()) {
            HashMap<String, Object> parameters = CoreHelper.getTakePictureParametersFromPrefs(this);
            // Launch the camera to take a picture.
            // TODO cheque take picture
            CaptureImage.takePicture(this, parameters);
            //Show the progress bar
            ProgressBar pBar = (ProgressBar) findViewById(ing.rbi.poc.R.id.cheque_progressBar);
            pBar.setVisibility(1);
            pBar.bringToFront();
        } else {
            ShowDLG("Invalid Reference", "Cheque Reference Required");
        }
    }

    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
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
            //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, uri));
            galleryAddPic(fullpath.toString());
            // Send the new picture taken to the enhancement screen so that users can modify it if necessary.
            gotoEnhanceImage(uri);
        } catch (IOException e) {
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
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle results for activities launched by this activity.
        try {
            if (requestCode == CHOOSE_IMAGE && data != null && data.getData() != null) {
                // The user picked an image from the gallery.                
                // Send the new picture taken to the enhancement screen so that users can modify it if necessary.
                Uri uri = data.getData();
                gotoEnhanceImage(uri);
            } else if (requestCode == ENH_OPER) {
                // The enhancement screen has finished. So, let Android know that a change was made 
                // to the gallery to ensure it gets refreshed.
                //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
                galleryAddPic("file://" + Environment.getExternalStorageDirectory());
                if (FrontTaken == false) {
                    // Now Select the 2nd picture
                    //Show the 2nd prompt
                    FileName1 = data.getStringExtra("SavedFile");
                    Intent intent = new Intent(this, AddBackOfCheque.class);
                    startActivityForResult(intent, ADD_Second_Page);
                } else {
                    //Now update the batch
                    FileName2 = data.getStringExtra("SavedFile");
                    BatchUpdate(this);
                }

            } else if (requestCode == ADD_Second_Page) {
                FrontTaken = true;
                if (resultCode == 1) {
                    onTakePicture(findViewById(ing.rbi.poc.R.id.btn_take_back_picture));
                } else {
                    setContentView(ing.rbi.poc.R.layout.activity_add_cheque);
                    BatchUpdate(this);
                }
            }
        } catch (Exception e) {
            // Log a message and display the error to the user using our utility function.
            Log.e(TAG, e.getMessage(), e);
            CoreHelper.displayError(this, e);
        }
    }

    private void BatchUpdate(Context context) {
        setContentView(ing.rbi.poc.R.layout.activity_add_cheque);
        UpdateBatch Batch = new UpdateBatch(this);
        Batch.BatchID = BatchID;
        Batch.ticket = ticket;
        Batch.URI = URI;
        Batch.FileName = FileName1;
        if (FileName2 != null) {
            Batch.FileName2 = FileName2;
        }
        Batch.docReference = ChequeReference;
        //Get the preferences
        SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this);
        Batch.DefaultCustomerName = gprefs.getString("Default Customer Name", "");
        Batch.DefaultAccountName = gprefs.getString("Default Account Name", "");
        if (Batch.DefaultAccountName == "" || Batch.DefaultCustomerName == "") {
            ShowDLG("Default Account Settings not Set", "Check Preferences");
        } else {
            Batch.execute();
            while (!Batch.Completed == true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (Batch.BatchUpdated = true) {
                //Now wait for the results
                GetChequeResults CResults = new GetChequeResults(this);
                CResults.BatchID = BatchID;
                CResults.execute();
                while (!CResults.Completed == true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                String AccountNumber = CResults.AccountNumber;
                String ChequeAmount = CResults.ChequeAmount;
                String ChequeDate = CResults.ChequeDate;
                String ChequeNumber = CResults.ChequeNumber;
                String SortCode = CResults.SortCode;
                ProgressBar pBar = (ProgressBar) findViewById(ing.rbi.poc.R.id.cheque_progressBar);
                pBar.setVisibility(0);
                Intent intent = new Intent(this, ChequeResults.class);
                intent.putExtra("BatchID", BatchID);
                intent.putExtra("ChequeReference", ChequeReference);
                intent.putExtra("AccountNumber", AccountNumber);
                intent.putExtra("ChequeAmount", ChequeAmount);
                intent.putExtra("ChequeDate", ChequeDate);
                intent.putExtra("ChequeNumber", ChequeNumber);
                intent.putExtra("SortCode", SortCode);
                startActivity(intent);
            } else {
                ShowDLG("Batch Not Updated", "Unable to Add Cheque");
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
