package ing.rbi.poc;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;



import com.google.gson.Gson;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class GetBillResults extends AsyncTask {
	private Context context;
	public Boolean Completed = false;
	public String BatchID;
	public String CustomerAccount = "";
	public String CreditAccount = "";
	public String AmountDue = "";
	public String AmountPaid = "";
	public String Currency = "";
	
	//private ProgressDialog dialog;
	
	@Override
	protected void onPreExecute() {
//	    dialog = new ProgressDialog(context);
//	    dialog.setMessage("Please wait...");
//	    dialog.show();
//	    dialog.getCurrentFocus();
//
	   
	} 

	@Override
	protected void onPostExecute(Object result) {
		// TODO Auto-generated method stub
//		if (dialog.isShowing()) {
//			dialog.dismiss();
//		}
		super.onPostExecute(result);
	}
	
	private class BillValuesResponse{
		public BillValuesResultDetails BillValuesResult;
	}
private class BillValuesResultDetails {
	public String CustomerAccount;
	public String CreditAccount;
	public String AmountDue;
	public String AmountPaid;
	public String Currency;
	
}
	public GetBillResults(Context context){
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
		PrefURI = PrefURI + "Service1.svc/Bill/" + StrBatchID;
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(PrefURI);
		HttpResponse response;
		try {
			response = client.execute(request);
			String strResponse = EntityUtils.toString(response.getEntity(), "UTF8");
			
			//Desierialize the message back
			Gson gsonResponse = new Gson();
			BillValuesResponse BResValues = gsonResponse.fromJson(strResponse, BillValuesResponse.class);
			CustomerAccount = BResValues.BillValuesResult.CustomerAccount;
			CreditAccount = BResValues.BillValuesResult.CreditAccount; 
			AmountDue = BResValues.BillValuesResult.AmountDue;
			AmountPaid = BResValues.BillValuesResult.AmountPaid;
			Currency = BResValues.BillValuesResult.Currency;
		}
		catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Completed = true;
	}
	

}
