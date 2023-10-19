package com.lms.sqlfather.model.dto;

import lombok.Data;

import java.io.Serializable;
@Data
public class UserFindBackPasswordRequest implements Serializable {


    private String email;
    private String emailCode;
    private String userPassword;

    private String checkPassword;
}
