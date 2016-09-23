package ru.killer666.issuetimewatchdog;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
enum IssueState {
    Working("working now"), Idle("idle");

    @Getter
    private final String value;
}
