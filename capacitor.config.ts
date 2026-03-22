import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.ourspace.app',
  appName: 'OurSpace',
  webDir: 'dist',
  server: {
    // Use HTTPS scheme — required for Firebase Auth, secure cookies,
    // and CORS to work correctly inside the Android WebView
    androidScheme: 'https',
  },
  plugins: {
    SplashScreen: {
      launchShowDuration: 2000,
      launchAutoHide: true,
      backgroundColor: "#ffffff",
      showSpinner: true,
      androidSpinnerStyle: "large",
      spinnerColor: "#f43f5e"
    },
    Keyboard: {
      // Resize the WebView when keyboard opens (fixes Notes input being hidden)
      resize: "body",
      resizeOnFullScreen: true
    }
  }
};

export default config;
