package com.example.demo.service.impl;

import com.example.demo.domain.Person;
import com.example.demo.domain.dto.PersonDTO;
import com.example.demo.domain.mapper.PersonMapper;
import com.example.demo.domain.repository.PersonRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceImplTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PersonMapper personMapper;

    @InjectMocks
    private PersonServiceImpl personService;

    private Person testPerson;
    private PersonDTO testPersonDTO;
    private List<Person> personList;
    private List<PersonDTO> personDTOList;

    @BeforeEach
    void setUp() {
        // Set up test data
        testPerson = new Person(1L, "John", "Doe", "john.doe@example.com", "123-456-7890", "123 Main St", null);
        testPersonDTO = new PersonDTO(1L, "John", "Doe", "john.doe@example.com", "123-456-7890", "123 Main St");
        
        Person person2 = new Person(2L, "Jane", "Smith", "jane.smith@example.com", "987-654-3210", "456 Oak Ave", null);
        PersonDTO personDTO2 = new PersonDTO(2L, "Jane", "Smith", "jane.smith@example.com", "987-654-3210", "456 Oak Ave");
        
        personList = Arrays.asList(testPerson, person2);
        personDTOList = Arrays.asList(testPersonDTO, personDTO2);
    }

    @Test
    void getAllPersons() {
        // Arrange
        when(personRepository.findAll()).thenReturn(personList);
        when(personMapper.toDtoList(personList)).thenReturn(personDTOList);

        // Act
        List<PersonDTO> result = personService.getAllPersons();

        // Assert
        assertEquals(2, result.size());
        assertEquals(testPersonDTO.getFirstName(), result.get(0).getFirstName());
        assertEquals(testPersonDTO.getLastName(), result.get(0).getLastName());
        verify(personRepository, times(1)).findAll();
        verify(personMapper, times(1)).toDtoList(personList);
    }

    @Test
    void getPersonById_Found() {
        // Arrange
        when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));
        when(personMapper.toDto(testPerson)).thenReturn(testPersonDTO);

        // Act
        Optional<PersonDTO> result = personService.getPersonById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testPersonDTO.getId(), result.get().getId());
        assertEquals(testPersonDTO.getFirstName(), result.get().getFirstName());
        verify(personRepository, times(1)).findById(1L);
        verify(personMapper, times(1)).toDto(testPerson);
    }

    @Test
    void getPersonById_NotFound() {
        // Arrange
        when(personRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<PersonDTO> result = personService.getPersonById(99L);

        // Assert
        assertFalse(result.isPresent());
        verify(personRepository, times(1)).findById(99L);
        verify(personMapper, never()).toDto(any());
    }

    @Test
    void createPerson() {
        // Arrange
        when(personMapper.toEntity(testPersonDTO)).thenReturn(testPerson);
        when(personRepository.save(testPerson)).thenReturn(testPerson);
        when(personMapper.toDto(testPerson)).thenReturn(testPersonDTO);

        // Act
        PersonDTO result = personService.createPerson(testPersonDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testPersonDTO.getFirstName(), result.getFirstName());
        assertEquals(testPersonDTO.getLastName(), result.getLastName());
        verify(personMapper, times(1)).toEntity(testPersonDTO);
        verify(personRepository, times(1)).save(testPerson);
        verify(personMapper, times(1)).toDto(testPerson);
    }

    @Test
    void updatePerson_Found() {
        // Arrange
        when(personRepository.existsById(1L)).thenReturn(true);
        when(personMapper.toEntity(testPersonDTO)).thenReturn(testPerson);
        when(personRepository.save(testPerson)).thenReturn(testPerson);
        when(personMapper.toDto(testPerson)).thenReturn(testPersonDTO);

        // Act
        PersonDTO result = personService.updatePerson(1L, testPersonDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testPersonDTO.getFirstName(), result.getFirstName());
        assertEquals(testPersonDTO.getLastName(), result.getLastName());
        verify(personRepository, times(1)).existsById(1L);
        verify(personMapper, times(1)).toEntity(testPersonDTO);
        verify(personRepository, times(1)).save(testPerson);
        verify(personMapper, times(1)).toDto(testPerson);
    }

    @Test
    void updatePerson_NotFound() {
        // Arrange
        when(personRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            personService.updatePerson(99L, testPersonDTO);
        });
        verify(personRepository, times(1)).existsById(99L);
        verify(personMapper, never()).toEntity(any());
        verify(personRepository, never()).save(any());
    }

    @Test
    void deletePerson_Found() {
        // Arrange
        when(personRepository.existsById(1L)).thenReturn(true);
        doNothing().when(personRepository).deleteById(1L);

        // Act
        personService.deletePerson(1L);

        // Assert
        verify(personRepository, times(1)).existsById(1L);
        verify(personRepository, times(1)).deleteById(1L);
    }

    @Test
    void deletePerson_NotFound() {
        // Arrange
        when(personRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            personService.deletePerson(99L);
        });
        verify(personRepository, times(1)).existsById(99L);
        verify(personRepository, never()).deleteById(any());
    }

    @Test
    void findByLastName() {
        // Arrange
        when(personRepository.findByLastName("Doe")).thenReturn(List.of(testPerson));
        when(personMapper.toDtoList(List.of(testPerson))).thenReturn(List.of(testPersonDTO));

        // Act
        List<PersonDTO> result = personService.findByLastName("Doe");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Doe", result.get(0).getLastName());
        verify(personRepository, times(1)).findByLastName("Doe");
        verify(personMapper, times(1)).toDtoList(List.of(testPerson));
    }

    @Test
    void findByFirstNameAndLastName() {
        // Arrange
        when(personRepository.findByFirstNameAndLastName("John", "Doe")).thenReturn(List.of(testPerson));
        when(personMapper.toDtoList(List.of(testPerson))).thenReturn(List.of(testPersonDTO));

        // Act
        List<PersonDTO> result = personService.findByFirstNameAndLastName("John", "Doe");

        // Assert
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Doe", result.get(0).getLastName());
        verify(personRepository, times(1)).findByFirstNameAndLastName("John", "Doe");
        verify(personMapper, times(1)).toDtoList(List.of(testPerson));
    }

    @Test
    void findByEmailContaining() {
        // Arrange
        when(personRepository.findByEmailContaining("example")).thenReturn(personList);
        when(personMapper.toDtoList(personList)).thenReturn(personDTOList);

        // Act
        List<PersonDTO> result = personService.findByEmailContaining("example");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.get(0).getEmail().contains("example"));
        assertTrue(result.get(1).getEmail().contains("example"));
        verify(personRepository, times(1)).findByEmailContaining("example");
        verify(personMapper, times(1)).toDtoList(personList);
    }
} 