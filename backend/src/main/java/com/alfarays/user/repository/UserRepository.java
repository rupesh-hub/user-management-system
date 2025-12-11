package com.alfarays.user.repository;


import com.alfarays.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Query(name = "User.findByUsername")
    Optional<User> findByUsername(@Param("username") String username);

    @Query(name = "User.findByEmail")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT U FROM User U WHERE LOWER(U.email) = :username OR U.username = :username")
    Optional<User> findByUsernameOrEmail(@Param("username") String username);

    @Query(name = "User.usernameExists")
    Optional<Boolean> usernameExists(@Param("username") String username);

    @Query(name = "User.emailExists")
    Optional<Boolean> emailExists(@Param("email") String email);

}