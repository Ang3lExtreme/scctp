package scc.Data.DTO;

public class Login {

    private String user, pwd;

    public Login(String user, String pwd) {
        this.user = user;
        this.pwd = pwd;
    }

    public Login(){}

    public String getUser() {
        return user;
    }

    public String getPwd() {
        return pwd;
    }

}
