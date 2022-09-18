package com.mouken.settings.form;

import lombok.Data;

@Data
public class Notifications {

    private boolean partyCreatedByEmail;

    private boolean partyCreatedByWeb;

    private boolean partyEnrollmentResultByEmail;

    private boolean partyEnrollmentResultByWeb;

    private boolean partyUpdatedByEmail;

    private boolean partyUpdatedByWeb;
}