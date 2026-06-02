package com.example.echo_panda_mobile.data.remote

import com.example.echo_panda_mobile.data.repository.TokenStorage
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AuthAuthenticator(private val tokenStorage: TokenStorage) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // IMPORTANT: We removed the aggressive tokenStorage.clearSession() from here.
        // Clearing the session on every 401 is dangerous because a single failed 
        // secondary request (like fetching favorites) would log the user out of the 
        // entire app.
        
        // Let the 401 response reach the repository/ViewModel, which can then 
        // decide if it's a fatal session expiry or just a specific error.
        
        return null
    }
}
