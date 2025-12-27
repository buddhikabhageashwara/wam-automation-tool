package wam.automationtool.domain.entity.testcasestep.alias;

import lombok.Getter;

@Getter
public enum AliasType {

  AGENT_EXECUTION("AGENT_EXECUTION"),
  SERVER_LOG("SERVER_LOG"),
  CACHED_DATA("CACHED_DATA"),
  MYSQL_DB_CONNECTION("MYSQL_DB_CONNECTION"),
  MONGO_DB_CONNECTION("MONGO_DB_CONNECTION");

  private final String id;

  AliasType(final String id) {
    this.id = id;
  }

  public static boolean isNotExist(final String aliasTypeId) {
    for (final AliasType type : values()) {
      if (type.getId().equals(aliasTypeId)) {
        return false;
      }
    }
    return true;
  }

}
