package com.site.elohim.repository;

import com.site.elohim.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByUserId(String userId);
    Optional<Users> findByUserName(String userName);
    Optional<Users> findById(Long id);
    List<Users> findByUserRoleOrderByUserName(String role);
    boolean existsById(Long id);
    boolean existsByMemberId(Long memberId);
}
