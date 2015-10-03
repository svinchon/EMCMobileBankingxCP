/** -------------------------------------------------------------------------
* Copyright  2013 EMC Corporation.  All rights reserved.
---------------------------------------------------------------------------- */

package ing.rbi.poc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import emc.captiva.mobile.sdk.CaptureException;
import emc.captiva.mobile.sdk.CaptureImage;
import emc.captiva.mobile.sdk.QuadrilateralCropCallback;

/**
 * This activity provides the ability to enhance the image.
 */
public class EnhanceImageActivity extends Activity implements QuadrilateralCropCallback {
	private static String TAG = EnhanceImageActivity.class.getSimpleName();
	private static boolean _imgEdited = false;
	private PZImageView _imageView = null;
	private boolean _displayed = false;
	private String _filename = null;
	private ImageButton _backButton = null;
	private Button _undoButton = null;
	private ProgressBar _progressBar = null;	
	private Menu _menu = null;
	private RelativeLayout _enhanceLayout = null;
	private String SavedFileName = "";
	private Boolean IDDoc = false;

	@Override
	public void cropComplete(boolean cropped) {
		// If the image is cropped, display the 'UndoAll' button.
		if (cropped) {
			startEdit();
		}
	}

    /* (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
        
        // Handle the menu item selections.
    	Log.v(TAG, "Enhance Image Operation - " + item.getTitle());
        int menuID = item.getItemId();
		try {
			switch (menuID) {

                // TODO SEB call enhance method manually
				case ing.rbi.poc.R.id.EnhanceForMe: {
                    enhanceForMe();
    				break;
				}
				case ing.rbi.poc.R.id.ABQuadCrop: {
					// Get the parameters to set up the quadrilateral crop.
					SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this);
					String quadCropColor = "blue";
					String temp = "4";
					Integer quadCropLineWidth = CoreHelper.getInteger(temp, 4);
					temp = "24";
					Integer quadCropCircleRadius = CoreHelper.getInteger(temp, 24);
					Boolean quadCropShadeBackground = true;

					// Add the parameters to a HashMap for passing into the show call.
					HashMap<String, Object> quadCropParams = new HashMap<String, Object>();
					quadCropParams.put(CaptureImage.CROP_CONTEXT, this);
					quadCropParams.put(CaptureImage.CROP_COLOR, quadCropColor);
					quadCropParams.put(CaptureImage.CROP_LINE_WIDTH, quadCropLineWidth);
					quadCropParams.put(CaptureImage.CROP_CIRCLE_RADIUS, quadCropCircleRadius);
					quadCropParams.put(CaptureImage.CROP_SHADE_BACKGROUND, quadCropShadeBackground);

					// Start the Quadrilateral Crop activity.
					CaptureImage.showQuadrilateralCrop(this, quadCropParams);
					break;
				}
                case ing.rbi.poc.R.id.ABInfo: {
				   // Launch the image info activity.
				   Intent intent = new Intent(this, ImageInfoActivity.class);
			       startActivity(intent);
			       break;
				}
				case ing.rbi.poc.R.id.ABBlackWhite: {
				    // Apply the adaptive black and white filter.					
					applySlowFilter(CaptureImage.FILTER_ADAPTIVE_BINARY);					
					break;
				}
				case ing.rbi.poc.R.id.ABGray: {
				    // Apply the gray scale filter.
					applySlowFilter(CaptureImage.FILTER_GRAYSCALE);
					break;
				}
				case ing.rbi.poc.R.id.ABDeskew: {
				    // Apply the deskew/perspective filter.
					applySlowFilter(CaptureImage.FILTER_PERSPECTIVE);
					break;
				}
				case ing.rbi.poc.R.id.ABResize: {
				    // Resize the image to minus 200 pixels.
					startEdit();
					HashMap<String, Object> parameters = new HashMap<String, Object>();
					Map<String, Object> properties = CaptureImage.getImageProperties();
					int imageWidth = (Integer)properties.get(CaptureImage.IMAGE_PROPERTY_WIDTH);
					int imageHeight = (Integer)properties.get(CaptureImage.IMAGE_PROPERTY_HEIGHT);
					
					// Resize width and height to be 80% of original width and height.
					parameters.put(CaptureImage.FILTER_PARAM_RESIZE_WIDTH, (int)(imageWidth * 0.80));
					parameters.put(CaptureImage.FILTER_PARAM_RESIZE_HEIGHT, (int)(imageHeight * 0.80));
					
					CaptureImage.applyFilters(new String[] { CaptureImage.FILTER_RESIZE }, parameters);
					break;
				}
				case ing.rbi.poc.R.id.ABRotate180:
				case ing.rbi.poc.R.id.ABRotateLeft:
				case ing.rbi.poc.R.id.ABRotateRight: {
				    // Rotate the image.
					startEdit();				    
					HashMap<String, Object> parameters = new HashMap<String, Object>();
					parameters.put(CaptureImage.FILTER_PARAM_ROTATION_DEGREE, menuID == ing.rbi.poc.R.id.ABRotateLeft ? 270 : ( menuID == ing.rbi.poc.R.id.ABRotateRight ? 90 : 180));
					CaptureImage.applyFilters(new String[] { CaptureImage.FILTER_ROTATION }, parameters);
					_imageView.setImageBitmap(getImage());
					break;
				}
				case ing.rbi.poc.R.id.ABCrop: {

				    // Launch image cropping activity.
				    Intent intent = new Intent(this, EnhanceImageCropActivity.class);
				    startActivityForResult(intent, ing.rbi.poc.R.id.ABCrop);
					break;
				}
				
				case ing.rbi.poc.R.id.ABAutoCrop: {
				    // Apply the auto-cropping operation.
					startEdit();					
				    CaptureImage.applyFilters(new String[] { CaptureImage.FILTER_CROP }, getAutoCropPadding());
				    _imageView.setImageBitmap(getImage());
					break;
				}

				default: {
					return super.onOptionsItemSelected(item);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			CoreHelper.displayError(this, e);
		}
    	
		return true;
    }

    // TODO SEB enhance function
    private void enhanceForMe() {
        startEdit();

        // prefs
        SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this);

        Map<String, Object> properties = CaptureImage.getImageProperties();
        int imageWidth = (Integer)properties.get(CaptureImage.IMAGE_PROPERTY_WIDTH);
        int imageHeight = (Integer)properties.get(CaptureImage.IMAGE_PROPERTY_HEIGHT);

        HashMap<String, Object> parameters = new HashMap<String, Object>();

        int _boxWidthPercent = 90;
        int _boxWidth = imageWidth *_boxWidthPercent/100;
        float _boxWidthHeightRatio = 1.3f;
        int _boxHeight = (int)(_boxWidth / _boxWidthHeightRatio);
        int _left = (int)((imageWidth-_boxWidth)/2);
        int _top=(int)((imageHeight-_boxHeight)/2);
        int _right=(int)((imageWidth-_boxWidth)/2+_boxWidth);
        int _bottom=(int)((imageHeight-_boxHeight)/2+_boxHeight);
        Rect rect = new Rect(
                (int)(_left),
                (int)(_top),
                (int)(_right),
                (int)(_bottom)
        );
        parameters.put(CaptureImage.FILTER_PARAM_CROP_RECTANGLE, rect);
        CaptureImage.applyFilters(new String[] { CaptureImage.FILTER_CROP }, parameters);

        // auto crop
        //CaptureImage.applyFilters(new String[] { CaptureImage.FILTER_CROP }, getAutoCropPadding());

        // resize
        // current size
       //int imageHeight = (Integer)properties.get(CaptureImage.);
        Log.v(TAG, "Enhance Image Operation - " + "enhanceForMe" + " - " + imageWidth + "x" + imageHeight);
        // new size
        int targetWidth = 1024;
        int targetHeight = 768;
        boolean isPortrait;
        float format = (float)imageWidth/imageHeight;
        float target_max_format = (float)targetWidth/targetHeight;
        int imageWidthNew = targetWidth;
        int imageHeightNew = targetHeight;
        if (format > 1 ) {
            isPortrait = false;
            if (format > target_max_format) {
                Log.v(TAG, "Enhance Image Operation - " + "enhanceForMe" + " - landscape and large");
                imageWidthNew = targetWidth;
                imageHeightNew = imageHeight / imageWidth * targetHeight;
            } else {
                Log.v(TAG, "Enhance Image Operation - " + "enhanceForMe" + " - landscape and not large");
                imageHeightNew = targetHeight;
                imageWidthNew = imageWidth / imageHeight * targetWidth;
            }
        } else {
            isPortrait = true;
            if (format > target_max_format) {
                Log.v(TAG, "Enhance Image Operation - " + "enhanceForMe" + " - portrait");
                imageWidthNew = targetHeight;
                imageHeightNew = imageHeight / imageWidth * targetWidth;
            } else {
                Log.v(TAG, "Enhance Image Operation - " + "enhanceForMe" + " - portrait");
                imageHeightNew = targetWidth;
                imageWidthNew = imageWidth / imageHeight * targetHeight;
            }
        }
        //HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(CaptureImage.FILTER_PARAM_RESIZE_WIDTH, (int)(imageWidthNew));
        parameters.put(CaptureImage.FILTER_PARAM_RESIZE_HEIGHT, (int) (imageHeightNew));
        Log.v(TAG, "Enhance Image Operation - " + "enhanceForMe" + " - " + imageWidthNew + "x" + imageHeightNew);
        // apply
        CaptureImage.applyFilters(new String[]{CaptureImage.FILTER_RESIZE}, parameters);

        // deskew
        //applySlowFilter(CaptureImage.FILTER_PERSPECTIVE);

        // B & W
        //applySlowFilter(CaptureImage.FILTER_ADAPTIVE_BINARY);
    }

    /**
	 * The Undo All button handler. This will revert any changes made.
	 * @param view    The view for the control event.
	 */
	public void onUndoAll(View view) {
		// Cancel any edits, reload the image, and tell the view to refresh.
	    cancelEdit();
	    AddCheque._newLoad = true;
	    AddBill._newLoad = true;
	    AddPID._newLoad = true;
        ((MediaSelectedHandler)getMediaSelectedHandlerFromIntent()).setNewLoadForImage(true);
		loadImage();
		_imageView.invalidate();
	}
	
