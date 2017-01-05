package ru.killer666.issuetimewatchdog.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum IssueState {
    Working("working now"), Idle("idle");

    @Getter
    private final String value;
}
