# Panaus Android App
Native Jetpack Compose application optimized for performance and stability.

## Key Optimizations
- **Startup Performance**: Firebase initialization moved to background threads to eliminate UI thread freezes.
- **Build Optimization**: R8 Full Mode enabled with custom ProGuard rules to prevent lock verification issues.
- **Error Handling**: Integrated `UiFreezeDetector` and `GlobalErrorHandler` for robust monitoring.

## GitHub Actions & Secrets
The CI/CD pipeline in `.github/workflows/android.yml` handles automated builds and releases.

### Required GitHub Secrets
To ensure successful builds, update your GitHub Repository Secrets with:
1. `GOOGLE_SERVICES_JSON`: The full content of your `app/google-services.json`.
2. `FIREBASE_SERVER_KEY`: From Firebase Console settings.
3. `ANALYTICS_PROPERTY_ID`: If using Google Analytics reporting.
4. `PREMIUM_SUBSCRIPTION_SKU`: The SKU ID for in-app purchases.

## Local Development
1. Open in Android Studio.
2. Build with Gradle to sync dependencies.
3. Use `./gradlew assembleDebug` for local testing.
