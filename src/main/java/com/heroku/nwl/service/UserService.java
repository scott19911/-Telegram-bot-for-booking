package com.heroku.nwl.service;

import com.heroku.nwl.model.Role;
import com.heroku.nwl.model.User;
import com.heroku.nwl.model.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    @Value("${bot.admin.phone}")
    private String botAdminPhone;
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User registerUser(Message msg) {
        User user = userRepository.findById(msg.getChatId()).orElse(new User());
        var chatId = msg.getChatId();
        var chat = msg.getChat();
        user.setChatId(chatId);
        user.setFirstName(chat.getFirstName());
        user.setLastName(chat.getLastName());
        user.setUserName(chat.getUserName());
        user.setPhoneNumber(msg.getContact().getPhoneNumber());
        user.setRole(Role.USER);
        if (user.getPhoneNumber().equals(botAdminPhone)) {
            user.setRole(Role.ADMIN);
        }
        userRepository.save(user);
        return user;
    }
    public Role getUserRole (long chatId){
        return userRepository.findByChatId(chatId).getRole();
    }
}
