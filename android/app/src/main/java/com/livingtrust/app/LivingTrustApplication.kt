package com.livingtrust.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * LivingTrustApplication — the app-wide entry point for Android.
 *
 * WHY extend Application?
 * Android creates exactly ONE Application object when the process starts,
 * before any screen (Activity) is shown. By subclassing it we can initialize
 * app-wide libraries here instead of repeating setup in every screen.
 *
 * WHY @HiltAndroidApp?
 * This annotation tells Hilt (our Dependency Injection framework) to generate
 * all the wiring code it needs at compile time. Without it, Hilt cannot inject
 * dependencies into Activities, ViewModels, or any other Android component.
 *
 * What is Dependency Injection (DI)?
 * Instead of writing:
 *   val repo = AuthRepositoryImpl(AuthApi(), TokenManager())   // ← manual, hard to test
 * you declare what a class NEEDS and Hilt provides it automatically:
 *   class AuthViewModel @Inject constructor(val repo: AuthRepository)  // ← Hilt handles it
 *
 * Benefits:
 *   - Easier to test: swap real objects with fakes in unit tests
 *   - No hidden wiring: dependencies are declared in constructors
 *   - Hilt manages object lifetimes (Singleton, ViewModel scope, etc.)
 */
@HiltAndroidApp
class LivingTrustApplication : Application()
