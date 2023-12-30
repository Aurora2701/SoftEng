package it.polito.ezshop.data;

public class UserClass implements User {

    private Integer id;
    private String username;
    private String password;
    private String role;

    public UserClass(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        if (id>0)
          this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        if (username != null && !username.isEmpty())
            this.username = username;
    }

    @Override
    public String getPassword() {
      return password;
    }

    @Override
    public void setPassword(String password) {
        if (password != null && !password.isEmpty())
            this.password = password;
    }

    @Override
    public String getRole() {
       return role;
    }

    @Override
    public void setRole(String role) {
        if (role != null && !role.isEmpty())
           this.role = role;
    }
}
