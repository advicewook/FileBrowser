import java.io.*;
import java.nio.Buffer;
import java.util.List;
import java.util.Objects;

public class UserInfoForGit {
    private String ID;
    private String token;
    private String filePath;
    private boolean hasInfo = false;

    public UserInfoForGit() {}

    public UserInfoForGit(String ID, String token) {
        this.ID = ID;
        this.token = token;
    }

    public UserInfoForGit(String ID, String token, String filePath){
        this.ID = ID;
        this.token = token;
        this.filePath = filePath;
    }

    public static void readAuthFile(UserInfoForGit info,String filePath) {

        info.setFilePath(filePath);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("ID=")) {
                    info.setID(line.substring(3));
                } else if (line.startsWith("Token=")) {
                    info.setToken(line.substring(6));
                }
            }

            reader.close();

            if(!info.getID().isEmpty() && !info.getToken().isEmpty()) {
                info.hasInfo = true;
            }
            else {
                info.hasInfo = false;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void writeAuthFile(UserInfoForGit info, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
            writer.write("ID=" + info.getID());
            writer.newLine();
            writer.write("Token=" + info.getToken());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public boolean isHasInfo() {
        return hasInfo;
    }

    public void setHasInfo(boolean hasInfo) {
        this.hasInfo = hasInfo;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
