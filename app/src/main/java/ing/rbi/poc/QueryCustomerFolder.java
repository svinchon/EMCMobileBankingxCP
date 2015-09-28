package ing.rbi.poc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;



import com.google.gson.Gson;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;




public class QueryCustomerFolder extends AsyncTask {
	private Context context;
	public String[] ItemName = new String[1];
	public String[] ItemID = new String[1];
    public String[] ItemContentType = new String[1];
	public Integer ItemCount = 0;
	public String URI = "";
	public String CustomerName = "";
	public Boolean Completed = false;
	public String dlgMessage = "";
	public String dlgTitle = "";
	private class DQLResponse {
		public String id;
		public String title;
		public String updated;
		public Authors[] author;
		public Link[] links;
		public Entry[] entries;
	}
	private class Entry {
		public String id;
		public String title;
		public String updated;
		public Content content;
	}
	private class Content {
		public String name;
		public PropertyVals properties;
	}
	private class PropertyVals {
		public String r_object_id;
		public String object_name;
        public String a_content_type;
	}
	private class Authors {
		public String name;
	}
	private class Link {
		public String rel;
		public String href;
	}
	public QueryCustomerFolder(Context context) {
		this.context = context;
	}
	@Override
	protected Object doInBackground(Object... params) {
		// TODO Auto-generated method stub
		queryFolder();
		Completed = true;
		return null;
	}
  private void queryFolder() {
	  //First get the URI and Customer name
	  String dqlURI = URI+ "/dctm-rest/repositories/corp.json?dql=";
      String DQL = "SELECT r_object_id, object_name,a_content_type FROM dm_document WHERE folder('/EMC Banking Accounts/" + CustomerName + "', DESCEND)";
	  DQL = DQL.replace(" ", "%20");
	  dqlURI = dqlURI + DQL;
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(dqlURI);
		String AuthValue = "";
		String UserPassword = "dmadmin:demo.demo";
		String encodedBytes = "";
		try {
			encodedBytes = Base64.encodeToString(UserPassword.getBytes("UTF-8"), Base64.NO_WRAP);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		AuthValue = "Basic " + encodedBytes;
		request.addHeader("Authorization", AuthValue);
		request.addHeader("Content-Type", "application/json");
		request.addHeader("Accept", "application/json");
		//HttpPost request = new HttpPost(UpdateURI);
		try {
			HttpResponse response =client.execute(request);
			String strResponse = EntityUtils.toString(response.getEntity(), "UTF8");
			Gson gsonResponse = new Gson();
			DQLResponse DQLResponse = gsonResponse.fromJson(strResponse, DQLResponse.class);
			//Check it's worked OK
			if (!DQLResponse.title.isEmpty()) {
				//Get a list of all of the Object IDs and names
				//Now loop through all of the records
				int Count = 0;
				//Find the length of the array
				Integer NumOfEntries = DQLResponse.entries.length;
				String[] t_id = new String[NumOfEntries];
				String[] t_name = new String[NumOfEntries];
                String[] t_contentType = new String[NumOfEntries];
				while (Count < NumOfEntries) {
					
					t_id[Count] = DQLResponse.entries[Count].content.properties.r_object_id;
					t_name[Count] = DQLResponse.entries[Count].content.properties.object_name;
                    t_contentType[Count] = DQLResponse.entries[Count].content.properties.a_content_type;
					Count = Count + 1;
					ItemCount = Count;
				}
				//Now copy the Arrays
				ItemID = t_id;
				ItemName = t_name;
                ItemContentType = t_contentType;
			}
		}
		catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			dlgMessage = e.getMessage().toString();
			dlgTitle = "Unable to Connect to xCP";
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			dlgMessage = e.getMessage().toString();
			dlgTitle = "Unable to Connect to xCP";
		}
		finally {
	}
	  
	  
	  
  }
}
