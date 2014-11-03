package hu.denield.chatly;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import hu.denield.chatly.adapter.MessageListAdapter;
import hu.denield.chatly.contants.Extras;
import hu.denield.chatly.data.MessageDataManager;

public class MainActivity extends BaseActivity {

    private String username;
    private String password;

    private EditText messageInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            username = getIntent().getStringExtra(Extras.USERNAME);
            password = getIntent().getStringExtra(Extras.PASSWORD);
        }

        //TODO: auto login
        if (!validateUser(username, password)) return;

        //Load the view from the inflated layout
        ListView listView = (ListView) findViewById(R.id.listview_messages);

        //Set and create the adapter
        listView.setAdapter(new MessageListAdapter(this, MessageDataManager.getInstance().getUsers()));

        messageInput = (EditText) findViewById(R.id.chat_message_input);

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, getString(R.string.action_settings), Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean validateUser(String username, String password) {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        if (username != null && password != null) {
            if (username.equals("admin") && password.equals("nimda")) {
                return true;
            }
            loginIntent.putExtra(Extras.ERROR, getString(R.string.login_wrong_username_or_password));
        }
        startActivity(loginIntent);
        finish();
        return false;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }
}
