package com.example.demo.service;

import com.example.demo.domain.Person;
import com.example.demo.domain.dto.PersonDTO;

import java.util.List;
import java.util.Optional;

public interface PersonService {
    List<PersonDTO> getAllPersons();
    Optional<PersonDTO> getPersonById(Long id);
    PersonDTO createPerson(PersonDTO personDTO);
    PersonDTO updatePerson(Long id, PersonDTO personDTO);
    void deletePerson(Long id);
    List<PersonDTO> findByLastName(String lastName);
    List<PersonDTO> findByFirstNameAndLastName(String firstName, String lastName);
    List<PersonDTO> findByEmailContaining(String email);
} 