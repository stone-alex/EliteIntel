package test.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

import elite.intel.ai.brain.handlers.commands.Bindings.GameCommand;

@RegisterRowMapper(BindingsDao.KeyBindingMapper.class)
public interface BindingsDao {


	@SqlUpdate("INSERT OR REPLACE INTO missing_key_bindings (command_name) VALUES (:command)")
	KeyBinding addMisingBinding(@BindBean BindingsDao.KeyBindingMapper data);

	@SqlQuery("SELECT * FROM missing_key_bindings WHERE id = :id")
	KeyBinding getNextBinding(@BindBean("id") Integer id);

	@SqlQuery("SELECT * FROM missing_key_bindings WHERE command_name = :command")
	KeyBinding getBinding(@BindBean("command") String command);

	@SqlUpdate("DELETE FROM missing_key_bindings WHERE command_name = :command")
	void deleteBinding(@BindBean("command") String command);

	@SqlUpdate("DELETE FROM missing_key_bindings WHERE command_name = :command")
	void deleteKeybinding(@BindBean("command") String command);

	class KeyBindingMapper implements RowMapper<KeyBinding> {
		@Override public KeyBinding map(ResultSet rs, StatementContext ctx) throws SQLException {
			KeyBinding key = new KeyBinding();
			key.setCommand(rs.getString("command"));
			return key;
		}
	}

	class KeyBinding {
		
		public KeyBinding() {}
		
		private GameCommand command;

		public KeyBinding(){}
		
		public getCommand(){
			return this.command;
		}

		public setCommand(String command) {
			this.command = (GameCommand) command;
		}

	}
}