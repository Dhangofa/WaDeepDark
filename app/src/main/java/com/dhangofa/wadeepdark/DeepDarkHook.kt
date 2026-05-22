package com.dhangofa.wadeepdark

import android.content.res.Resources
import android.graphics.Color
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
            Color.parseColor("#111b21"), 
            Color.parseColor("#0b141a"), 
            Color.parseColor("#0c151c")  
        )

        val waUIElements = setOf(
            Color.parseColor("#202c33"), 
            Color.parseColor("#1f2c34"), 
            Color.parseColor("#182229")  
        )

        val colorHook = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val originalColor = param.result as? Int ?: return

                when (originalColor) {
                    in waBackgrounds -> param.result = amoledBlack
                    in waUIElements -> param.result = darkGray
                }
            }
        }

        try {
            XposedHelpers.findAndHookMethod(
                Resources::class.java,
                "getColor",
                Int::class.javaPrimitiveType,
                colorHook
            )

            XposedHelpers.findAndHookMethod(
                Resources::class.java,
                "getColor",
                Int::class.javaPrimitiveType,
                Resources.Theme::class.java,
                colorHook
            )
            
            XposedBridge.log("WaDeepDark: Colors hooked successfully!")
        } catch (e: Throwable) {
            XposedBridge.log("WaDeepDark Error: ${e.message}")
        }
    }
}