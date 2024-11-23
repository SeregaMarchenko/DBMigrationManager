# DBMigrationManager

DBMigrationManager is a utility for managing database migrations via the command line. It provides functionality for applying, rolling back, and reporting database migrations in a controlled and consistent manner.

## Requirements

- **Java 17** or higher
- **Maven 3.6.3** or higher
- **PostgreSQL 9.6** or higher

## Project Setup

1. **Clone the repository** or **copy the project** to your local machine:
    ```bash
    git clone <repository-url>
    cd DBMigrationManager
    ```

2. **Ensure you have Java and Maven installed**:
    ```bash
    java -version
    mvn -version
    ```

## Configuration

### Database Configuration

1. **Create a file named `application.properties`** in the `src/main/resources` directory.
2. **Add the following lines** and replace the placeholders (`<...>`) with your database connection details:

    ```properties
    db.url=jdbc:postgresql://<your-database-host>:<port>/<database-name>
    db.username=<your-database-username>
    db.password=<your-database-password>
    ```

### Creating Essential Tables

The application will automatically create the necessary tables (`migration_history` and `migration_lock`) if they do not exist.

## Building and Running the Project

1. **Build the project using Maven**:
    ```bash
    mvn clean package
    ```

2. **Run the project from the command line**:
    ```bash
    mvn exec:java -Dexec.mainClass="org.example.MigrationTool"
    ```

## Usage

Once the utility is running in the command line, you can use the following commands:

- `migrate` - Apply all pending migrations.
- `rollback <version>` - Rollback to the specified version.
- `rollback` - Rollback the most recent migration.
- `status` - Print the current migration status.
- `exit` - Exit the utility.

### Examples

```bash
Please enter a command: 'migrate', 'rollback <version>', 'rollback', 'status', or 'exit' to quit.
migrate
[INFO] ... migrations applied ...

Please enter a command: 'migrate', 'rollback <version>', 'rollback', 'status', or 'exit' to quit.
rollback 5
[INFO] ... rolled back to version 5 ...

Please enter a command: 'migrate', 'rollback <version>', 'rollback', 'status', or 'exit' to quit.
status
[INFO] Current Database Version: 10
[INFO] Applied Migrations:
- V10__Create_Roles_Table.sql
- V5__Create_Orders_Table.sql

Please enter a command: 'migrate', 'rollback <version>', 'rollback', 'status', or 'exit' to quit.
exit
Exiting...
```



## Detailed Information about Key Classes

### `MigrationService`
Manages the migration operations, including applying and rolling back migrations, and printing migration status.

### `RollbackExecutor`
Handles rolling back migrations to the most recent or a specified version and generates corresponding reports.

### `MigrationStatusPrinter`
Prints the current status of migrations including the current version and the list of applied migrations.

### `MigrationReportService`
Generates JSON reports for the applied and rolled back migrations.

### `MigrationHistoryService`
Manages the migration history records in the database, including retrieving, recording, and removing migrations.

### `MigrationLockService`
Manages the migration locks in the database to prevent concurrent migrations.

### `EssentialTableCreator`
Creates essential tables (`migration_history` and `migration_lock`) if they do not exist.

### `ConnectionManager`
Utility class for managing database connections.

### `MigrationFileReader`
Utility class for reading migration files from the specified directory.

### `MigrationRollbackGenerator`
Generates rollback SQL scripts for migrations.

## Error Handling

The application logs all errors and exceptions.

In the case of a failure during migration or rollback, the transaction is rolled back and the lock is released.

## Troubleshooting

### Common Issues

1. **Error: `java.nio.file.NoSuchFileException`**:
   - Ensure the migration files exist in the correct directory.
   - Verify the path to the files and check for any typos.
   - Ensure your application has the necessary permissions to read the files.

2. **Database Connection Issues**:
   - Verify the database URL, username, and password in `application.properties`.
   - Ensure the database server is running and accessible.

3. **Java and Maven Installation Issues**:
   - Ensure that Java and Maven are correctly installed and their paths are set in the environment variables.
   - Run `java -version` and `mvn -version` to check their installations.