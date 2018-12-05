package com.adai.gkdnavi.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adai.gkdnavi.R;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/11/8 16:17
 * @updateAuthor $Author$
 * @updateDate $Date$
 */

public class InputDialog extends Dialog implements View.OnClickListener {
    private TextView mTitle;
    private TextView mConfirm;
    private TextView mCancel;
    private TextView mInputName1;
    private TextView mInputName2;
    private LinearLayout mInputLayout2;
    private EditText mEt1, mEt2;

    public interface InputConfirmListener {
        void confirm(String input1, String input2);
    }

    InputConfirmListener mInputConfirmListener;

    private void assignViews() {
        mTitle = (TextView) findViewById(R.id.title);
        mConfirm = (TextView) findViewById(R.id.confirm);
        mCancel = (TextView) findViewById(R.id.cancel);
        mInputName1 = (TextView) findViewById(R.id.input_name1);
        mInputName2 = (TextView) findViewById(R.id.input_name2);
        mInputLayout2 = (LinearLayout) findViewById(R.id.input2);
        mEt1 = (EditText) findViewById(R.id.et1);
        mEt2 = (EditText) findViewById(R.id.et2);
    }

    public InputDialog(Context context) {
        this(context, 0);
    }

    public InputDialog(Context context, int themeResId) {
        super(context, themeResId);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_input);
        initWidth();
        assignViews();
        initEvent();
    }

    private void initWidth() {
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        DisplayMetrics outMetrics = new DisplayMetrics();
        window.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        float density = outMetrics.density;
        int widthPixels = outMetrics.widthPixels;
        int heightPixels = outMetrics.heightPixels;
        int width = widthPixels > heightPixels ? heightPixels : widthPixels;
        attributes.width = (int) (width - 32* density);
        window.setAttributes(attributes);
    }

    public InputDialog setTitle(String title) {
        mTitle.setText(title);
        return this;
    }

    public InputDialog setContent(int inputNum, String inputName1, String inputName2, InputConfirmListener inputConfirmListener) {
        if (inputNum > 2 || inputNum < 1) {
            throw new IllegalAccessError("inputNum only 1 or 2");
        }
        mInputConfirmListener = inputConfirmListener;
        if (inputName1 == null) {

            return this;
        }
        if (inputNum == 1) {
//            mEt1.setInputType(InputType.TYPE_CLASS_TEXT);
            mInputLayout2.setVisibility(View.GONE);
        } else {
            mInputName2.setText(inputName2 == null ? "" : inputName2);
        }
        mInputName1.setText(inputName1);
        return this;
    }

    private void initEvent() {
        mConfirm.setOnClickListener(this);
        mCancel.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                mInputConfirmListener.confirm(mEt1.getText().toString(), mEt2.getText().toString());
                dismiss();
                break;
            case R.id.cancel:
                dismiss();
                break;
        }
    }
}
