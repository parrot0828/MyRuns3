package kangwon.cse.jck.myruns3;

import android.Manifest;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class UserProfileActivity extends AppCompatActivity {

    // "저장" 버튼을 누르면
    // (1) ImageView에 보이는 사진을 내부저장소에 정해진 파일 이름(profile.png)으로 저장한다.
    // (2) 기타 프로필 정보를 SharedPreferences에 저장한다
    // "사진 변경" 버튼을 누르면 카메라 앱 액티비티를 구동한다.
    // 카메라 앱을 구동할 때는 사진을 저장할 외부저장소 공용공간의 파일이름을 지정해 준다.
    // 카메라 앱 액티비티가 종료하면 onActivityResult 메소드가 호출되는데 이 때 cropping app을 구동한다.
    // Cropping 앱 액티비티가 종료하면 다시 onActivityResult 메소드가 호출되는데 이 때 crop된 사진을 ImageView에 나타나게 한다
    // 그리고 crop된 사진을 내부저장소에 temp.png 이름으로 저장하고 hasTempPhoto를 true로 설정한다.
    // 이 상태는 사용자가 프로필 사진을 바꾸려고 사진을 찍고 크랍을 해서 화면의 ImageView에는 새 사진이 보이지만
    // 아직 이 새 사진을 프로필 사진으로 저장하지 않은 상태이다. 사용자는 새 사진이 마음에 들지 않아 사진을 새로 찍을 수도 있다.

    // 이 상태에서 화면을 회전하면 ImageView에 현재 보이는 사진(새 프로필 후보 사진)이 유지되어야 한다.
    // hasTempPhoto는 프로필 사진으로 아직 저장하지 않은 유효한 후보 사진이 있는지 여부를 알려주는 상태변수이다.
    // "저장" 버튼을 누르면 새 사진을 profile.png 파일에 저장하고 hasTempPhoto를 false로 설정한다.
    // "저장" 버튼을 누르기 전에 화면을 회전하면 새 화면에서 profile.png가 아닌 temp.png가 ImageView에 보인다.
    //

    private Uri mImageUri;      // 카메라 사진을 저장할 외부저장소 파일의 Uri.
    private ImageView mImageView;
    Button changePhotoButton;

    private boolean hasTempPhoto = false;   // 아직 프로필 사진으로 확정하지 않은 임시 사진이 있나?
    private boolean isFromCamera = false;   // 프로필 후보 사진이 카메라로부터 왔나?


    public static final String PREFS = "Profile_Info";  // 사용자 정보를 저장한 SharedPreferences 이름.

    public static final int REQUEST_CAMERA_CAPTURE = 0; // 카메라 앱을 구동할 때 지정하는 요청 번호.
    public static final int REQUEST_PICK_IMAGE = 1;     // 갤러리를 앱을 구동할 때 지정하는 요청 번호.

    private static final int WRITE_PERMISSION_CHECK_CODE = 1002;

    private static final String IMAGE_UNSPECIFIED = "image/*";

    // 프로필 이미지를 저장할 내부저장소 파일이름.
    static final String PROFILE_IMAGE_FILENAME = "profile.png";
    // Crop된 사진을 당분간 (프로필 이미지로 저장하기 전까지) 저장하기 위한 내부저장소 파일이름.
    static final String TEMP_IMAGE_FILENAME = "temp.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mImageView = (ImageView) findViewById(R.id.profile_image);
        changePhotoButton = (Button) findViewById(R.id.button_change);

        loadPhoto(PROFILE_IMAGE_FILENAME);  // 프로필 이미지가 ImageView에 보이게 함.

        // Find tempPhoto path
        File tempPhoto = new File(this.getFilesDir().getAbsolutePath() +
                "/" + TEMP_IMAGE_FILENAME);

        // 기기 회전 등의 이유로 액티비티가 새로 만들어질 때,
        // temp 이미지가 존재하면 복구한다. (프로필 이미지 대신 temp 이미지가 ImageView에 보이게 한다.)
        if (savedInstanceState != null) {
            hasTempPhoto = savedInstanceState.getBoolean("hasTempPhoto");
            if (tempPhoto.exists() && hasTempPhoto)
                loadPhoto(TEMP_IMAGE_FILENAME);
        }

        // Retrieve profile information
        loadUserInfo();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        changePhotoButton.setEnabled(savedInstanceState.getBoolean("changePhotoButtonEnabled"));
        Log.d("changeButtonEnabled", "" + changePhotoButton.isEnabled());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the image capture uri before the activity goes into background
        outState.putBoolean("hasTempPhoto", hasTempPhoto);
        outState.putBoolean("changePhotoButtonEnabled", changePhotoButton.isEnabled());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void selectSave(View v) {
        // ImaageView에 보이는 사진을 프로필 사진으로 저장한다.
        savePhoto(PROFILE_IMAGE_FILENAME);

        // 프로필 사진을 저장했으므로 temp photo는 더 이상 필요하지 않다.
        File tempPhoto = new File(this.getFilesDir().getAbsolutePath() + "/" + TEMP_IMAGE_FILENAME);
        boolean deleted = tempPhoto.delete();
        Log.w("Delete Check", "File deleted: " + tempPhoto + ": " + deleted);
        hasTempPhoto = false;

        // Save remaining entry into shared preference
        saveUserInfo();

        // Inform user that the profile information is saved
        Toast.makeText(getApplicationContext(), "저장되었습니다.",
                Toast.LENGTH_SHORT).show();
    }

    public void selectCancel(View v) {
        // Close the activity
        finish();
    }

    public void changePhoto(View v) {
        // 일단 아무거나 적었
        // 사진을 찍어 외부저장소 공용공간에 저장한다.
        // 내부저장소는 앱마다 각각 갖고 있는 있는 장소이고
        // 내부저장소에는 해당 앱만 접근할 수 있으므로
        // 이 앱의 내부저장소에는 카메라앱이 사진을 저장할 수 없다.

        // API 23 이상에서는 앱 설치 때 외부저장소 접근 권한을 얻었다 하더라고
        // 실행 중에 외부저장소 접근 권한을 명시적으로 추가로 얻어야 한다.

        //외부 저장소 쓰기 권한이 없는 경우에는 권한을 요청한다.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // 앱을 처음으로 실행한 경우에는 권한이 없다. --> 권한 요청 --> 권한 부여 요청 대화창이 뜸.

            // 권한 부여 대화창에서 사용자가 처음 한 번 권한 부여를 거부한 후
            // 사용자가 다시 기능을 이용하려고 시도하면 재차 권한 부여를 요청하는 대화창이 뜨는데
            // 이 때부터는 대화창에 "다시 묻지 않음" 체크박스가 권한 부여 요청과 함께 보이게 된다.

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_CHECK_CODE);
        } else {
            // 권한을 이미 갖고 있는 경우에는 사진 얻기를 시작한다.
            getPhoto();
        }
    }

    // Dialog 창을 띄워 카메라로 찍을 것인지, 갤러리에서 선택할 것인지 물어 본다.
    private void getPhoto() {
        DialogFragment photoPickerDialog = MyRunsDialogFragment.newInstance(MyRunsDialogFragment.PHOTO_PICKER_ID);
        photoPickerDialog.show(getFragmentManager(), "photo_picker_tag");
    }

    // 권한 요청에 대해 사용자가 응답하면 이 메소드가 호출된다.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == WRITE_PERMISSION_CHECK_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한을 새로 얻음.
                // 사진 얻는 작업 시작.
                getPhoto();
            }
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // 권한을 왜 요청하는지 사용자에게 설명해야 하나?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // 사용자가 권한을 허용하지 않은 적이 있을 때는
                    // shouldShowRequestPermissionRationale가 true이다.
                    // 이 권한이 왜 필요한지 사용자에게 설명해 줄 필요가 있다.

                    // 사용자가 권한을 허용 여부를 아직 한 번도 응답하지 않은 때는
                    // shouldShowRequestPermissionRationale가 false이다.
                    // 이 권한이 왜 필요한지 설명해 주는 게 오히려 번거로운 일일 수 있기 때문이다.
                    // (이 프로그램에서는 사용자가 권한 부여에 대해 한 번도 응답하지 않은 경우에는
                    //  이 곳으로 올 수 없다! 권한 요청에 대한 응답이 있었기 때문에 이 곳으로 왔다!)

                    Toast.makeText(this, "사진을 찍어 외부 저장소에 저장해야 하므로 외부저장소 접근권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                } else {
                    // 사용자가 권한을 허용하지 않으면서 "다시 묻지 않음"을 체크한 이후에는
                    // shouldShowRequestPermissionRationale가 false이다.

                    // 맨 처음 권한 요청 대화창에는 "다시 묻지 않음" 체크 박스가 없다.
                    // 맨 처음 권한 요청 대화창에서 권한 부여를 거부하고 나면
                    // 이 기능을 수행하기 위해 왜 권한이 필요한지 설명이 보이게 된다.
                    // 그 이후에도 사용자가 권한 부여를 거부하고 "다시 이상 묻지 않기"를 설정했다면
                    // 사용자를 더 이상 귀찮게 하지 말고 기능 자체를 불능화한다.
                    changePhotoButton.setEnabled(false);
                }
            }
        }
    }

    /**
     * 카메라 혹은 갤러리로부터 사진을 가져오도록 한다.
     * @param whichItem 카메라, 혹은 갤러리
     */
    public void selectPhotoPickerItem(int whichItem) {
        Intent intent;
        // 외부저장소 쓰기 권한이 있는 경우에만 아래 코드를 실행한다.

        // 사진을 저장할 외부저장소 공용공간 내 파일 Uri.
        mImageUri = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "temp_"
                + String.valueOf(System.currentTimeMillis()) + ".jpg"));

        switch (whichItem) {
            case MyRunsDialogFragment.SELECT_FROM_CAMERA:

                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // 카메라로 찍은 사진을 저장할 파일 Uri를 인텐트에 적어준다.
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);

                try {
                    // 카메라 앱을 구동한다.
                    startActivityForResult(intent, REQUEST_CAMERA_CAPTURE);
                    // API 24 이상에서는 file:// 형태의 Uri를 package 경계 넘어로 (다른 앱에게) 보낼 수 없다.
                    // 즉 이 앱에서 카메라 앱으로 file:// 형태의 Uri를 보낼 수 없다.
                    // content:// 형태의 Uri만을 보낼 수 있다.
                    // 그래서 위 문장에서  android.os.FileUriExposedException 이 발생한다.
                    // API 24 이상의 target을 지원하려면 파일을 저장할 곳 Uri를 얻기 위해
                    // (file:// 형태의 Uri를 반환하는) Uri.fromFile 메소드를 사용하는 대신
                    // (content://<authority>/<path> 형태의 Uri를 반환하는) FileProvider.getUriForFile 메소드를 사용해야 한다.
                    // 위에 있는 Uri.fromFile을 단순히 FileProvider.geUriForFile로 바꾸면 되는 것은 아니다. 부가적인 작업이 필요하다.
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }

                isFromCamera = true;
                break;

            case MyRunsDialogFragment.SELECT_FROM_GALLERY:

                // Take photo from gallery
                intent = new Intent(Intent.ACTION_PICK);
                intent.setType(IMAGE_UNSPECIFIED);

                // Save photo temporarily
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                intent.putExtra("return-data", true);

                try {
                    // Trigger the cropping activity
                    startActivityForResult(intent, REQUEST_PICK_IMAGE);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                break;

            default:
                return;
        }
    }


    // 카메라 액티비티나 갤러리 액티비티, cropper 액티비티가 완료되면 이 메소드가 호출된다.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case REQUEST_CAMERA_CAPTURE:    // 카메라 액티티비가 종료한 경우
                if (resultCode == RESULT_OK)
                    // Start to crop image
                    // 아래 사이트에 게시된 cropper 라이브러리를 사용한다.
                    // https://github.com/ArthurHub/Android-Image-Cropper
                    CropImage.activity(mImageUri)
                            .start(this);
                break;

            case REQUEST_PICK_IMAGE:    // 갤러리 액티비티가 종료한 경우.
                mImageUri = data.getData();
                // Start to crop image
                // 아래 사이트에 게시된 cropper 라이브러리를 사용한다.
                // https://github.com/ArthurHub/Android-Image-Cropper
                CropImage.activity(mImageUri)
                        .start(this);
                break;

            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:         // Cropper 액티비티가 종료한 경우

                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    mImageView.setImageURI(resultUri);
                    savePhoto(TEMP_IMAGE_FILENAME); // ImageView에 보이는 이미지를 temp 파일에 저장한다.
                    hasTempPhoto = true;
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    error.printStackTrace();
                }
                // 카메라로 찍은 원본 사진을 삭제한다.
                File tempPhoto = new File(mImageUri.getPath());
                if (tempPhoto.exists())
                    tempPhoto.delete();
                break;
        }
    }

    // ImageView에 보이는 사진을 내부저장소의 파일에 저장한다.
    // path: 사진을 저장할 파일 이름.
    private void savePhoto(String path) {

        mImageView.buildDrawingCache();
        Bitmap bmap = mImageView.getDrawingCache();
        try {
            // Save profile photo into internal storage
            FileOutputStream wFile = openFileOutput(
                    path, MODE_PRIVATE);
            bmap.compress(Bitmap.CompressFormat.PNG, 100, wFile);
            wFile.flush();
            wFile.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void loadPhoto(String path) {

        try {
            // Retrieve photo from internal storage
            FileInputStream rFile = openFileInput(path);
            Bitmap bmap = BitmapFactory.decodeStream(rFile);
            mImageView.setImageBitmap(bmap);
            rFile.close();

        } catch (IOException e) {
            // Set to default profile pict if nothing is stored
            mImageView.setImageResource(R.drawable.profile_icon);
        }
    }

    private void saveUserInfo() {

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        final SharedPreferences.Editor edit = prefs.edit();

        EditText textName = (EditText) findViewById(R.id.edit_name);
        edit.putString("name", textName.getText().toString());

        EditText textEmail = (EditText) findViewById(R.id.edit_email);
        edit.putString("email", textEmail.getText().toString());

        EditText textPhone = (EditText) findViewById(R.id.edit_phone);
        edit.putString("phone", textPhone.getText().toString());

        EditText textClass = (EditText) findViewById(R.id.edit_class);
        edit.putString("class", textClass.getText().toString());

        EditText textMajor = (EditText) findViewById(R.id.edit_major);
        edit.putString("major", textMajor.getText().toString());

        RadioButton isFemale = (RadioButton) findViewById(R.id.radioButton_female);
        edit.putBoolean("is_female", isFemale.isChecked());

        RadioButton isMale = (RadioButton) findViewById(R.id.radioButton_male);
        edit.putBoolean("is_male", isMale.isChecked());

        edit.putBoolean("changePhotoButtonEnabled", changePhotoButton.isEnabled());

        // Commit change into shared preference
        edit.commit();
    }

    private void loadUserInfo() {

        // Retrieve information from shared preference
        // 주어진 이름의 SaredPreferences가 없는 경우 새로 만들어진다.
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        EditText textName = (EditText) findViewById(R.id.edit_name);
        // 빈 문자열("")을 textView에 적으면 "힌트" 문자열이 지워지지 않고 그대로 유지된다.
        textName.setText(prefs.getString("name", ""));

        EditText textEmail = (EditText) findViewById(R.id.edit_email);
        textEmail.setText(prefs.getString("email", ""));

        EditText textPhone = (EditText) findViewById(R.id.edit_phone);
        textPhone.setText(prefs.getString("phone", ""));

        EditText textClass = (EditText) findViewById(R.id.edit_class);
        textClass.setText(prefs.getString("class", ""));

        EditText textMajor = (EditText) findViewById(R.id.edit_major);
        textMajor.setText(prefs.getString("major", ""));

        RadioButton isFemale = (RadioButton) findViewById(R.id.radioButton_female);
        isFemale.setChecked(prefs.getBoolean("is_female", false));

        RadioButton isMale = (RadioButton) findViewById(R.id.radioButton_male);
        isMale.setChecked(prefs.getBoolean("is_male", false));

        changePhotoButton.setEnabled(prefs.getBoolean("changePhotoButtonEnabled", true));

    }

    // 외부저장소가 있는지 여부를 확인한다.
    public boolean externalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

}




