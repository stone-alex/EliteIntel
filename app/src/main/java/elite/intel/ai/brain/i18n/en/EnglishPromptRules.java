package elite.intel.ai.brain.i18n.en;

import elite.intel.ai.brain.actions.Commands;
import elite.intel.ai.brain.actions.Queries;
import elite.intel.ai.brain.i18n.PromptLanguageRules;

public class EnglishPromptRules implements PromptLanguageRules {

    @Override
    public String languageName() {
        return "English";
    }

    @Override
    public String queryStarterExamples() {
        return "what, where, how, which, why, is, are, does, tell me, how much, how many";
    }

    @Override
    public String commandVerbExamples() {
        return "show / display / open / access / find / search / locate / activate / navigate / plot / deploy / retract / enable / disable / turn on / turn off";
    }

    @Override
    public String queryPhraseExamples() {
        return "where / tell me / how much / how many / any / what is / what are";
    }

    @Override
    public String disambiguationHints() {
        String activate = Commands.ACTIVATE.getAction();
        String lights = Commands.LIGHTS_ON_OFF.getAction();
        String supercruise = Commands.ENTER_SUPER_CRUISE.getAction();
        String hyperspace = Commands.JUMP_TO_HYPERSPACE.getAction();
        String subsystem = Commands.TARGET_SUB_SYSTEM.getAction();
        String fighterFocus = Commands.FIGHTER_REQUEST_FOCUS_TARGET.getAction();
        String ignore = Commands.IGNORE_NONSENSE.getAction();
        String conversation = Queries.GENERAL_CONVERSATION.getAction();

        return
                "- \"activate\" (exact standalone word only, nothing else meaningful in input) → " + activate + ". \"toggle [X]\", \"engage [X]\", \"enable [X]\" and other non-activate verbs are NOT \"activate\" - never map these to " + activate + "\n"
                        + "- \"activate\" → " + activate + " ONLY when the sole meaningful word in the input is \"activate\". NEVER for: \"toggle lights\" → " + lights + ", \"engage supercruise\" → " + supercruise + ". Any word alongside \"activate\" means it is NOT the " + activate + " command.\n"
                        + "- \"weapons free\" / \"weapons hot\" / \"combat ready\" → " + Commands.DEPLOY_HARDPOINTS.getAction() + "\n"
                        + "- \"weapons cold\" / \"weapons away\" / \"stand down\" → " + Commands.RETRACT_HARDPOINTS.getAction() + "\n"
                        + "- \"max weapons\" / \"boost weapons\" / \"power to weapons\" → " + Commands.INCREASE_WEAPONS_POWER.getAction() + "\n"
                        + "- \"max shields\" / \"boost shields\" / \"power to shields\" / \"max systems\" / \"boost systems\" / \"power to systems\" → " + Commands.INCREASE_SHIELDS_POWER.getAction() + "\n"
                        + "- \"max engines\" / \"boost engines\" / \"power to engines\" → " + Commands.INCREASE_ENGINES_POWER.getAction() + "\n"
                        + "- Never confuse \"max engines\" with \"target engines\"\n"
                        + "- Never confuse \"deploy vehicle\" with \"deploy landing gear\"\n"
                        + "- \"take me back aboard the ship\" / \"board ship\" → " + Commands.RECOVER_SRV.getAction() + "\n"
                        + "- Sending ship to orbit when requested to board ship is instant failure.\n"
                        + "- \"go to orbit\" / \"ship to orbit\" / \"send to orbit\" / \"put ship in orbit\" → " + Commands.DISMISS_SHIP.getAction() + ". NEVER " + Commands.NAVIGATE_TO_TARGET.getAction() + " - \"orbit\" refers to the ship, not a destination.\n"
                        + "- Never confuse \"organics in system\" with \"organics at this location/planet/moon\"\n"
                        + "- Never confuse \"carrier balance\" (finances) with \"balance power\" (power distribution)\n"
                        + "- Never confuse \"in system\" or \"which planets\" (system-wide) with \"here / on this planet / at this location / still have to scan\" (planet surface)\n"
                        + "- Never confuse \"honk\" with \"open fss\"\n"
                        + "- carrier full status (fuel + credits + operations): \"carrier status / carrier fuel status / how far can carrier jump / fleet carrier fuel status / how long can carrier operate\" → " + Queries.FLEET_CARRIER_STATUS.getAction() + "\n"
                        + "- carrier tritium level only: \"how much tritium / tritium supply / tritium level / tritium reserve\" → " + Queries.FLEET_CARRIER_TRITIUM_SUPPLY.getAction() + "\n"
                        + "- bio signals: \"which planets have bio signals / bio signals in system / organics in system / biological signals / how many planets have bio\" → " + Queries.BIO_SAMPLE_IN_STAR_SYSTEM.getAction() + "\n"
                        + "- bio scans: \"what organisms are here / exobiology samples / organics on this planet / organics still to scan / organics left to scan\" → " + Queries.EXOBIOLOGY_SAMPLES_ON_THIS_PLANET.getAction() + "\n"
                        + "- For EXPLICIT \"player profile\" (these two words, in this order, optionally followed by additional context words like \"summarize ranks\", \"summarize progress\") → '" + Queries.PLAYER_PROFILE_ANALYSIS.getAction() + "'. Any other phrasing that does NOT begin with \"player profile\", including rank, stats, progress, name, or commander - return " + ignore + " or " + conversation + ". This is an instant fail if triggered by anything else.\n"
                        + "- \"galaxy map\" / \"open galaxy map\" / \"display galaxy map\" / \"show galaxy map\" → " + Commands.OPEN_GALAXY_MAP.getAction() + " (NOT carrier management or any other panel)\n"
                        + "- \"system map\" / \"open system map\" / \"display system map\" / \"local map\" → " + Commands.OPEN_SYSTEM_MAP.getAction() + "\n"
                        + "- \"supercruise\" / \"go supercruise\" / \"enter supercruise\" → " + supercruise + " (NOT " + hyperspace + " - supercruise stays in-system)\n"
                        + "- \"navigate to active mission\" / \"go to mission\" / \"plot route to mission\" → " + Commands.NAVIGATE_TO_NEXT_MISSION.getAction() + " (NOT " + Queries.ANALYZE_MISSIONS.getAction() + " - navigation, not a query)\n"
                        + "- \"navigate to codex entry\" / \"navigate to next codex\" → " + Commands.NAVIGATE_TO_NEXT_BIO_SAMPLE.getAction() + " (travel to the sample, do NOT use " + Commands.DELETE_CODEX_ENTRY.getAction() + ")\n"
                        + "- \"cargo scoop\" / \"open cargo scoop\" / \"deploy cargo scoop\" / \"close cargo scoop\" / \"retract cargo scoop\" → " + Commands.TOGGLE_CARGO_SCOOP.getAction() + "\n"
                        + "- \"unbound keys\" / \"check key bindings\" / \"missing bindings\" / \"keybind check\" → " + Queries.KEY_BINDINGS_ANALYSIS.getAction() + " (this IS a valid game command, not meta-talk)\n"
                        + "- \"listen\" / \"listen up\" / \"wake up\" alone → " + Commands.WAKEUP.getAction() + "\n"
                        + "- \"listen [+ any instruction]\" → treat as a normal command/query\n"
                        + "- \"exit\" or \"close\" → " + Commands.EXIT_CLOSE.getAction() + "\n"
                        + "- \"drop\" alone / \"drop in\" / \"drop out\" → " + Commands.DROP_FROM_SUPER_CRUISE.getAction() + "\n"
                        + "- \"halt\" alone → " + Commands.SET_SPEED_ZERO.getAction() + "\n"
                        + "- \"taxi\" alone / \"auto docking\" / \"autopilot\" → " + Commands.TAXI.getAction() + " (automated ship approach and landing at a pad - not a ground vehicle)\n"
                        + "- \"lets go\" / \"jump to ...\" / \"enter hyperspace\" → " + hyperspace + "\n"
                        + "- \"confirm ...\" → only match confirm-requiring actions when \"confirm\" is literally in the input\n"
                        + "- \"clear ...\" → only match clear-requiring actions when \"clear\" is literally in the input\n"
                        + "- \"target wingman 1/2/3\" → their specific wingman actions\n"
                        + "- \"target next route system\" → " + Commands.TARGET_DESTINATION.getAction() + "\n"
                        + "- \"target most dangerous / highest threat\" → " + Commands.SELECT_HIGHEST_THREAT.getAction() + "\n"
                        + "- \"focus [my] target\" / \"focus on target\" → " + fighterFocus + " (NOT " + subsystem + ")\n"
                        + "- \"target fsd\" → " + subsystem + " ONLY. NEVER " + hyperspace + ". Targeting a subsystem is not engaging it.\n"
                        + "- \"target [anything else]\" → " + subsystem + ", key = the words after \"target\"\n"
                        + "- organics / biology / exobiology on a planet or here → " + Queries.EXOBIOLOGY_SAMPLES_ON_THIS_PLANET.getAction() + ", NOT geo/materials\n"
                        + "- organics / bio signals in a system or which planets → " + Queries.BIO_SAMPLE_IN_STAR_SYSTEM.getAction() + "\n"
                        + "- \"how much X do we have\" / \"do we have any X\" (specific item) → " + Queries.MATERIALS_INVENTORY.getAction() + " (handles both engineering materials AND cargo commodities)\n"
                        + "- \"what are we carrying\" / \"list cargo\" / \"cargo contents\" (no specific item) → " + Queries.CARGO_HOLD_CONTENTS.getAction() + "\n"
                        + "- \"geo signals / geological\" → " + Queries.QUERY_GEO_SIGNALS.getAction() + " (NOT " + Commands.FIND_BRAIN_TREES.getAction() + ")\n"
                        + "- \"find mission providers\" / \"find pirate mission providers\" → " + Commands.FIND_HUNTING_GROUNDS.getAction() + " (NOT fleet carrier)\n"
                        + "- profit from bounties is not profit from missions for bounties → '" + Queries.TOTAL_BOUNTIES.getAction() + "'\n"
                        + "- profit from missions is not profit from bounties for missions → '" + Queries.ANALYZE_MISSIONS.getAction() + "'\n"
                        + "- profit from discovery is not profit from bounties or missions → '" + Queries.EXPLORATION_PROFITS.getAction() + "'\n"
                        + "- HARD RULE: if the word \"honk\" appears anywhere in the input, the ONLY valid action is '" + Commands.HONK_THE_SYSTEM.getAction() + "'. No other action is permitted when \"honk\" is present.\n";
    }
}
