package forumserverapp;

import com.google.protobuf.Empty;
import forum.ExistingTopics;
import forum.ForumGrpc;
import forum.ForumMessage;
import forum.SubscribeUnSubscribe;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ForumServiceImpl extends ForumGrpc.ForumImplBase {

    public ForumServiceImpl() {}

    private final ConcurrentHashMap<String, ConcurrentMap<String, StreamObserver<ForumMessage>>> struct = new ConcurrentHashMap<>();

    @Override
    public void topicSubscribe(SubscribeUnSubscribe request, StreamObserver<ForumMessage> responseObserver) {

        String user = request.getUsrName();
        String topic = request.getTopicName();

        struct.putIfAbsent(topic, new ConcurrentHashMap<>());
        ConcurrentMap<String, StreamObserver<ForumMessage>> topicSubscribers = struct.get(topic);

        topicSubscribers.put(user, responseObserver);

        System.out.println("User: " + user + "subscribed to: " + topic);
    }

    @Override
    public void topicUnSubscribe(SubscribeUnSubscribe request, StreamObserver<Empty> responseObserver) {

        String user = request.getUsrName();
        String topic = request.getTopicName();

        ConcurrentMap<String, StreamObserver<ForumMessage>> userMap = struct.get(topic);

        if (userMap != null){ //caso o tópico exista
            StreamObserver<ForumMessage> removed = userMap.remove(user);

            if (removed != null) {
                try {
                    removed.onCompleted(); //tentar fechar o stream do cliente
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        responseObserver.onNext(Empty.newBuilder().build()); //envia uma resposta vazia (sucesso) ao cliente
        responseObserver.onCompleted(); //termina a mensagem
    }

    @Override
    public void getAllTopics(Empty request, StreamObserver<ExistingTopics> responseObserver) {

        ExistingTopics reply = ExistingTopics.newBuilder().addAllTopicName(struct.keySet()).build();

        responseObserver.onNext(reply); //enviar a resposta ao cliente
        responseObserver.onCompleted(); //fechar o stream

    }

    @Override
    public void publishMessage(ForumMessage request, StreamObserver<Empty> responseObserver) {

        String user = request.getFromUser();
        String topic = request.getTopicName();

        ConcurrentMap<String, StreamObserver<ForumMessage>> topicSubscribers = struct.get(topic);

        if(topicSubscribers == null || !topicSubscribers.containsKey(user)){
            responseObserver.onError(Status.PERMISSION_DENIED.withDescription("O utilizador não está subscrito no tópico").asRuntimeException());
        }

        for (StreamObserver<ForumMessage> observer : topicSubscribers.values()){ //enviar a mensagem a cada utilizador subscrito no tópico
            try{
                observer.onNext(request);
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }


}
