---
name: adapt-template-client-template
description: Use when the user wants to adapt or initialize the template-client template for a new project — renaming packages, iOS framework name, project slug, or external API dependency after forking.
---

# Adapting template-client for a New Project

## Overview

This skill adapts the `template-client` KMP mobile template to a specific project by replacing all placeholder identifiers with real project values.

## Step 1 — Collect Parameters

Ask the user for these values (show as a table, wait for all answers before proceeding):

| Parameter | Template value | Description |
|---|---|---|
| `rootProjectName` | `template-client` | Full name for `rootProject.name` in `settings.gradle.kts` (e.g. `dentflow-client`) |
| `mavenGroup` | `com.template` | Maven group / top-level package (e.g. `com.dentflow`) |
| `packageRoot` | `com.template.client` | Root Kotlin package used in all source files (e.g. `com.dentflow.client`) |
| `androidAppId` | `com.template.client.android` | Android `namespace` and `applicationId` in `androidApp/build.gradle.kts` |
| `iosFrameworkName` | `TemplateShared` | iOS XCFramework base name used in `shared/build.gradle.kts` and Xcode (e.g. `DentFlowShared`) |
| `apiDependencyGroup` | `com.template` | Group of the external `client-api-core` artifact in `libs.versions.toml` |
| `apiDependencyName` | `client-api-core` | Artifact name of the external API dependency (e.g. `dentflow-client-api-core`) |

Derived automatically (do not ask):
- `packagePath` = `packageRoot` with `.` replaced by `/` (for directory structure)

## Step 2 — Show Substitution Plan

Before making any changes, present the complete list of planned substitutions grouped by file and confirm with the user.

## Step 3 — Execute Changes

Apply changes in this exact order.

### 3.1 Gradle build files

**`settings.gradle.kts`**
- `"template-client"` → `"{rootProjectName}"`

**`build.gradle.kts` (root)**
- `group = "com.template"` → `group = "{mavenGroup}"`

**`androidApp/build.gradle.kts`**
- `namespace = "com.template.client.android"` → `namespace = "{androidAppId}"`
- `applicationId = "com.template.client.android"` → `applicationId = "{androidAppId}"`

**`shared/build.gradle.kts`**
- `namespace = "com.template.client.shared"` → `namespace = "{packageRoot}.shared"`
- `val frameworkName = "TemplateShared"` → `val frameworkName = "{iosFrameworkName}"`

**`client-kit/core/data/build.gradle.kts`**
- `namespace = "com.template.client.kit.core.data"` → `namespace = "{packageRoot}.kit.core.data"`

**`client-kit/core/domain/build.gradle.kts`**
- `namespace = "com.template.client.kit.core.domain"` → `namespace = "{packageRoot}.kit.core.domain"`

**`client-kit/core/presentation/build.gradle.kts`**
- `namespace = "com.template.client.kit.core.presentation"` → `namespace = "{packageRoot}.kit.core.presentation"`

**`client-kit/auth/data/build.gradle.kts`**
- `namespace = "com.template.client.kit.auth.data"` → `namespace = "{packageRoot}.kit.auth.data"`

**`client-kit/auth/domain/build.gradle.kts`**
- `namespace = "com.template.client.kit.auth.domain"` → `namespace = "{packageRoot}.kit.auth.domain"`

**`client-kit/auth/presentation/build.gradle.kts`**
- `namespace = "com.template.client.kit.auth.presentation"` → `namespace = "{packageRoot}.kit.auth.presentation"`

**`client-kit/navigation/data/build.gradle.kts`**
- `namespace = "com.template.client.kit.navigation.data"` → `namespace = "{packageRoot}.kit.navigation.data"`

**`client-kit/navigation/domain/build.gradle.kts`**
- `namespace = "com.template.client.kit.navigation.domain"` → `namespace = "{packageRoot}.kit.navigation.domain"`

**`client-kit/navigation/presentation/build.gradle.kts`**
- `namespace = "com.template.client.kit.navigation.presentation"` → `namespace = "{packageRoot}.kit.navigation.presentation"`

### 3.2 Version catalog

**`gradle/libs.versions.toml`**
- Rename alias `template-client-api-core` → `{camelCase of apiDependencyName}` (update both definition and all usages in `build.gradle.kts` files)
- `group = "com.template"` (for the api-core entry) → `group = "{apiDependencyGroup}"`
- `name = "client-api-core"` → `name = "{apiDependencyName}"`

Also rename the version alias `template-client-api-version` to something matching the new name.

### 3.3 Kotlin source files — package declarations and imports

For every `.kt` file under the project:
- `package com.template.client` → `package {packageRoot}`
- `import com.template.client` → `import {packageRoot}`

Use Grep to find all affected files first:
```bash
grep -rl "com\.template\.client" --include="*.kt" .
```

Then apply edits to each file using `replace_all: true`.

### 3.4 Move source directories

After editing file contents, move the source directories to match the new package path.

The current directory structure uses `com/template/client/` inside each `src/` tree. Before running, replace `OLD_PKG_PATH` and `NEW_PKG_PATH` literally in the script with the actual paths (do NOT use shell variables — use `sed` to avoid `/` delimiter conflicts).

```bash
# OLD_PKG_PATH = com/template/client
# NEW_PKG_PATH = {packageRoot with . replaced by /}  e.g. com/dentflow/client

find . -type d -path "*/kotlin/OLD_PKG_PATH" | grep -v "/build/" | while read dir; do
  newdir="$(echo "$dir" | sed 's|OLD_PKG_PATH|NEW_PKG_PATH|')"
  mkdir -p "$(dirname "$newdir")"
  mv "$dir" "$newdir"
  old_parent="$(dirname "$dir")"
  while [ "$old_parent" != "." ] && rmdir "$old_parent" 2>/dev/null; do
    old_parent="$(dirname "$old_parent")"
  done
done
```

**Before running:** substitute the literal strings `OLD_PKG_PATH` and `NEW_PKG_PATH` with the actual paths. Run from the repo root.

### 3.5 iOS framework reference

After renaming `frameworkName` in `shared/build.gradle.kts`, update the Xcode project to reference `{iosFrameworkName}.xcframework` instead of `TemplateShared.xcframework`.

This step is manual — instruct the user to update it in Xcode or notify them of the change.

### 3.6 Update CLAUDE.md

In `CLAUDE.md`:
- Update **Project Overview** section: project name, package, framework name
- Update **Identifier Map** table with new values
- Update the **Step-by-Step Rename** examples to reflect the new baseline

## Step 4 — Verify

After all changes:

1. Grep for remaining occurrences of old identifiers:
```bash
grep -r "com\.template\|TemplateShared\|template-client" \
  --include="*.kt" --include="*.kts" --include="*.toml" --include="*.md" \
  --exclude-dir=build --exclude-dir=".gradle" .
```

2. Fix any remaining occurrences before finishing.

3. Report a summary: files changed, directories renamed, manual steps remaining (Xcode update).

## Common Mistakes

| Mistake | Fix |
|---|---|
| Renaming directories before editing file contents | Always edit first, rename after |
| Forgetting `gradle/libs.versions.toml` | The external API alias and group must be updated there |
| Only updating `namespace` but not `applicationId` | Both must be updated in `androidApp/build.gradle.kts` |
| Skipping directory rename | Package declaration and directory path must match or Kotlin won't compile |
| Forgetting the Xcode project | `TemplateShared.xcframework` reference in Xcode must be updated manually |
| Missing test files | `commonTest/` files also contain `package com.template.client` — update them too |
