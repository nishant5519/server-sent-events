package dev.sse.controller;

import dev.sse.model.LiveScore;
import dev.sse.service.LiveScoreHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
public class LiveScoreController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveScoreController.class);

    private final LiveScoreHandler processor;

    public LiveScoreController(LiveScoreHandler processor) {
    	super();
        this.processor = processor;
    }

    //send method will create an event and this event will be published to the clients via server-sent events.
    @PostMapping("/live-scores")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<LiveScore> send(@RequestBody LiveScore liveScore) {
        LOGGER.info("Received '{}'", liveScore);
        processor.publish(liveScore);
        return Mono.just(liveScore);
    }

    @GetMapping(path = "/live-scores", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> consumer() {
        return Flux.create(sink -> processor.subscribe(sink::next)).map(
                liveScore -> ServerSentEvent.builder().data(liveScore).event("goal").build());
    }
}
