# PostgreSQL configuration

The BI JDBC layer accepts PostgreSQL JDBC URLs in the existing ERP setup
configuration. No separate connection mechanism is required.

```ini
databaseLabel=erp-postgresql
databaseString=jdbc:postgresql://db-host:5432/erp_database
databaseLogin=erp_user
databasePassword=change-me
databaseSchema=public
databaseCatalog=erp_database
databasePoolCnt=5
databaseMaxPoolCnt=50
```

The PostgreSQL JDBC driver is already declared by `bi/pom.xml`. Credentials can
also be supplied through the existing deployment-specific configuration rather
than committed to source control.

## Compatibility notes

- Existing ERP metadata tables such as `ddd_table` and `dddviewrptcols` must be
  present in PostgreSQL before `BiSchema` can load the ERP model.
- Unquoted PostgreSQL identifiers are folded to lowercase. Existing table and
  column names should therefore remain lowercase, or SQL must quote them
  consistently.
- Database-generated record IDs should use an identity/sequence-backed column
  with a default value. `SelectUtil.insertByTableRec` uses `DEFAULT` for that
  column and reads the generated key back.
- SQL stored in ERP view and report definitions must use PostgreSQL-compatible
  syntax. MySQL-specific functions or backtick-quoted identifiers require
  migration even though the JDBC access layer supports PostgreSQL.
