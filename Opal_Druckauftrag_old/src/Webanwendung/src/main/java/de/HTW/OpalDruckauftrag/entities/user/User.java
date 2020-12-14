package de.HTW.OpalDruckauftrag.entities.user;


import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Entity
@Table(name="User")
/**
 * user-Entity ermöglicht OR-DB-Mapping für Spring Security
 * @author Francesco Ryplewitz
 * @version 1.2
 */
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotEmpty(message = "username is required")
    @Column(unique = true,name="username")
    private String username;

    @NotEmpty(message = "password is required")
    @Column(name = "password")
    private String password;

    /**
     * Spring-Data-JPA benötigt leeren Constructor
     */
    public User(){}

    /**
     * Constructor mit Parameterübergabe
     * @param username
     * @param password
     */
    public User( String username, String password) {
        this.username=username;
        this.password=password;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
