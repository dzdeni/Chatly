package hu.denield.chatly;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import hu.denield.chatly.contants.Extras;


public class LoginActivity extends BaseActivity {

    private EditText username;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        username = (EditText) findViewById(R.id.edit_text_username);
        password = (EditText) findViewById(R.id.edit_text_password);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_login;
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
        if (id == R.id.action_about) {
            Toast.makeText(this, getString(R.string.action_about), Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loginOnClick(View view) {
        Intent loginIntent = new Intent(this, MainActivity.class);
        loginIntent.putExtra(Extras.USERNAME, username.getText().toString());
        loginIntent.putExtra(Extras.PASSWORD, password.getText().toString());
        startActivity(loginIntent);
        finish();
    }
}
