package com.qhhz.cocos.libandroid;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class PermissionTipDialog extends Dialog implements View.OnClickListener {
    // 为了提供更好的广告服务，请允许以下权限：
    // <b>XXXX权限</b><br/>用于XXXXXXX
    public static String Title = "这是一个标题：";
    public static String ContentHtml = "<b>这里是内容</b>";
    public static IVoidCallback Callback = null;

    public PermissionTipDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_permision_tip);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);
        //自定义Dialog宽度
        ((TextView) findViewById(R.id.title)).setText(PermissionTipDialog.Title);
        ((TextView) findViewById(R.id.content)).setText(Html.fromHtml(PermissionTipDialog.ContentHtml));
        ((Button) findViewById(R.id.next)).setOnClickListener(v -> {
            dismiss();
            PermissionTipDialog.Callback.callback();
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                break;
        }
    }

//
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_permision_tip);
//
//        TextView titleT = (TextView) findViewById(R.id.title);
//        titleT.setText(Title);
//
//        TextView contentT = (TextView) findViewById(R.id.content);
//        contentT.setText(Html.fromHtml(ContentHtml));
//
//        Button btn = (Button) findViewById(R.id.next);
//        btn.setOnClickListener(v -> {
//            if (Callback != null) Callback.callback();
//            this.finish();
//        });
//    }

}
