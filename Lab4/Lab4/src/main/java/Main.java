import cn.operations.Operacoes;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

public class Main {
    public static void main(String[] args) throws Exception {
        FirestoreOptions options = FirestoreOptions
                .newBuilder().setProjectId("cn2526-t3-g12")
                .setDatabaseId("cn-g12-sv2026-firestore")
                .build();

        Firestore db = options.getService();
        String collectionName = "ocupacoes";

        Operacoes op = new Operacoes(db, collectionName);

        op.presentContent("Lab4-2044");
        op.deleteField("Lab4-2017", "location");
        op.simpleQuery("Marvila");
        op.query("Lab4-2044", "Marvila", "Desportivo");
        op.queryDates();
        op.querySpecificDates("02/02/2017", "09/02/2017");

    }

}
