package org.jjrn.Agenda

data class Clase(val hora: String, val lugar: String, val asignatura: String, val profesor: String)
data class User(val name: String, val user: String, val email: String, val password: String)
data class Tarea(val clase: String, val fecha: String, val nombre: String)