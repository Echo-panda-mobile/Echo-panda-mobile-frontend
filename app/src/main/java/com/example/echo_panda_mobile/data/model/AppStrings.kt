package com.example.echo_panda_mobile.data.model

/**
 * Localized strings for the app
 */
object AppStrings {

    private val englishStrings = mapOf(
        // Navigation
        "settings" to "Settings",
        "my_profile" to "My Profile",
        "profile" to "Profile",
        "artist_profile" to "Artist Profile",
        "home" to "Home",
        "discover" to "Discover",
        "albums" to "Albums",
        "library" to "Library",
        "liked_songs" to "Liked Songs",
        "playlists" to "Playlists",
        "favorites" to "Favorites",

        // Settings
        "dark_mode" to "Dark Mode",
        "dark_mode_desc" to "Enable dark theme",
        "light_mode" to "Light Mode",
        "light_mode_desc" to "Enable light theme",
        "language" to "Language",
        "english" to "English",
        "khmer" to "ខ្មែរ",
        "notifications" to "Notifications",
        "notifications_desc" to "Receive push notifications",
        "audio_quality" to "Audio Quality",
        "audio_quality_desc" to "Currently: High",
        "explicit_content" to "Explicit Content",
        "explicit_content_desc" to "Allow explicit tracks",
        "two_factor_auth" to "Two-Factor Authentication",
        "two_factor_auth_desc" to "Secure your account",
        "privacy_policy" to "Privacy Policy",
        "privacy_policy_desc" to "View our policy",
        "version" to "Version",
        "version_desc" to "App version 1.0.0",
        "about" to "About",
        "account" to "Account",
        "audio" to "Audio",
        "display" to "Display",
        "privacy_security" to "Privacy & Security",
        "private_account" to "Private Account",
        "private_account_desc" to "Make your profile private",

        // Auth
        "login" to "Login",
        "logout" to "Log Out",
        "logout_confirm" to "Are you sure you want to log out of Echo Panda?",
        "sign_up" to "Sign Up",
        "password" to "Password",
        "email" to "Email",
        "name" to "Name",
        "welcome" to "Welcome back",

        // Profile
        "save_changes" to "Save Changes",
        "upload_photo" to "Upload Photo",
        "change_photo" to "Change Photo",
        "clear_photo" to "Clear Photo",
        "profile_updated" to "Profile updated successfully",
        "edit_profile" to "Edit Profile",
        "followers" to "Followers",
        "following" to "Following",
        "your_playlists" to "Your Playlists",
        "no_playlists" to "No Playlists Yet",
        "no_playlists_desc" to "Start creating your own music world!",

        // Home
        "continue_listening" to "Continue Listening",
        "popular_artists" to "Popular Artists",
        "top_albums" to "Top Albums",
        "based_on_recent" to "Based on your recent listening",

        // Discover
        "music_genres" to "Music Genres",
        "mood_playlist" to "Mood Playlist",
        "new_release_songs" to "New Release Songs",

        // Messages
        "loading" to "Loading...",
        "error" to "Error",
        "success" to "Success",
        "cancel" to "Cancel",
        "save" to "Save",
        "delete" to "Delete",
        "edit" to "Edit",
        "back" to "Back",
        "next" to "Next",
        "done" to "Done",
        "close" to "Close",
        "view_all" to "View All"
    )

