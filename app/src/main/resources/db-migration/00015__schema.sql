alter table game_session
add column sendMarketData boolean default false;
alter table game_session
add column sendOutfittingData boolean default false;
alter table game_session
add column sendShipyardData boolean default false;

create table if not exists mission_provider (
    id                     INTEGER PRIMARY KEY AUTOINCREMENT,
    starSystem             text             not null,
    x                      double precision not null,
    y                      double precision not null,
    z                      double precision not null,
    missionProviderFaction text,
    targetFactionID        integer
);

create table if not exists pirate_factions (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    starSystem    TEXT             not null unique,
    x             double precision not null,
    y             double precision not null,
    z             double precision not null,
    hasResSite    boolean          not null default false,
    targetFaction TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS mission_provider_null_unique
    ON mission_provider(starSystem, targetFactionID)
    WHERE missionProviderFaction IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS mission_provider_faction_unique
    ON mission_provider(starSystem, missionProviderFaction, targetFactionID)
    WHERE missionProviderFaction IS NOT NULL;

create table if not exists help_topics (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    topic          TEXT             not null,
    help           TEXT             not null
);


insert into help_topics (topic, help) values ('carrier routing','I use Spansh to plot routes. Open any Galaxy Map, select your target star, and copy its name using the last button in the right hand column. After that say: Calculate Fleet Carrier Route—I’ll grab the star, check your carrier’s position, and save a Spansh route. If Spansh misses your current system, I’ll find the closest known star. However if target system is not in Spansh you will have to repeat the process until we find one that is. To jump: open the Fleet Carrier Map, place the cursor in to the text field and when ready ask me to Enter next Fleet Carrier destination, and I’ll paste the next system. Schedule the jump in-game and repeat.');
insert into help_topics (topic, help) values ('commodity search','Need to buy a commodity like Tritium or Bromellite? Say Where can I buy Tritium? and I’ll search EDSM for markets within 250 light years, save the info, and tell you, Found 5 markets for Tritium! To head there, say Navigate to the best market—I’ll plot a route to the cheapest system’s station via your Galaxy Map (be in the cockpit). Forgot the station? Ask Where’s the market located? and I’ll remind you. Note: Speech-to-text may trip on names like Grandidierite—add fixes like grande=grandidierite to the dictionary directory in my install. EDSM data isn’t real-time, so prices or stock may vary.');
insert into help_topics (topic, help) values ('custom commands','I can execute most of the game controls, but I will not fire weapons or switch to silent running. Other than key-mapping for the game controls I can do some custom commands. Say All power to shields, engines, weapons, or Equalize power to instantly rebalance pips. Say Exit to HUD to quickly escape nested menus and return to your heads-up display. Say Clear Codex Entries to wipe scanned entries, but use cautiously. Say Clear Cache to reset the session—careful, it’s a big reset. Say Plot Route to Fleet Carrier to chart a course to your carrier’s last known location. Say Take Me Home to navigate to your set home system. Say Set as home system to mark your current system as home. Say Set optimal speed to hit 75% throttle in supercruise, avoiding station overshoots. There are other commands and queries. Just talk to me and get to know me.');
insert into help_topics (topic, help) values ('exobiology', 'Hunting organic bio samples like Fungoida in rough terrain? I have a plan to keep you on track. Fly low, spot bio colonies, and zap them with your Composition Analyzer to log a Codex Entry. This saves your ship’s coords in me (stay close to the sample) and may earn 50k credits. Tag all species you find. Land within 2 km if possible, or take an SRV trip—I’ll guide you. Say Navigate to nearest codex entry or Navigate to next bio sample, and I’ll lead you to the closest tagged spot. Scan a sample like Frutexa, and I’ll know to focus on that type for your next navigation request. Keep scanning until done. I use your Codex tag coords, so scan up close. More tags give me more spots to guide you to.');
insert into help_topics (topic, help) values ('exploration', 'Exploring a new star system? I’ll help you. On entering an uncharted system, if discovery announcements are on, I’ll let you know if you’re the first to get here! To scan, say: Open FSS and scan, or just say Hunk! — I’ll will prep the FSS for you to hunt planets and signals. As you scan, I will only announce interesting stuff, like high-value planets, or ones with exobiology. After you scan everything, ask me anything about the data you have collected. You can toggle my automated Discovery announcements On or Off. Just ask.');
insert into help_topics (topic, help) values ('planetary navigation','Targeting a specific spot like material coords or a bio sample? I will guide you to the target on the ground. Say Navigate to latitude 41.435, longitude -75.230 (or your coordinates), and I’ll guide you from orbit to surface. In supercruise, keep medium speed to catch my voice prompts—too fast, you’ll miss turns; too slow, you might drop early. Landing on the Dark side? Fly by instruments like a pro. At ~400 km, I’ll give glide angle updates, like glide angle, -35 degrees—you decide to glide or adjust. After gliding, I’ll guide you to within 1,000 meters, and prompt you with: Find a parking spot. In your SRV or on foot, I’ll keep directing until you’re 50 meters from the target. Want out? Say Cancel navigation, and I’ll stop, no fuss.');
insert into help_topics (topic, help) values ('system settings', 'Customize my settings with natural language. Say Turn Route Announcements On to hear about jumps, like scoopable stars or system security—say Off to silence it (default: On). Say Turn Discovery Announcements On for alerts on first-mapped systems or high-value planets—Off keeps it quiet (default: On). Say Add Mining Target Painite to flag prospector limpet hits for that material—unset by default. Say Turn On Radio for spicy transmissions like pirate threats, but I will skip boring chatter like cruise ship spam. (default: Off). Ask me for route or discovery details anytime.');
insert into help_topics (topic, help) values ('available queries', 'There are too many queries to list. In general, I can provide data analysis on just about everything I have access to. Provided the data acquisition has been implemented. I can also tap in to my own knowledge including subjects outside of Elite Dangerous universe.');
insert into help_topics (topic, help) values ('Pirate Massacre Missions','Ask me to find hunting grounds within a range in light years. I will find potential target system and matching mission provider system. You will have to do reconnaissance in the target mission first to confirm presence of Resource Site. Once Conformed ask me to plot route to mission provider system. Go there, and land at ports. Take missions for the target system. This will confirm reconnaissance. Do the missions. As you progress through the missions you can ask me about remaining kills and profits.');
insert into help_topics (topic, help) values ('Trade Route','Use your trade ship and say Plot me a trade route to start trading by buying low and selling high. How the Greed Engine Works: If your current ship has zero cargo capacity the app will remind you that this ship cannot carry cargo and to switch to a freighter even a Sidewinder with one cargo rack. Every ship gets its own trading profile set once and remembered. Setting or changing the profile is voice only by saying Alter trading profile followed by the setting. Required minimum settings: Alter trading profile starting capital twenty million, Alter trading profile maximum distance from star six thousand light seconds, Alter trading profile maximum stops eight. Pro tip: say maximum stops eight not to eight as speech-to-text may mishear to as two. Optional settings: Alter trading profile allow permit systems, Alter trading profile allow planetary ports, Alter trading profile allow fleet carriers (note they might move), Alter trading profile allow prohibited commodities. To calculate the route say Calculate trade route or Plot profitable trade route then wait 30-60 seconds for Spansh to compute. To navigate say Plot route to next trade stop or similar with station synonyms. On arrival the app announces the station and commodity to buy or sell. To check say What am I buying here or Where am I selling this cargo specifying buy or sell. After loading cargo say Plot route to next trade stop again to go to the sell location. Route persists until finished overridden or canceled. For a new route empty cargo hold first.');