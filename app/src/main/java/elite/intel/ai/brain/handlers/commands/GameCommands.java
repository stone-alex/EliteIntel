package elite.intel.ai.brain.handlers.commands;

import elite.intel.ai.brain.handlers.commands.custom.OpenGalaxyMapHandler;
import elite.intel.ai.brain.handlers.commands.custom.PerformFSSScanHandler;

/**
 * Represents the available actions and their associated game commands for
 * the in-game control system. The class helps in mapping specific actions
 * to their respective commands, which are linked with identifiers and
 * associated classes for controlling the game behavior.
 */
public class GameCommands {
    /**
     * Enumeration representing various game commands and their associated attributes.
     * Each game command is linked with a binding name, an action, and a controller class.
     * This enum is used to map specific in-game actions to identifiers for processing and handling user inputs.
     */
    public enum GameCommand {

        /// ------------------------------------------------------------------------------------------------------------
        /// mappings for custom commands
        TOGGLE_CARGO_SCOOP("", "ToggleCargoScoop", GenericGameController.class),
        TOGGLE_CARGO_SCOOP_BUGGY("", "ToggleCargoScoop_Buggy", GenericGameController.class),
        HARDPOINTS_TOGGLE("", "DeployHardpointToggle", GenericGameController.class),
        FOCUS_COMMS_PANEL("", "FocusCommsPanel", GenericGameController.class),
        FOCUS_COMMS_PANEL_BUGGY("", "FocusCommsPanel_Buggy", GenericGameController.class),
        FOCUS_CONTACTS_PANEL("", "FocusLeftPanel", GenericGameController.class),
        FOCUS_CONTACTS_PANEL_BUGGY("", "FocusLeftPanel_Buggy", GenericGameController.class),
        FOCUS_LEFT_PANEL("", "FocusLeftPanel", GenericGameController.class),
        FOCUS_LEFT_PANEL_BUGGY("", "FocusLeftPanel_Buggy", GenericGameController.class),
        FOCUS_INTERNAL_PANEL("", "FocusRightPanel", GenericGameController.class),
        FOCUS_INTERNAL_PANEL_BUGGY("", "FocusRightPanel_Buggy", GenericGameController.class),
        FOCUS_STATUS_PANEL("", "FocusRightPanel", GenericGameController.class),
        FOCUS_STATUS_PANEL_BUGGY("", "FocusRightPanel_Buggy", GenericGameController.class),
        FOCUS_RADAR_PANEL("", "FocusRadarPanel", GenericGameController.class),
        FOCUS_RADAR_PANEL_BUGGY("", "FocusRadarPanel_Buggy", GenericGameController.class),
        FOCUS_LOADOUT_PANEL("", "FocusRadarPanel", GenericGameController.class),
        FOCUS_LOADOUT_PANEL_BUGGY("", "FocusRadarPanel_Buggy", GenericGameController.class),
        EJECT_ALL_CARGO("", "EjectAllCargo", GenericGameController.class),
        EJECT_ALL_CARGO_BUGGY("", "EjectAllCargo_Buggy", GenericGameController.class),
        LANDING_GEAR_TOGGLE("", "LandingGearToggle", GenericGameController.class),
        RESET_POWER_DISTRIBUTION("", "ResetPowerDistribution", GenericGameController.class),
        RESET_POWER_DISTRIBUTION_BUGGY("", "ResetPowerDistribution_Buggy", GenericGameController.class),

        INCREASE_ENGINES_POWER("", "IncreaseEnginesPower", GenericGameController.class),
        INCREASE_SYSTEMS_POWER("", "IncreaseSystemsPower", GenericGameController.class),
        INCREASE_SHIELDS_POWER("", "IncreaseSystemsPower", GenericGameController.class),
        INCREASE_WEAPONS_POWER("", "IncreaseWeaponsPower", GenericGameController.class),

        INCREASE_ENGINES_POWER_BUGGY("", "IncreaseEnginesPower_Buggy", GenericGameController.class),
        INCREASE_SYSTEMS_POWER_BUGGY("", "IncreaseSystemsPower_Buggy", GenericGameController.class),
        INCREASE_SHIELDS_POWER_BUGGY("", "IncreaseSystemsPower_Buggy", GenericGameController.class),
        INCREASE_WEAPONS_POWER_BUGGY("", "IncreaseWeaponsPower_Buggy", GenericGameController.class),

