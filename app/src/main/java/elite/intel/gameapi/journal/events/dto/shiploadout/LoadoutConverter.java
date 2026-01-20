package elite.intel.gameapi.journal.events.dto.shiploadout;

import elite.intel.gameapi.journal.events.LoadoutEvent;

import java.util.ArrayList;
import java.util.List;

import static elite.intel.util.StringUtls.toReadableModuleName;

public class LoadoutConverter {

    public static ShipLoadOutDto toShipLoadOutDto(LoadoutEvent event) {
        ShipLoadOutDto dto = new ShipLoadOutDto();
        dto.setCargoCapacity(event.getCargoCapacity());
        FuelCapacityDto fuelCapacity = new FuelCapacityDto();
        fuelCapacity.setMainTank(event.getFuelCapacity().getMain());
        fuelCapacity.setReserveTank(event.getFuelCapacity().getReserve());
        dto.setFuelCapacity(fuelCapacity);
        dto.setHullHealth(event.getHullHealth() * 100f);
        dto.setMaxJumpRange(Math.round(event.getMaxJumpRange() * 100.0) / 100.0);
        dto.setModules(toModules(event.getModules()));
        dto.setModulesValue(event.getModulesValue());
        dto.setInsurance(event.getRebuy());
        dto.setShipId(event.getShipId());
        dto.setShipIdent(event.getShipIdent());
        dto.setShipMake(event.getShip());
        dto.setShipName(event.getShipName());
        dto.setUnladenMass(event.getUnladenMass());
        return dto;
    }

    private static List<ModuleDto> toModules(List<LoadoutEvent.Module> modules) {
        ArrayList<ModuleDto> result = new ArrayList<>();
        for (LoadoutEvent.Module module : modules) {
            result.add(toModule(module));
        }
        return result;
    }

    private static ModuleDto toModule(LoadoutEvent.Module module) {
        ModuleDto dto = new ModuleDto();
        dto.setSlot(toReadableModuleName(module.getSlot()));
        dto.setItem(toReadableModuleName(module.getItem()));
        dto.setOn(module.isOn());
        dto.setPriority(module.getPriority());
        dto.setHealthPercentage(module.getHealth() * 100f);
        dto.setValue(module.getValue());
        dto.setAmmoInClip(module.getAmmoInClip());
        dto.setAmmoInHopper(module.getAmmoInHopper());
        dto.setEngineering(toEngineering(module.getEngineering()));
        return dto;
    }

    private static EngineeringDto toEngineering(LoadoutEvent.Engineering engineering) {
        if (engineering == null) {
            return null;
        }
        EngineeringDto dto = new EngineeringDto();
        dto.setEngineer(engineering.getEngineer());
        dto.setBlueprintName(toReadableModuleName(engineering.getBlueprintName()));
        dto.setQuality(engineering.getQuality());
        dto.setExperimentalEffectLocalised(engineering.getExperimentalEffectLocalised());
        dto.setModifiers(toModifiers(engineering.getModifiers()));
        return dto;
    }

    private static List<ModifierDto> toModifiers(List<LoadoutEvent.Modifier> modifiers) {
        if (modifiers == null) {
            return null;
        }
        ArrayList<ModifierDto> result = new ArrayList<>();
        for (LoadoutEvent.Modifier modifier : modifiers) {
            result.add(toModifier(modifier));
        }
        return result;
    }

    private static ModifierDto toModifier(LoadoutEvent.Modifier modifier) {
        ModifierDto dto = new ModifierDto();
        dto.setLabel(toReadableModuleName(modifier.getLabel()));
        dto.setValue(modifier.getValue());
        return dto;
    }
}