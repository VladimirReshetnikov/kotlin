package kotlin

import java.util.ArrayList
import kotlin.platform.platformName

/**
 * Returns a single list of all elements from all collections in the given collection.
 */
public fun <T> Iterable<Iterable<T>>.flatten(): List<T> {
    val result = ArrayList<T>()
    for (element in this) {
        result.addAll(element)
    }
    return result
}

/**
 * Returns a sequence of all elements from all sequences in this sequence.
 */
public fun <T> Sequence<Sequence<T>>.flatten(): Sequence<T> {
    return FlatteningSequence(this, { it })
}

/**
 * Returns a sequence of all elements from all sequences in this sequence.
 */
deprecated("Use Sequence<T> instead of Stream<T>")
public fun <T> Stream<Stream<T>>.flatten(): Stream<T> {
    return FlatteningStream(this, { it })
}

/**
 * Returns a single list of all elements from all arrays in the given array.
 */
public fun <T> Array<Array<out T>>.flatten(): List<T> {
    val result = ArrayList<T>(sumBy { it.size() })
    for (element in this) {
        result.addAll(element)
    }
    return result
}

/**
 * Returns a list of non-empty strings from this collection.
 */
public fun Iterable<String>.dropEmpty(): List<String> = filterNot { it.isEmpty() }

/**
 * Returns a list of non-empty strings from this array.
 */
public fun Array<String>.dropEmpty(): List<String> = filterNot { it.isEmpty() }

/**
 * Returns a sequence of non-empty strings from this sequence.
 */
public fun Sequence<String>.dropEmpty(): Sequence<String> = filterNot { it.isEmpty() }