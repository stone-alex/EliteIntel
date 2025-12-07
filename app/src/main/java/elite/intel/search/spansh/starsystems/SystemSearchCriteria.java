package elite.intel.search.spansh.starsystems;

import com.google.gson.annotations.SerializedName;
import elite.intel.gameapi.gamestate.dtos.BaseJsonDto;
import elite.intel.util.json.ToJsonConvertible;

import java.util.Collections;
import java.util.List;

public class SystemSearchCriteria extends BaseJsonDto implements ToJsonConvertible {

    @SerializedName("filters")
    private Filters filters;

    @SerializedName("sort")
    private List<Object> sort = Collections.emptyList();

    @SerializedName("size")
    private int size = 10;

    @SerializedName("page")
    private int page = 0;

    public SystemSearchCriteria() {
        // default constructor for Gson
    }

    public Filters getFilters() {
        return filters;
    }

    public void setFilters(Filters filters) {
        this.filters = filters;
    }

    public List<Object> getSort() {
        return sort;
    }

    public void setSort(List<Object> sort) {
        this.sort = sort != null ? sort : Collections.emptyList();
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public static class Filters {
        @SerializedName("system_name")
        private SystemNameFilter systemName;

        public SystemNameFilter getSystemName() {
            return systemName;
        }

        public void setSystemName(SystemNameFilter systemName) {
            this.systemName = systemName;
        }
    }

    public static class SystemNameFilter {
        @SerializedName("value")
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}