package com.abdulla.nsspda

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.abdulla.nsspda.attendance.presentation.details.AttendanceDetailsRoute
import com.abdulla.nsspda.attendance.presentation.history.AttendanceHistoryRoute
import com.abdulla.nsspda.attendance.presentation.marking.AttendanceMarkingRoute
import com.abdulla.nsspda.attendance.presentation.percentage.AttendancePercentageRoute
import com.abdulla.nsspda.classoverview.presentation.ClassOverviewRoute
import com.abdulla.nsspda.navigation.AppRoutes
import com.abdulla.nsspda.semester.presentation.SemesterRoute
import com.abdulla.nsspda.student.presentation.StudentRoute
import com.abdulla.nsspda.ui.theme.AppAnimation

@Composable
fun MyAppNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.SEMESTER_LIST,

        // Forward navigation:
        // New screen enters gently from the right.
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth ->
                    fullWidth / 5
                },
                animationSpec = tween(
                    durationMillis = AppAnimation.Normal,
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = AppAnimation.Normal
                )
            )
        },

        // Current screen moves slightly left and fades.
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth ->
                    -fullWidth / 10
                },
                animationSpec = tween(
                    durationMillis = AppAnimation.Fast,
                    easing = FastOutSlowInEasing
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = AppAnimation.Fast
                )
            )
        },

        // Back navigation:
        // Previous screen returns gently from the left.
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth ->
                    -fullWidth / 5
                },
                animationSpec = tween(
                    durationMillis = AppAnimation.Normal,
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = AppAnimation.Normal
                )
            )
        },

        // Current screen exits toward the right.
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth ->
                    fullWidth / 5
                },
                animationSpec = tween(
                    durationMillis = AppAnimation.Fast,
                    easing = FastOutSlowInEasing
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = AppAnimation.Fast
                )
            )
        }
    ) {
        composable(
            route = AppRoutes.SEMESTER_LIST
        ) {
            SemesterRoute(
                navController = navController
            )
        }

        composable(
            route = AppRoutes.CLASS_OVERVIEW_PATTERN,
            arguments = classArguments()
        ) {
            ClassOverviewRoute(
                onNavigateBack = navController::navigateUp,
                onNavigateToStudents = {
                        semester,
                        branch,
                        subject ->

                    navController.navigate(
                        AppRoutes.students(
                            semester = semester,
                            branch = branch,
                            subject = subject
                        )
                    )
                },
                onNavigateToAttendanceHistory = {
                        semester,
                        branch,
                        subject ->

                    navController.navigate(
                        AppRoutes.attendanceHistory(
                            semester = semester,
                            branch = branch,
                            subject = subject
                        )
                    )
                }
            )
        }

        composable(
            route = AppRoutes.STUDENTS_PATTERN,
            arguments = classArguments()
        ) {
            StudentRoute(
                onNavigateBack = navController::navigateUp
            )
        }

        composable(
            route = AppRoutes.ATTENDANCE_HISTORY_PATTERN,
            arguments = classArguments()
        ) {
            AttendanceHistoryRoute(
                navController = navController
            )
        }

        composable(
            route = AppRoutes.ATTENDANCE_MARKING_PATTERN,
            arguments = classArguments()
        ) {
            AttendanceMarkingRoute(
                navController = navController
            )
        }

        composable(
            route = AppRoutes.ATTENDANCE_DETAILS_PATTERN,
            arguments = classArguments() + navArgument(
                name = "date"
            ) {
                type = NavType.StringType
            }
        ) {
            AttendanceDetailsRoute(
                navController = navController
            )
        }

        composable(
            route = AppRoutes.ATTENDANCE_PERCENTAGE_PATTERN,
            arguments = classArguments()
        ) {
            AttendancePercentageRoute(
                navController = navController
            )
        }
    }
}

private fun classArguments() = listOf(
    navArgument("semester") {
        type = NavType.StringType
    },
    navArgument("branch") {
        type = NavType.StringType
    },
    navArgument("subject") {
        type = NavType.StringType
    }
)