# Changelog
All notable changes to the Allsky Companion App will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.7.6] - 2026-04-14
### Fixed
- **Compilation Error**: Removed lingering import statement for the removed database module that was preventing successful compilation of the previous release.

## [1.7.5] - 2026-04-14
### Changed
- **Removed Smart Caching**: Temporarily reverted the Room Database caching system to ensure media screens reliably load without displaying blank thumbnails on the initial launch.
- **Removed System Monitoring**: Temporarily disabled the System Monitoring module as the Raspberry Pi system page HTML structures vary too wildly across different Allsky versions to be reliably parsed at this time.

## [1.7.4] - 2026-04-14
### Fixed
- **Compilation Error**: Fixed an errant syntax error in the repository layer that caused the previous deployment build to fail.

## [1.7.3] - 2026-04-13
### Fixed
- **Smart Caching UX**: Resolved the issue where the app would briefly display "No content available" before content popped in. The app now correctly displays a loading spinner if the local database is empty while seamlessly fetching fresh network data in the background.
- **Fallback Thumbnails**: Fixed a bug where video files lacking a native thumbnail would display as a blank square instead of the designated fallback thumbnail in the Media screens.
- **System Monitoring**: Completed the implementation of the System Status module. The app now correctly parses and displays your Allsky Raspberry Pi's CPU Load, CPU Temperature, Disk Usage, Memory Used, and Uptime directly on the Main Screen.

## [1.7.2] - 2026-04-13
### Fixed
- **Build Stabilization**: Fixed a critical compiler error in the ViewModel layer related to the newly introduced System Info data mapping.

## [1.7.1] - 2026-04-13
### Fixed
- **Build Stabilization**: Addressed minor compiler errors related to the Room database integration to ensure successful release packaging.

## [1.7.0] - 2026-04-12
### Added
- **Smart Caching (Room Database)**: The app now uses a local SQLite database (Room) to cache your timelapses, startrails, and other media. This makes the Media Screens load instantly while it checks for updates in the background.
- **Dynamic Theming (Material You / Palette)**: The app's background and UI elements now intelligently extract colors from the live sky image and adapt their theme to match your current sky conditions in real-time.
- **System Monitoring Hub**: Added a new "System Monitoring" module to the Main Screen that scrapes your Allsky Raspberry Pi's health metrics (CPU Temp, Disk Usage, etc.) directly from the web interface.
- **Interactive Forecast Widget**: Expanded the Android home screen widget to display a sleek 3-day weather forecast directly beneath the live sky image.
- **Advanced Video Streaming**: Integrated Media3 ExoPlayer with HLS (`.m3u8`) support and optimized buffering parameters to ensure smooth, adaptive streaming of your high-resolution timelapses.

## [1.6.0] - 2026-04-12
### Added
- **Live View Home Screen Widget**: Re-engineered the Android home screen widget to accurately resolve the live image stream path and handle Basic Authentication credentials securely. Tapping the widget image now launches the app directly for a seamless experience.
- **Fully Integrated Weather API**: The OpenWeather API key is now completely integrated into the app. The manual API key input fields have been permanently removed from both the Setup and Settings screens, simplifying the onboarding process for new users.

## [1.5.5] - 2026-04-12
### Fixed
- **Fatal Rendering Crash on Startup**: Resolved a critical `ResourceResolutionException` caused by unsupported placeholder image formats (WebP/HTML masquerading as WebP). All custom thumbnails (timelapses, raw images, moon phase) have been rigorously verified and converted to clean `.jpg` formats to ensure guaranteed decoding via Jetpack Compose's `painterResource`, preventing the app from crashing after setup.

## [1.5.4] - 2026-04-12
### Fixed
- **Critical Startup Crash on Android 13+**: Resolved a hard crash that occurred immediately after entering logon details and latitude/longitude. This was caused by the app's background weather worker attempting to display a push notification without declaring or requesting the `POST_NOTIFICATIONS` permission required in newer Android versions. The permission is now correctly declared, and safety checks are in place to prevent the app from forcefully closing if permission hasn't been granted.
- **Autofill Tuning**: Further stabilized the keyboard input settings to maximize the chance of password managers like 1Password successfully detecting the login fields during setup.

## [1.5.3] - 2026-04-12
### Fixed
- **NavHost Crash**: Resolved a critical issue where the app would crash immediately after setup because the navigation start destination changed dynamically.
- **1Password Autofill**: Enhanced accessibility labels and semantic properties on username and password fields to ensure proper triggering of password managers like 1Password.

## [1.5.1] - 2026-04-11
### Added
- **Pull-to-Refresh**: Easily refresh content natively by swiping down on the Main and Media screens.
- **Smooth Animations**: Added beautiful crossfade and fade-in animations for loading states and live views to enhance the overall feel and align with Android design standards.

### Fixed
- **Critical Crash Fix**: Completely refactored data storage and synchronization logic to strictly utilize asynchronous Flow and Suspend architecture, permanently resolving the "crash on load" issue after entering credentials.
- **Performance**: Optimized Main Screen state observation.

## [1.5.0] - 2026-04-11
### Added
- **GPS Integration**: Capture station's Latitude and Longitude during setup using phone's GPS.
- **Enhanced Setup Experience**: Modern UI redesign for the initial setup screens.
- **High-Res Moon Phase**: High-resolution moon image with dynamic shadow mask.
- **Custom Placeholders**: New custom high-quality placeholders for Raw Images and Timelapses.
- **Built-in Weather API Key**: The default OpenWeather API key is now baked into the app.

### Fixed
- **Startup Stability**: Reverted to a proven synchronous preference loading system to eliminate "crash on load" issues.
- **Media Viewer Controls**: Fixed visibility and responsiveness of viewer buttons.

## [1.4.8] - 2026-04-11
### Fixed
- **App Stability**: Replaced blocking data calls with asynchronous operations to prevent deadlocks and crashes during app initialization.
- **Startup Crash**: Fixed a critical issue that caused the app to crash on load for some users.
- **Setup UX**: Refined autofill support for better compatibility across devices.

## [1.4.7] - 2026-04-11
### Added
- **New Timelapse Placeholder**: Replaced the generic play icon with the custom high-quality placeholder for all timelapses.

## [1.4.6] - 2026-04-11
### Added
- **Built-in Weather API Key**: The default OpenWeather API key is now baked into the app, so you don't have to enter it manually during setup.

### Fixed
- **Timelapse Placeholders**: Fixed an issue where timelapses were incorrectly using the raw image placeholder.

## [1.4.5] - 2026-04-11
### Added
- **GPS Integration**: Added ability to capture station's Latitude and Longitude during setup using the phone's GPS.
- **Enhanced Setup Experience**: Modern UI redesign for the initial setup screens with vertical gradients and improved clarity.
- **High-Res Moon Phase**: Replaced blurry moon emojis with a high-resolution moon image and a dynamic shadow mask for accurate phase visualization.
- **Custom Raw Image Placeholder**: Added a high-quality placeholder image for the Daily Raw Images section.

### Fixed
- **Media Viewer Controls**: Improved responsiveness and visibility of Download and Close (X) buttons in the image and video players.
- **Autofill Support**: Enhanced login fields with proper keyboard types and hints for password managers.
- **Moon Phase Calculation**: Improved the accuracy of the moon phase algorithm using Instant-based UTC time.

## [1.4.4] - 2026-04-09
### Added
- **Native Date Picker**: Replaced legacy text entry with a modern Material3 DatePickerDialog.
- **Themed Placeholders**: Added a dynamic vertical gradient for missing or loading media thumbnails.
- **Station-Centric Weather**: Forecasts now accurately use the Allsky station's coordinates instead of the device GPS.

### Fixed
- **Refined Media Filtering**: Tightened logic to accurately exclude system/generic files without breaking URL query strings or causing false positives.
- **Image Viewer Usability**: Enlarged the 'X' (Close) button tap area and improved dismissal gesture consistency.
- **Download Authentication**: Fixed the download functionality to ensure Basic Auth headers are correctly passed to Android's DownloadManager.
- **About Page Update**: Refreshed content, updated authorship, and added proper credit to the original concept creators.

## [1.4.3] - 2026-04-07
### Fixed
- Fixed a build failure caused by missing adaptive icon background resources.

