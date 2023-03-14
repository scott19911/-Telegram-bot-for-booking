package com.heroku.nwl.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByChatId(Long chatId);
    List<User> findByRole(Role role);
    List<User> findAll();

}
