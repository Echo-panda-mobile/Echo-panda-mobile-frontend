# Dynamic API Integration Guide

## Overview
The app is now fully set up for dynamic API integration with a complete state management system. You can:
- ✅ Use mock data immediately for UI testing
- ✅ See loading states with shimmer animations
- ✅ Handle errors gracefully with retry logic
- ✅ Seamlessly swap from mock to real API data
- ✅ Refresh data with pull-to-refresh

## Architecture

### Components

#### 1. **Data Models** (`DashboardModels.kt`)
```kotlin
data class DashboardStats(
    val monthlyRevenue: Double,
    val revenueGrowth: Double,
    val streams: String,
    val listeners: String,
    val publishedSongs: Int,
    // ... more fields
)

data class ArtistDashboardData(
    val user: User,
    val stats: DashboardStats,
    val topTrack: TopTrack,
    val recentActivities: List<ActivityItem>
)
```

#### 2. **Repository** (`DashboardRepository.kt`)
Handles API calls and mock data:
```kotlin
suspend fun getArtistDashboard(
    user: User,
    useMockData: Boolean = true
): DashboardResult<ArtistDashboardData>
```

#### 3. **ViewModel** (`ArtistDashboardViewModel.kt`)
Manages UI state and user interactions:
- Loading, Success, Error states
- Load, refresh, and retry logic
- Toggle between mock and real data

#### 4. **UI Screen** (`ArtistDashboardScreen.kt`)
Fully dynamic Composable that:
- Observes ViewModel state
- Shows appropriate UI based on state
- Displays loading skeleton
- Handles errors gracefully

## Usage

### Basic Integration

```kotlin
// In your navigation or screen composable
@Composable
fun MyScreen(currentUser: User) {
    ArtistDashboardScreen(
        currentUser = currentUser,
        onNavigateToProfile = { /* ... */ },
        onNavigateToSettings = { /* ... */ },
        viewModel = remember { ArtistDashboardViewModel() }
    )
}
```

### Switching from Mock to Real API

1. **Currently (Mock Data)**:
```kotlin
val result = dashboardRepository.getArtistDashboard(
    user = user,
    useMockData = true  // ← Using mock data
)
```

2. **When Backend is Ready**:
   - Update `DashboardRepository.getArtistDashboard()` to make actual API calls
   - Replace the `delay(1000)` with your Retrofit/HTTP call
   - The UI will automatically work with real data!

Example API integration (template):
```kotlin
suspend fun getArtistDashboard(
    user: User,
    useMockData: Boolean = true
): DashboardResult<ArtistDashboardData> {
    return try {
        if (useMockData) {
            delay(1000)
            val mockData = DashboardMockData.getMockDashboardData(user)
            DashboardResult.Success(mockData)
        } else {
            // TODO: Call actual API
            val response = apiService.getArtistDashboard(user.id)
            DashboardResult.Success(response)
        }
    } catch (e: Exception) {
        DashboardResult.Error(e.message ?: "Unknown error")
    }
}
```

## UI States

### 1. **Loading State**
- Shows animated skeleton loaders
- Shimmer animation on all placeholder cards
- Non-blocking - user can still interact

### 2. **Success State**
- Displays all dashboard data
- Smooth scroll through content
- Pull-to-refresh available

### 3. **Error State**
- Shows error message
- Retry button for user
- Graceful error handling

## Features Included

### ✅ Mock Data
Located in `DashboardModels.kt`:
```kotlin
object DashboardMockData {
    fun getMockStats(): DashboardStats
    fun getMockTopTrack(): TopTrack
    fun getMockActivities(): List<ActivityItem>
    fun getMockDashboardData(user: User): ArtistDashboardData
}
```

### ✅ Loading Skeleton
Animated shimmer effect that shows:
- Header skeleton
- Revenue card placeholder
- Stats cards placeholders
- Track card placeholder
- Activity items placeholders

### ✅ Error Handling
- Automatic error messages
- Retry button functionality
- State preservation on refresh errors

### ✅ State Management
StateFlow for reactive updates:
```kotlin
val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
val useMockData: StateFlow<Boolean> = _useMockData.asStateFlow()
```

## Testing

### Test with Mock Data
```kotlin
// Mock data is enabled by default
val viewModel = ArtistDashboardViewModel()
viewModel.loadDashboard(testUser)
// UI shows mock data immediately
```

### Test with API
1. Implement actual API calls in `DashboardRepository`
2. Toggle mock flag:
```kotlin
viewModel.setUseMockData(false)
```

### Test Error Handling
Manually trigger error in repository for testing:
```kotlin
return DashboardResult.Error("Network error")
```

## API Integration Checklist

- [ ] Define API service interface (Retrofit)
- [ ] Update `DashboardRepository.getArtistDashboard()` with API call
- [ ] Map API response to `ArtistDashboardData` model
- [ ] Test with real backend
- [ ] Remove mock data toggle (optional)
- [ ] Add proper error handling for different HTTP errors

## Customization

### Add New Data Fields

1. Update `DashboardStats` in `DashboardModels.kt`:
```kotlin
data class DashboardStats(
    // ... existing fields
    val newField: String = ""  // Add with default
)
```

2. Update mock data:
```kotlin
fun getMockStats() = DashboardStats(
    // ... existing fields
    newField = "mock value"
)
```

3. Update UI in `ArtistDashboardScreen.kt` to display new field

4. Update API response mapping in repository

### Modify Loading Skeleton

Edit `DashboardLoadingState()` composable to match your new UI:
```kotlin
@Composable
private fun DashboardLoadingState() {
    // Add/remove skeleton placeholders here
}
```

### Change Animation Speed

Modify animation in `DashboardLoadingState()`:
```kotlin
shimmerAlpha.animateTo(
    targetValue = 0.8f,
    animationSpec = infiniteRepeatable(
        animation = tween(1000),  // ← Change duration here
        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
    )
)
```

## File Structure

```
data/
├── model/
│   ├── DashboardModels.kt       (New - data classes + mock data)
│   ├── User.kt
│   └── MusicModels.kt
├── repository/
│   ├── DashboardRepository.kt   (New - API integration point)
│   ├── AuthRepository.kt
│   └── MusicRepository.kt

presentation/
├── viewsmodel/
│   ├── ArtistDashboardViewModel.kt  (New - state management)
│   └── ...
└── views/
    └── user/
        └── artist/
            └── ArtistDashboardScreen.kt  (Updated - fully dynamic)
```

## Common Issues & Solutions

### Loading Never Completes
- Check if `currentUser` is being passed to the screen
- Verify `viewModel.loadDashboard()` is called in LaunchedEffect

### Mock Data Not Showing
- Ensure `useMockData = true` in repository
- Check if ViewModel is collecting correct state

### Error State Always Shows
- Add try-catch in repository
- Log error messages for debugging
- Check if mock data toggle is working

## Next Steps

1. ✅ Review the mock data in `DashboardModels.kt`
2. ✅ Test UI with mock data
3. ✅ When backend is ready, update `DashboardRepository` with API calls
4. ✅ Test with real data
5. ✅ Customize UI as needed
6. ✅ Deploy to production

## Support

The system is designed to be:
- **Flexible**: Easy to add new data fields
- **Testable**: Mock data always available for testing
- **Maintainable**: Clear separation of concerns
- **Scalable**: Ready for production APIs
