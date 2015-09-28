package ing.rbi.poc;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class AddBackOfCheque extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(ing.rbi.poc.R.layout.activity_add_back_of_cheque);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(ing.rbi.poc.R.menu.add_back_of_cheque, menu);
		return true;
	}
	
	public void onAddBack (View view) {
		completeAndReturn(1);
	}
public void onNoAddBack (View view) {
	completeAndReturn(0);
	
	
	}
private void completeAndReturn(int resultCode) {
    // Pass back our result and finish.
    setResult(resultCode); 
    finish();
}


}
