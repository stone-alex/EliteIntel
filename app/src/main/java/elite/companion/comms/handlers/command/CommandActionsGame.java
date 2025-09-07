package elite.companion.comms.handlers.command;

/**
 * Represents the available actions and their associated game commands for
 * the in-game control system. The class helps in mapping specific actions
 * to their respective commands, which are linked with identifiers and
 * associated classes for controlling the game behavior.
 */
public class CommandActionsGame {
    /**
     * Enumeration representing various game commands and their associated attributes.
     * Each game command is linked with a binding name, an action, and a controller class.
     * This enum is used to map specific in-game actions to identifiers for processing and handling user inputs.
     */
    public enum GameCommand {
        CARGO_SCOOP("cargo_scoop", "ToggleCargoScoop", GenericGameController.class),
        CARGO_SCOOP_BUGGY("cargo_scoop_buggy", "ToggleCargoScoop_Buggy", GenericGameController.class),
        CHANGE_CONSTRUCTION_OPTION("change_construction_option", "ChangeConstructionOption", GenericGameController.class),
        CYCLE_NEXT_PAGE("cycle_next_page", "CycleNextPage", GenericGameController.class),
        CYCLE_NEXT_PANEL("cycle_next_panel", "CycleNextPanel", GenericGameController.class),
        CYCLE_NEXT_SUBSYSTEM("cycle_next_subsystem", "CycleNextSubsystem", GenericGameController.class),
        CYCLE_PREVIOUS_PAGE("cycle_previous_page", "CyclePreviousPage", GenericGameController.class),
        CYCLE_PREVIOUS_PANEL("cycle_previous_panel", "CyclePreviousPanel", GenericGameController.class),
        CYCLE_PREVIOUS_SUBSYSTEM("cycle_previous_subsystem", "CyclePreviousSubsystem", GenericGameController.class),
        DEPLOY_HARDPOINT_TOGGLE("weapons_hot", "DeployHardpointToggle", GenericGameController.class),
        RETRACT_HARDPOINTS("retract_hardpoints", "DeployHardpointToggle", GenericGameController.class),
        DEPLOY_HEAT_SINK("deploy_heat_sink", "DeployHeatSink", GenericGameController.class),
        DRIVE_ASSIST("drive_assist", "ToggleDriveAssist", GenericGameController.class),
        EJECT_ALL_CARGO("eject_all_cargo", "EjectAllCargo", GenericGameController.class),
        EJECT_ALL_CARGO_BUGGY("eject_all_cargo_buggy", "EjectAllCargo_Buggy", GenericGameController.class),
        EXIT_SETTLEMENT_PLACEMENT_CAMERA("exit_settlement_placement_camera", "ExitSettlementPlacementCamera", GenericGameController.class),

        EXPLORATION_FSSENTER("show_fss", "ExplorationFSSEnter", GenericGameController.class),
        EXPLORATION_FSSQUIT("exit_fss", "ExplorationFSSQuit", GenericGameController.class),
        EXPLORATION_SAACHANGE_SCANNED_AREA_VIEW_TOGGLE("exploration_saachange_scanned_area_view_toggle", "ExplorationSAAChangeScannedAreaViewToggle", GenericGameController.class),
        EXPLORATION_SAANEXT_GENUS("next_lifeform", "ExplorationSAANextGenus", GenericGameController.class),
        EXPLORATION_SAAPREVIOUS_GENUS("previous_lifeform", "ExplorationSAAPreviousGenus", GenericGameController.class),
        //FLIGHT_ASSIST("flight_assist", "ToggleFlightAssist", GenericGameController.class),
        FOCUS_COMMS_PANEL("focus_comms_panel", "FocusCommsPanel", GenericGameController.class),
        FOCUS_COMMS_PANEL_BUGGY("focus_comms_panel_buggy", "FocusCommsPanel_Buggy", GenericGameController.class),
        FOCUS_COMMS_PANEL_HUMANOID("focus_comms_panel_humanoid", "FocusCommsPanel_Humanoid", GenericGameController.class),
        FOCUS_LEFT_PANEL("focus_navigation_panel", "FocusLeftPanel", GenericGameController.class),
        FOCUS_CONTACTS_PANEL("focus_contacts_panel", "FocusLeftPanel", GenericGameController.class),
        FOCUS_LEFT_PANEL_BUGGY("focus_navigation_panel_buggy", "FocusLeftPanel_Buggy", GenericGameController.class),
        FOCUS_RADAR_PANEL("focus_radar_panel", "FocusRadarPanel", GenericGameController.class),
        FOCUS_RADAR_PANEL_BUGGY("focus_radar_panel_buggy", "FocusRadarPanel_Buggy", GenericGameController.class),
        FOCUS_RIGHT_PANEL("focus_right_panel", "FocusRightPanel", GenericGameController.class),
        FOCUS_RIGHT_PANEL_BUGGY("focus_right_panel_buggy", "FocusRightPanel_Buggy", GenericGameController.class),
        FORWARD_KEY("forward_key", "ForwardKey", GenericGameController.class),
        FREE_CAM("free_cam", "ToggleFreeCam", GenericGameController.class),
        FSTOP_DEC("fstop_dec", "FStopDec", GenericGameController.class),
        FSTOP_INC("fstop_inc", "FStopInc", GenericGameController.class),
        GALNET_AUDIO_CLEAR_QUEUE("galnet_audio_clear_queue", "GalnetAudio_ClearQueue", GenericGameController.class),
        GALNET_AUDIO_PLAY_PAUSE("galnet_audio_play_pause", "GalnetAudio_Play_Pause", GenericGameController.class),
        GALNET_AUDIO_SKIP_BACKWARD("galnet_audio_skip_backward", "GalnetAudio_SkipBackward", GenericGameController.class),
        GALNET_AUDIO_SKIP_FORWARD("galnet_audio_skip_forward", "GalnetAudio_SkipForward", GenericGameController.class),
        HYPER_SUPER_COMBINATION("hyper_super_combination", "HyperSuperCombination", GenericGameController.class),

