package forumpubsub;


import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.gson.Gson;
import com.google.pubsub.v1.PubsubMessage;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MessageReceiveHandler implements MessageReceiver {

    Firestore db;
    String topicId;
    String receiverName;

    public MessageReceiveHandler(Firestore db, String topicId, String receiverName){
        this.db = db;
        this.topicId = topicId;
        this.receiverName = receiverName;
    }

    @Override
    public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
        /*System.out.println("Message (Id:" + message.getMessageId() +
                " Data:"+message.getData().toStringUtf8()+")");
            // acknowledge only after message processed
        consumer.ack(); // positive acknowledge*/

        String json = message.getData().toStringUtf8();
        Gson gson = new Gson();

        ForumMessage fMessage = gson.fromJson(json, ForumMessage.class);

        Map<String, Object> doc = new HashMap<>();

        doc.put("author", fMessage.author);
        doc.put("text", fMessage.text);
        doc.put("topicName", topicId);
        doc.put("receiverName", receiverName);
        doc.put("date", Timestamp.now());

        try {
            db.collection("forum-message")
                    .document()
                    .set(doc)
                    .get();

            consumer.ack();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}

