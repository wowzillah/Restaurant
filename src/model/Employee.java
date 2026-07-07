package model;

import model.enums.Role;

import java.io.Serial;
import java.io.Serializable;

public class Employee implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private Role role;
    private String pinCode ; // 4 digits PIN for fast login
    private boolean active;

    public Employee(int id, Role role, String name, String pinCode) {
        this.pinCode = pinCode;
        this.role = role;
        this.name = name;
        this.id = id;
        this.active = true;
    }
  //Security Method

  public boolean verifyPin(String inputPin) {
        return this.pinCode.equals(inputPin);
  }

    public void setName(String name) {
        this.name = name;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public String getPinCode() {
        return pinCode;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return name + " (" + role + ")";
    }
}
