package ing.rbi.poc;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;


public class UpdatexCP extends AsyncTask {
	//private ProgressDialog dialog;
	public boolean Completed = false;
	public String dlgMessage = "";
	public String dlgTitle = "";
	private Context context;
	public String UpdateURI = "";
	public UpdatexCP(Context context) {
		this.context = context;
	}
	@Override
	protected void onPreExecute() {
//	    dialog = new ProgressDialog(context);
//	    dialog.setMessage("Please wait...");
//	    dialog.setIndeterminate(true);
//	    dialog.show();
	    super.onPreExecute();
	} 
	
	@Override
	protected void onPostExecute(Object result) {
		// TODO Auto-generated method stub
//		if (dialog.isShowing()) {
//			dialog.dismiss();
//		}
		super.onPostExecute(result);
	}
	@Override
	protected Object doInBackground(Object... params) {
		// TODO Auto-generated method stub
		UpdatexCPfunc();
		Completed = true;
		return null;
	}
	private void UpdatexCPfunc() {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(UpdateURI);
		//HttpPost request = new HttpPost(UpdateURI);
		try {
			HttpResponse response =client.execute(request);
			String rString = response.toString();
			rString = rString + "";
		}
		catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			dlgMessage = e.getMessage().toString();
			dlgTitle = "Unable to Update xCP";
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			dlgMessage = e.getMessage().toString();
			dlgTitle = "Unable to Update xCP";
		}
		finally {
			
		
	}
	}
}
