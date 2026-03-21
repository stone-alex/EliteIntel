update player
set is_discovery_announcement_on = 0
where is_discovery_announcement_on is 1;
update player
set is_route_announcement_on = 0
where is_route_announcement_on is 1;
update player
set is_mining_announcement_on = 0
where is_mining_announcement_on is 1;
update player
set is_radio_transmission_on = 0
where is_radio_transmission_on is 1;