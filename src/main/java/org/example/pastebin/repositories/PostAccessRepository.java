package org.example.pastebin.repositories;

import org.example.pastebin.model.Person;
import org.example.pastebin.model.PostAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostAccessRepository extends JpaRepository<PostAccess, Long> {
    List<PostAccess> findByPerson(Person person);
}
