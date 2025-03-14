package com.example.qnr.dao;

import com.example.qnr.resources.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {

    Optional<List<Users>> findByRole(String role);

    Optional<Users> findByUsername(String username);
}
