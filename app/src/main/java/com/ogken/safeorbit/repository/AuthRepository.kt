package com.ogken.safeorbit.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.ogken.safeorbit.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository interface managing Firebase Authentication.
 */
interface AuthRepository {
    val authState: Flow<User?>
    fun getCurrentFirebaseUser(): FirebaseUser?
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signUp(email: String, password: String, displayName: String, role: String): Result<User>
    suspend fun signOut()
    suspend fun sendPasswordReset(email: String): Result<Unit>
    fun validateEmail(email: String): String?
    fun validatePassword(password: String): String?
}

/**
 * Production implementation powered by Firebase Authentication and Cloud Firestore.
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override val authState: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                // Fetch profile from Firestore
                firestore.collection("users").document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null && snapshot.exists()) {
                            val user = User.fromMap(snapshot.data ?: emptyMap(), firebaseUser.uid)
                            trySend(user)
                        } else {
                            // Basic user representation before Firestore sync completes
                            val fallback = User(
                                id = firebaseUser.uid,
                                name = firebaseUser.displayName ?: "SafeOrbit User",
                                email = firebaseUser.email ?: "",
                                isProfileSetupComplete = false
                            )
                            trySend(fallback)
                        }
                    }
            } else {
                trySend(null)
            }
        }

        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun getCurrentFirebaseUser(): FirebaseUser? = auth.currentUser

    override fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> "Email address cannot be empty"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() -> "Please enter a valid email address"
            else -> null
        }
    }

    override fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "Password cannot be empty"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        val emailError = validateEmail(email)
        if (emailError != null) return Result.failure(IllegalArgumentException(emailError))

        val passwordError = validatePassword(password)
        if (passwordError != null) return Result.failure(IllegalArgumentException(passwordError))

        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Authentication failed: User is null")

            // Load user profile from Firestore
            val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = if (doc.exists()) {
                User.fromMap(doc.data ?: emptyMap(), firebaseUser.uid)
            } else {
                val newUser = User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: email.substringBefore("@"),
                    email = firebaseUser.email ?: email.trim(),
                    isProfileSetupComplete = true
                )
                firestore.collection("users").document(firebaseUser.uid).set(newUser.toMap()).await()
                newUser
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, password: String, displayName: String, role: String): Result<User> {
        val emailError = validateEmail(email)
        if (emailError != null) return Result.failure(IllegalArgumentException(emailError))

        val passwordError = validatePassword(password)
        if (passwordError != null) return Result.failure(IllegalArgumentException(passwordError))

        val trimmedName = displayName.trim()
        if (trimmedName.length < 2) return Result.failure(IllegalArgumentException("Display name must be at least 2 characters"))

        return try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Registration failed: User is null")

            // Update Firebase User display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(trimmedName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // Save user profile in Cloud Firestore (never store plain password)
            val user = User(
                id = firebaseUser.uid,
                name = trimmedName,
                email = firebaseUser.email ?: email.trim(),
                role = role.ifBlank { "Member" },
                avatarInitial = trimmedName.take(1).uppercase(),
                isProfileSetupComplete = true,
                createdAt = System.currentTimeMillis()
            )
            firestore.collection("users").document(firebaseUser.uid).set(user.toMap()).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        val emailError = validateEmail(email)
        if (emailError != null) return Result.failure(IllegalArgumentException(emailError))

        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        val instance: AuthRepository by lazy { FirebaseAuthRepository() }
    }
}

/**
 * In-memory test double for local JVM unit testing of authentication workflows.
 */
class InMemoryAuthRepository : AuthRepository {
    private val authFlow = MutableStateFlow<User?>(null)

    override val authState: Flow<User?> = authFlow.asStateFlow()

    override fun getCurrentFirebaseUser(): FirebaseUser? = null

    override fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        return when {
            trimmed.isEmpty() -> "Email address cannot be empty"
            !trimmed.matches(emailRegex) -> "Please enter a valid email address"
            else -> null
        }
    }

    override fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "Password cannot be empty"
            password.length < 8 -> "Password must be at least 8 characters"
            !password.any { it.isUpperCase() } -> "Password must contain at least one uppercase letter"
            !password.any { it.isLowerCase() } -> "Password must contain at least one lowercase letter"
            !password.any { it.isDigit() } -> "Password must contain at least one number"
            else -> null
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        val emailErr = validateEmail(email)
        if (emailErr != null) return Result.failure(IllegalArgumentException(emailErr))
        val passErr = validatePassword(password)
        if (passErr != null) return Result.failure(IllegalArgumentException(passErr))

        val user = User(
            id = "test_user_id",
            name = email.substringBefore("@"),
            email = email.trim(),
            isProfileSetupComplete = true
        )
        authFlow.value = user
        return Result.success(user)
    }

    override suspend fun signUp(email: String, password: String, displayName: String, role: String): Result<User> {
        val emailErr = validateEmail(email)
        if (emailErr != null) return Result.failure(IllegalArgumentException(emailErr))
        val passErr = validatePassword(password)
        if (passErr != null) return Result.failure(IllegalArgumentException(passErr))
        if (displayName.trim().isEmpty()) return Result.failure(IllegalArgumentException("Display name cannot be empty"))

        val user = User(
            id = "test_user_id",
            name = displayName.trim(),
            email = email.trim(),
            role = role.ifBlank { "Member" },
            isProfileSetupComplete = true
        )
        authFlow.value = user
        return Result.success(user)
    }

    override suspend fun signOut() {
        authFlow.value = null
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        val emailErr = validateEmail(email)
        if (emailErr != null) return Result.failure(IllegalArgumentException(emailErr))
        return Result.success(Unit)
    }

    companion object {
        val instance: InMemoryAuthRepository by lazy { InMemoryAuthRepository() }
    }
}
