/*
 * Definition of directory group nodes
 */
create table sp_directory_group (
    id bigint not null auto_increment,
    created_at bigint,
    created_by varchar(40),
    last_modified_at bigint,
    last_modified_by varchar(40),
    optlock_revision bigint,
    tenant varchar(40) not null,
    description varchar(512),
    name varchar(64) not null,
    directory_parent bigint,
    primary key (id)
);

alter table sp_directory_group
	add constraint fk_directory_parent
	foreign key (directory_parent)
	references sp_directory_group (id)
	on delete cascade;

/*
 * Adapt target to be able to assign them to groups
 */
alter table sp_target
    add directory_group bigint;

alter table sp_target
    add constraint fk_directory_group
    foreign key (directory_group)
    references sp_directory_group (id)
	on delete restrict;

/*
 * Add support closure table to allow for faster queries
 */
create table sp_directory_tree (
    ancestor bigint not null,
    descendant bigint not null,
    depth int,
    primary key (ancestor, descendant)
);

-- Delete cascades to closures as they are not necessary without group
alter table sp_directory_tree
	add constraint fk_group_ancestor
	foreign key (ancestor)
	references sp_directory_group (id)
	on delete cascade;

alter table sp_directory_tree
	add constraint fk_group_descendant
	foreign key (descendant)
	references sp_directory_group (id)
	on delete cascade;

/*
 * Procedures to ease closure table handling
 */
-- Add group to parent procedure
DELIMITER $

CREATE PROCEDURE p_group_node_add (
  param_group    bigint,
  param_parent   bigint
)
BEGIN
  -- Update group relationship information
  INSERT INTO sp_directory_tree (
    ancestor,
    descendant,
    depth
  )
  SELECT
    ancestor,
    param_group,
    depth + 1
  FROM
    sp_directory_tree
  WHERE descendant = param_parent
  UNION
  ALL
  -- Self relationship is needed to ease moving and adding of nodes
  SELECT
    param_group,
    param_group,
    0;
END$
DELIMITER ;

-- Move group to parent procedure
DELIMITER $

CREATE PROCEDURE p_group_node_move (
  param_group   bigint,
  param_parent  bigint
)
BEGIN
  -- Delete old relationships between moved group (gr) and old ancestors (par),
  -- as well as old ancestors (par) and the moved groups child nodes (sub)
  DELETE par
  FROM sp_directory_tree par
    JOIN sp_directory_tree sub
        ON sub.descendant = par.descendant
    LEFT JOIN sp_directory_tree gr
        ON gr.ancestor = sub.ancestor AND gr.descendant = par.ancestor
  WHERE
        sub.ancestor = param_group AND gr.ancestor IS NULL;

  -- Update group relationship information
  INSERT INTO sp_directory_tree (
    ancestor,
    descendant,
    depth
  )
  SELECT
    par.ancestor,
    sub.descendant,
    par.depth + sub.depth + 1
  FROM
    sp_directory_tree sub, sp_directory_tree par
  WHERE
      sub.ancestor = param_group
    AND
      par.descendant = param_parent;
END$
DELIMITER ;

-- Delete group from directory tree procedure
DELIMITER $

CREATE PROCEDURE p_group_node_delete (
  param_group   bigint
)
BEGIN
  -- Delete old relationships between deleted group (gr) and all ancestors (par)
  -- DELETE
  -- FROM sp_directory_tree
  -- WHERE descendant = param_group;
END$
DELIMITER ;