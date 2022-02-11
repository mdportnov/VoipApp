package ru.mephi.shared

typealias Stack<T> = MutableList<T>

fun <T> Stack<T>.push(item: T) = add(item)

fun <T> Stack<T>.pop(): T? = if (isNotEmpty()) removeAt(lastIndex) else null

fun <T> Stack<T>.peek(): T? = if (isNotEmpty()) this[lastIndex] else null

fun <T> Stack<T>.popFromStackTill(el: T) {
    while (this.peek() != el)
        this.pop()
}