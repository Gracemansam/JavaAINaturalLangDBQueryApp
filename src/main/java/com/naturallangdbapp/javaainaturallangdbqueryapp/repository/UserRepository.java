package com.naturallangdbapp.javaainaturallangdbqueryapp.repository;

import com.naturallangdbapp.javaainaturallangdbqueryapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByNameContainingIgnoreCase(String name);

    @Query(value = "?1", nativeQuery = true)
    List<Object[]> executeNativeQuery(String sql);
}