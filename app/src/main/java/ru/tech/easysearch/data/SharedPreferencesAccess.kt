package ru.tech.easysearch.data

import android.content.Context

object SharedPreferencesAccess {

    private const val mainSharedPrefsKey = "eSearch"
    private const val defLabels =
        "ic_google_logo+ic_bing_logo+ic_yandex_logo+ic_mailru_logo+ic_yahoo_logo"

    const val AD_BLOCK = "adblock"
    const val IMAGE_LOADING = "imgload"
    const val LOCATION_ACCESS = "location"
    const val SAVE_HISTORY = "svhist"
    const val COOKIES = "cookies"
    const val JS = "javascript"
    const val POPUPS = "popupmessages"
    const val DOM_STORAGE = "domstorage"

    fun loadLabelList(context: Context): String? {
        return context.getSharedPreferences(mainSharedPrefsKey, Context.MODE_PRIVATE)
            .getString("label", defLabels)
    }

    fun saveLabelList(context: Context, string: String) {
        context.getSharedPreferences(mainSharedPrefsKey, Context.MODE_PRIVATE).edit()
            .putString("label", string).apply()
    }

    fun getSetting(context: Context, key: String): Boolean {
        return context.getSharedPreferences(mainSharedPrefsKey, Context.MODE_PRIVATE)
            .getBoolean(key, true)
    }

    fun setSetting(context: Context, key: String, value: Boolean) {
        context.getSharedPreferences(mainSharedPrefsKey, Context.MODE_PRIVATE).edit()
            .putBoolean(key, value).apply()
    }

}