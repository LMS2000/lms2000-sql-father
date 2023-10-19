package com.lms.sqlfather.model.dto;

import lombok.Data;

import java.io.Serializable;
@Data
public class UserEmailCodeRequest implements Serializable {



    private String email;

    private String emailCode;
}
