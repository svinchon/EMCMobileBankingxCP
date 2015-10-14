package ing.rbi.poc;

import android.app.Activity;

import java.io.Serializable;

public interface MediaSelectedHandler extends Serializable{
public boolean isNewLoadForImage();
public void setNewLoadForImage(boolean shouldLoadNew);
public MediaButtonClickHandler getMediaButtonClickHandler();
public void handlePostEnhanceImage(int enhanceResultCode, String filePath, Activity currentActivity);
public void setupUIAfterEnhanceImage();
public boolean ifBatchUpdateInProgress();
}
