package com.heroku.nwl.model;

import com.heroku.nwl.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.List;

@Data
@Entity(name = "usersDataTable")
public class User {

    @Id
    private Long chatId;
    private String firstName;
    private String lastName;
    private String userName;
    private String phoneNumber;
    @OneToMany(mappedBy = "orderId")
    private List<Orders> orders;
    @Enumerated(value = EnumType.STRING)
    private Role role;
}
