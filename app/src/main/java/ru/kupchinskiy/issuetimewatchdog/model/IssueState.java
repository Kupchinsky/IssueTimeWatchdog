package ru.kupchinskiy.issuetimewatchdog.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum IssueState {

    Working("Working now"), Idle("Idle");

    @Getter
    private final String value;

}
