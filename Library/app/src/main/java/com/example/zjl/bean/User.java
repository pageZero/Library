package com.example.zjl.bean;

import java.io.Serializable;

/**
 * Created by zjl on 2016/5/29.
 *
 */
public class User implements Serializable{

  //  {"id":{"$id":"573eed88b2f0f88684fe3f28"},"UserId":"123","UserName":"123",
    // "UserLoginPassword":"123","UserDepartment":"depart1","UserRole":"Common",
    // "Privilege":["\u4fee\u6539\u5bc6\u7801"],
    // "Enable":"YES","UserRoleArr":[{"RoleId":{"$id":"573c9ffe993f2cb8ed56695b"},
    // "RoleName":"Common"}]}

    private ID id;
    private String UserId;
    private String UserName;
    private String UserLoginPassword;
    private String UserDepartment;
    private String UserRole;
    private String[] Privilege;
    private String Enable;
    private Role[] UserRoleArr;


    public void setEnable(String enable) {
        Enable = enable;
    }

    public String getEnable() {
        return Enable;
    }

    public void setPrivilege(String[] privilege) {
        Privilege = privilege;
    }

    public String[] getPrivilege() {
        return Privilege;
    }

    public void setUserRole(String userRole) {
        UserRole = userRole;
    }

    public String getUserRole() {
        return UserRole;
    }

    public Role[] getRole() {
        return UserRoleArr;
    }

    public void setRole(Role[] role) {
        this.UserRoleArr = role;
    }

    public User(){
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        this.UserId = userId;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        this.UserName = userName;
    }

    public String getPassword() {
        return UserLoginPassword;
    }

    public void setPassword(String password) {
        this.UserLoginPassword = password;
    }

    public String getDepartment() {
        return UserDepartment;
    }

    public void setDepartment(String department) {
        this.UserDepartment = department;
    }



    public class ID{
        private String $id;

        public String getId() {
            return $id;
        }

        public void setId(String id) {
            this.$id = id;
        }
    }
    public class Role{
        private ID RoleId;
        private String RoleName;

        public ID getRoleId() {
            return RoleId;
        }

        public void setRoleId(ID roleId) {
            RoleId = roleId;
        }

        public String getRoleName() {
            return RoleName;
        }

        public void setRoleName(String roleName) {
            RoleName = roleName;
        }
    }

}
