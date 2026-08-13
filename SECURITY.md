# Security Policy

TeleFlow never requires developers to commit Telegram credentials, session files, passwords, one-time codes, signing keys, or service tokens. Configure `TELEGRAM_API_ID` and `TELEGRAM_API_HASH` only in an ignored local configuration file, a user-level Gradle property, or environment variables.

If you discover a vulnerability or accidentally commit a credential, revoke or rotate the affected credential immediately and contact the repository maintainer privately. Do not create a public issue containing secret material.
