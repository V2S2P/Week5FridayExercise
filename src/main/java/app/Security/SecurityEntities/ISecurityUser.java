package app.Security.SecurityEntities;

public interface ISecurityUser {
    //    Set<String> getRolesAsStrings();
    boolean verifyPassword(String pw);
    void addRole(Role role);
    void removeRole(Role role);
}
