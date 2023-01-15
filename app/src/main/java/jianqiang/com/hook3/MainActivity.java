package jianqiang.com.hook3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import jianqiang.com.hook3.hook.AMSHookHelper;
import jianqiang.com.hook3.hook.HookUtil;

public class MainActivity extends Activity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        if (android.os.Build.VERSION.SDK_INT <= 28) {
            try {
                AMSHookHelper.hookAMN();
                AMSHookHelper.hookActivityThread();
            } catch (Throwable throwable) {
                throw new RuntimeException("hook failed", throwable);
            }
        }else if (android.os.Build.VERSION.SDK_INT <= 30){//android 10、android11
            try {
                HookUtil.hookAMS();
                HookUtil.hookHandler();
            } catch (Throwable throwable) {
                throw new RuntimeException("hook failed", throwable);
            }
        }else{//android 12以后
            try {
                HookUtil.hookAMS();
                HookUtil.attachContext();
            } catch (Throwable throwable) {
                throw new RuntimeException("hook failed", throwable);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button button = new Button(this);
        button.setText("启动TargetActivity");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 启动目标Activity; 注意这个Activity是没有在AndroidManifest.xml中显式声明的
                // 但是调用者并不需要知道, 就像一个普通的Activity一样
                startActivity(new Intent(MainActivity.this, TargetActivity.class));
            }
        });
        setContentView(button);

    }
}