        ENTER_SUPERCRUISE("enter_supercruise", "Supercruise", GenericGameController.class),
        EXIT_SUPERCRUISE("exit_supercruise", "Supercruise", GenericGameController.class),
        //TOGGLE_FTL("toggle_ftl", "Supercruise", GenericGameController.class),
        JUMP_TO_HYPERSPACE("jump_to_hyperspace", "Hyperspace", GenericGameController.class),


        HEAD_LOOK_RESET("head_look_reset", "HeadLookReset", GenericGameController.class),
        LANDING_GEAR_TOGGLE("landing_gear_toggle", "LandingGearToggle", GenericGameController.class),
        NIGHT_VISION("night_vision", "NightVisionToggle", GenericGameController.class),
        OPEN_CODEX_GO_TO_DISCOVERY("open_codex_go_to_discovery", "OpenCodexGoToDiscovery", GenericGameController.class),
        OPEN_CODEX_GO_TO_DISCOVERY_BUGGY("open_codex_go_to_discovery_buggy", "OpenCodexGoToDiscovery_Buggy", GenericGameController.class),
        PAUSE("pause", "Pause", GenericGameController.class),
        PLAYER_HUDMODE_TOGGLE("combat_or_analysis_mode", "PlayerHUDModeToggle", GenericGameController.class),
        QUICK_COMMS_PANEL_BUGGY("quick_comms_panel_buggy", "QuickCommsPanel_Buggy", GenericGameController.class),
        QUICK_COMMS_PANEL_HUMANOID("quick_comms_panel_humanoid", "QuickCommsPanel_Humanoid", GenericGameController.class),
        RADAR_DECREASE_RANGE("radar_decrease_range", "RadarDecreaseRange", GenericGameController.class),
        RADAR_INCREASE_RANGE("radar_increase_range", "RadarIncreaseRange", GenericGameController.class),
        RECALL_DISMISS_SHIP("recall_dismiss_ship", "RecallDismissShip", GenericGameController.class),
        REQUEST_DEFENSIVE_BEHAVIOUR("fighter_defensive_behaviour", "OrderDefensiveBehaviour", GenericGameController.class),
        REQUEST_FOCUS_TARGET("fighter_attack_focus_target", "OrderFocusTarget", GenericGameController.class),
        REQUEST_HOLD_FIRE("fighter_hold_your_fire", "OrderHoldFire", GenericGameController.class),
        REQUEST_REQUEST_DOCK("fighter_return_to_base", "OrderRequestDock", GenericGameController.class),
        RESET_POWER_DISTRIBUTION("equalize_power", "ResetPowerDistribution", GenericGameController.class),
        //RESET_POWER_DISTRIBUTION_BUGGY("equalize_power_buggy", "ResetPowerDistribution_Buggy", GenericGameController.class),
        SELECT_TARGETS_TARGET("select_wingman_target", "SelectTargetsTarget", GenericGameController.class),
        SET_SPEED100("set_speed_to_maximum", "SetSpeed100", GenericGameController.class),
        SET_SPEED25("set_speed_to_low", "SetSpeed25", GenericGameController.class),
        PLANETARY_APPROACH_SPEED75("planetary_approach", "SetSpeed25", GenericGameController.class),
        SET_SPEED50("set_speed_to_medium", "SetSpeed50", GenericGameController.class),
        SET_SPEED75("set_speed_to_optimal", "SetSpeed75", GenericGameController.class),
        SET_SPEED_ZERO("set_speed_to_zero", "SetSpeedZero", GenericGameController.class),
        AUTO_DOC1("set_auto_pilot", "SetSpeedZero", GenericGameController.class),
        AUTO_DOC2("take_us_in", "SetSpeedZero", GenericGameController.class),
        AUTO_DOC3("automatic_docking", "SetSpeedZero", GenericGameController.class),
        TARGET_NEXT_ROUTE_SYSTEM("target_next_system_in_route", "TargetNextRouteSystem", GenericGameController.class),
        TARGET_WINGMAN0("target_wingman0", "TargetWingman0", GenericGameController.class),
        TARGET_WINGMAN1("target_wingman1", "TargetWingman1", GenericGameController.class),
        TARGET_WINGMAN2("target_wingman2", "TargetWingman2", GenericGameController.class),
        SELECT_HIGHEST_THREAT("select_highest_threat", "SelectHighestThreat", GenericGameController.class),
        UI_ACTIVATE("ui_activate", "UI_Select", GenericGameController.class),
        WING_NAV_LOCK("lock_on_wingman", "WingNavLock", GenericGameController.class),


