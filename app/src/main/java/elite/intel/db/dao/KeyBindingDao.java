package elite.intel.db.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RegisterRowMapper(KeyBindingDao.KeyBindingMapper.class)
public interface KeyBindingDao {
	
	@SqlQuery("""
        INSERT OR UPDATE INTO bindings (key_binding)
        VALUES(:binding)
        """)
    void addBinding(@BindBean KeyBindingDao.KeyBinding binding);
    
    @SqlUpdate(""" 
        DELETE * FROM bindings
        WHERE key_binding = :binding
        """)
    void removeBinding(@BindBean KeyBindingDao.KeyBinding binding);

    @SqlUpdate("DELETE * FROM bindings")
    void clear();

    @SqlQuery("SELECT * FROM bindings")
    List<KeyBinding> getBindings();


    class KeyBindingMapper implements RowMapper<KeyBindingDao.KeyBinding> {

        @Override public KeyBinding map(ResultSet rs, StatementContext ctx) throws SQLException {
            KeyBinding binding = new KeyBinding();
            binding.setKeyBinding(rs.getString("key_binding"));
            binding.setId(rs.getInt("id"));
            return binding;
        }
    }

    class KeyBinding {
    	private Integer id;
    	private String keyBinding;

    	public KeyBinding() {
    	}

    	public String getKeyBinding() {
    		return keyBinding;
    	}

    	public void setKeyBinding(String keyBinding) {
    		this.keyBinding = keyBinding;
    	}

    	public Integer getId() {
    		return id;
    	}

    	public void setId(Integer id) {
    		this.id = id;
    	}

    }
}