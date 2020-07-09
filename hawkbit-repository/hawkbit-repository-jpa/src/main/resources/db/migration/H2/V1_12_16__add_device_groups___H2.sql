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

-- Only this constraint is necessary as only groups without descendants can be deleted
alter table sp_directory_tree
    add constraint fk_group_descendant
    foreign key (descendant)
    references sp_directory_group (id)
    on delete cascade;

/*
 * Procedures to ease closure table handling
 */
-- Add group to parent procedure
CREATE ALIAS p_group_node_add AS $$
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
@CODE
void p_group_node_add(final Connection conn, final Long param_group, final Long param_parent) throws SQLException{
    StringBuffer sql = new StringBuffer();
    sql.append("INSERT INTO sp_directory_tree (ancestor, descendant, depth) ");
    sql.append("SELECT ancestor, " + param_group + ", depth + 1 FROM sp_directory_tree WHERE descendant = " + param_parent + " ");
    sql.append("UNION ALL ");
    sql.append("SELECT " + param_group + ", " + param_group + ", 0 ");
    PreparedStatement statement = conn.prepareStatement(sql.toString());
    statement.execute();
}
$$;

-- Move group to parent procedure
CREATE ALIAS p_group_node_move AS $$
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
@CODE
void p_group_node_move(final Connection conn, final Long param_group, final Long param_parent) throws SQLException{
    // Delete old relationships between moved group (gr) and old ancestors (par),
    // as well as old ancestors (par) and the moved groups child nodes (sub)
    StringBuffer delSql = new StringBuffer();
    delSql.append("DELETE FROM sp_directory_tree ");
    delSql.append("WHERE (ancestor, descendant) IN ( ");
    delSql.append("SELECT par.ancestor, par.descendant FROM sp_directory_tree par ");

    delSql.append("JOIN sp_directory_tree sub ON sub.descendant = par.descendant ");
    delSql.append("LEFT JOIN sp_directory_tree gr ON gr.ancestor = sub.ancestor AND gr.descendant = par.ancestor ");
    delSql.append("WHERE sub.ancestor = " + param_group + " AND gr.ancestor IS NULL)");
    PreparedStatement delStatement = conn.prepareStatement(delSql.toString());
    delStatement.execute();

    // Update group relationship information
    StringBuffer insSql = new StringBuffer();
    insSql.append("INSERT INTO sp_directory_tree (ancestor, descendant, depth) ");
    insSql.append("SELECT par.ancestor, sub.descendant, par.depth + sub.depth + 1 ");
    insSql.append("FROM sp_directory_tree sub, sp_directory_tree par ");
    insSql.append("WHERE sub.ancestor = " + param_group + " AND par.descendant = "+ param_parent);
    PreparedStatement insStatement = conn.prepareStatement(insSql.toString());
    insStatement.execute();
}
$$;