package com.applozic.mobicomkit.uiwidgets.people.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.UserService;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.RemoveInterfaceListener;
import com.applozic.mobicomkit.uiwidgets.emoji.Emojicon;
import com.applozic.mobicomkit.uiwidgets.emoji.EmojiconEditText;
import com.applozic.mobicomkit.uiwidgets.emoji.EmojiconGridView;
import com.applozic.mobicomkit.uiwidgets.emoji.EmojiconTextView;
import com.applozic.mobicomkit.uiwidgets.emoji.EmojiconsPopup;
import com.applozic.mobicomkit.uiwidgets.uilistener.MobicomkitUriListener;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ChannelCreateActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ContactSelectionActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.PictureUploadPopUpFragment;
import com.applozic.mobicomkit.uiwidgets.instruction.ApplozicPermissions;
import com.applozic.mobicomkit.uiwidgets.people.fragment.ProfileFragment;
import com.applozic.mobicommons.commons.core.utils.PermissionsUtils;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.applozic.mobicomkit.Applozic;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.applozic.mobicommons.commons.core.utils.PermissionsUtils.REQUEST_CAMERA;


/**
 * Created by Aamir on 07-Jun-17.
 */

public class ProfileActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, MobicomkitUriListener, RemoveInterfaceListener {
    private static final int REQUEST_CAMERA = 1;
    private static String[] PERMISSIONS_CAMERA = {Manifest.permission.CAMERA};

