package com.dhangofa.wadeepdark;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class DeepDarkHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.whatsapp")) return;

        final int pureBlack = Color.parseColor("#000000");
        final int darkGrey = Color.parseColor("#151515"); // Premium dark grey for bubbles/menus

        // The master color modifier
        XC_MethodHook colorInterceptHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args[0] == null) return;
                param.args[0] = getModifiedColor((int) param.args[0], pureBlack, darkGrey);
            }
        };

        XC_MethodHook returnColorHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.getResult() == null) return;
                param.setResult(getModifiedColor((int) param.getResult(), pureBlack, darkGrey));
            }
        };

        // THE MAIN SCREEN FIX: Force the root window background of every screen to be black
        XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.thisObject;
                try {
                    activity.getWindow().setBackgroundDrawable(new ColorDrawable(pureBlack));
                } catch (Throwable ignored) {}
            }
        });

        try {
            // Hook standard UI drawing
            XposedHelpers.findAndHookMethod(Paint.class, "setColor", int.class, colorInterceptHook);
            XposedHelpers.findAndHookMethod(Canvas.class, "drawColor", int.class, colorInterceptHook);
            XposedHelpers.findAndHookMethod(View.class, "setBackgroundColor", int.class, colorInterceptHook);
            
            // Hook Drawables
            XposedHelpers.findAndHookMethod(ColorDrawable.class, "setColor", int.class, colorInterceptHook);
            XposedHelpers.findAndHookConstructor(ColorDrawable.class, int.class, colorInterceptHook);
            
            // Hook XML colors
            XposedHelpers.findAndHookMethod(TypedArray.class, "getColor", int.class, int.class, returnColorHook);
            XposedHelpers.findAndHookMethod(ColorStateList.class, "getDefaultColor", returnColorHook);
            XposedHelpers.findAndHookMethod(ColorStateList.class, "getColorForState", int[].class, int.class, returnColorHook);

            // Hook Chat Bubble Filters
            XposedHelpers.findAndHookConstructor(android.graphics.PorterDuffColorFilter.class, int.class, android.graphics.PorterDuff.Mode.class, colorInterceptHook);
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                XposedHelpers.findAndHookConstructor(android.graphics.BlendModeColorFilter.class, int.class, android.graphics.BlendMode.class, colorInterceptHook);
            }

            XposedBridge.log("WaDeepDark: Precise UI Targeting Active!");
        } catch (Throwable t) {
            XposedBridge.log("WaDeepDark Error: " + t.getMessage());
        }
    }

    private int getModifiedColor(int originalColor, int pureBlack, int darkGrey) {
        int a = Color.alpha(originalColor);
        if (a == 0) return originalColor; // Ignore transparent pixels entirely

        int r = Color.red(originalColor);
        int g = Color.green(originalColor);
        int b = Color.blue(originalColor);

        // 1. Deep Backgrounds (#0b141a, #111b21) -> Turn Pure Black
        if (r >= 5 && r <= 25 && g >= 15 && g <= 35 && b >= 20 && b <= 40) {
            return (originalColor & 0xFF000000) | (pureBlack & 0x00FFFFFF);
        }
        
        // 2. Menus, App Bars, Nav Bars, Incoming Bubbles (#182229, #202c33, #233138) -> Turn Dark Grey
        if (r >= 20 && r <= 45 && g >= 30 && g <= 55 && b >= 35 && b <= 65) {
            return (originalColor & 0xFF000000) | (darkGrey & 0x00FFFFFF);
        }

        // 3. Outgoing Bubble (#005c4b) -> Turn Dark Grey
        if (r <= 20 && g >= 70 && g <= 110 && b >= 60 && b <= 90) {
            return (originalColor & 0xFF000000) | (darkGrey & 0x00FFFFFF);
        }

        return originalColor; // Leave all other colors (like green text/icons) alone
    }
}
