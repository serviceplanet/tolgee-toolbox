# Tolgee Toolbox

**Tolgee Toolbox is an un-official collection of utilities and plugins to aid in the use of Tolgee.**

**While functional, at this point it is mostly a useful prototype.**

Let's start with the obvious question; How does this application differ from the (excellent!) [tolgee-cli](https://github.com/tolgee/tolgee-cli) tool? The `tolgee-cli` tool works great for front-end development but didn't really fit our Maven multi-module projects well (at the time of writing). 

Conceptually this tool also works a bit different. This tool uploads the entire message file to the "Import" functionality in Tolgee. Whereas the `tolgee-cli` tool works directly on the translation entries in Tolgee.

The minimum supported Java version by this tool is 21.

# Usage

The tolgee-toolbox application primarily works with configuration files called `tolgee-toolbox.toml` which are written in [TOML](https://toml.io/).

Each project is expected to have at least a single `tolgee-toolbox.toml` configuration file to tell these utilities how to interact with Tolgee (see an example below). `tolgee-toolbox.toml` files which contain a `[[projects]]` table should be placed in the directory alongside the message files.

Example `tolgee-toolbox.toml`:

```toml
# Defines configuration on how to interact with the Tolgee server.
[tolgee]
# Location of Tolgee REST API.
api.url = "http://127.0.0.1:8080"

[general]
# Stop if a project definition has no namespace specified. Prevents potentially messy accidents if you use namespaces 
# but forgot to configure one in a project definition.
missing_namespace_is_fail = true

# Multiple projects can be specified.
[[projects]]
# ID of the project in Tolgee.
tolgee.id = 1
# OPTIONAL: Namespace to use in the Tolgee project.
tolgee.namespace = "android.ui"

# Supported placeholders for the "files" option in the "projects.sources" and "projects.targets":
# 
# * ${locale separator=dash, region_case=lower} Inserts a locale tag such as 'nl_NL'. Requires the following options:
# ** 'separator':
# *** 'underscore' For example "nl_nl" or "en_US".
# *** 'dash' For example "nl-nl" or "en-us".
# ** 'region_case':
# *** 'lower' For example "nl_nl" or "en_us".
# *** 'upper' For example "nl_NL" or "en_NL".

# The message source files. These are pushed (uploaded) to the Tolgee's server import functionality.
[[projects.sources]]
# Select source files based on a file pattern with placeholders. 
files = "Messages_${locale separator=underscore, region_case=lower}.properties"

[[projects.sources]]
# Select a single source file (without using a placeholder).
files = "Messages.properties"
# The locale is mandatory if there is no placeholder from which a locale can be extracted 
# (i.e. no tag such as "${locale separator=underscore, region_case=lower}"). Must be specified as a IETF BCP 47 
# language tag string.
locale = "nl-NL"

# Specifies where the pulled (downloaded) messages from the Tolgee's server export functionality are written to.
[[projects.targets]]
# Pattern used to create message target files. 
files = "foo-directory_${locale separator=dash, region_case=upper}/Messages.properties"
# Message format to create. Possible types: "JSON", "XLIFF", "PO", "APPLE_STRINGS_STRINGSDICT", "APPLE_XLIFF", 
# "ANDROID_XML", "FLUTTER_ARB" "PROPERTIES"
type = "PROPERTIES"
# Do not create translation files for these locales. Useful if for example there is a base language without a 
# tag in the file name such as "Messages.properties". Must contain IETF BCP 47 language tag strings.
excluded_locales = ["nl-NL"]

[[projects.targets]]
# Select a single target file (without using a placeholder).
files = "Messages.properties"
type = "PROPERTIES"
# The locale is mandatory if there is no placeholder from which a locale can be extracted 
# (for example "${locale_underscore_lower}")
locale = "nl-NL"
```

`tolgee-toolbox.toml` Files in child directories inherit settings from parent directories and can override these settings. Take a look at the following example:

```
my-project/
├─ module-a/
│  ├─ tolgee-toolbox.toml     
├─ module-b/
│  ├─ tolgee-toolbox.toml
├─ tolgee-toolbox.toml
```

`module-a/tolgee-toolbox.toml` and `module-b/tolgee-toolbox.toml` inherit the `[tolgee]` and `[general]` sections from the `my-project/tolgee-toolbox.toml` file.

You can then use the `tolgee-toolbox` tool in the project root (`my-project/`) to push and pull translations to Tolgee.

# Examples

## A basic Java back-end project 

Below is an example setup of what a pretty basic Java project could look like (for brevity we have left out the actual Java sources). This is not necessarily the best setup, it's just an example of how a lot of Java projects are structured.

In this setup it is assumed developers add messages to the `Messages.properties` files in English (`en-US`) and these need to be uploaded to Tolgee. The files containing translations (such as `Messages_nl_NL.properties`, `Messages_de_DE.properties`, etc.) are not touched by the developers and are maintained solely in Tolgee (and should therefor be downloaded from Tolgee).

**Project layout:**

```
my-project/
├─ module-a/
│  ├─ src
│  │  └─ main
│  │     └─ resources
│  │        ├─ Messages.properties
│  │        ├─ Messages_nl_NL.properties
│  │        ├─ Messages_de_DE.properties
│  │        └─ tolgee-toolbox.toml 
│  └─ pom.xml 
├─ module-b/
│  ├─ src
│  │  └─ main
│  │     └─ resources
│  │        ├─ Messages.properties
│  │        ├─ Messages_nl_NL.properties
│  │        ├─ Messages_de_DE.properties
│  │        └─ tolgee-toolbox.toml 
│  └─ pom.xml
├─ pom.xml
└─ tolgee-toolbox.toml
```

**Config files:**

`my-project/tolgee-toolbox.toml`:
```toml
[tolgee]
# Location of Tolgee REST API.
api.url = "http://127.0.0.1:8080"
```

`my-project/module-a/src/main/resources/tolgee-toolbox.toml`:

```toml
[[projects.sources]]
# Select a single source file (without using a placeholder).
files = "Messages.properties"
# The locale is mandatory if there is no placeholder from which a locale can be extracted 
# (i.e. no tag such as "${locale separator=underscore, region_case=lower}")
locale = "en-US"

# Specifies where the pulled (downloaded) messages from the Tolgee's server export functionality are written to.
[[projects.targets]]
# Pattern used to create message target files. 
files = "Messages_${locale separator=underscore, region_case=upper}.properties"
# Message format to create. Possible types: "JSON", "XLIFF", "PO", "APPLE_STRINGS_STRINGSDICT", "APPLE_XLIFF", 
# "ANDROID_XML", "FLUTTER_ARB" "PROPERTIES"
type = "PROPERTIES"
```

`my-project/module-b/src/main/resources/tolgee-toolbox.toml`:

```toml
[[projects.sources]]
# Select a single source file (without using a placeholder).
files = "Messages.properties"
# The locale must be specified (and only be specified) if there is no placeholder from which a locale can be extracted 
# (i.e. no tag such as "${locale separator=underscore, region_case=lower}")
locale = "en-US"

# Specifies where the pulled (downloaded) messages from the Tolgee's server export functionality are written to.
[[projects.targets]]
# Pattern used to create message target files. 
files = "Messages_${locale separator=underscore, region_case=upper}.properties"
# Message format to create. Possible types: "JSON", "XLIFF", "PO", "APPLE_STRINGS_STRINGSDICT", "APPLE_XLIFF", 
# "ANDROID_XML", "FLUTTER_ARB" "PROPERTIES"
type = "PROPERTIES"
```

Push (upload) translation files to Tolgee. This is similar to using the "Import" function in Tolgee:

```shell
$ cd my-project
$ export TOLGEE_TOOLBOX_API_KEY=<YOUR SUPER SECRET TOLGEE API KEY>
$ tolgee-toolbox push
```

You can now navigate to the "Import" page of the Tolgee project and complete the import. 

Pull (download) all translations from Tolgee and update the local translation files. This is similar to using the "Export" function in Tolgee.

```shell
$ cd my-project
$ export TOLGEE_TOOLBOX_API_KEY=<YOUR SUPER SECRET TOLGEE API KEY>
$ tolgee-toolbox pull
```

# Development 

Starting a Tolgee development environment:

```shell
$ podman run -v tolgee_data:/data/ -p 8085:8080 tolgee/tolgee
```

# Known gotchas / issues / limitations

* Tolgee by default supports up to 100 entries in its importer. If you exceed this you will get an HTTP 400 error. See Tolgee's [ImportService.kt](https://github.com/tolgee/tolgee-platform/blob/c39d3dbb5351ffc7d237f1a854d146eb6663d851/backend/data/src/main/kotlin/io/tolgee/service/dataImport/ImportService.kt#L89-L91).
* `toolgee-toolbox pull` will fail when there are no translations yet for a language. Tolgee will return a HTTP 400 (bad request) when trying to export.
