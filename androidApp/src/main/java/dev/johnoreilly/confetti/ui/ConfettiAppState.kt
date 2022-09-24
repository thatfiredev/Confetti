package dev.johnoreilly.confetti.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.johnoreilly.confetti.sessions.navigation.RoomsDestination
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.navigation.TopLevelDestination
import dev.johnoreilly.confetti.rooms.navigation.SessionsDestination
import dev.johnoreilly.confetti.speakers.navigation.SpeakersDestination

@Composable
fun rememberConfettiAppState(
    windowSizeClass: WindowSizeClass,
    navController: NavHostController = rememberNavController()
): ConfettiAppState {
    return remember(navController, windowSizeClass) {
        ConfettiAppState(navController, windowSizeClass)
    }
}


@Stable
class ConfettiAppState(
    val navController: NavHostController,
    val windowSizeClass: WindowSizeClass
) {
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val shouldShowBottomBar: Boolean
        get() = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact ||
            windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact

    val shouldShowNavRail: Boolean
        get() = !shouldShowBottomBar

    /**
     * Top level destinations to be used in the BottomBar and NavRail
     */
    val topLevelDestinations: List<TopLevelDestination> = listOf(
        TopLevelDestination(
            route = SessionsDestination.route,
            destination = SessionsDestination.destination,
            selectedIcon = Icons.Filled.PlayArrow,
            unselectedIcon =Icons.Outlined.PlayArrow,
            iconTextId = R.string.sessions
        ),
        TopLevelDestination(
            route = SpeakersDestination.route,
            destination = SpeakersDestination.destination,
            selectedIcon = Icons.Filled.Person,
            unselectedIcon =Icons.Outlined.Person,
            iconTextId = R.string.speakers
        ),
        TopLevelDestination(
            route = RoomsDestination.route,
            destination = RoomsDestination.destination,
            selectedIcon = Icons.Filled.LocationOn,
            unselectedIcon =Icons.Outlined.LocationOn,
            iconTextId = R.string.rooms
        ),
        )


    /**
     * UI logic for navigating to a particular destination in the app. The NavigationOptions to
     * navigate with are based on the type of destination, which could be a top level destination or
     * just a regular destination.
     *
     * Top level destinations have only one copy of the destination of the back stack, and save and
     * restore state whenever you navigate to and from it.
     * Regular destinations can have multiple copies in the back stack and state isn't saved nor
     * restored.
     *
     * @param destination: The [ConfettiNavigationDestination] the app needs to navigate to.
     * @param route: Optional route to navigate to in case the destination contains arguments.
     */
    fun navigate(destination: ConfettiNavigationDestination, route: String? = null) {
        if (destination is TopLevelDestination) {
            navController.navigate(route ?: destination.route) {
                // Pop up to the start destination of the graph to
                // avoid building up a large stack of destinations
                // on the back stack as users select items
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                // Avoid multiple copies of the same destination when
                // reselecting the same item
                launchSingleTop = true
                // Restore state when reselecting a previously selected item
                restoreState = true
            }
        } else {
            navController.navigate(route ?: destination.route)
        }
    }

    fun onBackClick() {
        navController.popBackStack()
    }
}