## [1.4.2] - 2026-04-07
### Fixed
- **App Icon Overhaul**: Removed legacy vector assets that were preventing the new custom icon from appearing on modern Android devices.
- **Layout Persistence**: Improved the layout logic to ensure new modules like "Best Viewing Night" appear for all users, even if they had a previous layout saved.
- **Restore Defaults**: Added a "Restore Defaults" button to the Layout Editor.
- **Robust Media Fetching**: Added a realistic User-Agent and fixed nested URL authentication issues to ensure galleries populate correctly.
- **Full Screen Media**: Fixed a bug where clicking media in the full-screen gallery view wouldn't open the image/video player.

## [1.4.1] - 2026-04-07
### Fixed
- Fixed critical compilation errors in MainScreen, AboutScreen, SettingsScreen, and AllskyRepository.
- Improved Date handling and units in UI.

## [1.4.0] - 2026-04-07
### Added
- **New App Icon**: Completely updated the application identity with a fresh new icon.
- **Best Viewing Night Detection**: Added a smart logic to scan your weather forecast and pinpoint the upcoming night with the best astronomical viewing conditions (lowest clouds and no precipitation).
- **Station-Specific Weather**: Removed phone location dependency. You can now set the exact Latitude and Longitude of your camera station in Settings for pinpoint accurate forecasts.
- **Download Media**: You can now download images and videos directly to your device from the full-screen viewers.
- **Calendar Date Picker**: Replaced manual date typing with an intuitive Android calendar picker in the media galleries.

### Fixed
- **Media Cleanup**: Automatically hiding system files like `allsky-logo.jpg` and `image.jpg` from the galleries.
- **Media Placeholders**: Added dark background placeholders for video and image thumbnails to prevent layout flickering.
- **About Page Redesign**: Updated author information and credits with a fresh look.

## [1.3.5] - 2026-04-06
### Fixed
- **URL Auto-Correct Bug**: Removed an overzealous auto-correct feature that was silently appending `/allsky` to users' base URLs and saving it to preferences. This caused the app to look in the wrong directory, completely breaking media discovery for many setups.

## [1.3.4] - 2026-04-06
### Fixed
- **Authentication Error Visibility**: Fixed an issue where "No content available" would be displayed if the Allsky Portal was protected by Basic Authentication but credentials were not entered in the app settings. The app now explicitly throws a 401 Unauthorized error and displays a clear message to the user prompting them to enter their Username and Password.

## [1.3.3] - 2026-04-06
### Fixed
- Fixed an issue where media would not display if the web server incorrectly returned a "200 OK" placeholder page for missing subdirectories (e.g., `/videos/`) instead of a 404 error. The app now explicitly requests the portal page first and checks for valid media tags before falling back.
- When selecting a specific date in the "Images" tab, the app now correctly queries the portal directly for that date instead of just listing available days.

## [1.3.2] - 2026-04-06
### Added
- **Customizable Main Screen Layout**: Added a new Layout Editor accessible from the side menu, allowing users to dynamically reorder and toggle modules (Live View, Weather, Timelapses, etc.).
- **Media Date Picker**: Media modules (Timelapses, Keograms, Startrails, Meteors, Images) now have dedicated screens accessible via the Side Menu. Each includes a Date Picker to fetch historical data for a specific day.
- **Dynamic Weather Background**: The Main Screen background now dynamically changes color based on the current weather conditions (e.g., Dark Blue/Purple for Clear, Slate Blue for Rain, Dark Gray for Clouds).
- **Navigation Drawer Menu**: Upgraded the app's navigation system. The settings panel has been replaced by a proper side menu, and all URL/API configurations have been moved to a clean, dedicated "Settings" screen.

## [1.2.2] - 2026-04-06
### Fixed
- Fixed "No Content Available" error by adding the required `&day=All` parameter when fetching portal media.
- Fixed historic image discovery by parsing thumbnail `<img>` tags (since the portal does not use standard links for raw images).
- Cleaned up `/thumbnails` paths when retrieving full-resolution image links.

## [1.2.1] - 2026-04-06
### Fixed
- Robust media discovery for Portal-style URLs (`index.php?page=list_...`).
- Improved support for daily image listings by parsing date-specific portal pages.
- Enhanced path normalization to handle various Allsky URL configurations.

## [1.2.0] - 2026-04-06
### Added
- New "Meteor Recordings" section to view captured meteor events.
- Support for "Historic" daily image archives by correctly parsing daily subdirectories.

### Fixed
- Major overhaul of media discovery to be more robust across different Allsky Portal versions.
- Fixed path resolution issues using absolute URL logic.
- Improved date extraction from filenames and directory names.
- Better handling of relative and root-relative links in directory listings.

