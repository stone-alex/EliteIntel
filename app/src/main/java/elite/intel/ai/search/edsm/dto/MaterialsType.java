package elite.intel.ai.search.edsm.dto;

public enum MaterialsType {
    MATERIAL("materials"), ENCODED("data"), ALL("all");

    private String type;

    MaterialsType(String type){
        this.type = type;
    }

    public String getType(){
        return this.type;
    }
}