        //excluded, but needed for bindings. used in custom handler
        //these are special cases for excluded commands not sent to Grok, handled via a custom action,
        EXIT_KEY("ui_exit", "UI_Back", GenericGameController.class),
        INCREASE_ENGINES_POWER("_", "IncreaseEnginesPower", GenericGameController.class),
        INCREASE_SYSTEMS_POWER("_", "IncreaseSystemsPower", GenericGameController.class),
        INCREASE_SHIELDS_POWER("_", "IncreaseSystemsPower", GenericGameController.class),
        INCREASE_WEAPONS_POWER("_", "IncreaseWeaponsPower", GenericGameController.class),
        UI_DOWN("ui_down", "UI_Down", GenericGameController.class),
        UI_LEFT("ui_left", "UI_Left", GenericGameController.class),
        UI_RIGHT("ui_right", "UI_Right", GenericGameController.class),
        UIFOCUS("ui_focus", "UIFocus", GenericGameController.class),
        UI_SELECT("ui_select", "UI_Select", GenericGameController.class),
        UI_TOGGLE("ui_toggle", "UI_Toggle", GenericGameController.class),
        UI_UP("ui_up", "UI_Up", GenericGameController.class),
        GALAXY_MAP("gm", "GalaxyMapOpen", OpenGalaxyMapHandler.class),
        GALAXY_MAP_BUGGY("gm_buggy", "GalaxyMapOpen_Buggy", GenericGameController.class),
        GALAXY_MAP_HOME("gm_home", "GalaxyMapHome", GenericGameController.class),
        GALAXY_MAP_HUMANOID("gm_humanoid", "GalaxyMapOpen_Humanoid", GenericGameController.class),
        SYSTEM_MAP("system_map", "SystemMapOpen", GenericGameController.class),
        SYSTEM_MAP_HUMANOID("system_map_humanoid", "SystemMapOpen_Humanoid", GenericGameController.class),


        EXPLORATION_FSSDISCOVERY_SCAN("_", "", PerformFSSScanHandler.class);

        private final String userCommand;
        private final String gameBinding;
        private final Class<? extends CommandHandler> handlerClass;

        GameCommand(String userCommand, String gameBinding, Class<? extends CommandHandler> handlerClass) {
            this.userCommand = userCommand;
            this.gameBinding = gameBinding;
            this.handlerClass = handlerClass;
        }

        public String getUserCommand() {
            return userCommand;
        }

        public String getGameBinding() {
            return gameBinding;
        }

        public Class<? extends CommandHandler> getHandlerClass() {
            return handlerClass;
        }
    }

    public static String getGameBinding(String userCommand) {
        for (GameCommand command : GameCommand.values()) {
            if (command.getUserCommand().equals(userCommand)) {
                return command.getGameBinding();
            }
        }
        return null;
    }

    public static String[] getUserCommands() {
        String[] commands = new String[GameCommand.values().length];
        for (int i = 0; i < GameCommand.values().length; i++) {
            // special cases for excluded commands
            if (GameCommand.values()[i] == GameCommand.INCREASE_ENGINES_POWER) continue;
            if (GameCommand.values()[i] == GameCommand.INCREASE_SHIELDS_POWER) continue;
            if (GameCommand.values()[i] == GameCommand.INCREASE_SYSTEMS_POWER) continue;
            if (GameCommand.values()[i] == GameCommand.INCREASE_WEAPONS_POWER) continue;
            if (GameCommand.values()[i] == GameCommand.EXPLORATION_FSSDISCOVERY_SCAN) continue;
            if (GameCommand.values()[i] == GameCommand.UI_DOWN) continue;
            if (GameCommand.values()[i] == GameCommand.UI_LEFT) continue;
            if (GameCommand.values()[i] == GameCommand.UI_RIGHT) continue;
            if (GameCommand.values()[i] == GameCommand.UI_UP) continue;
            if (GameCommand.values()[i] == GameCommand.UI_TOGGLE) continue;
            if (GameCommand.values()[i] == GameCommand.GALAXY_MAP) continue;
            if (GameCommand.values()[i] == GameCommand.GALAXY_MAP_BUGGY) continue;
            if (GameCommand.values()[i] == GameCommand.GALAXY_MAP_HOME) continue;
            if (GameCommand.values()[i] == GameCommand.GALAXY_MAP_HUMANOID) continue;
            if (GameCommand.values()[i] == GameCommand.SYSTEM_MAP) continue;
            if (GameCommand.values()[i] == GameCommand.SYSTEM_MAP_HUMANOID) continue;
            if (GameCommand.values()[i] == GameCommand.EXIT_KEY) continue;

            commands[i] = GameCommand.values()[i].getUserCommand();
        }
        return commands;
    }
}