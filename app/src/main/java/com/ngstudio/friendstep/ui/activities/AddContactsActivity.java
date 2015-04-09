package com.ngstudio.friendstep.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ngstudio.friendstep.R;

public class AddContactsActivity extends BaseActivity implements View.OnClickListener {

    private Button buttonAdd;
    private EditText etName;
    private EditText etMobile;

    @Override
    protected int getFragmentContainerId() {
        return R.layout.activity_add_contacts;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getFragmentContainerId());
        etName = (EditText) findViewById(R.id.etName);
        etMobile = (EditText) findViewById(R.id.etMobile);
        buttonAdd = (Button) findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.putExtra(CONTACTS_NAME, etName.getText().toString());
        intent.putExtra(CONTACTS_MOBILE, etMobile.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    public final static String CONTACTS_NAME = "contacts_name";
    public final static String CONTACTS_MOBILE = "contacts_mobile";

}
