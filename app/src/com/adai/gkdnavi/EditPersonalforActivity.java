package com.adai.gkdnavi;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.adai.gkd.bean.UserInfoBean;
import com.adai.gkd.bean.request.UserSingleupPagebean;
import com.adai.gkd.contacts.CurrentUserInfo;
import com.adai.gkd.contacts.RequestMethods;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.utils.ImageLoadHelper;
import com.adai.gkdnavi.utils.ImageUriUtil;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.VoiceManager;
import com.example.ipcamera.application.VLCApplication;
import com.photocrop.Crop;
import com.photopicker.PhotoPickerActivity;
import com.widget.piechart.ScreenUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class EditPersonalforActivity extends BaseActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_SELECT_PHOTO = 1;
    private static final int REQUEST_CODE_CUT_PHOTO = 2;
    private static final int REQUEST_CODE_SET_NICKNAME = 3;
    private static final int REQUEST_CODE_SET_SIGN = 4;
    private static final int REQUEST_CODE_SET_SET = 5;
    private ImageView headimg, QR_Code;
    private LinearLayout lineheadimg, line_QR_Code;
    private TextView mTvNickname, id, mTvSex;
    private LinearLayout linenickname, mLlSex;
    private TextView sign;
    private LinearLayout linesign;
    private Button quit;
    private boolean isInfoChange = false;
    private String imagePath = null;
    private ImageView saveInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_personalfor);
        initView();
        init();
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(getString(R.string.personal_info));
        this.quit = (Button) findViewById(R.id.quit);
        this.linesign = (LinearLayout) findViewById(R.id.line_sign);
        this.sign = (TextView) findViewById(R.id.sign);
        mTvSex = (TextView) findViewById(R.id.tv_sex);
        mLlSex = (LinearLayout) findViewById(R.id.ll_sex);
        this.linenickname = (LinearLayout) findViewById(R.id.line_nickname);
        this.mTvNickname = (TextView) findViewById(R.id.nickname);
        this.id = (TextView) findViewById(R.id.id);
        this.lineheadimg = (LinearLayout) findViewById(R.id.line_headimg);
        this.headimg = (ImageView) findViewById(R.id.head_img);
        this.line_QR_Code = (LinearLayout) findViewById(R.id.line_QR_Code);
        this.QR_Code = (ImageView) findViewById(R.id.QR_Code);

        saveInfo = (ImageView) findViewById(R.id.right_img);
        saveInfo.setImageResource(R.drawable.bg_save_selector);
        saveInfo.setOnClickListener(this);
        lineheadimg.setOnClickListener(this);
        linenickname.setOnClickListener(this);
        linesign.setOnClickListener(this);
        line_QR_Code.setOnClickListener(this);
        mLlSex.setOnClickListener(this);
        quit.setOnClickListener(this);
    }

    @Override
    protected void init() {
        super.init();
//        ImageLoaderUtil.getInstance().loadImageWithoutCache(this,CurrentUserInfo.portrait,R.drawable.default_header_img,headimg);
        ImageLoadHelper.getInstance().displayImageWithoutCatch(CurrentUserInfo.portrait, headimg, R.drawable.default_header_img);
        mTvNickname.setText(CurrentUserInfo.nickname);
        id.setText(CurrentUserInfo.id + "");
        if ("M".equals(CurrentUserInfo.sex)) {
            mTvSex.setText(getString(R.string.man));
        } else if ("F".equals(CurrentUserInfo.sex)) {
            mTvSex.setText(getString(R.string.female));
        } else {
            mTvSex.setText(getString(R.string.secrecy));
        }
        QR_Code.setImageResource(R.drawable.usersetting_nomal);
        sign.setText(CurrentUserInfo.signature);
    }

    private EditText et_nickname;

    private void showNicknameEdit() {
        et_nickname = new EditText(mContext);
        et_nickname.setText(mTvNickname.getText());
        new AlertDialog.Builder(mContext).setView(et_nickname).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!et_nickname.getText().toString().equals(mTvNickname.getText())) {
                    mTvNickname.setText(et_nickname.getText().toString());
                    saveInfo.setVisibility(View.VISIBLE);
                }
            }
        }).setNegativeButton(R.string.cancel, null).create().show();
    }

    private EditText et_sign;
    private int maxLen = 50;

    private void showSignEdit() {
        et_sign = new EditText(mContext);
        et_sign.setText(sign.getText());
        et_sign.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Editable editable = et_sign.getText();
                int len = editable.length();
                if (len > maxLen) {
                    int selEndIndex = Selection.getSelectionEnd(editable);
                    String str = editable.toString();
                    //截取新字符串
                    String newStr = str.substring(0, maxLen);
                    et_sign.setText(newStr);
                    editable = et_sign.getText();
                    //新字符串的长度
                    int newLen = editable.length();
                    //旧光标位置超过字符串长度
                    if (selEndIndex > newLen) {
                        selEndIndex = editable.length();
                    }
                    //设置新光标所在的位置
                    Selection.setSelection(editable, selEndIndex);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        new AlertDialog.Builder(mContext).setView(et_sign).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!et_sign.getText().toString().equals(sign.getText())) {
                    sign.setText(et_sign.getText().toString());
                    saveInfo.setVisibility(View.VISIBLE);
                }
            }
        }).setNegativeButton(R.string.cancel, null).create().show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_sex:
