alter table post alter column link set not null;
alter table post add constraint link_unique unique(link);