	/**
	 * The menu button handler. This will open the menu. 
	 * @param view    The view for the control event.
	 */
	public void onMenuButton(View view) {
		// Show the menu.
		this.openOptionsMenu();
	}

    /**
	 * The back button handler. This will handle saving behavior.
	 * @param view    The view for the control event.
	 */
	public void onBackButton(View view) {
	    // Re-use the back pressed logic.
		processEnhanceAndBack();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	private void galleryAddPic(String mCurrentPhotoPath) {
	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	    File f = new File(mCurrentPhotoPath);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    this.sendBroadcast(mediaScanIntent);
	}

    private void processEnhanceAndBack() {
        // If the image has not been edited, then leave without saving.
        if (!_imgEdited)
        {
           completeAndReturn(Constants.EVENT_ENHANCE_OPERATION_ENHANCE_NO, _filename);
           return;
        }

        // If the image has been edited, then save a copy of the image under a new filename to the gallery.
        try {
            //Show the progress bar
            String fullpath = saveCurrentImage();

// Let Android know so that it shows immediately in the image gallery. Note that TIFF
// images cannot be viewed by the Android gallery viewer as of 4.2.2. However, if you
// save a TIFF image to the gallery storage folder, it will still save and you can
// verify that it is there by using the Android "My Files" application if available
// on your device, or the Android Debug Bridge (adb). You can get the path to the file
// by debugging this application's save function.
if (fullpath != null) {
               galleryAddPic(fullpath);
//sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(new File(fullpath))));
}

// Finish this activity and return the result.
           completeAndReturn(RESULT_OK, SavedFileName);
       }
       catch (CaptureException e) {
           // If an exception happens we finish this activity and send back the cancel result when the error dialog is dimissed.
           DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                   dialog.dismiss();
                   completeAndReturn(RESULT_CANCELED, null);
               }
           };

CoreHelper.displayError(this, e, listener);
}
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu.
		if (IDDoc == true) {
			getMenuInflater().inflate(ing.rbi.poc.R.menu.activity_enhance_image, menu);
		}
		else{
			getMenuInflater().inflate(ing.rbi.poc.R.menu.activity_enhance_image_id, menu);
		}
		
