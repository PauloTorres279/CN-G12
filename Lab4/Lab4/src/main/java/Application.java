import cn.operations.DocumentProcessor;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Application {

    Firestore db;
    String KEY_JSON = "C:\\Users\\paulo\\Desktop\\ISEL\\8_Semestre\\CN\\Lab4\\cn2526-t3-g12-eb5239a35f30.json";
    String CSV_PATH = "C:\\Users\\paulo\\Desktop\\ISEL\\8_Semestre\\CN\\Lab4\\Lab4\\OcupacaoEspacosPublicos.csv";

    public Application() throws Exception {
        InputStream serviceAccount = new FileInputStream(KEY_JSON);
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

        FirestoreOptions options = FirestoreOptions
                .newBuilder().setDatabaseId("cn-g12-sv2026-firestore").setCredentials(credentials)
                .build();

        db = options.getService();

        DocumentProcessor.insertDocuments(CSV_PATH, db, "ocupacoes");
    }

    public static void main(String[] args){
        try{
            new Application();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}




