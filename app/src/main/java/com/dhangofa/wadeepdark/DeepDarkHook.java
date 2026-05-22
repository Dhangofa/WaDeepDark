package com.dhangofa.wadeepdark;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class DeepDarkHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.whatsapp")) return;

        // 1. General Color Interceptor (For standard INTs)
        XC_MethodHook intColorHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args[0] == null) return;
                int color = (int) param.args[0];
                if (isWhatsAppBackground(color)) {
                    param.args[0] = color & 0xFF000000; // Force black, keep transparency
                }
            }
        };

        // 2. Return Color Interceptor (For XML inflation)
        XC_MethodHook returnIntColorHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.getResult() == null) return;
                int color = (int) param.getResult();
                if (isWhatsAppBackground(color)) {
                    param.setResult(color & 0xFF000000);
                }
            }
        };

        // 3. Compose Shape Interceptor (Catches Nav Bars, App Bars, and Compose rectangles)
        XC_MethodHook canvasShapeHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                // Find the Paint object in the arguments (usually the last argument)
                Paint paint = null;
                for (int i = param.args.length - 1; i >= 0; i--) {
                    if (param.args[i] instanceof Paint) {
                        paint = (Paint) param.args[i];
                        break;
                    }
                }
                
                if (paint != null) {
                    int color = paint.getColor();
                    if (isWhatsAppBackground(color)) {
                        paint.setColor(color & 0xFF000000);
                    }
                }
            }
        };

        try {
            // == JETPACK COMPOSE & GEOMETRY == (Fixes Nav Bar, Headers, Compose UI)
            XposedHelpers.findAndHookMethod(Canvas.class, "drawRect", float.class, float.class, float.class, float.class, Paint.class, canvasShapeHook);
            XposedHelpers.findAndHookMethod(Canvas.class, "drawRect", RectF.class, Paint.class, canvasShapeHook);
            XposedHelpers.findAndHookMethod(Canvas.class, "drawRect", android.graphics.Rect.class, Paint.class, canvasShapeHook);
            XposedHelpers.findAndHookMethod(Canvas.class, "drawRoundRect", RectF.class, float.class, float.class, Paint.class, canvasShapeHook);
            XposedHelpers.findAndHookMethod(Canvas.class, "drawPath", Path.class, Paint.class, canvasShapeHook);

            // == WINDOW SURFACES == (Fixes the absolute base background of Settings and Chats List)
            XposedHelpers.findAndHookMethod(Window.class, "setStatusBarColor", int.class, intColorHook);
            XposedHelpers.findAndHookMethod(Window.class, "setNavigationBarColor", int.class, intColorHook);
            
            // == STANDARD RENDERERS ==
            XposedHelpers.findAndHookMethod(ColorDrawable.class, "draw", Canvas.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    ColorDrawable cd = (ColorDrawable) param.thisObject;
                    if (isWhatsAppBackground(cd.getColor())) cd.setColor(cd.getColor() & 0xFF000000);
                }
            });
            XposedHelpers.findAndHookMethod(Paint.class, "setColor", int.class, intColorHook);
            XposedHelpers.findAndHookMethod(Canvas.class, "drawColor", int.class, intColorHook);

            // == CHAT BUBBLE FILTERS ==
            XposedHelpers.findAndHookConstructor(android.graphics.PorterDuffColorFilter.class, int.class, android.graphics.PorterDuff.Mode.class, intColorHook);
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                XposedHelpers.findAndHookConstructor(android.graphics.BlendModeColorFilter.class, int.class, android.graphics.BlendMode.class, intColorHook);
            }

            // == EXPLICIT VIEWS ==
            XposedHelpers.findAndHookMethod(View.class, "setBackgroundColor", int.class, intColorHook);
            XposedHelpers.findAndHookMethod(TypedArray.class, "getColor", int.class, int.class, returnIntColorHook);
            XposedHelpers.findAndHookMethod(ColorStateList.class, "getDefaultColor", returnIntColorHook);

            XposedBridge.log("WaDeepDark: Compose Geometry & Window Hooks Active!");
        } catch (Throwable t) {
            XposedBridge.log("WaDeepDark Error: " + t.getMessage());
        }
    }

    private boolean isWhatsAppBackground(int color) {
        int a = Color.alpha(color);
        if (a < 10) return false; // Ignore highly transparent elements

        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        // 1. Broad Dark Mode Detector:
        // Catches any dark grey/greenish tint (including Material You shifts) 
        // while ignoring pure black (r+g+b > 15) so we don't process already-black pixels.
        boolean isDarkBackground = (r < 55 && g < 65 && b < 75 && (r + g + b) > 15);
        
        // 2. Outgoing Chat Bubble (Dark Teal)
        boolean isOutgoingBubble = (r < 40 && g > 50 && g < 120 && b > 40 && b < 100);

        return isDarkBackground || isOutgoingBubble;
    }
}
