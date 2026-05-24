package com.example.echo_panda_mobile.data.repository

import androidx.compose.ui.graphics.Color
import com.example.echo_panda_mobile.data.model.*
import kotlinx.coroutines.delay

// ─── Result wrapper (reuse pattern from AuthRepository) ───────────────────────
sealed class MusicResult<out T> {
    data class Success<T>(val data: T) : MusicResult<T>()
    data class Error(val message: String) : MusicResult<Nothing>()
    object Loading : MusicResult<Nothing>()
}

class MusicRepository {

    // ── Fake data — replace with Retrofit calls when API is ready ─────────────

    suspend fun getRecentPlaylists(): MusicResult<List<Playlist>> {
        delay(500)
        return MusicResult.Success(
            listOf(
                Playlist("1", "Coffee & Jazz",  placeholderColors = listOf(Color(0xFF3A2A1A), Color(0xFF1A1208))),
                Playlist("2", "New Songs",      labelOverlay = "TOP\nNew Songs", placeholderColors = listOf(Color(0xFF1E3A2F), Color(0xFF0D2019))),
                Playlist("3", "RELEASED",       placeholderColors = listOf(Color(0xFF3A1E2F), Color(0xFF20090F))),
                Playlist("4", "Anything Goes",  placeholderColors = listOf(Color(0xFF1E2A3A), Color(0xFF091520))),
                Playlist("5", "Anime OSTs",     placeholderColors = listOf(Color(0xFF3A2A1E), Color(0xFF201508))),
                Playlist("6", "Harry's House",  placeholderColors = listOf(Color(0xFF2A1E3A), Color(0xFF120920))),
                Playlist("7", "Lo-Fi Beats",    placeholderColors = listOf(Color(0xFF2C2C3A), Color(0xFF1A1A26))),
            )
        )
    }

    suspend fun getPopularArtists(): MusicResult<List<Artist>> {
        delay(500)
        return MusicResult.Success(
            listOf(
                Artist("1", "Eminiem",     monthlyListeners = "65.4M", placeholderColors = listOf(Color(0xFF1A1A2A), Color(0xFF0A0A12))),
                Artist("2", "Lana Del Ray",monthlyListeners = "55.1M", placeholderColors = listOf(Color(0xFF2A1A1A), Color(0xFF120A0A))),
                Artist("3", "Adele",       monthlyListeners = "48.2M", placeholderColors = listOf(Color(0xFF3A2A1A), Color(0xFF1A1208))),
                Artist("4", "Harry Styles",monthlyListeners = "42.9M", placeholderColors = listOf(Color(0xFF1A2A2A), Color(0xFF0A1212))),
                Artist("5", "Drake",       monthlyListeners = "78.1M", placeholderColors = listOf(Color(0xFF2A2A1A), Color(0xFF12120A))),
                Artist("6", "Imagine Dr.", monthlyListeners = "35.6M", placeholderColors = listOf(Color(0xFF1A2A1A), Color(0xFF0A120A))),
            )
        )
    }

    suspend fun getTopAlbums(): MusicResult<List<Album>> {
        delay(500)
        return MusicResult.Success(
            listOf(
                Album("1", "Adele 21",   "Adele",      placeholderColors = listOf(Color(0xFF3A2A1A), Color(0xFF1A1208))),
                Album("2", "Scorpion",   "Drake",      placeholderColors = listOf(Color(0xFF1A1A2A), Color(0xFF0A0A12))),
                Album("3", "Born To Die","Lana Del Ray",placeholderColors = listOf(Color(0xFF2A1A1A), Color(0xFF120A0A))),
                Album("4", "After Hours","The Weeknd", placeholderColors = listOf(Color(0xFF3A1A1A), Color(0xFF1A0808))),
            )
        )
    }

    suspend fun getNewAlbums(): MusicResult<List<Album>> {
        delay(400)
        return MusicResult.Success(
            listOf(
                Album("5", "Harry's House", "Harry Styles", placeholderColors = listOf(Color(0xFF2A1E3A), Color(0xFF120920))),
                Album("6", "Justice",       "Justin Bieber",placeholderColors = listOf(Color(0xFF1E3A2A), Color(0xFF0D2019))),
                Album("7", "Future Nostalgia","Dua Lipa",   placeholderColors = listOf(Color(0xFF3A1E2F), Color(0xFF20090F))),
                Album("8", "Certified Lover Boy", "Drake",  placeholderColors = listOf(Color(0xFF1E2A3A), Color(0xFF091520))),
            )
        )
    }

