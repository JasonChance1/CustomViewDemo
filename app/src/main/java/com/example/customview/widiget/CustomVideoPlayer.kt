package com.example.customview.widiget

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.customview.R

class CustomVideoPlayer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var playerView: PlayerView
    private lateinit var customSeekBar: SeekBar
    private lateinit var btnPlayPause: ImageButton
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    
    private var exoPlayer: ExoPlayer? = null
    private val updateHandler = Handler(Looper.getMainLooper())
    private lateinit var updateRunnable: Runnable
    
    private var videoUri: Uri? = null
    private var isPlaying: Boolean = false
    private var isPrepared: Boolean = false
    
    // 播放状态监听器
    interface PlaybackListener {
        fun onVideoPrepared(duration: Long)
        fun onVideoStarted()
        fun onVideoPaused()
        fun onVideoCompleted()
        fun onError(error: Exception)
    }
    
    private var playbackListener: PlaybackListener? = null

    init {
        initView(context)
    }

    private fun initView(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_video_player, this, true)
        
        playerView = view.findViewById(R.id.playerView)
        customSeekBar = view.findViewById(R.id.customSeekBar)
        btnPlayPause = view.findViewById(R.id.btnPlayPause)
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime)
        tvTotalTime = view.findViewById(R.id.tvTotalTime)
        
        setupPlayer()
        setupControls()
        setupGestureDetector()
    }

    private fun setupPlayer() {
        val playerBuilder = ExoPlayer.Builder(context)
        exoPlayer = playerBuilder.build().apply {
            playerView.player = this
            playerView.useController = false
            
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            isPrepared = true
                            customSeekBar.max = duration.toInt()
                            tvTotalTime.text = formatTime(duration)
                            playbackListener?.onVideoPrepared(duration)
                            updatePlayPauseButton()
                            startProgressUpdate()
                        }
                        Player.STATE_ENDED -> {
                            this@CustomVideoPlayer.isPlaying = false
                            playbackListener?.onVideoCompleted()
                            updatePlayPauseButton()
                            stopProgressUpdate()
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    this@CustomVideoPlayer.isPlaying = isPlaying
                    if (isPlaying) {
                        playbackListener?.onVideoStarted()
                        startProgressUpdate()
                    } else {
                        playbackListener?.onVideoPaused()
                        stopProgressUpdate()
                    }
                    updatePlayPauseButton()
                }

                override fun onPlayerError(error: PlaybackException) {
                    playbackListener?.onError(error)
                }
            })
        }
    }

    private fun setupControls() {
        btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

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
                exoPlayer?.seekTo(seekBar?.progress?.toLong() ?: 0)
                if (isPlaying) {
                    startProgressUpdate()
                }
            }
        })
    }

    private fun setupGestureDetector() {
        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                // 可以添加双击暂停/播放功能
                togglePlayPause()
                return true
            }
        })

        playerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    // 公共方法
    fun setVideoUri(uri: Uri) {
        this.videoUri = uri
        exoPlayer?.setMediaItem(MediaItem.fromUri(uri))
        exoPlayer?.prepare()
    }

    fun setVideoPath(path: String) {
        setVideoUri(Uri.parse("file://$path"))
    }

    fun setVideoFile(path: String) {
        setVideoUri(Uri.fromFile(java.io.File(path)))
    }

    fun play() {
        if (!isPrepared && videoUri != null) {
            setVideoUri(videoUri!!)
        }
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun togglePlayPause() {
        if (isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    fun getDuration(): Long {
        return exoPlayer?.duration ?: 0
    }

    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0
    }

    fun isVideoPlaying(): Boolean {
        return isPlaying
    }

    fun setPlaybackListener(listener: PlaybackListener) {
        this.playbackListener = listener
    }

    fun release() {
        stopProgressUpdate()
        exoPlayer?.release()
        exoPlayer = null
    }

    // 私有方法
    private fun startProgressUpdate() {
        stopProgressUpdate() // 确保之前的更新被停止
        
        updateRunnable = object : Runnable {
            override fun run() {
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        val currentPosition = player.currentPosition.toInt()
                        customSeekBar.progress = currentPosition
                        tvCurrentTime.text = formatTime(currentPosition.toLong())
                        updateHandler.postDelayed(this, 1000)
                    }
                }
            }
        }
        updateHandler.post(updateRunnable)
    }

    private fun stopProgressUpdate() {
        updateHandler.removeCallbacksAndMessages(null)
    }

    private fun updatePlayPauseButton() {
        val iconRes = when {
            exoPlayer?.isPlaying == true -> R.drawable.ic_pause
            exoPlayer?.playbackState == Player.STATE_ENDED -> R.drawable.ic_replay
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
}