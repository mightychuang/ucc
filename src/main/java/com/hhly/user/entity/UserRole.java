package com.hhly.user.entity;

import com.hhly.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
public class UserRole extends BaseEntity{


    private String userId;

    private String roleCode;

}