package com.dhangofa.wadeepdark;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
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

        // General Color Interceptor
        XC_MethodHook intColorHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args[0] == null) return;
                int color = (int) param.args[0];
                if (isWhatsAppGreen(color)) {
                    param.args[0] = color & 0xFF000000; // Keep transparency, force black
                }
            }
        };

        // Return Color Interceptor (For XML inflation)
        XC_MethodHook returnIntColorHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.getResult() == null) return;
                int color = (int) param.getResult();
                if (isWhatsAppGreen(color)) {
                    param.setResult(color & 0xFF000000);
                }
            }
        };

        // THE SILVER BULLET: Catches XML-inflated Main Backgrounds, App Bars, and Nav Bars
        XC_MethodHook drawableDrawHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    ColorDrawable cd = (ColorDrawable) param.thisObject;
                    int color = cd.getColor();
                    if (isWhatsAppGreen(color)) {
                        // Modify the drawable's color right before it hits the canvas
                        cd.setColor(color & 0xFF000000);
                    }
                } catch (Throwable ignored) {}
            }
        };

        try {
            // Hooking the actual drawing of solid backgrounds
            XposedHelpers.findAndHookMethod(ColorDrawable.class, "draw", Canvas.class, drawableDrawHook);

            // Hooking Paint & Canvas (Catches Custom bubbles and dynamic UI)
            XposedHelpers.findAndHookMethod(Paint.class, "setColor", int.class, intColorHook);
            XposedHelpers.findAndHookMethod(Canvas.class, "drawColor", int.class, intColorHook);
            
            // Hooking Color Filters (Catches the PNG tint for chat bubbles)
            XposedHelpers.findAndHookConstructor(android.graphics.PorterDuffColorFilter.class, int.class, android.graphics.PorterDuff.Mode.class, intColorHook);
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                XposedHelpers.findAndHookConstructor(android.graphics.BlendModeColorFilter.class, int.class, android.graphics.BlendMode.class, intColorHook);
            }

            // Hooking Explicit Views & XML inflation
            XposedHelpers.findAndHookMethod(View.class, "setBackgroundColor", int.class, intColorHook);
            XposedHelpers.findAndHookMethod(TypedArray.class, "getColor", int.class, int.class, returnIntColorHook);
            XposedHelpers.findAndHookMethod(ColorStateList.class, "getColorForState", int[].class, int.class, returnIntColorHook);
            XposedHelpers.findAndHookMethod(ColorStateList.class, "getDefaultColor", returnIntColorHook);

            XposedBridge.log("WaDeepDark: Advanced Draw-Time Intercept Active!");
        } catch (Throwable t) {
            XposedBridge.log("WaDeepDark Error: " + t.getMessage());
        }
    }

    private boolean isWhatsAppGreen(int color) {
        int a = Color.alpha(color);
        if (a < 10) return false; // Ignore highly transparent pixels to prevent black boxes

        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        // 1. Catches the Main Backgrounds, App Bars, Nav Bars (e.g., #111b21, #202c33)
        boolean isDarkGrayGreen = (r < 50 && g > 15 && g < 60 && b > 20 && b < 70);
        
        // 2. Catches the Outgoing Chat Bubble (Dark Teal e.g., #005c4b)
        // Red is very low, Green is high, Blue is medium.
        boolean isOutgoingBubble = (r < 40 && g > 50 && g < 120 && b > 40 && b < 100);

        return isDarkGrayGreen || isOutgoingBubble;
    }
}