    private ImageView img_profile;
    private ImageView selectImageProfileIcon, statusEdit,displayNameEdit,emoji;
    private TextView displayNameText;
    EditText status_et;
    private TextView statusText;
    private ActionBar mActionBar;
    private Uri imageChangeUri;
    private static final String TAG = "ProfileActivity";
    private String DEFAULT_CONATCT_IMAGE = "applozic_default_contactImg.jpeg";
    private ApplozicPermissions applozicPermissions;
    Contact userContact;
    File profilePhotoFile;
    private LinearLayout layout;
    private Snackbar snackbar;
    private String changedStatusString;
    private String displayName;
    private ImageLoader mImageLoader;
    AppContactService contactService;
    private String groupIconImageLink;
    AlCustomizationSettings alCustomizationSettings;
    ActionBar actionBar;
    EmojiconsPopup popup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.al_activity_profile);
        MobileAds.initialize(this, "ca-app-pub-5246243065157193~3806692664");
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        populateAutoComplete();

        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Profile");

        layout = (LinearLayout) findViewById(R.id.footerSnack);
        img_profile = (ImageView) findViewById(R.id.applozic_user_profile);
        statusEdit = (ImageView)findViewById(R.id.status_edit_btn);
        selectImageProfileIcon = (ImageView)findViewById(R.id.applozic_user_profile_camera);
        displayNameText = (TextView) findViewById(R.id.applozic_profile_displayname);
        statusText = (TextView) findViewById(R.id.applozic_profile_status);
        mActionBar = getSupportActionBar();
       selectImageProfileIcon.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               processImagePicker();
           }
       });
        final Context context = this.getApplicationContext();

        String jsonString = FileUtils.loadSettingsJsonFile(getApplicationContext());
        if (!TextUtils.isEmpty(jsonString)) {
            alCustomizationSettings = (AlCustomizationSettings) GsonUtils.getObjectFromJson(jsonString, AlCustomizationSettings.class);
        } else {
            alCustomizationSettings = new AlCustomizationSettings();
        }
        mImageLoader = new ImageLoader(context, img_profile.getHeight()) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return contactService.downloadContactImage(context, (Contact) data);
            }
        };
        contactService = new AppContactService(this);
        userContact = contactService.getContactById(MobiComUserPreference.getInstance(this).getUserId());
        displayNameText.setText(userContact.getDisplayName());

        int drawableResourceId = getResources().getIdentifier(alCustomizationSettings.getAttachCameraIconName(), "drawable", getPackageName());
        selectImageProfileIcon.setImageResource(drawableResourceId);

        if (!TextUtils.isEmpty(userContact.getStatus())) {
            statusText.setText(userContact.getStatus());
        }

       // LayoutInflater factory = LayoutInflater.from(ProfileActivity.this);
      //  final View builderview = factory.inflate(R.layout.edit_your_status_layout, null);

       // final View rootView = findViewById(R.id.b);
      //  popup = new EmojiconsPopup(builderview, this);



        statusEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Status");

               // builder.setView(builderview);

               final EditText input = new EditText(ProfileActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
              /* status_et = (EmojiconEditText)builderview.findViewById(R.id.change_status_et);
                emoji = (ImageView)builderview.findViewById(R.id.change_status_smiley);


                popup.setSizeForSoftKeyboard();
                //If the emoji popup is dismissed, change emojiButton to smiley icon
                popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

                    @Override
                    public void onDismiss() {
                        changeEmojiKeyboardIcon(emoji, R.drawable.smiley);
                    }
                });
                //If the text keyboard closes, also dismiss the emoji popup
                popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

                    @Override
                    public void onKeyboardOpen(int keyBoardHeight) {

                    }

                    @Override
                    public void onKeyboardClose() {
                        if (popup.isShowing())
                            popup.dismiss();
                    }
                });

                //On emoji clicked, add it to edittext
                popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

                    @Override
                    public void onEmojiconClicked(Emojicon emojicon) {
                        if (status_et == null || emojicon == null) {
                            return;
                        }

                        int start = status_et.getSelectionStart();
                        int end = status_et.getSelectionEnd();
                        if (start < 0) {
                            status_et.append(emojicon.getEmoji());
                        } else {
                            status_et.getText().replace(Math.min(start, end),
                                    Math.max(start, end), emojicon.getEmoji(), 0,
                                    emojicon.getEmoji().length());
                        }
                    }
                });

                //On backspace clicked, emulate the KEYCODE_DEL key event
                popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

                    @Override
                    public void onEmojiconBackspaceClicked(View v) {
                        KeyEvent event = new KeyEvent(
                                0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                        status_et.dispatchKeyEvent(event);
                    }
                });



                emoji.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //If popup is not showing => emoji keyboard is not visible, we need to show it
                        if (!popup.isShowing()) {

                            //If keyboard is visible, simply show the emoji popup
                            if (popup.isKeyBoardOpen()) {
                                popup.showAtBottom();
                                changeEmojiKeyboardIcon(emoji, R.drawable.ic_action_keyboard);
                            }

                            //else, open the text keyboard first and immediately after that show the emoji popup
                            else {
                                status_et.setFocusableInTouchMode(true);
                                status_et.requestFocus();
                                popup.showAtBottomPending();
                                final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputMethodManager.showSoftInput(status_et, InputMethodManager.SHOW_IMPLICIT);
                                changeEmojiKeyboardIcon(emoji, R.drawable.ic_action_keyboard);
                            }
                        }

                        //If popup is showing, simply dismiss it to show the undelying text keyboard
                        else {
                            popup.dismiss();
                        }
                    }
                });*/

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {


                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changedStatusString = input.getText().toString();
                        new ProfileActivity.ProfilePictureUpload(changedStatusString, ProfileActivity.this).execute((Void[]) null);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
        /*displayNameEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle(getString(R.string.display_name));
                final EditText input = new EditText(ProfileActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                builder.setView(input);


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        displayName = input.getText().toString();
                        if (!displayName.trim().isEmpty() && !TextUtils.isEmpty(displayName)) {
                            new ProfileActivity.ProfilePictureUpload(displayName, ProfileActivity.this).execute((Void[]) null);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });*/

        mImageLoader.setImageFadeIn(false);
        mImageLoader.setLoadingImage(R.drawable.applozic_ic_contact_picture_180_holo_light);
        mImageLoader.loadImage(userContact, img_profile);

    }

    private void changeEmojiKeyboardIcon(ImageView iconToBeChanged, int drawableResourceId) {
        iconToBeChanged.setImageResource(drawableResourceId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       // menu.findItem(R.id.refresh).setVisible(false);
        //menu.findItem(R.id.menu_search).setVisible(false);
        return true;
    }

    public void handleProfileimageUpload(boolean isSaveFile, Uri imageUri, File file) {
        img_profile.setImageDrawable(null);
        img_profile.setImageURI(imageUri);
        new ProfileActivity.ProfilePictureUpload(true, profilePhotoFile, imageChangeUri, ProfileActivity.this).execute((Void[]) null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(intent);
                if (resultCode == RESULT_OK) {
                    if (intent == null) {
                        return;
                    }

                    File file = FileClientService.getFilePath(DEFAULT_CONATCT_IMAGE, ProfileActivity.this, "image", true);
                    if (file == null || !file.exists()) {
                        Log.i(TAG, "file not found,exporting it from drawable");
                        Bitmap bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.applozic_ic_contact_picture_180_holo_light);
                        String filePath = ImageUtils.saveImageToInternalStorage(FileClientService.getFilePath(DEFAULT_CONATCT_IMAGE, this.getApplicationContext(), "image", true), bm);
                        file = new File(filePath);
                    }
                    handleProfileimageUpload(false, Uri.parse(file.getAbsolutePath()), file);
                    if (imageChangeUri != null) {
                        imageChangeUri = result.getUri();
                        img_profile.setImageDrawable(null); // <--- added to force redraw of ImageView
                        img_profile.setImageURI(imageChangeUri);
                        new ProfileActivity.ProfilePictureUpload(true, profilePhotoFile, imageChangeUri, ProfileActivity.this).execute((Void[]) null);
                    } else {
                        imageChangeUri = result.getUri();
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String imageFileName = "JPEG_" + timeStamp + "_" + ".jpeg";
                        img_profile.setImageDrawable(null); // <--- added to force redraw of ImageView
                        img_profile.setImageURI(imageChangeUri);
                        profilePhotoFile = FileClientService.getFilePath(imageFileName, this, "image/jpeg");
                        new ProfileActivity.ProfilePictureUpload(true, profilePhotoFile, imageChangeUri, ProfileActivity.this).execute((Void[]) null);
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
                }
            }
            if (resultCode == Activity.RESULT_OK) {
                handleOnActivityResult(requestCode, intent);
            }
        } catch (Exception e) {
            Log.i(TAG, "exception in profile image");
        }
    }
    public void handleOnActivityResult(int requestCode, Intent intent) {

        switch (requestCode) {

            case ProfileFragment.REQUEST_CODE_ATTACH_PHOTO:
                Uri selectedFileUri = (intent == null ? null : intent.getData());
                imageChangeUri = null;
                beginCrop(selectedFileUri);
                break;

            case ProfileFragment.REQUEST_CODE_TAKE_PHOTO:
                beginCrop(imageChangeUri);
                break;

        }
    }

    void beginCrop(Uri imageUri) {
        try {
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.OFF)
                    .setMultiTouchEnabled(true)
                    .start(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            if (PermissionsUtils.verifyPermissions(grantResults)) {
                showSnackBar(R.string.phone_camera_permission_granted);
            } else {
                showSnackBar(R.string.phone_camera_permission_not_granted);
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
    @Override
    public void removeCallBack() {
        try {
            imageChangeUri = null;
            groupIconImageLink = null;
           img_profile.setImageDrawable(null); // <--- added to force redraw of ImageView
            img_profile.setImageResource(R.drawable.applozic_group_icon);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void processImagePicker() {

        if (PermissionsUtils.isCameraPermissionGranted(this) && !PermissionsUtils.checkSelfForStoragePermission(this)) {

            new Handler().post(new Runnable() {
                public void run() {
                    FragmentManager supportFragmentManager = getSupportFragmentManager();
                    DialogFragment fragment = PictureUploadPopUpFragment.newInstance(true, imageChangeUri == null);
                    FragmentTransaction fragmentTransaction = supportFragmentManager
                            .beginTransaction();
                    Fragment prev = getSupportFragmentManager().findFragmentByTag("PhotosAttachmentFragment");
                    if (prev != null) {
                        fragmentTransaction.remove(prev);
                    }
                    fragmentTransaction.addToBackStack(null);
                    fragment.show(fragmentTransaction, "PhotosAttachmentFragment");
                }
            });

        } else {
            if (Utils.hasMarshmallow()) {
                if (PermissionsUtils.checkSelfForCameraPermission(this)) {
                    applozicPermissions.requestCameraPermission();
                } else {
                    applozicPermissions.requestStoragePermissions();
                }
            } else {
                processImagePicker();
            }
        }
    }
    @Override
    public Uri getCurrentImageUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_" + ".jpeg";
        profilePhotoFile = FileClientService.getFilePath(imageFileName, getApplicationContext(), "image/jpeg");
        imageChangeUri = FileProvider.getUriForFile(this, Utils.getMetaDataValue(this, MobiComKitConstants.PACKAGE_NAME) + ".provider", profilePhotoFile);
        return imageChangeUri;
    }

    public void showRunTimePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissions();

        }
    }


    private void requestCameraPermissions() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            Snackbar.make(layout, R.string.phone_camera_permission,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat
                                    .requestPermissions(ProfileActivity.this, PERMISSIONS_CAMERA,
                                            REQUEST_CAMERA);
                        }
                    }).show();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_CAMERA, REQUEST_CAMERA);
        }

    }
    private void populateAutoComplete() {
       if (Utils.hasMarshmallow()) {
            showRunTimePermission();
        }
    }



    public class ProfilePictureUpload extends AsyncTask<Void, Void, Boolean> {

        Context context;
        Uri fileUri;
        String displayName;
        File file;
        boolean isSaveFile;
        UserService userService;
        String status;
        FileClientService fileClientService;
        private ProgressDialog progressDialog;
        private String groupIconImageLink;


        public ProfilePictureUpload(boolean isSaveFile, File file, Uri fileUri, Context context) {
            this.context = context;
            this.fileUri = fileUri;
            this.file = file;
            this.isSaveFile = isSaveFile;
            this.fileClientService = new FileClientService(context);
            this.userService = UserService.getInstance(context);

        }
        public ProfilePictureUpload(String status, Context context) {
            this.context = context;
            this.status = status;
            this.fileClientService = new FileClientService(context);
            this.userService = UserService.getInstance(context);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context, "",
                    context.getString(R.string.applozic_contacts_loading_info), true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                if (fileUri != null) {
                    String filePath = file.getAbsolutePath();
                    if (isSaveFile) {
                        fileClientService.writeFile(fileUri, file);
                    }
                    groupIconImageLink = fileClientService.uploadProfileImage(filePath);
                    userService.updateDisplayNameORImageLink(displayName, groupIconImageLink,filePath, status);
                }

                if (TextUtils.isEmpty(displayName)) {
                    this.displayName = userContact.getDisplayName();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(ProfileActivity.class.getName(), "Exception");

            }
            return true;


        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if (!TextUtils.isEmpty(changedStatusString)) {
                statusText.setText(changedStatusString);
            }
            progressDialog.dismiss();
        }

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