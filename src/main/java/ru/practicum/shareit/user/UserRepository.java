package ru.practicum.shareit.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    @Query("""
        select (count(u) > 0) from User u
        where lower(u.email) = lower(:email)
          and (:ignoredUserId is null or u.id <> :ignoredUserId)
        """)
    boolean existsByEmailIgnoreUser(@Param("email") String email,
                                    @Param("ignoredUserId") Long ignoredUserId);
}