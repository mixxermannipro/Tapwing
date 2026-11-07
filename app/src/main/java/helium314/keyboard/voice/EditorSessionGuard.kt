// SPDX-License-Identifier: GPL-3.0-or-later
package helium314.keyboard.voice

/** The IME owns eligibility and replaces the token on every editor or lifecycle change. */
internal class EditorSessionGuard {
    class Token

    var current: Token? = null
        private set

    fun update(eligible: Boolean) {
        current = if (eligible) Token() else null
    }

    fun isCurrent(token: Token): Boolean = token === current
}
