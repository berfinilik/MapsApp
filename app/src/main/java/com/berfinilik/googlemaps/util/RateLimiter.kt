package com.berfinilik.googlemaps

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RateLimiter(private val permitsPerSecond: Double) {
    private val scheduler = Executors.newScheduledThreadPool(1)

    fun <T> execute(action: () -> T): T {
        val future = scheduler.schedule(action, (3000 / permitsPerSecond).toLong(), TimeUnit.MILLISECONDS) // 3 saniye bekleme süresi
        return future.get()
    }
}