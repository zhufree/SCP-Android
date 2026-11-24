#!/usr/bin/env python3
"""从旧图标 res/drawable/ic_launcher.jpg 抠出白色 SCP 标志，生成自适应图标前景。

用法（需要 Pillow、numpy）：python3 tools/make_adaptive_icon.py
输出：app/src/main/res/drawable-nodpi/ic_launcher_foreground.png（432x432，透明底白色标志，位于 66dp 安全区内）
同一张 PNG 也作为 monochrome 层（Android 13 主题图标只取 alpha）。背景是 drawable/ic_launcher_background.xml 的矢量渐变。
"""
from pathlib import Path

import numpy as np
from PIL import Image, ImageFilter

RES = Path(__file__).resolve().parent.parent / "app/src/main/res"
CANVAS = 432          # 108dp * 4
LOGO_DP = 64          # 标志宽度，安全区直径 66dp


def main():
    src = Image.open(RES / "drawable/ic_launcher.jpg").convert("RGB")
    rgb = np.array(src).astype(float)
    # 标志是纯白，绿色花纹的 R/B 通道接近 0：取 min(R, B) 作为 alpha
    alpha = np.clip((np.minimum(rgb[..., 0], rgb[..., 2]) - 70) / (190 - 70), 0, 1)
    mask = Image.fromarray((alpha * 255).astype("uint8"))
    # 原图只有 198px：6 倍上采样 + 模糊 + 拉高对比度，得到平滑锐利的边缘
    big = mask.resize((mask.width * 6, mask.height * 6), Image.LANCZOS).filter(ImageFilter.GaussianBlur(2.2))
    arr = np.clip((np.array(big).astype(float) / 255 - 0.5) * 2.2 + 0.5, 0, 1)
    big = Image.fromarray((arr * 255).astype("uint8"))
    logo = big.crop(big.point(lambda v: 255 if v > 127 else 0).getbbox())
    width = int(CANVAS * LOGO_DP / 108)
    logo = logo.resize((width, round(logo.height * width / logo.width)), Image.LANCZOS)
    white = Image.new("RGBA", logo.size, (255, 255, 255, 255))
    white.putalpha(logo)
    out = Image.new("RGBA", (CANVAS, CANVAS), (255, 255, 255, 0))
    out.paste(white, ((CANVAS - logo.width) // 2, (CANVAS - logo.height) // 2), white)
    out.save(RES / "drawable-nodpi/ic_launcher_foreground.png", optimize=True)


if __name__ == "__main__":
    main()
