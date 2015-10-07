package ing.rbi.poc;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link country_list.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link country_list#newInstance} factory method to
 * create an instance of this fragment.
 */
public class country_list extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Spinner spLoadFrom;
    private Spinner spDocType;
    private Resources res;
    private String[] displayCountry;
    private String[] displayDocType;
    private String[] displayDocTypeAdjusted;
    private String SelectedCountry;
    private String SelectedDocType;

    private MediaSelectedHandler mediaSelectHandler;
    private static String TAG = MediaButtonClickHandler.class.getSimpleName();
    private HashMap<String, String> intentData = new HashMap<String, String>();

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment country_list.
     */
    // TODO: Rename and change types and number of parameters
    public static country_list newInstance(String param1, String param2) {
        country_list fragment = new country_list();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public country_list() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Create the Listener
        View v = inflater.inflate(R.layout.fragment_country_list, container, false);
        //Country Listener
        spLoadFrom = (Spinner) v.findViewById(R.id.country_spinner);
        res = getResources();
        displayCountry = res.getStringArray(R.array.country_array);


        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, displayCountry);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spLoadFrom.setAdapter(spinnerArrayAdapter);

        SpinnerListener spListener = new SpinnerListener();


        spLoadFrom.setOnItemSelectedListener(spListener);

        //DocTypeListener
        spDocType = (Spinner) v.findViewById(R.id.doctype_spinner);
        spDocType.setEnabled(false);

        displayDocType = res.getStringArray(R.array.doctype_array);
        ArrayAdapter<String> doctypeSpinArrayAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, displayDocType);
        doctypeSpinArrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spDocType.setAdapter(doctypeSpinArrayAdapter);

        SpinnerListener DocSpin = new SpinnerListener();
        spDocType.setOnItemSelectedListener(DocSpin);
        // Inflate the layout for this fragment


        return v;
    }

    public class SpinnerListener implements AdapterView.OnItemSelectedListener {

        public SpinnerListener() {

        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Spinner SpinView = (Spinner) parent;
            //Check to see if country has been selected
            if (SpinView.getId() == R.id.country_spinner) if (position > 0) {
                spDocType.setEnabled(true);
                SelectedCountry = displayCountry[position];
                // TODO SEB code to ajust doc list depending on country
                List<String> list = new ArrayList<String>();
                String strToKeep;
                if (SelectedCountry.equals("UK")) {
                    strToKeep = "Passport";
                    for (int i = 0; i < displayDocType.length; i++) {
                        if (strToKeep.indexOf(displayDocType[i]) >= 0) {
                            list.add(displayDocType[i]);
                        }
                    }
                }  else if (SelectedCountry.equals("Netherlands")) {
                    strToKeep = "Passport;Driver License;National Identity Document";
                    for (int i = 0; i < displayDocType.length; i++) {
                        if (strToKeep.indexOf(displayDocType[i]) >= 0) {
                            list.add(displayDocType[i]);
                        }
                    }
                } else {
                    for (int i = 0; i < displayDocType.length; i++) {
                        list.add(displayDocType[i]);
                    }
                }
                displayDocTypeAdjusted = new String[list.size()];
                list.toArray(displayDocTypeAdjusted);
                ArrayAdapter<String> doctypeSpinArrayAdapter;
                doctypeSpinArrayAdapter = new ArrayAdapter<String>(
                        parent.getContext(),
                        android.R.layout.simple_spinner_item,
                        displayDocTypeAdjusted
                );
                doctypeSpinArrayAdapter.setDropDownViewResource(
                        android.R.layout.simple_dropdown_item_1line
                );
                spDocType.setAdapter(doctypeSpinArrayAdapter);
                spDocType.setEnabled(true);
            } else {
                spDocType.setEnabled(false);
            }
            else {
                if (position > 0) {
                    SelectedDocType = displayDocType[position];
                    //Check to see if it's a passport
                    if (
                            SelectedDocType.equals("Passport") || SelectedDocType.equals("Driver License")
                    ) {
                        handleProofID("Passport");
                    }
                    //Spanish ID
                    if (
                            SelectedDocType.equals("National Identity Document")
                            &&
                            SelectedCountry.equals("Spain")
                    ) {
                        //This is the two page document
                        handleSpainID("Spain_ID");
                    }
                    if (
                            SelectedDocType.equals("Invoice")
                            ||
                            SelectedDocType.equals("Insurance Certificate")
                    ) {
                        handleInvoice("Invoice");
                    }
                }
            }
        }

        ;

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    /*
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    */

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public void setMediaSelectHandler(MediaSelectedHandler mediaSelectHandler) {
        this.mediaSelectHandler = mediaSelectHandler;
    }

    public MediaSelectedHandler getMediaSelectHandler() {
        return mediaSelectHandler;
    }

    public synchronized void addToIntentData(String key, String value) {
        intentData.put(key, value);
    }

    public String getFromIntentData(String key) {
        return intentData.get(key);
    }

    public void cancelAllFlows() {
        //RadioGroup rl = (RadioGroup) this.getActivity().findViewById(ing.rbi.poc.R.id.radioGroup);
        //rl.clearCheck();

//        // TODO SEB changes to go back to country selection
//        LinearLayout layout_level1 = (LinearLayout) this.getActivity().findViewById(ing.rbi.poc.R.id.fragment_create_dynamic_layout_level1);
//        LinearLayout layout_level2 = (LinearLayout) this.getActivity().findViewById(ing.rbi.poc.R.id.fragment_create_dynamic_layout_level2);
//        layout_level1.removeAllViews();
//        layout_level2.removeAllViews();
        startActivity(this.getActivity().getIntent());
    }

    private void handleProofID(String flowType) {
        Activity context = country_list.this.getActivity();
        ProofIDHandler proofHandler = ProofIDHandler.getProofIDHandler(flowType);
        proofHandler.handleProofID(context, country_list.this);
    }

    private void handleSpainID(String flowType) {
        Activity context = country_list.this.getActivity();
        SpainIDHandler spainID = SpainIDHandler.getIDHandler(flowType);
        spainID.handleID(context, country_list.this);
    }

    private void handleInvoice(String flowType) {
        Activity context = country_list.this.getActivity();
        InvoiceHandler Invoice = InvoiceHandler.getInvoiceHandler(flowType);
        Invoice.handleInvoice(context, country_list.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle results for activities launched by this activity.
        try {
            if (requestCode == Constants.EVENT_CHOOSE_IMAGE && data != null && data.getData() != null) {
                // The user picked an image from the gallery.
                // Send the new picture taken to the enhancement screen so that users can modify it if necessary.
                Uri uri = data.getData();
                getMediaSelectHandler().getMediaButtonClickHandler().gotoEnhanceImage(uri);
            } else if (requestCode == Constants.EVENT_ENH_OPER) {
                if (resultCode != Activity.RESULT_CANCELED) {
                    intentData.put(Constants.FILE_NAME, data.getStringExtra("SavedFile"));
                    mediaSelectHandler.setupUIAfterEnhanceImage();
                }

            }
        } catch (Exception e) {
            // Log a message and display the error to the user using our utility function.
            Log.e(TAG, e.getMessage(), e);
            CoreHelper.displayError(this.getActivity(), e);
        }
    }


}
