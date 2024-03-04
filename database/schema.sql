--Stand 04.03.2024

create table difficulty
(
    id   int auto_increment
        primary key,
    name varchar(50) null,
    constraint Difficulty_unique_name
        unique (name)
);

create table genre
(
    id   int auto_increment
        primary key,
    name varchar(50) not null,
    constraint name_quinque__key
        unique (name)
);

create table note
(
    id            int auto_increment
        primary key,
    line_index    double not null,
    line_layer    double not null,
    cut_direction int    not null,
    type          int    not null,
    constraint note_unique_key
        unique (line_index, line_layer, cut_direction, type)
);

create table pattern
(
    id                     int auto_increment
        primary key,
    note_id                int not null,
    followed_by_note_id    int not null,
    count                  int not null,
    pattern_description_id int not null,
    constraint pattern_unique_key
        unique (note_id, followed_by_note_id, pattern_description_id),
    constraint fk_followed_by_note_id
        foreign key (followed_by_note_id) references note (id),
    constraint fk_note_id
        foreign key (note_id) references note (id)
);

create table pattern_description
(
    id   int auto_increment
        primary key,
    name varchar(100)       not null,
    bpm  double default 120 not null,
    nps  double default 5   not null,
    constraint pattern_description_pk
        unique (name, nps, bpm)
);

create table assignment_difficulty
(
    id                        int auto_increment
        primary key,
    fk_difficulty_id          int not null,
    fk_pattern_description_id int not null,
    constraint difficulty_assignment_unique_key
        unique (fk_pattern_description_id, fk_difficulty_id),
    constraint fk_difficulty_assignment_pattern_description_id
        foreign key (fk_pattern_description_id) references pattern_description (id),
    constraint fk_difficulty_id_fk
        foreign key (fk_difficulty_id) references difficulty (id)
);

create table assignment_genre
(
    id                        int auto_increment
        primary key,
    fk_genre_id               int not null,
    fk_pattern_description_id int null,
    constraint genre_assignment_unique_key
        unique (fk_pattern_description_id, fk_genre_id),
    constraint fk_genre_assignment_pattern_description_id
        foreign key (fk_pattern_description_id) references pattern_description (id),
    constraint fk_genre_id
        foreign key (fk_genre_id) references genre (id)
);

create table tag
(
    id   int auto_increment
        primary key,
    name varchar(50) not null,
    constraint name_unique_key
        unique (name)
);

create table assignment_tag
(
    id                        int auto_increment
        primary key,
    fk_tag_id                 int not null,
    fk_pattern_description_id int not null,
    constraint tag_assignment_unique_key
        unique (fk_pattern_description_id, fk_tag_id),
    constraint fk_tag_assignment_pattern_description_id
        foreign key (fk_pattern_description_id) references pattern_description (id),
    constraint fk_tag_id
        foreign key (fk_tag_id) references tag (id)
);

