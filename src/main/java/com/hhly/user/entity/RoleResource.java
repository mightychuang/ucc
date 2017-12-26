package com.hhly.user.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import com.hhly.common.entity.BaseEntity;

@Getter
@Setter
@ToString
public class RoleResource extends BaseEntity{


    private String roleCode;

    private String resCode;


}