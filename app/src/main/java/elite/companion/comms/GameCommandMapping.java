package elite.companion.comms;

public class GameCommandMapping {
    public enum GameCommand {
        //ADVANCE_MODE("advance_mode", "ToggleAdvanceMode"),
        //AUTO_BREAK_BUGGY_BUTTON("auto_break_buggy_button", "AutoBreakBuggyButton"),
        BACKWARD_KEY("cancel_back_backout_ui", "BackwardKey"),

//        CAM_TRANSLATE_BACKWARD("cam_translate_backward", "CamTranslateBackward"),
//        CAM_TRANSLATE_DOWN("cam_translate_down", "CamTranslateDown"),
//        CAM_TRANSLATE_FORWARD("cam_translate_forward", "CamTranslateForward"),
//        CAM_TRANSLATE_LEFT("cam_translate_left", "CamTranslateLeft"),
//        CAM_TRANSLATE_RIGHT("cam_translate_right", "CamTranslateRight"),
//        CAM_TRANSLATE_UP("cam_translate_up", "CamTranslateUp"),
//        CAM_YAW_RIGHT("cam_yaw_right", "CamYawRight"),
//        CAM_ZOOM_IN("cam_zoom_in", "CamZoomIn"),
//        CAM_ZOOM_OUT("cam_zoom_out", "CamZoomOut"),

