package com.solt.thiochat.data.Friends.Messages

import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class FriendMessagesDAO @Inject constructor() {
    @Inject lateinit var firestore: FirebaseFirestore

    fun sendMessage(){}
}