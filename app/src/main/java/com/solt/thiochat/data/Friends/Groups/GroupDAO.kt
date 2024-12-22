package com.solt.thiochat.data.Friends.Groups

import com.google.firebase.firestore.FirebaseFirestore
import com.solt.thiochat.data.OperationResult
import javax.inject.Inject
const val GROUP_COLLECTION = "group"
class GroupDAO @Inject constructor() {
  @Inject lateinit var firestore: FirebaseFirestore


}