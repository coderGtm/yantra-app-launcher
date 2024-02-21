package com.coderGtm.yantra.misc

import android.graphics.Typeface
import com.coderGtm.yantra.SUPPORT_URL
import com.coderGtm.yantra.terminal.Terminal
import java.util.Timer
import kotlin.concurrent.schedule

fun purchasePrank(primaryTerminal: Terminal, skuId: String) {
    primaryTerminal.activity.apply {
        Timer().schedule(2000) {
            runOnUiThread {
                primaryTerminal.output("No, wait! Everything is free in Yantra Launcher!",primaryTerminal.theme.warningTextColor, Typeface.BOLD)
                Timer().schedule(1500) {
                    runOnUiThread {
                        primaryTerminal.output("Just kidding! Let's get started with the purchase.",primaryTerminal.theme.resultTextColor, null)
                        Timer().schedule(2000) {
                            runOnUiThread {
                                primaryTerminal.output("Hang on! I'm just kidding again! Yantra Launcher is completely free and open-source!",primaryTerminal.theme.successTextColor, null)
                                Timer().schedule(2000) {
                                    runOnUiThread {
                                        primaryTerminal.output("But if you want to support the developer, you can see here: $SUPPORT_URL",primaryTerminal.theme.resultTextColor, null)
                                        Timer().schedule(2000) {
                                            runOnUiThread {
                                                primaryTerminal.output("-------------------------", primaryTerminal.theme.warningTextColor, null)
                                                primaryTerminal.output("Granting you access to $skuId...",primaryTerminal.theme.resultTextColor, null)
                                                Timer().schedule(1000) {
                                                    primaryTerminal.preferenceObject.edit().putBoolean(skuId+"___purchased", true).apply()
                                                    runOnUiThread {
                                                        primaryTerminal.output("Access granted!",primaryTerminal.theme.successTextColor, Typeface.BOLD)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}