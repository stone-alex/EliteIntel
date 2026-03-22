# EliteIntel AI Queries & Commands Guide

Hey Commander! This is a reference for the kinds of things you can ask or tell your **Elite Intel** sidekick.

**Idealy - You don't need to memorize any of these** - just speak naturally, and the app figures out what you mean. This
list exists so you know what's possible, not so you can recite scripts.

## First thing first. Having trouble?

- If app is firing random commands for no reason: brain problem (LLM is weak or configuration is wonky)
- If app is firing commands, but they do not activate: hands problem (Key bindings)
- If app can't hear you well: ears problem (noisy room low noise-to-rms ratio, audio not calibrated etc)
- If app does not speak: mouth problem. Audio connection is not routed properly or routed to an audio end point that you are not monitoring check audio routing at OS level.

### [Full Wiki is here](https://github.com/stone-alex/EliteIntel/wiki)

But here are some essentials to get you started.

## Audio Input

**Calibrate the audio in the app**. If the difference between Noise Floor and RMS is too low (e.g. less than 400) the
app will have hard time understanding you. A good ratio is at least 800-1000. Speakers and mic do not go together.
Headphones and mic are recommended.

**Play around with STT Threads**. Under the Settings / Audio. What do they do? They tell speech recognition how many
threads to ask from the processor.
It is a min/max. You can ask for 10 threads, but if your processor can only give you 5 - 5 is all you get. If your
processor can only
give it 1 thread, one thread is all you get even if you ask for 11 threads. The more threads you get
the faster the STT will process your speech. Faster, but not better. This does not have effect on quality.

## How to Speak Commands (successfully)

This app is not Voice Attack. It does not work well with a single word utterance because it has to extract meaning.
If you say "Inventory!" the app does not know what you want to do with it. Do you want to query cargo, or access
materials or open a panel?
Clarity of your commands is the key.

### General rules:

- Rule number 1. You are a commander:  **command, do not mumble**, speak clearly like a drill sargent.
- Rule number 2. **Be clear in your intent**. Single words do not convey meaning, they are too ambiguous.

---

## App

- Check missing or unbound key bindings.

## Exploration & Location

- Enable / Disable discovery announcements.
- Where are we right now?
- How far are we from the Bubble / our fleet carrier / last bio-sample?
- What materials are available on this planet / moon?
- Analyze the most recent scan / body data.
- What's the ETA for our fleet carrier jump?
- What time is it in [real Earth city]? (cloud only)
- Analyze the biome for this star system.
- Which planets still need bio or organic scans?
- What bio scans have we completed?
- What organics do we still have to scan?
- What landable planets or moons are in this system?
- What signals are in this system?
- Which planets have geo signals?
- Open FSS and scan. / Perform filtered spectrum scan.

## Exobiology

- Bio-sample progress on current planet / current star system (completed, partial, remaining).
- Exploration profit potential in this system.
- Last bio-sample location and distance. (requires codex entry scan with composition analyzer)
- Navigate to next bio-sample / organic / codex entry. (requires codex entry scan with composition analyzer)
- What organics or biology is on this planet / moon?
- Navigate to next organic / sample / codex entry.
- Biome analysis for [star system / planet / moon name].

## Fleet Carrier

Make sure you mention carrier, else it might think you are talking about the ship!

- What is our carrier range (includes reserve fuel if manually set) / stats / fuel.
- Set fuel level [amount] (sets the fleet carrier fuel reserves)
- Where is our carrier jumping next?
- How long until carrier arrival? (Fleet carrier ETA)
- What's my fleet carrier fuel status / jump range / fuel reserve?
- How long can we operate on current funds?
- How far can carrier we jump with current tritium?
- What's on the carrier route?
- Distance from the fleet carrier?

## Ship & Systems