    suspend fun getPopularAlbums(): MusicResult<List<Album>> {
        delay(400)
        return MusicResult.Success(
            listOf(
                Album("9", "Midnights",     "Taylor Swift", placeholderColors = listOf(Color(0xFF1A1A2A), Color(0xFF0A0A12))),
                Album("10", "SOS",          "SZA",          placeholderColors = listOf(Color(0xFF2A1A1A), Color(0xFF120A0A))),
                Album("11", "Un Verano Sin Ti", "Bad Bunny", placeholderColors = listOf(Color(0xFF3A2A1A), Color(0xFF1A1208))),
                Album("12", "Renaissance",  "Beyoncé",      placeholderColors = listOf(Color(0xFF1A2A2A), Color(0xFF0A1212))),
            )
        )
    }

    suspend fun getAlbumById(id: String): MusicResult<Album> {
        delay(500)
        // Find in our mock lists or create a detailed one
        val allMockAlbums = listOf(
            Album("1", "Adele 21",   "Adele",      placeholderColors = listOf(Color(0xFF3A2A1A), Color(0xFF1A1208))),
            Album("2", "Scorpion",   "Drake",      placeholderColors = listOf(Color(0xFF1A1A2A), Color(0xFF0A0A12))),
            Album("4", "After Hours","The Weeknd", placeholderColors = listOf(Color(0xFF3A1A1A), Color(0xFF1A0808))),
            Album("13", "The Eminem Show", "Eminem", placeholderColors = listOf(Color(0xFF3A0000), Color(0xFF1A0000)))
        )
        
        val base = allMockAlbums.find { it.id == id } ?: allMockAlbums.last()
        
        val mockTracks = listOf(
            Track("t1", "White America", base.artist, durationMs = 324000, placeholderColors = base.placeholderColors),
            Track("t2", "Business", base.artist, durationMs = 251000, placeholderColors = base.placeholderColors),
            Track("t3", "Cleaning' Out My Closet", base.artist, durationMs = 297000, placeholderColors = base.placeholderColors),
            Track("t4", "Square Dance", base.artist, durationMs = 323000, placeholderColors = base.placeholderColors),
            Track("t5", "When The Music Stops", base.artist, durationMs = 269000, placeholderColors = base.placeholderColors),
            Track("t6", "Soldier", base.artist, durationMs = 226000, placeholderColors = base.placeholderColors),
            Track("t7", "Say Good Bye Hollywood", base.artist, durationMs = 272000, placeholderColors = base.placeholderColors),
            Track("t8", "Drips", base.artist, durationMs = 285000, placeholderColors = base.placeholderColors),
            Track("t9", "Without Me", base.artist, durationMs = 290000, placeholderColors = base.placeholderColors),
            Track("t10", "Sing For The Moment", base.artist, durationMs = 339000, placeholderColors = base.placeholderColors),
        )

        return MusicResult.Success(
            base.copy(
                tracks = mockTracks,
                totalDuration = "1h 14m",
                artistImageUrl = null // would be artist avatar
            )
        )
    }

    suspend fun getTopMixes(): MusicResult<List<Playlist>> {
        delay(300)
        return MusicResult.Success(
            listOf(
                Playlist("m1", "Trending Music",    labelOverlay = "TRENDING\nMUSIC",    placeholderColors = listOf(Color(0xFF1565C0), Color(0xFF0D47A1))),
                Playlist("m2", "Weekly Top Songs",  labelOverlay = "WEEKLY TOP\nSONGS",  placeholderColors = listOf(Color(0xFFF9A825), Color(0xFFF57F17))),
                Playlist("m3", "Most Viewed Songs", labelOverlay = "MOST VIEWED\nSONGS", placeholderColors = listOf(Color(0xFFBF360C), Color(0xFF8D1A00))),
            )
        )
    }

    suspend fun getRecentListening(): MusicResult<List<Playlist>> {
        delay(300)
        return MusicResult.Success(
            listOf(
                Playlist("r1", "Room vibes",   placeholderColors = listOf(Color(0xFF2A2A3A), Color(0xFF1A1A26))),
                Playlist("r2", "Retro cassette",placeholderColors = listOf(Color(0xFF6A3A6A), Color(0xFF8B1A8B))),
            )
        )
    }

