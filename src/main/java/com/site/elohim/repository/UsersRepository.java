package com.site.elohim.repository;

import com.site.elohim.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByUserId(String userId);
    Optional<Users> findByUserIdAndUserPassword(String userId, String userPassword);
}