		_menu = menu;
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	
		super.onWindowFocusChanged(hasFocus);
		
		// If we don't have the focus, then no need to do anything.
		if (!hasFocus) {
		    return;
		}
		
		// Handle refreshing of the image here.
		if (!_displayed && _filename != null) {
		    // We haven't loaded the image yet so do that now.
			loadImage();
			_displayed = true;
		} 
		else {
		    // Refresh our image as we have a UI state change.
		    _imageView.setImageBitmap(getImage());
		}

		// Update our UI to reflect edit changes.
		if (_imgEdited) {
		    startEdit();
		} 
		else {
			cancelEdit();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(ing.rbi.poc.R.layout.activity_enhance_image);

		// Get filename to load.
		Bundle b = getIntent().getExtras();
		IDDoc = b.getBoolean("IDDoc");
		_filename = b.getString("Filename");
		
		// Populate members.
		_imageView = (PZImageView) findViewById(ing.rbi.poc.R.id.ImageView);
		_progressBar = (ProgressBar) findViewById(ing.rbi.poc.R.id.ProgressStatusBar);
		_backButton = (ImageButton) findViewById(ing.rbi.poc.R.id.BackButton);
		_undoButton = (Button) findViewById(ing.rbi.poc.R.id.UndoAllButton);
		_enhanceLayout = (RelativeLayout) findViewById(ing.rbi.poc.R.id.EnhanceLayout);
		
		// Determine whether we are being launched for the first time or have been rotated
		// and then set the appropriate edit mode from the start.
		if ((AddCheque._newLoad == false)||(AddBill._newLoad ==false)||(AddPID._newLoad == false) && _imgEdited == true) {
            startEdit();
		} else {
            _imgEdited = false;
            setBackButton(false);
		}		
		
	}
	
