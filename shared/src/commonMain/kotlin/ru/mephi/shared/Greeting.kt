package ru.mephi.shared

class Greeting {
    fun greeting(): String {
        return "Hello, ${ru.mephi.shared.Platform().platform}!"
    }
}