package com.heroku.nwl.service;

import com.heroku.nwl.config.CustomBotException;
import com.heroku.nwl.model.Role;
import com.heroku.nwl.model.User;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

public interface UserService {
    User registerUser(Message msg);
    Role getUserRole(long chatId);
    String getContact() throws CustomBotException;
    List<User> getAllUser();
    List<User> getUsersByRole(Role role);
    User getUserById(Long chatId);
}
