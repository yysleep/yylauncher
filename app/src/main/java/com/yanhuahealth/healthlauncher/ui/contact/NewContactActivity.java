



package com.yanhuahealth.healthlauncher.ui.contact;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.contact.Contact;
import com.yanhuahealth.healthlauncher.sys.ContactMgr;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.ui.base.simplecropimage.CropImage;
import com.yanhuahealth.healthlauncher.utils.Utilities;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 新建联系人页面.
 */
public class NewContactActivity extends YHBaseActivity {
    private EditText inputName;
    private EditText inputNumberOne;
    private EditText inputNumberTwo;
    private ImageView headImageView;
    private ProgressDialog progressDialog;

    String contactName;
    String contactNumberOne;
    String contactNumberTwo;
    private Bitmap headImage;
    private Uri uriAvatarFile;

    @Override
    protected String tag() {
        return NewContactActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_contact);
        initView();
    }

    public void initView() {
        inputName = (EditText) findViewById(R.id.input_name_et);
        inputNumberOne = (EditText) findViewById(R.id.input_number1_et);
        inputNumberTwo = (EditText) findViewById(R.id.input_number2_et);
        headImageView = (ImageView) findViewById(R.id.add_head_image_iv);
        headImageView.setImageResource(R.drawable.ic_head_image);
        NavBar navBar = new NavBar(this);
        navBar.setTitle("新建联系人");
        navBar.hideRight();

        // 拨号界面传递过来的电话号码显示
        if (getIntent() != null) {
            if (getIntent().getStringExtra("CallNumber") != null) {
                inputNumberOne.setText(getIntent().getStringExtra("CallNumber"));

                if (getIntent().getStringExtra("CallName") != null) {
                    inputNumberOne.setText(getIntent().getStringExtra("CallName"));
                }
            }
        }
    }

    public void yanhuaOnClick(View v) {
        switch (v.getId()) {

            case R.id.cancel_new_btn:
                finish();
                break;

            case R.id.add_head_image_ll:
                String[] imageWay = new String[]{"本地相册上传", "拍照上传", "删除头像"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(NewContactActivity.this);
                builder.setItems(imageWay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intentGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intentGallery, 0xa1);
                            dialog.dismiss();
                        } else if (which == 1) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            uriAvatarFile = Uri.fromFile(getOutputMediaFile());
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriAvatarFile);
                            startActivityForResult(intent, 0xa2);
                            dialog.dismiss();
                        } else if (which == 2) {
                            headImageView.setImageResource(R.drawable.ic_head_image);
                            headImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_head_image);
                            dialog.dismiss();
                        }
                    }
                });
                builder.create().show();
                break;

            case R.id.save_new_btn:

                // 保存联系人按钮触发事件
                contactName = inputName.getText().toString().trim();
                contactNumberOne = inputNumberOne.getText().toString().trim() + "";
                contactNumberTwo = inputNumberTwo.getText().toString().trim() + "";
                if (contactNumberOne.equals("") && !contactNumberTwo.equals("")) {
                    contactNumberOne = contactNumberTwo;
                    contactNumberTwo = "";
                }
                if ((contactName == null || contactName.equals("")) ||
                        ((contactNumberOne == null || contactNumberOne.equals("")) && (contactNumberTwo.equals("")))) {
                    Toast.makeText(NewContactActivity.this, "号码和姓名不可以为空", Toast.LENGTH_LONG).show();
                    return;
                }
                new contactTask().execute();
                break;

            default:
                break;

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // 从图库获取
            case 0xa1:
                if (resultCode == RESULT_OK) {
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(data.getData(),
                            filePathColumn, null, null, null);
                    assert cursor != null;
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();
                    startCropImage(this, new File(picturePath));
                }
                break;

            // 拍照获取
            case 0xa2:
                if (resultCode == RESULT_OK) {
                    if (uriAvatarFile != null) {
                        startCropImage(this, new File(uriAvatarFile.getPath()));
                    }
                }
                break;

            // 得到获取的头像
            case 0xa4:
                if (resultCode == RESULT_OK) {
                    String cropPath = data.getStringExtra(CropImage.IMAGE_CROPPED_PATH);
                    if (cropPath == null) {
                        return;
                    }

                    Bitmap photo = BitmapFactory.decodeFile(cropPath);
                    headImageView.setImageBitmap(Utilities.createCircleBitmap(photo));
                    headImage = photo;
                }
                break;

            default:
                return;
        }
    }

    public File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "medical");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                YHLog.d(tag(), "getOutputMediaFile - failed to create [medical] directory!");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath()
                + File.separator + "IMG_" + timeStamp + ".jpg");
        return mediaFile;
    }

    public static void startCropImage(Activity context, File file) {
        Intent intent = new Intent(context, CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, file.getPath());
        String[] pathSegs = file.getPath().split("\\.");
        String expandName = pathSegs[pathSegs.length - 1];
        String imageCroppedPath = LauncherConst.getImageRootPath() + "tmp-avatar-"
                + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "." + expandName;
        intent.putExtra(CropImage.IMAGE_CROPPED_PATH, imageCroppedPath);
        intent.putExtra(CropImage.SCALE, true);
        intent.putExtra(CropImage.ASPECT_X, 1);
        intent.putExtra(CropImage.ASPECT_Y, 1);
        context.startActivityForResult(intent, 0xa4);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ContactMgr.getInstance().isChange = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        ContactMgr.getInstance().isChange = true;
    }
    class contactTask extends AsyncTask<Void, Void,Void > {

        @Override
        protected Void doInBackground(Void... params) {
            long rawContactId = ContactMgr.getInstance().addContactToDB(NewContactActivity.this,
                    contactName, contactNumberOne, contactNumberTwo, headImage);
            Contact contact = ContactMgr.getInstance().getContactByRawContactId(rawContactId);
            if (contact != null) {
                Intent intent = getIntent();
                intent.putExtra(LauncherConst.INTENT_PARAM_CONTACT_NAME, contactName);
                intent.putExtra(LauncherConst.INTENT_PARAM_CONTACT_ID, contact.contactId);
                intent.putExtra(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, rawContactId);
                setResult(ContactMgr.RESULT_CODE_CONTACT, intent);
            }
            finish();
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(NewContactActivity.this, null, "正在保存");
        }

        @Override
        protected void onPostExecute(Void avoid) {
            super.onPostExecute(avoid);
            progressDialog.dismiss();
        }
    }
}
