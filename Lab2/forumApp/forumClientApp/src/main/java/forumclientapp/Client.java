package forumclientapp;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.protobuf.Empty;
import forum.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.util.Scanner;

public class Client {

    private static String svcIP = "34.175.121.175";
    private static int svcPort = 8000;
    private static ForumGrpc.ForumBlockingStub blockingStub;
    private static ForumGrpc.ForumStub noBlockStub;

    static void main(String[] args) {
        try {
            if (args.length == 2) {
                svcIP = args[0];
                svcPort = Integer.parseInt(args[1]);
            }

            System.out.println("connect to " + svcIP + ":" + svcPort);

            ManagedChannel channel = ManagedChannelBuilder.forAddress(svcIP, svcPort)
                    .usePlaintext()
                    .build();

            blockingStub = ForumGrpc.newBlockingStub(channel);
            noBlockStub = ForumGrpc.newStub(channel);

            Scanner sc = new Scanner(System.in); //usar scanner para obter o username a ser registado
            System.out.print("Username: ");
            String username = sc.nextLine();

            while (true) {
                try {
                    int option = menu();
                    switch (option) {
                        case 1 :
                            subscribe(username); break;
                        case 2 :
                            unsubscribe(username); break;
                        case 3 :
                            publish(username); break;
                        case 4 :
                            listTopics(); break;
                        case 99 :
                            channel.shutdown();
                            System.exit(0);

                    }
                } catch (Exception ex) {
                    System.out.println("Execution call error!");
                    ex.printStackTrace();
                }
            }

        } catch (Exception ex) {
            System.out.println("Unhandled exception");
            ex.printStackTrace();
        }
    }

    static void subscribe(String username) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Topic: ");
        String topic = sc.nextLine();

        SubscribeUnSubscribe req = SubscribeUnSubscribe.newBuilder()
                .setUsrName(username)
                .setTopicName(topic)
                .build();

        noBlockStub.topicSubscribe(req, new StreamObserver<>() {
            @Override
            public void onNext(ForumMessage msg) {
                System.out.println("\n[" + msg.getTopicName() + "] "
                        + msg.getFromUser() + ": " + msg.getTxtMsg());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Subscription error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Subscription ended.");
            }
        });

        System.out.println("Subscribed to " + topic);
    }

    static void unsubscribe(String username) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Topic: ");
        String topic = sc.nextLine();

        SubscribeUnSubscribe req = SubscribeUnSubscribe.newBuilder()
                .setUsrName(username)
                .setTopicName(topic)
                .build();

        blockingStub.topicUnSubscribe(req);
        System.out.println("Unsubscribed from " + topic);
    }

    static void publish(String username) throws IOException {
        Storage storage;
        StorageOptions storageOptions = StorageOptions.getDefaultInstance();
        storage = storageOptions.getService();

        Scanner sc = new Scanner(System.in);

        System.out.print("Topic: ");
        String topic = sc.nextLine();

        System.out.print("Message: ");
        String txt = sc.nextLine();

        System.out.print("Enter bucket name(leave in blank if desired): ");
        String bucketName = sc.nextLine();

        System.out.print("Enter blob name(leave in blank if desired): ");
        String blobName = sc.nextLine();

        System.out.println("Enter file path(leave in blank if desired): ");
        String absPathName = sc.nextLine();

        ForumMessage msg = ForumMessage.newBuilder()
                .setFromUser(username)
                .setTopicName(topic)
                .setTxtMsg(txt + "[;" + bucketName + ";" + blobName + "]")
                .build();

        if(!bucketName.isEmpty() && !blobName.isEmpty() && !absPathName.isEmpty()){
            StorageOperations storageOper = new StorageOperations(storage);
            storageOper.downloadBlobFromBucket(bucketName, blobName, absPathName);
        }

        try {
            blockingStub.publishMessage(msg);
            System.out.println("Message published.");
        } catch (Exception e) {
            System.out.println("Publish error: " + e.getMessage());
        }
    }

    static void listTopics() {
        ExistingTopics topics = blockingStub.getAllTopics(Empty.newBuilder().build());

        System.out.println("Topics:");
        for (String t : topics.getTopicNameList()) {
            System.out.println("- " + t);
        }
    }

    private static int menu() {
        int op;
        Scanner scan = new Scanner(System.in);
        do {
            System.out.println();
            System.out.println("    MENU");
            System.out.println(" 1 - Subscribe to topic");
            System.out.println(" 2 - Unsubscribe from topic");
            System.out.println(" 3 - Publish message");
            System.out.println(" 4 - List topics");
            System.out.println("99 - Exit");
            System.out.println();
            System.out.println("Choose an Option?");
            op = scan.nextInt();
        } while (!((op >= 1 && op <= 4) || op == 99));
        return op;
    }
}