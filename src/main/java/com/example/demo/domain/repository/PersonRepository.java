package com.example.demo.domain.repository;

import com.example.demo.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    List<Person> findByLastName(String lastName);
    List<Person> findByFirstNameAndLastName(String firstName, String lastName);
    
    @Query("SELECT p FROM Person p WHERE LOWER(p.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<Person> findByEmailContaining(@Param("email") String email);
    
    Optional<Person> findByEmail(String email);
} 