    private val khmerStrings = mapOf(
        // Navigation
        "settings" to "ការកំណត់",
        "my_profile" to "ប្រវត្តិរូបរបស់ខ្ញុំ",
        "profile" to "ប្រវត្តិរូប",
        "artist_profile" to "ប្រវត្តិរូបសិល្បករ",
        "home" to "ដើម",
        "discover" to "ស្វែងរក",
        "albums" to "អាល់ប៊ុម",
        "library" to "បណ្ណាល័យ",
        "liked_songs" to "បទដែលចូលចិត្ត",
        "playlists" to "បញ្ជីចម្រៀង",
        "favorites" to "សំណព្វចិត្ត",

        // Settings
        "dark_mode" to "របៀបងងឹត",
        "dark_mode_desc" to "បង្ហាញកម្មវិធីងងឹត",
        "light_mode" to "របៀបពន្លឺ",
        "light_mode_desc" to "បង្ហាញកម្មវិធីពន្លឺ",
        "language" to "ភាសា",
        "english" to "English",
        "khmer" to "ខ្មែរ",
        "notifications" to "ការជូនដំណឹង",
        "notifications_desc" to "ទទួលការជូនដំណឹង",
        "audio_quality" to "គុណភាពសម្លេង",
        "audio_quality_desc" to "បច្ចុប្បន្ន: ខ្ពស់",
        "explicit_content" to "ខ្លឹមសារច្បាស់លាស់",
        "explicit_content_desc" to "អនុញ្ញាតឱ្យបទលម្អិត",
        "two_factor_auth" to "ការផ្ទៀងផ្ទាត់ពីរកត្តា",
        "two_factor_auth_desc" to "សុវត្ថិភាពគណនីរបស់អ្នក",
        "privacy_policy" to "គោលការណ៍ឯកជនភាព",
        "privacy_policy_desc" to "មើលគោលការណ៍របស់យើង",
        "version" to "កំណែ",
        "version_desc" to "កម្មវិធីកំណែ 1.0.0",
        "about" to "អំពី",
        "account" to "គណនី",
        "audio" to "សម្លេង",
        "display" to "បង្ហាញ",
        "privacy_security" to "ឯកជនភាព និង សុវត្ថិភាព",
        "private_account" to "គណនីឯកជន",
        "private_account_desc" to "ធ្វើឱ្យប្រវត្តិរូបរបស់អ្នកឯកជន",

        // Auth
        "login" to "ចូល",
        "logout" to "ចាកចេញ",
        "logout_confirm" to "តើអ្នកប្រាកដថាចង់ចាកចេញពី Echo Panda មែនទេ?",
        "sign_up" to "ចុះឈ្មោះ",
        "password" to "ពាក្យសម្ងាត់",
        "email" to "អ៊ីមែល",
        "name" to "ឈ្មោះ",
        "welcome" to "សូមស្វាគមន៍",

        // Profile
        "save_changes" to "រក្សាទុកការផ្លាស់ប្តូរ",
        "upload_photo" to "ផ្ទុករូបថត",
        "change_photo" to "ផ្លាស់ប្តូររូបថត",
        "clear_photo" to "ដករូបថតចេញ",
        "profile_updated" to "ប្រវត្តិរូបត្រូវបានធ្វើបច្ចុប្បន្នភាព",
        "edit_profile" to "កែសម្រួលប្រវត្តិរូប",
        "followers" to "អ្នកតាមដាន",
        "following" to "កំពុងតាមដាន",
        "your_playlists" to "បញ្ជីចម្រៀងរបស់អ្នក",
        "no_playlists" to "មិនទាន់មានបញ្ជីចម្រៀង",
        "no_playlists_desc" to "ចាប់ផ្តើមបង្កើតពិភពតន្ត្រីផ្ទាល់ខ្លួនរបស់អ្នក!",

        // Home
        "continue_listening" to "បន្តស្ដាប់",
        "popular_artists" to "សិល្បករពេញនិយម",
        "top_albums" to "អាល់ប៊ុមល្បីៗ",
        "based_on_recent" to "ផ្អែកលើការស្តាប់ថ្មីៗរបស់អ្នក",

        // Discover
        "music_genres" to "ប្រភេទតន្ត្រី",
        "mood_playlist" to "បញ្ជីចម្រៀងតាមអារម្មណ៍",
        "new_release_songs" to "បទចម្រៀងចេញថ្មី",

        // Messages
        "loading" to "កំពុងផ្ទុក...",
        "error" to "កំហុស",
        "success" to "ជោគជ័យ",
        "cancel" to "បោះបង់",
        "save" to "រក្សាទុក",
        "delete" to "លុប",
        "edit" to "កែសម្រួល",
        "back" to "ត្រឡប់ក្រោយ",
        "next" to "បន្ទាប់",
        "done" to "រួចរាល់",
        "close" to "បិទ",
        "view_all" to "មើលទាំងអស់"
    )

    fun getString(key: String, language: String = "en"): String {
        return when (language) {
            "km" -> khmerStrings[key] ?: englishStrings[key] ?: key
            else -> englishStrings[key] ?: key
        }
    }

    fun getAllStrings(language: String = "en"): Map<String, String> {
        return when (language) {
            "km" -> khmerStrings
            else -> englishStrings
        }
    }
}
