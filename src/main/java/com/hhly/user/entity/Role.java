package com.hhly.user.entity;

import com.hhly.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
public class Role extends BaseEntity {

    private String name;

    private String code;

    private String appCode;

    private Byte state;

    private String description;


}