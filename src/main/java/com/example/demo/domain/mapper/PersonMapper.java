package com.example.demo.domain.mapper;

import com.example.demo.domain.Person;
import com.example.demo.domain.dto.PersonDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersonMapper {

    PersonDTO toDto(Person person);

    Person toEntity(PersonDTO personDTO);

    List<PersonDTO> toDtoList(List<Person> persons);

    List<Person> toEntityList(List<PersonDTO> personDTOs);
} 