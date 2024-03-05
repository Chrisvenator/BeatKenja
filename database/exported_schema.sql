
    create table assignment_difficulty (
       id integer not null auto_increment,
        fk_difficulty_id integer,
        fk_pattern_description_id integer,
        primary key (id)
    ) engine=InnoDB

    create table assignment_genre (
       id integer not null auto_increment,
        fk_genre_id integer,
        fk_pattern_description_id integer,
        primary key (id)
    ) engine=InnoDB

    create table assignment_tag (
       id integer not null auto_increment,
        fk_pattern_description_id integer,
        fk_tag_id integer,
        primary key (id)
    ) engine=InnoDB

    create table difficulty (
       id integer not null auto_increment,
        name varchar(255),
        primary key (id)
    ) engine=InnoDB

    create table genre (
       id integer not null auto_increment,
        name varchar(255),
        primary key (id)
    ) engine=InnoDB

    create table note (
       id integer not null auto_increment,
        cut_direction integer,
        line_index double precision,
        line_layer double precision,
        type integer,
        primary key (id)
    ) engine=InnoDB

    create table pattern (
       id integer not null auto_increment,
        count integer,
        followed_by_note_id integer,
        note_id integer,
        pattern_description_id integer,
        primary key (id)
    ) engine=InnoDB

    create table tag (
       id integer not null auto_increment,
        name varchar(255),
        primary key (id)
    ) engine=InnoDB