        CARGO_SCOOP("cargo_scoop", "ToggleCargoScoop"),
        CARGO_SCOOP_BUGGY("cargo_scoop_buggy", "ToggleCargoScoop_Buggy"),
        CHANGE_CONSTRUCTION_OPTION("change_construction_option", "ChangeConstructionOption"),
        CYCLE_NEXT_PAGE("cycle_next_page", "CycleNextPage"),
        CYCLE_NEXT_PANEL("cycle_next_panel", "CycleNextPanel"),
        CYCLE_NEXT_SUBSYSTEM("cycle_next_subsystem", "CycleNextSubsystem"),
        CYCLE_PREVIOUS_PAGE("cycle_previous_page", "CyclePreviousPage"),
        CYCLE_PREVIOUS_PANEL("cycle_previous_panel", "CyclePreviousPanel"),
        CYCLE_PREVIOUS_SUBSYSTEM("cycle_previous_subsystem", "CyclePreviousSubsystem"),
        DEPLOY_HARDPOINT_TOGGLE("deploy_hardpoint_toggle", "DeployHardpointToggle"),
        DEPLOY_HEAT_SINK("deploy_heat_sink", "DeployHeatSink"),
        DOWN_THRUST_BUTTON("down_thrust_button", "DownThrustButton"),
        DRIVE_ASSIST("drive_assist", "ToggleDriveAssist"),
        EJECT_ALL_CARGO("eject_all_cargo", "EjectAllCargo"),
        EJECT_ALL_CARGO_BUGGY("eject_all_cargo_buggy", "EjectAllCargo_Buggy"),
        ENGAGE_SUPERCRUISE("engage_supercruise", "Supercruise"),
        EXIT_SETTLEMENT_PLACEMENT_CAMERA("exit_settlement_placement_camera", "ExitSettlementPlacementCamera"),
        EXPLORATION_FSSDISCOVERY_SCAN("exploration_fssdiscovery_scan", "ExplorationFSSDiscoveryScan"),
        EXPLORATION_FSSENTER("exploration_fssenter", "ExplorationFSSEnter"),
        EXPLORATION_FSSQUIT("exploration_fssquit", "ExplorationFSSQuit"),
        EXPLORATION_SAACHANGE_SCANNED_AREA_VIEW_TOGGLE("exploration_saachange_scanned_area_view_toggle", "ExplorationSAAChangeScannedAreaViewToggle"),
        EXPLORATION_SAANEXT_GENUS("exploration_saanext_genus", "ExplorationSAANextGenus"),
        EXPLORATION_SAAPREVIOUS_GENUS("exploration_saaprevious_genus", "ExplorationSAAPreviousGenus"),
        FLIGHT_ASSIST("flight_assist", "ToggleFlightAssist"),
        FOCUS_COMMS_PANEL("focus_comms_panel", "FocusCommsPanel"),
        FOCUS_COMMS_PANEL_BUGGY("focus_comms_panel_buggy", "FocusCommsPanel_Buggy"),
        FOCUS_COMMS_PANEL_HUMANOID("focus_comms_panel_humanoid", "FocusCommsPanel_Humanoid"),
        FOCUS_LEFT_PANEL("focus_left_panel", "FocusLeftPanel"),
        FOCUS_LEFT_PANEL_BUGGY("focus_left_panel_buggy", "FocusLeftPanel_Buggy"),
        FOCUS_RADAR_PANEL("focus_radar_panel", "FocusRadarPanel"),
        FOCUS_RADAR_PANEL_BUGGY("focus_radar_panel_buggy", "FocusRadarPanel_Buggy"),
        FOCUS_RIGHT_PANEL("focus_right_panel", "FocusRightPanel"),
        FOCUS_RIGHT_PANEL_BUGGY("focus_right_panel_buggy", "FocusRightPanel_Buggy"),
        FORWARD_KEY("forward_key", "ForwardKey"),
        FREE_CAM("free_cam", "ToggleFreeCam"),
        FSTOP_DEC("fstop_dec", "FStopDec"),
        FSTOP_INC("fstop_inc", "FStopInc"),
        GALAXY_MAP("galaxy_map", "GalaxyMapOpen"),
        GALAXY_MAP_BUGGY("galaxy_map_buggy", "GalaxyMapOpen_Buggy"),
        GALAXY_MAP_HOME("galaxy_map_home", "GalaxyMapHome"),
        GALAXY_MAP_HUMANOID("galaxy_map_humanoid", "GalaxyMapOpen_Humanoid"),
        GALNET_AUDIO_CLEAR_QUEUE("galnet_audio_clear_queue", "GalnetAudio_ClearQueue"),
        GALNET_AUDIO_PLAY_PAUSE("galnet_audio_play_pause", "GalnetAudio_Play_Pause"),
        GALNET_AUDIO_SKIP_BACKWARD("galnet_audio_skip_backward", "GalnetAudio_SkipBackward"),
        GALNET_AUDIO_SKIP_FORWARD("galnet_audio_skip_forward", "GalnetAudio_SkipForward"),
        HYPER_SUPER_COMBINATION("hyper_super_combination", "HyperSuperCombination"),
        INCREASE_ENGINES_POWER("power_to_engines", "IncreaseEnginesPower"),
        INCREASE_ENGINES_POWER_BUGGY("increase_engines_power_buggy", "IncreaseEnginesPower_Buggy"),
        INCREASE_SYSTEMS_POWER("power_to_systems", "IncreaseSystemsPower"),
        INCREASE_SHIELDS_POWER("power_to_shields", "IncreaseSystemsPower"),
        INCREASE_SYSTEMS_POWER_BUGGY("increase_systems_power_buggy", "IncreaseSystemsPower_Buggy"),
        INCREASE_WEAPONS_POWER("power_to_weapons", "IncreaseWeaponsPower"),
        INCREASE_WEAPONS_POWER_BUGGY("increase_weapons_power_buggy", "IncreaseWeaponsPower_Buggy"),
        JUMP_TO_HYPERSPACE("jump_to_hyperspace", "Hyperspace"),
        LANDING_GEAR_TOGGLE("landing_gear_toggle", "LandingGearToggle"),
        NIGHT_VISION("night_vision", "NightVisionToggle"),
        OPEN_CODEX_GO_TO_DISCOVERY("open_codex_go_to_discovery", "OpenCodexGoToDiscovery"),
        OPEN_CODEX_GO_TO_DISCOVERY_BUGGY("open_codex_go_to_discovery_buggy", "OpenCodexGoToDiscovery_Buggy"),
        PAUSE("pause", "Pause"),
        PLAYER_HUDMODE_TOGGLE("combat_or_analysis_mode", "PlayerHUDModeToggle"),
        QUICK_COMMS_PANEL_BUGGY("quick_comms_panel_buggy", "QuickCommsPanel_Buggy"),
        QUICK_COMMS_PANEL_HUMANOID("quick_comms_panel_humanoid", "QuickCommsPanel_Humanoid"),
        QUIT_CAMERA("quit_camera", "QuitCamera"),
        RADAR_DECREASE_RANGE("radar_decrease_range", "RadarDecreaseRange"),
        RADAR_INCREASE_RANGE("radar_increase_range", "RadarIncreaseRange"),
        RECALL_DISMISS_SHIP("recall_dismiss_ship", "RecallDismissShip"),

