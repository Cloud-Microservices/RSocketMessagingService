package rsocketmessagingservice.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsocketmessagingservice.boundaries.ExternalReferenceBoundary;
import rsocketmessagingservice.boundaries.IdBoundary;
import rsocketmessagingservice.boundaries.MessageBoundary;
import rsocketmessagingservice.logic.MessageService;

@Controller
public class RSocketMessageController {
    private MessageService messages;
    private Log logger = LogFactory.getLog(RSocketMessageController.class);

    //java -jar rsc-0.9.1.jar --request --route=publish-message-req-resp --data="{\"messageId\":\"msg-1\",\"publishedTimestamp\":\"2022-01-01T12:00:00Z\",\"messageType\":\"TEXT\",\"summary\":\"Test message\",\"externalReferences\":[{\"service\":\"TestService\",\"externalServiceId\":\"123\"}],\"messageDetails\":{\"key1\":\"value1\"}}" --debug tcp://localhost:7001
    @Autowired
    public void setMessages(MessageService messages) {
        this.messages = messages;
    }
    @MessageMapping("publish-message-req-resp")
    public Mono<MessageBoundary> create(@Payload MessageBoundary message) {
        this.logger.debug("invoking: publish-message-req-resp");
        return this.messages.publishMessage(message);

    }

    //java -jar rsc-0.9.1.jar --stream --route=getAll-req-stream --debug tcp://localhost:7001
    @MessageMapping("getAll-req-stream")
    public Flux<MessageBoundary> getAllMessages() {
        this.logger.debug("invoking: getAll-req-stream");
        return this.messages.getAllMessages();
    }


    /*
    java -jar rsc-0.9.1.jar --channel --route=getMessagesByIds-channel --data=- --debug tcp://localhost:7001
    Input: {"messageId":"65d5e5a08694d3680daa9c19"}
     */
        @MessageMapping("getMessagesByIds-channel")
        public Flux<MessageBoundary> getMessagesByIds(Flux<IdBoundary> ids) {
            this.logger.debug("invoking: getMessagesByIds-channel");
            return ids
                    .flatMap(id -> this.messages.getMessageById(id.getMessageId()));
        }

    //java -jar rsc-0.9.1.jar --fnf --route=deleteAll-fire-and-forget --debug tcp://localhost:7001
    @MessageMapping("deleteAll-fire-and-forget")
    public Mono<Void> deleteAllMessages() {
        this.logger.debug("invoking: deleteAll-fire-and-forget");
        return messages.deleteAllMessages();
    }

    /*
    java -jar rsc-0.9.1.jar --channel --route=getMessagesByExternalReferences-channel --data=- --debug tcp://localhost:7001
    {"service":"abc","externalServiceId":"12345"}
    {"service":"vsvsf","externalServiceId":"fhnsjls"}
    {"service":"AnotherService","externalServiceId":"456"}
     */
    @MessageMapping("getMessagesByExternalReferences-channel")
    public Flux<MessageBoundary> getMessagesByExternalReferences(Flux<ExternalReferenceBoundary> externalRefs) {
        this.logger.debug("invoking: getMessagesByExternalReferences-channel");
        return externalRefs
                .flatMap(ref -> this.messages.getMessagesByExternalReference(ref.getService(), ref.getExternalServiceId()));
    }
}
