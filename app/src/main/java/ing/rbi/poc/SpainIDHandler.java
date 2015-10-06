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
import android.widget.RadioButton;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tejas on 29/04/15.
 */
public class SpainIDHandler implements MediaSelectedHandler {
    private static SpainIDHandler myInstance;
    private String FLOW_NAME;
    private WeakReference<country_list> createFragmentWeakReference;
    private MediaButtonClickHandler mediaButtonClickHandler;
    private boolean newLoadForImage;
    private boolean batchUpdateInProgress;
    private boolean frontOfChequeProcessed;
    private GetChequeResults myChequeResults;
    private ProgressDialog progressDialog;
    private boolean creatingChequeInServer;
    private GetPIDResults myPidResults;


    public static SpainIDHandler getIDHandler(String flowType) {
        if (flowType != null) {
            myInstance = new SpainIDHandler(flowType);
        }
        return myInstance;
    }


    private SpainIDHandler(String flowType) {
        FLOW_NAME = flowType;
    }

    private SpainIDHandler() {
        super();
    }

    @Override
    public boolean isNewLoadForImage() {
        return newLoadForImage;
    }

    @Override
    public void setNewLoadForImage(boolean shouldLoadNew) {
        newLoadForImage = shouldLoadNew;
    }

    @Override
    public MediaButtonClickHandler getMediaButtonClickHandler() {
        return mediaButtonClickHandler;
    }

    @Override
    public void handlePostEnhanceImage(int enhanceResultCode, String filePath, Activity currentActivity) {
        batchUpdateInProgress = true;
        if (!frontOfChequeProcessed) {
            createFragmentWeakReference.get().addToIntentData(Constants.FRONT_CHEQUE_FILE_PATH, filePath);
        } else {
            createFragmentWeakReference.get().addToIntentData(Constants.BACK_CHEQUE_FILE_PATH, filePath);
        }
        ((EnhanceImageActivity) currentActivity).returnIntentResult(enhanceResultCode, filePath);
    }

    @Override
    public void setupUIAfterEnhanceImage() {
        if (!frontOfChequeProcessed) {
            //add thumbnail for front cheque and show radio buttons for back cheque
            String fileForFrontCheque = createFragmentWeakReference.get().getFromIntentData(Constants.FRONT_CHEQUE_FILE_PATH);
            addImageThumbnail(true, Gravity.CENTER_HORIZONTAL, fileForFrontCheque);
            addBackChequeUI();
        } else {
            //show 2 thumbnails side by side. remove radio buttons and media buttons
            realignUIAfterBackOfChequeProcess(true);
        }

    }

