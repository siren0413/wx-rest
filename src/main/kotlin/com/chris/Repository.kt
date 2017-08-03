package com.chris

import com.mongodb.MongoClient
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.Morphia
import org.springframework.stereotype.Component

@Component
class MongoRepository {

    final val datastore: Datastore

    init {
        val morphia = Morphia()
        morphia.mapPackage("com.chris")
        datastore = morphia.createDatastore(MongoClient("192.168.1.201"), "identity")
        datastore.ensureIndexes()
    }

}