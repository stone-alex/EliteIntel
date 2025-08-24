package elite.companion.comms;

public class GameCommandMapping {
    public enum GameCommand {
        VANITY_CAMERA_FIVE("vanity_camera_five", "VanityCameraFive"),
        GALAXY_MAP__BUGGY("galaxy_map__buggy", "GalaxyMapOpen_Buggy"),
        VANITY_CAMERA_EIGHT("vanity_camera_eight", "VanityCameraEight"),
        BACKWARD_KEY("backward_key", "BackwardKey"),
        OPEN_CODEX_GO_TO_DISCOVERY("open_codex_go_to_discovery", "OpenCodexGoToDiscovery"),
        FSTOP_DEC("fstop_dec", "FStopDec"),
        VANITY_CAMERA_NINE("vanity_camera_nine", "VanityCameraNine"),
        PHOTO_CAMERA__HUMANOID("photo_camera__humanoid", "PhotoCameraToggle_Humanoid"),
        GALNET_AUDIO__SKIP_BACKWARD("galnet_audio__skip_backward", "GalnetAudio_SkipBackward"),
        DRIVE_ASSIST("drive_assist", "ToggleDriveAssist"),
        CAM_TRANSLATE_DOWN("cam_translate_down", "CamTranslateDown"),
        MOVE_PLACEMENT_CAM_BACKWARDS("move_placement_cam_backwards", "MovePlacementCamBackwards"),
        MOVE_PLACEMENT_CAM_DOWN("move_placement_cam_down", "MovePlacementCamDown"),
        MOVE_FREE_CAM_UP("move_free_cam_up", "MoveFreeCamUp"),
        SELECT_TARGETS_TARGET("select_targets_target", "SelectTargetsTarget"),
        VANITY_CAMERA_TWO("vanity_camera_two", "VanityCameraTwo"),
        INCREASE_ENGINES_POWER__BUGGY("increase_engines_power__buggy", "IncreaseEnginesPower_Buggy"),
        INCREASE_SYSTEMS_POWER("increase_systems_power", "IncreaseSystemsPower"),
        SYSTEM_MAP("system_map", "SystemMapOpen"),
        MOVE_FREE_CAM_LEFT("move_free_cam_left", "MoveFreeCamLeft"),
        CARGO_SCOOP("cargo_scoop", "ToggleCargoScoop"),
        FOCUS_RIGHT_PANEL("focus_right_panel", "FocusRightPanel"),
        EXPLORATION_SAACHANGE_SCANNED_AREA_VIEW_TOGGLE("exploration_saachange_scanned_area_view_toggle", "ExplorationSAAChangeScannedAreaViewToggle"),
        MOVE_FREE_CAM_BACKWARDS("move_free_cam_backwards", "MoveFreeCamBackwards"),
        FREE_CAM("free_cam", "ToggleFreeCam"),
        FSTOP_INC("fstop_inc", "FStopInc"),
        RADAR_DECREASE_RANGE("radar_decrease_range", "RadarDecreaseRange"),
        EXPLORATION_SAANEXT_GENUS("exploration_saanext_genus", "ExplorationSAANextGenus"),
        FLIGHT_ASSIST("flight_assist", "ToggleFlightAssist"),
        INCREASE_ENGINES_POWER("increase_engines_power", "IncreaseEnginesPower"),
        ROLL_RIGHT_BUTTON("roll_right_button", "RollRightButton"),
        VANITY_CAMERA_ONE("vanity_camera_one", "VanityCameraOne"),
        ADVANCE_MODE("advance_mode", "ToggleAdvanceMode"),
        RECALL_DISMISS_SHIP("recall_dismiss_ship", "RecallDismissShip"),
        FOCUS_LEFT_PANEL__BUGGY("focus_left_panel__buggy", "FocusLeftPanel_Buggy"),
        GALAXY_MAP__HUMANOID("galaxy_map__humanoid", "GalaxyMapOpen_Humanoid"),
        REQUEST_FOCUS_TARGET("request_focus_target", "OrderFocusTarget"),
        PAUSE("pause", "Pause"),
        ROTATION_LOCK("rotation_lock", "ToggleRotationLock"),
        RADAR_INCREASE_RANGE("radar_increase_range", "RadarIncreaseRange"),
        FOCUS_RIGHT_PANEL__BUGGY("focus_right_panel__buggy", "FocusRightPanel_Buggy"),
        REQUEST_REQUEST_DOCK("request_request_dock", "OrderRequestDock"),
        EXPLORATION_FSSENTER("exploration_fssenter", "ExplorationFSSEnter"),
        QUICK_COMMS_PANEL__BUGGY("quick_comms_panel__buggy", "QuickCommsPanel_Buggy"),
        RESET_POWER_DISTRIBUTION("reset_power_distribution", "ResetPowerDistribution"),
        JUMP_TO_HYPERSPACE("jump_to_hyperspace", "Hyperspace"),
        PHOTO_CAMERA_TOGGLE("photo_camera_toggle", "PhotoCameraToggle"),
        MOVE_PLACEMENT_CAM_FORWARD("move_placement_cam_forward", "MovePlacementCamForward"),
        CAM_TRANSLATE_UP("cam_translate_up", "CamTranslateUp"),
        MOVE_FREE_CAM_FORWARD("move_free_cam_forward", "MoveFreeCamForward"),
        PLACEMENT_CAM_SPEED_INC("placement_cam_speed_inc", "PlacementCamSpeedInc"),
        FOCUS_COMMS_PANEL("focus_comms_panel", "FocusCommsPanel"),
        SET_SPEED100("set_speed100", "SetSpeed100"),
        CAM_TRANSLATE_BACKWARD("cam_translate_backward", "CamTranslateBackward"),
        PHOTO_CAMERA__BUGGY("photo_camera__buggy", "PhotoCameraToggle_Buggy"),
        CYCLE_NEXT_SUBSYSTEM("cycle_next_subsystem", "CycleNextSubsystem"),
        OPEN_CODEX_GO_TO_DISCOVERY__BUGGY("open_codex_go_to_discovery__buggy", "OpenCodexGoToDiscovery_Buggy"),
        DEPLOY_HARDPOINT_TOGGLE("deploy_hardpoint_toggle", "DeployHardpointToggle"),
        QUIT_CAMERA("quit_camera", "QuitCamera"),
        CYCLE_NEXT_PAGE("cycle_next_page", "CycleNextPage"),
        RESET_POWER_DISTRIBUTION__BUGGY("reset_power_distribution__buggy", "ResetPowerDistribution_Buggy"),
        FOCUS_COMMS_PANEL__BUGGY("focus_comms_panel__buggy", "FocusCommsPanel_Buggy"),
        FIX_CAMERA_RELATIVE_TOGGLE("fix_camera_relative_toggle", "FixCameraRelativeToggle"),
        WING_NAV_LOCK("wing_nav_lock", "WingNavLock"),
        MOVE_PLACEMENT_CAM_UP("move_placement_cam_up", "MovePlacementCamUp"),
        MOVE_PLACEMENT_CAM_RIGHT("move_placement_cam_right", "MovePlacementCamRight"),
        FOCUS_LEFT_PANEL("focus_left_panel", "FocusLeftPanel"),
        FOCUS_COMMS_PANEL__HUMANOID("focus_comms_panel__humanoid", "FocusCommsPanel_Humanoid"),
        EXIT_SETTLEMENT_PLACEMENT_CAMERA("exit_settlement_placement_camera", "ExitSettlementPlacementCamera"),
        PLACEMENT_CAM_SPEED_DEC("placement_cam_speed_dec", "PlacementCamSpeedDec"),
        USE_SHIELD_CELL("use_shield_cell", "UseShieldCell"),
        SET_SPEED25("set_speed25", "SetSpeed25"),
        FREE_CAM_HUD("free_cam_hud", "FreeCamToggleHUD"),
        CARGO_SCOOP__BUGGY("cargo_scoop__buggy", "ToggleCargoScoop_Buggy"),
        CHANGE_CONSTRUCTION_OPTION("change_construction_option", "ChangeConstructionOption"),
        TARGET_WINGMAN2("target_wingman2", "TargetWingman2"),
        TARGET_WINGMAN1("target_wingman1", "TargetWingman1"),
        CAM_ZOOM_OUT("cam_zoom_out", "CamZoomOut"),
        VANITY_CAMERA_SEVEN("vanity_camera_seven", "VanityCameraSeven"),
        FORWARD_KEY("forward_key", "ForwardKey"),
        TARGET_WINGMAN0("target_wingman0", "TargetWingman0"),
        UI__TOGGLE("ui__toggle", "UI_Toggle"),
        ENGAGE_SUPERCRUISE("engage_supercruise", "Supercruise"),
        GALAXY_MAP_HOME("galaxy_map_home", "GalaxyMapHome"),
        GALNET_AUDIO__SKIP_FORWARD("galnet_audio__skip_forward", "GalnetAudio_SkipForward"),
        FOCUS_RADAR_PANEL__BUGGY("focus_radar_panel__buggy", "FocusRadarPanel_Buggy"),
        REQUEST_HOLD_FIRE("request_hold_fire", "OrderHoldFire"),
        CAM_TRANSLATE_FORWARD("cam_translate_forward", "CamTranslateForward"),
        SYSTEM_MAP__HUMANOID("system_map__humanoid", "SystemMapOpen_Humanoid"),
        UI__LEFT("ui__left", "UI_Left"),
        EXPLORATION_FSSQUIT("exploration_fssquit", "ExplorationFSSQuit"),
        LANDING_GEAR_TOGGLE("landing_gear_toggle", "LandingGearToggle"),
        CAM_TRANSLATE_RIGHT("cam_translate_right", "CamTranslateRight"),
        EJECT_ALL_CARGO("eject_all_cargo", "EjectAllCargo"),
        MOVE_PLACEMENT_CAM_LEFT("move_placement_cam_left", "MovePlacementCamLeft"),
        UI__UP("ui__up", "UI_Up"),
        EXPLORATION_FSSDISCOVERY_SCAN("exploration_fssdiscovery_scan", "ExplorationFSSDiscoveryScan"),
        QUICK_COMMS_PANEL__HUMANOID("quick_comms_panel__humanoid", "QuickCommsPanel_Humanoid"),
        INCREASE_WEAPONS_POWER__BUGGY("increase_weapons_power__buggy", "IncreaseWeaponsPower_Buggy"),
        AUTO_BREAK_BUGGY_BUTTON("auto_break_buggy_button", "AutoBreakBuggyButton"),
        SET_SPEED75("set_speed75", "SetSpeed75"),
        PLAYER_HUDMODE_TOGGLE("player_hudmode_toggle", "PlayerHUDModeToggle"),
        MOVE_FREE_CAM_RIGHT("move_free_cam_right", "MoveFreeCamRight"),
        CAM_YAW_RIGHT("cam_yaw_right", "CamYawRight"),
        HYPER_SUPER_COMBINATION("hyper_super_combination", "HyperSuperCombination"),
        CAM_TRANSLATE_LEFT("cam_translate_left", "CamTranslateLeft"),
        UI__RIGHT("ui__right", "UI_Right"),
        STORE_TOGGLE("store_toggle", "StoreToggle"),
        GALNET_AUDIO__PLAY__PAUSE("galnet_audio__play__pause", "GalnetAudio_Play_Pause"),
        REQUEST_DEFENSIVE_BEHAVIOUR("request_defensive_behaviour", "OrderDefensiveBehaviour"),
        VANITY_CAMERA_FOUR("vanity_camera_four", "VanityCameraFour"),
        UIFOCUS("uifocus", "UIFocus"),
        CYCLE_PREVIOUS_PAGE("cycle_previous_page", "CyclePreviousPage"),
        DOWN_THRUST_BUTTON("down_thrust_button", "DownThrustButton"),
        GALNET_AUDIO__CLEAR_QUEUE("galnet_audio__clear_queue", "GalnetAudio_ClearQueue"),
        VANITY_CAMERA_SIX("vanity_camera_six", "VanityCameraSix"),
        TARGET_NEXT_ROUTE_SYSTEM("target_next_route_system", "TargetNextRouteSystem"),
        CAM_ZOOM_IN("cam_zoom_in", "CamZoomIn"),
        SET_SPEED_ZERO("set_speed_zero", "SetSpeedZero"),
        INCREASE_WEAPONS_POWER("increase_weapons_power", "IncreaseWeaponsPower"),
        VANITY_CAMERA_SCROLL_LEFT("vanity_camera_scroll_left", "VanityCameraScrollLeft"),
        SYSTEM_MAP__BUGGY("system_map__buggy", "SystemMapOpen_Buggy"),
        FREE_CAM_SPEED_DEC("free_cam_speed_dec", "FreeCamSpeedDec"),
        UI__DOWN("ui__down", "UI_Down"),
        CYCLE_PREVIOUS_SUBSYSTEM("cycle_previous_subsystem", "CyclePreviousSubsystem"),
        SET_SPEED50("set_speed50", "SetSpeed50"),
        FIX_CAMERA_WORLD_TOGGLE("fix_camera_world_toggle", "FixCameraWorldToggle"),
        VANITY_CAMERA_THREE("vanity_camera_three", "VanityCameraThree"),
        UP_THRUST_BUTTON("up_thrust_button", "UpThrustButton"),
        UI__SELECT("ui__select", "UI_Select"),
        INCREASE_SYSTEMS_POWER__BUGGY("increase_systems_power__buggy", "IncreaseSystemsPower_Buggy"),
        CYCLE_NEXT_PANEL("cycle_next_panel", "CycleNextPanel"),
        VANITY_CAMERA_SCROLL_RIGHT("vanity_camera_scroll_right", "VanityCameraScrollRight"),
        ROLL_LEFT_BUTTON("roll_left_button", "RollLeftButton"),
        DEPLOY_HEAT_SINK("deploy_heat_sink", "DeployHeatSink"),
        FOCUS_RADAR_PANEL("focus_radar_panel", "FocusRadarPanel"),
        CYCLE_PREVIOUS_PANEL("cycle_previous_panel", "CyclePreviousPanel"),
        EJECT_ALL_CARGO__BUGGY("eject_all_cargo__buggy", "EjectAllCargo_Buggy"),
        MOVE_FREE_CAM_DOWN("move_free_cam_down", "MoveFreeCamDown"),
        FREE_CAM_SPEED_INC("free_cam_speed_inc", "FreeCamSpeedInc"),
        EXPLORATION_SAAPREVIOUS_GENUS("exploration_saaprevious_genus", "ExplorationSAAPreviousGenus"),
        GALAXY_MAP("galaxy_map", "GalaxyMapOpen");

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
