package com.sivamachineworks.platform.auth.dto;

import java.util.List;
import java.util.UUID;

public class CurrentUserResponse {
    
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String plantLocation;
    private String department;
    private List<String> roles;
    private List<String> permissions;

    public CurrentUserResponse() {
    }

    public CurrentUserResponse(UUID id, String username, String email, String firstName, String lastName, String plantLocation, String department, List<String> roles, List<String> permissions) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.plantLocation = plantLocation;
        this.department = department;
        this.roles = roles;
        this.permissions = permissions;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPlantLocation() { return plantLocation; }
    public void setPlantLocation(String plantLocation) { this.plantLocation = plantLocation; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
