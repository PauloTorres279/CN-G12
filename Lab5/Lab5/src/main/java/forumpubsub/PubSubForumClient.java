package forumpubsub;


import com.google.api.core.ApiFuture;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.*;

import java.io.IOException;
import java.util.Scanner;

public class PubSubForumClient {
    static String projectId;

    private static String read(String msg, Scanner input) {
        System.out.println(msg);
        return input.nextLine();
    }

    public static void listTopics() throws IOException {
        TopicAdminClient topicAdmin = TopicAdminClient.create();
        TopicAdminClient.ListTopicsPagedResponse res = topicAdmin.listTopics(ProjectName.of(projectId));
        for (Topic top : res.iterateAll()) {
            System.out.println("Topic Name=" + top.getName());
        }
        topicAdmin.close();
    }

    public static void publishMessage(String topicID, String jsonData) throws Exception {
        TopicName topicName = TopicName.ofProjectTopicName(projectId, topicID);
        Publisher publisher = Publisher.newBuilder(topicName).build();
        ByteString msgData = ByteString.copyFromUtf8(jsonData);
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
            .setData(msgData)
            .build();
        ApiFuture<String> future = publisher.publish(pubsubMessage);
        String msgID = future.get();
        System.out.println("Message Published with ID=" + msgID);
        publisher.shutdown();
    }

    public static void createSubscription(String topicID, String subscriptionID) throws IOException {
        TopicName topicName = TopicName.of(projectId, topicID);
        SubscriptionName subscriptionName = SubscriptionName.of(projectId, subscriptionID);
        SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create();
        PushConfig pConfig = PushConfig.getDefaultInstance();
        subscriptionAdminClient.createSubscription(subscriptionName, topicName, pConfig, 0);
        subscriptionAdminClient.close();
    }

    public static void deleteSubscription(String subscriptionID) throws IOException {
        SubscriptionName subscriptionName = SubscriptionName.of(projectId, subscriptionID);
        SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create();
        subscriptionAdminClient.deleteSubscription(subscriptionName);
        subscriptionAdminClient.close();
    }

    public static void receiveMessages(String subscriptionID, Firestore db, String topicId, String receiverName){
        ProjectSubscriptionName subscriptionName =
                ProjectSubscriptionName.of(projectId, subscriptionID);
        ExecutorProvider executorProvider = InstantiatingExecutorProvider
                .newBuilder()
                .setExecutorThreadCount(1) // ensures only 1 message is processed
                .build();
        Subscriber subscriber =
                Subscriber.newBuilder(subscriptionName, new MessageReceiveHandler(db, topicId, receiverName))
                        .setExecutorProvider(executorProvider)
                        .build();
        subscriber.startAsync().awaitRunning();

        System.out.println("A receber mensagens...");
        System.out.println("Pressione ENTER para parar...");

        new Scanner(System.in).nextLine();

        subscriber.stopAsync().awaitTerminated();
    }

    private static int menu() {
        int op;
        Scanner scan = new Scanner(System.in);
        do {
            System.out.println();
            System.out.println("    MENU");
            System.out.println(" 1 - Listar tópicos");
            System.out.println(" 2 - Publicar mensagem");
            System.out.println(" 3 - Criar subscrição");
            System.out.println(" 4 - Apagar subscrição");
            System.out.println(" 5 - Receber mensagens");
            System.out.println("99 - Sair");
            System.out.println();
            System.out.println("Escolha uma opção?");
            op = scan.nextInt();
        } while (!((op >= 1 && op <= 5) || op == 99));
        return op;
    }

    static void main(String[] args) throws Exception {
        // - Set environment variable
        //     GOOGLE_APPLICATION_CREDENTIALS="pathname to AccountServiceKEY.json"
        // - set project_id as a command line argument
        projectId = args[0];

        FirestoreOptions options = FirestoreOptions
                .newBuilder().setProjectId("cn2526-t3-g12")
                .setDatabaseId("cn-g12-sv2026-firestore")
                .build();

        Firestore db = options.getService();

        Scanner scanInput = new Scanner(System.in);

        while (true) {
            try {
                int option = menu();
                switch (option) {
                    case 1 :
                        listTopics();
                        break;
                    case 2 :
                        String pubTopicName = read("Topic ID to Publish?", scanInput);
                        String author = read("Author name?", scanInput);
                        String msg = read("Message to Publish?", scanInput);
                        Gson gsonMsg = new Gson();
                        String jsonMsg = gsonMsg.toJson(new ForumMessage(author, msg));
                        publishMessage(pubTopicName, jsonMsg);
                        break;
                    case 3 :
                        String topicID = read("Topic ID to create Subscription?", scanInput);
                        String subsID = read("Subscription ID?", scanInput);
                        createSubscription(topicID, subsID);
                        break;
                    case 4 :
                        String subsDeleteID = read("Subscription ID?", scanInput);
                        deleteSubscription(subsDeleteID);
                        break;
                    case 5:
                        String subsMessageID = read("Subscription ID?", scanInput);
                        String topicId = read("Topic ID?", scanInput);
                        String receiverName = read("What is the receiver's name?", scanInput);
                        receiveMessages(subsMessageID, db, topicId, receiverName);
                        break;
                    case 99 :
                        System.exit(0);

                }
            } catch (Exception ex) {
                System.out.println("Execution call error!");
                ex.printStackTrace();
            }
        }

        // Send message to a topic



    }
}