        GALAXY_MAP("", "GalaxyMapOpen", OpenGalaxyMapHandler.class),
        GALAXY_MAP_BUGGY("", "GalaxyMapOpen_Buggy", OpenGalaxyMapHandler.class),

        LOCAL_MAP("", "SystemMapOpen", GenericGameController.class),
        LOCAL_MAP_BUGGY("", "SystemMapOpen_Buggy", GenericGameController.class),
        EXPLORATION_FSSDISCOVERY_SCAN("", "ExplorationFSSEnter", PerformFSSScanHandler.class),

        CAM_ZOOM_IN("", "CamZoomIn", GenericGameController.class),
        CAM_ZOOM_OUT("", "CamZoomOut",GenericGameController.class),
        EXIT_KEY("", "UI_Back", GenericGameController.class),
        UI_DOWN("", "UI_Down", GenericGameController.class),
        UI_LEFT("", "UI_Left", GenericGameController.class),
        UI_RIGHT("", "UI_Right", GenericGameController.class),
        UI_FOCUS("", "UIFocus", GenericGameController.class),
        UI_SELECT("", "UI_Select", GenericGameController.class),
        UI_TOGGLE("", "UI_Toggle", GenericGameController.class),
        UI_UP("", "UI_Up", GenericGameController.class),

        EXIT_SUPERCRUISE("", "Supercruise", GenericGameController.class),
        JUMP_TO_HYPERSPACE("", "Hyperspace", GenericGameController.class),
        ENTER_SUPERCRUISE("", "Supercruise", GenericGameController.class),


        //TODO: convert to custom
        ACTIVATE_ANALYSIS_MODE("", "PlayerHUDModeToggle", GenericGameController.class),
        ACTIVATE_ANALYSIS_MODE_BUGGY("", "PlayerHUDModeToggle_Buggy", GenericGameController.class),
        ACTIVATE_COMBAT_MODE("", "PlayerHUDModeToggle", GenericGameController.class),
        ACTIVATE_COMBAT_MODE_BUGGY("", "PlayerHUDModeToggle_Buggy", GenericGameController.class),


        /// ------------------------------------------------------------------------------------------------------------
        /// basic commands
        ACTIVATE("activate", "UI_Select", GenericGameController.class),
        NIGHT_VISION("activate_night_vision", "NightVisionToggle", GenericGameController.class),
        CYCLE_NEXT_PAGE("cycle_next_page", "CycleNextPage", GenericGameController.class),
        CYCLE_NEXT_PANEL("cycle_next_panel", "CycleNextPanel", GenericGameController.class),
        CYCLE_PREVIOUS_PAGE("cycle_previous_page", "CyclePreviousPage", GenericGameController.class),
        CYCLE_PREVIOUS_PANEL("cycle_previous_panel", "CyclePreviousPanel", GenericGameController.class),

        CYCLE_NEXT_SUBSYSTEM("cycle_next_subsystem", "CycleNextSubsystem", GenericGameController.class),
        CYCLE_PREVIOUS_SUBSYSTEM("cycle_previous_subsystem", "CyclePreviousSubsystem", GenericGameController.class),

        DEPLOY_HEAT_SINK("deploy_heat_sink", "DeployHeatSink", GenericGameController.class),
        DRIVE_ASSIST("drive_assist", "ToggleDriveAssist", GenericGameController.class),
        EXPLORATION_FSSQUIT("exit_fss", "ExplorationFSSQuit", GenericGameController.class),

        //EXIT_SETTLEMENT_PLACEMENT_CAMERA("exit_settlement_placement_camera", "ExitSettlementPlacementCamera", GenericGameController.class),

        //GALNET_AUDIO_CLEAR_QUEUE("galnet_audio_clear_queue", "GalnetAudio_ClearQueue", GenericGameController.class),
        //GALNET_AUDIO_PLAY_PAUSE("galnet_audio_play_pause", "GalnetAudio_Play_Pause", GenericGameController.class),
        //GALNET_AUDIO_SKIP_BACKWARD("galnet_audio_skip_backward", "GalnetAudio_SkipBackward", GenericGameController.class),
        //GALNET_AUDIO_SKIP_FORWARD("galnet_audio_skip_forward", "GalnetAudio_SkipForward", GenericGameController.class),

        //HEAD_LOOK_RESET("head_look_reset", "HeadLookReset", GenericGameController.class),

        RADAR_DECREASE_RANGE("radar_decrease_range", "RadarDecreaseRange", GenericGameController.class),
        RADAR_INCREASE_RANGE("radar_increase_range", "RadarIncreaseRange", GenericGameController.class),

