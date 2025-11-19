package elite.intel.session;

import elite.intel.ai.search.edsm.dto.EncodedMaterialsDto;
import elite.intel.ai.search.edsm.dto.MaterialsDto;

import java.util.List;

public class MaterialsData extends SessionPersistence implements java.io.Serializable {

    private static final String HISTORY_DIR = "session/";
    private static final String MATERIALS = "materials";
    public static final String ENCODED_MATERIALS = "encodedMaterials";
    private MaterialsDto materialsDto;
    private EncodedMaterialsDto encodedMaterialsDto;


    private MaterialsData(String directory) {
        super(directory);
        ensureFileAndDirectoryExist("materials.json");
        registerField(MATERIALS, this::getMaterials, this::setMaterialsDto, MaterialsDto.class);
        registerField(ENCODED_MATERIALS, this::getEncodedMaterialsDto, this::setEncodedMaterialsDto, EncodedMaterialsDto.class);
        loadSavedStateFromDisk();
    }

    private static volatile MaterialsData instance;

    public static MaterialsData getInstance() {
        if (instance == null) {
            synchronized (MaterialsData.class) {
                if (instance == null) {
                    instance = new MaterialsData(HISTORY_DIR);
                }
            }
        }
        return instance;
    }

    public MaterialsDto getMaterials() {
        return materialsDto;
    }

    public void setMaterialsDto(MaterialsDto materialsDto) {
        this.materialsDto = materialsDto;
        save();
    }

    public EncodedMaterialsDto getEncodedMaterialsDto() {
        return encodedMaterialsDto;
    }

    public void setEncodedMaterialsDto(EncodedMaterialsDto encodedMaterialsDto) {
        this.encodedMaterialsDto = encodedMaterialsDto;
        save();
    }

    private void loadSavedStateFromDisk() {
        loadSession(MaterialsData.this::loadFields);
    }
}
