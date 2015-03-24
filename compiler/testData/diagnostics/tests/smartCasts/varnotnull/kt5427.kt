// NB: it should already work!

fun foo(p: String?): Int {
    val x = if (p != null) { <!DEBUG_INFO_SMARTCAST!>p<!> } else "a"
    return x.length()
}
