package com.example.coroutinestart

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.coroutinestart.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
            lifecycleScope.launch {
                loadData()
            }
//            loadDataWithoutCoroutine()
        }
    }

    private suspend fun loadData() {
        binding.progress.isVisible = true
        binding.buttonLoad.isEnabled = false
        val city = loadCity()
        binding.tvLocation.text = city
        val temp = loadTemperature(city)
        binding.tvTemperature.text = temp.toString()
        binding.progress.isVisible = false
        binding.buttonLoad.isEnabled = true
    }

    private suspend fun loadCity(): String {
        delay(5000)
        return "Moscow"
    }

    private suspend fun loadTemperature(city: String): Int {
        Toast.makeText(
            this,
            "Loading temperature for city: $city",
            Toast.LENGTH_SHORT
        ).show()
        delay(5000)
        return 17
    }


    //region Тот же самый код, но без Coroutine. Чтобы понять, как это работает под капотом.
    // По факту под капотом Coroutine все куда сложнее, но примерно так.

    private fun loadDataWithoutCoroutine(step: Int = 0, obj: Any? = null){
        when (step) {
            0 -> {
                binding.progress.isVisible = true
                binding.buttonLoad.isEnabled = false
                loadCityWithoutCoroutine {
                    loadDataWithoutCoroutine(1, it)
                }
            }
            1 -> {
                val city = obj as String
                binding.tvLocation.text = city
                loadTemperatureWithoutCoroutine(city){
                    loadDataWithoutCoroutine(2, it)
                }
            }
            2 -> {
                val temp = obj as Int
                binding.tvTemperature.text = temp.toString()
                binding.progress.isVisible = false
                binding.buttonLoad.isEnabled = true
            }
        }
    }

    // Тут специально написано чуть иначе. Вот пример, как работает "delay(5000)"
    private fun loadCityWithoutCoroutine(callback: (String) -> Unit){
        Handler(Looper.getMainLooper()).postDelayed({
            callback.invoke("Moscow")
        }, 5000)
    }

    private fun loadTemperatureWithoutCoroutine(city: String, callback: (Int) -> Unit) {
        thread {
            runOnUiThread{
                Toast.makeText(
                    this,
                    "Loading temperature for city: $city",
                    Toast.LENGTH_SHORT
                ).show()
            }
            Thread.sleep(5000)
            runOnUiThread{
                callback.invoke(17)
            }
        }
    }
    //endregion
}