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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by tejas on 26/04/15.
 */
public class BillHandler implements MediaSelectedHandler {
    private static BillHandler myInstance;
    private boolean isNewLoadForImage;
    private String FLOW_NAME;
    private WeakReference<CreateFragment> createFragmentWeakReference;
    private MediaButtonClickHandler mediaButtonClickHandler;
    private GetBillResults myBillResults;
    private ProgressDialog progressDialog;
    private boolean batchUpdateInProgress;


    public static BillHandler getBillHandler(String flowName){
        if(flowName != null){
            myInstance = new BillHandler(flowName);
        }
        return myInstance;
    }
    private BillHandler(String flowType) {
        super();
        FLOW_NAME = flowType;
    }
    private BillHandler(){
        super();
    }

    public void handleBill(Activity activity, CreateFragment fragment){
        Activity context = activity;
        createFragmentWeakReference = new WeakReference<CreateFragment>(fragment);
        fragment.setMediaSelectHandler(this);
        fragment.addToIntentData(Constants.SELECTED_FLOW, FLOW_NAME);
        LayoutInflater layoutInflater = context.getLayoutInflater();
        View view = layoutInflater.inflate(ing.rbi.poc.R.layout.add_bill, null);
        LinearLayout layout_level1 = (LinearLayout) context.findViewById(ing.rbi.poc.R.id.fragment_create_dynamic_layout_level1);
        LinearLayout layout_level2 = (LinearLayout) context.findViewById(ing.rbi.poc.R.id.fragment_create_dynamic_layout_level2);

        View camera = view.findViewById(ing.rbi.poc.R.id.pid_camera_btn);
        View gallery =view.findViewById(ing.rbi.poc.R.id.pid_gallery_btn);
        /* GH
        mediaButtonClickHandler =new MediaButtonClickHandler(context, this, fragment, Constants.FLOW_ADD_BILL);
        */
        camera.setOnClickListener(mediaButtonClickHandler);
        gallery.setOnClickListener(mediaButtonClickHandler);


        //remove other views
        layout_level1.removeAllViews();
        layout_level2.removeAllViews();

        //add view
        layout_level1.addView(view);


    }
    private void addImageThumbnail() {
        LinearLayout layout_level_1 = (LinearLayout) createFragmentWeakReference.get().getActivity().findViewById(ing.rbi.poc.R.id.fragment_create_dynamic_layout_level1);
        View mediaButtons = createFragmentWeakReference.get().getActivity().findViewById(ing.rbi.poc.R.id.add_bill_media_buttons);
        ViewGroup parent = (ViewGroup) mediaButtons.getParent();
        parent.removeView(mediaButtons);

        LayoutInflater layoutInflater = createFragmentWeakReference.get().getActivity().getLayoutInflater();
        ImageView preview_image_view = (ImageView) layoutInflater.inflate(ing.rbi.poc.R.layout.preview_image,null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(500,500);
        layoutParams.topMargin = 40;
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        String filePath = createFragmentWeakReference.get().getFromIntentData(Constants.FILE_NAME);
        File file = new File(filePath);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        preview_image_view.setImageBitmap(bitmap);
        preview_image_view.setLayoutParams(layoutParams);
        layout_level_1.addView(preview_image_view);
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
        return mediaButtonClickHandler;
    }

    public void BatchUpdate(String filePath, final Activity currentActivity) {
        Activity activity = createFragmentWeakReference.get().getActivity();
        View layoutView = activity.findViewById(ing.rbi.poc.R.id.fragment_create_dynamic_layout_level1);
        /* GH
        BatchUtility.getBatchDetails(createFragmentWeakReference.get());
        */
        UpdateBatch Batch = new UpdateBatch(activity);
        Batch.BatchID = createFragmentWeakReference.get().getFromIntentData(Constants.BATCH_ID);
        Batch.ticket = createFragmentWeakReference.get().getFromIntentData(Constants.TICKET);
        Batch.URI = createFragmentWeakReference.get().getFromIntentData(Constants.URI);
        if(filePath == null) {
            Batch.FileName = createFragmentWeakReference.get().getFromIntentData(Constants.FILE_NAME);
        }
        Batch.FileName = filePath;
        //Batch.chequeValue = Double.parseDouble(ChequeValue);
        EditText bill_value_text = (EditText) layoutView.findViewById(ing.rbi.poc.R.id.BillValue);
        EditText bill_reference_text = (EditText) layoutView.findViewById(ing.rbi.poc.R.id.BillReference);
        //Batch.docValue = bill_value_text.getText().toString();
        Batch.docValue = "0";
        Batch.docReference = bill_reference_text.getText().toString();
        //Get the preferences
        SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(activity);
        Batch.DefaultCustomerName = gprefs.getString("Default Customer Name", "");
        Batch.DefaultAccountName = gprefs.getString("Default Account Name", "");
        if (Batch.DefaultAccountName == "" || Batch.DefaultCustomerName == "") {

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertUtility.ShowAlertDialog("Default Account Settings not Set", "Check Preferences", currentActivity);
                }
            });

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
                GetBillResults BResults = new GetBillResults(activity);
                BResults.BatchID = createFragmentWeakReference.get().getFromIntentData(Constants.BATCH_ID);
                BResults.execute();
                while (!BResults.Completed == true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //Get the Values back
                //setupBillDetailsUI(BResults);
                myBillResults = BResults;

            } else {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertUtility.ShowAlertDialog("Batch Not Updated", "Unable to Add Bill", currentActivity);
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

    @Override
    public void setupUIAfterEnhanceImage() {
        setupBillDetailsUI(myBillResults);
    }

    @Override
    public boolean ifBatchUpdateInProgress() {
        return batchUpdateInProgress;
    }

    private void setupBillDetailsUI(GetBillResults bResults) {
        String customerAccount = bResults.CustomerAccount;
        String creditAccount = bResults.CreditAccount;
        String amountDue = bResults.AmountDue;
        String amountPaid = bResults.AmountPaid;
        String currency = bResults.Currency;
        LayoutInflater layoutInflater = createFragmentWeakReference.get().getActivity().getLayoutInflater();
        View bill_details_view = layoutInflater.inflate(ing.rbi.poc.R.layout.bill_results, null);
        LinearLayout layout = (LinearLayout) createFragmentWeakReference.get().getActivity().findViewById(ing.rbi.poc.R.id.fragment_create_dynamic_layout_level2);
        addImageThumbnail();
        EditText customerAccount_Text = (EditText) bill_details_view.findViewById(ing.rbi.poc.R.id.txt_CustomerAccount);
        EditText creditAccount_Text = (EditText) bill_details_view.findViewById(ing.rbi.poc.R.id.txt_CreditAccount);
        EditText amountDue_Text = (EditText) bill_details_view.findViewById(ing.rbi.poc.R.id.txt_AmountDue);
        EditText amountPaid_Text = (EditText) bill_details_view.findViewById(ing.rbi.poc.R.id.txt_AmountPaid);
        EditText currency_Text = (EditText) bill_details_view.findViewById(ing.rbi.poc.R.id.txt_Currency);

        customerAccount_Text.setText(customerAccount);
        creditAccount_Text.setText(creditAccount);
        amountDue_Text.setText(amountDue);
        amountPaid_Text.setText(amountPaid);
        currency_Text.setText(currency);

        Button acceptButton = (Button) bill_details_view.findViewById(ing.rbi.poc.R.id.btn_Bill_Accept);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBillInServer();
            }
        });

        Button cancelButton = (Button) bill_details_view.findViewById(ing.rbi.poc.R.id.btn_Bill_Cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelBill();
            }
        });
        layout.addView(bill_details_view);
    }

    private void cancelBill() {
        createFragmentWeakReference.get().cancelAllFlows();
    }

    private void createBillInServer() {

        //TODO - code not there
        //Now go back to the main screen
        AppConnectionUtility.loginToStartScreen(createFragmentWeakReference.get().getActivity(), null, null);

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
}
