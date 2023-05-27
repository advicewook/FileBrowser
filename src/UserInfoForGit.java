public class UserInfoForGit {
    private String ID;
    private String token;

    public UserInfoForGit() {}

    public UserInfoForGit(String ID, String token) {
        this.ID = ID;
        this.token = token;
    }
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
