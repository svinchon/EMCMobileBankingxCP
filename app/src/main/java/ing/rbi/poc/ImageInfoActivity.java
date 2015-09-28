/** -------------------------------------------------------------------------
* Copyright  2013 EMC Corporation.  All rights reserved.
---------------------------------------------------------------------------- */

package ing.rbi.poc;

import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import emc.captiva.mobile.sdk.CaptureImage;

/**
 * This activity displays the device and image properties.
 */
public class ImageInfoActivity extends Activity {	
	private TextView _deviceIDLabel = null;
	private TextView _widthLabel = null;
	private TextView _heightLabel = null;
	private TextView _channelsLabel = null;
	private TextView _bitsPerPixLabel = null;
	private TextView _versionLabel = null;
	
	/**
	 * This is the back button handler.
	 * @param view    The view for the control event.
	 */
	public void onBackButton(View view) {
	    // Finish the activity and return to the previous.
        finish();
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(ing.rbi.poc.R.layout.activity_imageinfo);
		
		// Retrieve the controls.
		_deviceIDLabel = (TextView) findViewById(ing.rbi.poc.R.id.DeviceIDLabel);
		_widthLabel = (TextView) findViewById(ing.rbi.poc.R.id.ImageWidthLabel);
		_heightLabel = (TextView) findViewById(ing.rbi.poc.R.id.ImageHeightLabel);
		_channelsLabel = (TextView) findViewById(ing.rbi.poc.R.id.ImageChannelsLabel);
		_bitsPerPixLabel = (TextView) findViewById(ing.rbi.poc.R.id.ImageBitsPerPixelsLabel);
		_versionLabel = (TextView) findViewById(ing.rbi.poc.R.id.VersionLabel);
		
		// Get the image properties.
		Map<String, Object> properties = CaptureImage.getImageProperties();
		int imageWidth = (Integer)properties.get(CaptureImage.IMAGE_PROPERTY_WIDTH);
		int imageHeight = (Integer)properties.get(CaptureImage.IMAGE_PROPERTY_HEIGHT);
		int channels = (Integer)properties.get(CaptureImage.IMAGE_PROPERTY_CHANNELS);
		int bitsPerPix = (Integer)properties.get(CaptureImage.IMAGE_PROPERTY_BITSPERPIXEL);

		// Assign the properties to the controls.
		_deviceIDLabel.setText(getString(ing.rbi.poc.R.string.ImageInfo_DeviceID) + " " + CaptureImage.getDeviceId());
		_widthLabel.setText(getString(ing.rbi.poc.R.string.ImageInfo_Width) +  " " + Integer.toString(imageWidth));
		_heightLabel.setText(getString(ing.rbi.poc.R.string.ImageInfo_Height) +  " " + Integer.toString(imageHeight));
		_channelsLabel.setText(getString(ing.rbi.poc.R.string.ImageInfo_Channels) +  " " + Integer.toString(channels));
		_bitsPerPixLabel.setText(getString(ing.rbi.poc.R.string.ImageInfo_BitsPerPixel) +  " " + Integer.toString(bitsPerPix));
		_versionLabel.setText(getString(ing.rbi.poc.R.string.ImageInfo_Version) + " " + CaptureImage.getVersion());
	}
}
