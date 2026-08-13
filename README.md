# TeleFlow


**TeleFlow** is a native Android Telegram client foundation built with Kotlin, Jetpack Compose, and TDLib's asynchronous JSON interface. It is designed for a secure, local-first sign-in flow and displays only conversations and messages supplied by the signed-in user's Telegram account; it does not seed or simulate chat data.


> TeleFlow is an independent third-party client. It is not affiliated with, endorsed by, or sponsored by Telegram.


## Implemented capabilities


| Area | Included in this repository |
|---|---|
| Authentication | TDLib parameter setup, encrypted TDLib database key, phone number sign-in, verification-code sign-in, two-step verification password, resend-code request, QR confirmation state, and logout. |
| Messaging | Loads the real main chat list through TDLib, searches and filters loaded conversations, retrieves chat history, handles incoming-message updates, and sends text messages. |
| Experience | Material 3 interface, light/dark appearance, Arabic RTL-aware layout, English/Arabic string resources, responsive chat list, readable empty and error states. |
| Security | No hard-coded Telegram credentials, secrets excluded by `.gitignore`, Android Keystore-backed encrypted storage for the TDLib database key, release shrinking, clear-text traffic disabled, and no logging of phone numbers, OTPs, passwords, or API hashes. |
| Quality | Local unit and instrumentation tests, reproducible Gradle wrapper, release build instructions, and CI workflow. |


## Architecture


TeleFlow uses a deliberately small, maintainable structure. `TdlibJsonClient` owns a single TDLib JSON client and correlates asynchronous responses with their requests. `TelegramRepository` interprets authorization and data updates into immutable UI state. Compose screens render that state without adding mock chat or message data.


| Layer | Responsibility |
|---|---|
| `config` | Reads build-time credentials and protects TDLib's local database key with Android Keystore-backed encrypted preferences. |
| `data` | Owns TDLib initialization, JSON requests, authorization-state handling, chat updates, history retrieval, and text sending. |
| `ui` | Provides native Material 3 login, chat-list, search/filter, message-history, and composer experiences. |


## Requirements


| Requirement | Version / Notes |
|---|---|
| Android Studio | Ladybug or newer recommended |
| JDK | 17 or newer |
| Android SDK | Platform 35 and Build Tools 35.0.0 |
| Android device | Android 8.0 (API 26) or newer |
| Telegram credentials | A personal `API ID` and `API Hash` obtained through Telegram's API development tools |


## Secure Telegram API setup


Telegram requires each published third-party client to use its own API ID and API hash. Create them through [my.telegram.org/apps](https://my.telegram.org/apps) after signing in with an official Telegram app. Read Telegram's [API ID guidance](https://core.telegram.org/api/obtaining_api_id) and [API Terms of Service](https://core.telegram.org/api/terms) before distribution.


**Never commit the API hash, passwords, OTPs, session files, signing keys, or tokens.** TeleFlow resolves configuration in this order: Gradle properties, environment variables, then the ignored `local.properties` file.


### Option A — local.properties (recommended for Android Studio)


Create or update `local.properties` in the repository root. Preserve the SDK line and add the following placeholders with your own values:


```properties
sdk.dir=/absolute/path/to/Android/Sdk
TELEGRAM_API_ID=12345678
TELEGRAM_API_HASH=replace_with_your_api_hash
```


### Option B — user-level Gradle properties


Add the values to `~/.gradle/gradle.properties`:


```properties
TELEGRAM_API_ID=12345678
TELEGRAM_API_HASH=replace_with_your_api_hash
```


### Option C — environment variables


```bash
export TELEGRAM_API_ID=12345678
export TELEGRAM_API_HASH=replace_with_your_api_hash
./gradlew assembleDebug
```


The project deliberately builds without credentials for CI and static verification. At runtime it displays a configuration-required screen until valid local credentials are supplied. Do not use Telegram's sample API ID for a released product; Telegram explicitly limits it to testing.


## Build and run


```bash
git clone https://github.com/f6niir-del/teleflow-android-tdlib-client.git
cd teleflow-android-tdlib-client
# Configure your credentials by one of the safe methods above.
./gradlew testDebugUnitTest
./gradlew assembleDebug
```


Install `app/build/outputs/apk/debug/app-debug.apk` on an Android device or emulator. TDLib requires real network access and an actual Telegram account to execute sign-in and messaging flows.
