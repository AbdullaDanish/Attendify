package com.abdulla.nsspda.semester.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulla.nsspda.MainApplication
import com.abdulla.nsspda.semester.data.Semester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SemesterViewModel() : ViewModel() {
    val semesterDao = MainApplication.semesterDatabase.getSemesterDao()
    val semesterList : LiveData<List<Semester>>  = semesterDao.getAllData()
    fun addSemester(semester : Semester){
        viewModelScope.launch(Dispatchers.IO) {
            semesterDao.insert(semester)
        }

    }
    fun deleteSemester(semester: Semester){
        viewModelScope.launch(Dispatchers.IO) {
            semesterDao.delete(semester)
        }


    }
}

