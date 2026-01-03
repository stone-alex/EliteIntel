create table if not exists brain_trees (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    starSystem TEXT   not null unique,
    json       TEXT   not null,
    x          double not null default 0,
    y          double not null default 0,
    z          double not null default 0
);
create unique index if not exists idx_brain_trees_star_system on brain_trees (starSystem);

create table if not exists material_names (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT not null unique
);

INSERT OR IGNORE INTO material_names (name) VALUES
    ('Antimony'),
    ('Arsenic'),
    ('Cadmium'),
    ('Carbon'),
    ('Chromium'),
    ('Germanium'),
    ('Iron'),
    ('Manganese'),
    ('Mercury'),
    ('Molybdenum'),
    ('Nickel'),
    ('Niobium'),
    ('Phosphorus'),
    ('Polonium'),
    ('Ruthenium'),
    ('Selenium'),
    ('Sulphur'),
    ('Technetium'),
    ('Tellurium'),
    ('Tin'),
    ('Tungsten'),
    ('Vanadium'),
    ('Yttrium'),
    ('Zinc'),
    ('Zirconium');