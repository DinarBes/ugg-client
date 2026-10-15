---
name: adaptive-compose-layout
description: Use when implementing or changing responsive/adaptive Compose layouts, choosing layouts by window size, moving navigation between Scaffold slots, or discussing phone/tablet/desktop/web form factors in this KMP project.
---

# Adaptive Compose Layout

## Source of Truth

`KofShapeTheme` from `com.kartofans:ui-shape-styles` calculates `WindowAdaptiveInfo` once at the root of the Compose tree and provides it through `LocalKofAdaptiveInfo`.

Consume it directly in the composable that owns the alternative layouts:

```kotlin
val windowSizeClass = LocalKofAdaptiveInfo.current.windowSizeClass
```

Use the standard `WindowSizeClass` breakpoints to choose the composition:

```kotlin
val isMediumOrLarger = windowSizeClass.isWidthAtLeastBreakpoint(
    WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
)
```

The consuming presentation module must explicitly depend on `libs.kof.ui.shape.styles`.

## Rules

- Do not recalculate window size from `LocalWindowInfo`, `LocalDensity`, pixels, or custom `dp` thresholds.
- Do not create another adaptive provider, CompositionLocal, AppState flag, or screen-specific form-factor service in the application.
- Do not pass booleans such as `compact` through reusable UI components. Choose the composition at the layout owner and place the same component in the appropriate container or `Scaffold` slot.
- Keep `KofScaffold`/Material components in their intended structure and apply the supplied `innerPadding` to content. Do not reproduce Scaffold inset or bar measurement logic manually.
- Use window size to select layout, not operating system or device-name checks. This keeps common Compose code valid on Android, iOS, desktop, and web.
- Native Swift screens are independent of this Compose environment. Do not move adaptive state into shared business `AppState` merely to support Swift UI.

## Changing the Environment

If the adaptive source itself must change, update `KofShapeTheme` in `../kof-ui/ui-shape-styles`; do not override it in an application. Preserve `windowAdaptiveInfo` as an injectable theme parameter so previews and tests can provide deterministic values.

Use the stable `org.jetbrains.compose.material3.adaptive:adaptive` version supported by every configured KMP target, including `iosX64`. Verify Android, Wasm, and configured iOS targets before upgrading it.
