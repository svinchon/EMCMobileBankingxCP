package ing.rbi.poc;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import uk.co.josephearl.foundry.FoundryLayoutInflater;


public class CreateBatchTabbed extends Activity {

    ActionBar actionBar;
    ActionBar.Tab createTab, viewDocsTab;
    country_list createFragment;
    Fragment viewDocumentsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ing.rbi.poc.R.layout.activity_create_batch_tabbed);

        Bundle bundle=this.getIntent().getExtras();
        setupCreateFragment(bundle);
        setupViewDocsFragment(bundle);

        actionBar = getActionBar();


        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        createTab = actionBar.newTab().setText("  Create New");
        createTab.setIcon(ing.rbi.poc.R.drawable.i_create_new);


        viewDocsTab = actionBar.newTab().setText("  Documents");
        viewDocsTab.setIcon(ing.rbi.poc.R.drawable.i_view_documents);
        createTab.setTabListener(new MyTabListener(createFragment));
        viewDocsTab.setTabListener(new MyTabListener(viewDocumentsFragment));

        actionBar.addTab(createTab);
        // TODO turn hotfix by SEB into clean code
        //actionBar.addTab(viewDocsTab);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void setupViewDocsFragment(Bundle savedInstanceState) {
        viewDocumentsFragment = new ViewDocumentsFragment();
        viewDocumentsFragment.setArguments(savedInstanceState);
    }

    private void setupCreateFragment(Bundle savedInstanceState) {
        createFragment = new country_list();
        createFragment.setArguments(savedInstanceState);

        createFragment.addToIntentData(Constants.URI, getIntent().getStringExtra(Constants.URI));
        createFragment.addToIntentData(Constants.TICKET, getIntent().getStringExtra(Constants.TICKET));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(ing.rbi.poc.R.menu.menu_create_batch_tabbed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == ing.rbi.poc.R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void ShowDLG(String Title, String msg) {
        //Display a message saying that a flow needs to be selected
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(msg)
                .setTitle(Title);
        builder.setPositiveButton(ing.rbi.poc.R.string.OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private LayoutInflater foundryLayoutInflater;

    @Override
    public Object getSystemService(final String name) {
        if (Context.LAYOUT_INFLATER_SERVICE.equals(name)) {
            return getFoundryLayoutInflater();
        }
        return super.getSystemService(name);
    }

    @Override
    public LayoutInflater getLayoutInflater() {
        return getFoundryLayoutInflater();
    }

    private LayoutInflater getFoundryLayoutInflater() {
        if (foundryLayoutInflater == null) {
            foundryLayoutInflater = new FoundryLayoutInflater(this);
            foundryLayoutInflater.setFactory(this);
        }
        return foundryLayoutInflater;
    }

    @Override
    public void onBackPressed() {
        ActionBar.Tab selectedTab = actionBar.getSelectedTab();
        if(selectedTab.equals(createTab)){
            Intent intent = new Intent(this, LoginScreen.class);
            this.startActivity(intent);
        }
        else if(selectedTab.equals(viewDocsTab)){
            actionBar.setSelectedNavigationItem(0);
        }
        else {
            super.onBackPressed();
        }
    }
}



