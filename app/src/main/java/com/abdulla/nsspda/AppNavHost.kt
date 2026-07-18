package com.abdulla.nsspda

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.abdulla.nsspda.attendance.presentation.AttendanceListScreen
import com.abdulla.nsspda.attendance.presentation.AttendanceScreen
import com.abdulla.nsspda.attendance.presentation.CalculatePercentageScreen
import com.abdulla.nsspda.semester.presentation.SemesterListScreen
import com.abdulla.nsspda.student.presentation.StudentScreen
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MyAppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "semesterListScreen",
        enterTransition = {
            slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) + fadeIn(animationSpec = tween(500))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }) + fadeOut(animationSpec = tween(500))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth }) + fadeIn(animationSpec = tween(500))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }) + fadeOut(animationSpec = tween(500))
        }
    ) {
        composable("semesterListScreen") {
            SemesterListScreen(navController = navController)
        }
        composable("detailsScreen/{semester}/{branch}/{subject}") { backStackEntry ->
            val semester = backStackEntry.arguments?.getString("semester")
            val branch = backStackEntry.arguments?.getString("branch")
            val subject = backStackEntry.arguments?.getString("subject")

            AttendanceScreen(semester = semester, branch = branch,subject = subject, navController = navController)
        }
        composable("detailsScreen2/{semester}/{branch}/{subject}") { backStackEntry ->
            val semester = backStackEntry.arguments?.getString("semester")
            val branch = backStackEntry.arguments?.getString("branch")
            val subject = backStackEntry.arguments?.getString("subject")

            StudentScreen(semester = semester, branch = branch,subject = subject, navController = navController)
        }
        composable("detailsScreen3/{semester}/{branch}/{subject}/{date}") { backStackEntry ->
            val semester = backStackEntry.arguments?.getString("semester")
            val branch = backStackEntry.arguments?.getString("branch")
            val dateString = backStackEntry.arguments?.getString("date")
            val subject = backStackEntry.arguments?.getString("subject")
            val date = dateString?.let {
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(it)
            }
            AttendanceListScreen(semester = semester, branch = branch, date = date,subject = subject, navController = navController)
        }

        composable("calculatePercentageScreen/{semester}/{branch}/{subject}") { backStackEntry ->
            val semester = backStackEntry.arguments?.getString("semester")
            val branch = backStackEntry.arguments?.getString("branch")
            val subject = backStackEntry.arguments?.getString("subject")
            CalculatePercentageScreen(semester = semester, branch = branch,subject = subject,navController = navController)
        }
    }
}

