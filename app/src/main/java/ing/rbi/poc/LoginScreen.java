package ing.rbi.poc;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class LoginScreen extends Activity {
    String[] LoginCaptureFlows = null;
    String PrefURI = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ing.rbi.poc.R.layout.activity_login_screen);
        // License the application
        CoreHelper.license(this);
        EditText userField = ((EditText) findViewById(ing.rbi.poc.R.id.txt_username));
        userField.setText("Gareth");
        EditText passwordField = (EditText) findViewById(ing.rbi.poc.R.id.txt_password);
        passwordField.setText("password");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(ing.rbi.poc.R.menu.login_screen, menu);
        return true;
    };


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ing.rbi.poc.R.id.action_settings:
                // Launch the preference settings activity.
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void sendConnect(View view){
        EditText userField = (EditText) findViewById(ing.rbi.poc.R.id.txt_username);
        String userString = userField.getText().toString();
        EditText passwordField = (EditText) findViewById(ing.rbi.poc.R.id.txt_password);
        String passwordString = passwordField.getText().toString();
        AppConnectionUtility.loginToStartScreen(this, userString, passwordString);
    }
}
