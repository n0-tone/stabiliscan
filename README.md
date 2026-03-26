# StabiliScan

StabiliScan is an accessibility-focused Android application designed to assist elderly users and individuals with visual impairments or mild tremors in reading printed text. The app uses device sensors to ensure a stable image before processing, providing a reliable way to digitize and enlarge text from labels, documents, and signs.

## Main Features

- Stability Sensing: Automatically detects when the device is steady to capture the clearest possible image for text recognition.
- Text Recognition: Uses advanced OCR technology to convert printed characters into digital text.
- High Accessibility: Features a dedicated reading mode with very large, adjustable font sizes to accommodate different visual needs.
- Reading History: Saves previous captures so users can refer back to them later without needing to scan again.
- Modern Interface: Built with Jetpack Compose and Material 3, following the latest Android design standards for a fluid and intuitive experience.
- Dark Mode Support: Full compatibility with system light and dark themes, including a dynamic splash screen.

## How It Works

1. Launch the app and complete the initial onboarding.
2. Grant the required camera permission.
3. Point the camera at the text you wish to read.
4. The app will monitor the device's stability. Once stable, it processes the text.
5. A modal appears with the digitized text. Use the slider to adjust the font size to your comfort level.
6. Access previous scans anytime through the history menu.

## Technical Details

- Language: Kotlin
- UI Framework: Jetpack Compose
- Recognition Engine: Google ML Kit (Text Recognition)
- Camera: Android CameraX
- Data Management: Jetpack DataStore
- Minimum SDK: API 24 (Android 7.0)

## Permissions

This app requires Camera access to function. Camera data is processed locally on the device to ensure user privacy.