//                Intent intent_sex = new Intent(this, setSexActivity.class);
//                startActivityForResult(intent_sex, REQUEST_CODE_SET_SET);
                showSetSexDialog();
                break;
            case R.id.line_headimg:
//                startSelectPhoto();
                selectPhoto();
                break;
            case R.id.line_nickname:
                //跳转到昵称修改界面
                showModifyNameDialog();
//                Intent intent_nickname = new Intent(EditPersonalforActivity.this, SetNickNameActivity.class);
//                startActivityForResult(intent_nickname, REQUEST_CODE_SET_NICKNAME);
                break;
            case R.id.line_sign:
                //跳转到个性签名修改界面
//                Intent intent_signature = new Intent(EditPersonalforActivity.this, SetSignatureActivity.class);
//                intent_signature.putExtra("sign", CurrentUserInfo.signature);
//                startActivityForResult(intent_signature, REQUEST_CODE_SET_SIGN);
                showModifySignatureDialog();
                break;
            case R.id.quit:
                logout();
                break;
            case R.id.right_img:
                saveInfo(imagePath, null, null, null);
                break;
            case R.id.line_QR_Code:
                //二维码界面 待添加
                break;
        }
    }


    private void showSetSexDialog() {
        final Dialog dialog = new Dialog(this, R.style.NoBackgroundDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_select_sex, null);
        final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.rg_sex);
        if ("M".equals(CurrentUserInfo.sex)) {
            radioGroup.check(R.id.rb_man);
        } else if ("F".equals(CurrentUserInfo.sex)) {
            radioGroup.check(R.id.rb_female);
        } else {
            radioGroup.check(R.id.rb_secret);
        }
        AppCompatButton btn_confirm = (AppCompatButton) view.findViewById(R.id.btn_confirm);
        AppCompatButton btn_cancel = (AppCompatButton) view.findViewById(R.id.btn_cancel);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.show();
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sexTag = "";
                int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                switch (checkedRadioButtonId) {
                    case R.id.rb_secret:
                        sexTag = "S";
                        break;
                    case R.id.rb_man:
                        sexTag = "M";
                        break;
                    case R.id.rb_female:
                        sexTag = "F";
                        break;
                }
                if (sexTag.equals(CurrentUserInfo.sex)) {
                    dialog.dismiss();
                    return;
                }
                dialog.dismiss();
                saveInfo(null, null, sexTag, null);
            }
        });

    }

    private void showModifySignatureDialog() {
        final Dialog dialog = new Dialog(this, R.style.NoBackgroundDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_modify_name, null);
        TextView tvTitle = (TextView) view.findViewById(R.id.title);
        tvTitle.setText(R.string.set_signature);
        final EditText editText = (EditText) view.findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Editable editable = editText.getText();
                int len = editable.length();
                if (len > maxLen) {
                    ToastUtil.showShortToast(EditPersonalforActivity.this, EditPersonalforActivity.this.getString(R.string.beyond_maximum));
                    int selEndIndex = Selection.getSelectionEnd(editable);
                    String str = editable.toString();
                    //截取新字符串
                    String newStr = str.substring(0, maxLen);
                    editText.setText(newStr);
                    editable = editText.getText();
                    //新字符串的长度
                    int newLen = editable.length();
                    //旧光标位置超过字符串长度
                    if (selEndIndex > newLen) {
                        selEndIndex = editable.length();
                    }
                    //设置新光标所在的位置
                    Selection.setSelection(editable, selEndIndex);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editText.setHint(R.string.please_input_signature);
        AppCompatButton btn_confirm = (AppCompatButton) view.findViewById(R.id.btn_confirm);
        AppCompatButton btn_cancel = (AppCompatButton) view.findViewById(R.id.btn_cancel);
        Window window = getWindow();
        DisplayMetrics outMetrics = new DisplayMetrics();
        window.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        int widthPixels = outMetrics.widthPixels;
        int heightPixels = outMetrics.heightPixels;
        int width = widthPixels > heightPixels ? heightPixels : widthPixels;
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width - ScreenUtils.dp2px(this, 80), ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(view, params);
        dialog.setCancelable(true);
        dialog.show();
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String signature = editText.getText().toString();
                if (TextUtils.isEmpty(signature)) {
                    showToast(R.string.signature_can_not_empty);
                    return;
                }
                dialog.dismiss();
                saveInfo(null, null, null, signature);
            }
        });
    }

    private void showModifyNameDialog() {
        final Dialog dialog = new Dialog(this, R.style.NoBackgroundDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_modify_name, null);
        final EditText editText = (EditText) view.findViewById(R.id.editText);
        AppCompatButton btn_confirm = (AppCompatButton) view.findViewById(R.id.btn_confirm);
        AppCompatButton btn_cancel = (AppCompatButton) view.findViewById(R.id.btn_cancel);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        Window window = getWindow();
        DisplayMetrics outMetrics = new DisplayMetrics();
        window.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        int widthPixels = outMetrics.widthPixels;
        int heightPixels = outMetrics.heightPixels;
        int width = widthPixels > heightPixels ? heightPixels : widthPixels;
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width - ScreenUtils.dp2px(this, 80), ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(view, params);
        dialog.setCancelable(true);
        dialog.show();
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = editText.getText().toString();
                int len = 0;
                String Reg = "([\u4e00-\u9fa5])";
                for (int i = 0; i < nickname.length(); i++) {
                    String b = Character.toString(nickname.charAt(i));
                    if (b.matches(Reg)) {//如果是中文长度多1
                        len++;
                    }
                    len++;
                }

                if (TextUtils.isEmpty(nickname.trim())) {
                    showToast(R.string.notnon_nickname);
                    return;
                } else if (len > 16) {
                    showToast(R.string.nickname_too_long);
                    return;
                }

                dialog.dismiss();
                saveInfo(null, nickname, null, null);
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void saveInfo(String headpath, final String nickname, String sex, String signature) {
        showpDialog();
        RequestMethods.userUpdate(headpath, nickname, sex, signature, new HttpUtil.Callback<UserSingleupPagebean>() {

            @Override
            public void onCallback(UserSingleupPagebean result) {
                // TODO Auto-generated method stub
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            showToast(R.string.Modify_success);
                            UserInfoBean userinfo = result.data;
//                            if (userinfo != null)
//                                userinfo.signature = sign.getText().toString();
                            if (userinfo == null) {
                                return;
                            }
                            CurrentUserInfo.saveUserinfo(mContext, userinfo);
                            mTvNickname.setText(CurrentUserInfo.nickname);
                            if ("M".equals(CurrentUserInfo.sex)) {
                                mTvSex.setText(getString(R.string.man));
                            } else if ("F".equals(CurrentUserInfo.sex)) {
                                mTvSex.setText(getString(R.string.female));
                            } else {
                                mTvSex.setText(getString(R.string.secrecy));
                            }
                            sign.setText(CurrentUserInfo.signature);
                            break;

                        default:
                            showToast(TextUtils.isEmpty(result.message) ? getString(R.string.Modify_failure) : result.message);
                            break;
                    }
                }
                hidepDialog();
            }
        });
    }

    private void startSelectPhoto() {
        Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
        getAlbum.setType("image/*");
        startActivityForResult(getAlbum, REQUEST_CODE_SELECT_PHOTO);
    }

    private void selectPhoto() {
        Intent photo = new Intent(mContext, PhotoPickerActivity.class);
        photo.putExtra(PhotoPickerActivity.EXTRA_SELECT_MODE, PhotoPickerActivity.MODE_SINGLE);
        photo.putExtra(PhotoPickerActivity.EXTRA_SHOW_CAMERA, true);
        startActivityForResult(photo, REQUEST_CODE_SELECT_PHOTO);
    }

    private void startCrop(String path) {
        Crop.of(Uri.fromFile(new File(path)), Uri.fromFile(new File(VLCApplication.TEMP_PATH + "/head.jpg"))).asSquare().withMaxSize(300, 300).start(EditPersonalforActivity.this);
    }

    @Override
    protected void goBack() {
        if (isInfoChange)
            setResult(RESULT_OK);
        super.goBack();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        isInfoChange = true;
        switch (requestCode) {
            case REQUEST_CODE_SET_SET:
                if (data != null) {
                    if ("M".equals(CurrentUserInfo.sex)) {
                        mTvSex.setText(getString(R.string.man));
                    } else if ("F".equals(CurrentUserInfo.sex)) {
                        mTvSex.setText(getString(R.string.female));
                    } else {
                        mTvSex.setText(getString(R.string.secrecy));
                    }
                }
                break;
            case REQUEST_CODE_SELECT_PHOTO:
                if (data != null) {
//                    System.out.println("11================");
//                    startPhotoZoom(data.getData());
                    ArrayList<String> result = data.getStringArrayListExtra(PhotoPickerActivity.KEY_RESULT);
                    if (result != null && result.size() > 0) {
                        startCrop(result.get(0));
                    }
                }
                break;
            case REQUEST_CODE_CUT_PHOTO:
                if (data != null && data.getExtras() != null) {
                    Bitmap photo = data.getExtras().getParcelable("data");
                    if (photo != null) {
                        headimg.setImageBitmap(photo);
                        headimg.postInvalidate();
                        imagePath = VLCApplication.TEMP_PATH + "/head.png";
                        try {
                            FileOutputStream fos = new FileOutputStream(imagePath);
                            photo.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.close();
                            saveInfo.setVisibility(View.VISIBLE);
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case Crop.REQUEST_CROP:
                Uri result = Crop.getOutput(data);
                imagePath = ImageUriUtil.getImageAbsolutePath(EditPersonalforActivity.this, result);
                headimg.setImageURI(null);
                headimg.setImageURI(result);
                saveInfo.setVisibility(View.VISIBLE);
                break;
            case REQUEST_CODE_SET_NICKNAME:
                if (data != null) {

                    Bundle bundle = data.getExtras();
                    String nick = bundle.getString("mTvNickname");//这句话显示错误
                    mTvNickname.setText(nick);
                }
                break;
            case REQUEST_CODE_SET_SIGN:
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    String signature = bundle.getString("signature");//这句话显示错误
                    sign.setText(signature);
                }
                break;
        }
    }

    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");

        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", 400);
        intent.putExtra("outputY", 400);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(VLCApplication.TEMP_PATH + "/head.jpg")));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        System.out.println("22================");
        startActivityForResult(intent, REQUEST_CODE_CUT_PHOTO);
    }


    private void logout() {
        showpDialog(R.string.action_Are_logged_out);
        //        EMClient.getInstance().logout(true, new EMCallBack() {
//
//            @Override
//            public void onError(int arg0, String arg1) {
//                // TODO Auto-generated method stub
//                Log.i("info", "loginOut error = ");
//                runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        // TODO Auto-generated method stub
//                        showToast(getResources().getString(R.string.Logout_failed));
//                    }
//                });
//            }
//
//            @Override
//            public void onProgress(int arg0, String arg1) {
//                // TODO Auto-generated method stub
//                Log.i("info", "loginOut onProgress = " + arg0);
//            }
//
//            @Override
//            public void onSuccess() {
//                // TODO Auto-generated method stub
//                Log.i("info", "loginout onSuccess");// 在这里回调了注销成功
////						pd.dismiss();
//                runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        // TODO Auto-generated method stub
        hidepDialog();
        CurrentUserInfo.clearUserinfo(mContext);
//                        ImDBhelper.getInstance(mContext).clearFriends();
        startActivity(MainTabActivity.class);
//                    }
//                });
        VoiceManager.isLogin = false;
//            }
//
//        });

    }
}
