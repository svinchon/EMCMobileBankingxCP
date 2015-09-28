package ing.rbi.poc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


import com.google.gson.Gson;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.Property;

public class GetDocument extends AsyncTask  {
public String InputURI;
public Boolean Completed = false;
public String dlgMessage = "";
public String dlgTitle = "";
private class GetDocumentResponse {
	public String name;
	public Propterty properties;
	public link[] links;
}
private class Propterty {
	public String object_name;
	public String r_object_id;
	public Integer rendition;
	public String full_format;
	public String format;
	public Double full_content_size;
	public String set_time;
	public Integer i_vstamp;
	public String mime_type;
	public String dos_extension;
	public String format_name;
	public String[] parent_id;
	public Integer[] page;
	public String[] page_modifier;
}
private class link {
	public String rel;
	public String title;
	public String href;
}

private Context context;

public GetDocument(Context context){
    this.context = context;
}
	@Override
	protected Object doInBackground(Object... params) {
		// TODO Auto-generated method stub
		try {
			Get_Document();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Completed = true;
		return null;
	}
	
	private void Get_Document() throws ClientProtocolException, IOException {
		//Now create the http request
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(InputURI);
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
		//Now try to get the response
		try {
			HttpResponse response =client.execute(request);
			String strResponse = EntityUtils.toString(response.getEntity(), "UTF8");
			Gson gsonResponse = new Gson();
			GetDocumentResponse GetDocumentResponse = gsonResponse.fromJson(strResponse, GetDocumentResponse.class);
			String GetURL = GetDocumentResponse.links[2].href; 
			//Now download the document
			Download_Document(GetURL,GetDocumentResponse.properties.object_name,GetDocumentResponse.properties.full_format);
		}
		catch (ClientProtocolException e) {
			
		}
		finally {
	}
	}
	//Function to Download the document
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void Download_Document(String GetURL,String ObjectName, String FileExt) throws ClientProtocolException, IOException {
		
		//New
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(GetURL));
		request.setDescription("Downloading " + ObjectName);
		request.setTitle("Document Download");
		// in order for this if to run, you must use the android 3.2 to compile your app
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    request.allowScanningByMediaScanner();
		    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		}
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, ObjectName + "." + FileExt );
		// get download service and enqueue file
		
		DownloadManager manager = (DownloadManager) this.context.getSystemService(Context.DOWNLOAD_SERVICE);
		manager.enqueue(request);
	}
}
