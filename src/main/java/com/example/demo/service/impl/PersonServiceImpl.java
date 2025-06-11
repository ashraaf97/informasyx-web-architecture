package com.example.demo.service.impl;

import com.example.demo.domain.Person;
import com.example.demo.domain.dto.PersonDTO;
import com.example.demo.domain.mapper.PersonMapper;
import com.example.demo.domain.repository.PersonRepository;
import com.example.demo.service.PersonService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    @Override
    public List<PersonDTO> getAllPersons() {
        return personMapper.toDtoList(personRepository.findAll());
    }

    @Override
    public Optional<PersonDTO> getPersonById(Long id) {
        return personRepository.findById(id)
                .map(personMapper::toDto);
    }

    @Override
    public PersonDTO createPerson(PersonDTO personDTO) {
        Person person = personMapper.toEntity(personDTO);
        Person savedPerson = personRepository.save(person);
        return personMapper.toDto(savedPerson);
    }

    @Override
    public PersonDTO updatePerson(Long id, PersonDTO personDTO) {
        if (!personRepository.existsById(id)) {
            throw new EntityNotFoundException("Person not found with id: " + id);
        }
        
        Person person = personMapper.toEntity(personDTO);
        person.setId(id);
        Person updatedPerson = personRepository.save(person);
        return personMapper.toDto(updatedPerson);
    }

    @Override
    public void deletePerson(Long id) {
        if (!personRepository.existsById(id)) {
            throw new EntityNotFoundException("Person not found with id: " + id);
        }
        personRepository.deleteById(id);
    }

    @Override
    public List<PersonDTO> findByLastName(String lastName) {
        return personMapper.toDtoList(personRepository.findByLastName(lastName));
    }

    @Override
    public List<PersonDTO> findByFirstNameAndLastName(String firstName, String lastName) {
        return personMapper.toDtoList(
                personRepository.findByFirstNameAndLastName(firstName, lastName)
        );
    }

    @Override
    public List<PersonDTO> findByEmailContaining(String email) {
        return personMapper.toDtoList(
                personRepository.findByEmailContaining(email)
        );
    }
} 