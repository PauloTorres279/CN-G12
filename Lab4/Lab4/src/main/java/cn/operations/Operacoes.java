package cn.operations;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class Operacoes {
    private Firestore db;
    private String collectionName;

    public Operacoes(Firestore db, String collectionName){
        this.db = db;
        this.collectionName = collectionName;
    }

    public static OcupacaoTemporaria convertLineToObject (String line) throws ParseException {
        String[] cols = line.split(",");
        OcupacaoTemporaria ocup = new OcupacaoTemporaria();
        ocup.ID = Integer.parseInt(cols[0]);
        ocup.location = new Localizacao();
        ocup.location.point = new GeoPoint(Double.parseDouble(cols[1]), Double.parseDouble(cols[2]));
        ocup.location.coord = new Coordenadas();
        ocup.location.coord.X = Double.parseDouble(cols[1]);
        ocup.location.coord.Y = Double.parseDouble(cols[2]);
        ocup.location.freguesia = cols[3];
        ocup.location.local = cols[4];
        ocup.event = new Evento();
        ocup.event.evtID = Integer.parseInt(cols[5]);
        ocup.event.nome = cols[6];
        ocup.event.tipo = cols[7];
        ocup.event.details = new HashMap<String, String>();
        if (!cols[8].isEmpty()) ocup.event.details.put("Participantes", cols[8]);
        if (!cols[9].isEmpty()) ocup.event.details.put("Custo", cols[9]);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        ocup.event.dtInicio = formatter.parse(cols[10]);
        ocup.event.dtFinal = formatter.parse(cols[11]);
        ocup.event.licenciamento = new Licenciamento();
        ocup.event.licenciamento.code = cols[12];
        ocup.event.licenciamento.dtLicenc = formatter.parse(cols[13]);
        return ocup;
    }

    public static void insertDocuments(String pathnameCSV, Firestore db, String collectionName) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(pathnameCSV));
        CollectionReference colRef = db.collection(collectionName);
        String line;
        while ((line = reader.readLine()) != null) {
            OcupacaoTemporaria ocup = convertLineToObject(line);
            DocumentReference docRef = colRef.document("Lab4-" + ocup.ID);
            ApiFuture<WriteResult> resultFut = docRef.set(ocup);
            WriteResult result = resultFut.get();
            System.out.println("Update time : " + result.getUpdateTime());
        }
    }

    public void presentContent(String docId) throws ExecutionException, InterruptedException {
        System.out.println("Operação Alínea A: ");
        DocumentReference docRef = db.collection(collectionName).document(docId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        System.out.println("Document content: " + document.getData());

    }

    public void deleteField(String docId, String fieldToDelete) throws ExecutionException, InterruptedException {
        System.out.println("Operação Alínea B: ");
        DocumentReference docRef = db.collection(collectionName).document(docId);
        ApiFuture<WriteResult> writeResult = docRef.update(fieldToDelete, FieldValue.delete());
        System.out.println("Campo apagado: "+ writeResult.get().getUpdateTime());
    }

    public void simpleQuery(String freguesia) throws ExecutionException, InterruptedException {
        System.out.println("Operação Alínea C: ");
        Query query = db.collection(collectionName)
                .whereEqualTo("location.freguesia", freguesia);
        // retrieve query results asynchronously using query.get()
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot doc: querySnapshot.get().getDocuments()) {
            System.out.print("DocID: " + doc.getId());
            System.out.println(" Freguesia: " + doc.get("location.freguesia"));
        }
    }

    public void query(String id, String freguesia, String event) throws ExecutionException, InterruptedException {
        System.out.println("Operação Alínea D: ");
        Query query = db.collection(collectionName)
                .whereGreaterThan("event.evtID", id)
                .whereEqualTo("location.freguesia", freguesia)
                .whereEqualTo("event.tipo", event);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot doc: querySnapshot.get().getDocuments()) {
            System.out.print("DocID: " + doc.getId());
            System.out.println(" Freguesia: " + doc.getString("location.freguesia") + " - Evento: " + doc.getString("event.tipo"));
        }
    }

    public void queryDates() throws ParseException, ExecutionException, InterruptedException {
        System.out.println("Operação alínea E: ");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date dtInicio = sdf.parse("31/01/2017");
        Date dtFim = sdf.parse("01/03/2017");

        Query query = db.collection(collectionName)
                .whereGreaterThan("event.dtInicio", dtInicio)
                .whereLessThan("event.dtFinal", dtFim);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot doc: querySnapshot.get().getDocuments()) {
            System.out.print("DocID: " + doc.getId());
            System.out.println(" Freguesia: " + doc.getString("location.freguesia") + " - Evento: " + doc.getString("event.tipo"));
        }
    }

    public void querySpecificDates(String dtInicio, String dtFim) throws ParseException, ExecutionException, InterruptedException {
        System.out.println("Operação alínea F: ");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date dtInicioParsed = sdf.parse(dtInicio);
        Date dtFimParsed = sdf.parse(dtFim);


        Query query = db.collection(collectionName)
                .whereGreaterThan("event.dtInicio", dtInicioParsed)
                .whereLessThan("event.dtFinal", dtFimParsed);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot doc: querySnapshot.get().getDocuments()) {
            System.out.print("DocID: " + doc.getId());
            System.out.println(" Freguesia: " + doc.getString("location.freguesia") + " - Evento: " + doc.getString("event.tipo"));
        }


    }
}
