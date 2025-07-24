SET NAMES 'utf8mb4';
SET CHARACTER SET utf8mb4;
SET collation_connection = 'utf8mb4_unicode_ci';

create table if not exists audience_zone_template
(
    id           bigint auto_increment
        primary key,
    is_active    bit                                  not null,
    max_capacity int                                  not null,
    name         varchar(100)                         not null,
    seating_type enum ('MIXED', 'SEATED', 'STANDING') not null,
    area_id      bigint                               not null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

create table if not exists event_audience_zone
(
    id                 bigint auto_increment
        primary key,
    allocated_capacity int    not null,
    event_id           bigint not null,
    template_id        bigint not null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

alter table event_audience_zone
    add constraint FKfct4kt54yjw62qhtjik42e5a9
        foreign key (template_id) references audience_zone_template (id);

create table if not exists event_categories
(
    id   bigint auto_increment
        primary key,
    name varchar(255) not null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

alter table event_categories
    add constraint UK1et3muobyw9w9dur2ww8bvhh7
        unique (name);

create table if not exists event_gallery_images
(
    event_id   bigint       not null,
    image_path varchar(255) null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

create table if not exists event_has_categories
(
    event_id    bigint not null,
    category_id bigint not null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

alter table event_has_categories
    add primary key (event_id, category_id);

alter table event_has_categories
    add constraint FKkd4n7y6iff8my21tcsihrbh1w
        foreign key (category_id) references event_categories (id);

create table if not exists event_tags
(
    event_id bigint       not null,
    tag      varchar(255) null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

create table if not exists events
(
    id                  bigint auto_increment
        primary key,
    city                varchar(255)                                                                          null,
    country             varchar(255)                                                                          null,
    street              varchar(255)                                                                          null,
    zip_code            varchar(255)                                                                          null,
    created_at          datetime(6)                                                                           not null,
    deleted             tinyint(1) default 0                                                                  not null,
    display_on_homepage bit                                                                                   not null,
    end_date            datetime(6)                                                                           not null,
    full_description    text                                                                                  null,
    is_featured_event   bit                                                                                   not null,
    main_photo_path     varchar(255)                                                                          null,
    name                varchar(255)                                                                          not null,
    short_description   text                                                                                  null,
    start_date          datetime(6)                                                                           not null,
    status              enum ('ARCHIVED', 'CANCELLED', 'COMPLETED', 'DRAFT', 'PENDING_APPROVAL', 'PUBLISHED') not null,
    updated_at          datetime(6)                                                                           not null,
    creator_id          bigint                                                                                not null,
    structure_id        bigint                                                                                not null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

alter table event_audience_zone
    add constraint FK3rrj9t1eu8kv5ua6jox32b80h
        foreign key (event_id) references events (id);

alter table event_gallery_images
    add constraint FK97ckld1ko7cqg15eefv9swy4
        foreign key (event_id) references events (id);

alter table event_has_categories
    add constraint FKmj9asibvf869q25cv1ue95kyv
        foreign key (event_id) references events (id);

alter table event_tags
    add constraint FKiwoyitw224ykom58m5xnoa9y6
        foreign key (event_id) references events (id);

create table if not exists friendships
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6)                                                                not null,
    status      enum ('ACCEPTED', 'BLOCKED', 'CANCELLED_BY_SENDER', 'PENDING', 'REJECTED') not null,
    updated_at  datetime(6)                                                                not null,
    receiver_id bigint                                                                     not null,
    sender_id   bigint                                                                     not null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

alter table friendships
    add constraint UKbjhqa0q4d3irkedtrwyuh74a0
        unique (sender_id, receiver_id);

create table if not exists reservations
(
    id               bigint auto_increment
        primary key,
    reservation_date datetime(6) not null,
    user_id          bigint      not null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

create table if not exists structure_areas
(
    id           bigint auto_increment
        primary key,
    description  text         null,
    is_active    bit          not null,
    max_capacity int          not null,
    name         varchar(255) not null,
    structure_id bigint       not null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

alter table audience_zone_template
    add constraint FKfcdg35x9rmlx7ivdprwqkkp8m
        foreign key (area_id) references structure_areas (id);

alter table structure_areas
    add check (`max_capacity` >= 0);

create table if not exists structure_gallery_images
(
    structure_id bigint       not null,
    image_path   varchar(512) null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

create table if not exists structure_has_types
(
    structure_id bigint not null,
    type_id      bigint not null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

alter table structure_has_types
    add primary key (structure_id, type_id);

create table if not exists structure_social_media_links
(
    structure_id bigint        not null,
    link         varchar(2048) null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

create table if not exists structure_types
(
    id   bigint auto_increment
        primary key,
    icon varchar(255) null,
    name varchar(100) not null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

alter table structure_has_types
    add constraint FKcjpsajedkwf3ky0p2lk8hlif9
        foreign key (type_id) references structure_types (id);

alter table structure_types
    add constraint UKmol749htvj4jfpclftaye5gpm
        unique (name);

create table if not exists structures
(
    id          bigint auto_increment
        primary key,
    city        varchar(255)  not null,
    country     varchar(255)  not null,
    street      varchar(255)  not null,
    zip_code    varchar(255)  not null,
    cover_path  varchar(512)  null,
    created_at  datetime(6)   not null,
    description text          null,
    email       varchar(255)  null,
    is_active   bit           not null,
    logo_path   varchar(512)  null,
    name        varchar(255)  not null,
    phone       varchar(30)   null,
    updated_at  datetime(6)   not null,
    website_url varchar(2048) null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

alter table events
    add constraint FKi6fc9rbvaqu2oc1kib5psv74g
        foreign key (structure_id) references structures (id);

alter table structure_areas
    add constraint FKkttv01jn886hbkub5lkdavi6s
        foreign key (structure_id) references structures (id);

alter table structure_gallery_images
    add constraint FKo7amc1svy1s48lrkan6lvkmxj
        foreign key (structure_id) references structures (id);

alter table structure_has_types
    add constraint FKl3w3kgdeenk5yua338l40c1dr
        foreign key (structure_id) references structures (id);

alter table structure_social_media_links
    add constraint FK74nay97ljhm5q9ep43p7x0yd4
        foreign key (structure_id) references structures (id);

create table if not exists team_members
(
    id         bigint auto_increment
        primary key,
    email      varchar(255)                                                                                 not null,
    invited_at datetime(6)                                                                                  null,
    joined_at  datetime(6)                                                                                  null,
    role       enum ('ORGANIZATION_SERVICE', 'RESERVATION_SERVICE', 'SPECTATOR', 'STRUCTURE_ADMINISTRATOR') not null,
    status     enum ('ACTIVE', 'PENDING_INVITATION')                                                        not null,
    team_id    bigint                                                                                       not null,
    user_id    bigint                                                                                       null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

create table if not exists teams
(
    id           bigint auto_increment
        primary key,
    name         varchar(255) not null,
    structure_id bigint       not null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

alter table team_members
    add constraint FKtgca08el3ofisywcf11f0f76t
        foreign key (team_id) references teams (id);

alter table teams
    add constraint UK1v0mfi1fvmg21q1yjdxptw441
        unique (structure_id);

alter table teams
    add constraint FKq5or8388kcncumkl13cca1it3
        foreign key (structure_id) references structures (id);

create table if not exists tickets
(
    id                     binary(16)                                     not null,
    participant_email      varchar(255)                                   not null,
    participant_first_name varchar(255)                                   not null,
    participant_last_name  varchar(255)                                   not null,
    qr_code_value          varchar(255)                                   not null,
    reservation_date       datetime(6)                                    not null,
    validation_date        datetime(6)                                    null,
    status                 enum ('CANCELLED', 'EXPIRED', 'USED', 'VALID') not null,
    event_id               bigint                                         not null,
    event_audience_zone_id bigint                                         not null,
    reservation_id         bigint                                         not null,
    user_id                bigint                                         null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

alter table tickets
    add primary key (id);

alter table tickets
    add constraint UKbms3gi5rmutd1607nnbc4e08g
        unique (qr_code_value);

alter table tickets
    add constraint FK3utafe14rupaypjocldjaj4ol
        foreign key (event_id) references events (id);

alter table tickets
    add constraint FKkk3qg1y9d37e5cyex03f405yj
        foreign key (event_audience_zone_id) references event_audience_zone (id);

alter table tickets
    add constraint FKtefrntjvcsu43l1fjmybtqqmx
        foreign key (reservation_id) references reservations (id);

create table if not exists user_favorite_structures
(
    id           bigint auto_increment
        primary key,
    added_at     datetime(6) not null,
    structure_id bigint      not null,
    user_id      bigint      not null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

alter table user_favorite_structures
    add constraint UK6535moi5ddjvbj9uhgey5lf6h
        unique (user_id, structure_id);

alter table user_favorite_structures
    add constraint FKme98oq7ea9bqr3xwim9aqcl8g
        foreign key (structure_id) references structures (id);

create table if not exists users
(
    user_type          varchar(31)                                                                                  not null,
    id                 bigint auto_increment
        primary key,
    avatar_path        varchar(255)                                                                                 null,
    consent_given_at   datetime(6)                                                                                  null,
    created_at         datetime(6)                                                                                  not null,
    email              varchar(255)                                                                                 not null,
    first_name         varchar(255)                                                                                 not null,
    is_email_validated tinyint(1) default 0                                                                         not null,
    last_name          varchar(255)                                                                                 not null,
    password           varchar(255)                                                                                 not null,
    role               enum ('ORGANIZATION_SERVICE', 'RESERVATION_SERVICE', 'SPECTATOR', 'STRUCTURE_ADMINISTRATOR') not null,
    updated_at         datetime(6)                                                                                  not null,
    structure_id       bigint                                                                                       null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

alter table events
    add constraint FK7ljm71n1057envlomdxcni5hs
        foreign key (creator_id) references users (id);

alter table friendships
    add constraint FKpk7w2cj6m9n224ny2t7fhi47
        foreign key (receiver_id) references users (id);

alter table friendships
    add constraint FKs7n4v837jm41ijdacqgfe9acw
        foreign key (sender_id) references users (id);

alter table reservations
    add constraint FKb5g9io5h54iwl2inkno50ppln
        foreign key (user_id) references users (id);

alter table team_members
    add constraint FKee8x7x5026imwmma9kndkxs36
        foreign key (user_id) references users (id);

alter table tickets
    add constraint FK4eqsebpimnjen0q46ja6fl2hl
        foreign key (user_id) references users (id);

alter table user_favorite_structures
    add constraint FKlkmrigda7doij1nnrbtnkwmvq
        foreign key (user_id) references users (id);

alter table users
    add constraint UK6dotkott2kjsp8vw4d0m25fb7
        unique (email);

alter table users
    add constraint FKeawn2atjygnod8wrmsy4rruje
        foreign key (structure_id) references structures (id)
            on delete set null;

create table if not exists refresh_tokens
(
    id          bigint auto_increment
        primary key,
    token       varchar(255)         not null,
    expiry_date datetime(6)          not null,
    revoked     tinyint(1) default 0 not null,
    user_id     bigint               not null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

alter table refresh_tokens
    add constraint UK_refresh_tokens_token
        unique (token);

alter table refresh_tokens
    add constraint FK_refresh_tokens_user_id
        foreign key (user_id) references users (id)
            on delete cascade;

create table if not exists verification_tokens
(
    id          bigint auto_increment
        primary key,
    expiry_date datetime(6)                                                                                     not null,
    is_used     bit                                                                                             not null,
    payload     text                                                                                            null,
    token       varchar(255)                                                                                    not null,
    token_type  enum ('ACCOUNT_DELETION_CONFIRMATION', 'EMAIL_VALIDATION', 'PASSWORD_RESET', 'TEAM_INVITATION') not null,
    user_id     bigint                                                                                          null
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

alter table verification_tokens
    add constraint UK6q9nsb665s9f8qajm3j07kd1e
        unique (token);

alter table verification_tokens
    add constraint FK54y8mqsnq1rtyf581sfmrbp4f
        foreign key (user_id) references users (id);

