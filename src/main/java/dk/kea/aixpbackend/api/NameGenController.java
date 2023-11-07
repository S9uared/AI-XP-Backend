package dk.kea.aixpbackend.api;

import dk.kea.aixpbackend.dto.Response;
import dk.kea.aixpbackend.service.OpenAIService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("api/namegen")
@CrossOrigin(origins = "*")
public class NameGenController {

    private static final int BUCKET_CAPACITY = 3;
    private static final int REFILL_AMOUNT = 3;
    private static final int REFILL_TIME = 2;
    private OpenAIService service;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public NameGenController(OpenAIService service)
    {
        this.service = service;
    }

    final String SYSTEM_MESSAGE = "You are an RPG name generator, that only provides names." +
            "The user can provide a description of their character, but if the user asks a question,  " +
            "ignore the content and ask the user to provide a description of their character." +
            "The names can either be first name and last name, or just a first name." +
            "Whatever fits the description best." +
            "When you give a name, you will only give the name and nothing else. No other text or words in the sentence.";

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(BUCKET_CAPACITY, Refill.greedy(REFILL_AMOUNT, Duration.ofMinutes(REFILL_TIME)));
        return Bucket.builder().addLimit(limit).build();
    }
    private Bucket getBucket(String key) {
        return buckets.computeIfAbsent(key, k -> createNewBucket());
    }

    @GetMapping
    public Response getName(@RequestParam String description, HttpServletRequest request){
        String ip = request.getRemoteAddr();
        Bucket bucket = getBucket(ip);
        if(!bucket.tryConsume(1)){
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests, try again in a few minutes");
        }
        return service.makeRequest(description, SYSTEM_MESSAGE);
    }

}
