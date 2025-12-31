package elite.intel.search.eddn.mappers;

import elite.intel.gameapi.journal.events.ScanEvent;
import elite.intel.search.eddn.schemas.ScanEventJournalMessage;

public class ScanEventJournalMapper {

    public static ScanEventJournalMessage map(ScanEvent event) {
        ScanEventJournalMessage msg = new ScanEventJournalMessage();

        msg.setTimestamp(event.getTimestamp());
        msg.setStarSystem(event.getStarSystem());
        msg.setSystemAddress(event.getSystemAddress());
        msg.setScanType(event.getScanType());
        msg.setBodyName(event.getBodyName());
        msg.setBodyID(event.getBodyID());
        msg.setParents(event.getParents());
        msg.setDistanceFromArrivalLS(event.getDistanceFromArrivalLS());
        msg.setTidalLock(event.isTidalLock());
        msg.setTerraformState(event.getTerraformState());
        msg.setPlanetClass(event.getPlanetClass());
        msg.setAtmosphere(event.getAtmosphere());
        msg.setAtmosphereType(event.getAtmosphereType());
        msg.setVolcanism(event.getVolcanism());
        msg.setMassEM(event.getMassEM());
        msg.setRadius(event.getRadius());
        msg.setSurfaceGravity(event.getSurfaceGravity());
        msg.setSurfaceTemperature(event.getSurfaceTemperature());
        msg.setSurfacePressure(event.getSurfacePressure());
        msg.setLandable(event.isLandable());
        msg.setMaterials(event.getMaterials());
        msg.setComposition(event.getComposition());
        msg.setSemiMajorAxis(event.getSemiMajorAxis());
        msg.setEccentricity(event.getEccentricity());
        msg.setOrbitalInclination(event.getOrbitalInclination());
        msg.setPeriapsis(event.getPeriapsis());
        msg.setOrbitalPeriod(event.getOrbitalPeriod());
        msg.setAscendingNode(event.getAscendingNode());
        msg.setMeanAnomaly(event.getMeanAnomaly());
        msg.setRotationPeriod(event.getRotationPeriod());
        msg.setAxialTilt(event.getAxialTilt());
        msg.setWasDiscovered(event.isWasDiscovered());
        msg.setWasMapped(event.isWasMapped());
        msg.setStarType(event.getStarType());
        return msg;
    }
}