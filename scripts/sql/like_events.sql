-- 
-- create like-events table
-- 
create table like_events (
id varchar(40) not  null unique,
user_id varchar(40) not null,
entity_id varchar(140) not null,
entity_type varchar(20) not null,
ts timestamp ,
created_at timestamp default CURRENT_TIMESTAMP,
CONSTRAINT pk_id PRIMARY KEY(id)
);
