package com.abdulla.nsspda.attendance.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abdulla.nsspda.R
import com.abdulla.nsspda.attendance.viewmodel.AttendanceViewmodel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AttendanceScreen(
    semester: String?,
    branch: String?,
    subject: String?,
    navController: NavController
) {
    val studentAttendanceViewModel: AttendanceViewmodel = viewModel()
    val attendanceSummaries by studentAttendanceViewModel.attendanceSummaries.observeAsState(
        emptyList()
    )
    var expanded by remember { mutableStateOf(false) }
    val insertSubject = subject.toString()
    LaunchedEffect(Unit) {
        studentAttendanceViewModel.fetchUniqueAttendanceSummaries(
            semester.toString(),
            branch.toString(),
            subject.toString()
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Attendance Records Dates",
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(onClick = {
                            expanded = false
                            navController.navigate("calculatePercentageScreen/${semester}/${branch}/${subject}")
                        }) {
                            Text("Calculate Percentage")
                        }
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("detailsScreen2/${semester}/${branch}/${subject}")
                },
                shape = RoundedCornerShape(10)
            ) {
                Text(
                    text = "New Attendance", modifier = Modifier.padding(8.dp),
                    fontSize = 16.sp
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(id = R.drawable.pdalogo),
                contentDescription = "Application  Logo Watermark",
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.1f),
                contentScale = ContentScale.Fit
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.padding(bottom = 100.dp)
                ) {
                    items(attendanceSummaries) { summary ->
                        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        val formattedDate = formatter.format(summary.date)
                        Card(
                            modifier = Modifier
                                .padding(vertical = 10.dp, horizontal = 16.dp)
                                .shadow(8.dp, RoundedCornerShape(12.dp))
                                .clickable {
                                    navController.navigate("detailsScreen3/${semester}/${branch}/${subject}/${formattedDate}")
                                },
                            shape = RoundedCornerShape(12.dp),
                            elevation = 8.dp,
                            backgroundColor = MaterialTheme.colors.surface
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = "Date: $formattedDate",
                                    style = MaterialTheme.typography.h6.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.primary
                                    ),
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp)) // Space between date and semester

                                Text(
                                    text = "Sem: ${summary.sem}",
                                    style = MaterialTheme.typography.body1.copy(
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.Medium
                                    ),
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sub: $insertSubject",
                                    style = MaterialTheme.typography.body1.copy(
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.Medium
                                    ),
                                    fontSize = 16.sp
                                )
                            }
                        }

                    }
                }
            }
        }
    }

}