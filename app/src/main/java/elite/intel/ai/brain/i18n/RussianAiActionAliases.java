package elite.intel.ai.brain.i18n;

import elite.intel.session.Status;

import java.util.Map;

import static elite.intel.ai.brain.actions.Commands.*;
import static elite.intel.ai.brain.actions.Queries.*;

public class RussianAiActionAliases implements AiActionAliasProvider {

    @Override
    public void addAliases(Map<String, String> map, Status status, boolean isDryRun) {
        // always available
        map.put("проснись, слушай, слушай меня, активируйся", WAKEUP.getAction());
        map.put("спи, усни, иди спать, игнорируй меня, не слушай, не отслеживай", SLEEP.getAction());
        map.put("перебей, прерви, останови речь, прекрати говорить", INTERRUPT_TTS.getAction());

        // navigation
        map.put("лети к координатам {lat:X, lon:Y}, навигация к координатам {lat:X, lon:Y}, курс на координаты {lat:X, lon:Y}", NAVIGATE_TO_TARGET.getAction());
        map.put("навигация к активной миссии, проложи маршрут к активной миссии, проложи маршрут к миссии, веди к миссии, лети к миссии {key:X}", NAVIGATE_TO_NEXT_MISSION.getAction());
        map.put("лети к авианосцу, навигация к авианосцу, курс к авианосцу, вернись к авианосцу, веди нас к авианосцу", NAVIGATE_TO_CARRIER.getAction());
        map.put("навигация к зоне посадки, курс к зоне посадки, азимут к зоне посадки, обратно к зоне посадки", GET_HEADING_TO_LZ.getAction());
        map.put("навигация к следующей торговой остановке, лети к следующей торговой точке", NAVIGATE_TO_NEXT_TRADE_STOP.getAction());
        map.put("навигация из памяти, вставь из памяти, используй адрес из памяти", NAVIGATE_TO_ADDRESS_FROM_MEMORY.getAction());
        map.put("отмени навигацию, прерви навигацию, останови навигацию, сбрось маршрут", NAVIGATION_OFF.getAction());
        map.put("установи домашнюю систему, сделай текущую систему домашней, отметь домашнюю систему", SET_HOME_SYSTEM.getAction());
        map.put("веди домой, лети домой, навигация домой, вернись домой, проложи маршрут домой, курс домой", TAKE_ME_HOME.getAction());

        if (status.isInMainShip() || isDryRun) {
            // navigation
            map.put("выбери fsd пункт назначения, выбери пункт назначения, установи пункт назначения, выбери цель маршрута", TARGET_DESTINATION.getAction());
            map.put("прыжок в гиперпространство, прыгай, гиперпрыжок, войти в гиперпространство, поехали, следующий маршрутный пункт", JUMP_TO_HYPERSPACE.getAction());
            map.put("выйти из суперкруиза, выйти здесь, сбросить суперкруиз, выйти из сверхсвета, сбросься здесь", DROP_FROM_SUPER_CRUISE.getAction());
            map.put("войти в суперкруиз, суперкруиз, включи суперкруиз", ENTER_SUPER_CRUISE.getAction());
            map.put("запусти корабль, старт, отстыковаться, покинуть станцию, выйти из порта", LAUNCH_SHIP.getAction());

            // speed / throttle
            map.put("останови двигатели, стоп, полный стоп, остановись, заглуши двигатели, сбрось тягу, нулевая тяга, останови корабль", SET_SPEED_ZERO.getAction());
            map.put("такси к посадке, такси, автопосадка, автоматическая посадка", TAXI.getAction());
            map.put("четверть тяги, двадцать пять процентов, малая скорость, одна четверть", SET_SPEED25.getAction());
            map.put("половина тяги, пятьдесят процентов, половина скорости", SET_SPEED50.getAction());
            map.put("три четверти тяги, семьдесят пять процентов, три четверти скорости", SET_SPEED75.getAction());
            map.put("полная тяга, сто процентов, полный ход, максимальная скорость, максимум тяги", SET_SPEED100.getAction());
            map.put("увеличь скорость на {key:X}, прибавь скорость на {key:X}", INCREASE_SPEED_BY.getAction());
            map.put("уменьши скорость на {key:X}, сбавь скорость на {key:X}", DECREASE_SPEED_BY.getAction());
            map.put("установи оптимальную скорость, оптимальная скорость подхода, оптимизируй скорость подхода", SET_OPTIMAL_SPEED.getAction());
            map.put("шасси, шасси вниз, выпусти шасси, опусти посадочное шасси", DEPLOY_LANDING_GEAR.getAction());
            map.put("убери шасси, шасси вверх, подними шасси, сложи посадочное шасси", RETRACT_LANDING_GEAR.getAction());
            map.put("запроси стыковку, стыковка со станцией, запроси посадку, запрос посадки, запроси парковку, попроси площадку", REQUEST_DOCKING.getAction());

            // UI panels
            map.put("покажи панель истребителя, открой панель истребителя, отобрази панель истребителя", SHOW_FIGHTER_PANEL.getAction());

            // combat
            map.put("выпусти оружие, оружие к бою, боевой режим, оружие свободно, вооружиться, подготовить оружие", DEPLOY_HARDPOINTS.getAction());
            map.put("убери оружие, оружие убрать, снять оружие, выйти из боя, спрячь оружие, безопасный режим оружия", RETRACT_HARDPOINTS.getAction());
            map.put("цель fsd {key:fsd}, цель двигатели {key:drive}, цель распределитель питания {key:power distributor}, цель силовая установка {key:powerplant}, цель жизнеобеспечение {key:life support}", TARGET_SUB_SYSTEM.getAction());
            map.put("цель ведомый один, ведомый альфа", TARGET_WINGMAN0.getAction());
            map.put("цель ведомый два, ведомый браво", TARGET_WINGMAN1.getAction());
            map.put("цель ведомый три, ведомый чарли", TARGET_WINGMAN2.getAction());
            map.put("навигационная привязка крыла, привязка к ведомому, следовать за ведомым", WING_NAV_LOCK.getAction());
            map.put("приоритетная цель, цель с наибольшей угрозой, самая опасная цель, выбрать врага, следующий враг", SELECT_HIGHEST_THREAT.getAction());

            // vehicle deployment
            map.put("выпусти срв, запусти срв, выпусти транспорт, высади срв", DEPLOY_SRV.getAction());
            map.put("выпусти теплоотвод, запусти теплоотвод, сбрось тепло", DEPLOY_HEAT_SINK.getAction());

            // fighter orders
            map.put("выпусти истребитель, запусти истребитель, отправь истребитель", DEPLOY_FIGHTER.getAction());
            map.put("истребитель защищай корабль, истребитель оборона, приказ истребителю обороняться", FIGHTER_REQUEST_DEFENSIVE_BEHAVIOUR.getAction());
            map.put("истребитель атакуй мою цель, истребитель атаковать, фокус на цель, сосредоточься на цели", FIGHTER_REQUEST_FOCUS_TARGET.getAction());
            map.put("истребитель прекратить огонь, истребитель не стрелять, держать огонь", FIGHTER_REQUEST_HOLD_FIRE.getAction());
            map.put("истребитель вернись на корабль, истребитель стыковка, отозвать истребитель", FIGHTER_REQUEST_REQUEST_DOCK.getAction());
            map.put("истребитель свободный огонь, огонь по готовности, атаковать по усмотрению", FIGHTER_OPEN_ORDERS.getAction());
        }

        if (status.isInMainShip() && !status.isDocked() || isDryRun) {
            map.put("станции в системе, какие станции, ближайшие станции, космопорты, космические станции, доступна ли стыковка", QUERY_STATIONS.getAction());
        }

        if (status.isInSrv() && status.isDocked() || isDryRun) {
            map.put("покажи панель сервисов, открой панель сервисов, отобрази панель сервисов станции", SHOW_STATION_SERVICES.getAction());
        }

        if (status.isInMainShip() || status.isInSrv() || isDryRun) {
            // flight / ship systems
            map.put("переключись в боевой режим, включи боевой режим", ACTIVATE_COMBAT_MODE.getAction());
            map.put("переключись в режим анализа, включи режим анализа", ACTIVATE_ANALYSIS_MODE.getAction());
            map.put("грузовой ковш, открой грузовой ковш, закрой грузовой ковш, выпусти грузовой ковш, убери грузовой ковш, открой грузовой люк, закрой грузовой люк", TOGGLE_CARGO_SCOOP.getAction());
            map.put("ночное видение, включи ночное видение, выключи ночное видение {state:true/false}", NIGHT_VISION_ON_OFF.getAction());
            map.put("фары, свет, выключи свет, включи свет, корабельный свет, свет включить, свет выключить {state:true/false}", LIGHTS_ON_OFF.getAction());

            // UI panels
            map.put("покажи панель командира, открой центральную панель, открой панель ролей, открой планшет", SHOW_COMMANDER_PANEL.getAction());
            map.put("покажи панель экипажа, открой панель экипажа", SHOW_CREW.getAction());
            map.put("покажи домашнюю панель, открой внутреннюю панель", SHOW_INTERNAL_PANEL.getAction());
            map.put("покажи панель модулей, открой модули", SHOW_MODULES_PANEL.getAction());
            map.put("покажи огневые группы, открой огневые группы", SHOW_FIRE_GROUPS.getAction());
            map.put("покажи инвентарь, открой инвентарь", SHOW_INVENTORY_PANEL.getAction());
            map.put("покажи хранилище, открой склад, открой хранилище", SHOW_STORAGE_PANEL.getAction());

            // power
            map.put("энергию на щиты, максимум щитов, усилить щиты", INCREASE_SHIELDS_POWER.getAction());
            map.put("энергию на двигатели, максимум двигателей, усилить двигатели", INCREASE_ENGINES_POWER.getAction());
            map.put("энергию на оружие, максимум оружия, усилить оружие", INCREASE_WEAPONS_POWER.getAction());

            // vehicle deployment
            map.put("выйти из корабля, высадиться, покинуть корабль", DISEMBARK.getAction());
            map.put("сбалансируй энергию, баланс энергии, сбрось распределение энергии, распределить энергию поровну", RESET_POWER.getAction());
        }

        if (status.isInSrv() || isDryRun) {
            map.put("помощь вождения, ассистент вождения, ассистент срв {state:true/false}", DRIVE_ASSIST.getAction());
            map.put("вернуть срв, на борт корабля, вернуться в корабль, забрать срв, стыковка срв", RECOVER_SRV.getAction());
        }

        if (status.isInSrv() || status.isOnFoot() || isDryRun) {
            map.put("отправь корабль, корабль на орбиту, отозвать корабль с поверхности", DISMISS_SHIP.getAction());
            map.put("вернись на поверхность, забери меня, вызови корабль", RETURN_TO_SURFACE.getAction());
        }

        // market / traders / brokers
        map.put("найди торговца сырьевыми материалами, сырьевой торговец, где обменять сырьевые материалы {key:X}", FIND_RAW_MATERIAL_TRADER.getAction());
        map.put("найди торговца закодированными материалами, торговец данными, где обменять данные {key:X}", FIND_ENCODED_MATERIAL_TRADER.getAction());
        map.put("найди торговца произведенными материалами, торговец manufactured материалами {key:X}", FIND_MANUFACTURED_MATERIAL_TRADER.getAction());
        map.put("найди человеческого техноброкера, человеческий технологический брокер {key:X}", FIND_HUMAN_TECHNOLOGY_BROKER.getAction());
        map.put("найди guardian техноброкера, технологический брокер стражей {key:X}", FIND_GUARDIAN_TECHNOLOGY_BROKER.getAction());
        map.put("найди товар, найди ближайший товар, купить товар, где купить, найди рынок {key:X, max_distance:Y, state:true/false}", FIND_COMMODITY.getAction());
        map.put("найди ближайший авианосец, ближайший carrier, ближайший флиткэрриер", FIND_NEAREST_FLEET_CARRIER.getAction());

        // fleet carrier
        map.put("установи резерв топлива авианосца, резерв трития авианосца {key:X}", SET_CARRIER_FUEL_RESERVE.getAction());
        map.put("рассчитай маршрут авианосца, спланируй маршрут авианосца, маршрут прыжков авианосца", CALCULATE_FLEET_CARRIER_ROUTE.getAction());
        map.put("введи пункт назначения авианосца, установи пункт назначения авианосца, направление авианосца", ENTER_FLEET_CARRIER_DESTINATION.getAction());

        // trade
        map.put("рассчитай торговый маршрут, торговый маршрут", CALCULATE_TRADE_ROUTE.getAction());
        map.put("покажи параметры торгового маршрута, список параметров торговли", LIST_TRADE_ROUTE_PARAMETERS.getAction());
        map.put("монетизируй маршрут, оцени прибыль маршрута", MONETIZE_ROUTE.getAction());

        map.put("измени стартовый бюджет торгового профиля {key:X}", CHANGE_TRADE_PROFILE_SET_STARTING_BUDGET.getAction());
        map.put("измени максимальное число остановок торгового профиля {key:X}", CHANGE_TRADE_PROFILE_SET_MAX_NUMBER_OF_STOPS.getAction());
        map.put("измени максимальное расстояние торгового профиля {key:X}", CHANGE_TRADE_PROFILE_SET_MAX_DISTANCE_FROM_ENTRY.getAction());
        map.put("разрешить запрещенные товары в торговом профиле {state:true/false}", CHANGE_TRADE_PROFILE_SET_ALLOW_PROHIBITED_CARGO.getAction());
        map.put("разрешить планетарные порты в торговом профиле {state:true/false}", CHANGE_TRADE_PROFILE_SET_ALLOW_PLANETARY_PORT.getAction());
        map.put("разрешить системы с разрешением в торговом профиле {state:true/false}", CHANGE_TRADE_PROFILE_SET_ALLOW_PERMIT_SYSTEMS.getAction());
        map.put("разрешить strongholds в торговом профиле {state:true/false}", CHANGE_TRADE_PROFILE_SET_ALLOW_STRONGHOLDS.getAction());

        // announcements / app settings
        map.put("переключи радио, радиотрафик, радиопередачи {state:true/false}", SET_RADIO_TRANSMISSION_MODE.getAction());
        map.put("объявления радарных контактов {state:true/false}", SET_RADAR_CONTACT_ANNOUNCEMENT.getAction());
        map.put("объявления открытий {state:true/false}", DISCOVERY_ON_OFF.getAction());
        map.put("объявления маршрута {state:true/false}", ROUTE_ON_OFF.getAction());
        map.put("отключи все объявления, выключи все уведомления", DISABLE_ALL_ANNOUNCEMENTS.getAction());
        map.put("очисти напоминания, сбрось напоминания", CLEAR_REMINDERS.getAction());
        map.put("установи напоминание {key:X}, напомни {key:X}", SET_REMINDER.getAction());

        // UI panels
        map.put("активировать, активируй", ACTIVATE.getAction());
        map.put("покажи панель транзакций, открой транзакции", SHOW_TRANSACTIONS.getAction());
        map.put("покажи панель контактов, открой контакты", SHOW_CONTACTS.getAction());
        map.put("покажи панель навигации, открой навигацию", SHOW_NAVIGATION.getAction());
        map.put("покажи чат, открой чат, панель связи, коммс панель", SHOW_CHAT_PANEL.getAction());
        map.put("покажи почту, открой входящие, email", SHOW_INBOX_PANEL.getAction());
        map.put("покажи социальную панель, открой социальную панель", SHOW_SOCIAL_PANEL.getAction());
        map.put("покажи историю, открой историю", SHOW_HISTORY_PANEL.getAction());
        map.put("покажи эскадрилью, открой эскадрилью", SHOW_SQUADRON.getAction());
        map.put("покажи статус, открой панель статуса", SHOW_STATUS_PANEL.getAction());
        map.put("покажи управление авианосцем, открой carrier management", DISPLAY_CARRIER_MANAGEMENT.getAction());
        map.put("открой карту галактики, покажи карту галактики", OPEN_GALAXY_MAP.getAction());
        map.put("открой карту системы, покажи локальную карту, системная карта", OPEN_SYSTEM_MAP.getAction());
        map.put("закрыть панель, выйти, закрыть", EXIT_CLOSE.getAction());

        // pirate massacre missions
        map.put("навигация к системе выдачи миссий, курс к системе провайдера миссий", RECON_PROVIDER_SYSTEM.getAction());
        map.put("навигация к пиратскому провайдеру миссий, лети к выдаче пиратских миссий", NAVIGATE_TO_PIRATE_MISSION_PROVIDER.getAction());
        map.put("активные миссии, текущие миссии, журнал миссий, какие миссии, статус миссий, наши миссии", ANALYZE_MISSIONS.getAction());
        map.put("пиратская миссия, счет убийств, сколько убийств, прогресс massacre миссии, сколько пиратов осталось", PIRATE_MISSION_PROGRESS.getAction());
        map.put("найди охотничьи угодья {key:X}, найди место охоты {key:X}", FIND_HUNTING_GROUNDS.getAction());
        map.put("разведка охотничьих угодий, навигация к целевой системе, курс к системе охоты", RECON_TARGET_SYSTEM.getAction());
        map.put("игнорируй охотничьи угодья, пропусти место охоты", IGNORE_HUNTING_GROUND.getAction());
        map.put("подтверди охотничьи угодья, подтверди целевую систему", CONFIRM_HUNTING_GROUND.getAction());

        // science / mining / biology
        map.put("добавь цель добычи {key:X}", ADD_MINING_TARGET.getAction());
        map.put("удали цель добычи {key:X}", REMOVE_MINING_TARGET.getAction());
        map.put("очисти цели добычи", CLEAR_MINING_TARGETS.getAction());
        map.put("объявления добычи {state:true/false}", MINING_ON_OFF.getAction());
        map.put("найди мозговые деревья {key:X, max_distance:Y}", FIND_BRAIN_TREES.getAction());
        map.put("найди место добычи, найди точку добычи, найди hotspot, где майнить, найди астероидное поле {key:X, max_distance:Y}", FIND_MINING_SITE.getAction());
        map.put("найди место добычи трития, найди тритиевое поле {key:X, max_distance:Y}", FIND_FLEET_CARRIER_FUEL_MINING_SITE.getAction());
        map.put("навигация к следующему биосэмплу, к следующему образцу, к следующей органике, запись кодекса, навигация к кодексу", NAVIGATE_TO_NEXT_BIO_SAMPLE.getAction());
        map.put("просканируй систему, открой fss, полный скан, хонк, скан системы, full spectrum scan", OPEN_FSS_AND_SCAN.getAction());
        map.put("найди ближайшую vista genomics, найди геномику", FIND_VISTA_GENOMICS.getAction());
        map.put("удали запись кодекса, удали этот кодекс, удали эту запись, удали эту органику", DELETE_CODEX_ENTRY.getAction());

        map.put("проверь бинды, отсутствующие клавиши, неназначенные клавиши, клавиатурные привязки, проверка клавиш", KEY_BINDINGS_ANALYSIS.getAction());
        map.put("биосигналы в системе выполнены, органика просканирована в системе, сколько биосэмплов в системе, биосигналы в системе, какие планеты имеют биосигналы, какие планеты еще нужно сканировать", BIO_SAMPLE_IN_STAR_SYSTEM.getAction());
        map.put("экзобиологические образцы, биологические образцы, органика в локации, какие организмы, что осталось сканировать, оставшиеся организмы, прогресс экзобиологии, что просканировано здесь, какая органика на этой планете", EXOBIOLOGY_SAMPLES.getAction());
        map.put("расстояние до последнего биосэмпла, как далеко до образца, расстояние до последней органики, дистанция до биосэмпла, навигация к биосэмплу", DISTANCE_TO_LAST_BIO_SAMPLE.getAction());
        map.put("анализ биома, какой биом {key:X}, планетарный биом, анализ атмосферы, какая жизнь здесь, тип биома", PLANET_BIOME_ANALYSIS.getAction());
        map.put("звездные объекты, планеты в системе, посадочные планеты, планета или луна пригодна для посадки, тела в системе, какие планеты, сколько планет, кольца, ледяные кольца", QUERY_STELLAR_OBJETS.getAction());
        map.put("сигналы в системе, какие сигналы в системе, что есть в системе, обнаруженные сигналы, fss сигналы, hotspots, resource extraction sites, конфликтные зоны, эмиссии", QUERY_STELLAR_SIGNALS.getAction());
        map.put("геосигналы, геологические сигналы, вулканические сигналы, геологическая активность, вулканическая активность", QUERY_GEO_SIGNALS.getAction());

        map.put("авианосцы в системе, carriers в системе, сколько авианосцев, есть ли авианосцы рядом", QUERY_CARRIERS.getAction());
        map.put("маршрут авианосца, навигация авианосца, путь авианосца, сколько прыжков на маршруте авианосца, прыжков осталось", CARRIER_ROUTE_ANALYSIS.getAction());
        map.put("маршрут авианосца, куда летит авианосец, следующий прыжок авианосца, конечная точка авианосца", CARRIER_ROUTE.getAction());
        map.put("тритий авианосца, топливо авианосца, сколько трития, уровень трития, резерв трития", CARRIER_TRITIUM_SUPPLY.getAction());
        map.put("статус авианосца, финансы авианосца, баланс авианосца, обзор авианосца, средства авианосца, сколько авианосец может работать, запас хода авианосца", CARRIER_STATUS.getAction());
        map.put("eta авианосца, когда прибудет авианосец, сколько до прибытия авианосца, время прыжка авианосца", CARRIER_ETA.getAction());
        map.put("расстояние до авианосца, где наш авианосец, как далеко авианосец, дистанция до авианосца", DISTANCE_TO_CARRIER.getAction());
        map.put("безопасность системы, контроль фракции, кто контролирует систему, уровень безопасности, владелец системы, доминирующая фракция", SYSTEM_SECURITY_ANALYSIS.getAction());
        map.put("торговый профиль, торговые настройки, параметры торговли, торговая конфигурация, критерии торговли", TRADE_PROFILE_ANALYSIS.getAction());
        map.put("расстояние до звездного объекта, как далеко до тела, расстояние до планеты {key:X}, расстояние до луны, как далеко до станции, дистанция до тела", DISTANCE_TO_BODY.getAction());
        map.put("последний скан, что мы сканировали, последний просканированный объект, самый свежий скан", LAST_SCAN_ANALYSIS.getAction());
        map.put("инвентарь материалов {key:X}, сколько предметов {key:X}, сколько материалов {key:X}, есть ли материал {key:X}, сколько {key:X} у нас есть, инженерный материал {key:X}, сырьевой материал {key:X}, manufactured материал {key:X}, закодированный материал {key:X}", MATERIALS_INVENTORY.getAction());
        map.put("материалы планеты, материалы здесь, какие материалы на этой планете, поверхностные материалы, залежи материалов, минералы на планете", PLANET_MATERIALS.getAction());
        map.put("прибыль исследования, стоимость открытий, сколько стоит исследование, ценность сканов, прибыль картографирования, ценность экзобиологии", EXPLORATION_PROFITS.getAction());
        map.put("где я нахожусь, где мы находимся, наша позиция, в какой системе мы, где я, наши координаты, текущая система, на какой планете мы, текущая позиция, длина дня", CURRENT_LOCATION.getAction());
        map.put("информация о цели fsd, анализ пункта назначения, какую звезду мы выбрали, анализ fsd цели", FSD_TARGET_ANALYSIS.getAction());
        map.put("проложенный маршрут, топливо на следующей остановке, наличие топлива на маршруте, анализ маршрута, мы уже на месте, текущий маршрут, сколько прыжков осталось, следующая звезда scoopable", PLOTTED_ROUTE_ANALYSIS.getAction());
        map.put("торговый маршрут, текущий торговый план, чем мы торгуем, наш торговый план, торговые этапы", TRADE_ROUTE_ANALYSIS.getAction());
        map.put("оснащение, улучшения корабля, доступные модули, какие модули на станции, доступное оборудование, купить модули, части корабля", LOCAL_OUTFITTING.getAction());
        map.put("верфь, корабли на продажу, какие корабли на станции, купить корабль, доступные корабли, новый корабль", LOCAL_SHIPYARD.getAction());
        map.put("что в грузовом отсеке, что мы везем, содержимое груза, товары на борту, чем мы загружены", CARGO_HOLD_CONTENTS.getAction());
        map.put("профиль игрока", PLAYER_PROFILE_ANALYSIS.getAction());
        map.put("компоновка корабля, отчет о повреждениях, модули корабля, готовность к бою, оборудование корабля, характеристики корабля, на чем я лечу, что установлено, генератор щитов, усиление корпуса, сенсоры, двигатели, frameshift, топливный ковш", SHIP_LOADOUT.getAction());
        map.put("детали станции, какие сервисы здесь, какие услуги здесь, сервисы на станции, что предлагает станция, информация о станции, оборудование станции, что есть на этой станции, доступные сервисы", STATION_DETAILS.getAction());
        map.put("награды за головы, общий bounty, сколько bounty, заработок с bounty, кредиты за bounty", TOTAL_BOUNTIES.getAction());
        map.put("расстояние до пузыря, расстояние до sol, расстояние от sol, расстояние до земли, как далеко от пузыря, как далеко от цивилизации, расстояние от населенного космоса", DISTANCE_TO_BUBBLE.getAction());
        map.put("текущее время, сколько времени, время на земле, галактическое время, utc время, реальное время", TIME_IN_ZONE.getAction());
        map.put("напоминание, какое было напоминание, напоминание пункта назначения, есть ли напоминания, что мы установили как напоминание", REMINDER.getAction());
        map.put("локальные рынки, рынки на станциях и поселениях, рынки на аванпостах в системе", ANALYZE_MARKETS.getAction());
    }
}
