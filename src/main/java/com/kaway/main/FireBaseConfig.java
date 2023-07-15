package com.kaway.main;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.kaway.main.KawayConstants;
import java.io.IOException;

@Configuration
public class FireBaseConfig {

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public MyFireBaseApp getMyFirebaseApp() throws IOException {
        return new MyFireBaseApp();
    }

    public class MyFireBaseApp {
        FirebaseApp firebaseApp;

        public MyFireBaseApp() throws IOException {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setProjectId(KawayConstants.PROJECT_ID)
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .build();
            firebaseApp = FirebaseApp.initializeApp(options);
        }

        public FirebaseApp getFirebaseApp(){
            return this.firebaseApp;
        }

    }
}
