package ing.rbi.poc;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by tejas on 22/04/15.
 */
public class ViewDocumentsFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(ing.rbi.poc.R.layout.fragment_view_document, container, false);
        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sendFindDocuments(view);
    }

    public void sendFindDocuments(View view) {
        //Now Get all of the documentum documents for that account
        QueryCustomerFolder QFolder = new QueryCustomerFolder(this.getActivity());
        SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        String baseURI = "";
        baseURI = gprefs.getString("xCP BPS URI Address", "");
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
        String[] itemIDs = new String[ItemCount];
        String[] itemNames = new String[ItemCount];
        String[] itemContentType = new String[ItemCount];
        itemIDs = QFolder.ItemID;
        itemNames = QFolder.ItemName;
        itemContentType = QFolder.ItemContentType;
        if (ItemCount != 0) {
            // Show the list of documents
            //Now we need to star another activity to show the list of documents to the user
            showDocuments(itemIDs, itemNames, itemContentType, baseURI);
        }
        else {
            //Show a message
            ((CreateBatchTabbed)this.getActivity()).ShowDLG("No Documents Found", "No Documents Found");
        }

    }

    private void showDocuments(final String[] itemIDs, String[] itemNames, String[] itemContentType, final String baseURI) {
        //Set the values
        ;
        ListView DocListView = (ListView) this.getActivity().findViewById(ing.rbi.poc.R.id.fragment_documents_docList);
       // listAdapter = new ArrayAdapter<String>(this.getActivity(),android.R.layout.simple_expandable_list_item_1,itemNames);
        DocumentListArrayAdapter listAdapter = new DocumentListArrayAdapter(this.getActivity(), ing.rbi.poc.R.layout.doc_list_item, ing.rbi.poc.R.id.doc_list_item_text1 ,itemNames,itemContentType);

        DocListView.setAdapter(listAdapter);

        DocListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                int itemSelected = position;
                //Now retreive the document
                String URI = "";
                URI = "/dctm-rest/repositories/corp/objects/" + itemIDs[position] + "/contents/content.json?page=&format=&modifier=";
                URI = baseURI + URI;
                GetDocument GetDoc = new GetDocument(view.getContext());
                GetDoc.InputURI = URI;
                GetDoc.execute();
                //Loop until it's completed OK
                while (!GetDoc.Completed == true) {
                    try{Thread.sleep(100);}
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
            }

        });
    }

}
