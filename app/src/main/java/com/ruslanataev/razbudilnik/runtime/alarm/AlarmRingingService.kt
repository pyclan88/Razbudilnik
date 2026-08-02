package com.ruslanataev.razbudilnik.runtime.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.IBinder
import android.os.PowerManager
import com.ruslanataev.razbudilnik.runtime.alarm.recovery.AlarmRecoveryScheduler
import com.ruslanataev.razbudilnik.runtime.alarm.session.AlarmSessionStore
import com.ruslanataev.razbudilnik.runtime.alarm.volume.AlarmVolumeController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class AlarmRingingService : Service() {

    private var ringtone: Ringtone? = null
    private var isAlarmMuted: Boolean = false

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate,
    )

    private var volumeFadeJob: Job? = null

    private var volumeProtectionJob: Job? = null
    private var recoveryWatchdogJob: Job? = null

    private var currentVolume: Float = MUTED_VOLUME

    private var screenWakeLock: PowerManager.WakeLock? = null

    private lateinit var alarmVolumeController: AlarmVolumeController
    private lateinit var alarmRecoveryScheduler: AlarmRecoveryScheduler
    private lateinit var alarmSessionStore: AlarmSessionStore

    override fun onCreate() {
        super.onCreate()

        alarmVolumeController = AlarmVolumeController(this)
        alarmRecoveryScheduler = AlarmRecoveryScheduler(this)
        alarmSessionStore = AlarmSessionStore(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startAlarm(
                hour = intent.getIntExtra(EXTRA_HOUR, 7),
                minute = intent.getIntExtra(EXTRA_MINUTE, 0),
            )

            ACTION_ALARM_UI_VISIBLE -> showActiveAlarmNotification()

            ACTION_MUTE -> setAlarmMuted(true)
            ACTION_RESUME -> setAlarmMuted(false)
            ACTION_STOP -> stopAlarm()
            else -> stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        volumeFadeJob?.cancel()

        stopVolumeProtection()

        serviceScope.cancel()
        ringtone?.stop()
        ringtone = null

        releaseScreenWakeLock()
        super.onDestroy()
    }

    private fun startAlarm(hour: Int, minute: Int) {
        startVolumeProtection()

        wakeScreenIfNecessary()

        val notification = AlarmNotificationHelper(this).createAlarmNotification(hour, minute)

        startForeground(
            AlarmNotificationHelper.NOTIFICATION_ID_ALARM,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
        )

        alarmSessionStore.saveActiveSession(
            hour = hour,
            minute = minute,
        )

        startRecoveryWatchdog(hour, minute)

        if (ringtone?.isPlaying == true) {
            return
        }

        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        ringtone = RingtoneManager.getRingtone(this, ringtoneUri)?.apply {
            audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            isLooping = true

            volume = MUTED_VOLUME
            play()
        }

        currentVolume = MUTED_VOLUME

        fadeToVolume(
            targetVolume = if (isAlarmMuted) MUTED_VOLUME else AUDIBLE_VOLUME,
        )
    }

    private fun showActiveAlarmNotification() {
        val activeSession = alarmSessionStore.getActiveSession()

        if (activeSession == null) {
            stopSelf()
            return
        }

        val notification = AlarmNotificationHelper(this).createActiveAlarmNotification(
            hour = activeSession.hour,
            minute = activeSession.minute,
        )

        startForeground(
            AlarmNotificationHelper.NOTIFICATION_ID_ALARM,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
        )
    }

    private fun setAlarmMuted(isMuted: Boolean) {
        if (!isMuted) {
            alarmVolumeController.enforceProtectedVolume()
        }

        if (isAlarmMuted == isMuted) {
            return
        }

        isAlarmMuted = isMuted

        fadeToVolume(
            targetVolume = if (isMuted) MUTED_VOLUME else AUDIBLE_VOLUME,
        )
    }

    private fun stopAlarm() {
        stopRecoveryWatchdog()

        alarmSessionStore.clearActiveSession()

        isAlarmMuted = false

        fadeToVolume(
            targetVolume = MUTED_VOLUME,
            onFinished = ::finishStoppingAlarm,
        )
    }

    private fun finishStoppingAlarm() {
        ringtone?.stop()
        ringtone = null
        currentVolume = MUTED_VOLUME

        releaseScreenWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun fadeToVolume(
        targetVolume: Float,
        onFinished: () -> Unit = {},
    ) {
        volumeFadeJob?.cancel()

        val activeRingtone = ringtone

        if (activeRingtone == null || currentVolume == targetVolume) {
            currentVolume = targetVolume
            activeRingtone?.volume = targetVolume
            onFinished()
            return
        }

        val startVolume = currentVolume

        volumeFadeJob = serviceScope.launch {
            repeat(VOLUME_FADE_STEP_COUNT) { stepIndex ->
                val progress =
                    (stepIndex + 1).toFloat() / VOLUME_FADE_STEP_COUNT

                currentVolume = startVolume + (targetVolume - startVolume) * progress

                activeRingtone.volume = currentVolume
                delay(VOLUME_FADE_DELAY_MILLIS.milliseconds)
            }

            volumeFadeJob = null
            onFinished()
        }
    }

    @Suppress("DEPRECATION")
    private fun wakeScreenIfNecessary() {
        val powerManager = getSystemService(PowerManager::class.java)

        if (powerManager.isInteractive) {
            return
        }

        screenWakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "$packageName:alarm-screen",
        ).apply {
            acquire(SCREEN_WAKE_TIMEOUT_MILLIS)
        }
    }

    private fun releaseScreenWakeLock() {
        screenWakeLock
            ?.takeIf(PowerManager.WakeLock::isHeld)
            ?.release()

        screenWakeLock = null
    }

    private fun startVolumeProtection() {
        alarmVolumeController.startProtection()

        if (volumeProtectionJob?.isActive == true) {
            return
        }

        volumeProtectionJob = serviceScope.launch {
            while (true) {
                delay(VOLUME_PROTECTION_CHECK_INTERVAL_MILLIS.milliseconds)
                alarmVolumeController.enforceProtectedVolume()
            }
        }
    }

    private fun stopVolumeProtection() {
        volumeProtectionJob?.cancel()
        volumeProtectionJob = null

        alarmVolumeController.stopProtection()
    }

    private fun startRecoveryWatchdog(hour: Int, minute: Int) {
        recoveryWatchdogJob?.cancel()
        alarmRecoveryScheduler.postponeRecovery(hour, minute)

        recoveryWatchdogJob = serviceScope.launch {
            while (true) {
                delay(RECOVERY_WATCHDOG_REFRESH_INTERVAL_MILLIS.milliseconds)
                alarmRecoveryScheduler.postponeRecovery(hour, minute)
            }
        }
    }

    private fun stopRecoveryWatchdog() {
        recoveryWatchdogJob?.cancel()
        recoveryWatchdogJob = null
        alarmRecoveryScheduler.cancelRecovery()
    }

    companion object {
        private const val ACTION_START = "com.ruslanataev.razbudilnik.action.START_ALARM"
        private const val ACTION_ALARM_UI_VISIBLE =
            "com.ruslanataev.razbudilnik.action.ALARM_UI_VISIBLE"
        private const val ACTION_MUTE = "com.ruslanataev.razbudilnik.action.MUTE_ALARM"
        private const val ACTION_RESUME = "com.ruslanataev.razbudilnik.action.RESUME_ALARM"
        private const val ACTION_STOP = "com.ruslanataev.razbudilnik.action.STOP_ALARM"
        private const val EXTRA_HOUR = "extra_hour"
        private const val EXTRA_MINUTE = "extra_minute"
        private const val SCREEN_WAKE_TIMEOUT_MILLIS = 10_000L
        private const val MUTED_VOLUME = 0f
        private const val AUDIBLE_VOLUME = 1f
        private const val VOLUME_FADE_STEP_COUNT = 20
        private const val VOLUME_FADE_DELAY_MILLIS = 25L
        private const val VOLUME_PROTECTION_CHECK_INTERVAL_MILLIS = 250L
        private const val RECOVERY_WATCHDOG_REFRESH_INTERVAL_MILLIS = 2_000L

        fun createStartIntent(context: Context, hour: Int, minute: Int) =
            Intent(context, AlarmRingingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_HOUR, hour)
                putExtra(EXTRA_MINUTE, minute)
            }

        fun createAlarmUiVisibleIntent(context: Context) =
            Intent(context, AlarmRingingService::class.java).apply {
                action = ACTION_ALARM_UI_VISIBLE
            }

        fun createMuteIntent(context: Context) =
            Intent(context, AlarmRingingService::class.java).apply {
                action = ACTION_MUTE
            }

        fun createResumeIntent(context: Context) =
            Intent(context, AlarmRingingService::class.java).apply {
                action = ACTION_RESUME
            }

        fun createStopIntent(context: Context) =
            Intent(context, AlarmRingingService::class.java).apply {
                action = ACTION_STOP
            }
    }
}
