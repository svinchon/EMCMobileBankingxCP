package ing.rbi.poc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tejas on 26/04/15.
 */
public class ProofIDHandler implements MediaSelectedHandler{
    private boolean isNewLoadForImage;
    private static ProofIDHandler myInstance;
    private String FLOW_NAME;
    private MediaButtonClickHandler mediaButtonClickHandler;
    private WeakReference<country_list> createFragmentWeakReference;
    private GetPIDResults myPidResults;
    private ProgressDialog progressDialog;
    private boolean batchUpdateInProgress;




    public static ProofIDHandler getProofIDHandler(String flowName){
        if(flowName != null){
            myInstance = new ProofIDHandler(flowName);
        }
        return myInstance;
    }

    private ProofIDHandler(String FLOW_NAME) {
        super();
        this.FLOW_NAME = FLOW_NAME;
    }

    private ProofIDHandler() {
        super();
    }
    private MediaButtonClickHandler getMediaHandler() {
        return mediaButtonClickHandler;
    }


    public void handleProofID(Activity activity, country_list fragment){
        Activity context = activity;
        createFragmentWeakReference = new WeakReference<country_list>(fragment);
        LayoutInflater layoutInflater = context.getLayoutInflater();
        fragment.setMediaSelectHandler(this);
        fragment.addToIntentData(Constants.SELECTED_FLOW, FLOW_NAME);


        View view = layoutInflater.inflate(ing.rbi.poc.R.layout.add_proof_id, null);
        LinearLayout layout_level1 = (LinearLayout) context.findViewById(ing.rbi.poc.R.id.fragment_clist_dynamic_layout_level1);
        LinearLayout layout_level2 = (LinearLayout) context.findViewById(ing.rbi.poc.R.id.fragment_clist_dynamic_layout_level2);
        View camera = view.findViewById(ing.rbi.poc.R.id.pid_camera_btn);
        View gallery = view.findViewById(ing.rbi.poc.R.id.pid_gallery_btn);
        mediaButtonClickHandler =new MediaButtonClickHandler(context, this, fragment, Constants.FLOW_PROOF_ID);
        camera.setOnClickListener(mediaButtonClickHandler);
        gallery.setOnClickListener(mediaButtonClickHandler);

        //remove other views
        layout_level1.removeAllViews();
        layout_level2.removeAllViews();

        //add view
        layout_level1.addView(view);


    }

    @Override
    public boolean isNewLoadForImage() {
        return isNewLoadForImage;
    }

    @Override
    public void setNewLoadForImage(boolean shouldLoadNew) {
            isNewLoadForImage = shouldLoadNew;
    }

    @Override
    public MediaButtonClickHandler getMediaButtonClickHandler() {
        return getMediaHandler();
    }


