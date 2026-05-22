package com.dhangofa.wadeepdark;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.view.View;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class DeepDarkHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.whatsapp")) return;

        final int amoledBlack = Color.parseColor("#000000");
        final int darkGray = Color.parseColor("#0A0A0A");

        // Target RGB values (Ignoring Alpha/Transparency for now)
        final Set<Integer> waBackgroundsRGB = new HashSet<>(Arrays.asList(
            0x111b21, 0x0b141a, 0x0c151c
        ));

        final Set<Integer> waUIElementsRGB = new HashSet<>(Arrays.asList(
            0x202c33, 0x1f2c34, 0x182229
        ));

        XC_MethodHook colorInterceptHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args[0] == null) return;
                
                int originalColor = (Integer) param.args[0];
                
                // Extract just the RGB part to match WhatsApp's colors
                int rgbOnly = originalColor & 0x00FFFFFF;

                if (waBackgroundsRGB.contains(rgbOnly)) {
                    // Keep WhatsApp's original transparency, but inject our AMOLED black
                    param.args[0] = (originalColor & 0xFF000000) | (amoledBlack & 0x00FFFFFF);
                } else if (waUIElementsRGB.contains(rgbOnly)) {
                    // Keep WhatsApp's original transparency, but inject our dark gray
                    param.args[0] = (originalColor & 0xFF000000) | (darkGray & 0x00FFFFFF);
                }
            }
        };

        try {
            // 1. Hook Material Design ColorStateLists (Catches most XML theme colors)
            XposedHelpers.findAndHookMethod(
                ColorStateList.class, 
                "valueOf", 
                int.class, 
                colorInterceptHook
            );

            // 2. Hook Canvas Paint (Catches custom drawn chat bubbles)
            XposedHelpers.findAndHookMethod(
                Paint.class, 
                "setColor", 
                int.class, 
                colorInterceptHook
            );

            // 3. Hook standard ColorDrawables
            XposedHelpers.findAndHookConstructor(
                ColorDrawable.class, 
                int.class, 
                colorInterceptHook
            );
            
            XposedHelpers.findAndHookMethod(
                ColorDrawable.class, 
                "setColor", 
                int.class, 
                colorInterceptHook
            );

            // 4. Hook explicit View background color setters
            XposedHelpers.findAndHookMethod(
                View.class, 
                "setBackgroundColor", 
                int.class, 
                colorInterceptHook
            );

            XposedBridge.log("WaDeepDark: Deep Java UI hooks applied successfully.");
        } catch (Throwable t) {
            XposedBridge.log("WaDeepDark Error: " + t.getMessage());
        }
    }
}