	/**
	 * If crop applied then set edit mode = true;
	 * @param requestCode   The request code passed back from Crop.
	 * @param resultCode    The result code passed back from Crop.
	 * @param data 			Data passed back from Crop. 
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ing.rbi.poc.R.id.ABCrop && resultCode == RESULT_OK ) {
        	startEdit();
        }
    }
	
	/**
	 * Finish the activity based on the result code.
	 * @param resultCode    The result code to pass back.
	 */
	private void completeAndReturn(int resultCode, String fileNameToSend) {

       MediaSelectedHandler mediaSelectedHandler = getMediaSelectedHandlerFromIntent();
            mediaSelectedHandler.handlePostEnhanceImage(resultCode, fileNameToSend, this);
    }

    public void returnIntentResult(int resultCode, String fileNameToSend) {
        // Pass back our result and finish.
        Intent retData = new Intent();
        retData.putExtra("SavedFile", fileNameToSend);
        setResult(resultCode, retData);

        finish();
    }

    private MediaSelectedHandler getMediaSelectedHandlerFromIntent() {
        Intent orignalIntent = getIntent();
        String flowType = orignalIntent.getStringExtra(Constants.FLOW_TYPE_KEY);
        MediaSelectedHandler mediaSelectedHandler = null;

        if(flowType.equals(Constants.FLOW_PROOF_ID)){
            mediaSelectedHandler = ProofIDHandler.getProofIDHandler(null);
        }
        else if(flowType.equals(Constants.FLOW_ADD_BILL)){
            mediaSelectedHandler = BillHandler.getBillHandler(null);
        }
        else if(flowType.equals(Constants.FLOW_ADD_CHEQUE)){
            mediaSelectedHandler = ChequeHandler.getChequeHandler(null);
        }
		else if(flowType.equals(Constants.FLOW_SPAIN_ID)) {
			mediaSelectedHandler = SpainIDHandler.getIDHandler(null);
		}
		else if(flowType.equals(Constants.FLOW_INVOICE)) {
			mediaSelectedHandler = InvoiceHandler.getInvoiceHandler(null);
		}
        return mediaSelectedHandler;
    }

