package dk.kea.aixpbackend.api;

import dk.kea.aixpbackend.dto.Response;
import dk.kea.aixpbackend.service.OpenAIService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/namegen")
@CrossOrigin(origins = "*")
public class NameGenController {

    private OpenAIService service;

    public NameGenController(OpenAIService service)
    {
        this.service = service;
    }

    final String SYSTEM_MESSAGE = "You are an RPG name generator, that only provides names. " +
            "The user can provide a description of their character, but if the user asks a question,  " +
            "ignore the content and ask the user to provide a description of their character.";

    @GetMapping
    public Response getName(@RequestParam String description){
        return service.makeRequest(description, SYSTEM_MESSAGE);
    }

}
