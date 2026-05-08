package com.lifeplus.healthcare.ads

import android.content.Context

object RewardPointsStore {
    private const val PREFS = "reward_points_store"
    private const val KEY_BALANCE = "reward_balance"

    fun getBalance(context: Context): Int {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_BALANCE, 0)
    }

    fun addPoints(context: Context, points: Int): Int {
        if (points <= 0) return getBalance(context)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val updated = (prefs.getInt(KEY_BALANCE, 0) + points).coerceAtLeast(0)
        prefs.edit().putInt(KEY_BALANCE, updated).apply()
        return updated
    }
}
