package com.example.coroutinestart

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.coroutinestart.databinding.ActivityMainBinding
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {


    //region Комментарий
    /*
    1. В целях решения проблемы с передачей данных из субпотоков в главный используется класс: Handler.
    Объект этого класса можно создать на главном потоке (Main-thread), а затем из субпотоков
    (Background thread), ему можно передать объекты Runnable.
    И тогда метод Runnable.run() будет вызван на главном потоке.

    2. Кстати, важно. Чтобы создать отдельную ветку в субпотоке, то нужно вызывать метод:
        Looper.prepare() - лучше почитать, если буду использовать.
    */
    //endregion
    //region sendMessage
    /*
    Так же handler может принимать не только runnable, но и объект message.
    Но чтобы принимать значения, нужно переопределить метод handleMessage при создании handler

    private val handler = object : Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            println("HANDLE_msg $msg")
        }
    }

    handler.sendMessage(Message.obtain(handler, 0, 17))
    */
    //endregion

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.buttonLoad.setOnClickListener {
            loadData()
        }
    }

    private fun loadData() {
        binding.progress.isVisible = true
        binding.buttonLoad.isEnabled = false
        loadCity { it ->
            binding.tvLocation.text = it
            loadTemperature(it){
                binding.tvTemperature.text = it.toString()
                binding.progress.isVisible = false
                binding.buttonLoad.isEnabled = true
            }
        }
    }

    private fun loadCity(callback: (String) -> Unit) {
        thread {
            Thread.sleep(5000)
            runOnUiThread {
                callback.invoke("Moscow")
            }
        }
    }

    private fun loadTemperature(city: String, callback: (Int) -> Unit) {
        thread {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    this,
                    "Loading temperature for city: $city",
                    Toast.LENGTH_SHORT
                ).show()
            }
            Thread.sleep(5000)
            //region runOnUiThread - что это?
            /*
            Конструкцию "Handler(Looper.getMainLooper()).post" можно упростить аналогичным методом:
                runOnUiThread (Переводится как: "запусти на главном потоке")
            */
            //endregion
            runOnUiThread {   // Пример для слов выше
                callback.invoke(17)
            }
        }
    }
}