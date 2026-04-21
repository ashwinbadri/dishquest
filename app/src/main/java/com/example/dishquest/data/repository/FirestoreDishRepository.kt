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

    override suspend fun getRandomDish(): Dish = suspendCoroutine { continuation ->
        collection.get()
            .addOnSuccessListener { snapshot ->
                val dishes = snapshot.documents.mapNotNull { it.toDish() }
                if (dishes.isEmpty()) {
                    continuation.resumeWithException(Exception("No dishes found in Firestore"))
                } else {
                    continuation.resume(dishes.random())
                }
            }
            .addOnFailureListener { continuation.resumeWithException(it) }
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
            ingredients = get("ingredients") as? List<String> ?: emptyList()
        )
    } catch (e: Exception) {
        null
    }
}
