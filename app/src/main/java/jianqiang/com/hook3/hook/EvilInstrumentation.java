package jianqiang.com.hook3.hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

public class EvilInstrumentation extends Instrumentation {

    private static final String TAG = "EvilInstrumentation";

    // 替身Activity的包名, 也就是我们自己的包名

    // ActivityThread中原始的对象, 保存起来
    Instrumentation mBase;

    public EvilInstrumentation(Instrumentation base) {
        mBase = base;
    }

    public Activity newActivity(ClassLoader cl, String className,
                                Intent intent)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {

        // 把替身恢复成真身
        Intent rawIntent = intent.getParcelableExtra(HookUtil.TARGET_INTENT);

        System.err.println("##linux###rawIntent=="+rawIntent);

        if(rawIntent == null) {
            return mBase.newActivity(cl, className, intent);
        }

        String newClassName = rawIntent.getComponent().getClassName();
        return mBase.newActivity(cl, newClassName, rawIntent);
    }
}
