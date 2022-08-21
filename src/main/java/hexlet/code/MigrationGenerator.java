package hexlet.code;

import io.ebean.annotation.Platform;
import io.ebean.dbmigration.DbMigration;

import java.io.IOException;
import java.util.Objects;

public final class MigrationGenerator {

    public static void main(String[] args) throws IOException {
        // Создаём миграцию
        DbMigration dbMigration = DbMigration.create();
        // Указываем платформу, в нашем случае H2
        String env = System.getenv("APP_ENV");
        if (Objects.equals(env, "production")) {
            dbMigration.addPlatform(Platform.POSTGRES, "postgres");
        } else  {
            dbMigration.addPlatform(Platform.H2, "h2");
        }
        // Генерируем миграцию
        dbMigration.generateMigration();
    }
}
