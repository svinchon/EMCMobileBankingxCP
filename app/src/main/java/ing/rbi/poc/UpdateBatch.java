package ing.rbi.poc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


import com.google.gson.Gson;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;

public class UpdateBatch extends AsyncTask {
private Context context;
//private ProgressDialog dialog;
public boolean Completed = false;
public String BatchID;
public String URI;
public String ticket;
public String docValue;
public String docReference;
public String FileName;
public String FileName2;
public String DefaultAccountName;
public String DefaultCustomerName;
public boolean BatchUpdated = false;
private class UpdateBatchRequest {
	public String dispatch;
	public node[] nodes = new node[2];
	public value[] values = new value[7];
}
private class node {
	public Integer nodeId;
	public Integer parentId;
}
private class value {
	public Integer nodeId;
	public String valueName;
	public Object value;
	public String valueType;
	public Integer offest;
	public String fileExtension;
}
private class returnStatus{
	public Integer status;
	public String code;
	public String message;
	public String server;
}
public UpdateBatch(Context context){
    this.context = context;
}
@Override
protected void onPreExecute() {
//    dialog = new ProgressDialog(context);
//    dialog.setMessage("Please wait...");
//    dialog.setIndeterminate(true);
//    dialog.show();
    super.onPreExecute();
} 

@Override
protected void onPostExecute(Object result) {
	// TODO Auto-generated method stub
//	if (dialog.isShowing()) {
//		dialog.dismiss();
//	}
	super.onPostExecute(result);
}
	@Override
	protected Object doInBackground(Object... params) {
		// TODO Auto-generated method stub
		Update_Batch();
		Completed = true;
		return null;
	}
	private void Update_Batch() {
		Gson gson = new Gson();
		String UpdateBatchURI;
		UpdateBatchURI = BatchID;
		UpdateBatchRequest Batch = new UpdateBatchRequest();
		Batch.dispatch = "S";
		node ChequeNode = new node();
		node RootNode = new node();
		
		RootNode.parentId = 0;
		RootNode.nodeId = 1;
		//Create the cheque node
		ChequeNode.parentId = 1;
		ChequeNode.nodeId = 2;
		//Add the cheque node to the request
		Batch.nodes[0] = RootNode;
		Batch.nodes[1] = ChequeNode;		
		//Now add the values
		//Cheque Amount
		value DocumentValue = new value();
		DocumentValue.nodeId = 2;
		DocumentValue.valueName = "DocumentReference";
		DocumentValue.value = docReference;
		DocumentValue.valueType = "string";
		
		//Now add the image
		value DocumentImage = new value();
		DocumentImage.nodeId = 2;
		DocumentImage.valueName = "DocumentImage";
		DocumentImage.valueType = "file";
		DocumentImage.offest = 0;
		
		//Now add the Batch ID
		value VBatchID = new value();
		VBatchID.nodeId = 2;
		VBatchID.valueName = "BatchID";
		VBatchID.valueType = "string";
		String StrBatchID ="";
		Integer pos;
		String pointer;
		pos = BatchID.lastIndexOf("/") + 1;
				
		StrBatchID = BatchID.substring(pos);
		VBatchID.value = StrBatchID;
		
		DocumentImage.fileExtension = FileName.substring(FileName.lastIndexOf(".")+1);
		//Now Add the file Extension as that doesn't work
		//Now add the file extension
		value FileExtension = new value();
		FileExtension.nodeId = 2;
		FileExtension.valueName = "FileExtension";
		FileExtension.valueType = "string";
		FileExtension.value = DocumentImage.fileExtension;
		//Convert the file to base64 String
		InputStream inputStream;
		String encodedString = "";
		try {
			inputStream = new FileInputStream(FileName);
			byte[] bytes;
	        File file = new File(FileName);
	        byte[] buffer = new byte[(int) file.length()];
	        int bytesRead;
	        ByteArrayOutputStream output = new ByteArrayOutputStream();
	        try {
	            while ((bytesRead = inputStream.read(buffer)) != -1) {
	            output.write(buffer, 0, bytesRead);
	        }
	        } catch (IOException e) {
	        e.printStackTrace();
	        }
	        bytes = output.toByteArray();
	        encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		DocumentImage.value = encodedString;
		Batch.values[0] = DocumentValue;
		Batch.values[1] = DocumentImage;
		Batch.values[2] = FileExtension;
		Batch.values[3] = VBatchID; 
		//Now add the values from the preferences
		value CustomerName = new value();
		CustomerName.nodeId = 2;
		CustomerName.valueName = "CustomerName";
		CustomerName.valueType = "string";
		CustomerName.value = DefaultCustomerName;
		value AccountName = new value();
		AccountName.nodeId = 2;
		AccountName.valueName = "AccountName";
		AccountName.valueType = "string";
		AccountName.value = DefaultAccountName;
		Batch.values[4] = CustomerName;
		Batch.values[5] = AccountName;
		//Now add the document Reference
		value DRef = new value();
		DRef.nodeId = 2;
		DRef.valueName = "DocumentReference";
		DRef.valueType = "string";
		DRef.value = docReference;
		Batch.values[6] = DRef;
		if (FileName2 != null) {
			//REdim Batch.values
			value[] tvalues = new value[14];
			if (Batch.values!= null) {
				System.arraycopy(Batch.values, 0, tvalues, 0,7);
			    Batch.values = tvalues;
			}
			
		 //Now add the back image of the cheque
			//Create the back cheque node
			node BackChequeNode = new node();
			BackChequeNode.parentId = 1;
			BackChequeNode.nodeId = 3;
			// Redim the nodes array
			node[] tnodes = new node[3];
			if (Batch.nodes != null) {
				System.arraycopy(Batch.nodes, 0, tnodes, 0, 2);
				Batch.nodes = tnodes;
			}
			Batch.nodes[2] = BackChequeNode;
			//Now add the values
			//Cheque Amount
			value DocumentValue_Back = new value();
			DocumentValue_Back.nodeId = 3;
			DocumentValue_Back.valueName = "DocumentReference";
			DocumentValue_Back.value = docReference;
			DocumentValue_Back.valueType = "string";
			
			//Now add the image
			value DocumentImage_Back = new value();
			DocumentImage_Back.nodeId = 3;
			DocumentImage_Back.valueName = "DocumentImage";
			DocumentImage_Back.valueType = "file";
			DocumentImage_Back.offest = 0;
			
			//Now add the Batch ID
			value VBatchID_Back = new value();
			VBatchID_Back.nodeId = 3;
			VBatchID_Back.valueName = "BatchID";
			VBatchID_Back.valueType = "string";
					
			
			VBatchID_Back.value = StrBatchID;
			
			DocumentImage_Back.fileExtension = FileName2.substring(FileName.lastIndexOf(".")+1);
			//Now Add the file Extension as that doesn't work
			//Now add the file extension
			value FileExtension_Back = new value();
			FileExtension_Back.nodeId = 3;
			FileExtension_Back.valueName = "FileExtension";
			FileExtension_Back.valueType = "string";
			FileExtension_Back.value = DocumentImage_Back.fileExtension;
			//Convert the file to base64 String
			try {
				inputStream = new FileInputStream(FileName2);
				byte[] bytes;
		        File file = new File(FileName2);
		        byte[] buffer = new byte[(int) file.length()];
		        int bytesRead;
		        ByteArrayOutputStream output = new ByteArrayOutputStream();
		        try {
		            while ((bytesRead = inputStream.read(buffer)) != -1) {
		            output.write(buffer, 0, bytesRead);
		        }
		        } catch (IOException e) {
		        e.printStackTrace();
		        }
		        bytes = output.toByteArray();
		        encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			DocumentImage_Back.value = encodedString;
			Batch.values[7] = DocumentValue_Back;
			Batch.values[8] = DocumentImage_Back;
			Batch.values[9] = FileExtension_Back;
			Batch.values[10] = VBatchID_Back; 
			//Now add the values from the preferences
			value CustomerName_Back = new value();
			CustomerName_Back.nodeId = 3;
			CustomerName_Back.valueName = "CustomerName";
			CustomerName_Back.valueType = "string";
			CustomerName_Back.value = DefaultCustomerName;
			value AccountName_Back = new value();
			AccountName_Back.nodeId = 3;
			AccountName_Back.valueName = "AccountName";
			AccountName_Back.valueType = "string";
			AccountName_Back.value = DefaultAccountName;
			Batch.values[11] = CustomerName_Back;
			Batch.values[12] = AccountName_Back;
			//Now add the document Reference
			DRef.nodeId = 3;
			DRef.valueName = "DocumentReference";
			DRef.valueType = "string";
			DRef.value = docReference;
			Batch.values[13] = DRef;
			
		}
		//Now Construct the POST
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(UpdateBatchURI);
		request.addHeader("Content-Type","application/vnd.emc.captiva+json; charset=utf-8");
		request.addHeader("Accept", "application/vnd.emc.captiva+json, application/json");
		request.addHeader("Cookie", "CPTV-TICKET="+ticket);
		//Serialise the JSON
		String json = gson.toJson(Batch);
		//Now try to post
		
		//Now Try to post
				try {
					request.setEntity(new ByteArrayEntity(json.toString().getBytes("UTF8")));
					HttpResponse response =client.execute(request);
					String strResponse = EntityUtils.toString(response.getEntity(), "UTF8");
					Gson gsonResponse = new Gson();
					returnStatus returnStatusResponse = gsonResponse.fromJson(strResponse, returnStatus.class);
					BatchUpdated = true;
				}
				catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					BatchUpdated = false;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					BatchUpdated = false;
				}
				finally {
					Completed = true;
				}
	}
	

}
