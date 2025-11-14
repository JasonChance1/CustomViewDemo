package com.example.customview.ui
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.customview.R
import java.io.File

class VideoPlayActivity : AppCompatActivity() {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var customSeekBar: SeekBar
    private lateinit var btnPlayPause: ImageButton
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView

    private val updateHandler = Handler(Looper.getMainLooper())
    private lateinit var updateRunnable: Runnable

    private lateinit var videoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        // 获取录制的视频路径
        videoPath = "${getExternalFilesDir(null)}/test.mp4"

        initializePlayer()
        setupCustomControls()
    }

    private fun getRecordedVideoPath(): String {
        // 根据你的录制逻辑获取视频路径
        return "${getExternalFilesDir(null)}/recorded_video.mp4"
    }

    private fun initializePlayer() {
        // 创建ExoPlayer实例 - Media3版本
        val playerBuilder = ExoPlayer.Builder(this)
        exoPlayer = playerBuilder.build()

        // 设置PlayerView
        playerView = findViewById(R.id.playerView)
        playerView.player = exoPlayer
        playerView.useController = false // 禁用默认控制器

        // 设置媒体源
        val mediaItem = MediaItem.fromUri(Uri.fromFile(File(videoPath)))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        // 监听播放器状态 - Media3的Listener接口有所变化
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        // 播放器准备就绪
                        customSeekBar.max = exoPlayer.duration.toInt()
                        tvTotalTime.text = formatTime(exoPlayer.duration)
                        updatePlayPauseButton()
                        startProgressUpdate()
                    }
                    Player.STATE_ENDED -> {
                        // 播放结束
                        updatePlayPauseButton()
                        stopProgressUpdate()
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayPauseButton()
                if (isPlaying) {
                    startProgressUpdate()
                } else {
                    stopProgressUpdate()
                }
            }
        })
    }

    private fun setupCustomControls() {
        customSeekBar = findViewById(R.id.customSeekBar)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)

        // 播放/暂停按钮
        btnPlayPause.setOnClickListener {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                if (exoPlayer.playbackState == Player.STATE_ENDED) {
                    exoPlayer.seekTo(0)
                }
                exoPlayer.play()
            }
        }

        // 进度条拖动
        customSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    tvCurrentTime.text = formatTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                stopProgressUpdate()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                exoPlayer.seekTo(seekBar?.progress?.toLong() ?: 0)
                if (exoPlayer.playbackState != Player.STATE_ENDED) {
                    startProgressUpdate()
                }
            }
        })

        // 添加双击全屏功能
        setupDoubleTapForFullscreen()
    }

    private fun setupDoubleTapForFullscreen() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                toggleFullscreen()
                return true
            }
        })

        playerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun toggleFullscreen() {
        if (isFullscreen()) {
            // 退出全屏
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            supportActionBar?.show()
        } else {
            // 进入全屏
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            supportActionBar?.hide()
        }
    }

    private fun isFullscreen(): Boolean {
        return requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    private fun startProgressUpdate() {
        updateRunnable = object : Runnable {
            override fun run() {
                if (exoPlayer.isPlaying) {
                    val currentPosition = exoPlayer.currentPosition.toInt()
                    customSeekBar.progress = currentPosition
                    tvCurrentTime.text = formatTime(currentPosition.toLong())
                    updateHandler.postDelayed(this, 1000)
                }
            }
        }
        updateHandler.post(updateRunnable)
    }

    private fun stopProgressUpdate() {
        updateHandler.removeCallbacks(updateRunnable)
    }

    private fun updatePlayPauseButton() {
        val iconRes = when {
            exoPlayer.isPlaying -> R.drawable.ic_pause
            exoPlayer.playbackState == Player.STATE_ENDED -> R.drawable.ic_replay
            else -> R.drawable.ic_play
        }
        btnPlayPause.setImageResource(iconRes)
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onResume() {
        super.onResume()
        if (::exoPlayer.isInitialized && !exoPlayer.isPlaying) {
            exoPlayer.play()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::exoPlayer.isInitialized) {
            exoPlayer.pause()
        }
        stopProgressUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::exoPlayer.isInitialized) {
            exoPlayer.release()
        }
        stopProgressUpdate()
    }
}