# Changelog
All notable changes to the Allsky Companion App will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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