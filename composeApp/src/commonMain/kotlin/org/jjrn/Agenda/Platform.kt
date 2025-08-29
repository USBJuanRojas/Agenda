package org.jjrn.Agenda

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform