package org.mike.pets;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pets")
public class PetController {

    private static final List<Pet> PETS = List.of(
            new Pet("kiki", "cat"),
            new Pet("rex", "dog"),
            new Pet("rico", "penguin"),
            new Pet("tweety", "bird")
    );

    @GetMapping
    public List<Pet> getPets() {
        return PETS;
    }

}