        RECALL_DISMISS_SHIP("recall_or_dismiss_ship", "RecallDismissShip", GenericGameController.class),

        REQUEST_DEFENSIVE_BEHAVIOUR("defend_ship_fighter_order", "OrderDefensiveBehaviour", GenericGameController.class),
        REQUEST_FOCUS_TARGET("attack_my_target_fighter_order", "OrderFocusTarget", GenericGameController.class),
        REQUEST_HOLD_FIRE("hold_your_fire_fighter_order", "OrderHoldFire", GenericGameController.class),
        REQUEST_REQUEST_DOCK("fighter_recall", "OrderRequestDock", GenericGameController.class),

        SELECT_TARGETS_TARGET("select_wingman_target", "SelectTargetsTarget", GenericGameController.class),

        PLANETARY_APPROACH_SPEED75("planetary_approach", "SetSpeed25", GenericGameController.class),
        SET_SPEED25("set_speed_to_slow_throttle_25", "SetSpeed25", GenericGameController.class),
        SET_SPEED50("set_speed_to_medium_throttle_50", "SetSpeed50", GenericGameController.class),
        SET_SPEED75("set_speed_to_optimal_throttle_75", "SetSpeed75", GenericGameController.class),
        SET_OPTIMAL_SPEED("set_optimal_speed", "SetSpeed75", GenericGameController.class),
        SET_SPEED100("set_speed_to_maximum_throttle_100", "SetSpeed100", GenericGameController.class),
        SET_SPEED_ZERO("set_speed_to_zero_0", "SetSpeedZero", GenericGameController.class),

        AUTO_DOC1("engage_auto_cocking", "SetSpeedZero", GenericGameController.class),
        AUTO_DOC2("take_us_in", "SetSpeedZero", GenericGameController.class),
        AUTO_DOC3("engage_automatic_docking", "SetSpeedZero", GenericGameController.class),
        AUTO_DOC4("taxi", "SetSpeedZero", GenericGameController.class),

        SELECT_HIGHEST_THREAT("target_highest_threat", "SelectHighestThreat", GenericGameController.class),

        TARGET_NEXT_ROUTE_SYSTEM("select_or_target_next_system_in_route", "TargetNextRouteSystem", GenericGameController.class),

        TARGET_WINGMAN0("target_wingman0", "TargetWingman0", GenericGameController.class),
        TARGET_WINGMAN1("target_wingman1", "TargetWingman1", GenericGameController.class),
        TARGET_WINGMAN2("target_wingman2", "TargetWingman2", GenericGameController.class),
        WING_NAV_LOCK("lock_on_wingman", "WingNavLock", GenericGameController.class);

        //UP_THRUST_BUTTON("up_thrust_button", "UpThrustButton",GenericGameController.class),
        //DOWN_THRUST_BUTTON("down_thrust_button", "DownThrustButton",GenericGameController.class);


        ///
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

