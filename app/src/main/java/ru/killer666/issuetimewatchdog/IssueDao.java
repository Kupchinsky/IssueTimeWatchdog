package ru.killer666.issuetimewatchdog;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;

public class IssueDao extends RuntimeExceptionDao<Issue, Integer> {
    public IssueDao(Dao<Issue, Integer> dao) {
        super(dao);
    }
}
