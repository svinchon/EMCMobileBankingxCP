package ing.rbi.poc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class GetInvoiceResults extends AsyncTask{
	private Context context;
	public Boolean Completed = false;
	public String BatchID;
	public String InvoiceNumber;
	public String InvoiceDate;
	public String InvoiceAmount;

	//private ProgressDialog dialog;

	@Override
	protected void onPreExecute() {
//	    dialog = new ProgressDialog(context);
//	    dialog.setMessage("Please wait...");
//	    dialog.show();
//	    dialog.getCurrentFocus();


	}

	private class InvoiceValuesResponse{
		public InvoiceValuesResultDetails InvoiceValuesResult;
	}
	private class InvoiceValuesResultDetails {
		public String InvoiceNumber;
		public String InvoiceDate;
		public String InvoiceAmount;
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
	public GetInvoiceResults(Context context){
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
		PrefURI = PrefURI + "Service1.svc/INGI/" + StrBatchID;
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(PrefURI);
		HttpResponse response;
		try {
			response = client.execute(request);
			String strResponse = EntityUtils.toString(response.getEntity(), "UTF8");
			
			//Desierialize the message back
			Gson gsonResponse = new Gson();
			InvoiceValuesResponse InvoiceResValues = gsonResponse.fromJson(strResponse, InvoiceValuesResponse.class);
			InvoiceNumber = InvoiceResValues.InvoiceValuesResult.InvoiceNumber;
			InvoiceDate = InvoiceResValues.InvoiceValuesResult.InvoiceDate;
			InvoiceAmount = InvoiceResValues.InvoiceValuesResult.InvoiceAmount;

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
