/*
 * Definition of directory group nodes
 */
create table sp_directory_group (
    id NUMERIC(19) IDENTITY NOT NULL,
    created_at NUMERIC(19),
    created_by varchar(40),
    last_modified_at NUMERIC(19),
    last_modified_by varchar(40),
    optlock_revision INTEGER NULL,
    tenant varchar(40) not null,
    description varchar(512),
    name varchar(64) not null,
    directory_parent NUMERIC(19),
    primary key (id)
);

-- MS SQL cannot handle possible loops in cascading delete, it is therefore handled by trigger
alter table sp_directory_group
	add constraint fk_directory_parent
	foreign key (directory_parent)
	references sp_directory_group (id)
    on delete no action;

/*
 * Adapt target to be able to assign them to groups
 */
alter table sp_target
    add directory_group NUMERIC(19);

alter table sp_target
    add constraint fk_directory_group
    foreign key (directory_group)
    references sp_directory_group (id)
	on delete no action;

/*
 * Add support closure table to allow for faster queries
 */
create table sp_directory_tree (
    ancestor NUMERIC(19) not null,
    descendant NUMERIC(19) not null,
    depth int,
    primary key (ancestor, descendant)
);

-- Delete cascades to closures as they are not necessary without group
-- MS SQL cannot handle possible loops in cascading delete, it is therefore handled by trigger
alter table sp_directory_tree
    add constraint fk_group_ancestor
    foreign key (ancestor)
    references sp_directory_group (id)
    on delete no action;

alter table sp_directory_tree
	add constraint fk_group_descendant
	foreign key (descendant)
	references sp_directory_group (id)
    on delete cascade;
GO

/*
 * Procedures to ease closure table handling
 */
-- Add group to parent procedure
CREATE PROCEDURE p_group_node_add
  @param_group    NUMERIC(19),
  @param_parent   NUMERIC(19)
AS
BEGIN
  -- Update group relationship information
  INSERT INTO sp_directory_tree (
    ancestor,
    descendant,
    depth
  )
  SELECT
    ancestor,
    @param_group,
    depth + 1
  FROM
    sp_directory_tree
  WHERE descendant = @param_parent
  UNION
  ALL
  -- Self relationship is needed to ease moving and adding of nodes
  SELECT
    @param_group,
    @param_group,
    0
END
GO

-- Move group to parent procedure
CREATE PROCEDURE p_group_node_move
  @param_group   NUMERIC(19),
  @param_parent  NUMERIC(19)
AS
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
        sub.ancestor = @param_group AND gr.ancestor IS NULL

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
      sub.ancestor = @param_group
    AND
      par.descendant = @param_parent
END
GO

-- MS SQL needs additional trigger as it cannot handle (possible) looped references during delete
CREATE TRIGGER t_delete_directory_hierarchy
    ON sp_directory_group
    INSTEAD OF DELETE
    AS
BEGIN
    SET NOCOUNT ON;
    DELETE FROM sp_directory_group WHERE id IN (SELECT t.descendant FROM sp_directory_tree t, DELETED d WHERE d.id = t.ancestor);
    DELETE FROM sp_directory_tree WHERE ancestor IN (SELECT id FROM DELETED)
END
GO