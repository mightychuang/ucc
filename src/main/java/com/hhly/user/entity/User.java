package com.hhly.user.entity;


import com.hhly.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
public class User extends BaseEntity {

    //登录名称
    private String loginName;
    //真实姓名
    private String realName;
    //密码
    private String password;
    //手机号
    private String phone;
    //邮箱
    private String email;
    //状态
    private Byte state;
    //状态描述
    private String stateDesc;
    //用户类型
    private Byte accType;
    //注册方式
    private Byte regType;
    //是否认证
    private Byte isAuth;
    //所属应用编码
    private String appCode;
    //注册设备类型(IOS, ANDROID, PC, H5)
    private String regDeviceType;
    //注册设备ID
    private String regDeviceId;

}