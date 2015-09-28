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
public class InvoiceHandler implements MediaSelectedHandler{
    private boolean isNewLoadForImage;
    private static InvoiceHandler myInstance;
    private String FLOW_NAME;
    private MediaButtonClickHandler mediaButtonClickHandler;
    private WeakReference<country_list> createFragmentWeakReference;
    private GetInvoiceResults myInvoiceResults;
    private ProgressDialog progressDialog;
    private boolean batchUpdateInProgress;

    public static InvoiceHandler getInvoiceHandler(String flowName){
        if(flowName != null){
            myInstance = new InvoiceHandler(flowName);
        }
        return myInstance;
    }

    private InvoiceHandler(String FLOW_NAME) {
        super();
        this.FLOW_NAME = FLOW_NAME;
    }

    private InvoiceHandler() {
        super();
    }
    private MediaButtonClickHandler getMediaHandler() {
        return mediaButtonClickHandler;
    }


    public void handleInvoice(Activity activity, country_list fragment){
        Activity context = activity;
        createFragmentWeakReference = new WeakReference<country_list>(fragment);
        LayoutInflater layoutInflater = context.getLayoutInflater();
        fragment.setMediaSelectHandler(this);
        fragment.addToIntentData(Constants.SELECTED_FLOW, FLOW_NAME);


        View view = layoutInflater.inflate(R.layout.add_proof_id, null);
        LinearLayout layout_level1 = (LinearLayout) context.findViewById(R.id.fragment_clist_dynamic_layout_level1);
        LinearLayout layout_level2 = (LinearLayout) context.findViewById(R.id.fragment_clist_dynamic_layout_level2);
        View camera = view.findViewById(R.id.pid_camera_btn);
        View gallery = view.findViewById(R.id.pid_gallery_btn);
        mediaButtonClickHandler =new MediaButtonClickHandler(context, this, fragment, Constants.FLOW_INVOICE);
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
                GetInvoiceResults InvoiceResults = new GetInvoiceResults(activity);
                InvoiceResults.BatchID = createFragmentWeakReference.get().getFromIntentData(Constants.BATCH_ID);;
                InvoiceResults.execute();
                while (!InvoiceResults.Completed == true){
                    try{Thread.sleep(100);}
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
                //Get the Values back
                myInvoiceResults = InvoiceResults;
                //setupInvoiceDetailsUI(myInvoiceResults);
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
        setupInvoiceDetailsUI(myInvoiceResults);
    }

    @Override
    public boolean ifBatchUpdateInProgress() {
        return batchUpdateInProgress;
    }

    private void setupInvoiceDetailsUI(GetInvoiceResults InvoiceResults){
        LayoutInflater layoutInflater = createFragmentWeakReference.get().getActivity().getLayoutInflater();
        final View invoice_details_view = layoutInflater.inflate(R.layout.details_invoice,null);
        LinearLayout layout_level_2 = (LinearLayout) createFragmentWeakReference.get().getActivity().findViewById(R.id.fragment_clist_dynamic_layout_level2);
        addImageThumbnail();


        String InvoiceNumber = InvoiceResults.InvoiceNumber;
        String InvoiceDate = InvoiceResults.InvoiceDate;
        String InvoiceAmount = InvoiceResults.InvoiceAmount ;


        EditText InvoiceNumberText = (EditText) invoice_details_view.findViewById(R.id.txt_Invoice_Number);
        InvoiceNumberText.setText(InvoiceNumber);

        EditText InvoiceDateText = (EditText) invoice_details_view.findViewById(R.id.txt_Invoice_Date);
        InvoiceDateText.setText(InvoiceDate);

        EditText InvoiceAmountText = (EditText) invoice_details_view.findViewById(R.id.txt_Invoice_Amount);
        InvoiceAmountText.setText(InvoiceAmount);



        Button acceptButton = (Button) invoice_details_view.findViewById(R.id.btn_proof_id_accept);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createProofOfIDInServer(invoice_details_view);
            }
        });

        Button cancelButton = (Button) invoice_details_view.findViewById(R.id.btn_proof_id_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelProofSubmission();
            }
        });
        layout_level_2.addView(invoice_details_view);

    }

    private void addImageThumbnail() {
        LinearLayout layout_level_1 = (LinearLayout) createFragmentWeakReference.get().getActivity().findViewById(R.id.fragment_clist_dynamic_layout_level1);
        layout_level_1.removeAllViews();

        LayoutInflater layoutInflater = createFragmentWeakReference.get().getActivity().getLayoutInflater();
        ImageView preview_image_view = (ImageView) layoutInflater.inflate(R.layout.preview_image,null);
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


        //Now go back to the main screen
            AppConnectionUtility.loginToStartScreen(createFragmentWeakReference.get().getActivity(), null, null);
    }

}
