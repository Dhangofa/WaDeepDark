package com.dhangofa.wadeepdark;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class DeepDarkHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.whatsapp")) return;

        // Hook the Canvas.drawColor method.
        // This is the absolute last step before the screen turns green.
        XposedHelpers.findAndHookMethod(
            Canvas.class,
            "drawColor",
            int.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    int color = (int) param.args[0];
                    
                    // Detect the greenish dark shade
                    if (isWhatsAppGreen(color)) {
                        param.args[0] = Color.BLACK;
                    }
                }
            }
        );

        XposedBridge.log("WaDeepDark: Canvas drawing monitor active.");
    }

    private boolean isWhatsAppGreen(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        // This targets the specific "dark green" values found in your list
        return (r < 40 && g > 15 && g < 70 && b > 20 && b < 80);
    }
}
