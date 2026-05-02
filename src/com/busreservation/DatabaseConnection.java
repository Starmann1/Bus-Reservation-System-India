// File: src/com/busreservation/DatabaseConnection.java
package com.busreservation;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import org.bson.Document;

public class DatabaseConnection {
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "java_mini_project";

    public static MongoDatabase getDatabase() {
        if (mongoClient == null) {
            try {
                mongoClient = MongoClients.create(CONNECTION_STRING);
                database = mongoClient.getDatabase(DATABASE_NAME);
                System.out.println("MongoDB connected successfully!");
                initializeCounters();
            } catch (Exception e) {
                System.err.println("MongoDB connection failed!");
                e.printStackTrace();
            }
        }
        return database;
    }

    private static void initializeCounters() {
        MongoCollection<Document> counters = database.getCollection("counters");
        if (counters.countDocuments(Filters.eq("_id", "userid")) == 0) {
            counters.insertOne(new Document("_id", "userid").append("seq", 0));
        }
        if (counters.countDocuments(Filters.eq("_id", "bookingid")) == 0) {
            counters.insertOne(new Document("_id", "bookingid").append("seq", 0));
        }
    }

    public static int getNextSequence(String sequenceName) {
        MongoCollection<Document> counters = getDatabase().getCollection("counters");
        Document result = counters.findOneAndUpdate(
                Filters.eq("_id", sequenceName),
                Updates.inc("seq", 1),
                new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        );
        return result.getInteger("seq");
    }

    public static void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            System.out.println("MongoDB connection closed.");
        }
    }
}