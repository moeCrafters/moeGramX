<div align="center">
<a href="https://moegramx.t.me/">
        <picture>
          <source media="(prefers-color-scheme: dark)" srcset="https://files.kiri.su/moeGramX_dark.svg">
          <img src="https://files.kiri.su/moeGramX.svg">
        </picture>
    </a>

the **moest** client based on [Telegram-X](https://github.com/TGX-Android/Telegram-X) and [TDLib](https://core.telegram.org/tdlib) ~

[![Issues](https://img.shields.io/github/issues/moeCrafters/moeGramX?style=flat-square&color=red)](https://github.com/moeCrafters/moeGramX/issues)
[![Forks](https://img.shields.io/github/forks/moeCrafters/moeGramX?style=flat-square&color=blue)](https://github.com/moeCrafters/moeGramX/network/members)
[![Stars](https://img.shields.io/github/stars/moeCrafters/moeGramX?style=flat-square&color=yellow)](https://github.com/moeCrafters/moeGramX/stargazers)

[![Channel](https://img.shields.io/badge/Channel-%40moeGramX-blue?style=flat-square&logo=telegram&label=Channel)](https://t.me/moegramx)
[![Chat](https://img.shields.io/badge/Chat-%40moex__log-blue?style=flat-square&logo=telegram&label=Updates)](https://t.me/moe_chat)
[![Updates](https://img.shields.io/badge/Release-%40moe__chat-blue?style=flat-square&logo=telegram&label=Discussion)](https://t.me/moe_log)
</div>

## Features

- Message details
- Ability to replace mobile number<sup>(with username/userid/hidden label)</sub></sup>
- Hide reactions
- Hide new messages counter<sup><sup>(in the drawer burger)</sup></sup>
- Sent photos with high resolution <sup>(800px/1280px/2560px)</sup>
- Blur drawer background
- Square avatars
- Message panel buttons hiding
- Remember message options <sup>(copy/silent/sound)
- Do not send **choosing** sticker status<sup>typing status will be sent instead</sup>
- Copy photo or document<sup><sup>(with photo)</sup></sup> option
- and a bit more...
---
## Installation

### Prerequisites

* At least **5,34GB** of free disk space: **487,10MB** for source codes and around **4,85GB** for files generated after building all variants
* **4GB** of RAM
* **macOS** or **Linux**-based operating system. **Windows** platform is supported by using [MSYS](https://www.msys2.org/) (e.g., [Git Bash](https://gitforwindows.org/)).

#### macOS

* [Homebrew](https://brew.sh)
* git with LFS, wget and sed: `$ brew install git git-lfs wget gsed && git lfs install`

#### Linux

* git with LFS: Install with your package manager, e.g. `# apt install git git-lfs` for any Debian-based distribution
* Run `$ git lfs install` for the current user, if you didn't have `git-lfs` previously installed

#### Windows

* **moeGramX** does not provide official build instructions for Windows platform. It is recommended to rely on Linux distributions via [WSL](https://learn.microsoft.com/en-us/windows/wsl/install) instead.

## Build

-  **moeGramX** can be built with **Android Studio** <a href="#"><img src="https://i.imgur.com/cPvvFDP.png" align="center" width="20" height="23"/></a> or from the command line with **Gradle**:
1. `$ git clone --recursive --depth=1 --shallow-submodules https://github.com/moeCrafters/moeGramX mgx` — clone **moeGramX** with submodules
2. In case you forgot the `--recursive` flag, `cd` into `mgx` directory and: `$ git submodule init && git submodule update --init --recursive --depth=1`
3. Create `keystore.properties` file outside of source tree with the following properties:<br/>`keystore.file`: absolute path to the keystore file<br/>`keystore.password`: password for the keystore<br/>`key.alias`: key alias that will be used to sign the app<br/>`key.password`: key password.<br/>**Warning**: keep this file safe and make sure nobody, except you, has access to it. For production builds one could use a separate user with home folder encryption to avoid harm from physical theft
4. `$ cd mgx`
5. Run `$ scripts/./setup.sh` and follow up the instructions
6. If you plan to use GCM push messages, [setup Firebase](https://firebase.google.com/docs/android/setup) and replace `google-services.json` with the one that's suitable for the `app.id` you need
7. Now you can open the project using **[Android Studio](https://developer.android.com/studio/)** <a href="#"><img src="https://i.imgur.com/cPvvFDP.png" align="center" width="20" height="23"/></a> or build manually from the command line: `./gradlew assembleUniversalRelease`.

#### Available flavors <img src="https://developer.android.com/static/images/logos/android.svg" align="center" width="20" height="20"/>

* `arm64`: **arm64-v8a** build with `minSdkVersion` set to `21` (**Lollipop**)
* `arm32`: **armeabi-v7a** build
* `x64`: **x86_64** build with `minSdkVersion` set to `21` (**Lollipop**)
* `x86`: **x86** build
* `universal`: universal build that includes native bundles for all platforms.
---

## Contributing

This is a thing you can do without any special skills!

Here are a few things you can do:

- [Test and report issues](https://github.com/moeCrafters/moeGramX/issues/new/choose)
- [Translate the moegram strings into your language](https://crowdin.com/project/moex) -
  **moeGramX** is a fork of **Telegram-X** and most of the localizations follow the translations of **Telegram-X**, check it out [here](https://translations.telegram.org/en/android_x/). As for specialized strings for **moeGramX**, we use **Crowdin** to translate client.
---

## Third-party dependencies

List of third-party components used in **moeGramX** can be found [here](/docs/THIRDPARTY.md). Additionally, you can check the specific commit of the third-party component used, for example, [here](/app/jni/thirdparty) and [here](/thirdparty).

---

## License

`moeGramX` is licensed under the terms of the GNU General Public License v3.0.

License of components and third-party dependencies it relies on might differ, check `LICENSE` file in the corresponding folder.

[![License: GPLv3](https://img.shields.io/badge/License-GPL%20v3-red.svg?style=for-the-badge&color=E87777)](https://github.com/moeCrafters/moeGramX/blob/main/LICENSE)
