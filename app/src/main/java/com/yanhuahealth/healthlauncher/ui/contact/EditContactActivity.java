package com.yanhuahealth.healthlauncher.ui.contact;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yanhuahealth.healthlauncher.HealthLauncherApplication;
import com.yanhuahealth.healthlauncher.R;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.model.sys.appmgr.Shortcut;
import com.yanhuahealth.healthlauncher.sys.ContactMgr;
import com.yanhuahealth.healthlauncher.sys.appmgr.ShortcutMgr;
import com.yanhuahealth.healthlauncher.ui.base.NavBar;
import com.yanhuahealth.healthlauncher.ui.base.YHBaseActivity;
import com.yanhuahealth.healthlauncher.ui.base.simplecropimage.CropImage;
import com.yanhuahealth.healthlauncher.utils.DialogUtil;
import com.yanhuahealth.healthlauncher.utils.Utilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 编辑删除联系人
 */
public class EditContactActivity extends YHBaseActivity {
    private EditText inputName;
    private EditText inputNumberOne;
    private EditText inputNumberTwo;
    private ImageView headImageView;
    private TextView changeBitmap;

    private String phoneNumber = null;
    private String phoneNumberTwo = null;
    private String phoneName = null;
    private Bitmap editHeadImage;

    private long rawContactId;
    private long contactId;

    // 更改头像
    private Bitmap editBitmapIcon;
    private Uri uriAvatarFile;
    private DialogUtil dialogUtil;

    // 如果是通过 shortcut 来编辑
    // 则保存对应的 shortcut 的 标识
    private int shortcutId;

    // 用于控制用户当前的头像是否为默认头像
    private boolean isDefaultHead;

