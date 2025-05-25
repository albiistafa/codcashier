package dev.codcow.kasirku.middleware

import android.content.Context
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoleMiddleware @Inject constructor() {

    /**
     * Checks the user role and navigates to the appropriate screen
     * @param context The application context
     * @param navController The navigation controller
     * @param afterLogin Whether this check is performed after login (true) or app startup (false)
     */
    fun checkRoleAndNavigate(context: Context, navController: NavController, afterLogin: Boolean = false) {
        CoroutineScope(Dispatchers.IO).launch {
            val role = getUserRole(context)

            withContext(Dispatchers.Main) {
                when (role) {
                    "admin" -> {
                        if (afterLogin) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    }
                    "pegawai" -> {
                        if (afterLogin) {
                            navController.navigate(Screen.HomePegawai.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.HomePegawai.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    }
                    else -> {
                        // If role is not recognized or null, navigate to login
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    /**
     * Handles menu navigation based on user role
     * @param context The application context
     * @param navController The navigation controller
     */
    fun navigateToRoleBasedMenu(context: Context, navController: NavController) {
        val role = getUserRole(context)

        when (role) {
            "admin" -> navController.navigate(Screen.MenuAdmin.route)
            "pegawai" -> navController.navigate(Screen.Menu.route)
            else -> navController.navigate(Screen.Login.route)
        }
    }

    /**
     * Gets the user role from SharedPreferences
     * @param context The application context
     * @return The user role or null if not found
     */
    private fun getUserRole(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_role", null)
    }
}