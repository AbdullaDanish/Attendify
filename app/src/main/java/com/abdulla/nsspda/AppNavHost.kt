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
import com.abdulla.nsspda.attendance.presentation.history.search.AttendanceHistorySearchRoute
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
                    ) {
                        launchSingleTop = true
                    }
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
                    ) {
                        launchSingleTop = true
                    }
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
            route =
                AppRoutes.ATTENDANCE_HISTORY_SEARCH_PATTERN,
            arguments = classArguments(),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth ->
                        fullWidth / 4
                    },
                    animationSpec = tween(
                        durationMillis =
                            AppAnimation.Normal,
                        easing =
                            FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis =
                            AppAnimation.Normal
                    )
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        durationMillis =
                            AppAnimation.Fast
                    )
                )
            },
            popEnterTransition = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis =
                            AppAnimation.Normal
                    )
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth ->
                        fullWidth / 4
                    },
                    animationSpec = tween(
                        durationMillis =
                            AppAnimation.Fast,
                        easing =
                            FastOutSlowInEasing
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis =
                            AppAnimation.Fast
                    )
                )
            }
        ) {
            AttendanceHistorySearchRoute(
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
            arguments = classArguments() +
                    navArgument(
                        name = AppRoutes.ARG_DATE
                    ) {
                        type = NavType.StringType
                    }
        ) {
            AttendanceDetailsRoute(
                navController = navController
            )
        }

        composable(
            route =
                AppRoutes.ATTENDANCE_PERCENTAGE_PATTERN,
            arguments = classArguments()
        ) {
            AttendancePercentageRoute(
                navController = navController
            )
        }
    }
}

private fun classArguments() = listOf(
    navArgument(AppRoutes.ARG_SEMESTER) {
        type = NavType.StringType
    },
    navArgument(AppRoutes.ARG_BRANCH) {
        type = NavType.StringType
    },
    navArgument(AppRoutes.ARG_SUBJECT) {
        type = NavType.StringType
    }
)