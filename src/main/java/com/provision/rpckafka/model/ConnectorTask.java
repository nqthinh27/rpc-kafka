package com.provision.rpckafka.model;

import com.google.gson.annotations.SerializedName;

public class ConnectorTask {

    private Long id;
    private String state;

    @SerializedName("worker_id")
    private String workerId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }
}
