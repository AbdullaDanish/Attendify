package com.abdulla.nsspda.attendance.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulla.nsspda.MainApplication
import com.abdulla.nsspda.attendance.data.StudentAttendance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class AttendanceViewmodel : ViewModel() {
    val attendanceDao = MainApplication.semesterDatabase.getStudentAttendanceDao()
    private val _attendanceSummaries = MutableLiveData<List<AttendanceSummary>>()
    val attendanceSummaries: LiveData<List<AttendanceSummary>> get() = _attendanceSummaries
    private val _attendanceList = MutableLiveData<List<StudentAttendance>>()
    val attendanceList: LiveData<List<StudentAttendance>> get() = _attendanceList
    private val _attendancePercentages = MutableLiveData<List<AttendancePercentage>>()
    val attendancePercentages: LiveData<List<AttendancePercentage>> get() = _attendancePercentages

    fun addAttendance(studentAttendance: StudentAttendance){
        viewModelScope.launch(Dispatchers.IO) {
            attendanceDao.insertAttendance(studentAttendance)
        }

    }
    fun deleteAttendance(studentAttendance: StudentAttendance){
        viewModelScope.launch(Dispatchers.IO) {
            attendanceDao.deleteAttendance(studentAttendance)
        }
    }
    fun fetchUniqueAttendanceSummaries(semester: String, branch: String,subject:String) {
        viewModelScope.launch(Dispatchers.IO) {
            val summaries = attendanceDao.getUniqueAttendanceSummaries(semester, branch,subject)
            _attendanceSummaries.postValue(summaries)
        }
    }
    fun fetchAttendanceBySemesterBranchDate(semester: String, branch: String,subject: String, date: Date) {
        viewModelScope.launch {
            val attendance = attendanceDao.getAttendanceBySemesterBranchDate(semester, branch,subject, date)
            _attendanceList.postValue(attendance)
        }
    }
    fun deleteAttendanceBySemesterBranchDate(semester: String, branch: String,subject: String, date: Date) {
        viewModelScope.launch(Dispatchers.IO) {
            attendanceDao.deleteAttendanceBySemesterBranchDate(semester, branch,subject, date)
            fetchAttendanceBySemesterBranchDate(semester, branch,subject, date)
        }
    }

    fun updateAttendanceList(updatedAttendanceList: List<StudentAttendance>) {
        viewModelScope.launch(Dispatchers.IO) {
            attendanceDao.updateAttendanceList(updatedAttendanceList)
            val updatedAttendance = updatedAttendanceList.firstOrNull()?.let { attendance ->
                attendanceDao.getAttendanceBySemesterBranchDate(
                    attendance.semester,
                    attendance.branch,
                    attendance.subject,
                    attendance.date
                )
            }
            updatedAttendance?.let {
                _attendanceList.postValue(it)
            }
        }
    }
    fun calculateAttendancePercentage(
            semester: String,
            branch: String,
            subject: String,
            startDate: Date,
            endDate: Date
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                val attendanceList = attendanceDao.getAttendanceBetweenDates(semester, branch, subject, startDate, endDate)
                val studentAttendances = attendanceList.groupBy { it.usn }
                val percentages = studentAttendances.map { (usn, attendances) ->
                    val totalClasses = attendances.size
                    val presentCount = attendances.count { it.present }
                    val percentage = if (totalClasses > 0) (presentCount.toDouble() / totalClasses) * 100 else 0.0
                    AttendancePercentage(usn, attendances.first().studentName, percentage)
                }
                _attendancePercentages.postValue(percentages)
            }
        }

}
data class AttendanceSummary(
    val sem: String,
    val branch: String,
    val subject: String,
    val date: Date
)
data class AttendancePercentage(
    val usn: String,
    val studentName: String,
    val percentage: Double
)

