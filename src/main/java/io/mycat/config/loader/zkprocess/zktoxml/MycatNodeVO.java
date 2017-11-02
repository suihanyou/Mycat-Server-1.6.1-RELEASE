package io.mycat.config.loader.zkprocess.zktoxml;

import io.mycat.config.model.UserConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Title: MycatNodeVO.java
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 * 
 * @author SHY 2017年10月30日
 * @version 1.0
 */
public class MycatNodeVO {
    private String url;
    private int weight;
    private Map<String, MycatNodeUser> users;

    public MycatNodeVO(String url, String weight, Map<String, UserConfig> userConfigs) {
        this.url = url;
        this.weight = Integer.parseInt(weight) <= 0 ? 100 : Integer.parseInt(weight);
        for (String key : userConfigs.keySet()) {
            users = new HashMap<String, MycatNodeUser>();
            users.put(key, new MycatNodeUser(userConfigs.get(key)));
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public Map<String, MycatNodeUser> getUsers() {
        return users;
    }

    public void setUsers(Map<String, MycatNodeUser> users) {
        this.users = users;
    }
}
