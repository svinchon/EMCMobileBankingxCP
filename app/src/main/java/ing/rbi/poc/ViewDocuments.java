package ing.rbi.poc;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ViewDocuments extends Activity {
public String[] ItemID;
public String[] ItemName;
public Integer ItemSelected = 0;
public String BaseURI = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		ItemID = intent.getStringArrayExtra("ItemID");
		ItemName = intent.getStringArrayExtra("ItemName");
		BaseURI = intent.getStringExtra("BaseURI");
		//Change the content view
		setContentView(ing.rbi.poc.R.layout.activity_view_documents);
		//Set the values
		ArrayAdapter<String> listAdapter;
		ListView DocListView = (ListView) findViewById(ing.rbi.poc.R.id.lst_docs);
		listAdapter = new ArrayAdapter<String>(ViewDocuments.this,android.R.layout.simple_expandable_list_item_1,ItemName);
		DocListView.setAdapter(listAdapter);
		
		DocListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				ItemSelected = position;
				//Now retreive the document
				String URI = "";
				URI = "/dctm-rest/repositories/corp/objects/" + ItemID[position] + "/contents/content.json?page=&format=&modifier=";
				URI = BaseURI + URI;
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(ing.rbi.poc.R.menu.view_documents, menu);
		return true;
	}

}
