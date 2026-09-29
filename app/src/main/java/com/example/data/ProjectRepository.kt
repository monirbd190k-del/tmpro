package com.example.data

import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val dao: ProjectDao) {

    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()

    suspend fun getProjectById(id: Long): ProjectEntity? {
        return dao.getProjectById(id)
    }

    suspend fun saveProject(project: ProjectEntity): Long {
        return dao.insertProject(project)
    }

    suspend fun updateProject(project: ProjectEntity) {
        dao.updateProject(project)
    }

    suspend fun deleteProject(project: ProjectEntity) {
        dao.deleteProject(project)
    }

    suspend fun deleteProjectById(id: Long) {
        dao.deleteProjectById(id)
    }
}
