drop table if exists T_LAWYER;

/*==============================================================*/
/* Table: T_LAWYER                                              */
/*==============================================================*/
create table T_LAWYER
(
   ID                   bigint not null comment 'ID',
   USER_ID              bigint not null comment '用户ID',
   SPREA_PHONE          varchar(16) comment '备用电话',
   CODE                 varchar(32) not null comment '职业证号',
   ID_CARD              varchar(32) not null comment '身份证',
   PROVINCE             varchar(16) not null comment '省份',
   CITY                 varchar(16) not null comment '市',
   DISTRICT             varchar(32) comment '区',
   STREET               varchar(64) comment '街道',
   ADDRESS              varchar(256) not null comment '地址(省市区组合)',
   INSTRODUCE           varchar(500) comment '案件介绍',
   CARD_PIC             varchar(300) comment '身份证图片',
   PIC                  varchar(300) comment '执业照',
   CREATE_TIME          datetime comment '创建时间',
   IS_AUTH              tinyint(1) not null comment '是否认证(布尔值)',
   COMPANY              varchar(128) not null comment '律师公司',
   WORK_TIME            int not null comment '从事律师年限',
   IS_WORK              tinyint(1) not null comment '是否可以接单(布尔值,默认为true)',
   LOGO                 varchar(256) comment '头像(user表中带过来)',
   MAX_PRICE            varchar(16) comment '胜诉最高金额',
   primary key (ID)
);

alter table T_LAWYER comment '律师扩展表';


drop table if exists T_STUDENT;

/*==============================================================*/
/* Table: T_STUDENT                                             */
/*==============================================================*/
create table T_STUDENT
(
   ID                   bigint not null,
   CODE                 varchar(64) not null,
   SCHOOL               varchar(64) not null,
   BEGIN_TIME           datetime,
   END_TIME             datetime,
   USER_ID              bigint not null,
   primary key (ID)
);

alter table T_STUDENT comment '学生扩展表';

drop table if exists T_COMPANY;

/*==============================================================*/
/* Table: T_COMPANY                                             */
/*==============================================================*/
create table T_COMPANY
(
   ID                   bigint not null,
   NAME                 varchar(32) not null comment '企业名称',
   LEGAL                varchar(32) not null comment '法人代表',
   ID_CARD              varchar(32) comment '法人身份证',
   CODE                 varchar(16) comment '营业执照',
   PIC                  varchar(256) comment '营业执照照片',
   CERTIFICATE          varchar(32) comment '组织机构证件',
   ORG_CODE             varchar(32) comment '组织机构代码号',
   USER_ID              bigint not null comment '用户ID',
   primary key (ID)
);

alter table T_COMPANY comment '企业扩展表';


drop table if exists T_USER;

/*==============================================================*/
/* Table: T_USER                                               */
/*==============================================================*/
create table T_USER
(
   ID                   bigint not null comment 'ID',
   LOGIN_NAME           varchar(64) not null comment '登录账号',
   PASSWORD             varchar(128) not null comment '登录密码',
   USER_NAME            varchar(64) comment '用户名',
   PHONE                varchar(16) not null comment '手机号',
   EMAIL                varchar(32) comment '邮箱',
   STATE                int not null comment '状态',
   LOGIN_IP             int comment '登录IP',
   CREATE_TIME          datetime not null comment '创建时间',
   CREATE_ID            bigint comment '创建人',
   REAL_NAME            varchar(32) comment '真实姓名',
   LOGO                 varchar(256) comment '用户头像',
   APPLICATION_ID       bigint not null comment '应用ID',
   primary key (ID)
);

alter table T_USER comment '系统用户';


drop table if exists T_USER_ROLE;

/*==============================================================*/
/* Table: T_USER_ROLE                                           */
/*==============================================================*/
create table T_USER_ROLE
(
   ID                   bigint not null comment 'ID',
   USER_ID              bigint not null comment '用户ID',
   ROLE_ID              bigint not null comment '角色ID',
   primary key (ID)
);

alter table T_USER_ROLE comment '用户角色';


drop table if exists T_APPLICATION;

/*==============================================================*/
/* Table: T_APPLICATION                                         */
/*==============================================================*/
create table T_APPLICATION
(
   ID                   bigint not null,
   NAME                 varchar(32) not null comment '应用名称',
   CODE                 varchar(16) comment '应用CODE',
   STATE                int not null comment '状态',
   primary key (ID)
);

alter table T_APPLICATION comment '系统应用';


drop table if exists T_ROLE;

/*==============================================================*/
/* Table: T_ROLE                                                */
/*==============================================================*/
create table T_ROLE
(
   ID                   bigint not null comment 'ID',
   NAME                 varchar(64) not null comment '角色名',
   CODE                 varchar(32) not null comment '角色编码',
   DESCRIPTION          varchar(128) comment '角色描述',
   CREATE_USER          bigint not null comment '创建人',
   CREATE_TIME          datetime not null comment '创建时间',
   APPLICATION_ID       bigint not null comment '应用ID',
   STATE                int not null comment '状态',
   primary key (ID)
);

alter table T_ROLE comment '系统角色';


drop table if exists T_ROLE_RESORCE;

/*==============================================================*/
/* Table: T_ROLE_RESORCE                                        */
/*==============================================================*/
create table T_ROLE_RESORCE
(
   ID                   bigint not null comment 'ID',
   ROLE_ID              bigint not null comment '角色ID',
   RES_ID               bigint not null comment '资源ID',
   primary key (ID)
);

alter table T_ROLE_RESORCE comment '资源角色';


drop table if exists T_RESOURCE;

/*==============================================================*/
/* Table: T_RESOURCE                                            */
/*==============================================================*/
create table T_RESOURCE
(
   ID                   bigint not null comment 'ID',
   NAME                 varchar(64) not null comment '资源名称',
   CODE                 varchar(16) comment '资源编号',
   SUP_ID               bigint not null comment '父菜单ID',
   PATH                 varchar(64) not null comment '菜单路径(id组成)',
   DEPTH                int not null comment '菜单级别',
   URL                  varchar(128) not null comment '菜单URL',
   SEQ                  int comment '菜单排序',
   ICON                 varchar(64) comment '菜单图标',
   TYPE                 int comment '菜单类型',
   CREATE_TIME          datetime not null comment '创建时间',
   CREATE_USER          bigint comment '创建人',
   UPDATE_TIME          datetime comment '更新时间',
   UPDATE_USER          bigint comment '更新人',
   STATE                int not null comment '状态(0 启用 1禁用)',
   APPLICATION_ID     bigint not null comment '应用ID',
   primary key (ID)
);

alter table T_RESOURCE comment '资源菜单';