    public void BatchUpdate(String filePath, final Activity currentActivity) {
        BatchUtility.getBatchDetails(createFragmentWeakReference.get());
        final Activity activity = createFragmentWeakReference.get().getActivity();

        UpdateBatch Batch = new UpdateBatch(activity);
        Batch.BatchID = createFragmentWeakReference.get().getFromIntentData(Constants.BATCH_ID);
        Batch.ticket = createFragmentWeakReference.get().getFromIntentData(Constants.TICKET);;
        Batch.URI = createFragmentWeakReference.get().getFromIntentData(Constants.URI);
        if(filePath == null) {
            Batch.FileName = createFragmentWeakReference.get().getFromIntentData(Constants.FILE_NAME);
        }
        Batch.FileName = filePath;
        //Get the preferences
        SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(activity);
        Batch.DefaultCustomerName = gprefs.getString("Default Customer Name", "");
        Batch.DefaultAccountName = gprefs.getString("Default Account Name", "");
        if (Batch.DefaultAccountName == "" || Batch.DefaultCustomerName == "" ) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertUtility.ShowAlertDialog("Default Account Settings not Set", "Check Preferences", currentActivity);
                }
            });
        }
        else {
            Batch.execute();
            while (!Batch.Completed == true){
                try{Thread.sleep(100);}
                catch (InterruptedException e) { e.printStackTrace(); }
            }
            if (Batch.BatchUpdated = true) {
                //Now wait for the results
                GetPIDResults PIDResults = new GetPIDResults(activity);
                PIDResults.BatchID = createFragmentWeakReference.get().getFromIntentData(Constants.BATCH_ID);;
                PIDResults.execute();
                while (!PIDResults.Completed == true){
                    try{Thread.sleep(100);}
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
                //Get the Values back
                myPidResults = PIDResults;
                //setupIDProofDetailsUI(PIDResults);
            }
            else {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertUtility.ShowAlertDialog("Batch Not Updated", "Unable to Add ID", currentActivity);
                    }
                });

                //Now go back to the select CF screen
                //super.onBackPressed();
            }
            batchUpdateInProgress = false;
        }
    }

    @Override
    public void handlePostEnhanceImage(final int enhanceResultCode, final String filePath, final Activity currentActivity) {
        showProgress(currentActivity);
        if(enhanceResultCode == Constants.EVENT_ENHANCE_OPERATION_ENHANCE_YES) {
            getMediaButtonClickHandler().galleryAddPic(filePath);
        }
        batchUpdateInProgress = true;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                BatchUpdate(filePath, currentActivity);
                while(batchUpdateInProgress){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                createFragmentWeakReference.get().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                        ((EnhanceImageActivity)currentActivity).returnIntentResult(enhanceResultCode, filePath);
                    }
                });
            }
        });
        t.start();

    }

    private void showProgress(Activity activity) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        progressDialog.getCurrentFocus();
    }

    private void hideProgress() {
        if(progressDialog != null && progressDialog.isShowing())
        progressDialog.dismiss();
    }

    @Override
    public void setupUIAfterEnhanceImage() {
        setupIDProofDetailsUI(myPidResults);
    }

    @Override
    public boolean ifBatchUpdateInProgress() {
        return batchUpdateInProgress;
    }

    private void setupIDProofDetailsUI(GetPIDResults PIDResults){
        LayoutInflater layoutInflater = createFragmentWeakReference.get().getActivity().getLayoutInflater();
        final View proof_details_view = layoutInflater.inflate(ing.rbi.poc.R.layout.details_proof_id,null);
        LinearLayout layout_level_2 = (LinearLayout) createFragmentWeakReference.get().getActivity().findViewById(ing.rbi.poc.R.id.fragment_clist_dynamic_layout_level2);
        addImageThumbnail();


        String address = PIDResults.Address;
        String dOB = PIDResults.DOB;
        String documentNumber = PIDResults.DocumentNumber ;
        String documentType = PIDResults.DocumentType;
        String expirationDate = PIDResults.ExpirationDate;
        String forename = PIDResults.Forename;
        String surname = PIDResults.Surname;

        //EditText addressText = (EditText) proof_details_view.findViewById(ing.rbi.poc.R.id.txt_address);
        //addressText.setText(address);

        EditText dOBText = (EditText) proof_details_view.findViewById(ing.rbi.poc.R.id.txt_Invoice_Amount);
        dOBText.setText(dOB);

        EditText documentNumberText = (EditText) proof_details_view.findViewById(ing.rbi.poc.R.id.txt_docnumber);
        documentNumberText.setText(documentNumber);

        EditText documentTypeText = (EditText) proof_details_view.findViewById(ing.rbi.poc.R.id.txt_doctype);
        documentTypeText.setText(documentType);

        EditText expirationDateText = (EditText) proof_details_view.findViewById(ing.rbi.poc.R.id.txt_expdate);
        expirationDateText.setText(expirationDate);

        EditText forenameText = (EditText) proof_details_view.findViewById(ing.rbi.poc.R.id.txt_Invoice_Date);
        forenameText.setText(forename);

        EditText surnameText = (EditText) proof_details_view.findViewById(ing.rbi.poc.R.id.txt_Invoice_Number);
        surnameText.setText(surname);

//        Button acceptButton = (Button) proof_details_view.findViewById(ing.rbi.poc.R.id.btn_proof_id_accept);
//        acceptButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                createProofOfIDInServer(proof_details_view);
//            }
//        });

        Button cancelButton = (Button) proof_details_view.findViewById(ing.rbi.poc.R.id.btn_proof_id_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelProofSubmission();
            }
        });
        layout_level_2.addView(proof_details_view);

    }

    private void addImageThumbnail() {
        LinearLayout layout_level_1 = (LinearLayout) createFragmentWeakReference.get().getActivity().findViewById(ing.rbi.poc.R.id.fragment_clist_dynamic_layout_level1);
        layout_level_1.removeAllViews();

        LayoutInflater layoutInflater = createFragmentWeakReference.get().getActivity().getLayoutInflater();
        ImageView preview_image_view = (ImageView) layoutInflater.inflate(ing.rbi.poc.R.layout.preview_image,null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(500,500);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        String filePath = createFragmentWeakReference.get().getFromIntentData(Constants.FILE_NAME);
        File file = new File(filePath);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        preview_image_view.setImageBitmap(bitmap);
        preview_image_view.setLayoutParams(layoutParams);
        layout_level_1.addView(preview_image_view);

    }

    private void cancelProofSubmission() {
        createFragmentWeakReference.get().cancelAllFlows();
    }

    public void createProofOfIDInServer(View View) {
        View view = createFragmentWeakReference.get().getActivity().findViewById(ing.rbi.poc.R.id.fragment_clist_dynamic_layout_level2);
        SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(createFragmentWeakReference.get().getActivity());
        //Send the updated information to xCP
        EditText SN = (EditText) view.findViewById(ing.rbi.poc.R.id.txt_Invoice_Number);
        EditText FN = (EditText) view.findViewById(ing.rbi.poc.R.id.txt_Invoice_Date);
        EditText DB = (EditText) view.findViewById(ing.rbi.poc.R.id.txt_Invoice_Amount);
        EditText DT = (EditText) view.findViewById(ing.rbi.poc.R.id.txt_doctype);
        EditText ED = (EditText) view.findViewById(ing.rbi.poc.R.id.txt_expdate);
        EditText DN = (EditText) view.findViewById(ing.rbi.poc.R.id.txt_docnumber);
        EditText AD = (EditText) view.findViewById(ing.rbi.poc.R.id.txt_address);
        String xCPURI = "";
        xCPURI = gprefs.getString("xCP BPS URI Address", "");
        xCPURI = xCPURI + "/bps/http/Update_PID?";
        //Now add all of the variables
        String tBatchID = "";
        Integer pos = 0;
        String BatchID = createFragmentWeakReference.get().getFromIntentData(Constants.BATCH_ID);
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
        String Address = "&address=" + AD.getText().toString().replace(" ","%20");
        Address = Address.replace("?", "");
        Address = Address.replaceAll("[\n\r]", "%20");
        //This resets all of the batch options and goes back to the upload batch create screen
        xCPURI = xCPURI + CaptivaReference + Surname + Forename + dob + ExpDate + DocType + DocNumber + Address;
        // Now post the response
        UpdatexCP update = new UpdatexCP(createFragmentWeakReference.get().getActivity());
        update.UpdateURI = xCPURI;
        update.execute();
        while (!update.Completed == true){
            try{Thread.sleep(100);}
            catch (InterruptedException e) { e.printStackTrace(); }
        }

        //Now go back to the main screen
            AppConnectionUtility.loginToStartScreen(createFragmentWeakReference.get().getActivity(), null, null);
    }

}
