package ru.kupchinskiy.issuetimewatchdog.helper;

import android.database.sqlite.SQLiteDatabase;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.slf4j.Logger;

import java.sql.SQLException;

import ru.kupchinskiy.issuetimewatchdog.model.Issue;
import ru.kupchinskiy.issuetimewatchdog.model.TimeRecord;
import ru.kupchinskiy.issuetimewatchdog.model.TimeRecordLog;

@Singleton
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static Logger logger;

    private static final String DATABASE_NAME = "application.db";
    private static final int DATABASE_VERSION = 1;

    @Inject
    public DatabaseHelper(android.app.Application application) {
        super(application, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Issue.class);
            TableUtils.createTable(connectionSource, TimeRecord.class);
            TableUtils.createTable(connectionSource, TimeRecordLog.class);

            logger.info("Database created successfully");
        } catch (SQLException e) {
            logger.error("Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
    }

}
