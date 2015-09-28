/** -------------------------------------------------------------------------
* Copyright  2013 EMC Corporation.  All rights reserved.
---------------------------------------------------------------------------- */

package ing.rbi.poc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import emc.captiva.mobile.sdk.CaptureImage;

/**
 * A class that provides static helpers for common needs.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public final class CoreHelper {
    
    // Used to help compute a unique image filename.
    private static int _imageCounter = 0;
    
    /**
     * This function attempts to generate a unique filename.
     * @param prefix      The prefix for the filename.
     * @param extension   The extension for the filename.
     * @return            The filename as a string.
     */
    @SuppressLint("SimpleDateFormat")
    public static String getUniqueFilename(String prefix, String extension) {
        
        if (prefix == null) {
            prefix = "";
        }
        
        if (extension == null) {
            extension = "";
        }
        
        String timeStamp = new SimpleDateFormat("_yyyyMMdd_HHmmss").format(new Date());
        String tempFilename = prefix + timeStamp + String.valueOf(++_imageCounter) + extension;
        return tempFilename;
    }
    
    /**
     * This function returns the path to the image gallery.
     * @return    The File representing the path to the gallery.
     */
    public static File getImageGalleryPath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    }    
        
    /**
     * This function saves a file from an input stream.
     * @param inputStream    The input stream object to use to generate the file.
     * @param fullPath       The path to the location of where to write the file.
     * @throws IOException   The kind of exception that may be thrown from this function.
     */
    public static void saveFile(InputStream inputStream, File fullPath) throws IOException {
        byte[] buffer = new byte[1000];
        OutputStream ouputstream = new FileOutputStream(fullPath, false);
        int ret = 0, offset = 0;
        while (true) {
            ret = inputStream.read(buffer, 0, 1000);
            if (ret <= 0) {
                break;
            }
            
            ouputstream.write(buffer, 0, ret);
            offset = offset + ret;
        }
        
        ouputstream.flush();
        ouputstream.close();
        inputStream.close();
    }
    
    /**
     * This function displays a simple error message box from an exception.
     * @param context    The context to use as the basis for the generation of the message box.
     * @param exception  The exception to use to generate the message. 
     */
    public static void displayError(Activity context, Exception exception) {
    	displayError(context, exception.getMessage(), null);
    }
    
    /**
     * This function displays a simple error message box from a String.
     * @param context    The context to use as the basis for the generation of the message box.
     * @param message	 The String to use to generate the message.
     */
    public static void displayError(Activity context, String message) {
    	displayError(context, message, null);
    }
           
    /**
     * This function displays a simple error message box from an exception.
     * @param context    The context to use as the basis for the generation of the message box.
     * @param exception  The exception to use to generate the message.
     * @param listener	 Click listener for BUTTON_POSITIVE.
     */
    public static void displayError(Activity context, Exception exception, DialogInterface.OnClickListener listener) {
    	displayError(context, exception.getMessage(), listener);
    }
    
    /**
     * This function displays a simple error message box from a String.
     * @param context    The context to use as the basis for the generation of the message box.
     * @param message	 The String to use to generate the message.
     * @param listener	 Click listener for BUTTON_POSITIVE. 
     */
    public static void displayError(Activity context, String message, DialogInterface.OnClickListener listener) {    
    	final Context c = context;
    	final DialogInterface.OnClickListener l = listener;
    	final String m = message;
    	
    	context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
		    	AlertDialog dialog = new AlertDialog.Builder(c)
	    		.setTitle("Error")
	    		.setMessage(m)
	    		.setCancelable(false)    		
	    		.create();    	

		    	dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", l);
		    	dialog.show();
			}    	
    	});
    }
    
    /**
     * A simple helper function to generate a String from a content Uri.
     * @param context     The context to use as the basis for the generation of the message box.
     * @param uri     The Uri object to use to generate the String.
     * @return        The String generated from the Uri.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getFilePathFromContentUri(Context context, Uri uri) {
        String filePath = null;
        if (uri != null && "content".equals(uri.getScheme())) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && isMediaDocument(uri)){ // && isMediaDocument(uri)
                String wholeID = DocumentsContract.getDocumentId(uri);

                // Split at colon, use second item in the array
                String id = wholeID.split(":")[1];

                String[] column = { MediaStore.Images.Media.DATA };

                // where id is equal to
                String sel = MediaStore.Images.Media._ID + "=?";

                Cursor cursor = context.getContentResolver().
                        query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                column, sel, new String[]{ id }, null);



                int columnIndex = cursor.getColumnIndex(column[0]);

                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(columnIndex);
                }
                cursor.close();
                return filePath;
            }
            else {
                Cursor cursor = context.getContentResolver().query(uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
                cursor.moveToFirst();
                filePath = cursor.getString(0);
                cursor.close();
            }

            return filePath;
        }

        return uri.getPath();
    }
    private static boolean isMediaDocument(Uri uri)
    {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    /**
     * Attempts to return an extension as a suffix of a file.
     * @param filePath    The file path to use.
     * @return            The extension suffix if one exists or the empty string.
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static String getFileExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        
        int index = filePath.lastIndexOf('.');
        if (index == -1) {
            return "";
        }
        
        return filePath.substring(index);
    }
    
    /**
     * A simple helper function for retrieving resources.
     * @param context    The context object to use for the call.
     * @param resId      The resource ID to fetch.
     * @return           The string representation for the resource or empty string if it doesn't exist.
     */
    public static String getStringResource(Context context, int resId) {
        if (context != null) {
            return context.getString(resId);
        }
        
        return "";
    }
    
    /**
     * Parses string as a signed integer.
     * @param data			String holding the signed integer representation.
     * @param defaultValue	Value to return if conversion fails.
     * @return				Converted value.
     */
    public static int getInteger(String data, int defaultValue) {
    	int returnValue;
    	
    	try {
    		returnValue = Integer.parseInt(data);
    	} catch (NumberFormatException exception) {
    		returnValue = defaultValue;
    	}
    	
    	return returnValue;
    }
    
    /**
     * Parses string as a float.
     * @param data          String holding the float representation.
     * @param defaultValue  Value to return if conversion fails.
     * @return              Converted value.
     */
    public static float getFloat(String data, float defaultValue) {
        float returnValue;
        
        try {
            returnValue = Float.parseFloat(data);
        } catch (NumberFormatException exception) {
            returnValue = defaultValue;
        }
        
        return returnValue;
    }
    
    /**
     * The function serves as a helper function to retrieve all of the take picture preferences.
     * @param ctx    The context object to use for the call.
     * @return       The parameters retrieved from the settings preferences for taking a picture.
     */
    public static HashMap<String, Object> getTakePictureParametersFromPrefs(Context ctx) {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(CaptureImage.PICTURE_CONTEXT, ctx);
        if (ctx != null) {
            // Retrieve user's preferences.
            SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            boolean lightSensor = gprefs.getBoolean(CoreHelper.getStringResource(ctx, ing.rbi.poc.R.string.GPREF_SENSOR_LIGHT), true);
            boolean motionSensor = gprefs.getBoolean(CoreHelper.getStringResource(ctx, ing.rbi.poc.R.string.GPREF_SENSOR_MOTION), true);
            boolean focusSensor = gprefs.getBoolean(CoreHelper.getStringResource(ctx, ing.rbi.poc.R.string.GPREF_SENSOR_FOCUS), true);
            boolean guideLines = gprefs.getBoolean(CoreHelper.getStringResource(ctx, ing.rbi.poc.R.string.GPREF_GUIDELINES), false);
            boolean optimalCondReq = gprefs.getBoolean(CoreHelper.getStringResource(ctx, ing.rbi.poc.R.string.GPREF_OPTIMALCONDREQ), false);
            boolean cancelBtn = gprefs.getBoolean(CoreHelper.getStringResource(ctx, ing.rbi.poc.R.string.GPREF_CANCEL), false);
            String temp = gprefs.getString(CoreHelper.getStringResource(ctx, ing.rbi.poc.R.string.GPREF_SENSOR_LIGHT_VALUE), "50");
            Integer lightSensitivity = getInteger(temp, 50);
            temp = gprefs.getString(CoreHelper.getStringResource(ctx, ing.rbi.poc.R.string.GPREF_SENSOR_MOTION_VALUE), ".30");
            Float motionSensitivity = getFloat(temp, .30f);
                    
            // Timer preferences
            temp = gprefs.getString(CoreHelper.getStringResource(ctx, ing.rbi.poc.R.string.GPREF_CAPTUREDELAY), "");
            Integer captureDelayMs = getInteger(temp, 500);
            temp = gprefs.getString(CoreHelper.getStringResource(ctx, ing.rbi.poc.R.string.GPREF_CAPTURETIMEOUT), "");
            Integer captureTimeoutMs = getInteger(temp, 0);
            
            // Label preferences.
            String edgeLabel = gprefs.getString(CoreHelper.getStringResource(ctx, ing.rbi.poc.R.string.GPREF_EDGELABEL), "");
            String centerLabel = gprefs.getString(CoreHelper.getStringResource(ctx, ing.rbi.poc.R.string.GPREF_CENTERLABEL), "");
            String captureLabel = gprefs.getString(CoreHelper.getStringResource(ctx, ing.rbi.poc.R.string.GPREF_CAPTURINGLABEL), "");
            
            // Set preferences into parameters map.
            // LABELS
            parameters.put(CaptureImage.PICTURE_LABEL_EDGE, edgeLabel);
            parameters.put(CaptureImage.PICTURE_LABEL_CENTER, centerLabel);
            parameters.put(CaptureImage.PICTURE_LABEL_CAPTURE, captureLabel);
            
            // SENSORS:
            temp = "";
            temp = lightSensor == true ? temp.concat("l") : temp;
            temp = motionSensor == true ? temp.concat("m") : temp;
            temp = focusSensor == true ? temp.concat("f") : temp;
            if (temp != null && !temp.isEmpty()) {
                parameters.put(CaptureImage.PICTURE_SENSORS, temp);
            } else {
                parameters.put(CaptureImage.PICTURE_SENSORS, "");
            }
            
            parameters.put(CaptureImage.PICTURE_SENSITIVITY_LIGHT, lightSensitivity);
            parameters.put(CaptureImage.PICTURE_SENSITIVITY_MOTION, motionSensitivity);
            
            // GUIDELINES
            if (guideLines) {
                parameters.put(CaptureImage.PICTURE_GUIDELINES, true);
            } else {
                parameters.put(CaptureImage.PICTURE_GUIDELINES, false);
            }
            
            // CAPTUREDELAY
            parameters.put(CaptureImage.PICTURE_CAPTURE_DELAY, captureDelayMs);
            
            // CAPTURETIMEOUT
            parameters.put(CaptureImage.PICTURE_CAPTURE_TIMEOUT, captureTimeoutMs);
            
            // OPTIMALCONDREQ
            if (optimalCondReq) {
                parameters.put(CaptureImage.PICTURE_OPTIMAL_CONDITIONS, true);
            } else {
                parameters.put(CaptureImage.PICTURE_OPTIMAL_CONDITIONS, false);
            }
            
            // CANCEL BUTTON  
            if (cancelBtn) {
                parameters.put(CaptureImage.PICTURE_BUTTON_CANCEL, true);
            } else {
                parameters.put(CaptureImage.PICTURE_BUTTON_CANCEL, false);
            }
        }
        
        // Return parameters.
        return parameters;
    }
    
    /**
     * When we retrieve the bitmap, we will attempt to save memory on the device by scaling the
     * image to the size that will allow the image to fit into the image view. This does
     * not affect the image actually loaded into the SDK. This only changes the image used
     * to display. We could skip the scaling and just display the image as it, but large
     * images could use up a lot of the memory on the device.
     * @param view            The view to use to determine the display size.
     * @param heightOffset    The amount to subtract from the height for determining the display height.
     * @return                The new calculated display size for the image.
     */
    public static Rect calcImageSizePx(View view, int heightOffset) {
        
        // Get the size of the image loaded in the SDK.
        Map<String, Object> properties = CaptureImage.getImageProperties();
        float imageWidth = (Integer)properties.get(CaptureImage.IMAGE_PROPERTY_WIDTH);
        float imageHeight = (Integer)properties.get(CaptureImage.IMAGE_PROPERTY_HEIGHT);
        
        // Get the size of the available display minus the top and bottom bars.
        float availWidth = view.getWidth();
        float availHeight = view.getHeight() - heightOffset; 
        
        float ratioWidth = imageWidth / availWidth;
        float ratioHeight = imageHeight / availHeight;
        
        // Modify the height or width depending on the bigger ratio
        if (ratioWidth > ratioHeight) {
        	availHeight = imageHeight / ratioWidth;        	
        } else {
        	availWidth = imageWidth / ratioHeight;
        }
                
        // Set the new rectangle to represent the new image's size and return it.
        Rect rect = new Rect(0, 0, (int)availWidth, (int)availHeight);
        return rect;
    }
    public static boolean license(Context context) {
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);        
        //String license = preferences.getString(CoreHelper.getStringResource(context, R.string.GPREF_LICENSE), "");
        //String applicationId = preferences.getString(CoreHelper.getStringResource(context, R.string.GPREF_APPLICATIONID), "");
        String applicationId = "EMC World 2014 Demo";
        String license = ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Product=Captiva Mobile SDK, CreateDate=2014-04-23 14:44:54, KeyId=c340-fe9a-ae78, Sales Order No=EMC Internal, Customer Name=EMC Internal DEV, Application Name=EMC World 2014 Demo >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*!0834204886454239320/dVVgd1hAPj0XzDFEdkg+yPbWOR/a8apYFRHv3zDXnVNK2j0GY0ES0KsZxNTy6pUWZw0CY1Mof5Wk1PLqlRZnDQJjUyh/ydeE+dWm1PLqlQhmCynzgXkV7p2HFVpEOA==*>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>";		
        if (license.length() <= 0 || applicationId.length() <= 0) {
        	return false;
        }
        
        return CaptureImage.addLicenseKey(applicationId, license);
    }
}
