create table difficulty
(
    Difficulty_PK   int auto_increment
        primary key,
    Difficulty_Name varchar(50) not null,
    constraint difficulty_uk
        unique (Difficulty_Name)
);

create table genre
(
    Genre_pk   int auto_increment
        primary key,
    Genre_Name varchar(50) not null,
    constraint genre_uk
        unique (Genre_Name)
);

create table note
(
    Note_PK      int auto_increment comment 'Primary Key of Column Note'
        primary key,
    LineIndex    int           not null,
    LineLayer    int           not null,
    CutDirection int           not null,
    Type         int default 1 not null,
    Color        int default 1 not null,
    constraint note_uniqueKey
        unique (LineIndex, LineLayer, CutDirection, Type, Color)
);

create table tag
(
    Tag_pk   int auto_increment
        primary key,
    Tag_Name varchar(50) not null,
    constraint Tag_uk
        unique (Tag_Name)
);

create table note_probabilities
(
    Note_Probabilities_PK_ID int auto_increment
        primary key,
    BPM                      int   default 120 not null,
    NPS                      float default 5   not null,
    Difficulty_FK_ID         int               not null,
    Note_FK_ID               int               not null,
    Followed_By_Note_FK_ID   int               not null,
    count                    int   default 1   not null,
    Tags_FK_ID               int               not null,
    Genre_FK_ID              int               not null,
    constraint note_probabilities_UniqueKey2
        unique (Followed_By_Note_FK_ID, Genre_FK_ID, Tags_FK_ID, Difficulty_FK_ID, BPM, NPS, Note_FK_ID),
    constraint note_probabilities_difficulty_fk
        foreign key (Difficulty_FK_ID) references difficulty (Difficulty_PK),
    constraint note_probabilities_followed_by_note_fk
        foreign key (Followed_By_Note_FK_ID) references note (Note_PK),
    constraint note_probabilities_genre_Genre_pk_fk
        foreign key (Genre_FK_ID) references genre (Genre_pk),
    constraint note_probabilities_note_fk
        foreign key (Note_FK_ID) references note (Note_PK),
    constraint note_probabilities_tag_Tag_pk_fk
        foreign key (Tags_FK_ID) references tag (Tag_pk)
)
    comment 'Note_Probabilities contains the proabability that a Note x follows a note y';

