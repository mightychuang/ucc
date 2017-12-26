package com.hhly.user.entity;

import java.util.Date;

import com.hhly.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class BlackList extends BaseEntity {

    /** 主键ID **/
    private Integer id;

    /** NAME */
    private String blackName;

    /** IP,ACCOUT,IMEI --> TYPE */
    private String blackType;

    /** 是否永久(0:非永久 1:永久) --> FOREVER */
    private Integer forever;

    /** 过期时间 --> EXPIRARTION_TIME */
    private Date expirartionTime;

    /** DESCRIPTION */
    private String description;

    private static final long serialVersionUID = 1L;

}