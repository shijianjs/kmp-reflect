package io.github.shijianjs.kmp.reflect.demo.page.theme

import web.cssom.Color
import web.cssom.rgb

/**
 * The demo's palette, as typed colours.
 *
 * A token several components must paint with is exactly what a shared constant is for: the page,
 * the header and the sections all read the same values, and a colour changes in one place.
 */
internal val pageBackground: Color = rgb(15, 17, 21)

internal val panel: Color = rgb(23, 26, 33)

internal val panelStrong: Color = rgb(30, 34, 43)

internal val bodyText: Color = rgb(229, 231, 235)

internal val mutedText: Color = rgb(154, 163, 175)

internal val accent: Color = rgb(110, 168, 254)

internal val okText: Color = rgb(74, 222, 128)

internal val hairline: Color = rgb(42, 47, 58)

internal val selectedFill: Color = rgb(110, 168, 254, 0.12)

internal val okFill: Color = rgb(74, 222, 128, 0.12)

internal val mutedFill: Color = rgb(154, 163, 175, 0.12)
