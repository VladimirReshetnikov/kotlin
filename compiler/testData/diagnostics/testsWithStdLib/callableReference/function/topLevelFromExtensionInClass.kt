import kotlin.reflect.*

class A

fun foo() {}
fun bar(<!UNUSED_PARAMETER!>x<!>: Int) {}
fun baz() = "OK"

class B {
    fun A.main() {
        val x = ::foo
        val y = ::bar
        val z = ::baz

        x : KFunction0<Unit>
        y : KFunction1<Int, Unit>
        z : KFunction0<String>
    }
}
