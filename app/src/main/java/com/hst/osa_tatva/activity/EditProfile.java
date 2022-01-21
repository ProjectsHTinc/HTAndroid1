package com.hst.osa_tatva.activity;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.util.Log.d;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;

import com.google.android.material.textfield.TextInputEditText;
import com.hst.osa_tatva.R;
import com.hst.osa_tatva.helpers.AlertDialogHelper;
import com.hst.osa_tatva.helpers.ProgressDialogHelper;
import com.hst.osa_tatva.interfaces.DialogClickListener;
import com.hst.osa_tatva.servicehelpers.ServiceHelper;
import com.hst.osa_tatva.serviceinterfaces.IServiceListener;
import com.hst.osa_tatva.utils.AndroidMultiPartEntity;
import com.hst.osa_tatva.utils.CommonUtils;
import com.hst.osa_tatva.utils.OSAConstants;
import com.hst.osa_tatva.utils.OSAValidator;
import com.hst.osa_tatva.utils.PreferenceStorage;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditProfile extends AppCompatActivity implements View.OnClickListener, IServiceListener,
        DialogClickListener {

    private static final String TAG = EditProfile.class.getName();
    private ServiceHelper serviceHelper;
    private ProgressDialogHelper progressDialogHelper;

    private SimpleDateFormat mDateFormatter;

    private Uri outputFileUri;
    private File file;
    private File sourceFile;
    private File destFile;
    static final int REQUEST_IMAGE_GET = 1;
    public static final String IMAGE_DIRECTORY = "ImageScalling";

    private String mActualFilePath = null;
    private Uri mSelectedImageUri = null;
    private Bitmap mCurrentUserImageBitmap = null;
    private String mUpdatedImageUrl = null;
    private String page;
    public static final String DATE_FORMAT = "yyyyMMdd_HHmmss";
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public String[] permission = {CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};
    private ActivityResultLauncher<Intent> activityResultLauncher;

    private ImageView profileImage;
    private TextView saveProfile;
    private TextInputEditText profileName, profilePhone, profileMail;
    private String resStr;
    private String fullName;
    private String phoneNo;
    private String mailId;
    private String gender;
    private String profile_image;
    private String checkProfileState = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_left_arrow));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(SampleEditProfileActivity.this, MainActivity.class));
                finish();
            }
        });

//        page = getIntent().getExtras().getString("page");

        saveProfile = (TextView) findViewById(R.id.update);
        saveProfile.setOnClickListener(this);

