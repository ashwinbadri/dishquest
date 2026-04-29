package com.example.dishquest.data.repository

import com.example.dishquest.data.model.Dish
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirestoreDishRepository : DishRepository {

    // Verify this matches your Firestore collection name
    private val collection = Firebase.firestore.collection("dishes")

    override suspend fun getRandomDish(): Dish {
        val tried = mutableSetOf<Int>()
        while (tried.size < 323) {
            val id = (1..323).random()
            if (!tried.add(id)) continue
            val doc = suspendCoroutine { continuation ->
                collection.document(id.toString()).get()
                    .addOnSuccessListener { continuation.resume(it) }
                    .addOnFailureListener { continuation.resumeWithException(it) }
            }
            val dish = doc.toDish()
            if (dish != null) return dish
        }
        throw Exception("No dishes found in Firestore")
    }

    override suspend fun getDishById(id: String): Dish = suspendCoroutine { continuation ->
        collection.document(id).get()
            .addOnSuccessListener { doc ->
                val dish = doc.toDish()
                if (dish != null) continuation.resume(dish)
                else continuation.resumeWithException(Exception("Dish not found"))
            }
            .addOnFailureListener { continuation.resumeWithException(it) }
    }
}

private fun DocumentSnapshot.toDish(): Dish? {
    return try {
        Dish(
            id = this.id,
            name = getString("name") ?: return null,
            description = getString("description") ?: "",
            cuisine = getString("cuisine") ?: "",
            searchQuery = getString("searchQuery") ?: "",
            tags = get("tags") as? List<String> ?: emptyList(),
            ingredients = get("ingredients") as? List<String> ?: emptyList(),
            variants = get("variants") as? List<String> ?: emptyList(),
            origin = getString("origin") ?: "",
            history = getString("history") ?: "",
            howToEat = getString("howToEat") ?: "",
            shortDescription = getString("shortDescription") ?: "",
            availabilityTier = getString("availabilityTier") ?: ""
        )
    } catch (e: Exception) {
        null
    }
}
