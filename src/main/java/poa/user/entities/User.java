package poa.user.entities;

public class User {

    private final String name;
    private final String userName;
    private final String lastName;
    private final String email;
    private final String password;
    private String hashedPassword;

    public User(String name, String lastName, String username, String email, String password) {
        this.name = name;
        this.email = email;
        this.userName = username;
        this.lastName = lastName;
        this.password = password;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getName() {
        return name;
    }

    public String getUserName() {
        return userName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

}