## [1.1.9] - 2026-04-06
### Fixed
- Further improved media parsing for various Allsky directory listing styles.
- Fixed issue with relative paths and parent directory links in file lists.
- Support for `.mov` and `.mkv` timelapse formats.
- More robust date extraction from filenames.

## [1.1.8] - 2026-04-06
### Added
- Bold typographic redesign for a more modern "heroic" aesthetic.
- Enhanced Moon Phase display with improved layout and illumination details.
- Uppercase headers and increased letter spacing for a premium feel.

### Fixed
- Robust media parsing for Allsky installations, ensuring timelapses, keograms, and startrails are correctly identified even with non-standard directory listings.
- Improved date extraction from filenames when metadata is missing.

## [1.1.7] - 2026-04-05
### Fixed
- Added detection for live images located at `/current/tmp/image.jpg` (common in local Allsky installations).

## [1.1.6] - 2026-04-05
### Fixed
- Fixed issue where the app failed to load media or live view if the base URL did not contain the `/allsky` path.

## [1.1.5] - 2026-04-05
### Added
- Basic Authentication support for Allsky installations behind proxies.
- Night viewing conditions forecast in the app and Push Notifications.

### Fixed
- Live image loading errors caused by trailing slashes in the Allsky URL.

## [1.1.4] - 2024-11-18
### Fixed
- Update dialog "Later" button now properly closes dialog
- Update status in settings panel now correctly shows available updates
- Added ability to reopen update dialog by clicking status in settings
- Improved update state handling and dialog visibility
- Better user experience for update notifications

## [1.1.3] - 2024-11-17
### Added
- Home screen widget for live Allsky image
  - Manual refresh functionality
  - Last update timestamp display
  - Error state handling
  - Loading state indication
  - Configurable size

### Fixed
- Update dialog "Later" button now properly closes dialog
- Settings panel now correctly shows update status
- Added version number to update available message
- Improved update state handling in settings

## [1.1.2] - 2024-11-16
### Fixed
- Fixed moon phase calculation accuracy
- Improved moon phase illumination calculation
- Updated moon phase boundaries for better precision
- Added illumination percentage display to moon phase card

### Changed
- Upgraded to Media3 for video playback
- Replaced deprecated ExoPlayer components
- Enhanced video player stability

## [1.1.1] - 2024-11-15
### Improved
- Enhanced error handling in weather data fetching
- More robust URL validation for Allsky server connection
- Better error messages for users when API key is missing
- Improved stability of live image updates
- Added debug logging for better troubleshooting
- Enhanced error handling in media gallery parsing

### Fixed
- Proper error handling for invalid Allsky URLs
- Better handling of missing weather API keys
- Improved error state management in LiveImageViewModel
- More robust parsing of media gallery items

## [1.1.0] - 2024-11-15
### Added
- Multi-language support
  - German (de) translation
  - Spanish (es) translation
  - French (fr) translation
  - Italian (it) translation
- Automatic language selection based on system settings
- RTL support with AutoMirrored icons
- Modernized About screen with interactive components
- Component links with license information

### Changed
- Moved all hardcoded strings to resource files
- Updated UI components to use string resources
- Improved accessibility with proper content descriptions
- Enhanced About screen layout with Material 3 cards
- Restructured string resources with clear categorization

### Fixed
- Language-specific date and time formats
- Proper handling of system language changes
- RTL layout issues in navigation icons

## [1.0.0] - 2024-11-14
### Added
- Initial release
- Live image view with 30-second refresh
- Moon phase display with illumination percentage
- Weather forecast integration with OpenWeather API
- Keogram gallery with full-screen viewer
- Startrail gallery with full-screen viewer
- Timelapse video gallery
- Setup wizard for first launch
- Settings panel for URL and API key configuration
- About page with library attributions
- Dark/Light theme support
- Dynamic color support for Android 12+
- Location-based weather data
- Automatic content refresh on URL change
- Support for Android 10 and above

### Technical Features
- MVVM architecture with unidirectional data flow
- Coroutines for asynchronous operations
- StateFlow for state management
- Jetpack Compose UI
- Material 3 design system
- Repository pattern for data access
- HTML parsing for media galleries
- Lifecycle-aware components
- DataStore for preferences
- GPS location services