package com.qhhz.cocos.libandroid;


import android.app.Activity;
#IFUM
import android.app.Application;
#ENDUM
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
#IFUM
import com.umeng.commonsdk.UMConfigure;
#ENDUM

public class ProtocolConfirmActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Runkit.get().isAgreeProtocol()) {
            processNext();
        } else {
            setContentView(R.layout.activity_protocol_confirm);
            Runkit.FullScreen(getWindow());
            initConfirmContent();
            Button btnRje = (Button) findViewById(R.id.btnReject);
            Button btnAge = (Button) findViewById(R.id.btnAgree);
            btnRje.setOnClickListener(v -> {
                android.os.Process.killProcess(android.os.Process.myPid());
            });
            btnAge.setOnClickListener(v -> {
                Runkit.get().userAgreeProtocol();
                this.processNext();
            });
        }
    }

    private void processNext() {
        Runkit.get().onConfirmProtocol();
#IFUM
        Application app = Runkit.get().app();
        UMConfigure.init(app, app.getResources().getString(R.string.um_appkey), app.getResources().getString(R.string.CHANNEL), UMConfigure.DEVICE_TYPE_PHONE, "");
#ENDUM
        this.finish();
    }

    private void initConfirmContent() {
        TextView t = (TextView) findViewById(R.id.protocolConfirmContent);
        String str = "感谢您使用本产品，请在使用前认真阅读\n" + "《隐私声明》，《用户协议》确认同意后即可" + "开启服务。使用过程中，我们将在根据具体功" + "能需要获取集项手机权限时，再次请您确认同" + "意，并在条款说明的范围内收集、使用、共享" + "并保护您的个人信息;如您拒绝开启权限，将不" + "影响其他功能的使用。\n客服QQ：2422025128";

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        Activity self = this;
        ssb.append(str);

        final int start = str.indexOf("《");//第一个出现的位置
        ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                self.startActivity(new Intent(self, PrivateActivity.class));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.text_click_blue));       //设置文件颜色
                // 去掉下划线
                ds.setUnderlineText(false);
            }

        }, start, start + 6, 0);

        final int end = str.lastIndexOf("《");//最后一个出现的位置
        ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                self.startActivity(new Intent(self, ProtocolActivity.class));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.text_click_blue));       //设置文件颜色
                // 去掉下划线
                ds.setUnderlineText(false);
            }

        }, end, end + 6, 0);

        t.setMovementMethod(LinkMovementMethod.getInstance());
        t.setText(ssb, TextView.BufferType.SPANNABLE);
    }
}