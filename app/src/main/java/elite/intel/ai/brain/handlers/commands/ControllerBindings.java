package elite.intel.ai.brain.handlers.commands;

/**
 * Represents the available actions and their associated game commands for
 * the in-game control system. The class helps in mapping specific actions
 * to their respective commands, which are linked with identifiers and
 * associated classes for controlling the game behavior.
 */
public class ControllerBindings {
    /**
     * Enumeration representing various game commands and their associated attributes.
     * Each game command is linked with a binding name, an action, and a controller class.
     * This enum is used to map specific in-game actions to identifiers for processing and handling user inputs.
     */
    public enum GameCommand {

        /// ------------------------------------------------------------------------------------------------------------
        /// mappings for custom commands
        BINDING_TOGGLE_CARGO_SCOOP("ToggleCargoScoop"),
        BINDING_TOGGLE_CARGO_SCOOP_BUGGY("ToggleCargoScoop_Buggy"),
        BINDING_HARDPOINTS_TOGGLE("DeployHardpointToggle"),
        BINDING_FOCUS_COMMS_PANEL("FocusCommsPanel"),
        BINDING_FOCUS_COMMS_PANEL_BUGGY("FocusCommsPanel_Buggy"),
        BINDING_FOCUS_CONTACTS_PANEL("FocusLeftPanel"),
        BINDING_FOCUS_CONTACTS_PANEL_BUGGY("FocusLeftPanel_Buggy"),
        BINDING_FOCUS_LEFT_PANEL("FocusLeftPanel"),
        BINDING_FOCUS_LEFT_PANEL_BUGGY("FocusLeftPanel_Buggy"),
        BINDING_FOCUS_INTERNAL_PANEL("FocusRightPanel"),
        BINDING_FOCUS_INTERNAL_PANEL_BUGGY("FocusRightPanel_Buggy"),
        BINDING_FOCUS_STATUS_PANEL("FocusRightPanel"),
        BINDING_FOCUS_STATUS_PANEL_BUGGY("FocusRightPanel_Buggy"),
        BINDING_FOCUS_RADAR_PANEL("FocusRadarPanel"),
        BINDING_FOCUS_RADAR_PANEL_BUGGY("FocusRadarPanel_Buggy"),
        BINDING_FOCUS_LOADOUT_PANEL("FocusRadarPanel"),
        BINDING_FOCUS_LOADOUT_PANEL_BUGGY("FocusRadarPanel_Buggy"),
        BINDING_EJECT_ALL_CARGO("EjectAllCargo"),
        BINDING_EJECT_ALL_CARGO_BUGGY("EjectAllCargo_Buggy"),
        BINDING_LANDING_GEAR_TOGGLE("LandingGearToggle"),
        BINDING_RESET_POWER_DISTRIBUTION("ResetPowerDistribution"),
        BINDING_RESET_POWER_DISTRIBUTION_BUGGY("ResetPowerDistribution_Buggy"),

        BINDING_INCREASE_ENGINES_POWER("IncreaseEnginesPower"),
        BINDING_INCREASE_SYSTEMS_POWER("IncreaseSystemsPower"),
        BINDING_INCREASE_SHIELDS_POWER("IncreaseSystemsPower"),
        BINDING_INCREASE_WEAPONS_POWER("IncreaseWeaponsPower"),

        BINDING_INCREASE_ENGINES_POWER_BUGGY("IncreaseEnginesPower_Buggy"),
        BINDING_INCREASE_SYSTEMS_POWER_BUGGY("IncreaseSystemsPower_Buggy"),
        BINDING_INCREASE_SHIELDS_POWER_BUGGY("IncreaseSystemsPower_Buggy"),
        BINDING_INCREASE_WEAPONS_POWER_BUGGY("IncreaseWeaponsPower_Buggy"),

        BINDING_GALAXY_MAP("GalaxyMapOpen"),
        BINDING_GALAXY_MAP_BUGGY("GalaxyMapOpen_Buggy"),

        BINDING_LOCAL_MAP("SystemMapOpen"),
        BINDING_LOCAL_MAP_BUGGY("SystemMapOpen_Buggy"),
        BINDING_EXPLORATION_FSSDISCOVERY_SCAN("ExplorationFSSEnter"),

        BINDING_CAM_ZOOM_IN("CamZoomIn"),
        BINDING_CAM_ZOOM_OUT("CamZoomOut"),
        BINDING_EXIT_KEY("UI_Back"),
        BINDING_UI_DOWN("UI_Down"),
        BINDING_UI_LEFT("UI_Left"),
        BINDING_UI_RIGHT("UI_Right"),
        BINDING_UI_FOCUS("UIFocus"),
        BINDING_UI_SELECT("UI_Select"),
        BINDING_UI_TOGGLE("UI_Toggle"),
        BINDING_UI_UP("UI_Up"),

