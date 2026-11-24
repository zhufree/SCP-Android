/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.free.scp.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import info.free.scp.R
import info.free.scp.util.ThemeUtil
import info.free.scp.util.ThemeUtil.NIGHT_THEME

@Composable
private fun dayColorScheme(): ColorScheme = lightColorScheme(
    primary = colorResource(R.color.md_day_primary),
    onPrimary = colorResource(R.color.md_day_onPrimary),
    primaryContainer = colorResource(R.color.md_day_primaryContainer),
    onPrimaryContainer = colorResource(R.color.md_day_onPrimaryContainer),
    secondary = colorResource(R.color.md_day_secondary),
    onSecondary = colorResource(R.color.md_day_onSecondary),
    secondaryContainer = colorResource(R.color.md_day_secondaryContainer),
    onSecondaryContainer = colorResource(R.color.md_day_onSecondaryContainer),
    tertiary = colorResource(R.color.md_day_tertiary),
    onTertiary = colorResource(R.color.md_day_onTertiary),
    tertiaryContainer = colorResource(R.color.md_day_tertiaryContainer),
    onTertiaryContainer = colorResource(R.color.md_day_onTertiaryContainer),
    background = colorResource(R.color.md_day_background),
    onBackground = colorResource(R.color.md_day_onBackground),
    surface = colorResource(R.color.md_day_surface),
    onSurface = colorResource(R.color.md_day_onSurface),
    surfaceVariant = colorResource(R.color.md_day_surfaceVariant),
    onSurfaceVariant = colorResource(R.color.md_day_onSurfaceVariant),
    outline = colorResource(R.color.md_day_outline),
    outlineVariant = colorResource(R.color.md_day_outlineVariant),
    error = colorResource(R.color.md_day_error),
    onError = colorResource(R.color.md_day_onError),
    errorContainer = colorResource(R.color.md_day_errorContainer),
    onErrorContainer = colorResource(R.color.md_day_onErrorContainer),
    surfaceContainerLowest = colorResource(R.color.md_day_surfaceContainerLowest),
    surfaceContainerLow = colorResource(R.color.md_day_surfaceContainerLow),
    surfaceContainer = colorResource(R.color.md_day_surfaceContainer),
    surfaceContainerHigh = colorResource(R.color.md_day_surfaceContainerHigh),
    surfaceContainerHighest = colorResource(R.color.md_day_surfaceContainerHighest),
)

@Composable
private fun nightColorScheme(): ColorScheme = darkColorScheme(
    primary = colorResource(R.color.md_night_primary),
    onPrimary = colorResource(R.color.md_night_onPrimary),
    primaryContainer = colorResource(R.color.md_night_primaryContainer),
    onPrimaryContainer = colorResource(R.color.md_night_onPrimaryContainer),
    secondary = colorResource(R.color.md_night_secondary),
    onSecondary = colorResource(R.color.md_night_onSecondary),
    secondaryContainer = colorResource(R.color.md_night_secondaryContainer),
    onSecondaryContainer = colorResource(R.color.md_night_onSecondaryContainer),
    tertiary = colorResource(R.color.md_night_tertiary),
    onTertiary = colorResource(R.color.md_night_onTertiary),
    tertiaryContainer = colorResource(R.color.md_night_tertiaryContainer),
    onTertiaryContainer = colorResource(R.color.md_night_onTertiaryContainer),
    background = colorResource(R.color.md_night_background),
    onBackground = colorResource(R.color.md_night_onBackground),
    surface = colorResource(R.color.md_night_surface),
    onSurface = colorResource(R.color.md_night_onSurface),
    surfaceVariant = colorResource(R.color.md_night_surfaceVariant),
    onSurfaceVariant = colorResource(R.color.md_night_onSurfaceVariant),
    outline = colorResource(R.color.md_night_outline),
    outlineVariant = colorResource(R.color.md_night_outlineVariant),
    error = colorResource(R.color.md_night_error),
    onError = colorResource(R.color.md_night_onError),
    errorContainer = colorResource(R.color.md_night_errorContainer),
    onErrorContainer = colorResource(R.color.md_night_onErrorContainer),
    surfaceContainerLowest = colorResource(R.color.md_night_surfaceContainerLowest),
    surfaceContainerLow = colorResource(R.color.md_night_surfaceContainerLow),
    surfaceContainer = colorResource(R.color.md_night_surfaceContainer),
    surfaceContainerHigh = colorResource(R.color.md_night_surfaceContainerHigh),
    surfaceContainerHighest = colorResource(R.color.md_night_surfaceContainerHighest),
)

/**
 * 日夜只由应用内的 [ThemeUtil.currentTheme] 决定，不读取系统深色模式（isSystemInDarkTheme）。
 * 色值与 View 侧共用 colors.xml 的 md_day_* / md_night_*。
 */
@Composable
fun MainTheme(content: @Composable () -> Unit) {
    val colorScheme = if (ThemeUtil.currentTheme == NIGHT_THEME) nightColorScheme() else dayColorScheme()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
