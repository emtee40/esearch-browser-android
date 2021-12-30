package ru.tech.easysearch.extensions

import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.WebResourceError
import android.webkit.WebView
import android.webkit.WebViewClient.*
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils.HSLToColor
import androidx.core.graphics.ColorUtils.colorToHSL
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.ImageViewCompat
import ru.tech.easysearch.R
import ru.tech.easysearch.application.ESearchApplication
import ru.tech.easysearch.data.DataArrays.NEGATIVE_COLOR
import ru.tech.easysearch.data.DataArrays.faviconParser
import ru.tech.easysearch.data.SharedPreferencesAccess.AD_BLOCK
import ru.tech.easysearch.data.SharedPreferencesAccess.CAMERA_ACCESS
import ru.tech.easysearch.data.SharedPreferencesAccess.COOKIES
import ru.tech.easysearch.data.SharedPreferencesAccess.DOM_STORAGE
import ru.tech.easysearch.data.SharedPreferencesAccess.EYE_PROTECTION
import ru.tech.easysearch.data.SharedPreferencesAccess.IMAGE_LOADING
import ru.tech.easysearch.data.SharedPreferencesAccess.JS
import ru.tech.easysearch.data.SharedPreferencesAccess.LOCATION_ACCESS
import ru.tech.easysearch.data.SharedPreferencesAccess.MIC_ACCESS
import ru.tech.easysearch.data.SharedPreferencesAccess.POPUPS
import ru.tech.easysearch.data.SharedPreferencesAccess.SAVE_HISTORY
import ru.tech.easysearch.data.SharedPreferencesAccess.getSetting
import ru.tech.easysearch.helper.utils.permissions.PermissionUtils.grantPermissionsStorage
import ru.tech.easysearch.model.SettingsItem
import java.io.*
import java.net.URL
import java.net.URLConnection
import java.util.*


object Extensions {

    fun Context.dipToPixels(dipValue: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resources.displayMetrics)

    @ColorInt
    fun Context.getAttrColor(
        @AttrRes attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }

    fun Context.adjustFontSize(fontScale: Float = 1.0f): Context {
        val configuration = resources.configuration
        configuration.fontScale = fontScale
        return createConfigurationContext(configuration)
    }

    fun ImageView.setTint(@ColorInt color: Int?) {
        if (color != null) {
            ImageViewCompat.setImageTintMode(this, PorterDuff.Mode.SRC_ATOP)
            ImageViewCompat.setImageTintList(
                this,
                ColorStateList.valueOf(color)
            )
        } else ImageViewCompat.setImageTintList(this, null)
    }

    fun Drawable.getBitmap(): Bitmap? {
        val bitmap = Bitmap.createBitmap(
            intrinsicWidth,
            intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }

    fun Bitmap.toByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 90, stream)
        return stream.toByteArray()
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun WebResourceError.errorMessage(): String {
        return when (errorCode) {
            ERROR_AUTHENTICATION -> "User authentication failed on server"
            ERROR_TIMEOUT -> "The server is taking too much time to communicate. Try again later."
            ERROR_TOO_MANY_REQUESTS -> "Too many requests during this load"
            ERROR_BAD_URL -> "Check entered URL.."
            ERROR_CONNECT -> "Failed to connect to the server"
            ERROR_FAILED_SSL_HANDSHAKE -> "Failed to perform SSL handshake"
            ERROR_HOST_LOOKUP -> "Server or proxy hostname lookup failed"
            ERROR_PROXY_AUTHENTICATION -> "User authentication failed on proxy"
            ERROR_REDIRECT_LOOP -> "Too many redirects"
            ERROR_UNSUPPORTED_AUTH_SCHEME -> "Unsupported authentication scheme (not basic or digest)"
            ERROR_UNSUPPORTED_SCHEME -> "Unsupported scheme"
            ERROR_FILE -> "Generic file error"
            ERROR_FILE_NOT_FOUND -> "File not found"
            ERROR_IO -> "The server failed to communicate. Try again later."
            else -> "Generic error"
        }
    }

    fun Class<*>.getResId(resName: String): Int {
        return try {
            getDeclaredField(resName).let { it.getInt(it) }
        } catch (e: Exception) {
            -1
        }
    }

    fun View.hideKeyboard(context: Context) {
        (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            windowToken,
            0
        )
    }

    fun Context.fetchFavicon(url: String): Bitmap {
        val iconUri: Uri = Uri.parse("$faviconParser${Uri.parse(url).host}")
        val inputStream: InputStream?
        val buffer: BufferedInputStream?
        return try {
            val urlConnection: URLConnection = URL(iconUri.toString()).openConnection()
            urlConnection.connect()
            inputStream = urlConnection.getInputStream()
            buffer = BufferedInputStream(inputStream, 8192)
            BitmapFactory.decodeStream(buffer)
        } catch (e: Exception) {
            ContextCompat.getDrawable(this, R.drawable.ic_earth_24)!!.toBitmap()
        }
    }

