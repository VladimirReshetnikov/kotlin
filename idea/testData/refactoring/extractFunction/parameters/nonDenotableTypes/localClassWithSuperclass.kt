trait T

fun foo(): T {
    class A: T

    // SIBGLING:
    fun bar(): T {
        return <selection>A()</selection>
    }
}