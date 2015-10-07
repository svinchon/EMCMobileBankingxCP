package ing.rbi.poc;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.google.gson.Gson;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class GetPIDResults extends AsyncTask{
	private Context context;
	public Boolean Completed = false;
	public String BatchID;
	public String Address;
	public String DOB;
	public String DocumentNumber;
	public String DocumentType;
	public String ExpirationDate;
	public String Forename;
	public String Surname;
	//private ProgressDialog dialog;
	
	@Override
	protected void onPreExecute() {
//	    dialog = new ProgressDialog(context);
//	    dialog.setMessage("Please wait...");
//	    dialog.show();
//	    dialog.getCurrentFocus();
	    
	   
	} 
	
	private class PIDValuesResponse{
		public PIDValuesResultDetails PIDValuesResult;
	}
	private class PIDValuesResultDetails {
		public String Address;
		public String DOB;
		public String DocumentNumber;
		public String DocumentType;
		public String ExpirationDate;
		public String Forename;
		public String Surname;
	}
	@Override
	protected Object doInBackground(Object... params) {
		// TODO Auto-generated method stub
		GetResults();
		return null;
	}
	@Override
	protected void onPostExecute(Object result) {
		// TODO Auto-generated method stub
//		if (dialog.isShowing()) {
//			dialog.dismiss();
//		}
		super.onPostExecute(result);
	}
	public GetPIDResults(Context context){
		this.context  = context;
	}

	private void GetResults() {
		String PrefURI;
		//first get the URI from the preferences
		SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(context);
		PrefURI = gprefs.getString("Results URI Address", "");
		String StrBatchID ="";
		Integer pos;
		String pointer;
		pos = BatchID.lastIndexOf("/") + 1;	
		StrBatchID = BatchID.substring(pos);
		PrefURI = PrefURI + "Service1.svc/PID/" + StrBatchID;
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(PrefURI);
		HttpResponse response;
        String strResponse = "";
		try {
			response = client.execute(request);
			strResponse = EntityUtils.toString(response.getEntity(), "UTF8");
            Log.e("DEBUG", "Server says:" + strResponse);
            Log.e("DEBUG", "isValidJSON says:" + isValidJSON(strResponse));
    		//Desierialize the message back
			Gson gsonResponse = new Gson();
			PIDValuesResponse PIDResValues = gsonResponse.fromJson(strResponse, PIDValuesResponse.class);
			Address = PIDResValues.PIDValuesResult.Address;
			DOB = PIDResValues.PIDValuesResult.DOB;
			DocumentNumber = PIDResValues.PIDValuesResult.DocumentNumber;
			DocumentType = PIDResValues.PIDValuesResult.DocumentType;
			ExpirationDate = PIDResValues.PIDValuesResult.ExpirationDate;
			Forename = PIDResValues.PIDValuesResult.Forename;
			Surname = PIDResValues.PIDValuesResult.Surname;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Completed = true;
	}

    private boolean isValidJSON(String str) {
        try {
            new JSONObject(str);
        } catch (JSONException ex) {
            try {
                new JSONArray(str);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

}
