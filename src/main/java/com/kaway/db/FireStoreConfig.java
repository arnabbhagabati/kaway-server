package com.kaway.db;

import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.kaway.main.KawayConstants;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Configuration
@Component
public class FireStoreConfig {

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public FireStoreContainer dbContainer() throws IOException {
        return new FireStoreContainer();
    }

    public class FireStoreContainer {
        public Firestore db;

        FireStoreContainer() throws IOException {
            FirestoreOptions firestoreOptions =
                    FirestoreOptions.getDefaultInstance().toBuilder()
                            .setProjectId(KawayConstants.PROJECT_ID)
                            .setCredentials(GoogleCredentials.getApplicationDefault())
                            .build();
            db = firestoreOptions.getService();

        }
    }

}