        //Fighter Commands
        REQUEST_DEFENSIVE_BEHAVIOUR("fighter_defensive_behaviour", "OrderDefensiveBehaviour"),
        REQUEST_FOCUS_TARGET("fighter_attack_focus_target", "OrderFocusTarget"),
        REQUEST_HOLD_FIRE("fighter_hold_your_fire", "OrderHoldFire"),
        REQUEST_REQUEST_DOCK("fightert_return_to_base", "OrderRequestDock"),

        RESET_POWER_DISTRIBUTION("equalize_power", "ResetPowerDistribution"),
        RESET_POWER_DISTRIBUTION_BUGGY("reset_power_distribution_buggy", "ResetPowerDistribution_Buggy"),

        SELECT_TARGETS_TARGET("select_targets_target", "SelectTargetsTarget"),

        SET_SPEED100("set_speed100", "SetSpeed100"),
        SET_SPEED25("set_speed25", "SetSpeed25"),
        SET_SPEED50("set_speed50", "SetSpeed50"),
        SET_SPEED75("set_speed75", "SetSpeed75"),
        SET_SPEED_ZERO("set_speed_zero", "SetSpeedZero"),
        AUTO_DOC("engage_auto_docking_auto_pilot_bring_us_in", "SetSpeedZero"),

        SYSTEM_MAP("system_map", "SystemMapOpen"),
        SYSTEM_MAP_BUGGY("system_map_buggy", "SystemMapOpen_Buggy"),
        SYSTEM_MAP_HUMANOID("system_map_humanoid", "SystemMapOpen_Humanoid"),

        TARGET_NEXT_ROUTE_SYSTEM("target_next_route_system", "TargetNextRouteSystem"),
        TARGET_WINGMAN0("target_wingman0", "TargetWingman0"),
        TARGET_WINGMAN1("target_wingman1", "TargetWingman1"),
        TARGET_WINGMAN2("target_wingman2", "TargetWingman2"),

        UI_DOWN("ui_down", "UI_Down"),
        UI_LEFT("ui_left", "UI_Left"),
        UI_RIGHT("ui_right", "UI_Right"),
        UIFOCUS("uifocus", "UIFocus"),
        UI_SELECT("ui_select", "UI_Select"),
        UI_TOGGLE("ui_toggle", "UI_Toggle"),
        UI_UP("ui_up", "UI_Up"),
        UP_THRUST_BUTTON("up_thrust_button", "UpThrustButton"),
        USE_SHIELD_CELL("use_shield_cell", "UseShieldCell"),

        WING_NAV_LOCK("wing_nav_lock", "WingNavLock");



        private final String userCommand;
        private final String gameBinding;

        GameCommand(String userCommand, String gameBinding) {
            this.userCommand = userCommand;
            this.gameBinding = gameBinding;
        }

        public String getUserCommand() {
            return userCommand;
        }

        public String getGameBinding() {
            return gameBinding;
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
            commands[i] = GameCommand.values()[i].getUserCommand();
        }
        return commands;
    }
}
