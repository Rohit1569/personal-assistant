# KIWI AI Assistant - Technical Documentation

## 1. Overview
KIWI AI is a next-generation personal productivity assistant that combines voice biometrics, automated intent processing, and deep system integration to handle tasks, finance, and health tracking.

---

## 2. System Architecture
The system follows a **Client-Server Architecture**:
- **Frontend:** Android Application (Kotlin, Jetpack Compose, Hilt, Retrofit).
- **Backend:** Node.js API (Express, Sequelize ORM).
- **Database:** PostgreSQL (Hosted on Neon Tech).

---

## 3. Core Implementation Flows

### 3.1 Voice Command Flow
How a user command is processed from sound to action:
1.  **Capture:** `NeuralMicManager` uses Android's `SpeechRecognizer` with the user's selected `Locale`.
2.  **Transcription:** Voice is converted to text locally on the device.
3.  **Parsing:** The text is passed to `VoiceIntentProcessor.kt`. It uses keyword matching and regex to identify the **Intent** (e.g., "Schedule Meeting", "Send WhatsApp").
4.  **Execution:** The `SchedulingViewModel` receives the `IntentResult` and calls the appropriate manager:
    -   `CommunicationManager` for SMS/WhatsApp.
    -   `BackgroundManager` for Google Calendar CRUD.
    -   `ExternalAppManager` for YouTube/Maps/Amazon.
5.  **Feedback:** `TtsManager` speaks a confirmation back to the user in their selected language.

### 3.2 Authentication & Security Flow
1.  **Signup:** User enters details -> Backend sends OTP via Nodemailer -> User verifies OTP.
2.  **Single-Device Binding:** During verification, the app captures `Settings.Secure.ANDROID_ID`. The backend saves this as `device_id`. Any future login attempt from a different ID is blocked.
3.  **Voice Biometric Enrollment:** After signup, users are redirected to `VoiceEnrollmentScreen.kt`. They record specific phrases. The app generates a biometric signature (MFCC-based) which is encrypted and stored in the database.
4.  **Verification:** Future sensitive commands require a real-time voice match against this signature.

### 3.3 Multi-Language Support
1.  **Selection:** User selects a language from the UI globe icon.
2.  **STT Sync:** The `Locale` is passed to `SpeechRecognizer` intents to improve regional accent accuracy (e.g., `en-IN` vs `en-US`).
3.  **TTS Sync:** `TtsManager.setLanguage(locale)` is called so the AI's "voice" matches the user's preference.
4.  **Processor Sync:** `VoiceIntentProcessor` contains multi-lingual keyword maps (English, Hindi, Hinglish, Spanish).

---

## 4. Frontend Documentation (Android)

### Tech Stack
- **UI:** Jetpack Compose (Material 3)
- **Dependency Injection:** Hilt
- **Networking:** Retrofit 2 & OkHttp
- **Speech Engine:** Android SpeechRecognizer API
- **Local Storage:** SharedPreferences (via TokenManager)

### Key UI Modules
- **BOT Screen:** Central AI orb and voice interaction hub.
- **PLAN Screen:** Task, Notes, and local-only "Ideas" tracking.
- **MONEY Screen:** Financial expense logging.
- **BODY Screen:** Fitness profile management synced to cloud.
- **CONTROL Screen:** Admin dashboard (visible only to `admin` role).

---

## 5. Backend Documentation (Node.js)

### Tech Stack
- **Runtime:** Node.js
- **Framework:** Express.js
- **ORM:** Sequelize (PostgreSQL Dialect)
- **Security:** JWT (Role-based), Bcrypt.js, Helmet

### Database Schema Highlights
- **Users:** `id`, `email`, `password_hash`, `device_id`, `voice_signature`, `role`, `is_active`.
- **FitnessProfiles:** Linked via `user_id`. Stores metrics like height, weight, water intake.
- **Tasks / Notes / Expenses:** Standard relational tables for user data.

---

## 6. Administrative Control System
- **Access Management:** Admins can search for any user and toggle their `is_active` status.
- **Data Inspection:** Admins have a tabular view of all logs (Expenses, Tasks, Notes) for any specific user to provide support or monitoring.
- **JWT Protection:** The `isAdmin` middleware protects sensitive routes from standard user access.

---

## 7. Installation & Setup

### Backend
1. `cd backend`
2. `npm install`
3. Configure `.env` with `DATABASE_URL`, `JWT_SECRET`, and SMTP credentials.
4. `npx sequelize-cli db:migrate`
5. `npm start`

### Frontend
1. Open in Android Studio.
2. Update `NetworkModule.kt` with your current Mac IP (e.g., `192.168.0.x`).
3. Build and run on a physical device.

---

## 8. Troubleshooting
- **Accessibility Service:** If "Kiwi Voice Automation" is greyed out on sideloaded APKs, go to App Info -> 3 dots -> "Allow restricted settings".
- **Connection Error:** Ensure your phone and Mac are on the same Wi-Fi and the IP in `NetworkModule.kt` matches your Mac's current IP.
