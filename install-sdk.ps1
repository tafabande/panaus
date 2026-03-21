$ErrorActionPreference = "Stop"
$sdkDir = "$env:LOCALAPPDATA\Android\Sdk"
$toolsDir = "$sdkDir\cmdline-tools\latest"

if (!(Test-Path $toolsDir)) {
    Write-Host "Creating Directories..."
    New-Item -ItemType Directory -Force -Path "$sdkDir\cmdline-tools" | Out-Null
}

Write-Host "Downloading Android SDK Command-line Tools..."
Invoke-WebRequest -Uri "https://dl.google.com/android/repository/commandlinetools-win-10406996_latest.zip" -OutFile "sdk.zip"

Write-Host "Extracting ZIP..."
Expand-Archive -Path "sdk.zip" -DestinationPath "$sdkDir\cmdline-tools\temp" -Force

Write-Host "Moving files..."
Move-Item -Path "$sdkDir\cmdline-tools\temp\cmdline-tools\*" -Destination $toolsDir -Force
Remove-Item "$sdkDir\cmdline-tools\temp" -Recurse -Force
Remove-Item "sdk.zip" -Force

Write-Host "Setting Environment Variable..."
[Environment]::SetEnvironmentVariable("ANDROID_HOME", $sdkDir, "User")
$env:ANDROID_HOME = $sdkDir

Write-Host "Installing SDK Packages..."
$sdkManager = "$toolsDir\bin\sdkmanager.bat"

echo "y" | & $sdkManager "platforms;android-34" "build-tools;34.0.0" "platform-tools"
echo "y" | & $sdkManager --licenses

Write-Host "SDK Installation Complete! Building APK..."
Set-Location -Path "c:\Users\User\Desktop\us\android"
& .\gradlew.bat assembleDebug

Write-Host "APK Built Successfully"
