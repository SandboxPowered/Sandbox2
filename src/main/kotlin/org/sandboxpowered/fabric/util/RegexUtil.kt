package org.sandboxpowered.fabric.util

object RegexUtil {
    /**
     * Converts a standard POSIX Shell globbing pattern into a regular expression
     * pattern. The result can be used with the standard [java.util.regex] API to
     * recognize strings which match the glob pattern.
     *
     * @see <a href="https://pubs.opengroup.org/onlinepubs/009695399/utilities/xcu_chap02.html#tag_02_13_01">the POSIX Shell language</a>
     *
     * @param pattern A glob pattern.
     * @return A regex pattern to recognize the given glob pattern.
     */
    fun convertGlobToRegex(pattern: String): String {
        val sb = StringBuilder(pattern.length)
        var inGroup = 0
        var inClass = 0
        var firstIndexInClass = -1
        val arr = pattern.toCharArray()
        var i = 0
        while (i < arr.size) {
            when (val ch = arr[i]) {
                '\\' -> if (++i >= arr.size) {
                    sb.append('\\')
                } else {
                    val next = arr[i]
                    when (next) {
                        ',' -> {
                        }
                        'Q', 'E' -> {
                            // extra escape needed
                            sb.append('\\')
                            sb.append('\\')
                        }
                        else -> sb.append('\\')
                    }
                    sb.append(next)
                }
                '*' -> if (inClass == 0) sb.append(".*") else sb.append('*')
                '?' -> if (inClass == 0) sb.append('.') else sb.append('?')
                '[' -> {
                    inClass++
                    firstIndexInClass = i + 1
                    sb.append('[')
                }
                ']' -> {
                    inClass--
                    sb.append(']')
                }
                '.', '(', ')', '+', '|', '^', '$', '@', '%' -> {
                    if (inClass == 0 || firstIndexInClass == i && ch == '^') sb.append('\\')
                    sb.append(ch)
                }
                '!' -> if (firstIndexInClass == i) sb.append('^') else sb.append('!')
                '{' -> {
                    inGroup++
                    sb.append('(')
                }
                '}' -> {
                    inGroup--
                    sb.append(')')
                }
                ',' -> if (inGroup > 0) sb.append('|') else sb.append(',')
                else -> sb.append(ch)
            }
            i++
        }
        return sb.toString()
    }
}