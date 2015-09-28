package ing.rbi.poc;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CreateFragment extends Fragment {

    private MediaSelectedHandler mediaSelectHandler;
    private static String TAG = MediaButtonClickHandler.class.getSimpleName();
    private HashMap<String, String> intentData = new HashMap<String, String>();





    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //View view = inflater.inflate(ing.rbi.poc.R.layout.fragment_create, container, false);
        View view = inflater.inflate(R.layout.fragment_country_list, container, false);
        country_list fragment1 = new country_list();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(this.getId(), fragment1);
        fragmentTransaction.commit();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //setupCountryList();
        //setupRadioButtons(savedInstanceState);
    }

    private void setupRadioButtons(Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        String[] workflowTypes = bundle.getStringArray(getString(ing.rbi.poc.R.string.capture_flow_types));
        RadioGroup rl = (RadioGroup) getView().findViewById(ing.rbi.poc.R.id.radioGroup);
        FlowTypeCheckChangeListener radioGroupChangeListener = new FlowTypeCheckChangeListener();
        rl.setOnCheckedChangeListener(radioGroupChangeListener);
        Typeface FONT_CANTARELL_OBLIQUE = Typeface.createFromAsset(this.getActivity().getAssets(), "Cantarell-Regular.ttf");
        for (int i = 0; i < workflowTypes.length; i++) {
            RadioButton btn = new RadioButton(this.getActivity());
            btn.setId(i);
            btn.setText(workflowTypes[i]);
            btn.setTextSize(20);
            btn.setTypeface(FONT_CANTARELL_OBLIQUE);
            rl.addView(btn);
        }
    }
    public void setMediaSelectHandler(MediaSelectedHandler mediaSelectHandler) {
        this.mediaSelectHandler = mediaSelectHandler;
    }

    public MediaSelectedHandler getMediaSelectHandler() {
        return mediaSelectHandler;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle results for activities launched by this activity.
        try {
            if(requestCode == Constants.EVENT_CHOOSE_IMAGE && data != null && data.getData() != null) {
                // The user picked an image from the gallery.
                // Send the new picture taken to the enhancement screen so that users can modify it if necessary.
                Uri uri = data.getData();
                getMediaSelectHandler().getMediaButtonClickHandler().gotoEnhanceImage(uri);
            }
            else if (requestCode == Constants.EVENT_ENH_OPER) {
                if(resultCode != Activity.RESULT_CANCELED) {
                    intentData.put(Constants.FILE_NAME, data.getStringExtra("SavedFile"));
                    mediaSelectHandler.setupUIAfterEnhanceImage();
                }

            }
        }
        catch (Exception e) {
            // Log a message and display the error to the user using our utility function.
            Log.e(TAG, e.getMessage(), e);
            CoreHelper.displayError(this.getActivity(), e);
        }
    }

    public synchronized  void addToIntentData(String key, String value){
        intentData.put(key,value);
    }
    public String getFromIntentData(String key){
        return intentData.get(key);
    }


    //private class

    class FlowTypeCheckChangeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            if(i >= 0) {
                RadioButton selectedButton = (RadioButton) radioGroup.getChildAt(i);
                String buttonText = (String) selectedButton.getText();
                if (buttonText.contains("Cheque")) {
                    handleDepositCheque(buttonText);
                } else if (buttonText.contains("Bill")) {
                    handleBill(buttonText);
                } else if (buttonText.contains("ID")) {
                    handleProofID(buttonText);
                }
            }
        }

        private void handleProofID(String flowType) {
            Activity context = CreateFragment.this.getActivity();
            ProofIDHandler proofHandler = ProofIDHandler.getProofIDHandler(flowType);
           // proofHandler.handleProofID(context, CreateFragment.this);
        }

        private void handleBill(String flowType) {
            Activity context = CreateFragment.this.getActivity();
            BillHandler billHandler = BillHandler.getBillHandler(flowType);
            billHandler.handleBill(context, CreateFragment.this);
        }

        private void handleDepositCheque(String flowType) {
            Activity context = CreateFragment.this.getActivity();
            ChequeHandler chequeHandler = ChequeHandler.getChequeHandler(flowType);
            chequeHandler.handleCheque(context, CreateFragment.this);
        }
    }

    public void cancelAllFlows(){
            //RadioGroup rl = (RadioGroup) this.getActivity().findViewById(ing.rbi.poc.R.id.radioGroup);
            //rl.clearCheck();

            LinearLayout layout_level1 = (LinearLayout) this.getActivity().findViewById(ing.rbi.poc.R.id.fragment_create_dynamic_layout_level1);
            LinearLayout layout_level2 = (LinearLayout) this.getActivity().findViewById(ing.rbi.poc.R.id.fragment_create_dynamic_layout_level2);
            layout_level1.removeAllViews();
            layout_level2.removeAllViews();
    }
}
