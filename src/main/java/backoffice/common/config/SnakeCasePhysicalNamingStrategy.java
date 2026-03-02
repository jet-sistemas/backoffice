package backoffice.common.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class SnakeCasePhysicalNamingStrategy extends PhysicalNamingStrategyStandardImpl {

  @Override
  public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
    return new Identifier(toSnakeCase(name.getText()), name.isQuoted());
  }

  private String toSnakeCase(String text) {
    return text.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
  }
}
