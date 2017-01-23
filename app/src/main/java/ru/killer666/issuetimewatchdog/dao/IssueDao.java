package ru.killer666.issuetimewatchdog.dao;

import java.util.List;

import ru.killer666.issuetimewatchdog.model.Issue;

public interface IssueDao extends RuntimeExceptionDao<Issue, Integer> {

    List<Issue> queryNotAutoRemove();

    Issue queryWorkingState();

    boolean trackorKeyExists(String trackorKey);

}