    suspend fun getFeaturedArtist(): MusicResult<FeaturedArtist> {
        delay(200)
        return MusicResult.Success(
            FeaturedArtist(
                artist = Artist(
                    "fe1", "Billie Eilish",
                    placeholderColors = listOf(Color(0xFF1A1230), Color(0xFF2A1845))
                ),
                description = "You can have easy access to every song of Billie Eilish by just clicking the Listen Now button. You can also follow her for supporting her.",
                ctaLabel = "Listen Now"
            )
        )
    }

    // ── Discover ──────────────────────────────────────────────────────────────

    suspend fun getGenres(): MusicResult<List<Genre>> {
        delay(400)
        return MusicResult.Success(
            listOf(
                Genre("g1", "Rap Tracks",  "Rap Songs",  listOf(Color(0xFF3A1A2A), Color(0xFF1A0A12))),
                Genre("g2", "Pop Tracks",  "Pop Songs",  listOf(Color(0xFF1A2A4A), Color(0xFF0A1228))),
                Genre("g3", "Rock Tracks", "Rock Songs", listOf(Color(0xFF2A2A3A), Color(0xFF12121E))),
                Genre("g4", "Jazz Tracks", "Jazz Songs", listOf(Color(0xFF1A3A2A), Color(0xFF0A1A12))),
                Genre("g5", "R&B Tracks",  "R&B Songs",  listOf(Color(0xFF3A2A1A), Color(0xFF1A1208))),
            )
        )
    }

    suspend fun getMoodPlaylists(): MusicResult<List<MoodPlaylist>> {
        delay(400)
        return MusicResult.Success(
            listOf(
                MoodPlaylist("mo1", "Sad Songs",     "Sad Songs",     listOf(Color(0xFF1A2030), Color(0xFF0A1018))),
                MoodPlaylist("mo2", "Workout Songs", "Workout Songs", listOf(Color(0xFF3A2A10), Color(0xFF1A1208))),
                MoodPlaylist("mo3", "Chill Songs",   "Chill Songs",   listOf(Color(0xFF2A1A3A), Color(0xFF12081E))),
                MoodPlaylist("mo4", "Party Songs",   "Party Songs",   listOf(Color(0xFF3A1A10), Color(0xFF1A0808))),
            )
        )
    }

    suspend fun getNewReleases(): MusicResult<List<Track>> {
        delay(400)
        return MusicResult.Success(
            listOf(
                Track("t1", "Time",         "Lusiano",  placeholderColors = listOf(Color(0xFF3A1A1A), Color(0xFF1A0A0A))),
                Track("t2", "112",          "Jazzek",   placeholderColors = listOf(Color(0xFF101A3A), Color(0xFF080E1A))),
                Track("t3", "We Dont Care", "DL Gulum", placeholderColors = listOf(Color(0xFF1A2A1A), Color(0xFF0A120A))),
                Track("t4", "Midnight",     "Ella M.",  placeholderColors = listOf(Color(0xFF2A1A3A), Color(0xFF12081E))),
            )
        )
    }

    suspend fun getBrowseCategories(): MusicResult<List<BrowseCategory>> {
        delay(200)
        return MusicResult.Success(
            listOf(
                BrowseCategory("bc1", "Made\nfor You",  Color(0xFF1565C0), listOf(Color(0xFF0D47A1), Color(0xFF1976D2))),
                BrowseCategory("bc2", "RELEASED",       Color(0xFF6A1B9A), listOf(Color(0xFF4A148C), Color(0xFF8E24AA))),
                BrowseCategory("bc3", "Music\nCharts",  Color(0xFF00838F), listOf(Color(0xFF006064), Color(0xFF00ACC1))),
                BrowseCategory("bc4", "Podcasts",       Color(0xFFC62828), listOf(Color(0xFFB71C1C), Color(0xFFEF5350))),
                BrowseCategory("bc5", "Bollywood",      Color(0xFFF57F17), listOf(Color(0xFFE65100), Color(0xFFFFB300))),
                BrowseCategory("bc6", "Pop\nFusion",    Color(0xFF2E7D32), listOf(Color(0xFF1B5E20), Color(0xFF43A047))),
            )
        )
    }

