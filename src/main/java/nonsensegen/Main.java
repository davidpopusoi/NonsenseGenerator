package nonsensegen;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");

        try {
            String path = "C:/Users/User/Downloads/credentials.json";
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(path));
            System.out.println("Credentials loaded successfully from: " + path);
        } catch (IOException e) {
            System.err.println("Error loading credentials. " + e.getMessage());
        }
    }
}