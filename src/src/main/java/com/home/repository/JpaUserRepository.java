package com.home.repository;

import com.home.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface JpaUserRepository extends JpaRepository<User, Integer> {

    Optional<User> getByChatId(Long chatId);

    Optional<User> getById(Long id);

    List<User> getUsersByIdBetween(Long firstId, Long lastId);

}
