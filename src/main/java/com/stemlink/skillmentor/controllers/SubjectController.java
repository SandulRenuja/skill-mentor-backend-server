package com.stemlink.skillmentor.controllers;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/subjects")
public class SubjectController {

    // controllers
    // services
    // repositories
    // models or entities
    // utils
    // configs

    // Path parameter
    // Query Parameter => ?name=IT

    // Mock Database
    private final List<String> subjects = new ArrayList<>((
            List.of("Maths", "Science", "History")
    ));




    @GetMapping
    public String getAllSubjects(@RequestParam(name = "name", defaultValue = "all") String name) {
        String result =  subjects.toString();
        System.out.println(result); // -> use loggers (slf4j)
        return result;
    }

    // subjects/1
    @GetMapping("{id}")
    public String getSubjectById(
            @PathVariable int id
            ) {
        System.out.println("GET BY ID" + id);
        return subjects.get(id);
    }

    //    {"name":"Science","id":1}
    @PostMapping
    public String createSubject(@RequestBody String subject) {
        System.out.println("POST");
        subjects.add(subject);
        return "create subject";
    }

    @PutMapping("{id}")
    public String updateSubject(@RequestBody String updatedSubject) {
        System.out.println("PUT" + updatedSubject);
        return "update subject";
    }

    @DeleteMapping("{id}")
    public String deleteSubject(@PathVariable int id) {
        System.out.println("DELETE");
        subjects.remove(id);
        return subjects.toString();
    }
}
