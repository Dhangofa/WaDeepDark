package com.dhangofa.wadeepdark

import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Paint
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class DeepDarkHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.whatsapp") return

        val amoledBlack = Color.parseColor("#000000")
        val darkGray = Color.parseColor("#0A0A0A")

        val waBackgrounds = setOf(
            Color.parseColor("#111b21"), // Main chat list background
            Color.parseColor("#0b141a"), // Inside chat background
            Color.parseColor("#0c151c")  // Splash screen
        )

        val waUIElements = setOf(
            Color.parseColor("#202c33"), // Appbar / Header / Outgoing Bubble
            Color.parseColor("#1f2c34"), // Alternate cards
            Color.parseColor("#182229")  // Navigation bars
        )

        // 1. Hook for Methods RETURNING a color (TypedArray & Resources)
        val returnColorHook = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val originalColor = param.result as? Int ?: return
                when (originalColor) {
                    in waBackgrounds -> param.result = amoledBlack
                    in waUIElements -> param.result = darkGray
                }
            }
        }

        // 2. Hook for Methods CONSUMING a color (Paint for Custom Canvas Drawing)
        val paintColorHook = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val originalColor = param.args[0] as? Int ?: return
                when (originalColor) {
                    in waBackgrounds -> param.args[0] = amoledBlack
                    in waUIElements -> param.args[0] = darkGray
                }
            }
        }

        try {
            // Hook XML Attribute resolution (Fixes main backgrounds)
            XposedHelpers.findAndHookMethod(
                TypedArray::class.java,
                "getColor",
                Int::class.javaPrimitiveType, // index
                Int::class.javaPrimitiveType, // defValue
                returnColorHook
            )

            // Hook programmatic drawing (Fixes custom chat bubbles and headers)
            XposedHelpers.findAndHookMethod(
                Paint::class.java,
                "setColor",
                Int::class.javaPrimitiveType, // color
                paintColorHook
            )

            // Keep the legacy Resources.getColor hooks as a fallback
            XposedHelpers.findAndHookMethod(
                Resources::class.java,
                "getColor",
                Int::class.javaPrimitiveType,
                returnColorHook
            )

            XposedHelpers.findAndHookMethod(
                Resources::class.java,
                "getColor",
                Int::class.javaPrimitiveType,
                Resources.Theme::class.java,
                returnColorHook
            )

            XposedBridge.log("WaDeepDark: Colors hooked successfully across all rendering layers!")
        } catch (e: Throwable) {
            XposedBridge.log("WaDeepDark Error: ${e.message}")
        }
    }
}
