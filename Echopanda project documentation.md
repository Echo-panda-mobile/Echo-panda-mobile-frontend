# ECHO PANDA — Project Documentation

**Mobile Music Streaming Application**
*Android (Kotlin) • Jetpack Compose • Firebase • REST API*
*June 2026*

---

## Table of Contents

1. [Problem Statement](#1-problem-statement)
2. [Features List](#2-features-list)
3. [User Stories](#3-user-stories)
4. [Backend Integration](#4-backend-integration)

---

## 1. Problem Statement

### 1.1 Overview

The music industry faces a significant challenge: talented independent artists struggle to reach their audience, while music listeners have limited access to discovering new, emerging artists from their local community. Existing major streaming platforms are dominated by mainstream content, leaving independent and local artists largely invisible.

### 1.2 Core Problem

Echo Panda addresses three interconnected problems:

- **For Listeners:** Users lack a dedicated platform that surfaces independent and regional artists alongside professionally curated music discovery tools such as genre browsing, tag-based discovery, and mood playlists. Existing platforms do not support multi-role experiences (regular users, artists, and administrators) within a single unified mobile application.

- **For Artists:** Independent artists have no streamlined mobile tool to manage their discography, upload songs, create albums, track analytics, receive fan comments, and manage notifications — all in one place, with direct backend connectivity.

- **For Platform Administrators:** There is no mobile-first admin panel that allows content moderation, user management, song and album review, and artist onboarding, making platform governance difficult on the go.

### 1.3 Purpose of the App

Echo Panda is a role-based Android music streaming application built to solve these challenges by providing:

- A rich listening experience for regular users — with a home feed, music discovery, album browsing, playlists, favorites, and a full-featured player.
- A creator hub for artists — to upload songs, manage albums, view analytics, respond to comments, and control their profile.
- An admin panel for platform operators — to manage users, review music content, moderate tags, categories, and albums from a mobile interface.

> *Echo Panda is not just a music player — it is a complete music ecosystem connecting listeners, creators, and platform operators in one role-aware Android application.*

---

## 2. Features List

### 2.1 Authentication & Onboarding

- Email and password login with JWT token-based session management
- Google Sign-In via Firebase Authentication (OAuth 2.0)
- New user registration (Sign Up) with role assignment
- Forgot Password flow with email-based OTP verification screen
- Intro / onboarding screens for first-time users
- Token refresh and secure token storage using Android DataStore

### 2.2 User (Listener) Features

#### Home Screen
- Personalized home feed with popular artists, featured albums, and recent listens
- Pull-to-refresh for live content updates
- Mini bar player docked at bottom for continuous playback while browsing

#### Discover Screen
- Browse music by genre, tags, and mood playlists
- Voice search using Android speech recognition
- Search artists and songs with real-time filtering
- View all songs, all artists, and explore genre-specific catalogs

#### Album & Artist Screen
- Browse all albums with cover art and artist info
- Album detail page with full track listing and play controls
- Artist detail page with biography, monthly listeners, and discography
- Follow/unfollow artists, view artist comments

#### Player Screen
- Full-screen music player with album art, progress bar, seek control
- Play, pause, skip (next/previous), shuffle, and repeat controls
- Add currently playing song to a playlist from the player
- Share song feature
- Favorite / unfavorite a song directly from the player
- Resume playback from last position

#### Library & Favorites
- Personal library showing playlists and recently played
- Create and manage custom playlists
- Playlist detail screen with song listing
- Dedicated Favorites screen for liked songs

#### Profile & Settings
- User profile screen with name, avatar, and account details
- Edit profile information and upload a profile photo
- Settings screen with language preference (app-wide locale switching)

### 2.3 Artist Features

- Artist Dashboard with overview of uploads and activity
- Upload songs with audio file upload, metadata, and cover art (S3 pre-signed URL)
- Create and manage albums with song assignments
- Edit existing song metadata (title, cover image)
- View and manage all uploaded music (Artist Music Screen)
- View and respond to fan comments
- Artist-specific player for previewing own tracks
- View analytics (play counts, listener stats)
- Notifications screen for platform alerts
- Edit artist profile (name, bio, cover image)
- Preferences, security, and help/support screens

### 2.4 Admin Features

- Admin Dashboard with platform-wide statistics
- User management: view, filter, and manage all registered users
- User detail page with role management and the ability to add artists
- Music management: browse all songs with song detail and moderation controls
- Album detail moderation screen
- Library management: browse genres, tags, and categories
- Tag detail and tag-based album browsing
- Category detail and category-based album browsing
- Admin profile screen

### 2.5 Cross-Cutting / Technical Features

- Role-based navigation: separate nav graphs for User, Artist, and Admin roles
- Global music player (ExoPlayer via Media3) persists across screen navigation
- Audio metadata extraction from uploaded files
- Image compression before upload to optimize bandwidth
- AWS S3 file upload with pre-signed URLs and real-time upload progress tracking
- FCM (Firebase Cloud Messaging) push notifications via custom messaging service
- HTTP interceptor for automatic token injection and token refresh on 401 responses
- Dark-themed design system with a consistent Echo Panda brand identity

---

## 3. User Stories

The following user stories capture the functional requirements of Echo Panda from the perspective of each role.

**Format:** *As a [user], I want to [do something] so that [reason].*

| As a... | I want to... | So that... |
|---|---|---|
| **Regular User** | log in with my Google account | I can access the app quickly without creating a separate password |
| **Regular User** | browse songs by genre and mood | I can discover music that fits my taste or current mood |
| **Regular User** | play a song from an album | I can listen to my favorite tracks with full playback controls |
| **Regular User** | favorite a song | I can easily find it again in my favorites list later |
| **Regular User** | add a song to a playlist | I can organize music I love into collections |
| **Regular User** | use voice search to find songs | I can find music hands-free without typing |
| **Regular User** | resume a song where I left off | I do not have to seek back to my position manually |
| **Regular User** | view an artist's profile and albums | I can explore all content from artists I like |
| **Regular User** | update my profile photo and name | I can personalize my account |
| **Regular User** | switch the app language | I can use the app in my preferred language |
| **Artist** | upload a new song with cover art | I can share my music with listeners on the platform |
| **Artist** | create an album and assign songs to it | I can organize my releases in a structured way |
| **Artist** | edit my song's title and cover image | I can correct mistakes or update my content |
| **Artist** | view listener analytics for my songs | I can understand how my music is performing |
| **Artist** | receive and view fan comments | I can stay engaged with my audience |
| **Artist** | edit my artist bio and profile image | My profile stays current and professional |
| **Artist** | preview my own uploaded tracks in the player | I can verify how my songs sound before publishing |
| **Admin** | view all registered users and their roles | I can manage the platform community |
| **Admin** | promote a user to an artist role | I can onboard new artists to the platform |
| **Admin** | review and moderate uploaded songs | I can ensure content quality and policy compliance |
| **Admin** | manage music categories and tags | I can keep the content taxonomy organized and relevant |
| **Admin** | view album details and take moderation action | I can remove inappropriate releases from the platform |
| **New User** | go through onboarding screens | I understand what Echo Panda offers before I start |
| **Returning User** | reset my password via email OTP | I can regain access to my account if I forget my password |

---

## 4. Backend Integration

### 4.1 Architecture Overview

Echo Panda follows a client-server architecture. The Android frontend communicates with a custom backend REST API via Retrofit2/OkHttp3. Firebase is used for authentication (Google Sign-In), and AWS S3 handles media file storage. Firebase Cloud Messaging delivers push notifications to artist devices.

> *The app uses a `BuildConfig.API_BASE_URL` environment variable to configure the backend URL at build time, supporting both development and production environments.*

### 4.2 Backend & Third-Party Services

| Component | Technology / Service | Purpose |
|---|---|---|
| **Primary API** | Custom REST Backend (Node.js / PHP / Python) | Main application backend exposing all API endpoints under `/api/*` |
| **HTTP Client** | Retrofit2 + OkHttp3 | Type-safe HTTP client for all REST API calls from the Android app |
| **Authentication** | Firebase Authentication | Google Sign-In (OAuth 2.0), ID token generation, and user identity |
| **Session / JWT** | Custom JWT tokens (Bearer) | Backend issues JWT on login; token stored securely in DataStore |
| **Token Refresh** | OkHttp Authenticator (`AuthAuthenticator.kt`) | Automatically refreshes expired JWT tokens on 401 responses |
| **File Storage** | AWS S3 (Pre-signed URLs) | Audio files and images uploaded directly to S3 via pre-signed URLs from backend |
| **Push Notifications** | Firebase Cloud Messaging (FCM) | Real-time push notifications for artists (`EchoPandaMessagingService.kt`) |
| **Local Storage** | Android DataStore (Preferences) | Secure local storage of auth tokens and user settings |
| **Media Playback** | ExoPlayer (AndroidX Media3) | Audio streaming and playback with session management (`MusicPlayerManager.kt`) |
| **Image Loading** | Coil (AsyncImage) | Async image loading with caching for album art and user avatars |

### 4.3 API Services

The app exposes three Retrofit interface services:

**`AuthApiService`**
Handles login, registration, logout, Firebase session, profile get/update, and pre-signed image URL for user avatar.
Endpoints: `/api/login`, `/api/me`, `/api/profile`, `/api/firebase/session`, `/api/logout`

**`MusicApiService`**
Handles all music-related operations: artists, albums, songs (CRUD), genres, tags, categories, favorites, playlists, stream tickets, listen history, and artist analytics.
Endpoints under: `/api/artists`, `/api/albums`, `/api/songs`, `/api/playlists`, `/api/favorites`, etc.

**`AdminApiService`**
Admin-only endpoints for user management, content moderation, artist promotion, and dashboard statistics.
Endpoints under: `/api/admin/*`

### 4.4 File Upload Flow (AWS S3)

Audio and image uploads follow a two-step pre-signed URL pattern:

1. **Step 1** — The app requests a pre-signed upload URL from the backend (`POST /api/upload/...presign`) with the filename, content type, and file size.
2. **Step 2** — The app uploads the file directly to AWS S3 using the pre-signed URL via `S3UploadManager.kt`, with real-time progress tracking reported via a callback (0–100%).
3. **Step 3** — After a successful S3 upload, the app notifies the backend with the final S3 URL to associate it with the song or profile record.

### 4.5 Authentication Flow

The authentication system supports two sign-in methods:

- **Email / Password Login:** Credentials are sent to `/api/login`. The backend returns a JWT token stored in DataStore via `TokenStorage.kt`.
- **Google Sign-In:** The app uses `FirebaseAuthManager.kt` to complete Google OAuth via Firebase. The resulting Firebase ID token is sent to `/api/firebase/session`, which creates or retrieves the user and returns a backend JWT.
- **Token Refresh:** `AuthAuthenticator.kt` (OkHttp Authenticator) intercepts 401 Unauthorized responses and automatically requests a new token before retrying the original request.
- **Logout:** Calls `/api/logout` and clears all local tokens and cookies.

### 4.6 Role-Based Access Control

After authentication, the backend returns the user's role (`USER`, `ARTIST`, or `ADMIN`). The app uses this role to:

- Navigate to the correct nav graph (`UserNavGraph`, `ArtistNavGraph`, or `AdminNavGraph`).
- Enforce security in `Routes.resolveRoute()`: non-admin users attempting to navigate to admin routes are silently redirected to their own home screen.
- Display role-specific UI components and bottom navigation bars.

---
