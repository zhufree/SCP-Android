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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import eth.zhufree.baihehub.ui.hehuanHong
import eth.zhufree.baihehub.ui.jiQing
import eth.zhufree.baihehub.ui.lightGray
import eth.zhufree.baihehub.ui.shizhuHong

private val DarkColorPalette = darkColors(
    primary = hehuanHong,
    primaryVariant = shizhuHong,
    secondary = jiQing,
    secondaryVariant = jiQing,
    background = Color.DarkGray,
    onPrimary = Color.White, // Main Text
    onSecondary = Color.LightGray, // Sub Text
    onSurface = Color.LightGray, // Top Bar Tint
    onBackground = Color.Black // Item bg
)

private val LightColorPalette = lightColors(
    primary = hehuanHong,
    primaryVariant = shizhuHong,
    secondary = jiQing,
    secondaryVariant = jiQing,
    background = lightGray,
    onPrimary = Color.Black, // Main Text
    onSecondary = Color.DarkGray, // Sub Text
    onSurface = Color.White, // Top Bar Tint
    onBackground = Color.White // Item bg

//    Other default colors to override
//    surface = Color.White,
//    onPrimary = Color.White,
//    onSecondary = Color.Black,
//    onBackground = Color.Black,
//    onSurface = Color.Black,

)

@Composable
fun MainTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
