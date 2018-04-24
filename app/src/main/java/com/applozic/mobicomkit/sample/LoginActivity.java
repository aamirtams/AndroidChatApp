package com.applozic.mobicomkit.sample;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.PushNotificationTask;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserLoginTask;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicommons.commons.core.utils.PermissionsUtils;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.applozic.audiovideo.activity.AudioCallActivityV2;
import com.applozic.audiovideo.activity.VideoActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import net.rimoto.intlphoneinput.IntlPhoneInput;

import static com.applozic.mobicommons.commons.core.utils.PermissionsUtils.REQUEST_CAMERA;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback, View.OnClickListener{




    private static final String TAG = "LoginActivity";
    private static final int REQUEST_CONTACTS = 1;
    private static String[] PERMISSIONS_CONTACTS = {Manifest.permission.READ_CONTACTS ,Manifest.permission.WRITE_CONTACTS};
    LinearLayout layout;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    //flag variable for exiting the application
    private boolean exit = false;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private IntlPhoneInput mUserIdView;
    private EditText mPhoneNumberView;
   // private EditText mPasswordView;
    private EditText mDisplayName;
    private View mProgressView;
    private View mLoginFormView;
    private Button mEmailSignInButton;
    //CallbackManager callbackManager;
   //private TextView mTitleView;
   //private Spinner mSpinnerView;
    private int touchCount = 0;
    private MobiComUserPreference mobiComUserPreference;
    //private LoginButton loginButton;


    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private ViewGroup mPhoneNumberViews;


    private EditText mVerificationField;

    private Button mStartButton;
    private Button mVerifyButton;
    private Button mResendButton;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FacebookSdk.sdkInitialize(this);




        setContentView(R.layout.activity_login);
        setupUI(findViewById(R.id.layout));
        layout = (LinearLayout) findViewById(R.id.footerSnack);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        populateAutoComplete();

        mPhoneNumberView = (EditText) findViewById(R.id.phoneNumber);
        mUserIdView = (IntlPhoneInput) findViewById(R.id.userId);
        final String myInternational;
        if (mUserIdView.isValid()){
            myInternational = mUserIdView.getNumber();
        }

        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        // Assign views
        mPhoneNumberViews = (ViewGroup) findViewById(R.id.phone_auth_fields);






        mVerificationField = (EditText) findViewById(R.id.field_verification_code);

        mStartButton = (Button) findViewById(R.id.button_start_verification);
        mVerifyButton = (Button) findViewById(R.id.button_verify_phone);
        mResendButton = (Button) findViewById(R.id.button_resend);

        // Assign click listeners
        mStartButton.setOnClickListener(this);
        mVerifyButton.setOnClickListener(this);
        mResendButton.setOnClickListener(this);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS, credential);
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]
                updateUI(STATE_VERIFY_FAILED);
                // [END_EXCLUDE]
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // [START_EXCLUDE]
                // Update UI
                updateUI(STATE_CODE_SENT);
                // [END_EXCLUDE]
            }
        };
        // [END phone_auth_callbacks]









        //mPasswordView = (EditText) findViewById(R.id.password);
        mDisplayName = (EditText) findViewById(R.id.displayName);
       /* mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin(User.AuthenticationType.APPLOZIC);
                    return true;
                }
                return false;
            }
       });*/
        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                Utils.toggleSoftKeyBoard(LoginActivity.this, true);
                attemptLogin(User.AuthenticationType.APPLOZIC);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


        //callbackManager = CallbackManager.Factory.create();

      //  mSpinnerView = (Spinner) findViewById(R.id.spinner_for_url);
        //mSpinnerView.setVisibility(View.INVISIBLE);
     /*   mTitleView = (TextView) findViewById(R.id.textViewTitle);
        mTitleView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                touchCount += 1;
                if (touchCount == 5) {
                    mSpinnerView.setVisibility(View.VISIBLE);
                    touchCount = 0;

                } else {
                    Toast.makeText(getApplicationContext(), "Click more  " + Integer.toString(5 - touchCount), Toast.LENGTH_SHORT).show();
                }
            }
        });*/

        mobiComUserPreference = MobiComUserPreference.getInstance(this);
      /*  mSpinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mobiComUserPreference.setUrl(adapterView.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });*/

    }


    public void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    Utils.toggleSoftKeyBoard(LoginActivity.this, true);
                    return false;
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }
    }

    private void populateAutoComplete() {
        if (Utils.isBetweenGingerBreadAndKitKat()) {
            // Use AccountManager (API 8+)
            new SetupEmailAutoCompleteTask().execute(null, null);
        } else if (Utils.hasMarshmallow()) {
            showRunTimePermission();
        }
    }




    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        // [START_EXCLUDE]
        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(mUserIdView.getText().toString());
        }
        // [END_EXCLUDE]
    }
    // [END on_start_check_user]

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }


    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    // [START resend_verification]
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // [START_EXCLUDE]
                            updateUI(STATE_SIGNIN_SUCCESS, user);
                            // [END_EXCLUDE]


                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                // [START_EXCLUDE silent]
                                mVerificationField.setError("Invalid code.");
                                // [END_EXCLUDE]
                            }
                            // [START_EXCLUDE silent]
                            // Update UI
                            updateUI(STATE_SIGNIN_FAILED);
                            // [END_EXCLUDE]
                        }
                    }
                });
    }
    // [END sign_in_with_phone]
    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {
            case STATE_INITIALIZED:
                // Initialized state, show only the phone number field and start button
                enableViews(mStartButton, mUserIdView);
                disableViews(mVerifyButton, mResendButton, mVerificationField, mDisplayName, mEmailView, mPhoneNumberView, mEmailSignInButton);

                break;
            case STATE_CODE_SENT:
                // Code sent state, show the verification field, the
                enableViews(mVerifyButton, mResendButton, mUserIdView, mVerificationField);
                disableViews(mStartButton, mDisplayName, mEmailView, mPhoneNumberView, mEmailSignInButton);

                break;
            case STATE_VERIFY_FAILED:
                // Verification has failed, show all options
                enableViews(mStartButton, mVerifyButton, mResendButton, mUserIdView,
                        mVerificationField);

                break;
            case STATE_VERIFY_SUCCESS:
                // Verification has succeeded, proceed to firebase sign in
                disableViews(mStartButton, mVerifyButton, mResendButton, mUserIdView,
                        mVerificationField);
                enableViews(mDisplayName, mEmailView, mPhoneNumberView, mEmailSignInButton);


                // Set the verification text based on the credential
                if (cred != null) {
                    if (cred.getSmsCode() != null) {
                        mVerificationField.setText(cred.getSmsCode());
                    } else {
                        mVerificationField.setText(R.string.instant_validation);
                    }
                }

                break;
            case STATE_SIGNIN_FAILED:
                // No-op, handled by sign-in check

                break;
            case STATE_SIGNIN_SUCCESS:
                // Np-op, handled by sign-in check
                enableViews(mDisplayName, mEmailView, mPhoneNumberView, mEmailSignInButton);

                break;
        }

        if (user == null) {
            // Signed out
            mPhoneNumberViews.setVisibility(View.VISIBLE);
            mLoginFormView.setVisibility(View.GONE);
            mEmailSignInButton.setVisibility(View.GONE);




        } else {
            // Signed in
            mPhoneNumberViews.setVisibility(View.GONE);
            mEmailSignInButton.setVisibility(View.VISIBLE);
            mLoginFormView.setVisibility(View.VISIBLE);

            enableViews(mUserIdView, mVerificationField);
            mVerificationField.setText(null);


        }
    }

    private boolean validatePhoneNumber() {
        String phoneNumber = mUserIdView.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }

        return true;
    }

    private void enableViews(View... views) {
        for (View v : views) {
            v.setEnabled(true);
        }
    }

    private void disableViews(View... views) {
        for (View v : views) {
            v.setEnabled(false);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_start_verification:
                if (!validatePhoneNumber()) {
                    return;
                }

                startPhoneNumberVerification(mUserIdView.getText().toString());
                break;
            case R.id.button_verify_phone:
                String code = mVerificationField.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    mVerificationField.setError("Cannot be empty.");
                    return;
                }

                verifyPhoneNumberWithCode(mVerificationId, code);

                break;
            case R.id.button_resend:
                resendVerificationCode(mUserIdView.getText().toString(), mResendToken);
                break;

        }
    }










    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin(User.AuthenticationType authenticationType) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
       // mPasswordView.setError(null);
        mDisplayName.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String phoneNumber = mPhoneNumberView.getText().toString();
        String userId = mUserIdView.getText().toString().trim();
       // String password = mPasswordView.getText().toString();
        String displayName = mDisplayName.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(mUserIdView.getText().toString()) || mUserIdView.getText().toString().trim().length() == 0) {
            focusView = mUserIdView;
            cancel = true;
        }
        // Check for a valid password, if the user entered one.
       /* if ((TextUtils.isEmpty(mPasswordView.getText().toString()) || mPasswordView.getText().toString().trim().length() == 0) && !isPasswordValid(mPasswordView.getText().toString())) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }*/

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            /*mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;*/
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            // callback for login process
            final Activity activity = LoginActivity.this;
            UserLoginTask.TaskListener listener = new UserLoginTask.TaskListener() {

                @Override
                public void onSuccess(RegistrationResponse registrationResponse, final Context context) {
                    mAuthTask = null;
                    showProgress(false);

                    //Basic settings...

                    ApplozicClient.getInstance(context).setContextBasedChat(true).setHandleDial(true).setIPCallEnabled(true);

                    Map<ApplozicSetting.RequestCode, String> activityCallbacks = new HashMap<ApplozicSetting.RequestCode, String>();
                    activityCallbacks.put(ApplozicSetting.RequestCode.USER_LOOUT, LoginActivity.class.getName());

                    activityCallbacks.put(ApplozicSetting.RequestCode.AUDIO_CALL, AudioCallActivityV2.class.getName());
                    activityCallbacks.put(ApplozicSetting.RequestCode.VIDEO_CALL, VideoActivity.class.getName());

                    ApplozicSetting.getInstance(context).setActivityCallbacks(activityCallbacks);


                    //Set activity callbacks
                    /*Map<ApplozicSetting.RequestCode, String> activityCallbacks = new HashMap<ApplozicSetting.RequestCode, String>();
                    activityCallbacks.put(ApplozicSetting.RequestCode.MESSAGE_TAP, MainActivity.class.getName());
                    ApplozicSetting.getInstance(context).setActivityCallbacks(activityCallbacks);*/

                    //Start GCM registration....

                    PushNotificationTask.TaskListener pushNotificationTaskListener = new PushNotificationTask.TaskListener() {
                        @Override
                        public void onSuccess(RegistrationResponse registrationResponse) {

                        }

                        @Override
                        public void onFailure(RegistrationResponse registrationResponse, Exception exception) {

                        }
                    };
                    PushNotificationTask pushNotificationTask = new PushNotificationTask(Applozic.getInstance(context).getDeviceRegistrationId(), pushNotificationTaskListener, context);
                    pushNotificationTask.execute((Void) null);

                    buildContactData();

                    //starting main MainActivity
                    Intent mainActvity = new Intent(context, MainActivity.class);
                    startActivity(mainActvity);
                    Intent intent = new Intent(context, ConversationActivity.class);
                    if (ApplozicClient.getInstance(LoginActivity.this).isContextBasedChat()) {
                        intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);
                    }
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                    mAuthTask = null;
                    showProgress(false);

                    mEmailSignInButton.setVisibility(View.VISIBLE);
                    AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                    alertDialog.setTitle(getString(R.string.text_alert));
                    alertDialog.setMessage(exception.toString());
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    if (!isFinishing()) {
                        alertDialog.show();
                    }
                }
            };

            User user = new User();
            user.setUserId(userId);
            user.setEmail(email);
           // user.setPassword(password);
            user.setDisplayName(displayName);
            user.setContactNumber(phoneNumber);
            user.setAuthenticationTypeId(authenticationType.getValue());
            user.setFeatures(getFeatureList());

            mAuthTask = new UserLoginTask(user, listener, this);
            mEmailSignInButton.setVisibility(View.INVISIBLE);
          //  mSpinnerView.setVisibility(View.INVISIBLE);
            mAuthTask.execute((Void) null);
        }
    }

    private List<String> getFeatureList() {

        List<String> featureList =  new ArrayList<>();
        featureList.add(User.Features.IP_AUDIO_CALL.getValue());
        featureList.add(User.Features.IP_VIDEO_CALL.getValue());
        return featureList;
    }

    /**
     * Don't use this method...this is only for demo purpose..
     */
    private void buildContactData() {

        Context context = getApplicationContext();
        AppContactService appContactService = new AppContactService(context);
        // avoid each time update ....
        if (!appContactService.isContactExists("zam")) {

            List<Contact> contactList = new ArrayList<Contact>();


            appContactService.addAll(contactList);
        }
    }

    private boolean isEmailValid(String email) {

        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 5;
    }

    @Override
    public void onBackPressed() {

        if (exit) {
            finish();
        } else {
            Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_SHORT).show();
            exit = true;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3000);
        }

    }




    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void showRunTimePermission() {
       if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestContactsPermissions();

        } else {

            new SetupEmailAutoCompleteTask().execute(null, null);
        }
    }


    private void requestContactsPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_CONTACTS)) {

            Snackbar.make(layout, R.string.contact_permission,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat
                                    .requestPermissions(LoginActivity.this, PERMISSIONS_CONTACTS,
                                            REQUEST_CONTACTS);
                        }
                    }).show();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_CONTACTS, REQUEST_CONTACTS);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

     if (requestCode == REQUEST_CONTACTS) {
            if (PermissionsUtils.verifyPermissions(grantResults)) {
                showSnackBar(R.string.contact_permission_granted);
                new SetupEmailAutoCompleteTask().execute(null, null);

            } else {
                showSnackBar(R.string.contact_permission_granted);
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void showSnackBar(int resId) {
        Snackbar.make(layout, resId,
                Snackbar.LENGTH_SHORT)
                .show();
    }

    /**
     * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
     * the email text field with results on the main UI thread.
     */
    class SetupEmailAutoCompleteTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            ArrayList<String> emailAddressCollection = new ArrayList<String>();

            // Get all emails from the user's contacts and copy them to a list.
            ContentResolver cr = getContentResolver();
            Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    null, null, null);
            while (emailCur.moveToNext()) {
                String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract
                        .CommonDataKinds.Email.DATA));
                emailAddressCollection.add(email);
            }
            emailCur.close();

            return emailAddressCollection;
        }

        @Override
        protected void onPostExecute(List<String> emailAddressCollection) {
            addEmailsToAutoComplete(emailAddressCollection);
        }
    }



}