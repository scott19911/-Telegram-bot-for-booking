package com.heroku.nwl.service;

import com.heroku.nwl.config.CustomBotException;
import com.heroku.nwl.model.Role;
import com.heroku.nwl.model.User;
import com.heroku.nwl.model.UserRepository;
import com.heroku.nwl.model.WorkSettings;
import com.heroku.nwl.model.WorkSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

import static com.heroku.nwl.constants.Constants.CONTACT_MESSAGE_WITHOUT_BRAKE;
import static com.heroku.nwl.constants.Constants.CONTACT_MESSAGE_WITH_BRAKE;
import static com.heroku.nwl.constants.Constants.DEFAULT_END_BRAKE;
import static com.heroku.nwl.constants.Constants.DEFAULT_START_BRAKE;
import static com.heroku.nwl.constants.ErrorMessage.ERROR_SERVICE_NOT_AVAILABLE;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    @Value("${bot.admin.phone}")
    private String botAdminPhone;
    private final WorkSettingsRepository workSettingsRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User registerUser(Message msg) {
        User user = userRepository.findById(msg.getChatId()).orElse(new User());
        var chatId = msg.getChatId();
        var chat = msg.getChat();
        user.setChatId(chatId);
        user.setFirstName(chat.getFirstName());
        user.setLastName(chat.getLastName());
        user.setUserName(chat.getUserName());
        String phone = msg.getContact().getPhoneNumber();

        user.setPhoneNumber(phone.startsWith("+") ? phone : "+" + phone);
        user.setRole(Role.USER);
        if (user.getPhoneNumber().equals(botAdminPhone)) {
            user.setRole(Role.ADMIN);
        }
        userRepository.save(user);
        return user;
    }

    public Role getUserRole(long chatId) {
        return userRepository.findByChatId(chatId).getRole();
    }
    public String getContact() throws CustomBotException {
        WorkSettings workSettings = workSettingsRepository.findById(1L).orElse(null);
        if (workSettings == null){
            log.info(ERROR_SERVICE_NOT_AVAILABLE);
            throw new CustomBotException(ERROR_SERVICE_NOT_AVAILABLE);
        }
        String message;
        if (!workSettings.getBreakFrom().equals(DEFAULT_START_BRAKE) && !workSettings.getBreakTo().equals(DEFAULT_END_BRAKE)) {
            message = String.format(CONTACT_MESSAGE_WITH_BRAKE,
                    workSettings.getOpenTime(),
                    workSettings.getCloseTime(),
                    workSettings.getBreakFrom(),
                    workSettings.getBreakTo(),
                    workSettings.getCity(),
                    workSettings.getStreet(),
                    workSettings.getBuilding(),
                    workSettings.getApartment(),
                    workSettings.getPhoneNumber(),
                    workSettings.getLink());
        } else {
            message = String.format(CONTACT_MESSAGE_WITHOUT_BRAKE,
                    workSettings.getOpenTime(),
                    workSettings.getCloseTime(),
                    workSettings.getCity(),
                    workSettings.getStreet(),
                    workSettings.getBuilding(),
                    workSettings.getApartment(),
                    workSettings.getPhoneNumber(),
                    workSettings.getLink());
        }
        return message;
    }
    public List<User> getAllUser(){
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public User getUserById(Long chatId) {
        return userRepository.findByChatId(chatId);
    }
}
