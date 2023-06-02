import java.io.*;
import java.util.List;

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

    public static void readAuthFile(UserInfoForGit info,String filePath) {

        info.setFilePath(filePath);

        try (FileInputStream fis = new FileInputStream(filePath);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader reader = new BufferedReader(isr)){

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("ID=")) {
                    info.setID(line.substring(3));
                } else if (line.startsWith("Token=")) {
                    info.setToken(line.substring(6));
                }
            }

            if(!info.getID().isEmpty() && !info.getToken().isEmpty()) {
                info.hasInfo = true;
                return;
            }
            else {
                info.hasInfo = false;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        return;
    }

    public static void writeAuthFile(UserInfoForGit info, String filePath){

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
            writer.write("ID=" + info.getID());
            writer.newLine();
            writer.write("Token=" + info.getToken());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return;
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
