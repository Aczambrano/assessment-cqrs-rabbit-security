package ec.com.sofka.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackages = "ec.com.sofka.database.account",
        reactiveMongoTemplateRef = "accountMongoTemplate"
)
public class AccountMongoConfig {

    @Value("${spring.data.mongodb.accounts-uri}")
    private String bankMongoUri;


    @PostConstruct
    public void init() {
        System.out.println("Mongo URI: " + bankMongoUri);
    }

    @Primary
    @Bean(name = "accountsDatabaseFactory")
    public ReactiveMongoDatabaseFactory accountsDatabaseFactory() {
        MongoClient mongoClient = MongoClients.create(bankMongoUri);
        return new SimpleReactiveMongoDatabaseFactory(mongoClient, "cuenta_bancaria");
    }

    @Primary
    @Bean(name = "accountMongoTemplate")
    public ReactiveMongoTemplate accountMongoTemplate(@Qualifier("accountsDatabaseFactory") ReactiveMongoDatabaseFactory bankDatabaseFactory) {
        return new ReactiveMongoTemplate(bankDatabaseFactory);
    }
}

