package com.example.dnd35.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/**
 * Registro durável da sessão hospedada neste aparelho.
 *
 * O snapshot é um documento versionado produzido pelo domínio da sessão. Não contém sockets,
 * Bitmap, Flow ou outros objetos de runtime. O registro é singleton para impedir que duas
 * sessões locais concorram pela recuperação do mesmo servidor.
 */
@Entity(tableName = "hosted_session")
data class HostedSessionEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val sessionId: String,
    val hostDisplayName: String,
    /** ACTIVE ou ENDED. Somente ACTIVE pode ser recuperada após recriar o processo. */
    val status: String,
    val snapshotVersion: Int,
    val snapshotJson: String,
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val SINGLETON_ID = 1
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_ENDED = "ENDED"
        const val CURRENT_SNAPSHOT_VERSION = 1
    }
}

@Dao
interface HostedSessionDao {
    @Query("SELECT * FROM hosted_session WHERE id = 1 AND status = 'ACTIVE' LIMIT 1")
    suspend fun getRecoverableSession(): HostedSessionEntity?

    @Query("SELECT * FROM hosted_session WHERE id = 1 LIMIT 1")
    suspend fun getStoredSession(): HostedSessionEntity?

    /** Substitui o checkpoint inteiro em uma única transação Room. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCheckpoint(session: HostedSessionEntity)

    /**
     * Marca a sessão como encerrada sem deixar snapshot recuperável.
     * A condição pelo sessionId evita encerrar acidentalmente uma sessão criada depois.
     */
    @Query("""
        UPDATE hosted_session
        SET status = 'ENDED', snapshotJson = '', updatedAt = :endedAt
        WHERE id = 1 AND sessionId = :sessionId
    """)
    suspend fun markManuallyEnded(sessionId: String, endedAt: Long = System.currentTimeMillis()): Int

    @Query("DELETE FROM hosted_session WHERE id = 1")
    suspend fun clearStoredSession()

    /** Confirma que uma escrita de checkpoint é uma operação transacional do banco. */
        suspend fun replaceCheckpoint(session: HostedSessionEntity) {
        saveCheckpoint(session)
    }
}
