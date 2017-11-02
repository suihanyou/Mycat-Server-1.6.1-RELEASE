package io.mycat.config.loader.zkprocess.zktoxml;

import io.mycat.config.model.UserConfig;

/**
 * <p>
 * Title: MycatNodeUser.java
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 * 
 * @author SHY 2017年10月31日
 * @version 1.0
 */
public class MycatNodeUser {
    private String userName;
    private String passwd;
    private String encryptPassword;

    public MycatNodeUser(UserConfig userConfig) {
        this.userName = userConfig.getName();
        this.passwd = userConfig.getPassword();
        this.encryptPassword = userConfig.getEncryptPassword();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getEncryptPassword() {
        return encryptPassword;
    }

    public void setEncryptPassword(String encryptPassword) {
        this.encryptPassword = encryptPassword;
    }
}
