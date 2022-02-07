package ru.geekbrains.netty_model_obj;

public class AuthenticationObj extends AbstractObj {

    private String login;
    private String password;

    public AuthenticationObj() {
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}