    private val allMockTracks = listOf(
        Track("t1", "Time", "Lusiano", durationMs = 180000, placeholderColors = listOf(Color(0xFF3A1A1A), Color(0xFF1A0A0A))),
        Track("t2", "112", "Jazzek", durationMs = 210000, placeholderColors = listOf(Color(0xFF101A3A), Color(0xFF080E1A))),
        Track("t3", "We Dont Care", "DL Gulum", durationMs = 195000, placeholderColors = listOf(Color(0xFF1A2A1A), Color(0xFF0A120A))),
        Track("t4", "Midnight", "Ella M.", durationMs = 225000, placeholderColors = listOf(Color(0xFF2A1A3A), Color(0xFF12081E))),
        Track("p1", "Dracula - JENNIE Remix", "Jennie", durationMs = 205000, placeholderColors = listOf(Color(0xFF2A1A1A), Color(0xFF120A0A))),
        Track("p2", "One Of The Girls", "Jennie", durationMs = 244000, placeholderColors = listOf(Color(0xFF1A1A2A), Color(0xFF0A0A12))),
        Track("p3", "like JENNIE", "Jennie", durationMs = 188000, placeholderColors = listOf(Color(0xFF3A2A1A), Color(0xFF1A1208))),
        Track("p4", "Mantra", "Jennie", durationMs = 175000, placeholderColors = listOf(Color(0xFF1A2A2A), Color(0xFF0A1212))),
        Track("p5", "Solo", "Jennie", durationMs = 169000, placeholderColors = listOf(Color(0xFF2A2A1A), Color(0xFF12120A))),
        Track("tr1", "White America", "Eminem", durationMs = 324000, placeholderColors = listOf(Color(0xFF3A0000), Color(0xFF1A0000))),
        Track("tr2", "Business", "Eminem", durationMs = 251000, placeholderColors = listOf(Color(0xFF3A0000), Color(0xFF1A0000))),
        Track("tr9", "Without Me", "Eminem", durationMs = 290000, placeholderColors = listOf(Color(0xFF3A0000), Color(0xFF1A0000))),
    )

    suspend fun getTrackById(id: String): MusicResult<Track> {
        delay(600)
        val track = allMockTracks.find { it.id == id } 
            ?: allMockTracks.firstOrNull { it.id.startsWith("t") } 
            ?: allMockTracks.first()

        val dynamicLyrics = when (track.artist.lowercase()) {
            "jennie" -> """
                [Intro]
                Yeah, yeah, yeah...
                
                [Verse 1]
                I'm sitting here in the dark
                Thinking 'bout how we fell apart
                The memories keep rushing in
                I don't know where to begin
                
                [Chorus]
                Cause I'm a shining solo
                I'm going solo-lo-lo-lo-lo
                Watch me as I light up the night
                Everything is gonna be alright
                
                [Verse 2]
                Don't need nobody to hold my hand
                I'm making my own master plan
                From Paris down to Tokyo
                You know I'm ready for the show
            """.trimIndent()
            "eminem" -> """
                Look, if you had one shot, or one opportunity
                To seize everything you ever wanted in one moment
                Would you capture it, or just let it slip?
                
                Yo, his palms are sweaty, knees weak, arms are heavy
                There's vomit on his sweater already, mom' spaghetti
                He's nervous, but on the surface he looks calm and ready
                To drop bombs, but he keeps on forgettin'
            """.trimIndent()
            else -> """
                (Instrumental Intro)
                
                Verse 1:
                Walking down this empty street
                Feeling the rhythm of the beat
                The stars above are shining bright
                In the middle of the neon light
                
                Chorus:
                Oh, this is our time to fly
                Reach for the colors in the sky
                No matter what they say or do
                I'll always be right here with you
            """.trimIndent()
        }

        return MusicResult.Success(
            track.copy(lyrics = dynamicLyrics)
        )
    }

    suspend fun toggleFavorite(trackId: String): MusicResult<Boolean> {
        delay(300)
        // In a real app, this would update Firestore
        return MusicResult.Success(true)
    }

    suspend fun downloadTrack(trackId: String): MusicResult<Boolean> {
        delay(1000)
        // In a real app, this would trigger WorkManager for download
        return MusicResult.Success(true)
    }

    suspend fun addToPlaylist(trackId: String, playlistId: String): MusicResult<Boolean> {
        delay(500)
        // In a real app, this would add trackId to the playlist's tracks field in Firestore
        return MusicResult.Success(true)
    }
}
