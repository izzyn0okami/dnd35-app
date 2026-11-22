package com.example

import com.example.dnd35.model.CharacterInventoryItem
import com.example.dnd35.session.SessionServer
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SessionServerShutdownTest {

    @Test
    fun `captura final suspende sem bloquear chamador quando checkpoint detem stateLock`() = runBlocking {
        val checkpointStarted = CountDownLatch(1)
        val releaseCheckpoint = CountDownLatch(1)
        val server = SessionServer(
            sessionId = "session-shutdown-lock",
            hostDisplayName = "Mestre",
            onSnapshotChanged = {
                checkpointStarted.countDown()
                check(releaseCheckpoint.await(8, TimeUnit.SECONDS)) {
                    "O teste não liberou o checkpoint a tempo."
                }
            }
        )
        val executor = Executors.newSingleThreadExecutor()
        try {
            val item = CharacterInventoryItem(name = "Espada longa", category = "Armas")
            val mutation = executor.submit { server.addLootItem(item) }

            assertTrue(
                "A mutação não chegou ao checkpoint que mantém stateLock.",
                checkpointStarted.await(5, TimeUnit.SECONDS)
            )

            // UNDISTPATCHED faz a chamada começar imediatamente. A função deve suspender
            // ao despachar a captura para I/O, em vez de bloquear esta thread aguardando o lock.
            val shutdown = async(start = CoroutineStart.UNDISPATCHED) {
                server.stopAndCreateSnapshotOnIo()
            }
            assertFalse(
                "A captura final bloqueou o chamador enquanto aguardava stateLock.",
                shutdown.isCompleted
            )

            releaseCheckpoint.countDown()
            val snapshot = withTimeout(8_000) { shutdown.await() }
            mutation.get(8, TimeUnit.SECONDS)

            assertEquals("Espada longa", snapshot.lootChest.single().name)
        } finally {
            releaseCheckpoint.countDown()
            executor.shutdownNow()
            server.stop()
        }
    }
}
