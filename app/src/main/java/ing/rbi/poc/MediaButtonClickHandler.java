package ing.rbi.poc;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import emc.captiva.mobile.sdk.CaptureImage;
import emc.captiva.mobile.sdk.PictureCallback;

/**
 * Created by tejas on 26/04/15.
 */
public class MediaButtonClickHandler implements View.OnClickListener, PictureCallback {
    private WeakReference<Context> contextWeakReference;
    private MediaSelectedHandler mediaSelectedHandler;
    private static String TAG = MediaButtonClickHandler.class.getSimpleName();
    private String FileName = "";
    private boolean _newLoad = true;
    private country_list fragment;
    String myFlowType;


    public MediaButtonClickHandler(Context context, MediaSelectedHandler handler, country_list fragment, String flowType) {
        super();
        contextWeakReference = new WeakReference<Context>(context);
        mediaSelectedHandler = handler;
        this.fragment = fragment;
        myFlowType = flowType;
    }

    @Override
    public void onClick(View view) {
        Button clickedButton = (Button) view;
        if(clickedButton.getText().equals("Camera")){
            handleCamera();
        }
        else if(clickedButton.getText().equals("Gallery")){
            handleGallery();
        }
    }

    private void handleGallery() {
        //This means that the button was pressed, we're now going to display a gallery which will allow users to select an image
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        fragment.startActivityForResult(Intent.createChooser(intent, "Select Image"), Constants.EVENT_CHOOSE_IMAGE);
    }

    private void handleCamera() {
        // Launch the camera to take a picture.
        HashMap<String, Object> parameters = CoreHelper.getTakePictureParametersFromPrefs(contextWeakReference.get());
        CaptureImage.takePicture(this, parameters);
    }

    @Override
    public void onPictureCanceled(int reason) {
        // TODO Auto-generated method stub
        // This callback will be called if the take picture operation was canceled.
        if (reason == PictureCallback.REASON_OPTIMAL_CONDITIONS) {
            CoreHelper.displayError((Activity) contextWeakReference.get(), "The optimal conditions were not met and the picture was canceled.");
        } else if (reason == PictureCallback.REASON_CAMERA_ERROR) {
            CoreHelper.displayError((Activity) contextWeakReference.get(), "An error occurred while accessing the camera.");
        }
    }

    @Override
    public void onPictureTaken(byte[] imageData) {
        // TODO Auto-generated method stub
        // Use our utility functions to obtain a unique filename to store into the image gallery.
        File fullpath = new File(CoreHelper.getImageGalleryPath(), CoreHelper.getUniqueFilename("Img", ".JPG"));
        try {
            // Use our utility function to save this JPG encoded byte array to storage.
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
            CoreHelper.saveFile(inputStream, fullpath);

            // Get a URI to broadcast and let Android know there is a new image in the gallery.
            Uri uri = Uri.fromFile(fullpath);
            //This is a security issue in KitKat
            //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, uri));
            galleryAddPic(fullpath.toString());
            // Send the new picture taken to the enhancement screen so that users can modify it if necessary.
           gotoEnhanceImage(uri);
        }
        catch (IOException e) {
            // Log a message and display an error using our utility function.
            Log.e(TAG, e.getMessage(), e);
            CoreHelper.displayError((Activity) contextWeakReference.get(), "Could not save the image to the gallery.");
        }
    }

    public void galleryAddPic(String mCurrentPhotoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        contextWeakReference.get().sendBroadcast(mediaScanIntent);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void gotoEnhanceImage(Uri uri) {
        // If we have a file, then send it to enhancement.
        if (uri != null) {
            String filepath =  CoreHelper.getFilePathFromContentUri(fragment.getActivity(), uri);
            FileName = filepath;
            Intent intent = new Intent(contextWeakReference.get(), EnhanceImageActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Constants.FILE_NAME, filepath);
            intent.putExtra(Constants.ID_DOC, true);
            intent.putExtra(Constants.FLOW_TYPE_KEY, myFlowType);
            intent.putExtra(Constants.LOAD_IMAGE_NEW, true);
            mediaSelectedHandler.setNewLoadForImage(true);
            fragment.startActivityForResult(intent, Constants.EVENT_ENH_OPER);
            //Clear the Intent
            intent.setAction("");

        }
    }

}
