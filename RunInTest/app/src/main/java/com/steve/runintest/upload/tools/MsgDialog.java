package com.steve.runintest.upload.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * 自定义信息确认对话框，需在实例化时，设置确认键的监听器
 * Created by derik on 17-2-14.
 */

public class MsgDialog {
    private AlertDialog dialog;

    public MsgDialog(Context context, String msg, DialogInterface.OnClickListener listener) {
        dialog = new AlertDialog.Builder(context).setMessage(msg).setPositiveButton("确定", listener).setNegativeButton("取消", null).create();

    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

}
