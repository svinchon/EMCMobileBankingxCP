package ing.rbi.poc;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.BasicManagedEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IInterface;
import android.provider.Settings;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;


public class ResolveConnection extends AsyncTask  {
public String uri;
public String user;
public String password;
public Boolean Completed = false;
public String[] CaptureFlows;
public String StrResponse = "";
public Boolean ConnectOK = false;
public String ticket = "";

private class link{
	public String rel;
	public String href;
}
private class sessionresponse {
	public returnStatus returnStatus;
	public String id;
	public String title;
	public link[] links;
}
private class returnStatus{
	public Integer status;
	public String code;
	public String message;
	public String server;
}
private class loginRequest {
	public String culture;
	public String licenseKey = "LICE083-CD06-6433";
	public String deviceId;
	public String applicationId = "APP3001-D09F-5EC8";
	public String username;
	public String password;
}
private class loginResponse {
	public returnStatus returnStatus;
	public String ticket;
}
private class tablesResponse {
	public returnStatus returnStatus;
	public String id;
	public String title;
	public String updated;
	public link[] links;
	public Content content;
}
private class Content {
	public String id;
	public String tableName;
	public String[] fieldNames;
	public Object[][] rows;
	}
private Context context;
private ProgressDialog dialog;
public ResolveConnection(Context context){
    this.context = context;
}	
@Override
protected void onPreExecute() {
    dialog = new ProgressDialog(context);
    dialog.setMessage("Please wait...");
    dialog.setIndeterminate(true);
    dialog.show();
    super.onPreExecute();
}  

private Boolean Connect() {
		String txtResponse = "";
		Boolean OK = false;
		try {
			
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(uri);
			//Nothing to construct here

			HttpResponse response = client.execute(request);
			HttpEntity httpEntity = response.getEntity();
			txtResponse = httpEntity.getContent().toString();
			Integer StatCode;
			StatCode = response.getStatusLine().getStatusCode();
			txtResponse  = Integer.toString(StatCode);
			String ticket;
			ticket = Login();
			
			if (ticket != "" && ticket != "null") {
				//Now Get a list of Capture flows and open a new screen
				CaptureFlows = GetCaptureFlowList(ticket);
				//Now we need to pass this to the new UI
				OK = true;
			}
			else {
			txtResponse = "Unable to Login";	
			OK = false;
			}
		} catch (ClientProtocolException e) {
			Log.d("HTTPCLIENT", e.getLocalizedMessage());
			txtResponse = "Unable To Reach Server";
		} catch (IOException e) {
			Log.d("HTTPCLIENT", e.getLocalizedMessage());
			txtResponse = "Unable To Reach Server";
			StrResponse = txtResponse;
		} catch(IllegalArgumentException e) {
			Log.d("HTTPCLIENT", e.getLocalizedMessage());
			txtResponse = "Unable To Reach Server";
		}
		finally{
			//If this works then try to login
		}
		return OK;
			
	}
	private String[] GetCaptureFlowList(String ticket) {
		//This will bring back a list of Capture Flows
		String[] CaptureFlowlist = null;
		//Set the URI to be the tables and just get the name of the CaptureFlows back
		String TablesUri = uri + "/tables/captureflows?view=Name";

		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(TablesUri);
			
			//Add the headers
			request.addHeader("Accept", "application/vnd.emc.captiva+json, application/json");
			//Not sure if this is how you set a cookie...
			request.addHeader("Cookie", "CPTV-TICKET="+ticket);
			
			//Nothing to construct here
			//GET
			HttpResponse response = client.execute(request);
			
			//Desierialize the message back
			String strResponse = EntityUtils.toString(response.getEntity(), "UTF8");
			Gson gsonResponse = new Gson();
			tablesResponse TablesResponse = gsonResponse.fromJson(strResponse, tablesResponse.class);
			
			//Now handle the two dimensional array
			
			//Pass to the temporary array
			//TablesFlowResponse = (String[][]) TablesResponse.content.rows;
			
			//Now loop round all of the returned rows
			int counter = 0;
			int numRows = 0;
			//numRows = TablesFlowResponse.length;
			numRows = TablesResponse.content.rows.length;
			//Set the array size
			CaptureFlowlist = new String[numRows];
			for (int i = 0; i < numRows; i ++ )
				if (TablesResponse.content.rows[i] != null)
				{   
					CaptureFlowlist[i] =TablesResponse.content.rows[i][0].toString();
					counter ++;
				}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			//When this works return the capture flows
        }
		
		return CaptureFlowlist;
	}
	private String Login() {
		//This uses the imported google JSON jar
		Gson gson = new Gson();
		DefaultHttpClient client = new DefaultHttpClient();
		//Add Session to the end
		String SessionURI = uri + "/session";
		//Set the type to Post
		HttpPost request = new HttpPost(SessionURI);
		//Construct the information
		request.addHeader("Content-Type","application/vnd.emc.captiva+json; charset=utf-8");
		request.addHeader("Accept", "application/vnd.emc.captiva+json, application/json");
		//Create the object and set the properties
		loginRequest loginRequest = new loginRequest();
		loginRequest.culture = "en-GB";
		loginRequest.username = user;
		loginRequest.password = password;
		//Get the DeviceID -- Not working properly yet..
		String androidID = Settings.Secure.ANDROID_ID;
		loginRequest.deviceId = androidID;
		
		//Serialise the JSON
		String json = gson.toJson(loginRequest);
		//Now try and post it
		try {
			request.setEntity(new ByteArrayEntity(json.toString().getBytes("UTF8")));
			HttpResponse response = client.execute(request);
			String strResponse = EntityUtils.toString(response.getEntity(), "UTF8");
			Gson gsonResponse = new Gson();
            loginResponse LoginResponse = null;
            try {
                LoginResponse = gsonResponse.fromJson(strResponse, loginResponse.class);
            } catch (Exception e) {
                //System.out.println("XXXX");
                Log.d("======> SEB", "Error reading return of REST call. Is your VPN up?");
            }

			//Get the ticket back
			// should test if strResponse not empty first!
			if (LoginResponse != null)
				ticket = LoginResponse.ticket;
			else {
				ticket = "null";
			}
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ticket;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ticket;
		}
		//Now get the ticket
		finally {
			
		}
		return ticket;
	}
		
	@Override
	protected void onPostExecute(Object result) {
		// TODO Auto-generated method stub
		if (dialog.isShowing()) {
			dialog.dismiss();
		}
		super.onPostExecute(result);
	}
	@Override
	protected Object doInBackground(Object... params) {
		// TODO Auto-generated method stub
		Boolean OK = false;
		OK = Connect();
		ConnectOK = OK;
		Completed = true;
		return null;
	}
}


