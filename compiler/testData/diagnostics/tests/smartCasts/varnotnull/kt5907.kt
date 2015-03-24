public class X {
    public val x : String? = null
    public fun fn(): Int {
        if (x != null)
            return <!DEBUG_INFO_SMARTCAST!>x<!>.length() // bogus error here
        else
            return 0
    }
}

