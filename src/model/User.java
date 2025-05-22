package model;


public abstract class User{
    //Instance variables for user's identity and authentication
    private int usersID ;
    private String name ;
    private String surname ;
    private String email ;
    private String password ;

    //Constructors
    public User(int usersID, String name, String surname, String email, String password){
        this.usersID = usersID ;
        this.name = name ;
        this.surname = surname ;
        this.email = email ;
        this.password = password ;
    }

    //Encapsulation - getter and setter methods
    public int getUsersID() {
        return usersID;
    }

    public void setUsersID(int usersID) {
        this.usersID = usersID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    

    
}