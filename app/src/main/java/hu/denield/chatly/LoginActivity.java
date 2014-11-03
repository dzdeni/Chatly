package hu.denield.chatly;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import hu.denield.chatly.contants.Extras;


public class LoginActivity extends BaseActivity {

    private EditText username;
    private EditText password;
    private TextView error;
    private String errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            errorText = getIntent().getStringExtra(Extras.ERROR);
        }

        username = (EditText) findViewById(R.id.login_username);
        password = (EditText) findViewById(R.id.login_password);
        error =  (TextView) findViewById(R.id.login_error);
        if (errorText != null) {
            error.setText(errorText);
        }
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
        if ((!username.getText().toString().trim().equals("")) && (!password.getText().toString().trim().equals(""))) {
            Intent loginIntent = new Intent(this, MainActivity.class);
            loginIntent.putExtra(Extras.USERNAME, username.getText().toString());
            loginIntent.putExtra(Extras.PASSWORD, password.getText().toString());
            startActivity(loginIntent);
            finish();
        } else {
            Toast.makeText(this, getString(R.string.login_empty_fields), Toast.LENGTH_SHORT).show();
        }
    }
}