//        checkProfileState = PreferenceStorage.getProfileUpdate(this);
        profileImage = (ImageView) findViewById(R.id.proPic);
        profileImage.setOnClickListener(this);
        String url = PreferenceStorage.getProfilePic(this);
        String getSocialUrl = PreferenceStorage.getSocialNetworkProfileUrl(this);
        if (((url != null) && !(url.isEmpty()))) {
            Picasso.get().load(url).placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile).into(profileImage);
        } else if (((getSocialUrl != null) && !(getSocialUrl.isEmpty()))) {
            Picasso.get().load(getSocialUrl).placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile).into(profileImage);
        }
        profileName = (TextInputEditText) findViewById(R.id.txt_name);
        if (PreferenceStorage.getFullName(this) != null) {
            profileName.setText(PreferenceStorage.getFullName(this));
        }
        profileMail = (TextInputEditText) findViewById(R.id.txt_mail);
        if (PreferenceStorage.getEmail(this) != null) {
            profileMail.setText(PreferenceStorage.getEmail(this));
        }
        profilePhone = (TextInputEditText) findViewById(R.id.txt_mobile_number);
        if (PreferenceStorage.getPhoneNo(this) != null) {
            profilePhone.setText(PreferenceStorage.getPhoneNo(this));
        }

        mDateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        initiateServices();
        showUserDetails();
    }

    public void initiateServices() {
        serviceHelper = new ServiceHelper(this);
        serviceHelper.setServiceListener(this);
        progressDialogHelper = new ProgressDialogHelper(this);
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        View v = getCurrentFocus();
//
//        if (v != null &&
//                (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
//                v instanceof EditText &&
//                !v.getClass().getName().startsWith("android.webkit.")) {
//            int scrcoords[] = new int[2];
//            v.getLocationOnScreen(scrcoords);
//            float x = ev.getRawX() + v.getLeft() - scrcoords[0];
//            float y = ev.getRawY() + v.getTop() - scrcoords[1];
//
//            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
//                hideKeyboard(this);
//        }
//        return super.dispatchTouchEvent(ev);
//    }
//
//    public static void hideKeyboard(Activity activity) {
//        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
//            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
//        }
//    }

    private boolean validateFields() {

        if (!OSAValidator.checkNullString(this.profileName.getText().toString().trim())) {
            profileName.setError(getString(R.string.empty_entry));
            reqFocus(profileName);
            return false;
        } else if (!OSAValidator.checkNullString(profilePhone.getText().toString().trim())) {
            profilePhone.setError(getString(R.string.empty_entry));
            reqFocus(profilePhone);
            return false;
        } else if (profileMail.getText().length() > 0) {
            if (!OSAValidator.isEmailValid(this.profileMail.getText().toString().trim())) {
                profileMail.setError(getString(R.string.error_email));
                reqFocus(profileMail);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private void reqFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private boolean validateSignInResponse(JSONObject response) {
        boolean signInSuccess = false;
        if ((response != null)) {
            try {
                String status = response.getString("status");
                String msg = response.getString(OSAConstants.PARAM_MESSAGE);
                d(TAG, "status val" + status + "msg" + msg);

                if ((status != null)) {
                    if (((status.equalsIgnoreCase("activationError")) || (status.equalsIgnoreCase("alreadyRegistered")) ||
                            (status.equalsIgnoreCase("notRegistered")) || (status.equalsIgnoreCase("error")))) {
                        signInSuccess = false;
                        d(TAG, "Show error dialog");
                        AlertDialogHelper.showSimpleAlertDialog(this, msg);

                    } else {
                        signInSuccess = true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return signInSuccess;
    }

    private void showUserDetails() {

        resStr = "userDetails";
        String userId = PreferenceStorage.getUserId(this);

        if (CommonUtils.isNetworkAvailable(getApplicationContext())) {

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(OSAConstants.KEY_USER_ID, userId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            dialogHelper.showProgressDialog(getString(R.string.progress_bar));
            String url = OSAConstants.BUILD_URL + OSAConstants.GET_PROFILE_DETAILS;
            serviceHelper.makeGetServiceCall(jsonObject.toString(), url);
        } else {
            AlertDialogHelper.showSimpleAlertDialog(this, getString(R.string.error_no_net));
        }
    }

    private void saveProfileData() {

        resStr = "saveProfile";
        String userId = PreferenceStorage.getUserId(this);
        fullName = profileName.getText().toString().trim();
        PreferenceStorage.saveFullName(this, fullName);
        phoneNo = profilePhone.getText().toString().trim();
        PreferenceStorage.savePhoneNo(this, phoneNo);
        mailId = profileMail.getText().toString().trim();
        PreferenceStorage.saveEmail(this, mailId);

        if (validateFields()) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(OSAConstants.KEY_USER_ID, userId);
                jsonObject.put(OSAConstants.PARAMS_FIRST_NAME, fullName);
                jsonObject.put(OSAConstants.PARAMS_LAST_NAME, "");
                jsonObject.put(OSAConstants.KEY_PHONE_NO, phoneNo);
                jsonObject.put(OSAConstants.KEY_EMAIL, mailId);
                jsonObject.put(OSAConstants.KEY_GENDER, "");
                jsonObject.put(OSAConstants.KEY_DOB, "");
                jsonObject.put(OSAConstants.KEY_NEWS_STATUS, "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String serviceUrl = OSAConstants.BUILD_URL + OSAConstants.PROFILE_UPDATE;
            serviceHelper.makeGetServiceCall(jsonObject.toString(), serviceUrl);
        }
    }

    private void openImageIntent() {

// Determine Uri of camera image to save.
        File pictureFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        final File root = new File(pictureFolder, "OSAImages");
//        final File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyDir");

        if (!root.exists()) {
            if (!root.mkdirs()) {
                d(TAG, "Failed to create directory for storing images");
                return;
            }
        }
        Calendar newCalendar = Calendar.getInstance();
        int month = newCalendar.get(Calendar.MONTH) + 1;
        int day = newCalendar.get(Calendar.DAY_OF_MONTH);
        int year = newCalendar.get(Calendar.YEAR);
        int hours = newCalendar.get(Calendar.HOUR_OF_DAY);
        int minutes = newCalendar.get(Calendar.MINUTE);
        int seconds = newCalendar.get(Calendar.SECOND);
        final String fname = PreferenceStorage.getUserId(this) + "_" + day + "_" + month + "_" + year + "_" + hours + "_" + minutes + "_" + seconds + ".png";
        final File sdImageMainDirectory = new File(root.getPath() + File.separator + fname);
        destFile = sdImageMainDirectory;
        outputFileUri = Uri.fromFile(sdImageMainDirectory);
        d(TAG, "camera output Uri" + outputFileUri);

        // Camera.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    + "/" + IMAGE_DIRECTORY);
        } else {
            file = new File(Environment.getExternalStorageDirectory()
                    + "/" + IMAGE_DIRECTORY);
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_PICK);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Profile Photo");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, REQUEST_IMAGE_GET);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_GET) {
                Log.d(TAG, "ONActivity Result");

                final boolean isCamera;
                if (data == null) {
                    Log.d(TAG, "camera is true");
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    Log.d(TAG, "camera action is" + action);
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                if (isCamera) {
                    Log.d(TAG, "Add to gallery");
                    mSelectedImageUri = outputFileUri;
                    mActualFilePath = outputFileUri.getPath();
                    galleryAddPic(mSelectedImageUri);
                } else {
//                    mSelectedImageUri = data == null ? null : data.getData();
//                    mActualFilePath = getRealPathFromURI(this, mSelectedImageUri);
//                    Log.d(TAG, "path to image is" + mActualFilePath);

                    if (data != null && data.getData() != null) {
                        try {
                            mSelectedImageUri = data.getData();
                            String[] filePathColumn = {MediaStore.Images.Media.DATA};
                            Cursor cursor = getContentResolver().query(mSelectedImageUri,
                                    filePathColumn, null, null, null);
                            cursor.moveToFirst();
                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            mActualFilePath = getRealPathFromURI(this, mSelectedImageUri);
                            cursor.close();
                            File f1 = new File(mActualFilePath);
                            mCurrentUserImageBitmap = decodeFile(f1);
                            //return Image Path to the Main Activity
                            Intent returnFromGalleryIntent = new Intent();
                            returnFromGalleryIntent.putExtra("picturePath", mActualFilePath);
                            setResult(RESULT_OK, returnFromGalleryIntent);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Intent returnFromGalleryIntent = new Intent();
                            setResult(RESULT_CANCELED, returnFromGalleryIntent);
                            finish();
                        }
                    } else {
                        Log.i(TAG, "RESULT_CANCELED");
                        Intent returnFromGalleryIntent = new Intent();
                        setResult(RESULT_CANCELED, returnFromGalleryIntent);
                        finish();
                    }
                }
                Log.d(TAG, "image Uri is" + mSelectedImageUri);
                if (mSelectedImageUri != null) {
                    Log.d(TAG, "image URI is" + mSelectedImageUri);
                    mUpdatedImageUrl = null;
                    mCurrentUserImageBitmap = decodeFile(destFile);
                    new UploadFileToServer().execute();
                }
            }
        }
    }

    private Bitmap decodeFile(File f) {

        Bitmap b = null;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int IMAGE_MAX_SIZE = 1024;
        int scale = 1;
        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        try {
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Log.d(TAG, "Width :" + b.getWidth() + " Height :" + b.getHeight());

        destFile = new File(file, "img_" + mDateFormatter.format(new Date()).toString() + ".png");
        mActualFilePath = destFile.getPath();
        try {
            FileOutputStream out = new FileOutputStream(destFile);
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return b;
    }

    @Override
    public void onClick(View v) {

        if (v == profileImage) {
            if (checkPermission()) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }else {
                    openImageIntent();
                }
            } else {
                requestPermission();
            }
        }
        if (v == saveProfile) {
            saveProfileData();
        }
    }

    @Override
    public void onResponse(JSONObject response) {

        if (validateSignInResponse(response)) {

            try {

                if (resStr.equalsIgnoreCase("userDetails")) {

                    JSONObject object = response.getJSONObject("get_profile_details");
                    Log.d(TAG, object.toString());
                    String userName = object.getString("first_name");
                    String userPhone = object.getString("phone_number");
                    String userMail = object.getString("email");
                    String userPic = object.getString("profile_picture");

                    if ((userName != null) && !(userName.isEmpty()) && !userName.equalsIgnoreCase("null")) {
                        PreferenceStorage.saveFullName(this, userName);
                    }
                    if ((userPhone != null) && !(userPhone.isEmpty()) && !userPhone.equalsIgnoreCase("null")) {
                        PreferenceStorage.savePhoneNo(this, userPhone);
                    }
                    if ((userMail != null) && !(userMail.isEmpty()) && !userMail.equalsIgnoreCase("null")) {
                        PreferenceStorage.saveEmail(this, userMail);
                    }
                    if ((userPic != null) && !(userPic.isEmpty()) && !userPic.equalsIgnoreCase("null")) {
                        PreferenceStorage.saveProfilePic(this, userPic);
                    }
                }

                if (resStr.equalsIgnoreCase("saveProfile")) {

                    Log.d(TAG, response.toString());
                    Toast.makeText(getApplicationContext(), response.getString("msg"), Toast.LENGTH_SHORT).show();

                    Intent saveIntent = new Intent(this, MainActivity.class);
                    startActivity(saveIntent);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onError(String error) {

    }

    @Override
    public void onAlertPositiveClicked(int tag) {

    }

    @Override
    public void onAlertNegativeClicked(int tag) {

    }

    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {

        private static final String TAG = "UploadFileToServer";
        private HttpClient httpclient;
        HttpPost httppost;
        public boolean isTaskAborted = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            httpclient = new DefaultHttpClient();
            httppost = new HttpPost(String.format(OSAConstants.BUILD_URL +
                    OSAConstants.PROFILE_PIC + PreferenceStorage.getUserId(getApplicationContext()) + "/"));

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {

                            }
                        });
                Log.d(TAG, "actual file path is" + mActualFilePath);
                if (mActualFilePath != null) {

                    File sourceFile = new File(mActualFilePath);

                    // Adding file data to http body
                    //fileToUpload
                    entity.addPart("user_pic", new FileBody(sourceFile));

                    // Extra parameters if you want to pass to server
//                    entity.addPart("user_type", new StringBody(PreferenceStorage.getUserType(ProfileActivity.this)));

                    httppost.setEntity(entity);

                    // Making server call
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity r_entity = response.getEntity();

                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        // Server response
                        responseString = EntityUtils.toString(r_entity);
                        try {
                            JSONObject resp = new JSONObject(responseString);
                            String successVal = resp.getString("status");

                            mUpdatedImageUrl = resp.getString("picture_url");
                            if (mUpdatedImageUrl != null) {
                                PreferenceStorage.saveProfilePic(getApplicationContext(), mUpdatedImageUrl);
                            }
                            Log.d(TAG, "updated image url is" + mUpdatedImageUrl);
                            if (successVal.equalsIgnoreCase("success")) {
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        responseString = "Error occurred! Http Status Code: "
                                + statusCode;
                    }
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Response from server: " + result);
            progressDialogHelper.hideProgressDialog();
            super.onPostExecute(result);

            if ((result == null) || (result.isEmpty()) || (result.contains("Error"))) {
                if (((mUpdatedImageUrl != null) && !(mUpdatedImageUrl.isEmpty()))) {
                    Picasso.get().load(mUpdatedImageUrl).into(profileImage);
                } else {
                    profileImage.setImageResource(R.drawable.ic_profile);
                }
                Toast.makeText(getApplicationContext(), "Unable to upload picture", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Uploaded successfully!", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(getIntent());
            }
//            saveProfileData();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    private void galleryAddPic(Uri uriRequest) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(uriRequest.getPath());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        String result = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            CursorLoader loader = new CursorLoader(context, contentUri, proj, null, null, null);
            Cursor cursor = loader.loadInBackground();
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                result = cursor.getString(column_index);
                cursor.close();
            } else {
                Log.d(TAG, "cursor is null");
            }
        } catch (Exception e) {
            result = null;
            Toast.makeText(this, "Was unable to save  image", Toast.LENGTH_SHORT).show();

//            }
        }
        return result;
    }

    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int readCheck = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
            int writeCheck = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
            return readCheck == PackageManager.PERMISSION_GRANTED && writeCheck == PackageManager.PERMISSION_GRANTED;
        }
    }

    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent reqInt = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                reqInt.addCategory(Intent.CATEGORY_DEFAULT);
                reqInt.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivityForResult(reqInt, REQUEST_IMAGE_GET);

            } catch (Exception e) {
                Intent reqInt = new Intent();
                reqInt.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(reqInt, REQUEST_IMAGE_GET);

            }
        } else {
            ActivityCompat.requestPermissions(this, permission, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImageIntent();
                } else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
