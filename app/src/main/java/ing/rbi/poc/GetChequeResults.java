package ing.rbi.poc;

import java.io.IOException;

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

public class GetChequeResults extends AsyncTask {
	private Context context;
	public Boolean Completed = false;
	public String BatchID;
	public String AccountNumber;
	public String ChequeAmount;
	public String ChequeDate;
	public String ChequeNumber;
	public String SortCode;
	private class ChequeValuesResponse{
		public ChequeValuesResultDetails ChequeValuesResult;
	}
private class ChequeValuesResultDetails {
	public String AccountNumber;
	public String ChequeAmount;
	public String ChequeDate;
	public String ChequeNumber;
	public String SortCode;
	
}
public GetChequeResults(Context context){
	this.context  = context;
}
	@Override
	protected Object doInBackground(Object... params) {
		// TODO Auto-generated method stub
		GetResults();
		return null;
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
		PrefURI = PrefURI + "Service1.svc/Cheque/" + StrBatchID;
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(PrefURI);
		HttpResponse response;
		try {
			response = client.execute(request);
			String strResponse = EntityUtils.toString(response.getEntity(), "UTF8");
			
			//Desierialize the message back
			Gson gsonResponse = new Gson();
			ChequeValuesResponse CResValues = gsonResponse.fromJson(strResponse, ChequeValuesResponse.class);
			AccountNumber = CResValues.ChequeValuesResult.AccountNumber;
			ChequeAmount = CResValues.ChequeValuesResult.ChequeAmount;
			ChequeDate = CResValues.ChequeValuesResult.ChequeDate;
			ChequeNumber = CResValues.ChequeValuesResult.ChequeNumber;
			SortCode = CResValues.ChequeValuesResult.SortCode;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Completed = true;
	}
	
}