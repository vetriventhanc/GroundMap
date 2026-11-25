
<div align="center">

# 🌍 GroundMap

### Smart Asset Management for Android

**Register assets. Track their condition. Report faults. Keep an activity history.**

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://www.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Status](https://img.shields.io/badge/Status-In%20Development-orange)]()

</div>

---

## 📖 About GroundMap

**GroundMap** is an Android asset-management application designed to help users manage assets and monitor their operational information from one place.

The app provides tools to register assets, view workspace information, update asset status and physical condition, report faults, and review recorded asset activities.

GroundMap is being developed with a focus on a clear mobile experience and practical asset operations.

## ✨ Features

### 🏷️ Asset Management
- Register assets and access them through the workspace.
- View asset information and details.
- Identify assets using their asset codes.

### ⚙️ Asset Operations
- Update an asset’s operational status.
- Update its physical condition.
- View current status and condition information.

### 🚨 Fault Reporting
- Submit fault reports for an asset.
- Set fault severity.
- Review previously reported faults.
- Update a report’s status.
- Add resolution notes and follow-up information.

### 🕘 Activity History
- View recorded asset activities.
- Review changes associated with assets.
- Follow asset events through the activity feed.

### 📱 Android Experience
- Native Android application.
- Kotlin and Jetpack Compose interface.
- Designed for use on Android devices and emulators.

## 🛠️ Technology Stack

| Area | Technology |
|---|---|
| Mobile platform | Android |
| Programming language | Kotlin |
| UI framework | Jetpack Compose |
| Backend | FastAPI (Python) |
| Database | PostgreSQL |
| ORM / Database driver | SQLAlchemy / psycopg |
| Database migrations | Alembic |
| API server | Uvicorn |
| IDE | Android Studio |

## 🗂️ Project Structure

```text
GroundMap/
└── android/
    └── app/
        └── src/
            └── main/
                ├── java/
                │   └── com/
                │       └── groundmap/
                │           └── android/
                │               ├── data/
                │               └── ui/
                └── AndroidManifest.xml
```

*This is a high-level overview. Adjust it if your repository structure differs.*

## 🚀 Getting Started

### Prerequisites

Before running GroundMap, make sure you have:

- [Android Studio](https://developer.android.com/studio)
- Android SDK and a compatible Android emulator, or a physical Android device
- Git
- The GroundMap backend and database configured for your environment

### 1. Clone the Repository

```bash
git clone https://github.com/vetriventhanc/GroundMap.git
cd GroundMap
```

### 2. Open the Android Project

1. Open Android Studio.
2. Select **Open**.
3. Choose the `android` directory in the cloned repository.
4. Allow Gradle to sync and complete any required setup.

### 3. Configure the Backend

GroundMap’s backend uses FastAPI and connects to a PostgreSQL database through SQLAlchemy.

1. Navigate to the backend directory.
2. Create a `.env` file in that directory.
3. Add your PostgreSQL connection string:

   ```env
   DATABASE_URL=postgresql+psycopg://USERNAME:PASSWORD@HOST:PORT/DATABASE_NAME

### 4. Run the App

1. Start your Android emulator or connect an Android device.
2. Select the device in Android Studio.
3. Run the app using the **Run** button.

## 🧭 Application Workflow

A typical workflow in GroundMap:

1. Open the app and access the workspace.
2. Register an asset or select an existing asset.
3. View the asset’s details and current condition.
4. Update its operational status or physical condition when needed.
5. Submit a fault report if an issue is identified.
6. Review the asset’s activity history to follow recorded events.

## 🖼️ Screenshots

### Dashboard
![GroundMap Dashboard](screenshots/dashboard.png)

### Asset Registry
![GroundMap Asset Registry](screenshots/asset-registry.png)

### QR Scanner
![GroundMap QR Scanner](screenshots/qr-scanner.png)

### Activity History
![GroundMap Activity History](screenshots/activity.png)

### Workspace
![GroundMap Workspace](screenshots/workspace.png)

### Asset Details
![GroundMap Asset Details](screenshots/asset-details.png)

## 🧪 Development Status

GroundMap is **under active development**.

Current implemented areas include:

- Workspace access and asset registration
- Asset operations and condition updates
- Fault reporting and report updates
- Asset activity history

The application and its documentation may change as development continues.

## 🗺️ Roadmap

Potential future improvements:

- [ ] Add screenshots and a short demo
- [ ] Expand asset search and filtering
- [ ] Improve fault report tracking
- [ ] Add more asset-history filtering options
- [ ] Document backend setup and API configuration
- [ ] Prepare a release build for testing

## 🔐 Configuration and Security

- Keep API keys, credentials, and private configuration out of source control.
- Use environment-appropriate API addresses.
- Do not commit production secrets or private user data.

## 🤝 Contributing

Ideas, feedback, and bug reports are welcome.

1. Fork the repository.
2. Create a branch for your change.
3. Make and test your changes.
4. Open a pull request describing what you changed.

For larger changes, consider opening an issue first to discuss the proposal.

## 📄 License

GroundMap is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

---

<div align="center">

### 🌍 GroundMap — Know your assets. Track every change.

Made with Kotlin and Jetpack Compose.

</div>
