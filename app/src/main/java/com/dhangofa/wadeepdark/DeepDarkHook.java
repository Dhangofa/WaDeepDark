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

        // 1. Hook for standard 32-bit INT colors
        XC_MethodHook intColorHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args[0] == null) return;
                int color = (int) param.args[0];
                if (isWhatsAppGreen(color)) {
                    // Keep original transparency (Alpha), force RGB to 000000 (Black)
                    param.args[0] = color & 0xFF000000;
                }
            }
        };

        // 2. Hook for Methods that RETURN an int color
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

        // 3. Hook for Jetpack Compose & Android 10+ 64-bit LONG colors (The Missing Link)
        XC_MethodHook longColorHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args[0] == null) return;
                long colorLong = (long) param.args[0];
                
                // Convert Android's 64-bit color back to a readable 32-bit int
                int colorInt = Color.toArgb(colorLong);
                
                if (isWhatsAppGreen(colorInt)) {
                    // Repack it as pure black into the 64-bit format
                    int alpha = Color.alpha(colorInt);
                    param.args[0] = Color.pack(Color.argb(alpha, 0, 0, 0));
                }
            }
        };

        try {
            // == JETPACK COMPOSE & HARDWARE ACCELERATION ==
            // These catch the modern UI engine that bypassed our previous hooks
            XposedHelpers.findAndHookMethod(Paint.class, "setColor", long.class, longColorHook);
            XposedHelpers.findAndHookMethod(Canvas.class, "drawColor", long.class, longColorHook);
        } catch (Throwable t) {
            XposedBridge.log("WaDeepDark: Long hooks skipped (Old Android Version)");
        }

        try {
            // == TRADITIONAL UI & XML ==
            XposedHelpers.findAndHookMethod(Paint.class, "setColor", int.class, intColorHook);
            XposedHelpers.findAndHookMethod(Canvas.class, "drawColor", int.class, intColorHook);
            XposedHelpers.findAndHookMethod(View.class, "setBackgroundColor", int.class, intColorHook);
            XposedHelpers.findAndHookMethod(ColorDrawable.class, "setColor", int.class, intColorHook);
            XposedHelpers.findAndHookConstructor(ColorDrawable.class, int.class, intColorHook);
            
            // Catches colors inflated from WhatsApp's raw XML files
            XposedHelpers.findAndHookMethod(TypedArray.class, "getColor", int.class, int.class, returnIntColorHook);
            
            // Catches dynamic theme changes
            XposedHelpers.findAndHookMethod(ColorStateList.class, "getColorForState", int[].class, int.class, returnIntColorHook);
            XposedHelpers.findAndHookMethod(ColorStateList.class, "getDefaultColor", returnIntColorHook);

            // == CHAT BUBBLES == 
            // WhatsApp tints their PNG chat bubbles using ColorFilters. We must hook these!
            XposedHelpers.findAndHookConstructor(android.graphics.PorterDuffColorFilter.class, int.class, android.graphics.PorterDuff.Mode.class, intColorHook);
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                XposedHelpers.findAndHookConstructor(android.graphics.BlendModeColorFilter.class, int.class, android.graphics.BlendMode.class, intColorHook);
            }

            XposedBridge.log("WaDeepDark: Full Spectrum Rendering Engine Intercept Active!");
        } catch (Throwable t) {
            XposedBridge.log("WaDeepDark Error: " + t.getMessage());
        }
    }

    private boolean isWhatsAppGreen(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        // Safely targets WhatsApp's entire dark mode spectrum (Including #111b21, #0b141a, #202c33)
        // Without accidentally turning green texts or checkmarks to black
        return (r < 45 && g > 15 && g < 75 && b > 20 && b < 85);
    }
        }
