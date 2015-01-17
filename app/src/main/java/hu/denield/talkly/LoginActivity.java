package hu.denield.talkly;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.CheckBox;

import hu.denield.talkly.constant.Extras;
import hu.denield.talkly.constant.Fragments;

public class LoginActivity extends BaseActivity {

    private EditText mUsername;
    private EditText mPassword;
    private TextView mError;
    private TextView mCheckBoxTextView;
    private CheckBox mRemember;
    private String mErrorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mErrorText = getIntent().getStringExtra(Extras.ERROR);
        }

        // get the views
        mUsername = (EditText) findViewById(R.id.login_username);
        mPassword = (EditText) findViewById(R.id.login_password);
        mRemember = (CheckBox) findViewById(R.id.login_check_box_remember);
        mError = (TextView) findViewById(R.id.login_error);
        mCheckBoxTextView = (TextView) findViewById(R.id.login_checkbox_remember_text);

        // if there is an error message, write it to the user
        if (mErrorText != null) {
            mError.setText(mErrorText);
        }

        // toggle the checkbox by its text
        mCheckBoxTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mRemember.setChecked(!mRemember.isCheck());
            }
        });
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
            Intent aboutIntent = new Intent(this, FragmentActivity.class);
            aboutIntent.putExtra(Extras.FRAGMENT, Fragments.FragmentName.ABOUT.ordinal());
            startActivity(aboutIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loginOnClick(View view) {
        if ((!mUsername.getText().toString().trim().equals("")) && (!mPassword.getText().toString().trim().equals(""))) {
            Intent loginIntent = new Intent(this, MainActivity.class);
            loginIntent.putExtra(Extras.USERNAME, mUsername.getText().toString());
            loginIntent.putExtra(Extras.PASSWORD, mPassword.getText().toString());
            loginIntent.putExtra(Extras.REMEMBER, mRemember.isCheck());
            startActivity(loginIntent);
            finish();
        } else {
            Toast.makeText(this, getString(R.string.login_empty_fields), Toast.LENGTH_SHORT).show();
        }
    }
}