- Enable / Disable route announcements.
- Is the next star scoopable?
- Analyze FSD target (allegiance, traffic, security, geo/bio data, etc.).
- What's in my cargo hold?
- What's my ship loadout?
- Analyze our route. (summary of the route, fuel availability
- What's my fuel level?

## Stations & Markets

- What services are at local stations?
- Outfitting / ship parts / modules for sale here?
- Any ships for sale at this station?
- Monetize my route (requires a trade profile set see Trade Profile Setup)
- Calculate trade route (requires a trade profile set see Trade Profile Setup)
- Where do I need to go to buy/sell [commodity]?
- What's in the local market?
- Data for stations, ports, and settlements in system.
- Remind me <blah> (reads reminders set during trade route run, tells you what station to go to etc.)

## Trade Profile Setup

- Alter trade profile set max distance from entry to 6000 (will filter stations for this range)
- Alter trade profile set the starting budget to 25000000 (sets the starting capital for the trade route)
- Alter trade profile set max stops to 10. (route will be no longer than 10 stops)

- Alter trading profile, starting capital [amount].
- Alter trading profile, maximum distance from star [X] light seconds.
- Alter trading profile, maximum stops [N].
- Alter trading profile, allow permit systems [on/off].
- Alter trading profile, allow planetary ports [on/off].
- Alter trading profile, allow fleet carriers [on/off].
- Alter trading profile, allow prohibited commodities [on/off].
- Alter trading profile, allow strongholds [on/off].
- Describe trade profile.
- Cancel / clear trade route.

## ⚔️ Combat & Missions

- Find hunting a ground (Will find target/mission provider paris. Will tell you to fly to a target system and confirm the presence of the resource extraction site)
- Confirm hunting ground (will manually confirm the presence of the resource extraction site, might be needed if we didnot detect it from beacon or auto-discovery)
- Plot / navigate reconnaissance route to hunting ground.
- Navigate to the mission provider. (this will activate once you have a confirmed hunting ground. Fly there take pirate missions against the target faction. Ask to navigate again, the app may have another star system with missions against the same faction)
- Navigate to active mission. (will navigate to a first mission star system in the list)
- How many pirate kills left? (calculates the stack)
- Potential profit from active pirate missions.
- Mission progress summary.
- Total bounties collected this session.
- What active missions do I have? (any missions, not just pirate)
- Target power plant (works now)
- Target Subsystem [fsd / power plant / thrusters / etc.] (broken now)
- Note: "Light Speed!" or "Supercruise!" will retract hardpoints / gear if needed and bail you out of there, but only if you are not mass-locked.

## 🧭 Navigation Commands

- Navigate to coordinates [latitude] [longitude]. (will guide you from orbit to location within 1 km, once landed within 100m)
- Navigate to landing zone. (will give you GPS nav back to where your ship landed last time)
- Navigate to next bio sample / codex entry.
- Navigate to nearest Fleet Carrier.
- Navigate to carrier / base / home system.
- Plot route to next trade stop / station / port.
- Calculate Fleet Carrier route. *(Open galaxy map, select star, copy name to clipboard first.)*
- Enter next Fleet Carrier destination. *(Open Fleet Carrier galaxy map first.)*
- Calculate trade route / plot profitable trade route.
- Find nearest Vista Genomics.
- Find nearest human / guardian tech broker.
- Find nearest raw / encoded / manufactured material trader.
- Find brain trees [within X light years].
- Find mining location for [material] [within X light years].
- Find where we can mine Tritium [within X light years].
- Find where we can buy [commodity] [within X light years].
- Find hunting grounds.
- Cancel navigation.
- Plot route to Fleet Carrier.
- Take me home / navigate to home system.
- Set as home system.
- Set optimal speed. *(Sets throttle to 75% for supercruise approach.)*
- Set fuel reserve to [amount].
- Target next system in route.
- Wing nav lock / lock on wing.
- Select / target highest threat.
- Target power plant / subsystem.

## 🎮 Ship Controls

- gear up / down.
- Deploy / retract hardpoints / weapons.
- Weapons Hot! (same as deploy hardpoints)
- Deploy heat sink.
- Deploy / recover SRV / car / buggy
- Open / close cargo scoop, cargo bay, cargo hatch.
- Request docking. / Contact tower, get a landing pad, parking spot etc.
- Engage Supercruise / light speed / FSD. (actual supercruise, not jump to next system)
- Jump to hyperspace / let's go / next waypoint. (actual jump, not supercruise)
- Drop / disengage FSD / drop from supercruise, dopr here!
- Full stop / engine stop / set speed zero.
- Set speed to: quarter / half / three-quarter / full throttle.
- Set speed plus / minus [amount].
- All power to shields / engines / weapons. Equalize power. / max shields, max engines, max weapons
- Combat mode / analysis mode. (activate combat mode!)
- Night vision on / off.
- Headlights on / off. / turn on headlights, light off etc.
- Drive assist on / off.
- Dismiss ship / go to orbit.
- Return to surface / pick me up.
- Open FSS and scan / honk / scan system / full spectrum scan.
- Show / display - galaxy map / local map / close map.
- Show comms panel / contacts panel / internal panel / central panel.
- Show carrier management panel.
- Exit to HUD.
- Interrupt / silence / cancel [stops TTS mid-sentence].
- Activate / punch it / engage.
- Show Navigation panel.
- Show Transaction panel.
- Show Home Panel.
- Show Modules panel.
- Show Fire groups
- Show ship panel
- Show manifest / inventory panel / cargo hold panel. (note - show manifast works without word panel, the other variants
  need word panel, else it can be confused with query cargo etc)

## 🎙️ Fighter Commands

- Order fighter to defend ship. (not tested, could be broken)
- Order fighter to focus my target. (not tested, could be broken)
- Order fighter to hold fire. (not tested, could be broken)
- Order fighter return to mothership. (not tested, could be broken)

## ⚙️ App & Session Commands

- Ignore me / stop ignoring me / listen to me. (ignore all commands or monitor commands)
- Change voice to [name]
- List available voices.
- Change personality to [Professional / Friendly / Unhinged / Rogue]. (could only)
- Change profile to [Imperial / Federation / Alliance]. (could only)
- Set reminder [text]. / Remind me [text].
- Clear reminders.
- Toggle route announcements on / off.
- Toggle discovery announcements on / off.
- Toggle mining and material announcements on / off.
- Toggle radio chatter on / off.
- Add mining target [material name].
- Clear mining targets.
- Clear codex entries. / Delete codex entry.
- Clear cache. *(Big red button - clears session. Use with caution.)*

## 🆘 Help commands (Cloud LLMs only)

- Help with <topic> (e.g. Help with pirate missions <- will explain how pirate missions features work)

## 💬 General Chat (Cloud LLMs only - local LLM will be stiff)

Anything else! When no specific command or query matches, Elite Intel falls back to general conversation. Ask about game
lore, real-world topics, your ship build, what to watch on Netflix - whatever. The AI's not just a command parser; it's
a Sidekick.

---

**Pro Tip**: Just talk like you're talking to a crewmate.
*"Hey, how's fuel looking?"* → AI checks ship hydrogen.
*"Where's the nearest market for LTDs?"* → AI finds the station.
*"What's growing on this rock?"* → AI runs biome + bio-sample analysis.
*"Silence"* or *"Shut up"* → AI stops talking mid-sentence.

[More commands here](https://github.com/stone-alex/EliteIntel/wiki/Obscure-System-Commands)

Fly safe, Commander! o7

----
Community 👉[**Matrix**](https://matrix.to/#/#krondor:matrix.org)👈
