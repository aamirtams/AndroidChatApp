package com.applozic.mobicomkit.uiwidgets.people.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.ContactService;
import com.applozic.mobicomkit.contact.database.ContactDatabase;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import net.rimoto.intlphoneinput.IntlPhoneInput;

/**
 * Created by Aamir on 04-Jul-17.
 */

public class AddContactActivity extends AppCompatActivity {
    private static final String TAG = "AddContactActivity";

    ActionBar actionBar;
    Button btnCancel,btnOK;
    ContactDatabase contactDatabase;
    IntlPhoneInput inputEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.applozic.mobicomkit.uiwidgets.R.layout.add_contact_popup);
        MobileAds.initialize(this, "ca-app-pub-5246243065157193~3806692664");
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        contactDatabase = new ContactDatabase(this);

        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Add User");

        btnCancel = (Button)findViewById(R.id.button_cancel);
        btnOK = (Button)findViewById(R.id.button_ok);
        inputEditText = (IntlPhoneInput)findViewById(R.id.popup_et);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editTextValue = inputEditText.getText().toString();
                if (TextUtils.isEmpty(editTextValue) || inputEditText.getText().toString().trim().length() == 0) {
                    Toast.makeText(AddContactActivity.this, R.string.empty_user_id_info, Toast.LENGTH_SHORT).show();
                }

                    Intent intent1 = new Intent(AddContactActivity.this, ConversationActivity.class);
                    intent1.putExtra(ConversationUIService.USER_ID, editTextValue);
                    startActivity(intent1);
                    finish();


            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void onBackPressed(){
       finish();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
           finish();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
