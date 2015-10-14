package ing.rbi.poc;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;


public class AsyncCreateBatch extends AsyncTask {
    public String ticket;
    public String BatchID;
    public String FlowSelected;
    public String URI;
    public String BatchName;
    private Context context;
    //private ProgressDialog dialog;
    public boolean Completed = false;
    public String dlgMessage = "";
    public String dlgTitle = "";

    //The Create Batch Class
    private class CreateBatchRequest {
        public String captureFlow;
        public String batchName;
        //Always set this to 3 for this application
        public Integer batchRootLevel = 3;
    }

    private class returnStatus {
        public Integer status;
        public String code;
        public String message;
        public String server;
    }

    private class link {
        public String rel;
        public String href;
    }

    private class Content {
        public String id;
        public String batchName;
        public String status;
        public String serverBatchId;
        public String captureFlow;
        public Integer batchRootLevel;
        public String lastUpdate;
        public String lastError;
    }

    private class CreateBatchResponse {
        public returnStatus returnStatus;
        public String id;
        public String title;
        public String updated;
        public link[] links;
        public Content content;
    }

    public AsyncCreateBatch(Context context) {
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
        CreateBatch();
        Completed = true;
        return null;
    }

    private void CreateBatch() {

        Gson gson = new Gson();
        //We can now start adding documents
        CreateBatchRequest BatchRequest = new CreateBatchRequest();
        BatchRequest.captureFlow = FlowSelected;
        //Set the batch name to be the CF and the next index from the server
        BatchRequest.batchName = FlowSelected + "_{NextIndex}";
        //Set the new URI
        String CBatchURI = URI + "/session/batches";
        //Now try an http post

        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(CBatchURI);
        request.addHeader("Content-Type", "application/vnd.emc.captiva+json; charset=utf-8");
        request.addHeader("Accept", "application/vnd.emc.captiva+json, application/json");
        request.addHeader("Cookie", "CPTV-TICKET=" + ticket);
        //Serialise the JSON
        String json = gson.toJson(BatchRequest);
        //Now Try to post
        boolean IsValidJSON = true;
        try {
            request.setEntity(new ByteArrayEntity(json.toString().getBytes("UTF8")));
            HttpResponse response = client.execute(request);
            delay(2);
            String strResponse = EntityUtils.toString(response.getEntity(), "UTF8");
            // TODO SEB test added to ensure server response is JSON before processing
            IsValidJSON = isValidJSON(strResponse);
            Gson gsonResponse = new Gson();
            CreateBatchResponse CreateBatchResponse = gsonResponse.fromJson(strResponse, CreateBatchResponse.class);
            BatchID = CreateBatchResponse.id.toString();
            BatchName = CreateBatchResponse.content.batchName.toString();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            dlgMessage = e.getMessage().toString();
            dlgTitle = "Unable to Create Batch";

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            dlgMessage = e.getMessage().toString();
            dlgTitle = "Unable to Create Batch";
        } catch (Exception e) {
            dlgMessage = "isValidJSON on Server response returned: " + IsValidJSON;
            //dlgMessage = e.getMessage().toString();
            dlgTitle = "Unable to Create Batch";
            e.printStackTrace();
        } finally {
        }

    }

    private void delay(int seconds) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean isValidJSON(String str) {
        try {
            new JSONObject(str);
        } catch (JSONException ex) {
            try {
                new JSONArray(str);
            } catch (JSONException ex1) {
                Log.e("ERROR", "isValidJSON returns FALSE");
                Log.e("ERROR", "Faulty input is: " + str);
                return false;
            }
        }
        return true;
    }

}
