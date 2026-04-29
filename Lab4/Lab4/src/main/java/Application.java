import cn.operations.Operacoes;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Scanner;

public class Application {

    Firestore db;
    String KEY_JSON = "C:\\Users\\paulo\\Desktop\\ISEL\\8_Semestre\\CN\\Lab4\\cn2526-t3-g12-da87b416c35c.json";
    String CSV_PATH = "C:\\Users\\paulo\\Desktop\\ISEL\\8_Semestre\\CN\\Lab4\\Lab4\\OcupacaoEspacosPublicos.csv";

    public Application() throws Exception {
        InputStream serviceAccount = new FileInputStream(KEY_JSON);
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

        FirestoreOptions options = FirestoreOptions
                .newBuilder().setDatabaseId("cn-g12-sv2026-firestore").setCredentials(credentials)
                .build();

        db = options.getService();

        Operacoes.insertDocuments(CSV_PATH, db, "ocupacoes");
    }

    static int Menu() {
        Scanner scan = new Scanner(System.in);
        int option;
        do {
            System.out.println("######## MENU ##########");
            System.out.println("Aplicação de interação com a base de dados Firestore:");
            System.out.println(" 0: Apresentar o conteúdo de um documento");
            System.out.println(" 1: Apagar um campo de um documento");
            System.out.println(" 2: Obter todos os documentos de uma determinada freguesia");
            System.out.println(" 3: Interrogação composta");
            System.out.println(" 4: Obter documentos com eventos de fevereiro 2017");
            System.out.println(" 5: Obter documentos com eventos entre duas datas");
            System.out.println("..........");
            System.out.println("99: Exit");
            System.out.print("Enter an Option: ");
            option = scan.nextInt();
        } while (!((option >= 0 && option <= 5) || option == 99));
        return option;
    }

}




