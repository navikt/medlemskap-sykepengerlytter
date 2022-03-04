package no.nav.medlemskap.sykepenger.lytter.security

import java.security.MessageDigest

val SALT = "Arbeids- og velferdsforvaltningen" //DON`T EVER CHANGE THIS!!
fun String.sha256(): String
{
    val bytes = MessageDigest.getInstance("SHA-256").digest((SALT+this).toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") {
        "%02x".format(it)
    }
}