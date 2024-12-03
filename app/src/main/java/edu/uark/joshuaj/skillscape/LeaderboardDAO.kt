package edu.uark.joshuaj.skillscape

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class LeaderboardDAO {

    companion object {
        private const val COLLECTION_NAME = "leaderboards"
    }

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    suspend fun updateUserScore(gameId: String, uid: String, username: String, newScore: Int) {
        val leaderboardRef = db.collection(COLLECTION_NAME).document(gameId)
            .collection("scores").document(uid)

        val snapshot = leaderboardRef.get().await()
        if (snapshot.exists()) {
            val existingScore = snapshot.getLong("score")?.toInt() ?: 0
            if (newScore > existingScore) {
                // Update with the new high score
                leaderboardRef.set(LeaderboardEntry(uid, username, newScore)).await()
            }
        } else {
            // Create new entry
            leaderboardRef.set(LeaderboardEntry(uid, username, newScore)).await()
        }
    }

    fun getTopScores(gameId: String, limit: Long = 10): Query {
        return db.collection(COLLECTION_NAME).document(gameId)
            .collection("scores")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(limit)
    }

    suspend fun getUserRankAndScore(gameId: String, uid: String): Pair<Int, Int> {
        if (uid.isBlank()) {
            throw IllegalArgumentException("User ID cannot be empty")
        }

        val scoresRef = db.collection(COLLECTION_NAME).document(gameId)
            .collection("scores")

        val userSnapshot = scoresRef.document(uid).get().await()
        val userScore = userSnapshot.getLong("score")?.toInt() ?: 0

        val allScoresSnapshot = scoresRef.orderBy("score", Query.Direction.DESCENDING).get().await()
        var rank = 0
        var position = 0

        for (doc in allScoresSnapshot.documents) {
            position++
            if (doc.id == uid) {
                rank = position
                break
            }
        }

        return Pair(rank, userScore)
    }
}
