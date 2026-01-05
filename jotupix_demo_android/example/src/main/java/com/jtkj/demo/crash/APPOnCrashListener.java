
package com.jtkj.demo.crash;

import android.content.Context;

import java.util.Properties;

public interface APPOnCrashListener {

    void onCrashDialog(Context context);

    void onCrashPost(Properties crashReport);
}
