package jianqiang.com.hook3.hook;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import java.lang.reflect.Field;
import java.util.List;

import jianqiang.com.hook3.RefInvoke;

/**
 * @author weishu
 * @date 16/1/7
 */
class MockClass2 implements Handler.Callback {

    Handler mBase;

    public MockClass2(Handler base) {
        mBase = base;
    }

    @Override
    public boolean handleMessage(Message msg) {
        System.err.println("linux###msg.what="+msg.what);

        if (android.os.Build.VERSION.SDK_INT <=27){
            switch (msg.what) {
                // ActivityThread里面 "LAUNCH_ACTIVITY" 这个字段的值是100
                // 本来使用反射的方式获取最好, 这里为了简便直接使用硬编码
                case 100:
                    handleLaunchActivity(msg);
                    break;
            }
        }else {
            switch (msg.what) {
                case 159:// android 9
                    // msg.obj = ClientTransaction
                    try {
                        // 获取 List<ClientTransactionItem> mActivityCallbacks 对象
                        Field mActivityCallbacksField = msg.obj.getClass()
                                .getDeclaredField("mActivityCallbacks");
                        mActivityCallbacksField.setAccessible(true);
                        List mActivityCallbacks = (List) mActivityCallbacksField.get(msg.obj);

                        for (int i = 0; i < mActivityCallbacks.size(); i++) {
                            // 打印 mActivityCallbacks 的所有item:
                            //android.app.servertransaction.WindowVisibilityItem
                            //android.app.servertransaction.LaunchActivityItem

                            // 如果是 LaunchActivityItem，则获取该类中的 mIntent 值，即 proxyIntent
                            if (mActivityCallbacks.get(i).getClass().getName()
                                    .equals("android.app.servertransaction.LaunchActivityItem")) {
                                Object launchActivityItem = mActivityCallbacks.get(i);
                                Field mIntentField = launchActivityItem.getClass()
                                        .getDeclaredField("mIntent");
                                mIntentField.setAccessible(true);
                                Intent proxyIntent = (Intent) mIntentField.get(launchActivityItem);

                                // 获取启动插件的 Intent，并替换回来
                                Intent intent = proxyIntent.getParcelableExtra(AMSHookHelper.EXTRA_TARGET_INTENT);
                                if (intent != null) {
                                    mIntentField.set(launchActivityItem, intent);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        mBase.handleMessage(msg);
        return true;
    }

    private void handleLaunchActivity(Message msg) {
        // 这里简单起见,直接取出TargetActivity;
        Object obj = msg.obj;

        // 把替身恢复成真身
        Intent intent = (Intent) RefInvoke.getFieldObject(obj, "intent");

        Intent targetIntent = intent.getParcelableExtra(AMSHookHelper.EXTRA_TARGET_INTENT);
        intent.setComponent(targetIntent.getComponent());
    }


}
