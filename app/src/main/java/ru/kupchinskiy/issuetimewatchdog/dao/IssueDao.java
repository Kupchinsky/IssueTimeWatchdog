package ru.kupchinskiy.issuetimewatchdog.dao;

import java.util.List;

import ru.kupchinskiy.issuetimewatchdog.model.Issue;

public interface IssueDao extends RuntimeExceptionDao<Issue, Integer> {

    List<Issue> queryNotAutoRemove();

    Issue queryWorkingState();

    boolean trackorKeyExists(String trackorKey);

}
