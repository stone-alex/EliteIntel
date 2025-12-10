alter table player drop column bounty_collected_this_session;
alter table player drop column total_bounty_profit;
alter table player drop column ship_cargo_capacity;
alter table player drop column ship_fuel_level;
alter table player drop column target_market_station_id;

alter table player add column  bounty_collected_lifetime bigint default 0;
