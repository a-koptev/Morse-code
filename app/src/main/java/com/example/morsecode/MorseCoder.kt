package com.example.morsecode

import kotlin.collections.HashMap


class MorseCoder {
    private val alpha = HashMap<Char, String>(33)
    private val dotDash = HashMap<String, Char>(33)
    private val delays = HashMap<Char, Int>()
    private fun fillAlpha() {
        alpha['А'] = ".-";   alpha['Б'] = "-...";  alpha['В'] = ".--"
        alpha['Г'] = "--.";  alpha['Д'] = "-..";   alpha['Е'] = "."
        alpha['Ж'] = "...-"; alpha['З'] = "--..";  alpha['И'] = ".."
        alpha['Й'] = ".---"; alpha['К'] = "-.-";   alpha['Л'] = ".-.."
        alpha['М'] = "--";   alpha['Н'] = "-.";    alpha['О'] = "---"
        alpha['П'] = ".--."; alpha['Р'] = ".-.";   alpha['С'] = "..."
        alpha['Т'] = "-";    alpha['У'] = "..-";   alpha['Ф'] = "..-."
        alpha['Х'] = "...."; alpha['Ц'] = "-.-.";  alpha['Ч'] = "---."
        alpha['Ш'] = "----"; alpha['Щ']  = "--.-"; alpha['Ъ'] = "--.--"
        alpha['Ы'] = "-.--"; alpha['Ь'] = "-..-";  alpha['Э'] = "..-.."
        alpha['Ю'] = "..--"; alpha['Я'] = ".-.-"

        alpha['А'] = ".-";   alpha['B'] = "-...";  alpha['C'] = "-.-."
        alpha['D'] = "-..";  alpha['E'] = ".";     alpha['F'] = "..-."
        alpha['G'] = "--.";  alpha['H'] = "....";  alpha['I'] = ".."
        alpha['J'] = ".---"; alpha['K'] = "-.-";   alpha['L'] = ".-.."
        alpha['M'] = "--";   alpha['N'] = "-.";    alpha['O'] = "---"
        alpha['P'] = ".--."; alpha['Q'] = "--.-";  alpha['R'] = ".-."
        alpha['S'] = "...";  alpha['T'] = "-";     alpha['U'] = "..-"
        alpha['V'] = "...-"; alpha['W'] = ".--";   alpha['X'] = "-..-"
        alpha['Y'] = "-.--"; alpha['Z']  = "--.."

        alpha['.'] = ".-.-.-";  alpha[','] = "--..--";  alpha['?'] = "..--.."
        alpha['\''] = ".----."; alpha['!'] = "-.-.--";  alpha['/'] = "-..-."
        alpha['('] = "-.--.";   alpha[')'] = "-.--.-";  alpha['&'] = ".-..."
        alpha[':'] = "---...";  alpha[';'] = "-.-.-.";  alpha['='] = "-...-"
        alpha['+'] = ".-.-.";   alpha['-'] = "-....-";  alpha['_'] = "..--.-"
        alpha['"'] = ".-..-.";  alpha['$'] = "...-..-"; alpha['@'] = ".--.-."

        alpha['0'] = "-----"; alpha['1'] = ".----"; alpha['2'] = "..---"
        alpha['3'] = "...--"; alpha['4'] = "....-"; alpha['5'] = "....."
        alpha['6'] = "-...."; alpha['7'] = "--..."; alpha['8'] = "---.."
        alpha['9'] = "----."

    }

    private fun fillDotDash() {
        dotDash[".-"] = 'А'; dotDash["-..."] = 'Б'; dotDash[".--"] = 'В'; dotDash["--."] = 'Г'
        dotDash["-.."] = 'Д'; dotDash["."] = 'Е'; dotDash["...-"] = 'Ж'; dotDash["--.."] = 'З'
        dotDash[".."] = 'И'; dotDash[".---"] = 'Й'; dotDash["-.-"] = 'К'; dotDash[".-.."] = 'Л'
        dotDash["--"] = 'М'; dotDash["-."] = 'Н'; dotDash["---"] = 'О'; dotDash[".--."] = 'П'
        dotDash[".-."] = 'Р'; dotDash["..."] = 'С'; dotDash["-.-.--"] = '!'; dotDash["-"] = 'Т'
        dotDash[".-..-."] = '"'; dotDash["..-"] = 'У'; dotDash["..-."] = 'Ф'; dotDash["...-..-"] = '$'
        dotDash["...."] = 'Х'; dotDash["-.-."] = 'Ц'; dotDash[".-..."] = '&'; dotDash["---."] = 'Ч'
        dotDash[".----."] = '\''; dotDash["----"] = 'Ш'; dotDash["-.--."] = '('; dotDash["--.-"] = 'Щ'
        dotDash["-.--.-"] = ')'; dotDash["--.--"] = 'Ъ'; dotDash["-.--"] = 'Ы'; dotDash[".-.-."] = '+'
        dotDash["-..-"] = 'Ь'; dotDash["--..--"] = ','; dotDash["..-.."] = 'Э'; dotDash["-....-"] = '-'
        dotDash["..--"] = 'Ю'; dotDash[".-.-.-"] = '.'; dotDash[".-.-"] = 'Я'; dotDash["-..-."] = '/'
        dotDash["-----"] = '0'; dotDash[".----"] = '1'; dotDash["..---"] = '2'; dotDash["...--"] = '3'
        dotDash["....-"] = '4'; dotDash["....."] = '5'; dotDash["-...."] = '6'; dotDash["--..."] = '7'
        dotDash["---.."] = '8'; dotDash["----."] = '9'; dotDash["---..."] = ':'; dotDash["-.-.-."] = ';'
        dotDash["-...-"] = '='; dotDash["..--.."] = '?'; dotDash[".--.-."] = '@';
        dotDash["..--.-"] = '_'
    }

    private fun fillDelays() {
        delays['.'] = 250 ; delays['-'] = 750
    }

    init {
        this.fillAlpha()
        this.fillDotDash()
        this.fillDelays()
    }


    fun encode(text: String): List<List<List<Int>>> {
        val words = text.uppercase().split(" ")
        val encodedWords = ArrayList<ArrayList<ArrayList<Int>>>()
        for (word in words) {
            val encWord = ArrayList<ArrayList<Int>>()
            for (char in word) {
                val charCode = alpha[char]
                encWord.add(toDelayArray(charCode!!))
            }
            encodedWords.add(encWord)
        }

        return encodedWords
    }

    fun decode(message: List<Char>): Char? {
        var test = ""
        for (i in message){
            test += i.toString()
        }
        return dotDash[test]
    }

    private fun toDelayArray(morseCode: String) : ArrayList<Int> {
        val wordDelays = ArrayList<Int>(morseCode.length)
        for (sign in morseCode) {
            wordDelays.add(delays[sign]!!)
        }
        return wordDelays
    }
}