    /**
     * Will display an infinite progress bar if present.
     * @param enable        Pass <code>true</code> to display and <code>false</code> to hide.   
     */

	/**
	 * Set our edit mode and update the display to show that we have been edited.
	 */
	private void startEdit() {
		_imgEdited = true;
		_undoButton.setVisibility(View.VISIBLE);
		setBackButton(true);
	}

	/**
	 * Cancel our edit mode and update the display to show that nothing has been edited.
	 */
	private void cancelEdit() {
		_imgEdited = false;
		_undoButton.setVisibility(View.INVISIBLE);
		setBackButton(false);
	}
	
	/**
	 * Set the appropriate back button based on the display. This will either be just a back
	 * arrow or a back arrow plus a check mark to show that something has been edited.
	 * @param edit    The edit mode to use to setup the UI.
	 */
	private void setBackButton(Boolean edit) {
//	    if (edit) {
//			_backButton.setImageResource(R.drawable.prevsave_button);
//		}
//		else {
//			_backButton.setImageResource(R.drawable.prev_button);
//		}
        _backButton.setImageResource(ing.rbi.poc.R.drawable.i_ok);
	}

	/**
	 * Load the image for the filename passed into this activity.
	 */
	private void loadImage() {
	    
	    // If the file exists then load the image into the SDK and display it.
		File file = new File(_filename);
		if (file.exists()) {
            Log.v (TAG, "Loading File = " + _filename);
            try {
                // We only load a new image if we are being re-launched from the main
                // activity or if there was an undo performed. This prevents a rotation
                // of the device from losing all the changes since a rotation will
                // destroy the activity and call onCreate again.
                //if (AddCheque._newLoad || AddBill._newLoad || AddPID._newLoad) {
                MediaSelectedHandler mediaSelectedHandler = getMediaSelectedHandlerFromIntent();
                if(mediaSelectedHandler.isNewLoadForImage()){
                    CaptureImage.load(file.getAbsolutePath());
                    AddCheque._newLoad = false;
                    AddBill._newLoad = false;
                    AddPID._newLoad = false;
                    mediaSelectedHandler.setNewLoadForImage(false);
                }
                // TODO SEB call enhance method automatically
                // added for enhance to be called by default
                enhanceForMe();

				_imageView.setImageBitmap(getImage());
            }
            catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                CoreHelper.displayError(this, e);
            }
		}
	}
	
	/**
	 * When we retrieve the bitmap, we will attempt to save memory on the device by scaling the
	 * image to the size that will allow the image to fit into the image view. This does
	 * not affect the image actually loaded into the SDK. This only changes the image used
	 * to display. We could skip the scaling and just display the image as it, but large
	 * images could use up a lot of the memory on the device.
	 * @return    The bitmap to use for the display.
	 */
	private Bitmap getImage() {
	    
	    // Set the new rectangle to represent the new image's size.
        Rect rect = CoreHelper.calcImageSizePx(_imageView, 0);
	    
	    // Tell the SDK to scale the image to the new size.
		Bitmap bitmap = null;
		if (rect.width() > 0 && rect.height() > 0){
			bitmap = CaptureImage.getImageForDisplay(rect.width(), rect.height(), null);
		}
		
		// Return the newly scaled image.
		return bitmap;
	}	
	
	/**
	 * This function starts and asynchronous task to perform the operation.
	 * It will start a progress bar to display to the user while it is executing and
	 * then cancel the progress bar when finished. 
	 * @param operation    The filter operation to perform.
	 */
	private void applySlowFilter(String operation) {	
		final String op = operation;
		
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void> () {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					CaptureImage.applyFilters(new String[] { op }, null);
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
				}
				
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				
			    // Set the image, turn of progress bar, and enable controls.
			    _imageView.setImageBitmap(getImage());
				//setProgressBar(false);
				if (_menu != null) {
				    _menu.setGroupVisible(ing.rbi.poc.R.id.ABMainGroup, true);
				}
				
				_enhanceLayout.setEnabled(true);
				_undoButton.setEnabled(true);
		        _backButton.setEnabled(true);	
		        _imageView._preventGesture = false;
				startEdit();
			}			
		};
	
		// Disable controls while filter is applied.
		_imageView._preventGesture = true;
		if (_menu != null) {
		    // This will cause slight flicker on some devices.
		    _menu.setGroupVisible(ing.rbi.poc.R.id.ABMainGroup, false);
		}
		
		_enhanceLayout.setEnabled(false);
		_undoButton.setEnabled(false);
		_backButton.setEnabled(false);		
		//setProgressBar(true);
		task.execute((Void)null);	
	} 

	/**
	 * This function saves the currently loaded image to a new file in the gallery directory.
	 * @return                     The generated filename for this new file.
	 * @throws CaptureException    The type of exception that can be thrown from this save operation.
	 */
	private String saveCurrentImage() throws CaptureException {
	    String fileName = null;
		try {	
		    // Get the preferences for saving.            
            SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this);
            String imgFormat = gprefs.getString(CoreHelper.getStringResource(this, ing.rbi.poc.R.string.GPREF_IMAGEFORMAT), CaptureImage.SAVE_JPG);
            String temp = gprefs.getString(CoreHelper.getStringResource(this, ing.rbi.poc.R.string.GPREF_JPGQUALITY), "95");
            Integer jpgQuality = CoreHelper.getInteger(temp, 95);
            temp = gprefs.getString(CoreHelper.getStringResource(this, ing.rbi.poc.R.string.GPREF_DPIX), "0");
            Integer dpix = CoreHelper.getInteger(temp, 0);
            temp = gprefs.getString(CoreHelper.getStringResource(this, ing.rbi.poc.R.string.GPREF_DPIY), "0");
            Integer dpiy = CoreHelper.getInteger(temp, 0);
            
            // Prepare a new filename.
            File fullpath = new File(CoreHelper.getImageGalleryPath(), CoreHelper.getUniqueFilename("Img", "." + imgFormat));
		    fileName = fullpath.toString();
		    
		    // Normalize the extension to the formats we support.
		    if (imgFormat.compareToIgnoreCase(CaptureImage.SAVE_JPG) != 0 && 
		    		imgFormat.compareToIgnoreCase(CaptureImage.SAVE_PNG) != 0 && 
		    		imgFormat.compareToIgnoreCase(CaptureImage.SAVE_TIF) != 0) {
		        imgFormat = CaptureImage.SAVE_JPG;
		    }
            
            // Build the save parameters.
            HashMap<String, Object> parameters = new HashMap<String, Object>();
            if (dpix > 0) {
                parameters.put(CaptureImage.SAVE_DPIX, dpix);
            }
            
            if (dpiy > 0) {
                parameters.put(CaptureImage.SAVE_DPIY, dpiy);
            }
            
		    if (imgFormat.compareToIgnoreCase(CaptureImage.SAVE_JPG) == 0) {
		        parameters.put(CaptureImage.SAVE_JPG_QUALITY, jpgQuality);
		    }
		    
            // Save the file.
		    CaptureImage.saveToFile(fileName, imgFormat, parameters);
		}
	    catch (CaptureException e) {
	    	Log.e(TAG, e.getMessage(), e);
	    	throw e;
	    }
		SavedFileName = fileName;
		return fileName;
	}
	
	/**
	 * Retrieve preferences for autocrop padding.
	 * @return The padding value in a Map.
	 */
	private Map<String, Object> getAutoCropPadding() {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
        SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this);
        String paddingText = gprefs.getString(CoreHelper.getStringResource(this, ing.rbi.poc.R.string.GPREF_FILTER_CROP_PADDING), "0.0");
		float padding = CoreHelper.getFloat(paddingText, 0.0f);
		
		parameters.put(CaptureImage.FILTER_PARAM_CROP_PADDING, padding);
		
		return parameters;
	}
}
