public class A() {
    public val foo: Int? = 1
}

fun Int.bar(i: Int) = i

fun test() {
    val p = A()
    if (p.foo is Int) <!DEBUG_INFO_SMARTCAST!>p.foo<!> bar 11
}
