package com.example.myapp007bfragmentsexample1

import androidx.fragment.app.Fragment

fun Fragment.replaceCurrentScreenWith(nextScreenFragment: Fragment) {
    parentFragmentManager.beginTransaction()
        .replace(R.id.fragmentContainer, nextScreenFragment)
        .commit()
}
