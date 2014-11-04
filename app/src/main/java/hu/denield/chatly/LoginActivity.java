package hu.denield.chatly;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import hu.denield.chatly.contants.Extras;


public class LoginActivity extends BaseActivity {

    private EditText mUsername;
    private EditText mPassword;
    private TextView mError;
    private CheckBox mRemember;
    private String mErrorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mErrorText = getIntent().getStringExtra(Extras.ERROR);
        }

        mUsername = (EditText) findViewById(R.id.login_username);
        mPassword = (EditText) findViewById(R.id.login_password);
        mRemember = (CheckBox) findViewById(R.id.check_box_remember);
        mError =  (TextView) findViewById(R.id.login_error);

        if (mErrorText != null) {
            mError.setText(mErrorText);
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
        if ((!mUsername.getText().toString().trim().equals("")) && (!mPassword.getText().toString().trim().equals(""))) {
            Intent loginIntent = new Intent(this, MainActivity.class);
            loginIntent.putExtra(Extras.USERNAME, mUsername.getText().toString());
            loginIntent.putExtra(Extras.PASSWORD, mPassword.getText().toString());
            loginIntent.putExtra(Extras.REMEMBER, mRemember.isChecked());
            startActivity(loginIntent);
            finish();
        } else {
            Toast.makeText(this, getString(R.string.login_empty_fields), Toast.LENGTH_SHORT).show();
        }
    }
}
