package com.bandmatch.bandmatch.domain.interaction;

import java.time.LocalDateTime;

public interface MusicInteraction {
    void send();
    void respond(boolean isAccepted);
    InteractionStatus getStatus();
    LocalDateTime getTimestamp();
}
