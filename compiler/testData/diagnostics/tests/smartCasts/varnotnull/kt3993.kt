// NB: should work fine already!

class User(val login : Boolean) {}

fun currentAccess(user: User?): Int {
    return when {
        user == null -> 0
        <!DEBUG_INFO_SMARTCAST!>user<!>.login -> 1 // nullity error
        else -> 2
    }
}