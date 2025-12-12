package elite.intel.search.eddn.schemas;

import com.google.gson.annotations.SerializedName;

public class EddnHeader {
    @SerializedName("softwareName")
    private String softwareName = "Elite Intel";
    @SerializedName("softwareVersion")
    private String softwareVersion = "v2025.12.10.beta-0158";
    @SerializedName("uploaderID")
    private String uploaderID;
    @SerializedName("gameversion")
    private String gameVersion = "4.3.0.1";
    @SerializedName("gamebuild")
    private String gameBuild = "r322188/r0 ";

    public EddnHeader(String uploaderID) {
        this.uploaderID = uploaderID;
    }

    public String getSoftwareName() {
        return softwareName;
    }


    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public void setSoftwareName(String softwareName) {
        this.softwareName = softwareName;
    }

    public String getUploaderID() {
        return uploaderID;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public String getGameBuild() {
        return gameBuild;
    }

    public void setGameBuild(String gameBuild) {
        this.gameBuild = gameBuild;
    }
}