alter table game_session
    drop column encryptedSTTKey;
alter table game_session
    drop column useLocalSTT;

delete
from help_topics
where id > 0;

insert into help_topics
values (1, 'carrier routing',
        'I use crowd sourced data to plot routes. This means not all destinations are available. But we can give it a try. Open any Galaxy Map, select your target star, and copy its name using the last button in the right hand column. After that say: Calculate Fleet Carrier Route. I’ll grab the star, check your carrier’s position, and request route calculation. If successful - we have a route. However if target system is not in available in crowd data - you will have to repeat the process until we find one that is. Once we have a route we are ready do jump. To jump: open the Galaxy map from the Fleet Carrier menu, not your ships galaxy map. Place the cursor in to the text field and when ready ask me to Enter next Fleet Carrier destination, and I’ll paste the next system. Schedule the jump in-game and repeat.');
insert into help_topics
values (2, 'commodity search',
        'Need to buy a commodity like Tritium or Bromellite? Say Where can I buy Bromellite? and I’ll search recent crowd-sourced market data for markets within 250 light years. If I find the commodity I will automatically pot the route to the market with the best price available. I will also save a reminder, and will remind you when you get to that system. Or you can ask me remind me, where we going? Note: some ED specific commodities might not be recognized by speech to text. ');
insert into help_topics
values (3, 'custom commands',
        'I can execute most of the game controls, but I will not fire weapons, drop all cargo, switch to silent running or self-destruct. Other than key-mapping for the game controls I can do some custom commands. Say All power to shields, engines, weapons, or Equalize power to instantly re-balance pips. Say Close or exitto quickly escape nested menus and return to your heads-up display. Say Clear Codex Entries to wipe scanned entries, but use cautiously. Say Clear Cache to reset the session. Careful, it’s a big reset. Say navigate to Fleet Carrier or lets go home, to chart a course to your carrier’s last known location or home star system. Say Set as home system to mark your current system as home. Say Set optimal speed to hit 75% throttle in supercruise, avoiding station overshoots. There are other commands and queries. Just talk to me and get to know me.');
insert into help_topics
values (4, 'exobiology',
        'Fly low, spot bio colonies, and zap them with your Composition Analyzer to log a Codex Entry. I will tag the coordinates of that colony. (But stay close to the sample for best results). Tag all species you find. Land where you can, and take the SRV trip. Ask me to navigate to next organic and I will guide you. Scan a sample like Frutexa, and I will know to focus on that genus for your next navigation request. Keep scanning until done. I use your Codex tag coords, so scan up close. More tags give me more spots to guide you to. If you end up with a codex that is too close and you can not scan it, ask me to delete current codex. If the codes is being navigated to I will forget about it.');
insert into help_topics
values (5, 'exploration',
        'On entering an uncharted system, if discovery announcements are on, I’ll let you know if you’re the first to get here! To scan, say: Open FSS and scan, or just say Hunk! - I will prep the FSS for you to hunt planets and signals. As you scan, I will only announce interesting stuff, like high-value planets, or ones with geo signals or exobiology. After you scan everything, ask me anything about the data you have collected. You can toggle my automated Discovery announcements On or Off. Just ask.');
insert into help_topics
values (6, 'planetary navigation',
        'Targeting a specific spot like material coords or a bio sample? I will guide you from orbit to the target on the ground. Say Navigate to latitude 41.43, longitude -75.23 (or your coordinates), and follow the guidance. In supercruise, keep medium speed to catch my voice prompts. Too fast and you will miss turns; too slow, you might drop early. Landing on the Dark side? Fly by instruments like a pro. I will give glide angle updates, like glide angle, -35 degrees-you decide to glide or adjust. After gliding, I will guide you to within 1,000 meters, and prompt you with: Find a parking spot. In your SRV or on foot, I’ll keep directing until you’re 50 meters from the target. Want out? Say Cancel navigation, and I will stop, no fuss.');
insert into help_topics
values (7, 'system settings',
        'Customize my settings with natural language. Say Turn Route Announcements On to hear about jumps, like scoopable stars or system security. Say Turn Discovery Announcements On for alerts on first mapped systems or high-value planets. Off keeps it quiet (default: On). Say Add Mining Target Painite to flag prospector limpet hits for that material. This one is unset by default. Say Turn On Radio for spicy transmissions like pirate threats, but I will skip boring chatter like cruise ship spam. (default: Off). Ask me for route or discovery details anytime.');
insert into help_topics
values (8, 'available queries',
        'I can provide data analysis on just about everything I have access to. Provided I have the data. If you run with a cloud LLM, can also tap in to my own knowledge including subjects outside of Elite Dangerous universe.');
insert into help_topics
values (9, 'Pirate Massacre Missions',
        'Ask me to find hunting grounds within a range in light years. I will find potential target system with Resource Extraction sites. You will have to do the reconnaissance at the target location first. I may not see the data on arrival, so either scan a nav beacon, or fly close to the ring planets. If I still do not issue a confirmation, you can confirm it manually by saying hunting ground confirmed. Now that we have a hunting ground, ask me to navigate to mission provider system. Go there, and land at ports. Take missions for the target system and same pirate faction as target. This will confirm reconnaissance. Do the missions. As you progress through the missions you can ask me about remaining kills and profits.');
insert into help_topics
values (20, 'Trade',
        'Use your trade ship and say Plot me a trade route to start trading by buying low and selling high. How the Greed Engine Works: If your current ship has zero cargo capacity the app will remind you that this ship cannot carry cargo and to switch to a freighter even a Sidewinder with one cargo rack. Every ship gets its own trading profile set once and remembered. Setting or changing the profile is voice only by saying Alter trading profile followed by the setting. Required minimum settings: Alter trading profile starting capital twenty million, Alter trading profile maximum distance from star six thousand light seconds, Alter trading profile maximum stops eight. Pro tip: say maximum stops eight not to eight as speech-to-text may mishear to as two. Optional settings: Alter trading profile allow permit systems, Alter trading profile allow planetary ports, Alter trading profile allow fleet carriers (note they might move), Alter trading profile allow prohibited commodities. To calculate the route say Calculate trade route or Plot profitable trade route then wait 30-60 seconds for Spansh to compute. To navigate say Plot route to next trade stop or similar with station synonyms. On arrival the app announces the station and commodity to buy or sell. To check say What am I buying here or Where am I selling this cargo specifying buy or sell. After loading cargo say Plot route to next trade stop again to go to the sell location. Route persists until finished overridden or canceled. For a new route empty cargo hold first.');

alter table game_session
    add column notificationVolume float default 0.75;