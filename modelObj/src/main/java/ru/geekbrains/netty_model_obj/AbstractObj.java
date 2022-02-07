package ru.geekbrains.netty_model_obj;

import java.io.Serializable;

public abstract class AbstractObj implements Serializable {
private Integer id;
private String message;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