    fun Context.setCoeff() {
        val displayMetrics = resources.displayMetrics
        val dpHeight = (displayMetrics.heightPixels / displayMetrics.density).toInt()
        val dpWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()

        ESearchApplication.coeff = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> (dpWidth - 210 - 35) / 5
            else -> (dpHeight - 210 - 35) / 5
        }
    }

    fun Context.createSettingsList(): List<SettingsItem> {
        return listOf(
            SettingsItem(
                ContextCompat.getDrawable(this, R.drawable.ic_eye_protection_24),
                getString(R.string.eyeProtection),
                getSetting(this, EYE_PROTECTION),
                EYE_PROTECTION
            ),
            SettingsItem(
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_block_24),
                getString(R.string.adblock),
                getSetting(this, AD_BLOCK),
                AD_BLOCK
            ),
            SettingsItem(
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_image_24),
                getString(R.string.imageLoading),
                getSetting(this, IMAGE_LOADING),
                IMAGE_LOADING
            ),
            SettingsItem(
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_location_on_24),
                getString(R.string.location),
                getSetting(this, LOCATION_ACCESS),
                LOCATION_ACCESS
            ),
            SettingsItem(
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_camera_alt_24),
                getString(R.string.camera),
                getSetting(this, CAMERA_ACCESS),
                CAMERA_ACCESS
            ),
            SettingsItem(
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_mic_24),
                getString(R.string.mic),
                getSetting(this, MIC_ACCESS),
                MIC_ACCESS
            ),
            SettingsItem(
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_history_24),
                getString(R.string.saveHistory),
                getSetting(this, SAVE_HISTORY),
                SAVE_HISTORY
            ),
            SettingsItem(
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_cookie_24),
                getString(R.string.cookies),
                getSetting(this, COOKIES),
                COOKIES
            ),
            SettingsItem(
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_javascript_24),
                getString(R.string.javascript),
                getSetting(this, JS),
                JS
            ),
            SettingsItem(
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_message_24),
                getString(R.string.popupMessages),
                getSetting(this, POPUPS),
                POPUPS
            ),
            SettingsItem(
                ContextCompat.getDrawable(this, R.drawable.ic_database_24),
                getString(R.string.domStorage),
                getSetting(this, DOM_STORAGE),
                DOM_STORAGE
            )
        )
    }

    fun WebView.forceNightMode(night: Boolean) {
        when (night) {
            true -> {
                val paint = Paint()
                val matrix = ColorMatrix()
                matrix.set(NEGATIVE_COLOR)
                val gcm = ColorMatrix()
                gcm.setSaturation(0f)
                val concat = ColorMatrix()
                concat.setConcat(matrix, gcm)
                val filter = ColorMatrixColorFilter(concat)
                paint.colorFilter = filter
                setLayerType(View.LAYER_TYPE_HARDWARE, paint)
            }
            false -> setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
    }

    fun AppCompatActivity.writeBitmap(bitmap: Bitmap) {
        val calendar = Calendar.getInstance()
        val day = calendar[Calendar.DAY_OF_MONTH]
        val month = calendar[Calendar.MONTH]
        val year = calendar[Calendar.YEAR]
        val strHour = calendar[Calendar.HOUR_OF_DAY]
        val strMinute = calendar[Calendar.MINUTE]
        val minute = when {
            strMinute < 10 -> "0$strMinute"
            else -> "$strMinute"
        }
        val hour = when {
            strHour < 10 -> "0$strHour"
            else -> "$strHour"
        }
        val millis = calendar[Calendar.MILLISECOND]

        val date = "$day-$month-${year}_$hour:$minute:$millis"

        try {
            execute(bitmap, date)
        } catch (e: FileNotFoundException) {
            Toast.makeText(
                this.applicationContext,
                R.string.noExternalStorageAccess,
                Toast.LENGTH_LONG
            ).show()
            grantPermissionsStorage(this)
        }

    }

    private fun AppCompatActivity.execute(bitmap: Bitmap, date: String) {
        val name = "Screenshot_ESearch_${date}"
        val fos: OutputStream? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver: ContentResolver = contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/ESearch")
            val imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            resolver.openOutputStream(imageUri!!)
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM
            ).toString() + File.separator + "ESearch"
            val file = File(imagesDir)
            if (!file.exists()) {
                file.mkdir()
            }
            val image = File(imagesDir, "$name.png")
            FileOutputStream(image)
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos!!.flush()
        fos.close()
    }

    @ColorInt
    fun Int.lightenColor(value: Float): Int {
        val hsl = FloatArray(3)
        colorToHSL(this, hsl)
        hsl[2] += value
        hsl[2] = 0f.coerceAtLeast(hsl[2].coerceAtMost(1f))
        return HSLToColor(hsl)
    }

    @ColorInt
    fun Int.darkenColor(value: Float): Int {
        val hsl = FloatArray(3)
        colorToHSL(this, hsl)
        hsl[2] -= value
        hsl[2] = 0f.coerceAtLeast(hsl[2].coerceAtMost(1f))
        return HSLToColor(hsl)
    }

}