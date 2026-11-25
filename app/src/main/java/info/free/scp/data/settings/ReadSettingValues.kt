package info.free.scp.data.settings

import android.content.SharedPreferences
import info.free.scp.SCPConstants
import kotlin.random.Random

/** `read_settings` 文件里的键。键名、存储类型沿用 PreferenceFragmentCompat 时代的写法，升级用户的旧值直接可读。 */
const val READ_SETTINGS_FILE = "read_settings"
const val KEY_HIDE_FINISHED = "hide_finished_article"
const val KEY_DETAIL_TEXT_SIZE = "detail_text_size"
const val KEY_DETAIL_HANZ_TYPE = "detail_hanz_type"

/** 用户资料文件 `level` 里的稳定编号。 */
const val USER_SETTINGS_FILE = "level"
const val KEY_USER_CODE = "user_code"
const val USER_CODE_BOUND = 600

val DETAIL_TEXT_SIZES = listOf(12, 14, 16, 18, 20)
const val DEFAULT_DETAIL_TEXT_SIZE = 16

/** 正文字号以 "16px" 字符串存储（DropDownPreference 的 entryValues），详情页直接拼进 CSS。 */
fun textSizeToStored(size: Int): String = "${size}px"

/** 旧值不在档位内（或无法解析）时取最近的档位，空值回到默认 16。 */
fun storedToTextSize(stored: String?): Int {
    val parsed = stored?.trim()?.removeSuffix("px")?.toIntOrNull() ?: return DEFAULT_DETAIL_TEXT_SIZE
    return DETAIL_TEXT_SIZES.minByOrNull { kotlin.math.abs(it - parsed) } ?: DEFAULT_DETAIL_TEXT_SIZE
}

/**
 * 繁简设置。旧版 DropDownPreference 把 "0"/"1" 以 **字符串** 存入，而旧的 `PreferenceUtil.getHanzType()` 用 getInt 读取，
 * 一旦有人调用就会 ClassCastException。这里按实际类型读取，缺省（从未设置过）按简体。
 */
fun SharedPreferences.readHanzType(): Int {
    val raw = all[KEY_DETAIL_HANZ_TYPE]
    val value = when (raw) {
        is String -> raw.trim().toIntOrNull()
        is Int -> raw
        else -> null
    }
    return if (value == SCPConstants.TRADITIONAL) SCPConstants.TRADITIONAL else SCPConstants.SIMPLE
}

/** 仍以字符串写入，保持与旧版相同的存储类型。 */
fun SharedPreferences.Editor.putHanzType(type: Int): SharedPreferences.Editor =
    putString(
        KEY_DETAIL_HANZ_TYPE,
        (if (type == SCPConstants.TRADITIONAL) SCPConstants.TRADITIONAL else SCPConstants.SIMPLE).toString()
    )

/** “编号”只在第一次生成并保存，之后保持不变。 */
fun SharedPreferences.stableUserCode(random: Random = Random.Default): Int {
    val existing = all[KEY_USER_CODE]
    if (existing is Int && existing in 0 until USER_CODE_BOUND) return existing
    val code = random.nextInt(USER_CODE_BOUND)
    edit().putInt(KEY_USER_CODE, code).apply()
    return code
}
