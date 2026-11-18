// tv-material's default text style turns off Android's font padding, a switch Compose for Desktop doesn't have (its text
// never has that padding). This stands in for the one class of the library that names it.
package androidx.tv.material3.tokens

import androidx.compose.ui.text.TextStyle

val DefaultTextStyle: TextStyle = TextStyle.Default
