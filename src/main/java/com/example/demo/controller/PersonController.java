package com.example.demo.controller;

import com.example.demo.domain.dto.PersonDTO;
import com.example.demo.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
@Tag(name = "Person Management", description = "APIs for managing persons")
public class PersonController {

    private final PersonService personService;

    @GetMapping
    @Operation(summary = "Get all persons", description = "Retrieves a list of all persons")
    public ResponseEntity<List<PersonDTO>> getAllPersons() {
        return ResponseEntity.ok(personService.getAllPersons());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get person by ID", description = "Retrieves a person by their ID")
    public ResponseEntity<PersonDTO> getPersonById(@PathVariable Long id) {
        return personService.getPersonById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create person", description = "Creates a new person")
    public ResponseEntity<PersonDTO> createPerson(@Valid @RequestBody PersonDTO personDTO) {
        PersonDTO createdPerson = personService.createPerson(personDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update person", description = "Updates an existing person by ID")
    public ResponseEntity<PersonDTO> updatePerson(@PathVariable Long id, @Valid @RequestBody PersonDTO personDTO) {
        try {
            PersonDTO updatedPerson = personService.updatePerson(id, personDTO);
            return ResponseEntity.ok(updatedPerson);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete person", description = "Deletes a person by ID")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        try {
            personService.deletePerson(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search/lastName")
    @Operation(summary = "Find by last name", description = "Finds persons by last name")
    public ResponseEntity<List<PersonDTO>> findByLastName(@RequestParam String lastName) {
        List<PersonDTO> persons = personService.findByLastName(lastName);
        return ResponseEntity.ok(persons);
    }

    @GetMapping("/search/name")
    @Operation(summary = "Find by full name", description = "Finds persons by first and last name")
    public ResponseEntity<List<PersonDTO>> findByFullName(
            @RequestParam String firstName,
            @RequestParam String lastName) {
        List<PersonDTO> persons = personService.findByFirstNameAndLastName(firstName, lastName);
        return ResponseEntity.ok(persons);
    }

    @GetMapping("/search/email")
    @Operation(summary = "Find by email", description = "Finds persons by email content")
    public ResponseEntity<List<PersonDTO>> findByEmailContaining(@RequestParam String email) {
        List<PersonDTO> persons = personService.findByEmailContaining(email);
        return ResponseEntity.ok(persons);
    }
} 