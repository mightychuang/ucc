package com.hhly.user.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import com.hhly.common.entity.BaseEntity;
@Getter
@Setter
@ToString
public class Resource extends BaseEntity{

    private String name;

    private String code;

    private Integer selfId;

    private Integer parentId;

    private String path;

    private Byte depth;

    private String url;

    private Byte seq;

    private String icon;

    private String type;

    private String method;

    private Byte state;

    private String appCode;

    private String applyTo;

    private Byte joinWhiteList;

}