        BINDING_EXIT_SUPERCRUISE("Supercruise"),
        BINDING_JUMP_TO_HYPERSPACE("Hyperspace"),
        BINDING_ENTER_SUPERCRUISE("Supercruise"),


        //TODO: convert to custom
        BINDING_ACTIVATE_ANALYSIS_MODE("PlayerHUDModeToggle"),
        BINDING_ACTIVATE_ANALYSIS_MODE_BUGGY("PlayerHUDModeToggle_Buggy"),
        BINDING_ACTIVATE_COMBAT_MODE("PlayerHUDModeToggle"),
        BINDING_ACTIVATE_COMBAT_MODE_BUGGY("PlayerHUDModeToggle_Buggy"),


        /// ------------------------------------------------------------------------------------------------------------
        /// basic commands
        BINDING_ACTIVATE("UI_Select"),
        BINDING_NIGHT_VISION_TOGGLE("NightVisionToggle"),
        BINDING_CYCLE_NEXT_PAGE("CycleNextPage"),
        BINDING_CYCLE_NEXT_PANEL("CycleNextPanel"),
        BINDING_CYCLE_PREVIOUS_PAGE("CyclePreviousPage"),
        BINDING_CYCLE_PREVIOUS_PANEL("CyclePreviousPanel"),

        BINDING_CYCLE_NEXT_SUBSYSTEM("CycleNextSubsystem"),
        BINDING_CYCLE_PREVIOUS_SUBSYSTEM("CyclePreviousSubsystem"),

        BINDING_DEPLOY_HEAT_SINK("DeployHeatSink"),
        BINDING_DRIVE_ASSIST("ToggleDriveAssist"),
        BINDING_EXPLORATION_FSSQUIT("ExplorationFSSQuit"),

        //EXIT_SETTLEMENT_PLACEMENT_CAMERA("exit_settlement_placement_camera", "ExitSettlementPlacementCamera"),

        //GALNET_AUDIO_CLEAR_QUEUE("galnet_audio_clear_queue", "GalnetAudio_ClearQueue"),
        //GALNET_AUDIO_PLAY_PAUSE("galnet_audio_play_pause", "GalnetAudio_Play_Pause"),
        //GALNET_AUDIO_SKIP_BACKWARD("galnet_audio_skip_backward", "GalnetAudio_SkipBackward"),
        //GALNET_AUDIO_SKIP_FORWARD("galnet_audio_skip_forward", "GalnetAudio_SkipForward"),

        BINDING_HEAD_LOOK_RESET("HeadLookReset"),

        BINDING_RADAR_DECREASE_RANGE("RadarDecreaseRange"),
        BINDING_RADAR_INCREASE_RANGE("RadarIncreaseRange"),

        BINDING_RECALL_DISMISS_SHIP("RecallDismissShip"),

        BINDING_REQUEST_DEFENSIVE_BEHAVIOUR("OrderDefensiveBehaviour"),
        BINDING_REQUEST_FOCUS_TARGET("OrderFocusTarget"),
        BINDING_REQUEST_HOLD_FIRE("OrderHoldFire"),
        BINDING_REQUEST_REQUEST_DOCK("OrderRequestDock"),

        BINDING_SELECT_TARGETS_TARGET("SelectTargetsTarget"),

        BINDING_PLANETARY_APPROACH_SPEED75("SetSpeed25"),
        BINDING_SET_SPEED25("SetSpeed25"),
        BINDING_SET_SPEED50("SetSpeed50"),
        BINDING_SET_SPEED75("SetSpeed75"),
        BINDING_SET_OPTIMAL_SPEED("SetSpeed75"),
        BINDING_SET_SPEED100("SetSpeed100"),
        BINDING_SET_SPEED_ZERO("SetSpeedZero"),
        BINDING_INCREASE_SPEED("ForwardKey"),
        BINDING_DECREASE_SPEED("BackwardKey"),

        BINDING_SHIP_LIGHTS_TOGGLE("ShipSpotLightToggle"),
        BINDING_BUGGY_LIGHTS_TOGGLE("HeadlightsBuggyButton"),


        BINDING_SELECT_HIGHEST_THREAT("SelectHighestThreat"),

        BINDING_TARGET_NEXT_ROUTE_SYSTEM("TargetNextRouteSystem"),

        BINDING_TARGET_WINGMAN0("TargetWingman0"),
        BINDING_TARGET_WINGMAN1("TargetWingman1"),
        BINDING_TARGET_WINGMAN2("TargetWingman2"),
        BINDING_WING_NAV_LOCK("WingNavLock");
        //UP_THRUST_BUTTON("up_thrust_button", "UpThrustButton",GenericGameController.class),
        //DOWN_THRUST_BUTTON("down_thrust_button", "DownThrustButton",GenericGameController.class);


        ///
        private final String gameBinding;

        GameCommand(String gameBinding) {
            this.gameBinding = gameBinding;
        }


        public String getGameBinding() {
            return gameBinding;
        }
    }
}