    @Override
    protected String tag() {
        return EditContactActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_contact);
        init();
    }

    private void init() {
        LinearLayout changeHeadImage = (LinearLayout) findViewById(R.id.add_head_image_ll);
        inputName = (EditText) findViewById(R.id.input_name_et);
        inputNumberOne = (EditText) findViewById(R.id.input_number1_et);
        inputNumberTwo = (EditText) findViewById(R.id.input_number2_et);
        headImageView = (ImageView) findViewById(R.id.add_head_image_iv);
        changeBitmap = (TextView) findViewById(R.id.change_bitmap);
        Button saveEdit = (Button) findViewById(R.id.save_new_btn);
        Button cancel = (Button) findViewById(R.id.cancel_new_btn);
        NavBar navBar = new NavBar(this);
        navBar.setTitle("编辑联系人");
        navBar.hideRight();

        Intent intent = getIntent();
        editBitmapIcon = intent.getParcelableExtra("toEditBitmap");

        // 如果当前编辑的联系人已经添加至桌面，则根据 shortcutId 来获取对应的 shortcut 信息
        shortcutId = intent.getIntExtra(LauncherConst.INTENT_PARAM_SHORTCUT_ID, -1);
        if (shortcutId > 0) {
            Shortcut shortcut = ShortcutMgr.getInstance().getShortcut(shortcutId);
            if (shortcut != null && shortcut.icon != null) {
                editBitmapIcon = shortcut.icon;
            }
        }

        contactId = intent.getLongExtra(LauncherConst.INTENT_PARAM_CONTACT_ID, -1);
        if (contactId <= 0) {
            finish();
            return;
        }

        ArrayList<String> contactInfo = ContactMgr.getInstance().getContactInfo(this, contactId);
        if (contactInfo == null || contactInfo.size() < 2) {
            finish();
            return;
        }

        phoneName = contactInfo.get(0);
        phoneNumber = contactInfo.get(1);
        if (contactInfo.size() > 2) {
            phoneNumberTwo = contactInfo.get(2);
        }

        rawContactId = intent.getLongExtra(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, -1);
        if (rawContactId <= 0) {
            finish();
            return;
        }

        dialogUtil = new DialogUtil(EditContactActivity.this, new DialogUtil.OnDialogUtilListener() {
            @Override
            public void onClick(View view) {
                headImageView.setImageResource(R.drawable.ic_head_image);
                editHeadImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_head_image);
                dialogUtil.dismiss();
            }
        });

        saveEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contactName = inputName.getText().toString().trim();
                String contactNumberOne = inputNumberOne.getText().toString().trim();
                String contactNumberTwo = inputNumberTwo.getText().toString().trim();

                if (contactNumberOne.equals("") && !contactNumberTwo.equals("")) {
                    contactNumberOne = contactNumberTwo;
                    contactNumberTwo = "";
                }
                if (!contactNumberOne.equals("") && !contactNumberTwo.equals("") && contactNumberOne.equals(contactNumberTwo)) {
                    contactNumberTwo = "";
                }

                // 判断编辑的时候至少需要填写一个号码，才允许保存
                if (!contactName.equals("") && (!contactNumberOne.equals("") || !contactNumberTwo.equals(""))) {
                    ContactMgr.getInstance().updateContact(EditContactActivity.this,
                            contactName, contactNumberOne, contactNumberTwo, isDefaultHead,
                            editHeadImage, rawContactId, contactId);
                } else {
                    Toast.makeText(EditContactActivity.this, "姓名和号码不能为空", Toast.LENGTH_LONG).show();
                    return;
                }

                if (shortcutId > 0) {
                    Intent intent = getIntent();
                    intent.putExtra(LauncherConst.INTENT_PARAM_CONTACT_NAME, contactName);
                    intent.putExtra(LauncherConst.INTENT_PARAM_CONTACT_ID, contactId);
                    intent.putExtra(LauncherConst.INTENT_PARAM_RAW_CONTACT_ID, rawContactId);
                    setResult(ContactMgr.RESULT_CODE_CONTACT, intent);
                    finish();
                } else {
                    finish();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 改变头像
        changeHeadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] imageWay = new String[]{"本地相册", "拍照上传", "删除头像"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(EditContactActivity.this);
                builder.setItems(imageWay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intentGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intentGallery, 0xa1);
                            dialog.dismiss();
                        } else if (which == 1) {
                            Intent intentPhotoPraph = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            uriAvatarFile = Uri.fromFile(getOutputMediaFile());
                            intentPhotoPraph.putExtra(MediaStore.EXTRA_OUTPUT, uriAvatarFile);
                            startActivityForResult(intentPhotoPraph, 0xa2);
                            dialog.dismiss();
                        } else if (which == 2) {
                            headImageView.setImageResource(R.drawable.ic_head_image);
                            editHeadImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_head_image);
                            dialog.dismiss();
                        }
                    }
                });
                builder.create().show();
            }
        });

        changeHeadImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dialogUtil.showFirstStyleDialog("删除头像", "是否删除头像", "删除");
                return true;
            }
        });

        initView();
    }

    private void initView() {
        // 得到联系人信息（姓名,号码，头像）
        ContentResolver resolver = getContentResolver();
        Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, " sort_key asc");
        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {
                // 获取用户头像
                if (editBitmapIcon == null) {
                    // 如果还没有获取到头像（如：不能够根据 shortcut 获取到对应的头像）
                    // 则再次根据 rawContactId 从本地数据库中获取该联系人的头像
                    // 当从联系人列表选择跳转时头像获取方式如下
                    Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
                    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(), uri);
                    if (input != null) {
                        editHeadImage = Utilities.createCircleBitmap(BitmapFactory.decodeStream(input));
                        isDefaultHead = false;
                    } else {
                        editHeadImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_head_image);
                        isDefaultHead = true;
                    }
                } else {
                    editHeadImage = editBitmapIcon;
                    changeBitmap.setText("修改头像");
                    isDefaultHead = false;
                }
            }

            phoneCursor.close();
        }

        // 页面表单设置
        inputName.setText(phoneName);

        if (phoneNumber != null) {
            inputNumberOne.setText(phoneNumber);
        }

        if (phoneNumberTwo != null) {
            inputNumberTwo.setText(phoneNumberTwo);
        }

        if (editHeadImage != null) {
            headImageView.setImageBitmap(editHeadImage);
        } else {
            headImageView.setImageResource(R.drawable.ic_head_image);
        }
    }

    /*
    *   联系人的每一条 data 记录
    *   一个联系人有多个号码，则会有多条 ContactPhoneData 记录
    */
    class ContactData {
        // 对应于 DATA 表中的主键标识
        public long id;

        // 用户手机号码
        public String phoneNumber;

        public ContactData(long id, String phoneNumber) {
            this.id = id;
            this.phoneNumber = phoneNumber;
        }

        @Override
        public String toString() {
            return "ContactPhoneData{" +
                    "id=" + id +
                    ", phoneNumber='" + phoneNumber + '\'' +
                    '}';
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
                    editHeadImage = photo;
                }
                break;

            default:
                break;
        }
    }

    // 更新联系人头像
    public static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "medical");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("medical", "failed to create [medical] directory!");

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

}