    public static String[] getGameControlCommands() {
        String[] commands = new String[GameCommand.values().length];
        for (int i = 0; i < GameCommand.values().length; i++) {
            // These commands used in custom handlers. exclude from generic command handler
            if (GameCommand.values()[i] == GameCommand.INCREASE_ENGINES_POWER) continue;
            if (GameCommand.values()[i] == GameCommand.INCREASE_SHIELDS_POWER) continue;
            if (GameCommand.values()[i] == GameCommand.INCREASE_SYSTEMS_POWER) continue;
            if (GameCommand.values()[i] == GameCommand.INCREASE_WEAPONS_POWER) continue;
            if (GameCommand.values()[i] == GameCommand.RESET_POWER_DISTRIBUTION) continue;

            if (GameCommand.values()[i] == GameCommand.INCREASE_ENGINES_POWER_BUGGY) continue;
            if (GameCommand.values()[i] == GameCommand.INCREASE_SHIELDS_POWER_BUGGY) continue;
            if (GameCommand.values()[i] == GameCommand.INCREASE_SYSTEMS_POWER_BUGGY) continue;
            if (GameCommand.values()[i] == GameCommand.INCREASE_WEAPONS_POWER_BUGGY) continue;
            if (GameCommand.values()[i] == GameCommand.RESET_POWER_DISTRIBUTION_BUGGY) continue;

            if (GameCommand.values()[i] == GameCommand.FOCUS_COMMS_PANEL) continue;
            if (GameCommand.values()[i] == GameCommand.FOCUS_COMMS_PANEL_BUGGY) continue;

            if (GameCommand.values()[i] == GameCommand.FOCUS_CONTACTS_PANEL) continue;
            if (GameCommand.values()[i] == GameCommand.FOCUS_CONTACTS_PANEL_BUGGY) continue;

            if (GameCommand.values()[i] == GameCommand.FOCUS_LEFT_PANEL) continue;
            if (GameCommand.values()[i] == GameCommand.FOCUS_LEFT_PANEL_BUGGY) continue;

            if (GameCommand.values()[i] == GameCommand.FOCUS_INTERNAL_PANEL) continue;
            if (GameCommand.values()[i] == GameCommand.FOCUS_INTERNAL_PANEL_BUGGY) continue;

            if (GameCommand.values()[i] == GameCommand.FOCUS_STATUS_PANEL) continue;
            if (GameCommand.values()[i] == GameCommand.FOCUS_STATUS_PANEL_BUGGY) continue;

            if (GameCommand.values()[i] == GameCommand.FOCUS_RADAR_PANEL) continue;
            if (GameCommand.values()[i] == GameCommand.FOCUS_RADAR_PANEL_BUGGY) continue;

            if (GameCommand.values()[i] == GameCommand.FOCUS_LOADOUT_PANEL) continue;
            if (GameCommand.values()[i] == GameCommand.FOCUS_LOADOUT_PANEL_BUGGY) continue;

            if (GameCommand.values()[i] == GameCommand.EJECT_ALL_CARGO) continue;
            if (GameCommand.values()[i] == GameCommand.EJECT_ALL_CARGO_BUGGY) continue;


            if (GameCommand.values()[i] == GameCommand.TOGGLE_CARGO_SCOOP) continue;
            if (GameCommand.values()[i] == GameCommand.TOGGLE_CARGO_SCOOP_BUGGY) continue;

            if (GameCommand.values()[i] == GameCommand.HARDPOINTS_TOGGLE) continue;
            if (GameCommand.values()[i] == GameCommand.LANDING_GEAR_TOGGLE) continue;


            if (GameCommand.values()[i] == GameCommand.EXPLORATION_FSSDISCOVERY_SCAN) continue;
            if (GameCommand.values()[i] == GameCommand.UI_DOWN) continue;
            if (GameCommand.values()[i] == GameCommand.UI_LEFT) continue;
            if (GameCommand.values()[i] == GameCommand.UI_RIGHT) continue;
            if (GameCommand.values()[i] == GameCommand.UI_UP) continue;
            if (GameCommand.values()[i] == GameCommand.UI_TOGGLE) continue;
            if (GameCommand.values()[i] == GameCommand.UI_FOCUS) continue;
            if (GameCommand.values()[i] == GameCommand.UI_SELECT) continue;

            if (GameCommand.values()[i] == GameCommand.GALAXY_MAP) continue;
            if (GameCommand.values()[i] == GameCommand.GALAXY_MAP_BUGGY) continue;
            if (GameCommand.values()[i] == GameCommand.LOCAL_MAP) continue;
            if (GameCommand.values()[i] == GameCommand.LOCAL_MAP_BUGGY) continue;

            if (GameCommand.values()[i] == GameCommand.EXIT_KEY) continue;
            if (GameCommand.values()[i] == GameCommand.CAM_ZOOM_IN) continue;
            if (GameCommand.values()[i] == GameCommand.CAM_ZOOM_OUT) continue;
            //if (GameCommand.values()[i] == GameCommand.UP_THRUST_BUTTON) continue;
            //if (GameCommand.values()[i] == GameCommand.DOWN_THRUST_BUTTON) continue;

            if (GameCommand.values()[i] == GameCommand.ENTER_SUPERCRUISE) continue;
            if (GameCommand.values()[i] == GameCommand.EXIT_SUPERCRUISE) continue;
            if (GameCommand.values()[i] == GameCommand.JUMP_TO_HYPERSPACE) continue;

            if (GameCommand.values()[i] == GameCommand.ACTIVATE_ANALYSIS_MODE) continue;
            if (GameCommand.values()[i] == GameCommand.ACTIVATE_ANALYSIS_MODE_BUGGY) continue;

            if (GameCommand.values()[i] == GameCommand.ACTIVATE_COMBAT_MODE) continue;
            if (GameCommand.values()[i] == GameCommand.ACTIVATE_COMBAT_MODE_BUGGY) continue;


            commands[i] = GameCommand.values()[i].getUserCommand();
        }
        return commands;
    }
}