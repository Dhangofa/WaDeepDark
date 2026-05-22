package com.dhangofa.wadeepdark;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.ContextThemeWrapper;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class DeepDarkHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.whatsapp")) return;

        // Force-replace the color values in the app's internal theme
        // This targets the root of the "greenish" problem
        XC_MethodHook themeHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Resources res = (Resources) param.getResult();
                
                // This is a direct override of the standard color lookup table
                // WhatsApp uses these specific IDs for their dark mode palette
                try {
                    // Try to force these to black if accessed via Resources
                    XposedHelpers.setStaticObjectField(res.getClass(), "primary_surface", Color.BLACK);
                    XposedHelpers.setStaticObjectField(res.getClass(), "chat_background", Color.BLACK);
                } catch (Throwable ignored) {}
            }
        };

        try {
            // Hook the creation of the theme context
            XposedHelpers.findAndHookMethod(
                ContextThemeWrapper.class,
                "getResources",
                themeHook
            );

            // Hook the basic color getter as a fallback
            XposedHelpers.findAndHookMethod(
                Resources.class,
                "getColor",
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int color = (int) param.getResult();
                        // If it's a "WhatsApp Greenish" color (roughly), force it to black
                        if (isWhatsAppGreen(color)) {
                            param.setResult(Color.BLACK);
                        }
                    }
                }
            );

            XposedBridge.log("WaDeepDark: Theme-level hooks injected.");
        } catch (Throwable t) {
            XposedBridge.log("WaDeepDark Error: " + t.getMessage());
        }
    }

    private boolean isWhatsAppGreen(int color) {
        // WhatsApp's greenish-dark is generally in this range of the color spectrum
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        // Returns true if the color is "darkish-green"
        return (r < 50 && g > 15 && g < 60 && b > 20 && b < 70);
    }
}
