// "Create function 'foo'" "true"

class A {
    fun foo(): String {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

fun test() {
    println("a = ${A().foo()}")
}