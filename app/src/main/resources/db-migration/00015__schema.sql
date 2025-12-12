alter table game_session add column sendMarketData boolean default false;
alter table game_session add column sendOutfittingData boolean default false;
alter table game_session add column sendShipyardData boolean default false;