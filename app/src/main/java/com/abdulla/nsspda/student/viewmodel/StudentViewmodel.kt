package com.abdulla.nsspda.student.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulla.nsspda.MainApplication
import com.abdulla.nsspda.student.data.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StudentViewModel() : ViewModel() {
    private val studentDao = MainApplication.semesterDatabase.getStudentData()
    private val _students = MutableLiveData<List<Student>>()
    val students: LiveData<List<Student>> = _students
    fun addStudent(student: Student,semester: String, branch: String) {
        viewModelScope.launch(Dispatchers.IO) {
            studentDao.insertStudent(student)
            getStudentsBySemesterAndBranch(semester, branch)
        }
    }
    fun getStudentsBySemesterAndBranch(semester: String, branch: String) {
        viewModelScope.launch {
            try {
                val studentList = studentDao.getStudentsBySemesterAndBranch(semester, branch)
                _students.postValue(studentList)
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error fetching students", e)
            }
        }
    }
    fun deleteStudent(student: Student) {
        viewModelScope.launch(Dispatchers.IO) {
            studentDao.deleteStudent(student)
            getStudentsBySemesterAndBranch(student.semester, student.branch)
        }
    }
    fun deleteAllStudents(semester: String, branch: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                studentDao.deleteAllStudentsBySemesterAndBranch(semester, branch)
                getStudentsBySemesterAndBranch(semester, branch)
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error deleting all students", e)
            }
        }
    }
}
