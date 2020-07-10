/*
 * Definition of directory group nodes
 */
create table sp_directory_group (
    id BIGSERIAL NOT NULL,
    created_at BIGINT,
    created_by VARCHAR(40),
    last_modified_at BIGINT,
    last_modified_by VARCHAR(40),
    optlock_revision BIGINT,
    tenant VARCHAR(40) NOT NULL,
    description VARCHAR(512),
    name VARCHAR(64) NOT NULL,
    directory_parent BIGINT,
    PRIMARY KEY (id)
);

alter table sp_directory_group
    ADD CONSTRAINT fk_directory_parent
    FOREIGN KEY (directory_parent)
    REFERENCES sp_directory_group (id)
    ON DELETE CASCADE;

/*
 * Adapt target to be able to assign them to groups
 */
alter table sp_target
    add directory_group BIGINT;

alter table sp_target
    ADD CONSTRAINT fk_directory_group
    FOREIGN KEY (directory_group)
    REFERENCES sp_directory_group (id)
	ON DELETE RESTRICT;

/*
 * Add support closure table to allow for faster queries
 */
create table sp_directory_tree (
    ancestor BIGINT NOT NULL,
    descendant BIGINT NOT NULL,
    depth int,
    PRIMARY KEY (ancestor, descendant),
    FOREIGN KEY (descendant) REFERENCES sp_directory_group (id) ON DELETE CASCADE
);

-- Delete cascades to closures as they are not necessary without group
alter table sp_directory_tree
    ADD CONSTRAINT fk_group_ancestor
    FOREIGN KEY (ancestor)
    REFERENCES sp_directory_group (id)
    ON DELETE CASCADE;

alter table sp_directory_tree
    ADD CONSTRAINT fk_group_descendant
    FOREIGN KEY (descendant)
    REFERENCES sp_directory_group (id)
    ON DELETE CASCADE;

/*
 * Procedures to ease closure table handling
 */
-- Add group to parent procedure
CREATE FUNCTION p_directory_tree_add (
  param_group    BIGINT,
  param_parent   BIGINT
) RETURNS BIGINT
LANGUAGE plpgsql
AS $$
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
  RETURN param_parent;
END;
$$;


-- Move group to parent procedure
CREATE FUNCTION p_directory_tree_move (
  param_group   BIGINT,
  param_parent  BIGINT
) RETURNS BIGINT
LANGUAGE plpgsql
AS $$
BEGIN
  -- Delete old relationships between moved group (gr) and old ancestors (par),
  -- as well as old ancestors (par) and the moved groups child nodes (sub)
  DELETE FROM sp_directory_tree
    WHERE (ancestor, descendant) IN (
      SELECT par.ancestor, par.descendant
      FROM sp_directory_tree par
        JOIN sp_directory_tree sub
          ON sub.descendant = par.descendant
        LEFT JOIN sp_directory_tree gr
          ON gr.ancestor = sub.ancestor AND gr.descendant = par.ancestor
      WHERE
        sub.ancestor = param_group AND gr.ancestor IS NULL
  );

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
  RETURN param_parent;
END;
$$;