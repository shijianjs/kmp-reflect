package io.github.shijianjs.kmp.reflect.demo.style

import web.cssom.Color
import web.cssom.CustomPropertyName
import web.cssom.`var`
import web.cssom.rgb

/**
 * The demo's palette, as Lumo tokens with a light fallback.
 *
 * The sections used to hard-code a dark page: their own colours, their own background. Inside the
 * shared shell they follow the theme switch instead, so every value is a Lumo custom property —
 * Lumo defines them on `:root` and flips them under `html[theme~="dark"]`, and custom properties are
 * inherited into shadow trees, which is how one switch repaints the sections without any of them
 * carrying a `[data-theme]` selector.
 *
 * The names say what the colour is *for*, not which token it happens to be: a section asks for
 * `hairline`, not for `--lumo-contrast-20pct`.
 */
internal val pageBackground: Color = `var`(CustomPropertyName<Color>("--lumo-base-color"), rgb(255, 255, 255))

internal val panel: Color = `var`(CustomPropertyName<Color>("--lumo-contrast-5pct"), rgb(246, 247, 251))

internal val panelStrong: Color = `var`(CustomPropertyName<Color>("--lumo-contrast-10pct"), rgb(236, 239, 244))

internal val bodyText: Color = `var`(CustomPropertyName<Color>("--lumo-body-text-color"), rgb(35, 39, 45))

internal val mutedText: Color = `var`(CustomPropertyName<Color>("--lumo-secondary-text-color"), rgb(87, 98, 110))

internal val accent: Color = `var`(CustomPropertyName<Color>("--lumo-primary-text-color"), rgb(0, 92, 207))

internal val okText: Color = `var`(CustomPropertyName<Color>("--lumo-success-text-color"), rgb(0, 116, 60))

internal val hairline: Color = `var`(CustomPropertyName<Color>("--lumo-contrast-20pct"), rgb(223, 228, 234))

internal val selectedFill: Color = `var`(CustomPropertyName<Color>("--lumo-primary-color-10pct"), rgb(236, 243, 255))

internal val okFill: Color = `var`(CustomPropertyName<Color>("--lumo-success-color-10pct"), rgb(232, 245, 233))

internal val mutedFill: Color = `var`(CustomPropertyName<Color>("--lumo-contrast-10pct"), rgb(236, 239, 244))