    private void realignUIAfterBackOfChequeProcess(boolean removeThumnbnailView) {
        removeAllUIElementsAddedBeforeBackChequeProcess(removeThumnbnailView);
        LinearLayout layout_level_1 = (LinearLayout) createFragmentWeakReference.get().getActivity().findViewById(R.id.fragment_clist_dynamic_layout_level1);
        //Setup two thumbnails for showing front and back of cheque
        String fileForFrontCheque = createFragmentWeakReference.get().getFromIntentData(Constants.FRONT_CHEQUE_FILE_PATH);
        String fileForBackCheque = createFragmentWeakReference.get().getFromIntentData(Constants.BACK_CHEQUE_FILE_PATH);

        LayoutInflater layoutInflater = createFragmentWeakReference.get().getActivity().getLayoutInflater();
        LinearLayout two_images_preview = (LinearLayout) layoutInflater.inflate(R.layout.cheque_two_thumbnails, null);
        File file1 = new File(fileForFrontCheque);
        File file2 = new File(fileForBackCheque);
        Bitmap bitmap1 = BitmapFactory.decodeFile(file1.getAbsolutePath());
        Bitmap bitmap2 = BitmapFactory.decodeFile(file2.getAbsolutePath());

        ImageView imageView1 = (ImageView) two_images_preview.findViewById(R.id.cheque_thumbnail_1);
        ImageView imageView2 = (ImageView) two_images_preview.findViewById(R.id.cheque_thumbnail_2);
        imageView1.setImageBitmap(bitmap1);
        imageView2.setImageBitmap(bitmap2);

        layout_level_1.addView(two_images_preview);

        //Add Proceed button
        Button proceedButton = (Button) layoutInflater.inflate(R.layout.cheque_proceed, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = (int) createFragmentWeakReference.get().getActivity().getResources().getDimension(R.dimen.margin_20);
        proceedButton.setLayoutParams(lp);
        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                proceedForBatchUpdate(true);
            }
        });
        layout_level_1.addView(proceedButton);
    }

    private void removeAllUIElementsAddedBeforeBackChequeProcess(boolean removeThumbnailView) {
        LinearLayout layout_level_1 = (LinearLayout) createFragmentWeakReference.get().getActivity().findViewById(ing.rbi.poc.R.id.fragment_clist_dynamic_layout_level1);

        //Remove all UI elements from front cheque addition
        //media buttons might already be removed
        View mediaButtonView = layout_level_1.findViewById(R.id.add_back_of_cheque_media_buttons);
        if(mediaButtonView != null) {
            ((ViewGroup) layout_level_1.findViewById(R.id.add_back_of_cheque_media_buttons).getParent()).removeView(layout_level_1.findViewById(ing.rbi.poc.R.id.add_back_of_cheque_media_buttons));
        }
        ((ViewGroup) layout_level_1.findViewById(R.id.back_cheque_radio_group).getParent()).removeView(layout_level_1.findViewById(ing.rbi.poc.R.id.back_cheque_radio_group));
        ((ViewGroup) layout_level_1.findViewById(R.id.label_back_cheque).getParent()).removeView(layout_level_1.findViewById(ing.rbi.poc.R.id.label_back_cheque));
        if(removeThumbnailView) {
            ((ViewGroup) layout_level_1.findViewById(R.id.image_preview).getParent()).removeView(layout_level_1.findViewById(ing.rbi.poc.R.id.image_preview));
        }
    }

    @Override
    public boolean ifBatchUpdateInProgress() {
        return batchUpdateInProgress;
    }

    public void handleID(Activity activity, country_list fragment) {
        Activity context = activity;
        createFragmentWeakReference = new WeakReference<country_list>(fragment);
        LayoutInflater layoutInflater = context.getLayoutInflater();
        fragment.setMediaSelectHandler(this);
        fragment.addToIntentData(Constants.SELECTED_FLOW, FLOW_NAME);


        View view = layoutInflater.inflate(R.layout.add_spain_id, null);
        LinearLayout layout_level1 = (LinearLayout) context.findViewById(ing.rbi.poc.R.id.fragment_clist_dynamic_layout_level1);
        LinearLayout layout_level2 = (LinearLayout) context.findViewById(ing.rbi.poc.R.id.fragment_clist_dynamic_layout_level2);
        View camera = view.findViewById(ing.rbi.poc.R.id.pid_camera_btn);
        View gallery = view.findViewById(ing.rbi.poc.R.id.pid_gallery_btn);
        mediaButtonClickHandler =new MediaButtonClickHandler(context, this, fragment, Constants.FLOW_SPAIN_ID);
        camera.setOnClickListener(mediaButtonClickHandler);
        gallery.setOnClickListener(mediaButtonClickHandler);

        //remove other views
        layout_level1.removeAllViews();
        layout_level2.removeAllViews();

        //add view
        layout_level1.addView(view);
    }

    private void addImageThumbnail(boolean shouldRemoveFrontChequeMediaButtons, int gravity, String filePath) {

        LinearLayout layout_level_1 = (LinearLayout) createFragmentWeakReference.get().getActivity().findViewById(ing.rbi.poc.R.id.fragment_clist_dynamic_layout_level1);
        if (shouldRemoveFrontChequeMediaButtons) {
            View mediaButtons = createFragmentWeakReference.get().getActivity().findViewById(R.id.add_cheque_media_buttons);
            ViewGroup parent = (ViewGroup) mediaButtons.getParent();
            parent.removeView(mediaButtons);
        }

        LayoutInflater layoutInflater = createFragmentWeakReference.get().getActivity().getLayoutInflater();
        ImageView preview_image_view = (ImageView) layoutInflater.inflate(R.layout.preview_image, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(500, 500);
        layoutParams.topMargin = 20;
        layoutParams.gravity = gravity;
        File file = new File(filePath);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        preview_image_view.setImageBitmap(bitmap);
        preview_image_view.setLayoutParams(layoutParams);
        layout_level_1.addView(preview_image_view);
    }


    private void addBackChequeUI() {
        frontOfChequeProcessed = true;
        LinearLayout layout_level_1 = (LinearLayout) createFragmentWeakReference.get().getActivity().findViewById(ing.rbi.poc.R.id.fragment_clist_dynamic_layout_level1);
        LayoutInflater layoutInflater = createFragmentWeakReference.get().getActivity().getLayoutInflater();
        final LinearLayout layoutBackOfCheque = (LinearLayout) layoutInflater.inflate(R.layout.back_of_cheque, null);

        final View backChequeMediaView = layoutBackOfCheque.findViewById(R.id.add_back_of_cheque_media_buttons);
        final View proceedButton = layoutBackOfCheque.findViewById(R.id.add_back_of_cheque_proceed);

        RadioButton yesButton = (RadioButton) layoutBackOfCheque.findViewById(R.id.back_cheque_radio_yes);
        RadioButton noButton = (RadioButton) layoutBackOfCheque.findViewById(R.id.back_cheque_radio_no);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewGroup mediaViewParent = (ViewGroup) backChequeMediaView.getParent();
                if (mediaViewParent != null) {
                    mediaViewParent.removeView(backChequeMediaView);
                }

                ViewGroup proceedButtonParent = (ViewGroup) proceedButton.getParent();
                if (proceedButtonParent != null) {
                    proceedButtonParent.removeView(proceedButton);
                }
                layoutBackOfCheque.addView(backChequeMediaView);
            }
        });
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewGroup mediaViewParent = (ViewGroup) backChequeMediaView.getParent();
                if (mediaViewParent != null) {
                    mediaViewParent.removeView(backChequeMediaView);
                }

                ViewGroup proceedButtonParent = (ViewGroup) proceedButton.getParent();
                if (proceedButtonParent != null) {
                    proceedButtonParent.removeView(proceedButton);
                }

                layoutBackOfCheque.addView(proceedButton);

            }
        });

        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                proceedForBatchUpdate(false);
            }
        });

        View camera = backChequeMediaView.findViewById(R.id.pid_camera_btn);
        View gallery = backChequeMediaView.findViewById(R.id.pid_gallery_btn);
        camera.setOnClickListener(mediaButtonClickHandler);
        gallery.setOnClickListener(mediaButtonClickHandler);

        ViewGroup mediaViewParent = (ViewGroup) backChequeMediaView.getParent();
        mediaViewParent.removeView(backChequeMediaView);
        ViewGroup proceedButtonParent = (ViewGroup) proceedButton.getParent();
        proceedButtonParent.removeView(proceedButton);

        layout_level_1.addView(layoutBackOfCheque);
    }

    private void proceedForBatchUpdate(final boolean backChequeAlsoProcessed) {
        batchUpdateInProgress = true;
        showProgressDialog();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                doBatchUpdate();
                while (batchUpdateInProgress) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                createFragmentWeakReference.get().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        removeProgressDialog();
                        setupIDProofDetailsUI(myPidResults);
                        //addChequeDetailsToSecondLevelUI(backChequeAlsoProcessed);
                    }
                });

            }
        });
        t.start();
    }

    private void setupIDProofDetailsUI(GetPIDResults PIDResults){

        LinearLayout layoutBackOfCheque = (LinearLayout) createFragmentWeakReference.get().getActivity().findViewById(R.id.layout_back_cheque);
        LinearLayout layoutparent = (LinearLayout) layoutBackOfCheque.getParent();
        View proceedButton = layoutparent.findViewById(R.id.add_cheque_proceed);
        ViewGroup proceedButtonParent = (ViewGroup) proceedButton.getParent();
        proceedButtonParent.removeView(proceedButton);

        LayoutInflater layoutInflater = createFragmentWeakReference.get().getActivity().getLayoutInflater();
        final View proof_details_view = layoutInflater.inflate(ing.rbi.poc.R.layout.details_proof_id,null);
        LinearLayout layout_level_2 = (LinearLayout) createFragmentWeakReference.get().getActivity().findViewById(ing.rbi.poc.R.id.fragment_clist_dynamic_layout_level2);
        //addImageThumbnail();


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

        Button acceptButton = (Button) proof_details_view.findViewById(ing.rbi.poc.R.id.btn_proof_id_accept);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //createProofOfIDInServer(proof_details_view);
                //Now go back to the main screen
                AppConnectionUtility.loginToStartScreen(createFragmentWeakReference.get().getActivity(), null, null);
            }
        }

        );

        Button cancelButton = (Button) proof_details_view.findViewById(ing.rbi.poc.R.id.btn_proof_id_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Now go back to the main screen
                AppConnectionUtility.loginToStartScreen(createFragmentWeakReference.get().getActivity(), null, null);
                //cancelProofSubmission();
            }
        });
        layout_level_2.addView(proof_details_view);

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
    private void cancelProofSubmission() {
        createFragmentWeakReference.get().cancelAllFlows();
    }

    private void addChequeDetailsToSecondLevelUI(boolean backChequeAlsoProcessed) {
        Activity activity = createFragmentWeakReference.get().getActivity();
        LinearLayout layout_level_1 = (LinearLayout) activity.findViewById(R.id.fragment_clist_dynamic_layout_level1);
        EditText chequeReferenceText = (EditText) layout_level_1.findViewById(R.id.ChequeReference);
        String chequeReference = chequeReferenceText.getText().toString();
        String AccountNumber = myChequeResults.AccountNumber;
        String ChequeAmount = myChequeResults.ChequeAmount;
        String ChequeDate = myChequeResults.ChequeDate;
        String ChequeNumber = myChequeResults.ChequeNumber;
        String SortCode = myChequeResults.SortCode;
        String batchID = createFragmentWeakReference.get().getFromIntentData(Constants.BATCH_ID);

        if(backChequeAlsoProcessed){
            Button proceedButton = (Button) layout_level_1.findViewById(R.id.add_cheque_proceed);
            ViewGroup parent = (ViewGroup) proceedButton.getParent();
            parent.removeView(proceedButton);
        }
        else {
            removeAllUIElementsAddedBeforeBackChequeProcess(false);
            Button proceedButton = (Button) layout_level_1.findViewById(R.id.add_back_of_cheque_proceed);
            ViewGroup parent = (ViewGroup) proceedButton.getParent();
            parent.removeView(proceedButton);
        }

        LinearLayout layout_level_2 = (LinearLayout) activity.findViewById(R.id.fragment_clist_dynamic_layout_level2);
        final View cheque_details_view = activity.getLayoutInflater().inflate(R.layout.cheque_results, null);
        Button acceptButton = (Button) cheque_details_view.findViewById(R.id.btn_Cheque_Accept);
        Button cancelButton = (Button) cheque_details_view.findViewById(R.id.btn_Cheque_Cancel);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               handleChequeUpload();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelChequeFlow();
            }
        });

        EditText chequeDateText = (EditText) cheque_details_view.findViewById(R.id.txt_chequedate);
        EditText chequeNumberText = (EditText) cheque_details_view.findViewById(R.id.txt_ChequeNumber);
        EditText branchCodeText = (EditText) cheque_details_view.findViewById(R.id.txt_BranchCode);
        EditText chequeAmountText = (EditText) cheque_details_view.findViewById(R.id.txt_ChequeAmount);
        EditText accountNumberText = (EditText) cheque_details_view.findViewById(R.id.txt_AccountNumber);
        chequeDateText.setText(ChequeDate);
        chequeNumberText.setText(ChequeNumber);
        branchCodeText.setText(SortCode);
        chequeAmountText.setText(ChequeAmount);
        accountNumberText.setText(AccountNumber);

        layout_level_2.addView(cheque_details_view);
    }

    private void handleChequeUpload() {
        showProgressDialog();
        creatingChequeInServer = true;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                createChequeInServer();
                while (creatingChequeInServer) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                createFragmentWeakReference.get().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        removeProgressDialog();
                        //go back to start screen
                        AppConnectionUtility.loginToStartScreen(createFragmentWeakReference.get().getActivity(), null, null);
                    }
                });
            }
        });
        t.start();
    }


    private void doBatchUpdate() {
        BatchUtility.getBatchDetails(createFragmentWeakReference.get());
        final Activity activity = createFragmentWeakReference.get().getActivity();
        UpdateBatch Batch = new UpdateBatch(activity);
        Batch.BatchID = createFragmentWeakReference.get().getFromIntentData(Constants.BATCH_ID);
        Batch.ticket = createFragmentWeakReference.get().getFromIntentData(Constants.TICKET);
        Batch.URI = createFragmentWeakReference.get().getFromIntentData(Constants.URI);
        Batch.FileName = createFragmentWeakReference.get().getFromIntentData(Constants.FRONT_CHEQUE_FILE_PATH);
        String backChequeFilePath = createFragmentWeakReference.get().getFromIntentData(Constants.BACK_CHEQUE_FILE_PATH);
        if (backChequeFilePath != null) {
            Batch.FileName2 = backChequeFilePath;
        }
        LinearLayout layout_level_1 = (LinearLayout) activity.findViewById(R.id.fragment_clist_dynamic_layout_level1);


        Batch.docReference = "null";
        //Get the preferences
        SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(activity);
        Batch.DefaultCustomerName = gprefs.getString("Default Customer Name", "");
        Batch.DefaultAccountName = gprefs.getString("Default Account Name", "");
        if (Batch.DefaultAccountName == "" || Batch.DefaultCustomerName == "") {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertUtility.ShowAlertDialog("Default Account Settings not Set", "Check Preferences", activity);
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
                GetPIDResults PIDResults = new GetPIDResults(activity);
                PIDResults.BatchID = createFragmentWeakReference.get().getFromIntentData(Constants.BATCH_ID);;
                PIDResults.execute();
                while (!PIDResults.Completed == true){
                    try{Thread.sleep(100);}
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
                //Get the Values back
                myPidResults = PIDResults;
                batchUpdateInProgress = false;
                //setupIDProofDetailsUI(PIDResults);
            } else {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertUtility.ShowAlertDialog("Batch Not Updated", "Unable to Add Cheque", activity);
                    }
                });
            }
        }
    }
    private void createChequeInServer() {
        Activity activity = createFragmentWeakReference.get().getActivity();
        SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(activity);
        EditText CA = (EditText) activity.findViewById(R.id.txt_ChequeAmount);
        EditText CD = (EditText) activity.findViewById(R.id.txt_chequedate);
        EditText CN = (EditText) activity.findViewById(R.id.txt_ChequeNumber);
        EditText BC = (EditText) activity.findViewById(R.id.txt_BranchCode);
        EditText AN = (EditText) activity.findViewById(R.id.txt_AccountNumber);
        String BatchID = createFragmentWeakReference.get().getFromIntentData(Constants.BATCH_ID);

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
        cheque_reference = "&cheque_reference=null";
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
        UpdatexCP update = new UpdatexCP(activity);
        update.UpdateURI = xCPURI;
        update.execute();
        while (!update.Completed == true){
            try{Thread.sleep(100);}
            catch (InterruptedException e) { e.printStackTrace(); }
        }

        creatingChequeInServer = false;
    }


    private void cancelChequeFlow() {
        createFragmentWeakReference.get().cancelAllFlows();
    }



    private void showProgressDialog() {
        progressDialog = new ProgressDialog(createFragmentWeakReference.get().getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        progressDialog.getCurrentFocus();
    }
    private void removeProgressDialog() {
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }
}
