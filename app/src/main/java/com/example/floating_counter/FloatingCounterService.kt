package com.example.floating_counter

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.IBinder
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide

class FloatingCounterService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private var counter = 0

    // Змінна для відтворення музики
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Інфлейтимо розмітку floating_counter.xml
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_counter, null)

        // Параметри вікна (оверлею)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

        // Обробка перетягування
        floatingView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })

        // Налаштування кнопок та тексту
        val tvCount = floatingView.findViewById<TextView>(R.id.tv_count)
        val btnPlus = floatingView.findViewById<Button>(R.id.btn_plus)
        val btnMinus = floatingView.findViewById<Button>(R.id.btn_minus)
        val congratsView = floatingView.findViewById<RelativeLayout>(R.id.congratulations)
        val animationText = floatingView.findViewById<TextView>(R.id.animationText)
        val catImage = floatingView.findViewById<ImageView>(R.id.catImage)

        // Завантажуємо GIF зображення за допомогою Glide
        Glide.with(this)
            .load(R.drawable.oiauia) // Ваш GIF
            .into(catImage)

        // Обробник для кнопки +
        btnPlus.setOnClickListener {
            counter++
            tvCount.text = counter.toString()
            updateEasterEgg(congratsView, animationText)
        }

        // Обробник для кнопки -
        btnMinus.setOnClickListener {
            counter--
            tvCount.text = counter.toString()
            updateEasterEgg(congratsView, animationText)
        }

        // Додаємо view в WindowManager
        windowManager.addView(floatingView, params)
    }

    /**
     * Метод перевіряє значення лічильника. Якщо воно рівне 77,
     * пасхалка показується та запускається музика. В іншому випадку,
     * пасхалка ховається, а музика зупиняється.
     */
    private fun updateEasterEgg(congratsView: RelativeLayout, animationText: TextView) {
        if (counter == 77) {
            congratsView.visibility = View.VISIBLE
            startRainbowAnimation(animationText)
            playEasterEggMusic()
        } else {
            congratsView.visibility = View.GONE
            stopEasterEggMusic()
        }
    }

    private fun startRainbowAnimation(textView: TextView) {
        // Створюємо веселкову анімацію для тексту
        val colorAnimator = ValueAnimator.ofArgb(
            Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA
        )
        colorAnimator.duration = 1000
        colorAnimator.repeatCount = ValueAnimator.INFINITE
        colorAnimator.addUpdateListener { animator ->
            textView.setTextColor(animator.animatedValue as Int)
        }

        // Створюємо рух тексту
        val moveAnimator = ObjectAnimator.ofFloat(textView, "translationX", -500f, 500f)
        moveAnimator.duration = 2000
        moveAnimator.repeatCount = ObjectAnimator.INFINITE
        moveAnimator.start()

        colorAnimator.start()
    }

    // Метод для запуску музики
    private fun playEasterEggMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.easter_egg)
            mediaPlayer?.isLooping = true  // За бажанням, щоб музика грала циклічно
        }
        mediaPlayer?.start()
    }

    // Метод для зупинки музики
    private fun stopEasterEggMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.reset()
                it.release()
            }
            mediaPlayer = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
        stopEasterEggMusic()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
