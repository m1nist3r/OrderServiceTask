package com.m1nist3r.order.configuration;


import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.lang.NonNull;

@EnableReactiveMongoRepositories
public class MongoReactiveConfiguration extends AbstractReactiveMongoConfiguration {

    @Value(value = "${database.name}")
    private String databaseName;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create();
    }

    @Override
    @NonNull
    protected String getDatabaseName() {
        return this.databaseName;
    }
}
