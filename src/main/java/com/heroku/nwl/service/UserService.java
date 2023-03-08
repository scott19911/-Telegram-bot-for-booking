package com.heroku.nwl.service;

import com.heroku.nwl.Role;
import com.heroku.nwl.constants.Constants;
import com.heroku.nwl.model.User;
import com.heroku.nwl.model.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    @Value("${bot.admin.phone}")
    private String botAdminPhone;
    public String registerUser(Message msg) {
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
        String message = String.format(
                Constants.USER_DATA,
                chatId,
                user.getFirstName(),
                user.getLastName(),
                user.getUserName(),
                user.getPhoneNumber());
        userRepository.save(user);
        return message;
    }
    public Role getUserRole (long chatId){
        return userRepository.findByChatId(chatId).getRole();
    }
}
