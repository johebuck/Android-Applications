package com.example.runes.hangman

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import org.json.JSONObject
import java.util.Random


import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main2.*

class Main2Activity : AppCompatActivity() {

    var numWrong = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        val words = loadDictionary()
        val randNum = rand()
        val randWord = buildWord(randNum.toString(), words)

        val charCount = randWord.length
        var newWord : String  = ""
        val letter = 'x'
        var i = 0
        while(i < charCount){
            newWord = newWord + letter.toString()
            i++
        }
        display(randWord)
        randWordText.text = newWord

        playAgain.visibility = View.GONE
        baseWrong.visibility = View.VISIBLE
        firstWrong.visibility = View.GONE
        secondWrong.visibility = View.GONE
        lastWrong.visibility = View.GONE

        guessLetterButton.setOnClickListener {_ ->
            val guess = inputText.text.toString()

            if (guess.length == 1){
                if (guess !in randWord)
                    wrongGuess()
                else if(guess in randWord)
                    checkLetters(randWord, guess)
            }
        }

        guessWordButton.setOnClickListener{_ ->
            val guessWord = inputText.text.toString()

            if (guessWord != randWord) {
                playAgain.visibility = View.VISIBLE
                guessWordButton.visibility = View.GONE
                guessLetterButton.visibility = View.GONE
            }
            else {
                randWordText.text = randWord
                winGame()
            }
        }

        playAgain.setOnClickListener{
            val intent = Intent(this, Main2Activity::class.java)
            startActivity(intent)
        }

    }

    private fun loadDictionary() : JSONObject {
        val filePath = "dictionary.json"
        val json_string = application.assets.open(filePath).bufferedReader().use{
            it.readText()
        }
        val jsonObj = JSONObject(json_string.substring(json_string.indexOf("{"), json_string.lastIndexOf("}") + 1))

        return jsonObj
    }

    fun checkLetters(rand: String, guess: String) {
        val currentWord = randWordText.text.toString()
        val userGuess = guess.single()
        var newWord : String = ""
        var letter = 'x'

        var i = 0
        while (i < rand.length){
            if(rand[i].toString() == guess){
                newWord += guess
            }
            else if(currentWord[i] != letter || currentWord[i] != userGuess){
                newWord += currentWord[i]
            }
            else{
                newWord += letter
            }
            i++
        }
        randWordText.text = newWord
    }

    fun buildWord(key : String, jsonObj : JSONObject) : String{

        return jsonObj[key] as String

    }

    val random = Random()

    fun rand() : Int {
        return random.nextInt(10 - 0)
    }


    fun display(word:String) {
        Toast.makeText(baseContext, "$word.", Toast.LENGTH_SHORT).show()
    }

    fun wrongGuess() {
        numWrong += 1
        if (numWrong == 1) {
            playAgain.visibility = View.GONE
            baseWrong.visibility = View.GONE
            firstWrong.visibility = View.VISIBLE
            secondWrong.visibility = View.GONE
            lastWrong.visibility = View.GONE
        }
        if (numWrong == 2) {
            playAgain.visibility = View.GONE
            baseWrong.visibility = View.GONE
            firstWrong.visibility = View.GONE
            secondWrong.visibility = View.VISIBLE
            lastWrong.visibility = View.GONE
        }
        if (numWrong == 3) {
            playAgain.visibility = View.VISIBLE
            baseWrong.visibility = View.GONE
            firstWrong.visibility = View.GONE
            secondWrong.visibility = View.GONE
            lastWrong.visibility = View.VISIBLE
        }
        display("wrong")
    }

    fun winGame() {
        // TODO
        playAgain.visibility = View.VISIBLE
        guessWordButton.visibility = View.GONE
        guessLetterButton.visibility = View.GONE
        randWordText.visibility = View.GONE
        display("You Win!")
    }
}
