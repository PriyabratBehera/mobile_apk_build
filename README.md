# Dummy Mobile CI App

A small Android + Node.js + PostgreSQL project for learning:

1. Build the Android APK manually.
2. Build the APK on an Azure Linux VM.
3. Build the APK using GitHub Actions on that VM.
4. Later, connect the workflow to Azure App Distribution/other distribution services.

## Architecture

Android APK -> Node.js/Express API -> PostgreSQL

The Android app displays employees and can add an employee.

## Project structure

- `android-app/` - native Android app written in Kotlin
- `backend/` - Node.js + Express API
- `docker-compose.yml` - PostgreSQL for local development

## 1. Start PostgreSQL

From the project root:

```bash
docker compose up -d database
```

## 2. Start backend

```bash
cd backend
npm install
npm start
```

The API runs on:

```text
http://localhost:5000
```

Health check:

```text
GET http://localhost:5000/api/health
```

## 3. Android API URL

For the Android emulator, the host machine is available as:

```text
http://10.0.2.2:5000
```

This is already configured in:

`android-app/app/src/main/java/com/example/dummyapp/MainActivity.kt`

If the APK will run on a physical phone, replace it with the VM/server IP and make port 5000 reachable.

## 4. Manual APK build

Install JDK 17 and Android SDK.

From `android-app`:

```bash
gradle assembleDebug
```

APK:

```text
android-app/app/build/outputs/apk/debug/app-debug.apk
```

## 5. Build on Azure VM

Recommended VM: Ubuntu 24.04 LTS.

Install:

- Git
- JDK 17
- Android SDK command-line tools
- Gradle

Then:

```bash
git clone <your-github-repository>
cd dummy-mobile-ci-app/android-app
gradle assembleDebug
```

## 6. GitHub Actions

The repository contains:

`.github/workflows/android-apk.yml`

It builds the debug APK and uploads it as a GitHub Actions artifact.

For learning how to use an Azure VM as a self-hosted GitHub Actions runner, install the GitHub Actions runner on the VM and change the workflow:

```yaml
runs-on: self-hosted
```

The workflow can then execute the Android build directly on your Azure VM.

## Important

The backend and database are not packaged inside the APK.

The APK is the Android frontend. The backend and database run separately.

Typical production architecture:

Android APK -> Azure VM/App Service -> PostgreSQL
