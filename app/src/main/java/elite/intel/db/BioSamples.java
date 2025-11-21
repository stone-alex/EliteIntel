package elite.intel.db;

import elite.intel.db.dao.BioSampleDao;
import elite.intel.db.util.Database;
import elite.intel.gameapi.journal.events.dto.BioSampleDto;
import elite.intel.util.json.GsonFactory;

import java.util.List;

public class BioSamples {
    private static final BioSamples INSTANCE = new BioSamples();

    private BioSamples() {
    }

    public static BioSamples getInstance() {
        return INSTANCE;
    }

    public List<BioSampleDto> listAll() {
        return Database.withDao(BioSampleDao.class, dao -> {
            BioSampleDao.BioSample[] bioSamples = dao.listAll();
            List<BioSampleDto> result = new java.util.ArrayList<>();
            for (BioSampleDao.BioSample sample : bioSamples) {
                result.add(GsonFactory.getGson().fromJson(sample.getJson(), BioSampleDto.class));
            }
            return result;
        });
    }

    public void addInBulk(List<BioSampleDto> data) {
        Database.withDao(BioSampleDao.class, dao ->{
            for (BioSampleDto sample : data) {
                BioSampleDao.BioSample entity = new BioSampleDao.BioSample();
                entity.setJson(sample.toJson());
                entity.setKey(sample.getKey());
                dao.upsert(entity);
            }
            return null;
        });
    }

    public void add(BioSampleDto bioSampleDto) {
        Database.withDao(BioSampleDao.class, dao ->{
            BioSampleDao.BioSample entity = new BioSampleDao.BioSample();
            entity.setJson(bioSampleDto.toJson());
            entity.setKey(bioSampleDto.getKey());
            dao.upsert(entity);
            return null;
        });
    }

    public void clear() {
        Database.withDao(BioSampleDao.class, dao ->{
            dao.clear();
            return null;
        });
    }
}
